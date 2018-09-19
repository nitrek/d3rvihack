package net.corda.examples.obligation.models

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class FloatingRateIndex(val spreadValue:String,
                             val initialRate:String)
{
}