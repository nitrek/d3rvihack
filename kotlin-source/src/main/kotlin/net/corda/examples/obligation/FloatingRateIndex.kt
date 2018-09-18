package net.corda.examples.obligation

data class FloatingRateIndex(val indexTenor: PaymentFrequency,
                             val spreadValue:String,
                             val initialRate:String)
{
}