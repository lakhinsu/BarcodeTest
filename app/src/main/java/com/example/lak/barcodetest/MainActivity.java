package com.example.lak.barcodetest;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.journeyapps.barcodescanner.CaptureActivity;

import org.eclipse.paho.client.mqttv3.MqttClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static String clientId = MqttClient.generateClientId();

    SharedPreferences sharedpreferences,sharedPreferences2;

    TextView textView;

    ListView Chats;

    public static final String MyPREFERENCES = "ChatPrefs" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       /* ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (!(MsgReceiver.class.equals(service.service.getClassName()))) {
                Log.i ("isMyServiceRunning?", true+"");*/
                Intent Service=new Intent(this,MsgReceiver.class);

                startService(Service);

       /*     }
        }*/



        textView=(TextView) findViewById(R.id.ContentDisp);
        Chats=(ListView) findViewById(R.id.Chats);

        List<String> values = new ArrayList<>();

        List<String> pass=new ArrayList<>();

        sharedpreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);

        textView.setText(sharedpreferences.getAll().toString());


        Map<String, ?> allEntries = sharedpreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
            values.add(entry.getKey().toString());
        }

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, values);

        Chats.setAdapter(arrayAdapter);


        Chats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                         public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                                             String text = arrayAdapter.getItem(position);
                                             Intent I=new Intent(getApplicationContext(),Main4Activity.class);
                                             I.putExtra("ChatName",text);
                                             startActivity(I);

                                         }
                                     }
                );

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            this.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.Scan) {
            Intent intent = new Intent(getApplicationContext(),Main3Activity.class);
            intent.setAction("com.google.zxing.client.android.SCAN");
            intent.putExtra("SAVE_HISTORY", false);
            startActivityForResult(intent, 13);



        }
        else if (id == R.id.Register) {

            sharedPreferences2=getSharedPreferences("UserPrefs",MODE_PRIVATE);
            if(sharedPreferences2.contains("nameKey")){
                Toast.makeText(getApplicationContext(),"Already Registerd !",Toast.LENGTH_SHORT).show();
            }
            else {
                Intent I = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(I);
            }

        } else if (id == R.id.About) {

            Intent I=new Intent(getApplicationContext(),Main2Activity.class);
            startActivity(I);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 13) {
            if (resultCode == RESULT_OK) {
                sharedpreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
                String contents = data.getStringExtra("SCAN_RESULT");
                String s[]=contents.split("\n",-1);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(s[0],s[1]);
                editor.commit();
                System.out.println(sharedpreferences.getAll());
            } else if (resultCode == RESULT_CANCELED) {
                System.out.println("Failed");
            }
        }
    }
}
