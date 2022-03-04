import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class Block {
    private final String previousHash;
    private final long timeStamp;
    private final ArrayList<Transaction> transactions = new ArrayList<>();
    private String merkleRoot;
    private String hash;
    private int nonce;

    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public String getHash() {
        return this.hash;
    }

    public String getPreviousHash() {
        return this.previousHash;
    }

    public String calculateHash() {
        return Service.applySha256(previousHash + timeStamp + nonce + merkleRoot);
    }

    public void mineBlock(PublicKey miner, int difficulty) {
        merkleRoot = Service.getMerkleRoot(transactions);
        String target = Service.getDifficultyString(difficulty);

        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }

        TransactionOutput output = new TransactionOutput(miner, Configuration.instance.miningReward, "Mining Reward | " + merkleRoot + " | " + previousHash);
        BitcoinNetwork.getInstance().getUtx0Map().put(output.getID(), output);
        Service.logNetworkMessage("| block mined |" + "\n\t" + " | Hash: " + hash + " |" + "\n\t" +  "| Miner: " + Service.getStringFromKey(miner)+ " |");
    }

    public void addTransaction(Transaction transaction) {
        if (transaction == null) {
            return;
        }

        if (!Objects.equals(previousHash, "0")) {
            if (!transaction.processTransaction()) {
                Service.logNetworkMessage("transaction failed to process");
                return;
            }
        }

        transactions.add(transaction);
    }
}