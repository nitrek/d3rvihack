package net.corda.examples.obligation.models

import net.corda.core.serialization.CordaSerializable
import java.util.*

@CordaSerializable
data class Notional(val initialValue : String,
                    val currency: String )
{
}