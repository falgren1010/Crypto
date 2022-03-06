package Console.Report.src.main.java;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.SecureRandom;

public final class Crypt {
	private static final Crypt instance = new Crypt();
    private SecretKey key;
    private String algorithm;
    private IvParameterSpec ivParameterSpec;
    private File folder;
    private String name;
	
	public static Crypt getInstance() {return instance;}

    public SecretKey generateKey(int n) throws Exception{
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    public IvParameterSpec generateIv(){
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public void encryptFile(String algorithm, SecretKey key, IvParameterSpec iv, File inputFile, File outputFile) throws Exception{
        Cipher cipher= Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE,key,iv);
        FileInputStream inputStream = new FileInputStream(inputFile);
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        byte[] buffer= new byte[64];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer))!=-1){
            byte[] output = cipher.update(buffer, 0, bytesRead);
            if(output != null){
                outputStream.write(output);
            }
        }
        byte[] outputBytes = cipher.doFinal();
        if (outputBytes !=null){
            outputStream.write(outputBytes);
        }
        inputStream.close();
        outputStream.close();
    }
    public void decryptFile(String algorithm, SecretKey key, IvParameterSpec iv, File inputFile, File outputFile) throws Exception{
        Cipher cipher= Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE,key,iv);
        FileInputStream inputStream = new FileInputStream(inputFile);
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        byte[] buffer= new byte[64];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer))!=-1){
            byte[] output = cipher.update(buffer, 0, bytesRead);
            if(output != null){
                outputStream.write(output);
            }
        }
        byte[] outputBytes = cipher.doFinal();
        if (outputBytes !=null){
            outputStream.write(outputBytes);
        }
        inputStream.close();
        outputStream.close();
    }

    public void init() throws Exception {
        key= generateKey(256);
        algorithm="AES/CBC/PKCS5Padding";
        ivParameterSpec= generateIv();
        folder= new File("data");
        for(File file: folder.listFiles()){
            name=file.getName();
            File encryptedFile=new File("data/"+name+".mcg");
            String path=encryptedFile.getAbsolutePath();
            encryptFile(algorithm,key,ivParameterSpec,file,encryptedFile);
            file.delete();
        }
    }

    public void end() throws Exception{
        String[] decryptedName;
        for(File file: folder.listFiles()){
            name=file.getName();
            decryptedName=name.split(".mcg");
            File decryptedFile=new File("data/"+decryptedName[0]);
            String path=decryptedFile.getAbsolutePath();
            decryptFile(algorithm,key,ivParameterSpec,file,decryptedFile);
            file.delete();
        }
    }

    public void delete() {
        for(File file : folder.listFiles()){
            file.delete();
        }
    }
}