package com.example.myapplication_0508_03;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private boolean flag=true;
    private static final String TAG = "a1m67";
    private EditText editText;
    private String recordPath;
    private String fileName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //申请权限
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},100);
//        recordPath = getExternalFilesDir("").getAbsolutePath();
        Button btn = findViewById(R.id.btn1);
//        EditText editText = findViewById(R.id.et);

        //点击事件

    }


    public void record(View view){
        Intent intent =  new Intent(this, MediaRecordActivity.class);
        intent.putExtra("recordPath",recordPath);
        startActivity(intent);
//        }
    }
    public void dialog(View view){
                    record(view);
//        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//        builder.setTitle("请输入文件名");
//        editText = new EditText(MainActivity.this);
//        builder.setView(editText);
//        builder.setPositiveButton("确定",new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                fileName = editText.getText().toString();
//                if (!"".equals(fileName)){
//                    recordPath = getExternalFilesDir("").getAbsolutePath();
//                    recordPath=recordPath+"/"+fileName+".pcm";
////                    record(view);
//                }
//            }
//        });
//        builder.setNegativeButton("取消",null);
////        builder.setCancelable(true);
//        builder.show();
    }
}