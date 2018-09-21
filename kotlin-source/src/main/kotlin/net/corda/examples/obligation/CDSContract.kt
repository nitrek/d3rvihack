package net.corda.examples.obligation

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class CDSContract : Contract {

    companion object {
        @JvmStatic
        val OBLIGATION_CONTRACT_ID = "net.corda.examples.obligation.CDSContract"
    }

    interface Commands : CommandData {
        class FullTerminationInitiation : TypeOnlyCommandData(), Commands
        class FullTerminationAcceptance : TypeOnlyCommandData(), Commands
    }

    override fun verify(tx: LedgerTransaction): Unit {
        val command = tx.commands.requireSingleCommand<Commands>()
        val setOfSigners = command.signers.toSet()
        when (command.value) {
            is Commands.FullTerminationInitiation -> verifyFullTerminationInitiation(tx, setOfSigners)
            is Commands.FullTerminationAcceptance -> verifyFullTerminationAcceptance(tx, setOfSigners)
            else -> throw IllegalArgumentException("Unrecognised command.")
        }
    }


    // This only allows one obligation issuance per transaction.
    private fun verifyFullTerminationInitiation(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        "No inputs should be consumed when issuing an obligation." using (tx.inputStates.size ==1)
        "Only one obligation state should be created when issuing an obligation." using (tx.outputStates.size == 1)

        val input = tx.inputsOfType<CreditDefaultSwap>().single()
        val output = tx.outputsOfType<CreditDefaultSwap>().single()
        "Fresh Termination, no previous initation" using (input.cdsTermination.status.equals("NEWTRADE"))
        "Output Status should be Termination Initiated" using(output.cdsTermination.status.equals("TERMIN"))

    }

    private fun verifyFullTerminationAcceptance(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        "No inputs should be consumed when issuing an obligation." using (tx.inputStates.size ==1)
        "Only one obligation state should be created when issuing an obligation." using (tx.outputStates.size == 1)

        val input = tx.inputsOfType<CreditDefaultSwap>().single()
        val output = tx.outputsOfType<CreditDefaultSwap>().single()
        "Fresh Termination, no previous initation" using (input.cdsTermination.status.equals("TERMIN"))
        "Output Status should be Termination Initiated" using(output.cdsTermination.status.equals("TERMINATED"))
        "Notional Value set to 0" using (output.protectionTerms.notionalAmount.equals("0"))

    }
}