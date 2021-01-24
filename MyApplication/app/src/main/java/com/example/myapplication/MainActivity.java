package com.example.myapplication;

import androidx.annotation.IntegerRes;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.KeyStoreBuilderParameters;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends ActivityBase {

    public static final boolean DEBUG = false;
    public static final int HTTPS_PORT = 8282;
    public static final String HTTPS_HOST = "localhost";
    public static final String TRUSTTORE_LOCATION = "app/Ressources/ClientKeyStore.jks";

    private TextView mTitleText;
    private EditText mHostAdress;
    private EditText mHostPort;
    private Button mLogin;
    private TextView mError;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHostAdress = (EditText) findViewById(R.id.main_activity_host_adress);
        mLogin = (Button) findViewById(R.id.main_activity_se_connecter);
        mError = (TextView) findViewById(R.id.main_activity_error);
        mError.setVisibility(View.INVISIBLE);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                address = mHostAdress.getText().toString();
                ConnectionSSL co = new ConnectionSSL(getApplicationContext(), mHostAdress.getText().toString(), "8282");
                co.execute();
                System.out.println("ici");
                boolean connected = false;
                try {
                    connected = (Boolean)co.get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("connected = "+connected);
                if(connected)
                {
                    System.out.println("Connecté");
                    Intent sendingActivity = new Intent(MainActivity.this, SendingActivity.class);
                    startActivity(sendingActivity);
                }
                else {
                    mError.setVisibility(View.VISIBLE);
                }
            }
        });

    }

}



