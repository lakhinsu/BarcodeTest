package com.example.lak.barcodetest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
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
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MsgReceiver extends Service {

    SharedPreferences sharedPreferences, sharedPreferences2;

    public MsgReceiver() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        /* TODO: Return the communication channel to the service. */

        /*throw new UnsupportedOperationException("Not yet implemented");*/
        return null;
    }
    @Override
    public void onDestroy()
    {
        Log.d("msgS", "ServiceStopped");
        Intent I=new Intent(getApplicationContext(),MsgReceiver.class);
        startService(I);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("msgS", "ServiceStarted");
        sharedPreferences = getSharedPreferences("ChatPrefs", MODE_PRIVATE);
        sharedPreferences2 = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        String Uname = sharedPreferences2.getString("nameKey", "");

        List<String> values = new ArrayList<>();

        final List<String> topics = new ArrayList<>();

        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
            values.add(entry.getKey().toString());
            topics.add(entry.getKey().toString() + Uname);
            System.out.println(entry.getKey().toString() + Uname);
        }
        final MqttAndroidClient client =
                new MqttAndroidClient(this.getApplicationContext(), "tcp://broker.hivemq.com:1883",MainActivity.clientId);
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
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
                    //Toast.makeText(getApplicationContext(),"Connection Succesfull",Toast.LENGTH_SHORT).show();

                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d("msg", "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            System.out.println("Here");
        }
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.i("error","connection loast");
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                Toast.makeText(getApplicationContext(),message+"Service".toString(),Toast.LENGTH_SHORT).show();

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
                    notificationManager.notify(13,mBuilder.build());

                }
                else{
                        /*Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        mBuilder.setSound(alarmSound);*/
                    mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(13, mBuilder.build());

                        /*Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        mBuilder.setSound(alarmSound);*/

                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i("notify" , "msg reached");
            }
        });
        return START_STICKY;
    }
}

