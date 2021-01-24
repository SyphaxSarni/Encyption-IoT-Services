package com.example.myapplication;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SendHeart extends AsyncTask<InputStream, Integer, Boolean> {

    String address;
    int port;
    SSLContext sc;
    String msg;

    public SendHeart(SSLContext sc, String msg, String address, String port) {
        this.address = address;
        this.port = Integer.valueOf(port);
        this.sc = sc;
        this.msg = msg;
    }

    @Override
    protected Boolean doInBackground(InputStream... inputStreams) {

        SSLSocketFactory f = sc.getSocketFactory();
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
