<p align="center">
  <img src="https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png" alt="Corda" width="500">
</p>

# Bootcamp CorDapp [<img src="https://raw.githubusercontent.com/corda/samples-java/master/webIDE.png" height=25 />](https://ide.corda.net/?folder=/home/coder/bootcamp-cordapp)

This project is the template we will use as a basis for developing a complete CorDapp 
during today's bootcamp. Our CorDapp will allow the issuance of tokens onto the ledger.

We'll develop the CorDapp using a test-driven approach. At each stage, you'll know your 
CorDapp is working once it passes both sets of tests defined in `src/test/java/bootcamp`.cno

## Set up

1. Download and install a JDK 8 JVM (minimum supported version 8u131)
2. Download and install IntelliJ Community Edition (supported versions 2017.x and 2018.x)
3. Download the bootcamp-cordapp repository:

       git clone https://github.com/corda/bootcamp-cordapp
       
4. Open IntelliJ. From the splash screen, click `Import Project`, select the `bootcamp—
cordapp` folder and click `Open`
5. Select `Import project from external model > Gradle > Next > Finish`
6. Click `File > Project Structure…` and select the Project SDK (Oracle JDK 8, 8u131+)

    i. Add a new SDK if required by clicking `New…` and selecting the JDK’s folder

7. Open the `Project` view by clicking `View > Tool Windows > Project`

## Links to useful resources

This project contains example state, contract and flow implementations:

* `src/main/java/java_examples/PessoaState`
* `src/main/java/java_examples/PessoaContract`
* `src/main/java/java_examples/CadastrarPessoaFlowInitiator`
* `src/main/java/java_examples/CadastrarPessoaFlowResponder`

There are also several web resources that you will likely find useful for this
bootcamp:

* Key Concepts docs (`docs.corda.net/key-concepts.html`)
* API docs (`docs.corda.net/api-index.html`)
* Cheat sheet (`docs.corda.net/cheat-sheet.html`)
* Sample CorDapps (`www.corda.net/samples`)
* Stack Overflow (`www.stackoverflow.com/questions/tagged/corda`)

## What we'll be building

Our CorDapp will have three parts:

### The PessoaState

States define shared facts on the ledger. Our state, PessoaState, will define a
person. It will have the following structure:

    -------------------
    |                 |
    |   PessoaState    |
    |                 |
    |   - cpf         |
    |   - nome        |
    |   - dono        |
    |                 |
    -------------------

### The PessoaContract

Contracts govern how states evolve over time. Our contract, PessoaContract,
will define how PessoaStates evolve. It will only allow the following type of
PessoaState transaction:

    -------------------------------------------------------------------------------------
    |                                                                                   |
    |    - - - - - - - - - -                                     -------------------    |
    |                                              ▲             |                 |    |
    |    |                 |                       | -►          |   PessoaState   |    |
    |            NO             -------------------     -►       |                 |    |
    |    |                 |    |      Issue command       -►    |   - cpf         |    |
    |          INPUTS           |     signed by dono     -►    |     - nome        |    |
    |    |                 |    -------------------     -►       |   - dono        |    |
    |                                              | -►          |                 |    |
    |    - - - - - - - - - -                       ▼             -------------------    |
    |                                                                                   |
    -------------------------------------------------------------------------------------

              No inputs             One issue command,                One output,
                                 issuer is a required signer       person is valid

To do so, PessoaContract will impose the following constraints on transactions
involving PessoaStates:

* The transaction has no input states
* The transaction has one output state
* The transaction has one command
* The output state is a PessoaState
* The output state has a valid cpf and nome
* The output state has a unique cpf registered
* The command is an CadastarPessoa command
* The command lists the PessoaState's dono as a required signer

### The CadastrarPesssoaFlow

Flows automate the process of updating the ledger. Our flow, CadastrarPesssoaFlow, will
automate the following steps:

            Issuer                  Owner                  Notary
              |                       |                       |
       Chooses a notary
              |                       |                       |
        Starts building
         a transaction                |                       |
              |
        Validate                      |                       |
        PessoaState
              |                       |                       |
        Adds the output               |                       |
          PessoaState
              |                       |                       |
           Adds the
         CadastrarPesssoa command     |                       |
              |
         Verifies the                 |                       |
          transaction
              |                       |                       |
          Signs the
         transaction                  |                       |
              |
              |----------------------------------------------►|
              |                       |                       |
                                                         Notarises the
              |                       |                   transaction
                                                              |
              |◀----------------------------------------------|
              |                       |                       |
         Records the
         transaction                  |                       |
              |
              |----------------------►|                       |
                                      |
              |                  Records the                  |
                                 transaction
              |                       |                       |
              ▼                       ▼                       ▼

## Running our CorDapp

Normally, you'd interact with a CorDapp via a client or webserver. So we can
focus on our CorDapp, we'll be running it via the node shell instead.

Once you've finished the CorDapp's code, run it with the following steps:

* Build a test network of nodes by opening a terminal window at the root of
  your project and running the following command:

    * Windows:   `gradlew.bat deployNodes`
    * macOS:     `./gradlew deployNodes`

* Start the nodes by running the following command:

    * Windows:   `build\nodes\runnodes.bat`
    * macOS:     `build/nodes/runnodes`

* Open the nodes are started, go to the terminal of Cidadao A (not the notary!) and run the following command:
    
    ```bash
    flow start examples.CadastrarPessoaFlowInitiator "cpf": "0123456789-00", "nome": "Person Name", dono: "CidadaoA"
    ```

* You can now see the persons in the vaults of Cidadao A by running the following command in their respective terminals:
    
    ```bash
    run vaultQuery contractStateType: examples.PessoaState
    ```
