package com.example.myapplication;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.drm.DrmStore;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fr.castorflex.android.verticalviewpager.VerticalViewPager;

public class FightActivity extends AppCompatActivity {
    //存储当前界面view临时变量
    private View vtmp;
    //页面变量
    private View upView;
    private View downView;
    //蓝牙相关
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    //竖向滑动界面
    private PagerAdapter adapter;
    private VerticalViewPager VVP;
    private List<View> viewList=new ArrayList<View>();
    private View view1, view2;
    //界面主按钮相关
    private Button btnStartStop;
    private Boolean isPressedBtnStartStop = false;
    private boolean hasEverPressedBtnStartStop = false;
    private Drawable START, STOP;
    //界面文字域
    private TextView blueToothStae;
    private TextView textView_HitType;
    private TextView textView_Speed;
    private TextView textView_Force;
    private TextView textView_HitNum;
    private TextView textView_ForeBackHand;
    private TextView textView_AvgBallSpeedDownPage;
    private TextView textView_LeftRightRoll;
    private TextView textView_MaxBallSpeedDownPage;
    private TextView textView_DropTimeDownPage;
    private TextView textView_Power;
    //界面其他
    private Toolbar toolbar;
    private OpenGLView openGLView;
    private RelativeLayout relativeLayout;
    private LineChartManager lineChartManager;
    //3D模型区块
    private SurfaceView surfaceView_3Dmodel;
    private MediaPlayerManager mediaPlayerManagerFor3D;
    private String mediaPath_Forehand= Environment.getExternalStorageDirectory().getAbsolutePath()+
            File.separator+"ballGame/demo_zheng.mp4";            //正手3D
    private String mediaPath_Backhand= Environment.getExternalStorageDirectory().getAbsolutePath()+
            File.separator+"ballGame/demo_fan.mp4";
    //图
    private PieChart pieChart;
    private LineChart lineChart_Fight;
    private DataChartManager dataChartManager;
    private String dataPath=Environment.getExternalStorageDirectory().getAbsolutePath()+
            File.separator+"ballGame/default/data.txt";
    //储存数据
    private DataManager dataManager=DataManager.getInstance();
    private final String dataRootPath=Environment.getExternalStorageDirectory().getAbsolutePath()+
            File.separator+"ballGame/fight";
    private String currentStoreFile;
    private WriteDataToFile writeDataToFile;
    private boolean isRecording=false;

    private int hitCounter=0;
    private List<Double> keepData=new ArrayList<>();

    private final int LOADACTION=0;
    //服务器相关
    //连接服务器信息更新
    private Handler handler=new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle=msg.getData();
            String result=bundle.get("CLIENTRESULT").toString();
            switch (result)
            {
                case "UPLOADSUCCESS":
                    new AlertDialog.Builder(FightActivity.this).setMessage("上传文件成功").show();
                    break;
                case "UPLOADFAIL":
                    new AlertDialog.Builder(FightActivity.this).setMessage("上传文件失败").show();
                    break;
            }
        }
    };
    private AIPingPongClientUsage aiPingPongClientUsage=new AIPingPongClientUsage(this.handler);

    private BasicAnalysis basicAnalysis=new BasicAnalysis();
    BluetoothGattCharacteristic mGattCharacteristics;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                blueToothStae.setVisibility(View.GONE);
                mConnected = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                blueToothStae.setVisibility(View.VISIBLE);
                mConnected = false;
//                textView_blueTooth.setText("未连接蓝牙");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //ReadDataFromCharacteristic();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)&&hasBegun) {
                //TODO
                byte[] data=intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                dataManager.fightDataByteSplit(data);
                System.out.println("data from BLT");
            }
        }
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

    public void findViewOfUpPage(){
        upView = view1;
        LineChart lineChart=(LineChart)view1.findViewById(R.id.linechart_Fight);
        this.lineChartManager=new LineChartManager(lineChart,
                new String[]{"x轴加速度","y轴加速度","z轴加速度"},
                new int[]{Color.parseColor("#ff0000"),Color.parseColor("#00ff00"),Color.parseColor("#0000ff")});
        blueToothStae=(TextView)view1.findViewById(R.id.blutooth_state);
        if(Main2Activity.getConnectState())
            blueToothStae.setVisibility(View.GONE);
        openGLView=(OpenGLView)view1.findViewById(R.id.model_3d);
        lineChart_Fight = (LineChart)view1.findViewById(R.id.linechart_Fight);
        textView_HitType = (TextView)view1.findViewById(R.id.textView_hitType);
        textView_Speed = (TextView)view1.findViewById(R.id.textView_speed);
        textView_Force = (TextView)view1.findViewById(R.id.textView_force);
        textView_HitNum = (TextView)view1.findViewById(R.id.textView_number);
        relativeLayout =(RelativeLayout) view1.findViewById(R.id.relativelayout_slip);
        relativeLayout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        VVP.setCurrentItem(1);
                    }
                }
        );
        TabHost tabHost=(TabHost)view1.findViewById(R.id.tabhost_fight);
        tabHost.setup();
        TabRender.TabHostRender(new int[]{R.id.tab1, R.id.tab2},
                new int[]{R.drawable.line_3d,R.drawable.line_3d_click,
                        R.drawable.chartline,R.drawable.chartline_click},tabHost,this);
    }

    public void findViewOfDownPage(){
        downView = view2;
        //TODO
        pieChart=(PieChart)view2.findViewById(R.id.piechart_fight);
        if(pieChart==null)
            System.out.println("piechart null");
        textView_ForeBackHand=(TextView)findViewById(R.id.textView_ForeBackHand);
        textView_AvgBallSpeedDownPage=(TextView)findViewById(R.id.textView_AvgBallSpeedDownPage);
        textView_LeftRightRoll=(TextView)findViewById(R.id.textView_LeftRightRoll);
        textView_MaxBallSpeedDownPage=(TextView)findViewById(R.id.textView_MaxBallSpeedDownPage);
        textView_DropTimeDownPage=(TextView)findViewById(R.id.textView_DropTimeDownPage);
        textView_Power=(TextView)findViewById(R.id.textView_Power);
    }
    private boolean hasBegun=false;
    private long startTime;
    private float speedCounter=0;
    private float maxSpeedRecorder=0;
    public void myListener(){

        btnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(VVP.getCurrentItem() == 0){
                hasBegun=true;
                startOrPauseRecording();
                if(isPressedBtnStartStop == false) {
                    FightActivity.this.speedCounter=FightActivity.this.maxSpeedRecorder
                            =FightActivity.this.hitCounter=0;
                    FightActivity.this.startTime=System.currentTimeMillis();
                    //String data="~,0,0,0,0,0,0,-735,203,263,-8105,573,-9639,-15568,-4372,15632,0,0,0,-964,-225,-626,-638,259,150,-7243,2738,-10108,25486,-6926,12322,-24,-5,-15,-2338,-429,-1385,-479,317,40,-6625,4907,-9453,8402,-12081,9808,-82,-16,-50,-4082,-868,-2149,-286,348,6247,-6751,5510,-6501,-4337,-25956,7396,-184,-38,-104,-5549,-2075,-2785,-139,328,6270,-8021,4333,-1427,8205,14093,9061,-323,-89,-173,-6664,-4158,-3140,-62,253,144,-10195,2501,3855,10412,-9153,18218,-490,-193,-252,-7021,-6498,-3254,-46,125,419,-11975,154,7606,15142,-12876,17526,-665,-356,-333,-6217,-8431,-3457,-55,-34,753,-12391,-2824,9291,-29462,-12876,624,-820,-567,-420,-4547,-9438,-3418,-89,-196,1114,-11377,-5956,9754,-7029,-12876,-3441,-934,-803,-505,-2862,-9406,-3056,-126,-339,1485,-9990,-7660,9438,7045,-5768,-341,-1006,-1038,-581,-1855,-8767,-2771,-136,-451,1847,-8411,-7063,7672,13286,15127,-2704,-1052,-1257,-651,-1468,-7938,-2553,-112,-521,2166,-7487,-5850,4815,18714,29054,-1681,-1089,-1455,-715,-1396,-7296,-2489,-81,-556,2400,-6840,-4113,1194,15127,-24267,-1777,-1124,-1638,-777,-1475,-6766,-2455,-57,-574,2545,-7050,-3002,-1506,14519,-19288,-40,-1161,-1807,-838,-1741,-6242,-2427,-48,-593,2620,-7283,-2322,-3637,19754,-16781,899,-1204,-1963,-899,-2010,-5829,-2416,-61,-616,2635,-7645,-2109,-5533,17572,-13453,1929,-1254,-2109,-959,-2159,-5571,-2489,-103,-643,2591,-7770,-1613,-7413,10619,-8694,987,-1308,-2248,-1021,-2237,-5440,-2598,-160,-664,2490,-7894,-568,-8821,6052,-5141,1947,-1364,-2384,-1086,-2332,-5376,-2701,-216,-681,2357,-8207,116,-9181,5470,-2905,3860,-1423,-2519,-1154,-2468,-5316,-2780,-275,-702,2216,-8576,-776,-9219,6880,-2078,5275,-1484,-2651,-1223,-2614,-5281,-2829,-350,-734,2077,-9182,-1357,-8706,6789,-1960,6865,-1550,-2784,-1294,-2795,-5239,-2825,-446,-774,1936,-9663,-1988,-8292,8725,-2560,8514,-1620,-2914,-1365,-2993,-5191,-2806,-561,-815,1789,-9801,-2166,-8081,9705,-2668,8733,-1694,-3044,-1435,-3176,-5167,-2843,-686,-845,1634,-9683,-2084,-8078,8050,-1436,7554,!";
                    String data="~,0,0,0,0,0,0,-127,-604,1874,-5444,9155,-9457,-9418,2651,16039,0,0,0,-179,-461,30,-129,-619,1712,-4905,9657,-9435,-15234,1507,16700,-4,-11,0,-399,-1085,50,-114,-620,1552,-4293,10387,-9275,-23421,615,16261,-14,-38,2,-697,-1831,94,-74,-611,1409,-3796,10922,-8492,-30255,-926,15765,-31,-84,4,-1048,-2630,246,-1,-598,1299,-3348,11427,-7139,30997,-4847,15973,-58,-150,10,-1407,-3469,506,112,-580,1237,-2932,11868,-5281,27303,-8912,14132,-93,-236,23,-1638,-4344,882,265,-551,1230,-2767,12031,-3084,25559,-15546,9447,-134,-345,45,-1603,-5076,1458,445,-504,1282,-3040,11694,-650,30201,-24314,9808,-174,-472,81,-1310,-5700,2232,636,-426,1387,-3744,11216,1845,29392,-31509,11181,-207,-614,137,-569,-6032,3153,818,-308,1537,-4752,10309,4313,31414,22716,15159,-221,-765,216,583,-6013,3838,958,-135,1712,-5220,9040,6437,-28410,15159,12929,-206,-916,312,1922,-5750,4041,1027,90,1892,-5172,7455,7969,-23058,13262,5821,-158,-1059,413,3177,-5299,3774,1013,346,2080,-5002,6009,8464,-19084,15748,1584,-79,-1192,507,4208,-4610,3069,902,592,2305,-5120,4603,8251,-14034,14546,1842,25,-1307,584,4646,-3957,1779,649,789,2615,-5314,2301,7707,2702,13768,-6919,142,-1406,628,4372,-3404,609,239,888,3016,-4994,662,6021,15003,23847,-3926,251,-1491,644,3909,-3067,-372,-157,888,3372,-3658,1409,3205,13931,31683,-7388,!";
                    //Main2Activity.dataManager.addData(data);
                    isPressedBtnStartStop = true;
                    btnStartStop.setCompoundDrawables(STOP,null,null,null);
                    btnStartStop.setText("停止        ");
                }
                else{
                    float time=(float)(System.currentTimeMillis()-startTime)/1000;
                    Main2Activity.databaseService.updateMessageOfDailyRecord(
                            FightActivity.this.hitCounter,
                            FightActivity.this.speedCounter/FightActivity.this.hitCounter,
                            FightActivity.this.maxSpeedRecorder,time);
                    isPressedBtnStartStop = false;
                    btnStartStop.setCompoundDrawables(START,null,null,null);
                    btnStartStop.setText("开始        ");
                }
            }
            else {
                VVP.setCurrentItem(0);
            }
            }
        });
        VVP.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 1){
                    //toolbar.setTitle("00:00");
                    btnStartStop.setCompoundDrawables(STOP,null,null,null);
                    btnStartStop.setText("返回顶部           ");
                }
                else{
                    //toolbar.setTitle("实战");
                    if(isPressedBtnStartStop == false) {
                        btnStartStop.setCompoundDrawables(START,null,null,null);
                        btnStartStop.setText("开始        ");
                    }
                    else{
                        btnStartStop.setCompoundDrawables(STOP,null,null,null);
                        btnStartStop.setText("停止        ");
                    }
                }
            }
//            class Task extends TimerTask{
//                public void run(){
//                    vtmp = adapter.currentFragment.getView();
//                    if(upView != null && vtmp != upView && isPressedBtnStartStop){
//                        findViewOfDownPage();
//                        hasEverPressedBtnStartStop = true;
//                        mediaPlayerManagerFor3D = new MediaPlayerManager(surfaceView_3Dmodel, mediaPath_Forehand, true);
//                        mediaPlayerManagerFor3D.startVideo();
//                        dataChartManager = new DataChartManager(lineChart_Fight,dataPath,
//                                new String[]{"accX","accY","accZ"},new int[]{Color.RED,Color.BLUE,Color.GREEN},FightActivity.this);
//                        dataChartManager.start();
//                    }
//                }
//            }
            @Override
            public void onPageScrollStateChanged(int state) {
//                if(state == 0){
//                    if(hasEverPressedBtnStartStop == false){
//                        if(isPressedBtnStartStop && vtmp == upView)
//                        {
//                            Timer tTmp = new Timer();
//                            Task ttTmp = new Task();
//                            tTmp.schedule(ttTmp, 50);
//                        }
//                    }
//                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        setContentView(R.layout.activity_fight);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_Fight);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Resources resources=this.getResources();
        //Drawable drawable=resources.getDrawable(R.drawable.bluetooth);
        //getSupportActionBar().setHomeAsUpIndicator(drawable);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );
        File folderPath = new File(dataRootPath);
        if(!folderPath.exists()){
            folderPath.mkdir();
        }

        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra("DeviceName");
        mDeviceAddress = intent.getStringExtra("DeviceAddress");

//        toolbar = (Toolbar) findViewById(R.id.toolbar_Fight);
//        setSupportActionBar(toolbar);

        btnStartStop = (Button)findViewById(R.id.btnStartStop);
        START = getResources().getDrawable(R.drawable.start);
        STOP = getResources().getDrawable(R.drawable.stop);
        Rect rectTmp = new Rect(60, 0, 160, 100);
        IconCompress.IconDealing(rectTmp);
        START.setBounds(rectTmp);
        STOP.setBounds(rectTmp);
        btnStartStop.setCompoundDrawables(START,null,null,null);


//        List<Fragment> fragments=new ArrayList<Fragment>();
//        fragments.add(new FightActivityUpFragment());
//        fragments.add(new FightActivityDownFragment());
//        adapter = new FragAdapter(getSupportFragmentManager(), fragments);
        LayoutInflater inflater=getLayoutInflater();
        view1 = inflater.inflate(R.layout.fragment_fight_activity_up, null);
        view2 = inflater.inflate(R.layout.fragment_fight_activity_down, null);
        viewList.add(view1);
        //viewList.add(view2);
        adapter = new PagerAdapter() {
            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                // TODO Auto-generated method stub
                return arg0 == arg1;
            }
            @Override
            public int getCount() {
                // TODO Auto-generated method stub
                return viewList.size();
            }
            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                // TODO Auto-generated method stub
                container.removeView(viewList.get(position));
            }
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                // TODO Auto-generated method stub
                container.addView(viewList.get(position));
                return viewList.get(position);
            }
        };
        VVP = (VerticalViewPager)findViewById(R.id.container);
        VVP.setAdapter(adapter);

        findViewOfUpPage();
        findViewOfDownPage();
//        VVP.setCurrentItem(0);
//        View view_up=fragments.get(0).getView();
//        relativeLayout =(RelativeLayout) view_up.findViewById(R.id.relativelayout_slip);
//        relativeLayout.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        VVP.setCurrentItem(1);
//                    }
//                }
//        );
        myListener();
        dataManager.setDataCallBack(
                new DataCallBack() {
                    @Override
                    public void onDataFinish() {
                        //TODO
                        try
                        {
                            List<Double> list=dataManager.getFightEntireData();
                            for(double x:list)
                                System.out.println("in callback "+x);
                            keepData.clear();
                            for(double x:list)
                                keepData.add(x);
                            if(keepData!=null)
                            {
                                if(dataManager.getHitingPoint()>0)
                                    hitCounter++;
                                if(openGLView!=null)
                                {
                                    openGLView.setData(keepData);
                                    openGLView.requestRender();
                                }
                                //lineChartManager.setLineData(doubles);
                                if(isRecording) {
                                    String ans="";
                                    for(double x:keepData)
                                        ans+=x+",";
                                    ans+="\n";
                                    System.out.println(ans);
                                    writeDataToFile.write(ans);
                                }
                                updateTextView();
                            }
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }
    private synchronized void startOrPauseRecording()
    {
        if(isRecording)
        {
            this.writeDataToFile.close();
            this.writeDataToFile=null;
            isRecording=false;
            //上传数据文件
//            if(this.currentStoreFile!=null)
//            {
//                this.aiPingPongClientUsage.upLoadFile("url",currentStoreFile);
//                this.currentStoreFile=null;
//            }
        }
        else
        {
            currentStoreFile=this.dataRootPath+File.separator
                    +Main2Activity.databaseService.getUser_id()+" "+DateTool.getCurrentTime()+".txt";
            this.writeDataToFile=new WriteDataToFile(currentStoreFile);
            isRecording=true;
        }
    }
    private void updateTextView()
    {
        //TODO
        try
        {
            List<Double> doubles=dataManager.getFightEntireData();
            if(doubles.size()==0)
                return ;
            double force=0;
            double speed=0;
            for(int i=0,len=doubles.size();i<len;i+=15)
            {
                force+=doubles.get(i+3)*0.185f;
                speed+=doubles.get(i+4);
            }
            force/=doubles.size()/15;
            force=Math.abs(force);
            speed/=doubles.size()/15;
            speed=Math.abs(speed);
            //textView_HitType.setText("");
            float tmp=10*(float)speed;
            this.speedCounter+=tmp;
            this.maxSpeedRecorder=Math.max(this.maxSpeedRecorder,tmp);
            textView_Speed.setText(DensityUtil.floatPrecision(2,tmp)+" m/s");
            textView_Force.setText(DensityUtil.floatPrecision(2,100*(float)force)+" N");
            textView_HitNum.setText(hitCounter+"");
            textView_HitType.setText("正手");

//            textView_ForeBackHand.setText("");
//            textView_AvgBallSpeedDownPage.setText("");
//            textView_LeftRightRoll.setText("");
//            textView_MaxBallSpeedDownPage.setText("");
//            textView_DropTimeDownPage.setText("");
            //textView_Power.setText(DensityUtil.floatPrecision(2,(float)basicAnalysis.bitForce(Main2Activity.doubles))+"");//
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK)
        {
            if(requestCode == LOADACTION)
            {
                try
                {
                    String path= data.getStringExtra("path");
                    ReadDataFromFile readDataFromFile=new ReadDataFromFile(path,false);
                    String actionData=readDataFromFile.nextLineData();
                    String actions[]=actionData.split(",");
                    System.out.println(actions.length);
                    List<Double> list=new ArrayList<>();
                    for(int i=0,len=actions.length;i<len;i++)
                        list.add(Double.valueOf(actions[i]));
                    if(openGLView!=null)
                    {
                        openGLView.setData(list);
                        openGLView.requestRender();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        final String rootFilePath=PlayBack.getSDPath()+File.separator+
                "ballGame"+File.separator+"Action";
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_collect) {
            System.out.println("正在保存动作");
            File rootFile=new File(rootFilePath);
            if(!rootFile.exists())
                rootFile.mkdir();
            final String fileName= rootFilePath+File.separator+DateTool.getCurrentTime()+".txt";
            System.out.println(fileName);
            WriteDataToFile writeDataToFile=new WriteDataToFile(fileName);
            List<Double> list=keepData;
            //System.out.println((list == null)+" "+list.size());
            String data="";
            for(double x:list)
                data+=x+",";
            writeDataToFile.write(data);
            writeDataToFile.close();
            System.out.println("保存动作成功");
            return true;
        }
        else if(id == R.id.action_load)
        {
            System.out.println("正在加载动作");
            Intent intent =new Intent();
            intent.putExtra("title","选择动作");
            intent.putExtra("rootdir",rootFilePath);
            intent.putExtra("type","txt");
            intent.setClass(FightActivity.this,ExDialog.class);
            ExDialog.Mode=ExDialog.SPECIALTYPE;
            startActivityForResult(intent, LOADACTION);
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fight, menu);
        return true;
    }
}
