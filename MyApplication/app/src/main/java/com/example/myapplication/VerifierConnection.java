package com.example.myapplication;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class VerifierConnection {
    Context context;
    String address;
    int port;
    public VerifierConnection(Context context, String address, String port)
    {
        this.context = context;
        this.address = address;
        this.port = Integer.valueOf(port);
    }
    protected boolean execute() {

        InputStream caInput = context.getResources().openRawResource(R.raw.clientkeystore);
        InputStream caInput2 = context.getResources().openRawResource(R.raw.truststoreclient);

        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("BKS");
            keyStore.load(caInput,"client123456789".toCharArray());
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            System.out.println(keyStore.aliases().toString());
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        KeyStore trustStore = null;
        try {
            trustStore = KeyStore.getInstance("BKS");
            trustStore.load(caInput2,"pfe123456789".toCharArray());
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = null;
        try {
            tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(trustStore);
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
        }

        KeyManagerFactory kmf =
                null;
        SSLContext sc = null;
        try {
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, "client123456789".toCharArray());
            sc = SSLContext.getInstance("TLS");
            sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new
                    SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }


        SSLSocketFactory f = sc.getSocketFactory();
        String msg = "Test message";

        try {
            SSLSocket c = (SSLSocket) f.createSocket(address, port);

            c.startHandshake();
            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
            BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()));
            w.write(msg, 0, msg.length());
            w.newLine();
            w.flush();

            // now read the socket
            String m = null;
            while ((m = r.readLine()) != null) {
                System.out.println(m);
            }

        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
