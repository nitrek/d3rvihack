package net.corda.examples.obligation

import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper
import net.corda.core.contracts.Amount
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.OpaqueBytes
import net.corda.core.utilities.getOrThrow
import net.corda.examples.obligation.flows.IssueIrsFixedFloatDeal
import net.corda.examples.obligation.flows.IssueIrsFloatFloatDeal
import net.corda.examples.obligation.models.*
import net.corda.finance.contracts.asset.Cash
import net.corda.finance.contracts.getCashBalances
import net.corda.finance.flows.CashIssueFlow
import org.isda.cdm.Event
import java.util.*
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.core.Response.Status.CREATED

@Path("obligation")
class API(val rpcOps: CordaRPCOps) {

    private val myIdentity = rpcOps.nodeInfo().legalIdentities.first()

    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    fun me() = mapOf("me" to myIdentity)

    @GET
    @Path("peers")
    @Produces(MediaType.APPLICATION_JSON)
    fun peers() = mapOf("peers" to rpcOps.networkMapSnapshot()
            .filter { nodeInfo -> nodeInfo.legalIdentities.first() != myIdentity }
            .map { it.legalIdentities.first().name.organisation })

    @GET
    @Path("get-irs-fixed-float")
    @Produces(MediaType.APPLICATION_JSON)
    fun getirsdeals() = rpcOps.vaultQuery(FixedFloatIRS::class.java).states

    @GET
    @Path("get-irs-float-float")
    @Produces(MediaType.APPLICATION_JSON)
    fun getirsdealsFF() = rpcOps.vaultQuery(FloatFloatIRS::class.java).states



    //my api
    @POST
    @Path("irs-create-deal")
    fun createDeal(payload:String): Response {
        // 1. Get party objects for the counterparty.
        val event = RosettaObjectMapper.getDefaultRosettaObjectMapper().readValue(payload, Event::class.java)
        val partyIdentity = rpcOps.partiesFromName(event.party.get(1).legalEntity.name, exactMatch = false).singleOrNull()
               ?: throw IllegalStateException("Couldn't lookup node identity for "+event.party.get(1).legalEntity.name)
       
        val contract = event.primitive.newTrade.get(0).contract
        val interestRatePayout = contract.contractualProduct.economicTerms.payout.interestRatePayout
        val acc1 = AccountDetails(contract.account.get(0).accountNumber, contract.account.get(0).servicingParty)
        val acc2 = AccountDetails(contract.account.get(1).accountNumber,contract.account.get(1).servicingParty)
        val basicInfo = IRSBasicInfo(contract.tradeDate.adjustableDate.unadjustedDate.toString(),"event.primitive.newTrade.get(0).contractReference.identifierValue.identifier.toString()",listOf(acc1), listOf(acc1))
        //val quantity = Notional("70000000.0", "EUR")
        val paymentFrequency = PaymentFrequency("Y", 1)
        val effictiveDate = CalculationPeriodDateReference(listOf("EUTA"), "Following", "2018-09-26")
        val terminationDate = CalculationPeriodDateReference(listOf("EUTA"), "Following", "2019-09-26")
        val paymentCalander = CalculationPeriodDateReference(listOf("EULA"), "Following", "2018-09-26")
        val paymentFrequencyRateIndex = PaymentFrequency("M", 6)
        val floatingRateIndex = FloatingRateIndex( "0.003000", "0.026587")
        var floatIndex =0;
        var fixedIndex =1;
        var type = "float"
        if(interestRatePayout.get(1).interestRate.fixedRate==null) {
            floatIndex =1;
            fixedIndex =0;
            type="fixed"
        }
        val floatingLeg = FloatingLeg(interestRatePayout.get(floatIndex).payerReceiver.payerPartyReference, interestRatePayout.get(floatIndex).payerReceiver.receiverPartyReference,
                Notional(interestRatePayout.get(floatIndex).quantity.notionalSchedule.notionalStepSchedule.initialValue.toString(),
                        interestRatePayout.get(floatIndex).quantity.notionalSchedule.notionalStepSchedule.currency.toString()),
                PaymentFrequency(interestRatePayout.get(floatIndex).paymentDates.paymentFrequency.period.toString(),interestRatePayout.get(floatIndex).paymentDates.paymentFrequency.periodMultiplier),
                CalculationPeriodDateReference(listOf(interestRatePayout.get(floatIndex).calculationPeriodDates.effectiveDate.adjustableDate.dateAdjustments.businessCenters.toString()),
                        interestRatePayout.get(floatIndex).calculationPeriodDates.effectiveDate.adjustableDate.dateAdjustments.businessDayConvention.toString(),
                        interestRatePayout.get(floatIndex).calculationPeriodDates.effectiveDate.adjustableDate.unadjustedDate.toString()),
                CalculationPeriodDateReference(listOf(interestRatePayout.get(floatIndex).calculationPeriodDates.terminationDate.dateAdjustments.businessCenters.toString()),
                        interestRatePayout.get(floatIndex).calculationPeriodDates.terminationDate.dateAdjustments.businessDayConvention.toString(),
                        interestRatePayout.get(floatIndex).calculationPeriodDates.terminationDate.unadjustedDate.toString())
                , interestRatePayout.get(floatIndex).dayCountFraction.toString(),

                CalculationPeriodDateReference(listOf(interestRatePayout.get(floatIndex).paymentDates.paymentDatesAdjustments.businessCenters.toString()),
                        interestRatePayout.get(floatIndex).paymentDates.paymentDatesAdjustments.businessDayConvention.toString(),
                        ""), interestRatePayout.get(floatIndex).resetDates.toString(),
                FloatingRateIndex(interestRatePayout.get(floatIndex).interestRate.floatingRate.spreadSchedule.get(0).initialValue.toString(),
                        interestRatePayout.get(floatIndex).interestRate.floatingRate.initialRate.toString()))


    val fixedLeg = FixedLeg(interestRatePayout.get(fixedIndex).payerReceiver.payerPartyReference, interestRatePayout.get(fixedIndex).payerReceiver.receiverPartyReference,
            Notional(interestRatePayout.get(fixedIndex).quantity.notionalSchedule.notionalStepSchedule.initialValue.toString(),
                    interestRatePayout.get(fixedIndex).quantity.notionalSchedule.notionalStepSchedule.currency.toString()),
            PaymentFrequency(interestRatePayout.get(fixedIndex).paymentDates.paymentFrequency.period.toString(),interestRatePayout.get(fixedIndex).paymentDates.paymentFrequency.periodMultiplier),
            CalculationPeriodDateReference(listOf(interestRatePayout.get(fixedIndex).calculationPeriodDates.effectiveDate.adjustableDate.dateAdjustments.businessCenters.toString()),
                    interestRatePayout.get(fixedIndex).calculationPeriodDates.effectiveDate.adjustableDate.dateAdjustments.businessDayConvention.toString(),
                    interestRatePayout.get(fixedIndex).calculationPeriodDates.effectiveDate.adjustableDate.unadjustedDate.toString()),
            CalculationPeriodDateReference(listOf(interestRatePayout.get(fixedIndex).calculationPeriodDates.terminationDate.dateAdjustments.businessCenters.toString()),
                    interestRatePayout.get(fixedIndex).calculationPeriodDates.terminationDate.dateAdjustments.businessDayConvention.toString(),
                    interestRatePayout.get(fixedIndex).calculationPeriodDates.terminationDate.unadjustedDate.toString())
            , interestRatePayout.get(fixedIndex).dayCountFraction.toString(),
            CalculationPeriodDateReference(listOf(interestRatePayout.get(fixedIndex).paymentDates.paymentDatesAdjustments.businessCenters.toString()),
                    interestRatePayout.get(fixedIndex).paymentDates.paymentDatesAdjustments.businessDayConvention.toString(),
                    ""),
            interestRatePayout.get(fixedIndex).interestRate.fixedRate.initialValue.toString()
            )
            
            var fixedFloatIRS:FixedFloatIRS
        var fixedLegBool = true
        System.out.println("legs created")
        if(type.equals("fixed",true))
        {
            fixedFloatIRS = net.corda.examples.obligation.FixedFloatIRS(basicInfo, fixedLeg, floatingLeg, myIdentity,partyIdentity)
        }
    else{
            fixedFloatIRS = net.corda.examples.obligation.FixedFloatIRS(basicInfo, fixedLeg, floatingLeg,partyIdentity, myIdentity)
            fixedLegBool = false
        }
        // 3. Start the IssueObligation flow. We block and wait for the flow to return.
        val (status, message) = try {
            val flowHandle = rpcOps.startFlowDynamic(
                    IssueIrsFixedFloatDeal.Initiator::class.java,
                    fixedFloatIRS,
                    partyIdentity,
                    myIdentity
            )

            val result = flowHandle.use { it.returnValue.getOrThrow() }
            CREATED to "Transaction id ${result.id} committed to ledger.\n${result.tx.outputs.single().data}"
        } catch (e: Exception) {
        e.printStackTrace()
            BAD_REQUEST to e.message
        }

        // 4. Return the result.
        return Response.status(status).entity(message).build()
    }

    @POST
    @Path("irs-create-deal-float")
    fun createDealFloat(payload:String): Response {
        // 1. Get party objects for the counterparty.
         val event = RosettaObjectMapper.getDefaultRosettaObjectMapper().readValue(payload, Event::class.java)
        val partyIdentity = rpcOps.partiesFromName(event.party.get(1).legalEntity.name, exactMatch = false).singleOrNull()
                ?: throw IllegalStateException("Couldn't lookup node identity for "+event.party.get(1).legalEntity.name)
        //val event = RosettaObjectMapper.getDefaultRosettaObjectMapper().readValue(payload, Event::class.java)
        val contract = event.primitive.newTrade.get(0).contract
        val interestRatePayout = contract.contractualProduct.economicTerms.payout.interestRatePayout
        val acc1 = AccountDetails(contract.account.get(0).accountNumber, contract.account.get(0).servicingParty)
        val acc2 = AccountDetails(contract.account.get(1).accountNumber,contract.account.get(1).servicingParty)
        val basicInfo = IRSBasicInfo(contract.tradeDate.adjustableDate.unadjustedDate.toString(),"event.primitive.newTrade.get(0).contractReference.identifierValue.identifier.toString()",listOf(acc1), listOf(acc1))
        //val quantity = Notional("70000000.0", "EUR")
        val paymentFrequency = PaymentFrequency("Y", 1)
        val effictiveDate = CalculationPeriodDateReference(listOf("EUTA"), "Following", "2018-09-26")
        val terminationDate = CalculationPeriodDateReference(listOf("EUTA"), "Following", "2019-09-26")
        val paymentCalander = CalculationPeriodDateReference(listOf("EULA"), "Following", "2018-09-26")
        val paymentFrequencyRateIndex = PaymentFrequency("M", 6)
        val floatingRateIndex = FloatingRateIndex( "0.003000", "0.026587")

        var floatIndex =0;
        var floatIndex1 =1;
        val floatingLeg1 = FloatingLeg(interestRatePayout.get(floatIndex).payerReceiver.payerPartyReference, interestRatePayout.get(floatIndex).payerReceiver.receiverPartyReference,
                Notional(interestRatePayout.get(floatIndex).quantity.notionalSchedule.notionalStepSchedule.initialValue.toString(),
                        interestRatePayout.get(floatIndex).quantity.notionalSchedule.notionalStepSchedule.currency.toString()),
                PaymentFrequency(interestRatePayout.get(floatIndex).paymentDates.paymentFrequency.period.toString(),interestRatePayout.get(floatIndex).paymentDates.paymentFrequency.periodMultiplier),
                CalculationPeriodDateReference(listOf(interestRatePayout.get(floatIndex).calculationPeriodDates.effectiveDate.adjustableDate.dateAdjustments.businessCenters.toString()),
                        interestRatePayout.get(floatIndex).calculationPeriodDates.effectiveDate.adjustableDate.dateAdjustments.businessDayConvention.toString(),
                        interestRatePayout.get(floatIndex).calculationPeriodDates.effectiveDate.adjustableDate.unadjustedDate.toString()),
                CalculationPeriodDateReference(listOf(interestRatePayout.get(floatIndex).calculationPeriodDates.terminationDate.dateAdjustments.businessCenters.toString()),
                        interestRatePayout.get(floatIndex).calculationPeriodDates.terminationDate.dateAdjustments.businessDayConvention.toString(),
                        interestRatePayout.get(floatIndex).calculationPeriodDates.terminationDate.unadjustedDate.toString())
                , interestRatePayout.get(floatIndex).dayCountFraction.toString(),

                CalculationPeriodDateReference(listOf(interestRatePayout.get(floatIndex).paymentDates.paymentDatesAdjustments.businessCenters.toString()),
                        interestRatePayout.get(floatIndex).paymentDates.paymentDatesAdjustments.businessDayConvention.toString(),
                        ""), interestRatePayout.get(floatIndex).resetDates.toString(),
                FloatingRateIndex(interestRatePayout.get(floatIndex).interestRate.floatingRate.spreadSchedule.get(0).initialValue.toString(),
                        interestRatePayout.get(floatIndex).interestRate.floatingRate.initialRate.toString()))


        val floatingLeg2 = FloatingLeg(interestRatePayout.get(floatIndex1).payerReceiver.payerPartyReference, interestRatePayout.get(floatIndex1).payerReceiver.receiverPartyReference,
                Notional(interestRatePayout.get(floatIndex1).quantity.notionalSchedule.notionalStepSchedule.initialValue.toString(),
                        interestRatePayout.get(floatIndex1).quantity.notionalSchedule.notionalStepSchedule.currency.toString()),
                PaymentFrequency(interestRatePayout.get(floatIndex1).paymentDates.paymentFrequency.period.toString(),interestRatePayout.get(floatIndex1).paymentDates.paymentFrequency.periodMultiplier),
                CalculationPeriodDateReference(listOf(interestRatePayout.get(floatIndex1).calculationPeriodDates.effectiveDate.adjustableDate.dateAdjustments.businessCenters.toString()),
                        interestRatePayout.get(floatIndex1).calculationPeriodDates.effectiveDate.adjustableDate.dateAdjustments.businessDayConvention.toString(),
                        interestRatePayout.get(floatIndex1).calculationPeriodDates.effectiveDate.adjustableDate.unadjustedDate.toString()),
                CalculationPeriodDateReference(listOf(interestRatePayout.get(floatIndex1).calculationPeriodDates.terminationDate.dateAdjustments.businessCenters.toString()),
                        interestRatePayout.get(floatIndex1).calculationPeriodDates.terminationDate.dateAdjustments.businessDayConvention.toString(),
                        interestRatePayout.get(floatIndex1).calculationPeriodDates.terminationDate.unadjustedDate.toString())
                , interestRatePayout.get(floatIndex1).dayCountFraction.toString(),

                CalculationPeriodDateReference(listOf(interestRatePayout.get(floatIndex1).paymentDates.paymentDatesAdjustments.businessCenters.toString()),
                        interestRatePayout.get(floatIndex1).paymentDates.paymentDatesAdjustments.businessDayConvention.toString(),
                        ""), interestRatePayout.get(floatIndex1).resetDates.toString(),
                FloatingRateIndex(interestRatePayout.get(floatIndex1).interestRate.floatingRate.spreadSchedule.get(0).initialValue.toString(),
                        interestRatePayout.get(floatIndex1).interestRate.floatingRate.initialRate.toString()))


            val floatFloatIRS = net.corda.examples.obligation.FloatFloatIRS(basicInfo, floatingLeg1, floatingLeg2, myIdentity,partyIdentity)

        // 3. Start the IssueObligation flow. We block and wait for the flow to return.
        val (status, message) = try {
            val flowHandle = rpcOps.startFlowDynamic(
                    IssueIrsFloatFloatDeal.Initiator::class.java,
                    floatFloatIRS,
                    floatFloatIRS.floatingLeg2Party
            )

            val result = flowHandle.use { it.returnValue.getOrThrow() }
            CREATED to "Transaction id ${result.id} committed to ledger.\n${result.tx.outputs.single().data}"
        } catch (e: Exception) {
            BAD_REQUEST to e.message
        }

        // 4. Return the result.
        return Response.status(status).entity(message).build()
    }
}