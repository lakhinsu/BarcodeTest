package com.example.lak.barcodetest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MsgReceiver extends Service {

    static boolean instance;

    SharedPreferences sharedPreferences, sharedPreferences2;

    static final List<String> topics = new ArrayList<>();

    public static MqttAndroidClient client;

    public MsgReceiver() {

        instance=true;

    }

    @Override
    public IBinder onBind(Intent intent) {
        /* TODO: Return the communication channel to the service. */

        throw new UnsupportedOperationException("Not yet implemented");
        //return null;
    }
    @Override
    public void onDestroy()
    {
        Log.d("msgS", "ServiceStopped");
        Intent I=new Intent(this,MsgReceiver.class);
        startService(I);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("msgS", "ServiceStarted");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.setPriority(100);
        NetworkChangeReciever networkChangeReceiver= new NetworkChangeReciever();
        registerReceiver(networkChangeReceiver, intentFilter);
        sharedPreferences = getSharedPreferences("ChatPrefs", MODE_PRIVATE);
        sharedPreferences2 = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        String Uname = sharedPreferences2.getString("nameKey", "");

        List<String> values = new ArrayList<>();

        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
            values.add(entry.getKey().toString());
            topics.add(entry.getKey().toString() + Uname);
            System.out.println(entry.getKey().toString() + Uname);
        }
        try {
            client = new MqttAndroidClient(getApplication().getApplicationContext(), "tcp://broker.hivemq.com:1883", MainActivity.clientId);
            //connectt();
            Log.d("msgS", "Outside ");
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    //client = new MqttAndroidClient(getApplication().getApplicationContext(), "tcp://broker.hivemq.com:1883", MainActivity.clientId);
                    Log.d("msgS", "Connection Lost");
                    connectt();
                }

                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                    //Toast.makeText(getApplicationContext(), message + "Service".toString(), Toast.LENGTH_SHORT).show();

                    Log.d("msgS", "Recieved");

                    try {
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput(topic + ".txt", Context.MODE_APPEND));
                        outputStreamWriter.append("Rcv:" + message.toString() + "\n");
                        outputStreamWriter.close();
                    } catch (IOException e) {
                        Log.e("Exception", "File write failed: " + e.toString());
                    }
                    String ret = "";

                    try {
                        InputStream inputStream = getApplicationContext().openFileInput(topic + ".txt");

                        if (inputStream != null) {
                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                            String receiveString = "";
                            StringBuilder stringBuilder = new StringBuilder();

                            while ((receiveString = bufferedReader.readLine()) != null) {
                                stringBuilder.append(receiveString+"\n");
                            }

                            inputStream.close();
                            ret = stringBuilder.toString();
                            Log.d("msgS", "Read " + ret);
                        }
                    } catch (FileNotFoundException e) {
                        Log.e("login activity", "File not found: " + e.toString());
                    } catch (IOException e) {
                        Log.e("login activity", "Can not read file: " + e.toString());
                    }

                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "SMILL")
                            .setSmallIcon(R.drawable.ic_launcher_background)
                            .setContentTitle(topic)
                            .setContentText(message.toString())
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        CharSequence name = "SmartIll";
                        String description = "Notification for smart illumination";
                        int importance = NotificationManager.IMPORTANCE_DEFAULT;
                        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                        NotificationChannel channel = new NotificationChannel("SMILL", name, importance);
                        channel.setDescription(description);
                        NotificationManager notificationManager = getSystemService(NotificationManager.class);
                        notificationManager.createNotificationChannel(channel);
                        notificationManager.notify(13, mBuilder.build());

                    } else {
                        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        notificationManager.notify(13, mBuilder.build());
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.i("notify", "msg reached");
                }
            });
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return START_STICKY;
    }

    static  public void connectt(){

        Log.d("msgS","Connectt method");
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("msgS","Connected"+this.toString());
                    // We are connected
                    int qos = 1;
                    try {
                        for(int i=0;i<topics.size();i++) {
                            IMqttToken subToken;
                            subToken = client.subscribe(topics.get(i), qos);
                            subToken.setActionCallback(new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    Log.i("conn", "subscribed");
                                }

                                @Override
                                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                    Log.i("conn", "failed to subscribe");
                                }
                            });
                        }
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    Log.d("msg", "onSuccess HEllo");

                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("msgS", "onFailure");
                }
            });
        }
        catch (MqttException e) {
            e.printStackTrace();
            System.out.println("Here");
        }

    }
}

