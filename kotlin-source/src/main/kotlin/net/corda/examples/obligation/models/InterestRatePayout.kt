package net.corda.examples.obligation.models

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class InterestRatePayout(val stateDate: String, val endDate: String, val interestRate: String, val dayCountFraction: String)
{
}

