package net.corda.examples.obligation

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.examples.obligation.models.*

data class CreditDefaultSwap(val contractIdentifier: ContractIdentifier,
                             val generalTerms: GeneralTerms,
                             val tradeDetails: TradeDetails,
                             val interestRatePayout: InterestRatePayout,
                             val premiumFee: PremiumFee,
                             val protectionTerms: ProtectionTerms,
                             val buyer:Party,
                             val seller:Party,
                             val cdsTermination: CDSTermination,
                             override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {

    override val participants: List<AbstractParty> get() = listOf(buyer,seller)

//    fun pay(amountToPay: Amount<Currency>) = copy(paid = paid + amountToPay)
//    fun withNewLender(newLender: AbstractParty) = copy(lender = newLender)
//    fun withoutLender() = copy(lender = NullKeys.NULL_PARTY)
    fun setCDSTermination(status: String,
                          initiatedBy: Party,
                          counterParty: Party,
                          terminationFee: String,
                          effectiveDate: String)= copy (cdsTermination = CDSTermination(status,initiatedBy,counterParty,terminationFee,effectiveDate))
    fun setNotional(notional : String, currency : String) = copy(protectionTerms= ProtectionTerms(notional,currency))
//    override fun toString(): String {
//        val lenderString = (lender as? Party)?.name?.organisation ?: lender.owningKey.toBase58String()
//        val borrowerString = (borrower as? Party)?.name?.organisation ?: borrower.owningKey.toBase58String()
//        return "Obligation($linearId): $borrowerString owes $lenderString $amount and has paid $paid so far."
//    }
}