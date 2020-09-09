package examples;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Iterables.find;

// `TokenIssueFlowInitiator` means that we can start the flow directly (instead of
// solely in response to another flow).
@InitiatingFlow
// `StartableByRPC` means that a node operator can start the flow via RPC.
@StartableByRPC
// Like all states, implements `FlowLogic`.
public class CadastrarPessoaFlowInitiator extends FlowLogic<Void> {
    private final String cpf;
    private final String nome;
    private final Party dono;

    @ConstructorForDeserialization
    public CadastrarPessoaFlowInitiator(String cpf, String nome, Party dono) {
        this.cpf = cpf;
        this.nome = nome;
        this.dono = dono;
    }

    private final ProgressTracker.Step AVALIANDO_INFORMACAO_CADASTRO = new ProgressTracker.Step("Avaliando informação para o cadastro de pessoa.");
    private final ProgressTracker.Step CONFIGURANDO_NOTARIO = new ProgressTracker.Step("Encontrando um notário na rede.");
    private final ProgressTracker.Step CONFIGURANDO_TRANSACAO = new ProgressTracker.Step("Configurando a transação do novo cadastro de pessoa.");
    private final ProgressTracker.Step REALIZANDO_CADASTRO = new ProgressTracker.Step("Realizando o cadastro de pessoa.");
    private final ProgressTracker.Step CADASTRO_REALIZADO = new ProgressTracker.Step("Cadastro da pessoa realizado com sucesso.");

    private final ProgressTracker progressTracker = new ProgressTracker(
            AVALIANDO_INFORMACAO_CADASTRO,
            CONFIGURANDO_NOTARIO,
            CONFIGURANDO_TRANSACAO,
            REALIZANDO_CADASTRO,
            CADASTRO_REALIZADO
    );

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    // Must be marked `@Suspendable` to allow the flow to be suspended
    // mid-execution.
    @Suspendable
    @Override
    // Overrides `call`, where we define the logic executed by the flow.
    public Void call() throws FlowException {

        progressTracker.setCurrentStep(AVALIANDO_INFORMACAO_CADASTRO);

        // obtem as pessoas cadastradas
        // TODO: Refinar busca
        List<StateAndRef<PessoaState>> pessoasCadastradas = getServiceHub().getVaultService().queryBy(PessoaState.class).getStates();

        // verifica se a pessoa existe para um dado cpf
        if (pessoasCadastradas.stream().anyMatch(item -> {
            PessoaState pessoaState = item.getState().getData();
            return pessoaState.getCpf().equals(this.cpf);
        })){
            throw new IllegalArgumentException("Pessoa já cadastrada para o cpf informado.");
        }

        progressTracker.setCurrentStep(CONFIGURANDO_NOTARIO);

        // obtem o notario
        CordaX500Name x500Name = CordaX500Name.parse("O=Cidadao,L=SP,C=BR");

        // We use the notary used by the input state.
        Party notary = getServiceHub().getNetworkMapCache().getNotary(x500Name);

        progressTracker.setCurrentStep(CONFIGURANDO_TRANSACAO);

        // We build a transaction using a `TransactionBuilder`.
        TransactionBuilder txBuilder = new TransactionBuilder();

        // After creating the `TransactionBuilder`, we must specify which
        // notary it will use.
        txBuilder.setNotary(notary);

        // cria novo estado
        PessoaState pessoaState = new PessoaState(this.cpf, this.nome, this.dono);

        // We add the output PessoaState to the transaction. Note that we also
        txBuilder.addOutputState(pessoaState, PessoaContract.ID);

        progressTracker.setCurrentStep(REALIZANDO_CADASTRO);

        // We add the Issue command to the transaction.
        // Note that we also specific who is required to sign the transaction.
        PessoaContract.Commands.CadastrarPesssoa cadastrarPessoaCommand = new PessoaContract.Commands.CadastrarPesssoa();
        List<PublicKey> requiredSigners = ImmutableList.of(this.dono.getOwningKey());
        txBuilder.addCommand(cadastrarPessoaCommand, requiredSigners);

        // We check that the transaction builder we've created meets the
        // contracts of the input and output states.
        txBuilder.verify(getServiceHub());

        // We finalise the transaction builder by signing it,
        // converting it into a `SignedTransaction`.
        SignedTransaction partlySignedTx = getServiceHub().signInitialTransaction(txBuilder);

        // We use `CollectSignaturesFlow` to automatically gather a
        // signature from each counterparty. The counterparty will need to
        // call `SignTransactionFlow` to decided whether or not to sign.
        FlowSession ownerSession = initiateFlow(this.dono);
        SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(partlySignedTx, ImmutableSet.of(ownerSession)));

        // We use `FinalityFlow` to automatically notarise the transaction
        // and have it recorded by all the `participants` of all the
        // transaction's states.
        subFlow(new FinalityFlow(fullySignedTx, Collections.emptyList())); // indica fluxo sem resposta

        progressTracker.setCurrentStep(CADASTRO_REALIZADO);

        return null;
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