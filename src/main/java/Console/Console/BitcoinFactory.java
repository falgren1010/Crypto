package Console.Console;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class BitcoinFactory {
    @SuppressWarnings("unchecked")
    public static Object build() {
        Object componentPort = null;
        try {
            URL[] urls={new File(Configuration.instance.pathToJavaArchive + "bitcoin.jar").toURI().toURL()};
            URLClassLoader urlClassLoader = new URLClassLoader(urls, BitcoinFactory.class.getClassLoader());
            Class bitcoinclass= Class.forName("BitcoinNetwork", true, urlClassLoader);
            Object bitcoininstance = bitcoinclass.getMethod("getInstance").invoke(null);
            componentPort = bitcoinclass.getDeclaredField("port").get(bitcoininstance);

        }catch (Exception e){e.printStackTrace();}
        return componentPort;
    }
}
