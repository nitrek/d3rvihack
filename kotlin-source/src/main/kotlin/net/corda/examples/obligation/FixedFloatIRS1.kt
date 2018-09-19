package net.corda.examples.obligation

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.examples.obligation.models.FixedLeg
import net.corda.examples.obligation.models.FloatingLeg
import net.corda.examples.obligation.models.IRSBasicInfo
import org.isda.cdm.Event;

data class FixedFloatIRS1(val event:Event,
                         val fixedLegParty:Party,
                         val floatingLegParty:Party,
                         override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {

    override val participants: List<AbstractParty> get() = listOf(floatingLegParty,fixedLegParty)
}