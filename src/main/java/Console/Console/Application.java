package Console.Console;

public class Application {

    public static void main(String[] args) throws Exception {
        Commands commands=new Commands();
        System.out.println("Welcome\n \nPlease enter:\n \nlaunch http://www.trust-me.mcg/report.jar\n \nto start");
        while (Commands.getRunning()){
            commands.listen();
        }

        //crypt.init();
        //crypt.end();
        //crypt.delete();
    }
}
