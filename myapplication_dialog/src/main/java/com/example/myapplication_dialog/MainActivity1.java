package com.example.myapplication_dialog;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity1 extends AppCompatActivity {


    private static final String TAG = "a1m67";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        //申请权限
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},100);

        Button btn = findViewById(R.id.btn1);
//        EditText editText = findViewById(R.id.et);
        //点击事件
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record(v);
            }
        });


    }


    public void record(View view){
        startActivity(new Intent(this, MediaRecordActivity.class));
    }
}