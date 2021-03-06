package Bitcoin;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class BitcoinNetwork {

    private static BitcoinNetwork instance;
    private final HashMap<String, TransactionOutput> utx0Map = new HashMap<>();
    private final HashMap<String, Wallet> miners = new HashMap<>();
    private final List<Block> blockchain = new ArrayList<>();
    private Transaction genesisTransaction;
    private Integer transactionSequence = 0;
    private Boolean validBlockchain;

    public BitcoinNetwork(){
        instance = this;

        this.validBlockchain = false;
        this.initMiners();
        this.doGenesisTransaction();

    }

    public static BitcoinNetwork getInstance(){
        if (instance == null)
            instance = new BitcoinNetwork();
        return instance;
    }

    private void initMiners(){
        this.miners.put("SatoshiNakamato", new Wallet());
        this.miners.put("Bob", new Wallet());
        this.miners.put("Eve", new Wallet());
        this.miners.put("Sam", new Wallet());
    }

    private void doGenesisTransaction(){
        this.genesisTransaction = new Transaction(miners.get("SatoshiNakamato").getPublicKey(), miners.get("SatoshiNakamato").getPublicKey(), 1.0, null );
        this.genesisTransaction.setId("0");
        this.genesisTransaction.generateSignature(miners.get("SatoshiNakamato").getPrivateKey());
        this.genesisTransaction.getOutputs().add(new TransactionOutput(this.genesisTransaction.getRecipient(),this.genesisTransaction.getValue(),this.genesisTransaction.getId()));
        this.utx0Map.put(this.genesisTransaction.getOutputs().get(0).getID(),this.genesisTransaction.getOutputs().get(0));
        Block genesisBlock = new Block("0");
        genesisBlock.addTransaction(genesisTransaction);
        this.addBlockToBlockchain(genesisBlock);
    }

    public HashMap<String, TransactionOutput> getUtx0Map() {
        return this.utx0Map;
    }

    public void incrementTransactionSequence(){
        this.transactionSequence++;
    }

    public Integer getTransactionSequence(){
        return this.transactionSequence;
    }

    private void addBlockToBlockchain(Block block){
        block.mineBlock(this.findFreeMiner(), Configuration.instance.difficultyLevel);
        this.blockchain.add(block);
        Service.logNetworkMessage("new Blockchain:" +"\n"+ Service.getJson(this.blockchain));
        isChainValid();
    }

    private PublicKey findFreeMiner(){
        Random rnd = new Random();
        int rndNb = rnd.nextInt(0,3);
        return  rndNb == 0 ? miners.get("Bob").getPublicKey() : rndNb == 1 ? miners.get("Eve").getPublicKey() :  miners.get("Sam").getPublicKey();
    }

    public Double buyBitcoin(PublicKey buyer, Double amount){
        if(amount < miners.get("SatoshiNakamato").getBalance()){
            addTransactionToBlockchain(miners.get("SatoshiNakamato").sendFunds(buyer, amount));
            return amount;
        }
        return null;
    }

    public Wallet createWallet(){
        return new Wallet();
    }

    public Boolean validateBlockchain(){
        return this.validBlockchain;
    }

    public void addTransactionToBlockchain(Transaction transaction){
        Block block = new Block(this.blockchain.get(this.blockchain.size() - 1).getHash());
        block.addTransaction(transaction);
        Service.logNetworkMessage("New Bitcoin.Transaction");
        this.addBlockToBlockchain(block);
        Service.logNetworkMessage(transaction.getValue() + " Bitcoin send by \n" + transaction.getSender().toString() +" to\n" + transaction.getRecipient().toString());
    }

    private void isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = Service.getDifficultyString(Configuration.instance.difficultyLevel);
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>();
        tempUTXOs.put(this.genesisTransaction.getOutputs().get(0).getID(), this.genesisTransaction.getOutputs().get(0));

        for (int i = 1; i < this.blockchain.size(); i++) {
            currentBlock = this.blockchain.get(i);
            previousBlock = this.blockchain.get(i - 1);

            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                Service.logNetworkMessage("#current hashes not equal");
                this.validBlockchain = false;
                return;
            }

            if (!previousBlock.getHash().equals(currentBlock.getPreviousHash())) {
                Service.logNetworkMessage("#trevious hashes not equal");
                this.validBlockchain = false;
                return;
            }

            if (!currentBlock.getHash().substring(0, Configuration.instance.difficultyLevel).equals(hashTarget)) {
                Service.logNetworkMessage("#block not mined");
                this.validBlockchain = false;
                return;
            }

            TransactionOutput tempOutput;
            for (int t = 0; t < currentBlock.getTransactions().size(); t++) {
                Transaction currentTransaction = currentBlock.getTransactions().get(t);

                if (currentTransaction.verifySignature()) {
                    Service.logNetworkMessage("#Signature on Bitcoin.Transaction(" + t + ") is Invalid");
                    this.validBlockchain = false;
                    return;
                }

                if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    Service.logNetworkMessage("#Inputs are not equal to outputs on Bitcoin.Transaction(" + t + ")");
                    this.validBlockchain = false;
                    return;
                }

                for (TransactionInput input : currentTransaction.getInputs()) {
                    tempOutput = tempUTXOs.get(input.getId());

                    if (tempOutput == null) {
                        Service.logNetworkMessage("#referenced input on transaction(" + t + ") is missing");
                        this.validBlockchain = false;
                        return;
                    }

                    if (input.getUTX0().getValue() != tempOutput.getValue()) {
                        Service.logNetworkMessage("#referenced input on transaction(" + t + ") value invalid");
                        this.validBlockchain = false;
                        return;
                    }

                    tempUTXOs.remove(input.getId());
                }

                for (TransactionOutput output : currentTransaction.getOutputs()) {
                    tempUTXOs.put(output.getID(), output);
                }

                if (currentTransaction.getOutputs().get(0).getRecipient() != currentTransaction.getRecipient()) {
                    Service.logNetworkMessage("#transaction(" + t + ") output recipient is invalid");
                    this.validBlockchain = false;
                    return;
                }

                if (currentTransaction.getOutputs().get(1).getRecipient() != currentTransaction.getSender()) {
                    Service.logNetworkMessage("#transaction(" + t + ") output 'change' is not sender");
                    this.validBlockchain = false;
                    return;
                }
            }
        }
        Service.logNetworkMessage("blockchain valid");
        this.validBlockchain = true;
    }

}
