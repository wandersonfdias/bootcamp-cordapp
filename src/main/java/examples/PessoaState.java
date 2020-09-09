package examples;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.List;


@CordaSerializable
@BelongsToContract(PessoaContract.class)
public class PessoaState implements ContractState {
    // The attributes that will be stored on the ledger as part of the state.
    private final String cpf;
    private final String nome;
    private final Party dono;

    @ConstructorForDeserialization
    public PessoaState(String cpf, String nome, Party dono) {
        this.cpf = cpf;
        this.nome = nome;
        this.dono = dono;
    }

    // Overrides `participants`, the only field defined by `ContractState`.
    // Defines which parties will store the state.
    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(dono);
    }

    public String getCpf() {
        return cpf;
    }

    public String getNome() {
        return nome;
    }

    public Party getDono() {
        return dono;
    }
}