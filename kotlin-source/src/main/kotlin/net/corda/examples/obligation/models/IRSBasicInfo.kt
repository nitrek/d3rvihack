package net.corda.examples.obligation.models

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class IRSBasicInfo(val tradeDate : String,
                        val accountDetailsParty1:List<AccountDetails>,
                        val accountDetailsParty2: List<AccountDetails>)
{
}