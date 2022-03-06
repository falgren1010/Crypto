package Console;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class Reportfactory {
    @SuppressWarnings("unchecked")
    public static Object build() {
        Object componentPort = null;
        try {
            URL[] urls={new File(Configuration.instance.pathToJavaArchive + "report.jar").toURI().toURL()};
            URLClassLoader urlClassLoader = new URLClassLoader(urls, Reportfactory.class.getClassLoader());
            Class reportclass= Class.forName("Crypt", true, urlClassLoader);
            Object reportinstance = reportclass.getMethod("getInstance").invoke(null);
            componentPort = reportclass.getDeclaredField("port").get(reportinstance);

        }catch (Exception e){e.printStackTrace();}
        return componentPort;
    }
}
