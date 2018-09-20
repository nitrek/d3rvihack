package net.corda.examples.obligation.models

import com.rosetta.model.lib.annotations.RosettaSynonym
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class BusinessCenterEnum constructor() {
    @RosettaSynonym(value = "AEAD", source = "FpML")
    AEAD,
    @RosettaSynonym(value = "AEDU", source = "FpML")
    AEDU,
    @RosettaSynonym(value = "AMYE", source = "FpML")
    AMYE,
    @RosettaSynonym(value = "BEBR", source = "FpML")
    BEBR,
    @RosettaSynonym(value = "BRBD", source = "FpML")
    BRBD,
    @RosettaSynonym(value = "CATO", source = "FpML")
    CATO,
    @RosettaSynonym(value = "CHZU", source = "FpML")
    CHZU,
    @RosettaSynonym(value = "CNBE", source = "FpML")
    CNBE,
    @RosettaSynonym(value = "DEFR", source = "FpML")
    DEFR,
    @RosettaSynonym(value = "EUTA", source = "FpML")
    EUTA,
    @RosettaSynonym(value = "GBLO", source = "FpML")
    GBLO,
    @RosettaSynonym(value = "INMU", source = "FpML")
    INMU,
    @RosettaSynonym(value = "JPTO", source = "FpML")
    JPTO,
    @RosettaSynonym(value = "KRSE", source = "FpML")
    KRSE,
    @RosettaSynonym(value = "USNY", source = "FpML")
    USNY
}
