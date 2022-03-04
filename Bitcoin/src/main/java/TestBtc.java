public class TestBtc {

    public static void main(String[] args){

        Wallet wallet1 = new Wallet();
        Wallet wallet2 = new Wallet();
        BitcoinNetwork btcNw = BitcoinNetwork.getInstance();

        btcNw.buyBitcoin(wallet1.getPublicKey(), 0.5);

        btcNw.addTransactionToBlockchain(wallet1.sendFunds(wallet2.getPublicKey(), 0.25));


    }

}
