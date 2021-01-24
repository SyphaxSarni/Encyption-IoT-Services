import java.net.ServerSocket;
import java.net.Socket;
import java.security.*; 
import java.io.* ;
import java.util.Scanner;
import java.util.Base64;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.spec.X509EncodedKeySpec;

public class ServerSide
{

	//ID is a unique integer that represents the number of the session 0512 is the first ID it will be incremented for every session
	static int ID =0;

	// A method to  send files 
	public void SendFile(File file, Socket socket)  throws IOException 
	{
		//Allocate memory space for an array where each field contain enough space for integers  
		byte byteArray [] = new byte [(int)file.length()]; 
		FileInputStream  fileInput = new FileInputStream(file);
		BufferedInputStream buffinput = new BufferedInputStream(fileInput);
		buffinput.read(byteArray,0,byteArray.length);
		OutputStream output = socket.getOutputStream();
		output.write(byteArray,0,byteArray.length);

		//close the streams  
		fileInput.close();
		buffinput.close();
		output.close();
		
	}//End of SendFile method 

	// A method to Receive files
	public void ReceiveFile(String file_to_receive, int file_size, Socket socket) throws IOException,InterruptedException
	{
			int current = 0;
	  		int bytesRead;
	  		//Instanciate an array object with the size of the file 
      		byte  mybytearray [] = new byte [file_size];

     		InputStream inputStream = socket.getInputStream();
      		FileOutputStream fileOutStream = new FileOutputStream(file_to_receive);
      		BufferedOutputStream bufferOutStream = new BufferedOutputStream(fileOutStream);
      		//read bytes from the sent file and store them in the declared array "mybytearray"
      		bytesRead = inputStream.read(mybytearray,0,mybytearray.length);
      		current = bytesRead;
      		do 
     		{
        		bytesRead = inputStream.read(mybytearray, current, (mybytearray.length-current));
        		
        		if(bytesRead >= 0) 
         		{
      				System.out.print(current);

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
  	}// End of FileToPublicKey method 
	

	//The main function 
	public static void main(String[] args) throws Exception
	{

		//Create an object from ServerSideSocket class
		ServerSide serverSideSocket = new ServerSide();
		//Create a new instance from the RSAKetPairGenerator class
        RSAKeyPairGenerator fogkeyPairGenerator = new RSAKeyPairGenerator();


		while(true)
		{

			//Increment the ID to name the new session
			ID = ID+1;	

			//Path to the session key 
			String SessionKey_To_Receive = "C:/"+ID+"/SessionKey";
			//Maximum file size to receive (hard coded)  
			int SessionKey_Size = 6022386;
			String SessionKey2_To_Receive = "C:/"+ID+"/SessionKey2";
			//Path to salt used to create the session key
			String Salt_To_Receive = "C:/"+ID+"/Salt";
			//Maximum file size to receive (hard coded)  
			int Salt_Size = 6022386;
			//Path to IV used to create the session key
			String IV_To_Receive = "C:/"+ID+"/iv";
			//Maximum file size to receive (hard coded)  
			int IV_Size = 6022386;
			//Path to the password used to create the session key
			String Password_To_Receive = "C:/"+ID+"/password";
			//Maximum file size to receive (hard coded)  
			int Password_Size = 6022386;


			//Create a new server socket object with port 1010
			System.out.print("[*] Server is up \n");
			ServerSocket server_socket = new ServerSocket(1010);

			//waiting for an incoming request on port 1010
			System.out.print("[*] Waiting ... \n");

			//Accept the client's request 
			Socket socket = server_socket.accept();
			Thread.sleep(1000);
			System.out.print("[*] Client connected \n\n\n");


			//++++++++++++++++++++++++ SECTION 1: RECEIVE FOGS SESSION KEY       +++++++++++++++++++++++++++// 
			
			//Create a folder which will contain all the useful files for the decryption of every session  
			File repertoire = new File("C:/"+ID);
			boolean bool = repertoire.mkdir();

			//RECIEVE FIRST HALF OF SESSION KEY

			//Call the method receiveFile with the directory of the file to receive
			serverSideSocket.ReceiveFile(SessionKey_To_Receive,SessionKey_Size,socket);

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
			socket = server_socket.accept();

			//RECIEVE SECOND HALF OF SESSION KEY

			//Call the method receiveFile with the directory of the file to receive
			serverSideSocket.ReceiveFile(SessionKey2_To_Receive,SessionKey_Size,socket);

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
			socket = server_socket.accept();



			//++++++++++++++++++++++++ SECTION 2: RECEIVE SALT       +++++++++++++++++++++++++++// 
			

			//Call the method receiveFile with the directory of the file to receive
			serverSideSocket.ReceiveFile(Salt_To_Receive,Salt_Size,socket);

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
			socket = server_socket.accept();

			//++++++++++++++++++++++++ SECTION 3: RECEIVE IV       +++++++++++++++++++++++++++// 
			

			//Call the method receiveFile with the directory of the file to receive
			serverSideSocket.ReceiveFile(IV_To_Receive,IV_Size,socket);

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
			socket = server_socket.accept();

			//++++++++++++++++++++++++ SECTION 4: RECEIVE PASSWORD       +++++++++++++++++++++++++++// 
			

			//Call the method receiveFile with the directory of the file to receive
			serverSideSocket.ReceiveFile(Password_To_Receive,Password_Size,socket);

			//check wether the file is received successfuly 
			File passwordfile = new File(Password_To_Receive);

			if(passwordfile.exists())
			{
				Thread.sleep(2000);
				System.out.println("[*] File " + Password_To_Receive + " downloaded \n\n ");
			}
			else //the file doesn't exist 
			{
				System.out.print("Error : The file doesn't exist \n");
			}
      		
      		//reopen the socket because the edn of stream closes the socket
			socket = server_socket.accept();						



			//++++++++++++++++++++++++ SECTION 5: RECEIVE THE NAME OF DATA FILES STORED IN ClOUD +++++++++++++++++++++++++//  

			BufferedReader buffread = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			//print the recieved data 
			String DataFileName = buffread.readLine();
			System.out.print("[*] DataFileName is :" +DataFileName +"\n");

			//Get the ID of the data file 
			int IDtoFind = Integer.parseInt( String.valueOf( DataFileName.charAt(0) ) );


		//+++++++++++++++++++++++++++ SECTION 6: SEND THE SESSION KEY TO SERVER +++++++++++++++++++++++++++//
        	
			//Path to the first half ot Session Key 
			String SessionKeyPath = "C:/"+IDtoFind+"/SessionKey";

     		//send the file to server
        	File sessionKeyFile = new File(SessionKeyPath);
			//Call the SendFile methode from the FogSideSocket class 
			serverSideSocket.SendFile(sessionKeyFile,socket);

			//File sent succesffuly 
			Thread.sleep(2000);
			System.out.print("[*] First half of Session Key returned successfully to Fog \n\n");

      		//reopen the socket because the end of stream closes the socket
			socket = server_socket.accept();

			//Path to the second half ot Session Key 
			SessionKeyPath = "C:/"+IDtoFind+"/SessionKey2";

     		//send the file to server
        	sessionKeyFile = new File(SessionKeyPath);
			//Call the SendFile methode from the FogSideSocket class 
			serverSideSocket.SendFile(sessionKeyFile,socket);

			//File sent succesffuly 
			Thread.sleep(2000);
			System.out.print("[*] Second half of Session Key returned successfully to Fog \n\n");

      		//reopen the socket because the end of stream closes the socket
			socket = server_socket.accept();



		//+++++++++++++++++++++++++++ SECTION 7: SEND THE SALT TO FOG  +++++++++++++++++++++++++++//
        	
			//Path to salt used to create the session key
			String SaltPath = "C:/"+IDtoFind+"/Salt";

     		//send the file to server
        	File SaltFile = new File(SaltPath);


			//Call the SendFile methode from the FogSideSocket class 
			serverSideSocket.SendFile(SaltFile,socket);

			//File received succesffuly 
			Thread.sleep(2000);
			System.out.print("[*] Salt returned successfully to Fog \n\n");
			
      		//reopen the socket because the edn of stream closes the socket
			socket = server_socket.accept();

		//+++++++++++++++++++++++++++ SECTION 8: SEND THE IV TO FOG  +++++++++++++++++++++++++++//
        	
			//Path to IV used to create the session key
			String IVPath = "C:/"+IDtoFind+"/iv";

     		//send the file to server
        	File IVFile = new File(IVPath);
			//Call the SendFile methode from the FogSideSocket class 
			serverSideSocket.SendFile(IVFile,socket);

			//File received succesffuly 
			Thread.sleep(2000);
			System.out.print("[*] IV returned successfully to Fog \n\n");
			
      		//reopen the socket because the edn of stream closes the socket
			socket = server_socket.accept();


		//+++++++++++++++++++++++++++ SECTION 9: SEND THE PASSWORD TO FOG +++++++++++++++++++++++++++//
        	

			//Path to the password used to create the session key
			String PasswordPath = "C:/"+IDtoFind+"/password";

     		//send the file to server
        	File PasswordFile = new File(PasswordPath);

			//Call the SendFile methode from the FogSideSocket class 
			serverSideSocket.SendFile(PasswordFile,socket);

			//File received succesffuly 
			Thread.sleep(2000);
			System.out.print("[*] Password returned successfully to Fog \n\n");
			
      		//reopen the socket because the edn of stream closes the socket
			socket = server_socket.accept();


			//Close connections safely
			//out.close();

			server_socket.close();
			socket.close();

			//pausing the processus for 3 seconds
			Thread.sleep(10000);
			System.out.print("[*] Server is down \n\n\n");
			//pausing the processus for 3 seconds
			Thread.sleep(2000);
			
			

		}//End of while 

		

        

	}//End of main


}//End of class
