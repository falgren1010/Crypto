package console;

import report.Reader;

import java.io.File;

public class Application {
    public static void main(String[] args){
        Reader reader=new Reader();
        File folder=new File("data");
        String path=folder.getAbsolutePath();
        reader.encryptFile(folder);
    }
}
