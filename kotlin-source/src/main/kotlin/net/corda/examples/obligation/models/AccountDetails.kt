package net.corda.examples.obligation.models

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class AccountDetails(val accountNumber : String,
                     val serviceParty : String)
{
}