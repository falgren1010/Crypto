package Bitcoin;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class Transaction {
    private transient final PublicKey sender;
    private transient final PublicKey recipient;
    private final String senderPublicKey;
    private final String recipientPublicKey;
    private final Double value;
    private transient final ArrayList<TransactionOutput> outputs = new ArrayList<>();
    private transient final ArrayList<TransactionInput> inputs;
    private String id;
    private byte[] signature;

    public Transaction(PublicKey from, PublicKey to, Double value, ArrayList<TransactionInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
        this.senderPublicKey = from.toString().replaceAll("\\n",",");
        this.recipientPublicKey = to.toString().replaceAll("\\n",",");
    }

    private String calculateHash() {
        BitcoinNetwork.getInstance().incrementTransactionSequence();
        return Service.applySha256(Service.getStringFromKey(sender) + Service.getStringFromKey(recipient)
                + value + BitcoinNetwork.getInstance().getTransactionSequence());
    }

    public void generateSignature(PrivateKey privateKey) {
        String data = Service.getStringFromKey(sender) + Service.getStringFromKey(recipient) + value;
        signature = Service.applyECDSASig(privateKey, data);
    }

    public boolean verifySignature() {
        String data = Service.getStringFromKey(sender) + Service.getStringFromKey(recipient) + value;
        return !Service.verifyECDSASig(sender, data, signature);

    }

    public boolean processTransaction() {
        if (verifySignature()) {
            Service.logNetworkMessage("#transaction signature failed to verify");
            return false;
        }

        for (TransactionInput i : inputs) {
            i.setUtx0(BitcoinNetwork.getInstance().getUtx0Map().get(i.getId()));
        }

       if (getInputsValue() < Configuration.instance.minimumTransaction) {
            Service.logNetworkMessage("#transaction input to small | " + getInputsValue());
            return false;
        }

        double leftOver = getInputsValue() - value;
        id = calculateHash();
        outputs.add(new TransactionOutput(recipient, value, id));
        outputs.add(new TransactionOutput(sender, leftOver, id));

        for (TransactionOutput o : outputs) {
            BitcoinNetwork.getInstance().getUtx0Map().put(o.getID(), o);
        }

        for (TransactionInput i : inputs) {
            if (i.getUTX0() == null) {
                continue;
            }
            BitcoinNetwork.getInstance().getUtx0Map().remove(i.getUTX0().getID());
        }

        return true;
    }

    public double getInputsValue() {
        double total = 0;

        for (TransactionInput i : inputs) {
            if (i.getUTX0() == null) {
                continue;
            }
            total += i.getUTX0().getValue();
        }

        return total;
    }

    public double getOutputsValue() {
        double total = 0;

        for (TransactionOutput o : outputs) {
            total += o.getValue();
        }

        return total;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PublicKey getSender() {
        return sender;
    }

    public PublicKey getRecipient() {
        return recipient;
    }

    public double getValue() {
        return value;
    }

    public ArrayList<TransactionInput> getInputs() {
        return inputs;
    }

    public ArrayList<TransactionOutput> getOutputs() {
        return outputs;
    }
}