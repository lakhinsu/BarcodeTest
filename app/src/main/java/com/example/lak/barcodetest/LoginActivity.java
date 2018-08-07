package com.example.lak.barcodetest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    /*private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    Button RegisterButton;
    EditText Name,Password;
    SharedPreferences sharedpreferences;

    public static final String MyPREFERENCES = "UserPrefs" ;
    public static final String NameKey = "nameKey";
    public static final String Passkey = "passKey";

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Name=(EditText) findViewById(R.id.Name);
        Password=(EditText) findViewById(R.id.Password);

        RegisterButton=(Button) findViewById(R.id.RegiterButton);


        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Name.getText().toString().length() == 0 || Password.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Please Fill up the details ", Toast.LENGTH_SHORT).notify();
                } else {
                    SharedPreferences.Editor data = sharedpreferences.edit();
                    data.putString(NameKey, Name.getText().toString());
                    data.putString(Passkey, Password.getText().toString());

                    data.commit();

                    Intent I=new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(I);
                }
            }

        });

    }
    @Override
    public void onBackPressed() {
       this.finish();
    }
}

