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

    public BitcoinNetwork(){
        instance = this;

        this.initMiners();
        this.doGenesisTransaction();
        this.isChainValid();

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

    public void incrementTransactionSquence(){
        this.transactionSequence++;
    }

    public Integer getTransactionSquence(){
        return this.transactionSequence;
    }

    private void addBlockToBlockchain(Block block){
        block.mineBlock(this.findFreeMiner(), Configuration.instance.difficultyLevel);
        this.blockchain.add(block);
        Service.logNetworkMessage("Blockchain:");
        Service.logNetworkMessage(Service.getJson(this.blockchain));
    }

    private PublicKey findFreeMiner(){
        Random rnd = new Random();
        int rndNb = rnd.nextInt(0,3);
        return  rndNb == 0 ? miners.get("Bob").getPublicKey() : rndNb == 1 ? miners.get("Eve").getPublicKey() :  miners.get("Sam").getPublicKey();
    }

    public Double buyBitcoin(PublicKey buyer, Double amount){
        if(amount < miners.get("SatoshiNakamato").getBalance()){
            addTransactionToBlockchain(miners.get("SatoshiNakamato").sendFunds(buyer, amount));
            Service.logNetworkMessage(amount + "Bitcoin bought by" + buyer.toString());
            return amount;
        }
        return null;
    }

    public void addTransactionToBlockchain(Transaction transaction){
        Block block = new Block(this.blockchain.get(this.blockchain.size() - 1).getHash());
        block.addTransaction(transaction);
        Service.logNetworkMessage("New Transaction");
        this.addBlockToBlockchain(block);
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
                return;
            }

            if (!previousBlock.getHash().equals(currentBlock.getPreviousHash())) {
                Service.logNetworkMessage("#trevious hashes not equal");
                return;
            }

            if (!currentBlock.getHash().substring(0, Configuration.instance.difficultyLevel).equals(hashTarget)) {
                Service.logNetworkMessage("#block not mined");
                return;
            }

            TransactionOutput tempOutput;
            for (int t = 0; t < currentBlock.getTransactions().size(); t++) {
                Transaction currentTransaction = currentBlock.getTransactions().get(t);

                if (currentTransaction.verifySignature()) {
                    Service.logNetworkMessage("#Signature on Transaction(" + t + ") is Invalid");
                    return;
                }

                if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    Service.logNetworkMessage("#Inputs are not equal to oututs on Transaction(" + t + ")");
                    return;
                }

                for (TransactionInput input : currentTransaction.getInputs()) {
                    tempOutput = tempUTXOs.get(input.getId());

                    if (tempOutput == null) {
                        Service.logNetworkMessage("#referenced input on transaction(" + t + ") is missing");
                        return;
                    }

                    if (input.getUTX0().getValue() != tempOutput.getValue()) {
                        Service.logNetworkMessage("#referenced input on transaction(" + t + ") value invalid");
                        return;
                    }

                    tempUTXOs.remove(input.getId());
                }

                for (TransactionOutput output : currentTransaction.getOutputs()) {
                    tempUTXOs.put(output.getID(), output);
                }

                if (currentTransaction.getOutputs().get(0).getRecipient() != currentTransaction.getRecipient()) {
                    Service.logNetworkMessage("#transaction(" + t + ") output recipient is invalid");
                    return;
                }

                if (currentTransaction.getOutputs().get(1).getRecipient() != currentTransaction.getSender()) {
                    Service.logNetworkMessage("#transaction(" + t + ") output 'change' is not sender");
                    return;
                }
            }
        }
        Service.logNetworkMessage("blockchain valid");
    }

}
