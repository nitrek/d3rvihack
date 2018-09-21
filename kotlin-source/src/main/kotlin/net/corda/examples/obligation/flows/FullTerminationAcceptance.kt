package net.corda.examples.obligation.flows

import co.paralleluniverse.fibers.Suspendable
import com.google.common.collect.ImmutableList
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import net.corda.core.utilities.seconds
import net.corda.examples.obligation.CDSContract
import net.corda.examples.obligation.CDSContract.Companion.CDS_CONTRACT_ID
import net.corda.examples.obligation.CreditDefaultSwap
import java.util.*

object FullTerminationAccceptance{
    @InitiatingFlow
    @StartableByRPC
    class Initiator(private val contractId: String, val acceptedBy: Party) : FlowLogic<SignedTransaction>() {

        companion object {
            object INITIALISING : Step("Performing initial steps.")
            object BUILDING : Step("Building and verifying transaction.")
            object SIGNING : Step("Signing transaction.")
            object COLLECTING : Step("Collecting counterparty signature.") {
                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
            }
            object FINALISING : Step("Finalising transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(INITIALISING, BUILDING, SIGNING, COLLECTING, FINALISING)
        }

        override val progressTracker: ProgressTracker = tracker()

        @Suspendable
        override fun call(): SignedTransaction {
            // Step 1. Initialisation.
            progressTracker.currentStep = INITIALISING
            val firstNotary = serviceHub.networkMapCache.notaryIdentities.firstOrNull()?: throw FlowException("No available notary.")
            val aString = "JUST_A_TEST_STRING"
            val result = UUID.nameUUIDFromBytes(aString.toByteArray())
            val cdsToTerminate = getObligationByLinearId(UniqueIdentifier(contractId,result))
            val cdsTerminateOutput = cdsToTerminate;


            if(cdsToTerminate.state.data.cdsTermination.counterParty != acceptedBy){
                throw FlowException("Acceptance of Termination has to be done by the counterParty.")
            }
            cdsTerminateOutput.state.data.setNotional("0",cdsTerminateOutput.state.data.protectionTerms.currency)
            // Step 2. Building.
            progressTracker.currentStep = BUILDING
            val utx = TransactionBuilder(firstNotary)
                    .addInputState(cdsToTerminate)
                    .addOutputState(cdsTerminateOutput.state.data, CDS_CONTRACT_ID)
                    .addCommand(CDSContract.Commands.FullTerminationInitiation(), cdsToTerminate.state.data.participants.map { it.owningKey })
                    .setTimeWindow(serviceHub.clock.instant(), 30.seconds)

            // Step 3. Sign the transaction.
            progressTracker.currentStep = SIGNING
            val ptx = serviceHub.signInitialTransaction(utx, acceptedBy.owningKey)

            // Step 4. Get the counter-party signature.
            progressTracker.currentStep = COLLECTING
            val lenderFlow = initiateFlow(cdsTerminateOutput.state.data.cdsTermination.initiatedBy!!)
            val stx = subFlow(CollectSignaturesFlow(
                    ptx,
                    setOf(lenderFlow),
                    listOf(acceptedBy.owningKey),
                    COLLECTING.childProgressTracker())
            )

            // Step 5. Finalise the transaction.
            progressTracker.currentStep = FINALISING
            return subFlow(FinalityFlow(stx, FINALISING.childProgressTracker()))
        }

        fun getObligationByLinearId(linearId: UniqueIdentifier): StateAndRef<CreditDefaultSwap> {
            val queryCriteria = QueryCriteria.LinearStateQueryCriteria(
                    null,
                    ImmutableList.of(linearId),
                    Vault.StateStatus.UNCONSUMED, null)

            return serviceHub.vaultService.queryBy<CreditDefaultSwap>(queryCriteria).states.singleOrNull()
                    ?: throw FlowException("Obligation with id $linearId not found.")
        }
    }

    @InitiatedBy(Initiator::class)
    class Responder(private val otherFlow: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val stx = subFlow(SignTxFlowNoChecking(otherFlow))
            return waitForLedgerCommit(stx.id)
        }
    }
}
internal class SignTxFlowNoChecking(otherFlow: FlowSession) : SignTransactionFlow(otherFlow) {
    override fun checkTransaction(tx: SignedTransaction) {
        // TODO: Add checking here.
    }
}



