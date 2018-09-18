package net.corda.examples.obligation


data class CalculationPeriodDateReference(val businessCenters : List<String>,
                                          val businessDayConvension:String,
                                          val unadjustedDate : String,
                                          val payRelativeTo:String = ""
                                 )
{
}