package report;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public final class Crypt {
    private KeyGenerator keyGenerator;
    private SecretKey key;
    private byte[] IV;
    private SecureRandom random;

    public void init() throws Exception{
        keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        key= keyGenerator.generateKey();
        IV = new byte[256];
        random = new SecureRandom();
        random.nextBytes(IV);
    }
    public byte[] encrypt(String data) throws Exception{
        byte[] plaintext=data.getBytes();
        Cipher cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec=new SecretKeySpec(key.getEncoded(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        cipher.init(Cipher.ENCRYPT_MODE,keySpec, ivSpec);
        byte[] crypt= cipher.doFinal(plaintext);
        return crypt;
    }
    public String decrypt(byte[] crypt) throws Exception{
        Cipher cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec=new SecretKeySpec(key.getEncoded(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        cipher.init(Cipher.DECRYPT_MODE,keySpec,ivSpec);
        byte[] cipherText=cipher.doFinal(crypt);
        String data=new String(cipherText);
        return data;
    }
}
