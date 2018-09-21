package net.corda.examples.obligation.models

import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class CDSTermination (val status: String?= null, val initiatedBy: Party?=null, val counterParty: Party?=null, val terminationFee: String?=null, val effectiveDate: String?=null)
{
}