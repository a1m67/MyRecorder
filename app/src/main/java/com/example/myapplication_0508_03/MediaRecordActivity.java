package com.example.myapplication_0508_03;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.VoicemailContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MediaRecordActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView textView;
    private Button btnRecord;
    private Button btnPlay;
    private Button btnDialog;
    private MediaRecorder mediaRecorder = new MediaRecorder();
    private AudioRecord audioRecord=null;
    private int recordBufSize=0;
    private int channelConfig;
    private boolean isRecording=false;
    private AudioTrack audioTrack;
    private int playBufSize;
    private SearchView searchView;
    private boolean isPlaying = false;
    public static int count=0;
    private FileInputStream is;//播放文件的数据流
    private Thread playThread;
    HashMap<String,String> hashMap;
    ListView    listView;
    List<String> list;
    ArrayAdapter arrayAdapter ;
    String playPath;
    String recordPath="";
    String channel="单声道";
    private EditText editText;
    String recordName;
    private Thread recordingThread;
    private Thread playingThread;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_record);
        btnRecord=findViewById(R.id.btnRecord);
//        btnPlay=findViewById(R.id.btnPlay);
        textView=findViewById(R.id.time);
        btnRecord.setOnClickListener(this::onClick);
        listView=findViewById(R.id.listView);
        searchView=findViewById(R.id.searchView);
        list=getFilesAllName(getExternalFilesDir("").getAbsolutePath());
        arrayAdapter = new ArrayAdapter(MediaRecordActivity.this, android.R.layout.simple_list_item_1,list);
        arrayAdapter.notifyDataSetChanged();
        listView.setAdapter(arrayAdapter);
        listView.setTextFilterEnabled(true);
        searchView.setQueryHint("搜索录音");
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playPath =   hashMap.get ( ((TextView)view).getText().toString()  );
                startPlay();
                Log.e("playPath",playPath);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)){
//                    listView.setFilterText(newText);
                    arrayAdapter.getFilter().filter(newText);
                }else{
                    listView.clearTextFilter();
                    arrayAdapter.getFilter().filter("");
                }
                return false;
            }
        });

    }


    private void initData(){
        playBufSize = AudioTrack.getMinBufferSize(16000,AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);//计算最小缓冲区
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 16000,AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,playBufSize,AudioTrack.MODE_STREAM);
    }
    Runnable playRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                //设置线程的优先级
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                byte[] tempBuffer = new byte[playBufSize];
                int readCount = 0;
                while (is.available() > 0) {
                    readCount= is.read(tempBuffer);
                    if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
                        continue;
                    }
                    if (readCount != 0 && readCount != -1) {//一边播放一边写入语音数据
                        //判断AudioTrack未初始化，停止播放的时候释放了，状态就为STATE_UNINITIALIZED
                        if(audioTrack.getState() == audioTrack.STATE_UNINITIALIZED){
                            initData();
                        }
                        if (null!=audioTrack){
                            audioTrack.play();
                            audioTrack.write(tempBuffer, 0, readCount);
                        }
                    }
                }
                stopPlay();//播放完就停止播放
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private void startThread() {
        destroyThread();
        isPlaying = true;
        if (playThread == null) {
            playThread = new Thread(playRunnable);
            playThread.start();
        }
    }
    private void setPath(String path) throws Exception {
        File file = new File(path);
        is = new FileInputStream(file);
    }
    public void stopPlay() {
        try {
            destroyThread();//销毁线程
            if (audioTrack != null) {
                if (audioTrack.getState() == AudioRecord.STATE_INITIALIZED) {//初始化成功
                    audioTrack.stop();//停止播放
                }
                if (audioTrack != null) {
                    audioTrack.release();//释放audioTrack资源
                }
            }
            if (is != null) {
                is.close();//关闭数据输入流
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void destroyThread() {
        try {
            isPlaying = false;
            if (null != playThread && Thread.State.RUNNABLE == playThread.getState()) {
                try {
                    Thread.sleep(100);
                    playThread.interrupt();
                } catch (Exception e) {
                    playThread = null;
                }
            }
            playThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            playThread = null;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startPlay(){
        initData();
        String path = playPath;
        try {
            setPath(path);
            startThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        //录音
//        btnPlay.setEnabled(true);
        String text1 = btnRecord.getText().toString();
        if ("录音".equals(text1)){

            //声道
            if ("单声道".equals(channel)) {
                channelConfig= AudioFormat.CHANNEL_IN_MONO;
            } else {
                channelConfig = AudioFormat.CHANNEL_IN_STEREO;
            }
            recordBufSize=AudioRecord.getMinBufferSize(16000,channelConfig,AudioFormat.ENCODING_PCM_16BIT);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,16000,channelConfig,AudioFormat.ENCODING_PCM_16BIT,recordBufSize);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("请输入文件名");
            editText = new EditText(this);
            builder.setView(editText);
            builder.setPositiveButton("确定",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String fileName = editText.getText().toString();
                    if (!"".equals(fileName)){
                        recordPath = getExternalFilesDir("").getAbsolutePath();
                        recordName=recordPath+"/"+fileName;
                        recordPath=recordPath+"/"+fileName+".pcm";

                        //如果fileName.pcm不存在才往里边添加。
                        if (!list.contains(recordName.substring(getExternalFilesDir("").getAbsolutePath().length()+1)))
                        list.add(recordName.substring(getExternalFilesDir("").getAbsolutePath().length()+1));

                        Log.e("hello","开始录音");
                        startRecord();
                        btnRecord.setText("结束");
                        Toast.makeText(MediaRecordActivity.this,"开始录音",Toast.LENGTH_SHORT).show();
//                        btnPlay.setEnabled(false);

                    }
                }
            });
            builder.setNegativeButton("取消",null);
            builder.show();




            System.out.println();

        }else {
            btnRecord.setText("录音");
            isRecording=false;
            arrayAdapter.notifyDataSetChanged();

            if (audioRecord!=null){
                audioRecord.stop();
                System.out.println("停止录音");
                audioRecord.release();
                audioRecord=null;
                recordingThread=null;
            }
//            btnPlay.setEnabled(true);

        }
    }
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void onClick1(View view) throws IOException, InterruptedException {
//        //放音
//        String text2 = btnPlay.getText().toString();
//        if ("放音".equals(text2)) {
//            startPlay();
//        }
//        else {
//            System.out.println("结束放音");
//            stopPlay();
//            btnPlay.setText("放音");
//        }
//    }

    private void startRecord() {
        count++;
        Log.e("path=",recordPath);
        if (isRecording) {
            return;
        }
        isRecording = true;
        audioRecord.startRecording();
        Log.i("audioRecordTest", "开始录音");
        hashMap.put(recordPath.substring(getExternalFilesDir("").getAbsolutePath().length()+1,recordPath.length()-4),recordPath);
        recordingThread = new Thread(() -> {
            byte data[] = new byte[recordBufSize];
            File file = new File(recordPath);
            FileOutputStream os = null;
            try {
                if (!file.exists()) {
                    file.createNewFile();
                    Log.i("audioRecordTest", "创建录音文件->" + recordPath);
                }
                os = new FileOutputStream(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int read;
            if (os != null) {
                while (isRecording) {
                    read = audioRecord.read(data, 0, recordBufSize);
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        try {
                            os.write(data);
//                            Log.i("audioRecordTest", "写录音数据->" + read);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        recordingThread.start();
    }
    private void stopRecord() {
        arrayAdapter.notifyDataSetChanged();
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
            Log.i("audioRecordTest", "停止录音");
            audioRecord.release();
            audioRecord = null;
            recordingThread = null;
        }
    }
    public List<String> getFilesAllName(String path) {
        path=getExternalFilesDir("").getAbsolutePath();
        File file=new File(path);
        //获取这个文件夹下的所有的文件
        File[] files=file.listFiles();
        if (files == null){ Log.e("error","空目录");return null;}
        List<String> s = new ArrayList<>();
        hashMap = new HashMap<>();
        for(int i =0;i<files.length;i++){
            if (files[i].getAbsolutePath().contains(".pcm")){
                String absolutePath = files[i].getAbsolutePath();
                hashMap.put( (absolutePath.substring(path.length()+1,files[i].getAbsolutePath().length()-4))    ,  absolutePath       );
                s.add((absolutePath.substring(path.length()+1,absolutePath.length()-4)  ));
            }
        }
        return s;
    }
}
