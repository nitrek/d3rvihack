package net.corda.examples.obligation


data class IRSBasicInfo(val tradeDate : String,
                     val tradeCurrency : String,
                     val accountDetailsParty1:List<AccountDetails>,
                     val accountDetailsParty2: List<AccountDetails>)
{
}