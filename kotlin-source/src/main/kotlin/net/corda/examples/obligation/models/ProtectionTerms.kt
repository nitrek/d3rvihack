package net.corda.examples.obligation.models

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class ProtectionTerms(val notionalAmount : String,
                           val currency : String)
{
}