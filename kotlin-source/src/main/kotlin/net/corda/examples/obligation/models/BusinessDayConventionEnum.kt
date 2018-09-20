package net.corda.examples.obligation.models

import com.rosetta.model.lib.annotations.RosettaSynonym
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class BusinessDayConventionEnum constructor() {
    @RosettaSynonym(value = "FOLLOWING", source = "FpML")
    FOLLOWING,
    @RosettaSynonym(value = "FRN", source = "FpML")
    FRN,
    @RosettaSynonym(value = "MODFOLLOWING", source = "FpML")
    MODFOLLOWING,
    @RosettaSynonym(value = "PRECEDING", source = "FpML")
    PRECEDING,
    @RosettaSynonym(value = "MODPRECEDING", source = "FpML")
    MODPRECEDING,
    @RosettaSynonym(value = "NEAREST", source = "FpML")
    NEAREST,
    @RosettaSynonym(value = "NONE", source = "FpML")
    NONE,
    @RosettaSynonym(value = "NotApplicable", source = "FpML")
    NOT_APPLICABLE
}