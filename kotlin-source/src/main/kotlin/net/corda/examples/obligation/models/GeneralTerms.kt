package net.corda.examples.obligation.models

import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class GeneralTerms (
    val businessCenter: List<String>,
    val businessDayConvention: String,
    val indexName: String,
    val indexSeries: String,
    val buyerCommonName: String,
    val sellerCommonName: String
)
{

}