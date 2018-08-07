package com.example.lak.barcodetest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Main4Activity extends AppCompatActivity {

    String clientId = MqttClient.generateClientId();

    SharedPreferences sharedPreferences;

    Button publish;
    EditText payload;
    ListView msgList;
    boolean flag=true;
    ArrayList<String> msgArray=new ArrayList<>();
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        payload=(EditText)findViewById(R.id.payload);
        publish=(Button)findViewById(R.id.publish);

        sharedPreferences=getSharedPreferences("UserPrefs",MODE_PRIVATE);

        String temp=sharedPreferences.getString("nameKey","");

        String pubstopic=temp;

        Bundle extras=getIntent().getExtras();
        String Uname=extras.getString("ChatName").toString();

        String substopic=Uname;

        substopic+=temp;

        pubstopic+=Uname;

        final String topic=substopic;

        final String subs=topic;

        final String topic2=pubstopic;
       // Toast.makeText(getApplicationContext(),topic,Toast.LENGTH_SHORT).show();
        setTitle(Uname);
        adapter = new ArrayAdapter<String>(this, R.layout.msg_listview, msgArray);

            final ListView listView = (ListView) findViewById(R.id.msg_list);
            listView.setAdapter(adapter);

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
                            IMqttToken subToken;
                            subToken = client.subscribe(topic,qos);
                            subToken.setActionCallback(new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    Log.i("conn","subscribed");
                                }

                                @Override
                                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                    Log.i("conn","failed to subscribe");
                                }
                            });
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        Log.d("msg", "onSuccess");
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


            publish.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String msg = payload.getText().toString();
                   // Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                    flag = false;
                    byte[] encodedPayload = new byte[0];
                    try {
                        encodedPayload = msg.getBytes("UTF-8");
                        MqttMessage message = new MqttMessage(encodedPayload);
                        client.publish(topic2, message);
                        payload.setText("");
                       // Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                        msgArray.add(message.toString());
                        adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.msg_listview, msgArray);
                        listView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                        listView.setAdapter(adapter);

                    } catch (UnsupportedEncodingException | MqttException e) {
                        e.printStackTrace();
                    }
                    // Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();

                }
            });

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.i("error","connection loast");
                }

                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    //Toast.makeText(getApplicationContext(),message.toString(),Toast.LENGTH_SHORT).show();
                    if(topic.equals(subs)) {
                        msgArray.add(message.toString());
                        //Toast.makeText(getApplicationContext(),message.toString()+"Screen",Toast.LENGTH_SHORT).show();
                        adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.msg_listview, msgArray);
                        listView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                        listView.setAdapter(adapter);
                    }
                    // NotificationManagerCompat notificationManager=new NotificationManagerCompat();

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
        }
        catch (Exception e) {
            Log.i("error","connection failed");
            e.printStackTrace();
            System.out.println("Here Outside");
        }
    }
}







