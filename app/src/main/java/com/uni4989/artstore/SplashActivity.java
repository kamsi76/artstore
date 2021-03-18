package com.uni4989.artstore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        Loadingstart();
    }

    private void Loadingstart(){
        Handler handler=new Handler();
        handler.postDelayed(new Runnable(){
            public void run(){

                //푸시를 통해 넘어온 항목을 확인하여 해당 화면으로 이동 또는 팝업을 띄운다.
                Bundle extras = getIntent().getExtras();
                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                if( extras != null ) {
                    String url = extras.getString("targetUrl");
                    intent.putExtra("targetUrl", url);
                }

                startActivity(intent);
                finish();
            }
        },2000);
    }
}