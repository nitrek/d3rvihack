package net.corda.examples.obligation.models

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class ContractIdentifier (
    val identifier: String,
    val version: String
    )
{

}