package com.example.lak.barcodetest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;


public class Main2Activity extends AppCompatActivity {

    SharedPreferences sharedpreferences;

    public static final String MyPREFERENCES = "UserPrefs" ;
    public static final String NameKey = "nameKey";
    public static final String Passkey = "passKey";

    TextView disp;
    ImageView barcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        disp=(TextView) findViewById(R.id.disp);
        barcode=(ImageView)findViewById(R.id.imageView2);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);

        if(sharedpreferences.contains(NameKey)){

            String s1=sharedpreferences.getString(NameKey,"");
            String s2=sharedpreferences.getString(Passkey,"");

            String s3=s1+"\n"+s2;

            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(s3, BarcodeFormat.QR_CODE,900 ,900);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                barcode.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }

            disp.setText(s3);

        }
        else
        {
            disp.setText("Not registered");
        }
    }
    @Override
    public void onBackPressed() {
        this.finish();
    }
}
