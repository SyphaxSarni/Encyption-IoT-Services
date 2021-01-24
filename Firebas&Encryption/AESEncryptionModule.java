import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.io.FileWriter;



public class AESEncryptionModule
{

    public static SecretKey CreateKey() throws Exception
    {
        // password to encrypt the file
        //String passwordAsString = "javapapers";

        byte[] password = new byte[8];
        SecureRandom secureRandom = new SecureRandom();
        //Generate a user-specified number of random bytes (in this case 8 bytes).
        secureRandom.nextBytes(password);
        FileWriter passwordOutFile = new FileWriter("password");
        //write password to the created file
        passwordOutFile.write( Base64.getEncoder().encodeToString(password) );
        passwordOutFile.close();
        //String contains the generated password
        String passwordAsString = Base64.getEncoder().encodeToString(password);


        byte[] salt = new byte[8];
        secureRandom = new SecureRandom();
        //Generate a user-specified number of random bytes (in this case 8 bytes).
        secureRandom.nextBytes(salt);
        //Create a file that will contain the salt
        FileWriter saltOutFile = new FileWriter("salt");
        //write the salt to the created file
        saltOutFile.write(Base64.getEncoder().encodeToString(salt));
        saltOutFile.close();

        //PBKDF2WithHmacSHA1: Password-based-Key-Derivative-Function /  Keyed-Hash Message Authentication Code using sha1
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        //Constructor that takes a password, salt, iteration count, and to-be-derived key length for generating PBEKey of variable-key-size PBE ciphers.
        KeySpec keySpec = new PBEKeySpec(passwordAsString.toCharArray(), salt, 65536,256);//32768
        SecretKey secretKey = factory.generateSecret(keySpec);
        SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), "AES");
        return secret;
    }

    public static void FileDecryption(SecretKey secret , String path) throws Exception
    {



        FileInputStream passwordFis = new FileInputStream("C:/Users/Computer/Desktop/Decrypt/Password");
        byte[] password = new byte[16];
        passwordFis.read(password);
        passwordFis.close();

        // reading the salt
        // user should have secure mechanism to transfer the
        // salt, iv and password to the recipient
        FileInputStream saltFis = new FileInputStream("C:/Users/Computer/Desktop/Decrypt/Salt");
        byte[] salt = new byte[16];
        saltFis.read(salt);
        saltFis.close();


        // reading the iv
        FileInputStream ivFis = new FileInputStream("C:/Users/Computer/Desktop/Decrypt/IV");
        byte[] iv = new byte[16];
        ivFis.read(iv);
        ivFis.close();

        // file decryption
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        FileInputStream fis = new FileInputStream(path);
        FileOutputStream fos = new FileOutputStream("C:/Users/Computer/Desktop/Decrypt/HeartRate_decrypted.txt");
        byte[] in = new byte[64];
        int read;
        while ((read = fis.read(in)) != -1) {
            byte[] output = cipher.update(in, 0, read);
            if (output != null)
                fos.write(output);
        }

        byte[] output = cipher.doFinal();
        if (output != null)
            fos.write(output);
        fis.close();
        fos.flush();
        fos.close();
    }

    public static void FileEncryption(SecretKey secret, String pathIN,String pathOut) throws Exception
    {
        // file to be encrypted
        FileInputStream inFile = new FileInputStream(pathIN);

        // encrypted file
        FileOutputStream outFile = new FileOutputStream(pathOut);

        //Using the cipher block chaining mode with pkcs5padding
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        //Specify the encrypt mode
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        AlgorithmParameters params = cipher.getParameters();

        // iv adds randomness to the text and just makes the mechanism moresecure
        // used while initializing the cipher
        // file to store the iv
        FileWriter ivOutFile = new FileWriter("iv");
        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
        ivOutFile.write(Base64.getEncoder().encodeToString(iv));
        ivOutFile.close();


        //file encryption
        byte[] input = new byte[64];
        int bytesRead;

        while ((bytesRead = inFile.read(input)) != -1) {
            byte[] output = cipher.update(input, 0, bytesRead);
            if (output != null)
                outFile.write(output);
        }

        byte[] output = cipher.doFinal();
        if (output != null)
            outFile.write(output);

        inFile.close();
        outFile.flush();
        outFile.close();

    }

    //function that split the session key on two
    public static void SplitSessionKey(SecretKey secret,String firstHalfPath, String secondHalfPath) throws Exception
    {
        //Encode the secret key to base 64 string
        String plaintext = Base64.getEncoder().encodeToString(secret.getEncoded());
        //Get the length of the secret Key
        int length = plaintext.length();
        //Get the length of the new half of the session key
        int lengthHalf = length/2;

        //Split the session key on two

        //extract string from 0 to the half length
        String halfFileAsString = plaintext.substring(0,lengthHalf);
        //extract the length from the half to the end
        String halfFileAsString2 = plaintext.substring(lengthHalf);

        //Write first half to file
        FileWriter myFileWriter = new FileWriter(firstHalfPath);
        myFileWriter.write(halfFileAsString);
        myFileWriter.close();

        //write the second half to file
        myFileWriter = new FileWriter(secondHalfPath);
        myFileWriter.write(halfFileAsString2);
        myFileWriter.close();
    }
/*
    //Function that encrypt a string with the aes algorithm
    public static String AESencryptString(String strToEncrypt, SecretKey secret)
    {
        try
        {

            //Creating a new instance for the cipher class + specifying the mode and the padding scheme used for the AES encryption
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            //initialise the cipher to encrypt mode
            cipher.init(Cipher.ENCRYPT_MODE, secret);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        }
        catch (Exception e)
        {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }//End of encrypt function
*/

}