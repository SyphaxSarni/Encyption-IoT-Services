import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.util.Base64;
import javax.crypto.Cipher;
import java.io.* ;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.spec.X509EncodedKeySpec;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.io.FileInputStream;
import java.security.PublicKey;



public class RSAEncryptionModule
{
    //Defining variables
    private PrivateKey privateKey;
    private PublicKey publicKey;

    //Method that converts a file to public key data type
    public static PublicKey FileToPublicKey(String filename) throws Exception
    {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        //x509 is the format of the certificat
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyfactory = KeyFactory.getInstance("RSA");
        return keyfactory.generatePublic(spec);

    }// End of FileToPublicKey method

    //Method that writes a publicKey object to a file
    public void writePublicKeyToFile(String path, byte[] key) throws IOException
    {
        //create a file object wich's directory is "path"
        File file = new File(path);
        file.getParentFile().mkdirs();
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        //write the generated key to the file created
        fileOutputStream.write(key);
        fileOutputStream.flush();
        fileOutputStream.close();
    }

    //Encrypt Method by passing the public key and the text to encrypt
    public static String RSAencryptString(String plainText, PublicKey publicKey) throws Exception
    {
        //Creating a new instance of the RSA Cipher
        Cipher encryptCipher = Cipher.getInstance("RSA");

        //Initialize the cipher to encrypt mode with the public key
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        //Passing the plain text bytes and get back the bytes of the encrypted message as an array
        byte[] cipherText = encryptCipher.doFinal(plainText.getBytes("UTF-8"));
        //Encode these bytes with a base64 and return it as a string
        return Base64.getEncoder().encodeToString(cipherText);
    }//End of method Encrypt

    //Decrypt Method by passing the Cipher text and the privatekey
    public static String RSAdecryptString(String cipherText, PrivateKey privateKey) throws Exception
    {
        //decode the base 64 bytes
        byte[] bytes = Base64.getDecoder().decode(cipherText);
        //Creating a new instance of the RSA Cipher
        Cipher decriptCipher = Cipher.getInstance("RSA");
        //Initialize the cipher to decrypt mode with the private key
        decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);
        //Return the decrypted message
        return new String(decriptCipher.doFinal(bytes), "UTF-8");
    }//End of Method decrypt

    private static void processFile(Cipher cipher, InputStream in, OutputStream out) throws Exception
    {


        byte[] ibuf = new byte[1024];
        int len;
        while ((len = in.read(ibuf)) != -1)
        {
            byte[] obuf = cipher.update(ibuf, 0, len);
            if ( obuf != null )
                out.write(obuf);
        }
        byte[] obuf = cipher.doFinal();
        if ( obuf != null )
            out.write(obuf);

    }


    public static void RSAencryptFile(String pathIn, String pathOut, PublicKey publicKey) throws Exception
    {

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        try (FileInputStream in = new FileInputStream(pathIn);
             FileOutputStream out = new FileOutputStream(pathOut))
        {
            processFile(cipher,in,out);
        }

    }

    public static void RSAdecryptFile(String pathIN,String pathOut) throws Exception
    {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, RSAEncryptionModule.getPrivateKey());
        try (FileInputStream in = new FileInputStream(pathIN);
             FileOutputStream out = new FileOutputStream(pathOut))
        {
            processFile(cipher,in,out);
        }
    }


    //Signing a message using the private key of the sender
    public static String sign (String plainText, PrivateKey privateKey) throws Exception
    {

        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(plainText.getBytes("UTF-8"));

        byte[] signature = privateSignature.sign();

        return Base64.getEncoder().encodeToString(signature);
    }//End of method sign

    //Verify whether a received message didn't change using the public key of the sender
    public static boolean verify(String plainText, String signature, PublicKey publicKey) throws Exception
    {

        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(publicKey);
        publicSignature.update(plainText.getBytes("UTF-8"));

        byte[] signatureBytes = Base64.getDecoder().decode(signature);

        return publicSignature.verify(signatureBytes);
    }//End of method verify


    //Convert private key to a an encoded PKCS8 privateKey encoded datatype
    public static PrivateKey getPrivateKey() throws Exception
    {
        byte[] keyBytes = Files.readAllBytes(Paths.get("C:/Users/Computer/Desktop/RSA/FogPrivateKey.der"));

        //PKCS8Encoded is the encoded format read by java
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        //specify the algorithm used
        KeyFactory keyfactory = KeyFactory.getInstance("RSA");
        return keyfactory.generatePrivate(spec);
    }

    //Convert x509 certificate to a publicKey datatype
    public static PublicKey getPublicKey() throws Exception
    {
        FileInputStream fileInputStream = new FileInputStream("C:/Users/Computer/Desktop/RSA/FogPublicCertificate.crt");
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
        X509Certificate fogCertificate = (X509Certificate) certificateFactory.generateCertificate(fileInputStream);
        return fogCertificate.getPublicKey();
    }


}//End of class
