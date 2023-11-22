package com.example.projectapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class GameActivity extends AppCompatActivity {



    WebView webView;
    EditText editText;
    TextView textView;
    Button btn1,btn2;
    Context context;

    Random rn =new Random();
    int r = rn.nextInt(4);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");
        String userPass = intent.getStringExtra("userPass");
        String userr = intent.getStringExtra("userr");


        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_test);
        webView = findViewById(R.id.webview);
        /*   editText = findViewById(R.id.edit_num);
        btn1 = findViewById(R.id.send_button);


        textView = findViewById(R.id.web_text);

        */
        btn2 = findViewById(R.id.exit_button);
        context = this;

        webView.getSettings().setJavaScriptEnabled(true);
     //   webView.addJavascriptInterface(new WebBridge(),"java");
        webView.loadUrl("file:///android_asset/test"+r+".html");

       /*
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.loadUrl("javascript:exam_script.plus_num("+editText.getText()+")");
            }
        });
         */
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GameActivity.this, MapActivity.class);
                intent.putExtra("userId",userId);
                intent.putExtra("userPass",userPass);
                intent.putExtra("r","");
                startActivity(intent);
            }
        });
    }

/*
    class WebBridge{
        @JavascriptInterface
        public void getNum(final int num){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context,"계산 결과는 "+num+"입니다.",Toast.LENGTH_LONG).show();
                    textView.setText("Java :::: "+num);
                }
            });
        }
    }*/
}

