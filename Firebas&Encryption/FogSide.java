import java.net.Socket;
import java.security.*;
import java.io.* ;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.spec.X509EncodedKeySpec;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import java.io.FileInputStream;
import java.io.IOException;


public class FogSide
{


    //A method to send files through a tcp socket
    public void SendFile(File file, Socket socket)  throws IOException
    {
        //Allocate memory space for an array where each field contain enough space for integers
        byte byteArray [] = new byte [(int)file.length()];
        FileInputStream  fileInput = new FileInputStream(file);
        BufferedInputStream buffinput = new BufferedInputStream(fileInput);
        buffinput.read(byteArray,0,byteArray.length);
        OutputStream output = socket.getOutputStream();
        output.write(byteArray,0,byteArray.length);
        //close safely
        fileInput.close();
        buffinput.close();
        output.close();

    }//End of SebdFile method


    // A method to Receive files from a tcp socket
    public void ReceiveFile(String file_to_receive, int file_size, Socket socket) throws IOException,InterruptedException
    {

        int current = 0;
        int bytesRead;

        //Create an array object with the size of the file
        byte  mybytearray [] = new byte [file_size];

        InputStream inputStream = socket.getInputStream();
        FileOutputStream fileOutStream = new FileOutputStream(file_to_receive);
        BufferedOutputStream bufferOutStream = new BufferedOutputStream(fileOutStream);

        //Read bytes from the sent file and store them in the declared array "mybytearray"
        bytesRead = inputStream.read(mybytearray,0,mybytearray.length);
        current = bytesRead;

        do
        {
            bytesRead = inputStream.read(mybytearray, current, (mybytearray.length-current));
            if(bytesRead >= 0)
            {
                current += bytesRead;
            }

        } while(bytesRead > -1);

        //write the content of the array to the file
        bufferOutStream.write(mybytearray, 0 , current);
        bufferOutStream.flush();

    }//End of ReceiveFile method

    //Method that converts a file to public key data type
    public static PublicKey FileToPublicKey(String filename) throws Exception
    {

        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyfactory = KeyFactory.getInstance("RSA");
        return keyfactory.generatePublic(spec);
    }

    //The main method
    public static void main(String[] args) throws Exception
    {

        //ID is a unique integer that represents the number of the session 0512 is the first ID it will be incremented for every session
        int ID = 0;

        //Create a new instance of the ClientSideSocket class
        FogSide clientSideSocket = new FogSide();
        //Create a new instance of the RSAEncryptionModule class
        RSAEncryptionModule rsaEncryptionModule = new RSAEncryptionModule();
        //Create a new instance of the AESEncryptionModule class
        AESEncryptionModule aesEncryptionModule = new AESEncryptionModule();

        FileInputStream serviceAccount = new FileInputStream("C:/Users/Computer/Desktop/PFE L3/Private Key/crypto-serv-firebase-adminsdk.json");
        //gs://crypto-serv.appspot.com
        FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).setStorageBucket("crypto-serv.appspot.com").build();

        FirebaseApp.initializeApp(options);
        serviceAccount = new FileInputStream("C:/Users/Computer/Desktop/PFE L3/Private Key/crypto-serv-firebase-adminsdk.json");


        Bucket bucket = StorageClient.getInstance().bucket();

        //For now the loop is true TODO: a loop where the condition is time every X time opens a socket
        while(true)
        {

            //Defining the ip address and the port number to connect with
            String ip = "192.168.0.101";  //windows server"s ip is 192.168.0.105
            int port = 1010; 	   //The tcp port used for this communication is 1010

            //Create a socket instance with the ip&port of the server; requesting server to accept communication
            Socket socket = new Socket(ip,port);
            System.out.print("[*] Connecting ... \n\n");

            //Increment the ID to name the new session
            ID = ID+1;

            //++++++++++++++++++++++++++ SECTION 1: CREATE THE SESSION KEY  +++++++++++++++++++++++++++++++++++++//

            //Create the session key using the CreateKey() method from the AESEncryptionModule class
            SecretKey sessionKey = aesEncryptionModule.CreateKey();

            //++++++++++++++++++++++++++ SECTION 2: ENCRYPT THE HEARTRATE DATA FILE  +++++++++++++++++++++++++++//

            //Get the Public key from fogs certificate to encrypt files
            PublicKey publicKey = rsaEncryptionModule.getPublicKey();

            //Path to the file which will be encrypted
            String FileToEncryptPath ="c:/Users/Computer/Desktop/heartrate.txt";
            String FileToStore = ID+"HeartRateEncrypted.des";

            //Encrypt the heartrate data file using the FileEncrypt method of the AESEncryptionModule class
            aesEncryptionModule.FileEncryption(sessionKey,FileToEncryptPath,FileToStore);
            System.out.print("[*] File : \"" +FileToEncryptPath +"\" is encrypted successfully. \n\n");
            //Upload encrypted heartrate  file to cloud
            UploadObject.uploadObject("crypto-serv",bucket.getName(),FileToStore,"C:/Users/Computer/Desktop/"+FileToStore);

            //++++++++++++++++++++++++++ SECTION 3: ENCRYPTE THE SESSION KEY  ++++++++++++++++++++++++++++++++++//

            //Split the session Key on two halfs
            String firstHalfPath = "sessionKey1";
            String secondHalfPath = "sessionKey2";
            String firstHalfPathEnc = "sessionKey1ENC";
            String secondHalfPathEnc = "sessionKey2ENC";


            //Cal the SplitSessionKey method from the aesEncryptionModule class
            aesEncryptionModule.SplitSessionKey(sessionKey,firstHalfPath,secondHalfPath);

            //Encrypt both halfs of th session key using fogs public key
            rsaEncryptionModule.RSAencryptFile(firstHalfPath,firstHalfPathEnc,publicKey);
            rsaEncryptionModule.RSAencryptFile(secondHalfPath,secondHalfPathEnc,publicKey);



            //+++++++++++++++++++++++++++ SECTION 4: SEND THE SESSION KEY TO SERVER +++++++++++++++++++++++++++//

        	/*
        	//Creating a new file which contains the session Key of the session number ID
			String SessionKeyPath ="SessionKey";
			//FileOutputStream fileOutputStream = new FileOutputStream(SessionKeyPath);

        	//Write the encrypted session key to a file
     		Fileter myFileWriter = new FileWriter(SessionKeyPath);
      		myFileWriter.write(sessionKeyAsCipher);
     		myFileWriter.close();Wri
     		*/

            //send the file to server
            File sessionKeyFile = new File(firstHalfPathEnc);
            //Call the SendFile methode from the FogSideSocket class
            clientSideSocket.SendFile(sessionKeyFile,socket);

            //File received succesffuly
            Thread.sleep(2000);

            boolean bool = sessionKeyFile.delete();

            socket = new Socket(ip,port);

            sessionKeyFile = new File(secondHalfPathEnc);
            //Call the SendFile methode from the FogSideSocket class
            clientSideSocket.SendFile(sessionKeyFile,socket);

            //File received succesffuly
            Thread.sleep(2000);

            bool =sessionKeyFile.delete();

            System.out.print("[*] Session Key Sent successfully to Server \n\n");

            socket = new Socket(ip,port);

            //+++++++++++++++++++++++++++ SECTION 4: SEND THE SALT OF THE SESSION KEY TO SERVER +++++++++++++++++++++++++//


            //Creating a new file which contains the session Key of the session number ID
            String SaltPath ="salt";
            String SaltEnc ="SaltPath";
            //FileOutputStream fileOutputStream = new FileOutputStream(SessionKeyPath);

            //Encrypt the File of the salt used for the AES encryption
            rsaEncryptionModule.RSAencryptFile(SaltPath,SaltEnc,publicKey);

            //rsaEncryptionModule.RSAdecryptFile("SaltPath","decryptedSuccess");
            //System.out.print("[*] Salt decrypted \n");

            File saltFile = new File(SaltEnc);
            //Call the SendFile methode from the FogSideSocket class
            clientSideSocket.SendFile(saltFile,socket);

            //File received succesffuly
            Thread.sleep(2000);
            System.out.print("[*] Salt Sent successfully to Server \n\n");

            //Delete the salt file
            File SaltFile = new File(SaltPath);


            //Delete received files
            bool= SaltFile.delete();
            bool = saltFile.delete();

            Thread.sleep(2000);

            socket = new Socket(ip,port);

            //+++++++++++++++++++++++++++ SECTION 4: SEND THE IV OF THE SESSION KEY TO SERVER +++++++++++++++++++++++++//


            //Creating a new file which contains the session Key of the session number ID
            String IVPath ="iv";
            String IVEnc ="ivPath";
            //FileOutputStream fileOutputStream = new FileOutputStream(SessionKeyPath);

            //Encrypt the File of the salt used for the AES encryption
            rsaEncryptionModule.RSAencryptFile(IVPath,IVEnc,publicKey);

            File ivFile = new File(IVEnc);
            //Call the SendFile methode from the FogSideSocket class
            clientSideSocket.SendFile(ivFile,socket);

            //File received succesffuly
            Thread.sleep(2000);
            System.out.print("[*] IV Sent successfully to Server \n\n");

            //Delete the iv file
            File IVFile = new File(IVPath);
            bool = IVFile.delete();
            bool = ivFile.delete();

            Thread.sleep(2000);

            socket = new Socket(ip,port);

            //+++++++++++++++++++++++++++ SECTION 4: SEND THE PASSWORD OF THE SESSION KEY TO SERVER +++++++++++++++++++++++++//


            //Creating a new file which contains the session Key of the session number ID
            String PasswordPath ="password";
            String PasswordEnc ="passwordPath";
            //FileOutputStream fileOutputStream = new FileOutputStream(SessionKeyPath);

            //Encrypt the File of the salt used for the AES encryption
            rsaEncryptionModule.RSAencryptFile(PasswordPath,PasswordEnc,publicKey);

            File passwordFile = new File(PasswordEnc);
            //Call the SendFile methode from the FogSideSocket class
            clientSideSocket.SendFile(passwordFile,socket);

            File PasswordFile = new File(PasswordPath);
            //File received succesffuly
            Thread.sleep(2000);
            System.out.print("[*] Password Sent successfully to Server \n\n");

            //delete files
            bool = PasswordFile.delete();
            bool = passwordFile.delete();

            Thread.sleep(2000);

            socket = new Socket(ip,port);




            //++++++++++++++++++++++++++ SECTION 5: DECRYPTING DATA FILE STORED IN CLOUD +++++++++++++++++++++++++++++++++++//


            String DataFileName = ID+"HeartRateEncrypted.des";

            //download file from cloud
            DownloadObject.downloadObject("crypto-serv",bucket.getName(),DataFileName,"C:/Users/Computer/Desktop/cloudownloadtest.des");

            //Send the name of the file to server to get all usefull files for decryption
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);

            //Writes the string then terminates the line
            out.println(DataFileName);

            //Path to the session key
            String SessionKey_To_Receive = "C:/Users/Computer/Desktop/SessionKey1Encrypted";
            //Maximum file size to receive (hard coded)
            int SessionKey_Size = 6022386;
            String SessionKey2_To_Receive = "C:/Users/Computer/Desktop/SessionKey2Encrypted";
            //Path to salt used to create the session key
            String Salt_To_Receive = "C:/Users/Computer/Desktop/SaltEncrypted";
            //Maximum file size to receive (hard coded)
            int Salt_Size = 6022386;
            //Path to IV used to create the session key
            String IV_To_Receive = "C:/Users/Computer/Desktop/ivEncrypted";
            //Maximum file size to receive (hard coded)
            int IV_Size = 6022386;
            //Path to the password used to create the session key
            String Password_To_Receive = "C:/Users/Computer/Desktop/passwordEncrypted";
            //Maximum file size to receive (hard coded)
            int Password_Size = 6022386;




            //++++++++++++++++++++++++ SECTION 6: RECEIVE SESSION KEY  FROM SERVER     +++++++++++++++++++++++++++//


            //Call the method receiveFile with the directory of the file to receive
            clientSideSocket.ReceiveFile(SessionKey_To_Receive,SessionKey_Size,socket);

            //check wether the file is received successfuly
            File sessionkeyfile = new File(SessionKey_To_Receive);

            if(sessionkeyfile.exists())
            {
                Thread.sleep(2000);
                System.out.println("[*] File " + SessionKey_To_Receive + " downloaded \n\n");
            }
            else //the file doesn't exist
            {
                System.out.print("Error : The file doesn't exist \n");
            }

            //reopen the socket because the edn of stream closes the socket
            socket = new Socket(ip,port);




            //Call the method receiveFile with the directory of the file to receive
            clientSideSocket.ReceiveFile(SessionKey2_To_Receive,SessionKey_Size,socket);

            //check wether the file is received successfuly
            sessionkeyfile = new File(SessionKey2_To_Receive);

            if(sessionkeyfile.exists())
            {
                Thread.sleep(2000);
                System.out.println("[*] File " + SessionKey2_To_Receive + " downloaded \n\n");
            }
            else //the file doesn't exist
            {
                System.out.print("Error : The file doesn't exist \n");
            }

            //reopen the socket because the edn of stream closes the socket
            socket = new Socket(ip,port);



            //++++++++++++++++++++++++ SECTION 2: RECEIVE SALT       +++++++++++++++++++++++++++//


            //Call the method receiveFile with the directory of the file to receive
            clientSideSocket.ReceiveFile(Salt_To_Receive,Salt_Size,socket);

            //check wether the file is received successfuly
            File saltfile = new File(Salt_To_Receive);

            if(saltfile.exists())
            {
                Thread.sleep(2000);
                System.out.println("[*] File " + Salt_To_Receive + " downloaded \n\n ");
            }
            else //the file doesn't exist
            {
                System.out.print("Error : The file doesn't exist \n");
            }

            //reopen the socket because the edn of stream closes the socket
            socket = new Socket(ip,port);

            //++++++++++++++++++++++++ SECTION 3: RECEIVE IV       +++++++++++++++++++++++++++//


            //Call the method receiveFile with the directory of the file to receive
            clientSideSocket.ReceiveFile(IV_To_Receive,IV_Size,socket);

            //check wether the file is received successfuly
            File ivfile = new File(IV_To_Receive);

            if(ivfile.exists())
            {
                Thread.sleep(2000);
                System.out.println("[*] File " + IV_To_Receive + " downloaded \n\n ");
            }
            else //the file doesn't exist
            {
                System.out.print("Error : The file doesn't exist \n");
            }

            //reopen the socket because the edn of stream closes the socket
            socket = new Socket(ip,port);

            //++++++++++++++++++++++++ SECTION 4: RECEIVE PASSWORD       +++++++++++++++++++++++++++//


            //Call the method receiveFile with the directory of the file to receive
            clientSideSocket.ReceiveFile(Password_To_Receive,Password_Size,socket);

            //check wether the file is received successfuly
            File passwordfile = new File(Password_To_Receive);

            if(passwordfile.exists())
            {
                Thread.sleep(2000);
                System.out.println("[*] File " + Password_To_Receive + " downloaded \n\n");
            }
            else //the file doesn't exist
            {
                System.out.print("Error : The file doesn't exist \n");
            }

            //reopen the socket because the edn of stream closes the socket
            socket = new Socket(ip,port);


            //++++++++++++++++++++++++ SECTION 4: DECRYPT THE HEARTRATE DATA FILE       +++++++++++++++++++++++++++//



            //Decrypt the heartrate file

            String Salt_To_Decrypt ="C:/Users/Computer/Desktop/Decrypt/Salt";
            String IV_To_Decrypt ="C:/Users/Computer/Desktop/Decrypt/IV";
            String Password_To_Decrypt ="C:/Users/Computer/Desktop/Decrypt/Password";
            String SessionKey_To_Decrypt ="C:/Users/Computer/Desktop/Decrypt/SessionKey1";
            String SessionKey2_To_Decrypt ="C:/Users/Computer/Desktop/Decrypt/SessionKey2";



            Thread.sleep(4000);
            //Decrypt Salt
            RSAEncryptionModule.RSAdecryptFile(Salt_To_Receive,Salt_To_Decrypt);
            System.out.print("[*] Salt decrypted \n\n");
            Thread.sleep(2000);

            //Decrypt IV
            rsaEncryptionModule.RSAdecryptFile(IV_To_Receive,IV_To_Decrypt);
            System.out.print("[*] IV decrypted \n\n");
            Thread.sleep(2000);

            //Decrypt Password
            rsaEncryptionModule.RSAdecryptFile(Password_To_Receive,Password_To_Decrypt);
            System.out.print("[*] Password decrypted \n\n");
            Thread.sleep(2000);

            //Decrypt session key
            rsaEncryptionModule.RSAdecryptFile(SessionKey_To_Receive,SessionKey_To_Decrypt);
            System.out.print("[*] First Half of Session Key decrypted \n\n");

            Thread.sleep(2000);

            //Decrypt session key
            rsaEncryptionModule.RSAdecryptFile(SessionKey2_To_Receive,SessionKey2_To_Decrypt);
            System.out.print("[*] Second Half of Session Key decrypted \n\n");

            //Join the two halfs of the session key

            //read the content of the first half file
            InputStream is = new FileInputStream(SessionKey_To_Decrypt);
            BufferedReader buffread = new BufferedReader(new InputStreamReader(is));

            String line = buffread.readLine();
            StringBuilder stringbuild = new StringBuilder();

            while(line != null)
            {
                stringbuild.append(line);
                line = buffread.readLine();
            }

            String firstHalfAsString =stringbuild.toString();

            //read the content of the second half file
            is = new FileInputStream(SessionKey2_To_Decrypt);
            buffread = new BufferedReader(new InputStreamReader(is));

            line = buffread.readLine();
            stringbuild = new StringBuilder();

            while(line != null)
            {
                stringbuild.append(line);
                line = buffread.readLine();
            }

            String secondHalfAsString = stringbuild.toString();


            String key = firstHalfAsString+secondHalfAsString;
            FileWriter myFileWriter = new FileWriter("C:/Users/Computer/Desktop/Decrypt/sessionKey");
            myFileWriter.write(key);
            myFileWriter.close();


            //string to secret key
            byte[] decodedKey = Base64.getDecoder().decode(key);
            // rebuild key using SecretKeySpec
            SecretKey originalsessionKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

            Thread.sleep(2000);
            //Decrypt the downloaded file
            aesEncryptionModule.FileDecryption(originalsessionKey, ID+"HeartRateEncrypted.des");
            System.out.print("[*] HeartRate Data file decrypted successfully \n\n");



            //Delete received files
            bool = sessionkeyfile.delete();
            bool = saltfile.delete();
            bool = ivfile.delete();
            bool = passwordfile.delete();









            //Close socket
            socket.close();

            //to assure sending a connect request after reopenning the socket from the server side
            Thread.sleep(5000);

        }//End of while

    }//End of main





}//End of class



