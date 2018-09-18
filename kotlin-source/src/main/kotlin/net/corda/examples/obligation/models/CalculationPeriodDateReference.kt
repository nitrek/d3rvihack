package net.corda.examples.obligation.models

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class CalculationPeriodDateReference(val businessCenters : List<String>,
                                          val businessDayConvension:String,
                                          val unadjustedDate : String,
                                          val payRelativeTo:String = ""
                                 )
{
}