package net.corda.examples.obligation.models

import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class CDSTermination (var status: String?= null, var initiatedBy: Party?=null, var counterParty: Party?=null, var terminationFee: String?=null, var effectiveDate: String?=null)
{
}