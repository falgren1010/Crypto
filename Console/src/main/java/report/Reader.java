package report;

import java.io.File;
import java.io.FileReader;

public class Reader {
    private Crypt crypt=new Crypt();
    private String name;
    private String help;
    private byte[] helper;
    private String path;
    private String newPath;

    public void encryptFile(File folder){
        try {
            crypt.init();
            path= folder.getPath();
            for(File file : folder.listFiles()){
                name=file.getName();
                helper=crypt.encrypt(file.toString());
                help=helper.toString();


            }
        }
        catch (Exception e){e.getMessage();}
    }
}
