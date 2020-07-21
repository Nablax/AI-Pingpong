package com.example.myapplication;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.myapplication.Main2Activity.REQUEST_CODE_ASK_PERMISSIONS;
import static com.example.myapplication.Main2Activity.permissionsArray;

public class PlayBack extends Activity implements SurfaceHolder.Callback {
    private DataManager dataManager=DataManager.getInstance();
    private static final String TAG = "PlayBack";

    private List<String> permissionsList = new ArrayList<String>();
    private static final int PERMISSION_REQUEST_CAMERA = 2;
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 3;
    private SurfaceView mSurfaceview;
    private Button mBtnStartStop;
    private Button mBtnPlay;
    private boolean mStartedFlg = false;//是否正在录像
    private boolean mIsPlay = false;//是否正在播放录像
    private MediaRecorder mRecorder;
    private SurfaceHolder mSurfaceHolder;
    private ImageView mImageView;
    private Camera camera;
    private MediaPlayer mediaPlayer;
    private String path;
    private File dir, folderPath;
    private TextView textView;
    private int text = 0;
    private String DateNow;
    private WriteDataToFile writeDataToFile;
    private final int COLLECTION=0;
    private final int FIGHT=1;
    private int mode=COLLECTION;
    private Drawable RECORDING;
    private Drawable RECORDING_DISCONNECTED;
    private Drawable RECORDINGSTOP_CONNECTED;
    private Drawable RECORDINGSTOP_DISCONNECTED;
    private Timer timer;
    private final int not_recording_disconnected=0;
    private final int not_recording_connected=1;
    private final int recording_disconnected=2;
    private final int recording_connected=3;
    private int drawableSate=not_recording_disconnected;

    private boolean isShowController = true;

    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private boolean mConnected = false;

    private String mDeviceName;
    private String mDeviceAddress;
    //计时
    private long startTime=-1;


    BluetoothGattCharacteristic mGattCharacteristics;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                stopRecording();
//                textView_blueTooth.setText("未连接蓝牙");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)&&mStartedFlg) {
                //TODO
                byte[] data=intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                //System.out.println("playback");
                //System.out.println("playback");
                dataManager.dataByteSplit(data);
                //dataManager.fightDataByteSplit(data);
                //System.out.println("playback");
            }
        }
    };

    private android.os.Handler handler = new android.os.Handler(
            new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    Bundle bundle=msg.getData();
                    String data=bundle.getString("updateImage");
                    if(data==null)
                        return false;
                    switch (data)
                    {
                        case "not_recording_disconnected":
                            mBtnStartStop.setBackground(RECORDING);
                            drawableSate=not_recording_connected;
                            break;
                        case "not_recording_connected":
                            mBtnStartStop.setBackground(RECORDING_DISCONNECTED);
                            drawableSate=not_recording_disconnected;
                            break;
                        case "recording_disconnected":
                            mBtnStartStop.setBackground(RECORDINGSTOP_CONNECTED);
                            drawableSate=recording_connected;
                            break;
                        case "recording_connected":
                            mBtnStartStop.setBackground(RECORDINGSTOP_DISCONNECTED);
                            drawableSate=recording_disconnected;
                            break;
                    }
                    return false;
                }
            }
    );
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            text++;
            textView.setText(text+"");
            handler.postDelayed(this,1000);
        }
    };


    private void myListener(){
        mBtnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsPlay) {
                    if (mediaPlayer != null) {
                        mIsPlay = false;
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                }
                if (!mStartedFlg) {
                    //handler.postDelayed(runnable,1000);
                    mImageView.setVisibility(View.GONE);
                    mBtnStartStop.setBackground(RECORDINGSTOP_DISCONNECTED);
                    drawableSate=not_recording_disconnected;

                    if (mRecorder == null) {
                        mRecorder = new MediaRecorder();
                    }

                    camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                    if (camera != null) {
                        camera.setDisplayOrientation(0);
                        camera.unlock();
                        mRecorder.setCamera(camera);
                    }

                    try {
                        // 这两项需要放在setOutputFormat之前
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

                        // Set output file format
                        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

                        // 这两项需要放在setOutputFormat之后
                        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);

                        //mRecorder.setVideoFrameRate(30);
                        mRecorder.setOrientationHint(0);

//                        List<Camera.Size> previewSizes = camera.getParameters().getSupportedPreviewSizes();
//                        for (int i = 0; i < previewSizes.size(); i++) {
//                            Camera.Size psize = previewSizes.get(i);
//                            System.out.println( "PreviewSize,width: " + psize.width + " height: " + psize.height);
//                        }
                        //mRecorder.setVideoSize(640, 480);
                        mRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
                        //设置记录会话的最大持续时间（毫秒）
                        mRecorder.setMaxDuration(100000 * 1000);
                        mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

                        path = getSDPath();
                        if (path != null) {
                            DateNow = getDate();
                            if(mode==FIGHT)
                            {
                                dir = new File(path + "/ballGame/Video");
                                if (!dir.exists()) {
                                    dir.mkdir();
                                }
                                folderPath = new File(dir + "/" + DateNow);
                                if(!folderPath.exists()){
                                    folderPath.mkdir();
                                }
                                path = folderPath + "/" + DateNow + ".mp4";
                                writeDataToFile=new WriteDataToFile(folderPath + File.separator + DateNow + ".txt");
                            }
                            else if(mode == COLLECTION)
                            {
                                dir = new File(path + "/ballGame/Collection");
                                if (!dir.exists()) {
                                    dir.mkdir();
                                }
                                path = dir + "/" + DateNow + ".mp4";
                                writeDataToFile=new WriteDataToFile(dir + File.separator + DateNow + ".txt");
                            }

                            mRecorder.setOutputFile(path);
                            mRecorder.prepare();
                            mRecorder.start();
                            //startTime=System.nanoTime();
                            startTime= System.currentTimeMillis();
                            mStartedFlg = true;
                            updateImage();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //stop
                    stopRecording();
                }
            }
        });

//        mBtnPlay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mIsPlay = true;
//                mImageView.setVisibility(View.GONE);
//                mBtnPlay.setBackground(VIDEO_PAUSE);
//                if (mediaPlayer == null) {
//                    mediaPlayer = new MediaPlayer();
//                }
//                mediaPlayer.reset();
//                Uri uri = Uri.parse(path);
//                mediaPlayer = MediaPlayer.create(PlayBack.this, uri);
//                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                mediaPlayer.setDisplay(mSurfaceHolder);
//                try{
//                    mediaPlayer.prepare();
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//                mediaPlayer.start();
//            }
//        });
    }
    //停止录像
    private void stopRecording()
    {
        if (mStartedFlg)
        {
            mStartedFlg = false;
            updateImage();
            try {
                //handler.removeCallbacks(runnable);
                mRecorder.stop();
                mRecorder.reset();
                mRecorder.release();
                mRecorder = null;
                if (camera != null) {
                    camera.release();
                    camera = null;
                }
                final EditText ETmp = new EditText(PlayBack.this);
                ETmp.setHint(DateNow);
                ETmp.setHintTextColor(Color.GRAY);
                new AlertDialog.Builder(PlayBack.this)
                        .setTitle("请输入保存文件夹")
                        .setView(ETmp)
                        .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(ETmp.getText() != null){
                                    FileCheck FCTmp = new FileCheck();
                                    if(FCTmp.isValidFileName(ETmp.getText().toString())){
                                        if(mode==FIGHT)
                                        {
                                            String newPath = dir + "/" + ETmp.getText().toString();
                                            folderPath.renameTo(new File(newPath));
//                                                        FCTmp.moveFile(path, newPath);
//                                                        FCTmp.deleteDirectory(folderPath.getPath());
                                        }
                                        else
                                        {
                                            File file=new File(dir + File.separator + DateNow + ".txt");
                                            file.renameTo(new File(dir + File.separator + ETmp.getText() + ".txt"));
                                            file=new File(dir + File.separator + DateNow + ".mp4");
                                            file.renameTo(new File(dir + File.separator + ETmp.getText() + ".mp4"));
                                        }
                                    }

                                }
                            }
                        })
                        .setNegativeButton("不保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(mode==FIGHT)
                                {
                                    FileCheck FCTmp = new FileCheck();
                                    FCTmp.deleteDirectory(folderPath.getPath());
                                }
                                else
                                {
                                    File file=new File(dir + File.separator + DateNow + ".txt");
                                    file.delete();
                                    file=new File(dir + File.separator + DateNow + ".mp4");
                                    file.delete();
                                }
                            }
                        })
                        .show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if(writeDataToFile!=null)
                {
                    writeDataToFile.close();
                    writeDataToFile=null;
                }
            }
        }
        mStartedFlg = false;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_play_back);

        mSurfaceview = (SurfaceView) findViewById(R.id.surfaceview);
        mImageView = (ImageView) findViewById(R.id.imageview);
        mBtnStartStop = (Button) findViewById(R.id.btnStartStop);
        //mBtnPlay = (Button) findViewById(R.id.btnPlayVideo);
        //textView = (TextView)findViewById(R.id.text);
        mDeviceAddress = Main2Activity.mDeviceAddress;
        RECORDING = getResources().getDrawable(R.drawable.recording_connecting);
        RECORDING_DISCONNECTED=getResources().getDrawable(R.drawable.recording);
        RECORDINGSTOP_CONNECTED = getResources().getDrawable(R.drawable.recording_stop_connecting);
        RECORDINGSTOP_DISCONNECTED=getResources().getDrawable(R.drawable.recording_stop_disconnected);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissionsArray) {
                if (this.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsList.add(permission);
                }
            }
            if(permissionsList.size()!=0)
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), REQUEST_CODE_ASK_PERMISSIONS);

        }

        myListener();
        SurfaceHolder holder = mSurfaceview.getHolder();
        holder.addCallback(this);
        // setType必须设置，要不出错.
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        dataManager.setDataCallBack(
                new DataCallBack() {
                    @Override
                    public void onDataFinish() {
                        //long time=System.nanoTime()-startTime;
                        long time=System.currentTimeMillis()-startTime;
                        String keep="";
                        //实时传输时间记录方式
                        List<Byte> list=dataManager.getEntireData();
                        if(time<0)
                            return ;
                        int tmpLen = list.size();
                        byte tmpFig = 0;
                        for(int i = 0;i < tmpLen; i++)
                        {
                            tmpFig = list.get(i);
                            if(i == 2){
                                keep += ",";
                            }
                            if(tmpFig <= 0xf && tmpFig >= 0x0){
                                keep += 0;
                            }
                            keep += ""+Integer.toHexString(tmpFig&0xff);
                        }
                        //keep=time/1000000+","+keep;
                        keep=time+","+keep;
                        //动作传输时间记录方式
//                        List<Double> doubles=dataManager.getFightEntireData();
//                        int counter=doubles.size()/15;
//                        for(Double x:doubles)
//                            keep+=x+",";
//                        long temp=time/1000000;
//                        keep=keep+(temp-counter*25)+","+temp;
                        if(writeDataToFile!=null)
                            writeDataToFile.write(keep+'\n');
                    }
                }
        );
        updateImage();
        timer=new Timer();
        timer.schedule(new Task(),1000,1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        System.out.println("regist");
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            //Log.d(TAG, "Connect request result=" + result);
        }
        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        if (!mStartedFlg) {
            mImageView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int i=0; i<permissions.length; i++) {
                    if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        //Toast.makeText(Main2Activity.this, "权限申请成功！"+permissions[i], Toast.LENGTH_SHORT).show();
                    } else {
                        finish();
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        stopRecording();
        //this.mediaPlayerManager.pauseVideo();
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /**
     * 获取系统时间
     *
     * @return
     */
    public static String getDate() {
        Calendar ca = Calendar.getInstance();
        int year = ca.get(Calendar.YEAR);           // 获取年份
        int month = ca.get(Calendar.MONTH) + 1;     // 获取月份
        int day = ca.get(Calendar.DATE);            // 获取日
        int minute = ca.get(Calendar.MINUTE);       // 分
        int hour = ca.get(Calendar.HOUR_OF_DAY);   // 小时
        int second = ca.get(Calendar.SECOND);       // 秒

        String date = "" + year + "-";
        if(month < 10)
            date = date + "0";
        date = date + month + "-";
        if(day < 10)
            date = date + "0";
        date = date + day + "-";
        if(hour < 10)
            date = date + "0";
        date = date + hour + "-";
        if(minute < 10)
            date = date + "0";
        date = date + minute + "-";
        if(second < 10)
            date = date + "0";
        date = date + second;
        Log.d(TAG, "date:" + date);

        return date;
    }

    /**
     * 获取SD path
     *
     * @return
     */
    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            return sdDir.toString();
        }

        return null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
        //mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // 将holder，这个holder为开始在onCreate里面取得的holder，将它赋给mSurfaceHolder
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mSurfaceview = null;
        mSurfaceHolder = null;
        handler.removeCallbacks(runnable);
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
            Log.d(TAG, "surfaceDestroyed release mRecorder");
        }
        if (camera != null) {
            camera.release();
            camera = null;
        }
        if (mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    private synchronized void updateImage()
    {
        System.out.println("playback "+drawableSate+" "+this.mStartedFlg+" "+BluetoothLeService.getmConnectionState());
        Message message =new Message();
        Bundle bundle=message.getData();
        if(!this.mStartedFlg)
        {
            if((BluetoothLeService.getmConnectionState()==BluetoothLeService.STATE_CONNECTED
                    ||BluetoothLeService.getmConnectionState()==BluetoothLeService.STATE_CONNECTING)
                    &&drawableSate!=not_recording_connected)
            {
                bundle.putString("updateImage","not_recording_disconnected");
            }
            else if(BluetoothLeService.getmConnectionState()==BluetoothLeService.STATE_DISCONNECTED
                    &&drawableSate!=not_recording_disconnected)
            {
                bundle.putString("updateImage","not_recording_connected");
            }
            handler.sendMessage(message);
            return ;
        }
        if((BluetoothLeService.getmConnectionState()==BluetoothLeService.STATE_CONNECTED
                ||BluetoothLeService.getmConnectionState()==BluetoothLeService.STATE_CONNECTING)
                && drawableSate!=recording_connected)
        {
            bundle.putString("updateImage","recording_disconnected");
        }
        else if(BluetoothLeService.getmConnectionState()==BluetoothLeService.STATE_DISCONNECTED
                &&drawableSate!=recording_disconnected)
        {
            bundle.putString("updateImage","recording_connected");
        }
        handler.sendMessage(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //System.out.println("playback timer cancel");
        if(timer!=null)
            timer.cancel();
    }

    private class Task extends TimerTask
    {
        @Override
        public void run() {
            updateImage();
        }
    }
}