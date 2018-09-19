package net.corda.examples.obligation

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import net.corda.finance.contracts.asset.Cash
import net.corda.finance.utils.sumCash
import java.security.PublicKey

class IRSContract : Contract {

    companion object {
        @JvmStatic
        val OBLIGATION_CONTRACT_ID = "net.corda.examples.obligation.IRSContract"
    }

    interface Commands : CommandData {
        class FloatFloatDeal : TypeOnlyCommandData(), Commands
        class FixedFloatDeal : TypeOnlyCommandData(), Commands
    }

    override fun verify(tx: LedgerTransaction): Unit {
        val command = tx.commands.requireSingleCommand<Commands>()
        val setOfSigners = command.signers.toSet()
        when (command.value) {
            is Commands.FixedFloatDeal -> verifyFixedFloatDeal(tx, setOfSigners)
            is Commands.FloatFloatDeal -> verifyFloatFloatDeal(tx, setOfSigners)
            else -> throw IllegalArgumentException("Unrecognised command.")
        }
    }


    // This only allows one obligation issuance per transaction.
    private fun verifyFixedFloatDeal(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        "No inputs should be consumed when issuing an obligation." using (tx.inputStates.isEmpty())
        "Only one obligation state should be created when issuing an obligation." using (tx.outputStates.size == 1)
    }
    private fun verifyFloatFloatDeal(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        "No inputs should be consumed when issuing an obligation." using (tx.inputStates.isEmpty())
        "Only one obligation state should be created when issuing an obligation." using (tx.outputStates.size == 1)
    }

}