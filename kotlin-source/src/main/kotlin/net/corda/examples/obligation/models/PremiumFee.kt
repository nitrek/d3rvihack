package net.corda.examples.obligation.models

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class PremiumFee(val amount : String,
                      val currency : String)
{
}