package net.corda.examples.obligation.models

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class PaymentFrequency(val period : String,
                            val periodMultiplyer : Int)
{
}