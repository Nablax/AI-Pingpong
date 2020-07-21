package com.example.myapplication;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class BallGameMainActivity extends AppCompatActivity {
    private final int FREE=0;
    private final int GAME=1;
    private int cuurentMode=FREE;

    private Button difficultLock;
    private Button btnPopStartStop;
    private String mDeviceName;
    private String mDeviceAddress;

    private boolean mConnected = false;
    private DataManager dataManager=DataManager.getInstance();
    private Drawable START, STOP;
    private boolean isPressedBtnPopStartStop = false;
    private CircularRingPercentageView timer_PopBall;
    //游戏相关属性
    private Handler handler=new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle=msg.getData();
            String result_type=bundle.get("UPDATETYPE").toString();
            switch (result_type)
            {
                case "VALUE":
                    String result_rate=bundle.get("UPDATERATE").toString();
                    String result_score=bundle.get("UPDATESCORE").toString();
                    textView_rate.setText("准确率: "+DensityUtil.floatPrecision(1,Float.valueOf(result_rate)*100)+"%");
                    timer_PopBall.updateTextMiddle(Integer.valueOf(result_score));
                    break;
                case "DIFFICULTY":
                    String result_difficulty=bundle.get("UPDATEDIFFICULTY").toString();
                    seekBar_difficulty.setProgress((int)(float)(Float.valueOf(result_difficulty)));
                    break;
            }
        }
    };
    private BallGameRule ballGameRule;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private TextView textView_ForeHandPopNum;
    private TextView textView_BackHandPopNum;
    private TextView textView_PopDropTime;
    private TextView textView_AvgPopSpeed;
    private TextView textView_rate;
    private SeekBar seekBar_volume;
    private SeekBar seekBar_difficulty;
    private BasicAnalysis basicAnalysis=new BasicAnalysis();
    BluetoothGattCharacteristic mGattCharacteristics;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
//                textView_blueTooth.setText("未连接蓝牙");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //TODO
                //String data=intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                //System.out.println(data);
                //Main2Activity.dataManager.addData(data);
            }        }
    };
    public void FindBlueToothList(List<BluetoothGattService> gattServices){
        if (gattServices == null) return;
        for (BluetoothGattService gattService : gattServices) {
            if(gattService.getUuid().toString().equals("6e400001-b5a3-f393-e0a9-e50e24dcca9e")){
                mGattCharacteristics = gattService.getCharacteristics().get(0);
                break;
            }
        }
    }
    public void ReadDataFromCharacteristic(){
        FindBlueToothList(mBluetoothLeService.getSupportedGattServices());
        if (mGattCharacteristics != null) {
            final BluetoothGattCharacteristic characteristic = mGattCharacteristics;
            final int charaProp = characteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                // If there is an active notification on a characteristic, clear
                // it first so it doesn't update the data field on the user interface.
                if (mNotifyCharacteristic != null) {
                    mBluetoothLeService.setCharacteristicNotification(
                            mNotifyCharacteristic, false);
                    mNotifyCharacteristic = null;
                }
                mBluetoothLeService.readCharacteristic(characteristic);
            }
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                mNotifyCharacteristic = characteristic;
                mBluetoothLeService.setCharacteristicNotification(
                        characteristic, true);
            }
        }
    }
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
//                Log.e(TAG, "Unable to initialize Bluetooth");
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    public void myListener(){
        if(cuurentMode==GAME)
        {
            ballGameRule=new BallGameRule(this,handler);
            seekBar_volume.setOnSeekBarChangeListener(
                    new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            ballGameRule.setVolume((float)progress/100);
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    }
            );
            seekBar_difficulty.setOnSeekBarChangeListener(
                    new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if(!isPressedBtnPopStartStop)
                                ballGameRule.setDifficulty(progress);
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    }
            );
            difficultLock.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ballGameRule.changeDifficultyLocked();
                            if(ballGameRule.getLockSate())
                                difficultLock.setBackground(DensityUtil.scaleImage(BallGameMainActivity.this,R.drawable.lock,20,20,DensityUtil.DP));
                            else
                                difficultLock.setBackground(DensityUtil.scaleImage(BallGameMainActivity.this,R.drawable.unlock,20,20,DensityUtil.DP));
                        }
                    }
            );
        }
        btnPopStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPressedBtnPopStartStop == false){
                    if(cuurentMode==GAME)
                    {
                        ballGameRule.start();
                        seekBar_difficulty.setOnTouchListener(
                                new View.OnTouchListener()
                                {
                                    @Override
                                    public boolean onTouch(View v, MotionEvent event) {
                                        //禁止用户拖动seekbar
                                        return true;
                                    }
                                }
                        );
                    }
                    isPressedBtnPopStartStop = true;
                    btnPopStartStop.setCompoundDrawables(STOP,null,null,null);
                    btnPopStartStop.setText("停止        ");
                    timer_PopBall.setMode(CircularRingPercentageView.TIMER);
                    timer_PopBall.start();
                }
                else{
                    isPressedBtnPopStartStop = false;
                    btnPopStartStop.setCompoundDrawables(START,null,null,null);
                    btnPopStartStop.setText("开始        ");
                    timer_PopBall.pause();
                    startResultActivity();
                    finish();
                }
            }
        });
    }
    private void startResultActivity()
    {
        Intent intent=new Intent();
        //TODO 传入游戏结果
        intent.setClass(BallGameMainActivity.this,GameResultActivity.class);
        startActivity(intent);
    }
    private int foreHand=0;
    private int backHand=0;
    private void updateTextView()
    {
        List<Double> doubles=dataManager.getFightEntireData();
        //TODO
        int juggle=1;
        //basicAnalysis.juggleDirection(doubles)
        if(juggle==1)
        {
            foreHand++;
            ballGameRule.playerAction(1);
        }
        else if(juggle==-1)
        {
            backHand++;
            ballGameRule.playerAction(2);
        }
        double vel=Math.abs(basicAnalysis.bitVel
                (DensityUtil.convertDoublesToDoubleArray(dataManager.getFightEntireData(),15)));
        textView_ForeHandPopNum.setText(foreHand+"个");
        textView_BackHandPopNum.setText(backHand+"个");
        textView_PopDropTime.setText("");
        textView_AvgPopSpeed.setText(DensityUtil.floatPrecision(2,(float)vel)+"m/s");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        Intent intent = getIntent();
        try
        {
            mDeviceName = intent.getStringExtra("DeviceName");
            mDeviceAddress = intent.getStringExtra("DeviceAddress");
            cuurentMode=intent.getStringExtra("GameMode").equals("GAME")?GAME:FREE;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        changeMode();
        textView_ForeHandPopNum=(TextView)findViewById(R.id.textView_ForeHandPopNum);
        textView_BackHandPopNum=(TextView)findViewById(R.id.textView_BackHandPopNum);
        textView_PopDropTime=(TextView)findViewById(R.id.textView_PopDropTime);
        textView_AvgPopSpeed=(TextView)findViewById(R.id.textView_AvgPopSpeed);
        dataManager.setDataCallBack(
                new DataCallBack() {
                    @Override
                    public void onDataFinish() {
                        //TODO
                        //Main2Activity.doubles=dataManager.getDataList(dataManager.convertStrToDoubleList(","));
                        //Main2Activity.cacheCount++;
                        updateTextView();
                    }
                }
        );
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ballgame, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.mode_free&&cuurentMode!=FREE)
        {
            cuurentMode=FREE;
            changeMode();
            return true;
        }
        else if(id == R.id.mode_game&&cuurentMode!=GAME)
        {
            cuurentMode=GAME;
            changeMode();
            return true;
        }
//        else if(id == R.id.ble)
//        {
//            Intent intent=new Intent();
//            intent.setClass(BallGameMainActivity.this,TestDeviceScanActivity.class);
//            startActivity(intent);
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        //this.mediaPlayerManager.pauseVideo();
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
        //this.mediaPlayerManager.reStartVideo();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    private void changeMode()
    {
        switch (cuurentMode)
        {
            case FREE:
                setContentView(R.layout.activity_ball_game_main);
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_BallGame);
                toolbar.setTitle("");
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                toolbar.setNavigationOnClickListener(
                        new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        }
                );
                timer_PopBall = (CircularRingPercentageView)findViewById(R.id.timer_PopBall);
                timer_PopBall.setMode(CircularRingPercentageView.BALLGAME);
                btnPopStartStop = (Button)findViewById(R.id.btnPopStartStop);
                START = getResources().getDrawable(R.drawable.start);
                STOP = getResources().getDrawable(R.drawable.stop);
                Rect rectTmp = new Rect(60, 0, 160, 100);
                IconCompress.IconDealing(rectTmp);
                START.setBounds(rectTmp);
                STOP.setBounds(rectTmp);
                btnPopStartStop.setCompoundDrawables(START,null,null,null);
                myListener();
                break;
            case GAME:
                setContentView(R.layout.activity_ball_game_task);
                toolbar = (Toolbar) findViewById(R.id.toolbar_BallGame);
                toolbar.setTitle("");
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                toolbar.setNavigationOnClickListener(
                        new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        }
                );
                seekBar_volume=(SeekBar)findViewById(R.id.seekBar_volume);
                seekBar_difficulty=(SeekBar)findViewById(R.id.seekBar_difficulty);
                textView_rate=(TextView)findViewById(R.id.textView_rate);
                timer_PopBall = (CircularRingPercentageView)findViewById(R.id.timer_BallGameTask);
                timer_PopBall.setMode(CircularRingPercentageView.BALLGAME);
                btnPopStartStop = (Button)findViewById(R.id.btnPopStartStop);
                START = getResources().getDrawable(R.drawable.start);
                STOP = getResources().getDrawable(R.drawable.stop);
                rectTmp = new Rect(60, 0, 160, 100);
                IconCompress.IconDealing(rectTmp);
                START.setBounds(rectTmp);
                STOP.setBounds(rectTmp);
                btnPopStartStop.setCompoundDrawables(START,null,null,null);
                difficultLock=(Button)findViewById(R.id.lock);
                difficultLock.setBackground(DensityUtil.scaleImage(this,R.drawable.unlock,40,40,DensityUtil.DP));
                myListener();
                break;
        }

    }
}
