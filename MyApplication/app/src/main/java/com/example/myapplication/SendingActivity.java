package com.example.myapplication;

import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class SendingActivity extends ActivityBase {

    private TextView mTitle;
//    private Switch mOnOff;
    private TextView mHeartBeat;
    private ProgressBar mProgressBar;
    private ImageView mHeart;
    private ImageView mArc;
    private ImageView mAnimatedHeartVector;
    private Button mOffOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending);
        mTitle = (TextView)findViewById(R.id.sending_activity_title);
        mHeartBeat = (TextView) findViewById(R.id.sending_activity_heartBeat);
        mHeart = (ImageView)findViewById(R.id.heart);
        mArc = (ImageView)findViewById(R.id.arc);
        mAnimatedHeartVector = (ImageView)findViewById(R.id.animated_heart_vector);
        mOffOn = (Button)findViewById(R.id.button_on2);
        mAnimatedHeartVector.setVisibility(View.INVISIBLE);
        final boolean[] b = {false};
//        mOnOff.setChecked(false);

        mOffOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    public void run() {
                        while (b[0]) {
                            InputStream caInput = getResources().openRawResource(R.raw.clientkeystore);
                            InputStream caInput2 = getResources().openRawResource(R.raw.truststoreclient);

                            KeyStore keyStore = null;
                            try {
                                keyStore = KeyStore.getInstance("BKS");
                                keyStore.load(caInput, "client123456789".toCharArray());
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
                                trustStore.load(caInput2, "pfe123456789".toCharArray());
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
                            final SSLContext finalSc = sc;
                            final Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
                            handler.post(new Runnable() {
                                public void run() {
                                    String hb = String.valueOf(heartBeatRand());
                                    mHeart.startAnimation(anim);
                                    animate(mAnimatedHeartVector);
                                    SendHeart sd = new SendHeart(finalSc, hb, address, String.valueOf(8282));
                                    sd.execute();
                                    mHeartBeat.setText(hb);
                                }
                            });
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                };
                Thread thread = new Thread(runnable);
                if(b[0]==true)
                {
                    b[0]=false;
                    mAnimatedHeartVector.setVisibility(View.INVISIBLE);
                    mHeartBeat.setText("--");
                }else
                {
                    b[0] =true;
                    mAnimatedHeartVector.setVisibility(View.VISIBLE);
                    thread.start();
                }
            }});

//        mOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                final Handler handler = new Handler();
//                Runnable runnable = new Runnable() {
//                    public void run() {
//                        while (b[0]) {
//                            InputStream caInput = getResources().openRawResource(R.raw.clientkeystore);
//                            InputStream caInput2 = getResources().openRawResource(R.raw.truststoreclient);
//
//                            KeyStore keyStore = null;
//                            try {
//                                keyStore = KeyStore.getInstance("BKS");
//                                keyStore.load(caInput,"client123456789".toCharArray());
//                            } catch (KeyStoreException e) {
//                                e.printStackTrace();
//                            } catch (CertificateException e) {
//                                e.printStackTrace();
//                            } catch (NoSuchAlgorithmException e) {
//                                e.printStackTrace();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                            try {
//                                System.out.println(keyStore.aliases().toString());
//                            } catch (KeyStoreException e) {
//                                e.printStackTrace();
//                            }
//                            KeyStore trustStore = null;
//                            try {
//                                trustStore = KeyStore.getInstance("BKS");
//                                trustStore.load(caInput2,"pfe123456789".toCharArray());
//                            } catch (KeyStoreException e) {
//                                e.printStackTrace();
//                            } catch (CertificateException e) {
//                                e.printStackTrace();
//                            } catch (NoSuchAlgorithmException e) {
//                                e.printStackTrace();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//
//                            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
//                            TrustManagerFactory tmf = null;
//                            try {
//                                tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
//                                tmf.init(trustStore);
//                            } catch (NoSuchAlgorithmException | KeyStoreException e) {
//                                e.printStackTrace();
//                            }
//
//                            KeyManagerFactory kmf =
//                                    null;
//                            SSLContext sc = null;
//                            try {
//                                kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//                                kmf.init(keyStore, "client123456789".toCharArray());
//                                sc = SSLContext.getInstance("TLS");
//                                sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new
//                                        SecureRandom());
//                            } catch (NoSuchAlgorithmException e) {
//                                e.printStackTrace();
//                            } catch (UnrecoverableKeyException e) {
//                                e.printStackTrace();
//                            } catch (KeyStoreException e) {
//                                e.printStackTrace();
//                            } catch (KeyManagementException e) {
//                                e.printStackTrace();
//                            }
//                            final SSLContext finalSc = sc;
//                            final Animation anim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadein);
//                            handler.post(new Runnable(){
//                                public void run() {
//                                    String hb=String.valueOf(heartBeatRand());
//                                    mHeart.startAnimation(anim);
//                                    animate(mAnimatedHeartVector);
//                                    SendHeart sd = new SendHeart(finalSc,hb,address,String.valueOf(port));
//                                    sd.execute();
//                                    mHeartBeat.setText(hb);
//                                }
//                            });
//                            try {
//                                Thread.sleep(1000);
//                            }
//                            catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                };
//                Thread thread = new Thread(runnable);
//                if (isChecked) {
//                    b[0] =true;
//                    mAnimatedHeartVector.setVisibility(View.VISIBLE);
//                    thread.start();
//
//                } else {
//                    b[0]=false;
//                    mAnimatedHeartVector.setVisibility(View.INVISIBLE);
//                    mHeartBeat.setText("--");
//                    // The toggle is disabled
//                }
//            }
//        });
    }

    protected int heartBeatRand()
    {
        int nombreAleatoire = 70 + (int)(Math.random() * ((120 - 70) + 1));
        return nombreAleatoire;
    }

    public void animate(View view) {
        ImageView v = (ImageView) view;
        Drawable d = v.getDrawable();
        if (d instanceof AnimatedVectorDrawableCompat)
        {
            AnimatedVectorDrawableCompat avd = (AnimatedVectorDrawableCompat)d;
            avd.start();
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (d instanceof AnimatedVectorDrawable)
            {
                AnimatedVectorDrawable avd = (AnimatedVectorDrawable)d;
                avd.start();
            }
        }
    }
}
