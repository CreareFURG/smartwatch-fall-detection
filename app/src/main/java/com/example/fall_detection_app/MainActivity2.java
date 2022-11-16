package com.example.fall_detection_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity2 extends Activity {

    TextView timer;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep screen on

        timer = findViewById(R.id.textView);
        new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timer.setText(" " + millisUntilFinished/1000);
            }

            @Override
            public void onFinish() {

            }
        }.start();

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent send = new Intent(getApplicationContext(), MainActivity.class);
                send.putExtra("confirm", "false");
                startActivity(send);
                finish();


            }
        });
    }
}