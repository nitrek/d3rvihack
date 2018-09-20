package net.corda.examples.obligation.models

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class TradeDetails(val productQualifier : String,
                        val tradeDate : String)
{
}