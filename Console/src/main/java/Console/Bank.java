package Console;

public class Bank {
    private double money;
    private double moneyround;

    public Bank(){
        this.money=5000;
    }

    public void pay(double Coins){
        money=money*0.000019*1000;
        money=money-(Coins*1000);
        money=money/0.000019/1000;

        moneyround=money;
        money=Math.round(money*100)/100;
        if(money>moneyround){
            money=money-0.01;
        }
    }
}
