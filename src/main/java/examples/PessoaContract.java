package examples;

import net.corda.core.contracts.*;
import net.corda.core.transactions.LedgerTransaction;

import java.security.PublicKey;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;

public class PessoaContract implements Contract {
    // Used to reference the contract in transactions.
    public static final String ID = PessoaContract.class.getName();

    public interface Commands extends CommandData {
        class CadastrarPesssoa implements Commands { }
        class AtualizarPessoa implements Commands { }
        class ConcederPermissaoConsulta implements Commands { }
    }

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);

        if (command.getValue() instanceof Commands.CadastrarPesssoa) {
            // Issue transaction rules...

            final PessoaState stateOutput = tx.outputsOfType(PessoaState.class).get(0);

            if (stateOutput.getCpf() == null || stateOutput.getCpf().isEmpty()) {
                throw new IllegalArgumentException("CPF da pessoa é obrigatório.");
            }

            if (stateOutput.getNome() == null || stateOutput.getNome().isEmpty()) {
                throw new IllegalArgumentException("Nome da pessoa é obrigatório.");
            }

        } else throw new IllegalArgumentException("Unrecognised command.");
    }
}