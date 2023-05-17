package com.example.myapplication_dialog;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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
    private boolean isPlaying = false;
    private String recordFileName;


    private FileInputStream is;//播放文件的数据流
    private Thread playThread;
    private boolean isStart = false;
    private volatile static MediaRecordActivity mInstance;


    String url ;
    //计时器
    Chronometer ch;//计时器
    Spinner spinner1,spinner2 ;
    String channel="单声道";
    int samplingRate = 16000;
    String s_samplingRate="";
    String [] strings = {"16000","44100"};
    private Thread recordingThread;
    private Thread playingThread;
    Button btn_end ;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_record);
        ch=findViewById(R.id.timer);
        btnRecord=findViewById(R.id.btnRecord);
        btnPlay=findViewById(R.id.btnPlay);
        textView=findViewById(R.id.time);
        btnRecord.setOnClickListener(this::onClick);

        btnPlay.setOnClickListener(view -> {
            try {
                onClick1(view);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });




    }




    private void initData(){
        //根据采样率，采样精度，单双声道来得到frame的大小。
        playBufSize = AudioTrack.getMinBufferSize(16000,AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);//计算最小缓冲区
        //注意，按照数字音频的知识，这个算出来的是一秒钟buffer的大小。
        //创建AudioTrack
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
        isStart = true;
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
            isStart = false;
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
        String path = getExternalFilesDir("").getAbsolutePath()+"/test1.pcm";
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
        btnPlay.setEnabled(true);
        String text1 = btnRecord.getText().toString();
        if ("录音".equals(text1)){
            btnRecord.setText("结束");
            btnPlay.setEnabled(false);
            //声道
            if ("单声道".equals(channel)) {
                channelConfig= AudioFormat.CHANNEL_IN_MONO;
            } else {
                channelConfig = AudioFormat.CHANNEL_IN_STEREO;
            }
            recordBufSize=AudioRecord.getMinBufferSize(16000,channelConfig,AudioFormat.ENCODING_PCM_16BIT);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,16000,channelConfig,AudioFormat.ENCODING_PCM_16BIT,recordBufSize);
            startRecord();
            System.out.println();
            ch.setBase(SystemClock.elapsedRealtime());
            ch.start();
            System.out.println("开始录音"+getExternalFilesDir("").getAbsolutePath());
        }else {
            btnRecord.setText("录音");
            isRecording=false;
            if (audioRecord!=null){
                audioRecord.stop();
                System.out.println("停止录音");
                audioRecord.release();
                audioRecord=null;
                recordingThread=null;
            }
            btnPlay.setEnabled(true);
            ch.stop();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void onClick1(View view) throws IOException, InterruptedException {
//        //播音
        String text2 = btnPlay.getText().toString();
        if ("放音".equals(text2)) {
            startPlay();
        }
        else {
            System.out.println("结束放音");
            stopPlay();
            btnPlay.setText("放音");
        }
    }

    private void startRecord() {
        url = getExternalFilesDir("").getAbsolutePath()+"/test1.pcm";
        if (isRecording) {
            return;
        }
        isRecording = true;
        audioRecord.startRecording();
        Log.i("audioRecordTest", "开始录音");

        recordingThread = new Thread(() -> {
            byte data[] = new byte[recordBufSize];
            File file = new File(url);
            FileOutputStream os = null;
            try {
                if (!file.exists()) {
                    file.createNewFile();
                    Log.i("audioRecordTest", "创建录音文件->" + url);
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
                            Log.i("audioRecordTest", "写录音数据->" + read);
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
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
            Log.i("audioRecordTest", "停止录音");
            audioRecord.release();
            audioRecord = null;
            recordingThread = null;
        }
    }

}
