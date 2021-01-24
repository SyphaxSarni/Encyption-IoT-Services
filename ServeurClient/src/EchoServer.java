import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.net.ServerSocketFactory;
import javax.net.ssl.*;

public class EchoServer {

    public static final boolean DEBUG = false;
    public static final int HTTPS_PORT = 8282;
    public static final String KEYSTORE_LOCATION = "C:/Keys/ServerKeyStore.jks";
    public static final String KEYSTORE_PASSWORD = "pfe123456789";

    // main program
    public static void main(String argv[]) throws Exception {

        // set system properties, alternatively you can also pass them as
        // arguments like -Djavax.net.ssl.keyStore="keystore"....
        System.out.println("Serveur lancé");
        System.setProperty("javax.net.ssl.trustStore", KEYSTORE_LOCATION);
        System.setProperty("javax.net.ssl.trustStorePassword", KEYSTORE_PASSWORD);
        System.setProperty("javax.net.ssl.keyStore", KEYSTORE_LOCATION);
        System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASSWORD);

        if (DEBUG)System.setProperty("javax.net.debug", "ssl:record");

        EchoServer server = new EchoServer();
        server.startServer();
    }

    // Start server
    public void startServer() {
        try {
            ServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket serversocket = (SSLServerSocket) ssf.createServerSocket(HTTPS_PORT);
            serversocket.setNeedClientAuth(true);
            while (true) {
                Socket client = serversocket.accept();
                ProcessRequest cc = new ProcessRequest(client);
            }
        } catch (Exception e) {
            System.out.println("Exception:" + e.getMessage());
        }
    }
}

class ProcessRequest extends Thread {

    Socket client;
    BufferedReader is;
    DataOutputStream out;

    public ProcessRequest(Socket s) { // constructor
        client = s;
        try {
            is = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new DataOutputStream(client.getOutputStream());
        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        this.start(); // Thread starts here...this start() will call run()
    }

    public void run() {
        try {
            // get a request and parse it.
            String request = is.readLine();
            System.out.println("Heartrate value: " + request+" BPS");
            try {
                writeOnFile(request);
                out.writeBytes("HTTP/1.0 200 OK\r\n");
                out.writeBytes("Content-Type: text/html\r\n");
                out.writeBytes("<html><head>Server Page: Hope you are liking this tutorial!</head>\r\n");
                out.writeBytes("<body><b/><p>Client sent: ");
                out.writeBytes(request + "</p></body></html>\r\n");
                out.flush();
            } catch (Exception e) {
                out.writeBytes("Content-Type: text/html\r\n");
                out.writeBytes("HTTP/1.0 400 " + e.getMessage() + "\r\n");
                out.flush();
            } finally {
                out.close();
            }
            client.close();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    public void writeOnFile(String msg)
    {
        DateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH");
        Date d = new Date();
        System.out.println("written to file "+format.format(d));
        String msg2 = new String(msg+"\n");
        Path fichier = Paths.get("C:/Users/Syphax/Desktop/Projet Fin D'étude/WorkSpace/ServeurClient/Données/"+format.format(d)+".txt");
        if(!(new File(fichier.toUri()).exists())) {
            try {
                new File(fichier.toUri()).createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Files.write(fichier, msg2.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}