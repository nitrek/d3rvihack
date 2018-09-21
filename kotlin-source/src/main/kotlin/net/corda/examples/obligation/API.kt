package net.corda.examples.obligation

import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper
import net.corda.core.contracts.Amount
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.OpaqueBytes
import net.corda.core.utilities.getOrThrow
import net.corda.examples.obligation.flows.IssueCdsDeal
import net.corda.examples.obligation.flows.IssueIrsFixedFloatDeal
import net.corda.examples.obligation.flows.IssueIrsFloatFloatDeal
import net.corda.examples.obligation.models.*
import net.corda.finance.contracts.asset.Cash
import net.corda.finance.contracts.getCashBalances
import net.corda.finance.flows.CashIssueFlow
import org.isda.cdm.ContractIdentifier
import org.isda.cdm.ContractualProduct
import org.isda.cdm.Event
import org.isda.cdm.IdentifierValue
import java.util.*
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.core.Response.Status.CREATED
import java.util.UUID.nameUUIDFromBytes



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

    @GET
    @Path("get-cds")
    @Produces(MediaType.APPLICATION_JSON)
    fun getcdsdeals() = rpcOps.vaultQuery(CreditDefaultSwap::class.java).states


    //my api
    @POST
    @Path("irs-create-deal")
    fun createDeal(payload:String): Response {
        // 1. Get party objects for the counterparty.
        val event = RosettaObjectMapper.getDefaultRosettaObjectMapper().readValue(payload, Event::class.java)
        val partyIdentity = rpcOps.partiesFromName(event.party.get(1).legalEntity.entityId, exactMatch = false).singleOrNull()
               ?: throw IllegalStateException("Couldn't lookup node identity for "+event.party.get(1).legalEntity.name)
       
        val contract = event.primitive.newTrade.get(0).contract
        val interestRatePayout = contract.contractualProduct.economicTerms.payout.interestRatePayout
        val acc1 = AccountDetails(contract.account.get(0).accountNumber, contract.account.get(0).servicingParty)
        val acc2 = AccountDetails(contract.account.get(1).accountNumber,contract.account.get(1).servicingParty)
        val basicInfo = IRSBasicInfo(contract.tradeDate.adjustableDate.unadjustedDate.toString(),contract.contractIdentifier.get(0).identifierValue.identifier,listOf(acc1), listOf(acc1))
        //val quantity = Notional("70000000.0", "EUR")

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
                        interestRatePayout.get(floatIndex).interestRate.floatingRate.initialRate.toString()),event.party.get(floatIndex).legalEntity.name)


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
            interestRatePayout.get(fixedIndex).interestRate.fixedRate.initialValue.toString(),
            event.party.get(fixedIndex).legalEntity.name)
            
            var fixedFloatIRS:FixedFloatIRS
        var fixedLegBool = true
        System.out.println("legs created")
        val aString = "JUST_A_TEST_STRING"
        val result = UUID.nameUUIDFromBytes(aString.toByteArray())
        if(type.equals("fixed",true))
        {
            fixedFloatIRS = net.corda.examples.obligation.FixedFloatIRS(basicInfo, fixedLeg, floatingLeg, myIdentity,partyIdentity,UniqueIdentifier(contract.contractIdentifier.get(0).identifierValue.identifier,result))
        }
    else{
            fixedFloatIRS = net.corda.examples.obligation.FixedFloatIRS(basicInfo, fixedLeg, floatingLeg,partyIdentity, myIdentity,UniqueIdentifier(contract.contractIdentifier.get(0).identifierValue.identifier,result))
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
        val partyIdentity = rpcOps.partiesFromName(event.party.get(1).legalEntity.entityId, exactMatch = false).singleOrNull()
                ?: throw IllegalStateException("Couldn't lookup node identity for "+event.party.get(1).legalEntity.name)
        //val event = RosettaObjectMapper.getDefaultRosettaObjectMapper().readValue(payload, Event::class.java)
        val contract = event.primitive.newTrade.get(0).contract
        val interestRatePayout = contract.contractualProduct.economicTerms.payout.interestRatePayout
        val acc1 = AccountDetails(contract.account.get(0).accountNumber, contract.account.get(0).servicingParty)
        val acc2 = AccountDetails(contract.account.get(1).accountNumber,contract.account.get(1).servicingParty)

        val basicInfo = IRSBasicInfo(contract.tradeDate.adjustableDate.unadjustedDate.toString(),contract.contractIdentifier.get(0).identifierValue.identifier,listOf(acc1), listOf(acc1))
        //val quantity = Notional("70000000.0", "EUR")


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
                        interestRatePayout.get(floatIndex).interestRate.floatingRate.initialRate.toString()),event.party.get(0).legalEntity.name)


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
                        interestRatePayout.get(floatIndex1).interestRate.floatingRate.initialRate.toString()),event.party.get(1).legalEntity.name)
        
            val aString = "JUST_A_TEST_STRING"
        val result = UUID.nameUUIDFromBytes(aString.toByteArray())
            val floatFloatIRS = net.corda.examples.obligation.FloatFloatIRS(basicInfo, floatingLeg1, floatingLeg2, myIdentity,partyIdentity,UniqueIdentifier(contract.contractIdentifier.get(0).identifierValue.identifier,result))

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

    @POST
    @Path("cds-create")
    fun createCreditDefaultSwap(payload:String): Response {
        // 1. Get party objects for the counterparty.
        val event = RosettaObjectMapper.getDefaultRosettaObjectMapper().readValue(payload, Event::class.java)
        val partyIdentity = rpcOps.partiesFromName(event.party.get(1).legalEntity.entityId, exactMatch = false).singleOrNull()
                ?: throw IllegalStateException("Couldn't lookup node identity for "+event.party.get(1).legalEntity.name)
        //val event = RosettaObjectMapper.getDefaultRosettaObjectMapper().readValue(payload, Event::class.java)
        val contract = event.primitive.newTrade.get(0).contract
        val contractIdentifier1 = contract.contractIdentifier.get(0)
        val eventIdentifier =  contractIdentifier1.identifierValue.identifier
        val version = contractIdentifier1.version
        val contractIdentifier = net.corda.examples.obligation.models.ContractIdentifier(eventIdentifier,version.toString())
        val terms = contract.contractualProduct.economicTerms.payout.creditDefaultPayout.generalTerms

        val payout = contract.contractualProduct.economicTerms.payout.interestRatePayout.get(0)
        val buyer = if (terms.buyerSeller.buyerPartyReference.equals(event.party.get(0).partyId.get(0))) myIdentity else partyIdentity
        val seller =  if (terms.buyerSeller.sellerPartyReference.equals(event.party.get(0).partyId.get(0))) myIdentity else partyIdentity

        val buyerName =  if (terms.buyerSeller.buyerPartyReference.equals(event.party.get(0).partyId.get(0))) event.party.get(0).legalEntity.name else event.party.get(1).legalEntity.name
        val sellerName =  if (terms.buyerSeller.sellerPartyReference.equals(event.party.get(0).partyId.get(0))) event.party.get(0).legalEntity.name else event.party.get(1).legalEntity.name

        val generalTerms = GeneralTerms(terms.dateAdjustments.businessCenters.businessCenter.map{it.toString()},terms.dateAdjustments.businessDayConvention.toString(),terms.indexReferenceInformation.indexName,terms.indexReferenceInformation.indexSeries.toString(),buyerName,sellerName)
        val interestRatePayout = InterestRatePayout(payout.calculationPeriodDates.effectiveDate.adjustableDate.unadjustedDate.toString(),payout.calculationPeriodDates.effectiveDate.adjustableDate.unadjustedDate.toString(),payout.interestRate.fixedRate.initialValue.toString(),payout.dayCountFraction.toString())
        val feeValue = contract.contractualProduct.economicTerms.payout.cashflow.get(0).cashflowAmount
        val premiumFee = PremiumFee(feeValue.amount.toString(),feeValue.currency.toString())
        val protection = contract.contractualProduct.economicTerms.payout.creditDefaultPayout.protectionTerms
        val protectionTerms = ProtectionTerms(protection.notionalAmount.amount.toString(),protection.notionalAmount.currency.toString())

        val detailsModel = TradeDetails(contract.contractualProduct.productIdentification.productQualifier,contract.tradeDate.adjustableDate.unadjustedDate.toString())


        val aString = "JUST_A_TEST_STRING"
        val result = UUID.nameUUIDFromBytes(aString.toByteArray())
        val creditDefaultSwap = net.corda.examples.obligation.CreditDefaultSwap(contractIdentifier,generalTerms,detailsModel,interestRatePayout,premiumFee,protectionTerms,buyer,seller,CDSTermination("NEWTRADE"),UniqueIdentifier(eventIdentifier,result))

        // 3. Start the IssueObligation flow. We block and wait for the flow to return.
        val (status, message) = try {
            val flowHandle = rpcOps.startFlowDynamic(
                    IssueCdsDeal.Initiator::class.java,
                    creditDefaultSwap,
                    creditDefaultSwap.seller
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