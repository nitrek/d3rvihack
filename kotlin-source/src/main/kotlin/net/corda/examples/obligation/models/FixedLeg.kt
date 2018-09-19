package net.corda.examples.obligation.models

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class FixedLeg(val payerPartyReference : String,
                    val receiverPartyReference : String,
                    val quantiy: Notional,
                    val paymentFrequency: PaymentFrequency,
                    val effectiveDate: CalculationPeriodDateReference,
                    val terminationDate: CalculationPeriodDateReference,
                    val dayCountBasis:String,
                    val paymentCalendar: CalculationPeriodDateReference,
                    val fixedRate:String)
{
}