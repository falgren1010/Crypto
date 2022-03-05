package Console;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;


public class Commands {
    private static boolean running=true;
    private final Object reportPort;
    private final Object bitcoinPort;
    private Bank bank=new Bank();
    private boolean state=false;
    public Commands(){
        reportPort= Reportfactory.build();
        bitcoinPort= BitcoinFactory.build();
    }
    Commands commands=new Commands();
    Method wallet=bitcoinPort.getClass().getDeclaredMethod("")

    public static boolean getRunning(){return running;}

    public void listen() throws Exception{
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String input=in.readLine();
        String[] result=input.split(" ");
        if(state==false){
            if(input.equals("launch http://www.trust-me.mcg/report.jar")){
                reportPort.getClass().getDeclaredMethod("init");
                System.out.println("Oops, your files have been encrypted. With a payment of 0.02755 BTC all files will be decrypted.");
                state=true;
            }
        }
        if(state==true){
            if(result[0].equals("exchange")){
                double coins=Double.parseDouble(result[1]);
                bank.pay(coins);
                bitcoinPort.getClass().getDeclaredMethod()

            }
        }
    };
}
