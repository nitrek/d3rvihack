package net.corda.examples.obligation


data class PaymentFrequency(val period : String,
                            val periodMultiplyer : Int)
{
}