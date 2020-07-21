package com.example.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    public static final int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final int PERMISSION_REQUEST_STORAGE = 0;
    private static final int PERMISSION_REQUEST_CAMERA = 2;
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 3;

    public static double screenWidthNow = 0;
    public static double screenLengthNow = 0;
    public static double defaultScreenWidth = 1080;
    public static double defaultScreenLength = 1920;
    public static double iconScaling = 1;
    private static final int LIGHTTHEME = 0;
    private static final int DARKTHEME = 1;
    private static int backTheme = LIGHTTHEME;

    public static final String[] permissionsArray = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO};
    private List<String> permissionsList = new ArrayList<String>();

    public static DatabaseService databaseService;
    private Button btnData;
    private Button btnTendency;
    private Button btnChooseMode;
    private TextView textView_TrainingTime;
    private TextView textView_AvgBallNum;
    private TextView textView_AvgSpeed;
    private TextView textView_jiance;
    private ImageView imageView_jiaoshi;
    private ImageView imageView_luntan;
    private ImageView imageView_zhanghao;
    private ImageView imageView_xunlian;
    private TextView textView_rank_sportTime;
    private TextView textView_rank_number;
    private TextView textView_rankType;
    private RelativeLayout inner_Relative_ForeHandNormalHit;
    private RelativeLayout inner_Relative_BackHandNormalHit;
    private RelativeLayout inner_Relative_ForeHandLongHit;
    private RelativeLayout inner_Relative_BackHandLongHit;
    private RelativeLayout inner_Relative_page2;

    private View view1, view2, view3, view4,view5;
    private View view_hitNumber;
    private View view_sportTime;
    private ViewPager viewPager_main2;  //对应的viewPager
    private ViewPager viewPager_page3;
    private List<View> viewList;//view数组
    private ScrollerBar scrollerBar_main2;
    private ScrollerBar scrollerBar_page3;
    //蓝牙选项
    private MenuItem blueToothItem;
    private String blueToothItemTitle="";
    private TextView textView_blueTooth;

    private TabManager tabManager;
    //时间相关
    //private SystemTimeManager systemTimeManager;
    private CircularRingPercentageView timerView;

    //private boolean changed=false;
    //视频相关对象
    private SurfaceView surfaceView;
    private MediaPlayerManager mediaPlayerManagerForReal;

    private SurfaceView surfaceView_pingpang;
    private MediaPlayerManager mediaPlayerManager;
    //文件名称
    private String chooseFilePath= Environment.getExternalStorageDirectory().getAbsolutePath()+
            File.separator+"ballGame/default";
    private String nameOfReal="real.mp4";
    private String nameOfData="data.txt";
    private String nameOfCounter="counterdata.txt";
    private String nameOfDrop="dropdata.txt";

    //  更新UI标志
    public final static int UPDATETIME=0;
    private static final int REQUEST = 1;
    private static final int REQUEST1 = 2;
    private static final int REQUEST_BULETOOTH=3;
    private static final int REQUEST_PLAYBACK = 4;
    //是否已经开始
    private String RankString;//选择的难度
    //标志位
    private boolean ifEverStarted=false;            //是否已经开始
    private boolean shouldPause=false;
    private boolean counterEverRelease=false;       //计数器是否曾被销毁
    private boolean hasChooseBluetooth=false;
    //蓝牙相关
    private DataManager dataManager=DataManager.getInstance();
    public static String mDeviceName;
    public static String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private static boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private LoadDialog loadDialog;
    private Handler handler=new Handler(
            new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
//                    if(!hasChooseBluetooth)
//                        return false;
//                    hasChooseBluetooth=false;
                    Bundle bundle=msg.getData();
                    try
                    {
                        String text= bundle.getString("showToast");
                        Toast toast;
                        toast=Toast.makeText(Main2Activity.this,text,Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    return false;
                }
            }
    );
    //数据相关
    private AIPingPongClientUsage aiPingPongClientUsage=new AIPingPongClientUsage(null);
    private List<UserDataStruct> userDataStructList_hitNumber=new ArrayList<UserDataStruct>();
    private List<UserDataStruct> userDataStructList_sportTime=new ArrayList<UserDataStruct>();

    BluetoothGattCharacteristic mGattCharacteristics;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("main");
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                textView_blueTooth.setText("已连接蓝牙设备:" + mDeviceName);
                mConnected = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                textView_blueTooth.setText("未连接蓝牙");

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                Timer t = new Timer();
                Task tt = new Task();
                t.schedule(tt, 5000);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //TODO
                byte[] data=intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                dataManager.dataByteSplit(data);
            }
        }
    };
    public static boolean getConnectState()
    {
        return mConnected;
    }
    public void FindBlueToothList(List<BluetoothGattService> gattServices){
        if (gattServices == null) return;
        for (BluetoothGattService gattService : gattServices) {
            if(gattService.getUuid().toString().equals("6e400001-b5a3-f393-e0a9-e50e24dcca9e")){
                mGattCharacteristics = gattService.getCharacteristics().get(0);
                break;
            }
        }
    }

    class Task extends TimerTask {
        public void run(){
            loadDialog.cancel();
            Message message =new Message();
            Bundle bundle=message.getData();
            String text="";
            if(mConnected)
                text+="连接"+mDeviceName+"成功";
            else
                text+="连接失败，请重新尝试";
            bundle.putString("showToast",text);
            handler.sendMessage(message);
            ReadDataFromCharacteristic();
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

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST) {
//                this.chooseFilePath=intent.getExtras().getString("path");
//                this.resetController(this.chooseFilePath);
//                this.shouldPause=true;
            }
            else if(requestCode == REQUEST1){
                RankString = intent.getExtras().getString("ReturnRank");
                //new AlertDialog.Builder(this).setMessage(RankString).show();
            }
            else if(requestCode == REQUEST_BULETOOTH)
            {
                try
                {
                    hasChooseBluetooth=true;
                    loadDialog=LoadDialog.showDialog(Main2Activity.this);
                    loadDialog.show();
                    //TODO
                    //System.out.println(blueToothItem.getTitle());
                    mDeviceName = intent.getExtras().getString("Name");
                    mDeviceAddress = intent.getExtras().getString("Address");

                    Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
                    bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

                    invalidateOptionsMenu();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private Boolean CopyAssetsFile(String filename, String des) {
        Boolean isSuccess = true;
        //复制安卓apk的assets目录下任意路径的单个文件到des文件夹，注意是否对des有写权限
        AssetManager assetManager = this.getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            String newFileName = des + "/" + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            e.printStackTrace();
            isSuccess = false;
        }
        return isSuccess;
    }

    private void myListener() {
        btnChooseMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
//                if(mConnected == false){
//                    new AlertDialog.Builder(Main2Activity.this).setMessage("未检测到蓝牙，请连接蓝牙").show();
//                }
//                else
//                {
                    Intent intent = new Intent();
                    intent.setClass(Main2Activity.this, ModeChoose.class);
                    intent.putExtra("DeviceName", mDeviceName);
                    intent.putExtra("DeviceAddress", mDeviceAddress);
                    startActivity(intent);
//                }
            }
        });
        viewPager_main2.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                scrollerBar_main2.setOffset(position, positionOffset);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPager_page3.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                scrollerBar_page3.setOffset(position, positionOffset);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        imageView_xunlian.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(viewPager_main2.getCurrentItem()!=0)
                            viewPager_main2.setCurrentItem(0);
                    }
                }
        );
        imageView_luntan.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(viewPager_main2.getCurrentItem()!=3)
                            viewPager_main2.setCurrentItem(3);
                    }
                }
        );
        imageView_jiaoshi.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(viewPager_main2.getCurrentItem()!=1)
                            viewPager_main2.setCurrentItem(1);
                    }
                }
        );
        imageView_zhanghao.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(viewPager_main2.getCurrentItem()!=4)
                            viewPager_main2.setCurrentItem(4);
                    }
                }
        );
        textView_rank_number.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(viewPager_page3.getCurrentItem()!=1)
                        {
                            viewPager_page3.setCurrentItem(1);
                            updateUserRank();
                        }
                    }
                }
        );
        textView_rank_sportTime.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(viewPager_page3.getCurrentItem()!=0)
                        {
                            viewPager_page3.setCurrentItem(0);
                            updateUserRank();
                        }
                    }
                }
        );
        inner_Relative_ForeHandNormalHit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        inner_Relative_BackHandNormalHit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        inner_Relative_ForeHandLongHit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        inner_Relative_BackHandLongHit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
    private void addDataIntoDataBase()
    {
        if(databaseService.ifExistDataOfOneDay("2017-01-01"))
            return ;
        Random random=new Random();
        String date="2017-01-01";
        for(int i=0;i<700;i++)
        {
            int hit=random.nextInt(200);
            float aver_speed= random.nextFloat()*20+1;
            float max_speed=random.nextFloat()*40+1;
            float sport_time=random.nextFloat()+1000;
            databaseService.InsertDataToDailyRecord(date,hit,aver_speed,max_speed,sport_time);
            date=DateTool.nextOrLastDay(date,1);
        }
        /*databaseService.setUser_id("lily");
        date="2000-1-10";
        for(int i=0;i<1000;i++)
        {
            int hit=random.nextInt(200);
            float aver_speed= random.nextFloat()*100+1;
            float max_speed=random.nextFloat()*200+1;
            float sport_time=random.nextFloat()*120+1;
            databaseService.InsertDataToDailyRecord(date,hit,aver_speed,max_speed,sport_time);
            date=DateTool.nextOrLastDay(date,1);
        }*/
        //databaseService.InsertDataToDailyRecord(10,10,10,10);
    }
    private void addUserDataIntoDataBase()
    {
        if(databaseService.ifUserRecordExistUser("用户0"))
            return;
        Random random=new Random();
        String positions[]={"重庆","北京","上海","厦门","广州","桂林","成都","乌鲁木齐","兰州","西安","吉林","天津","未知地区",};
        for(int i=0;i<=100;i++)
        {
            UserDataStruct struct=new UserDataStruct("用户"+i,positions[random.nextInt(positions.length)],"用户个性签名"+i,random.nextInt(100),random.nextInt(500),random.nextFloat()*10+1);
            databaseService.insertMessageIntoUserRecord(struct);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        //生成必要文件夹
        final String newRootPath=Environment.getExternalStorageDirectory().getAbsolutePath()+
                File.separator+"ballGame";
        File folderPath = new File(newRootPath);
        if(!folderPath.exists()){
            folderPath.mkdir();
        }
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        databaseService =new DatabaseService(this,"bill");
        addDataIntoDataBase();
        addUserDataIntoDataBase();
        //dataManager.setBeginAndEnd("~","!");
        dataManager.setDataCallBack(
                new DataCallBack() {
                    @Override
                    public void onDataFinish() {
                        //TODO
                        //doubles=dataManager.getDataList(dataManager.convertStrToDoubleList(","));
                        //cacheCount++;
                    }
                }
        );
        //this.addDataIntoDataBase();
        //databaseService.getAllFromDailyRecord();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.main_back_color_lightdark));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissionsArray) {
                if (this.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsList.add(permission);
                }
            }
            if(permissionsList.size()!=0)
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), REQUEST_CODE_ASK_PERMISSIONS);
        }

        File assetsPath = new File(newRootPath + File.separator + "pingpongbat.stl");
        if(!assetsPath.exists())
            CopyAssetsFile("pingpongbat.stl",newRootPath);

        scrollerBar_main2 = (ScrollerBar)findViewById(R.id.scrollBar_main2);
        scrollerBar_main2.setTabNum(5);
        scrollerBar_main2.setOffset(0, 2);
        viewPager_main2 = (ViewPager)findViewById(R.id.viewPager_main2);
        LayoutInflater inflater=getLayoutInflater();
        //view1=inflater.inflate(R.layout.activity_main2_page4, null);
        view1 = inflater.inflate(R.layout.activity_main2_page1, null);
        view2 = inflater.inflate(R.layout.activity_main2_page2, null);
        view3 = inflater.inflate(R.layout.activity_main2_page3, null);
        view4 = inflater.inflate(R.layout.activity_main2_page4, null);
        view5 = inflater.inflate(R.layout.activity_main2_page5, null);
        viewList = new ArrayList<View>();
        viewList.add(view5);
        viewList.add(view4);
        viewList.add(view1);
        viewList.add(view3);
        viewList.add(view2);
        PagerAdapter pagerAdapter = new PagerAdapter() {
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
        viewPager_main2.setAdapter(pagerAdapter);
        viewPager_main2.setCurrentItem(2);
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        screenWidthNow = point.x;
        screenLengthNow = point.y;
        if(screenWidthNow!=0 && screenLengthNow !=0){
            iconScaling = (defaultScreenWidth/screenWidthNow + defaultScreenLength/screenLengthNow)/2;
        }
        else{
            iconScaling = 1;
        }
        inner_Relative_page2=(RelativeLayout) view2.findViewById(R.id.page2);

        imageView_xunlian=(ImageView) findViewById(R.id.imageView_xunlian);
        imageView_jiaoshi=(ImageView) findViewById(R.id.imageView_jiaoshi);
        imageView_luntan=(ImageView) findViewById(R.id.imageView_paihang);
        imageView_zhanghao=(ImageView) findViewById(R.id.imageView_zhanghao);

        int imageWH = 80;
        imageView_xunlian.getLayoutParams().width =
        imageView_xunlian.getLayoutParams().height =
        imageView_jiaoshi.getLayoutParams().width =
        imageView_jiaoshi.getLayoutParams().height =
        imageView_luntan.getLayoutParams().width =
        imageView_luntan.getLayoutParams().height =
        imageView_zhanghao.getLayoutParams().width =
        imageView_zhanghao.getLayoutParams().height = (int)(imageWH/iconScaling);

        btnChooseMode = (Button)findViewById(R.id.btnChooseMode);
        Drawable START = getResources().getDrawable(R.drawable.start);

        Rect rectTmp = new Rect(60, 0, 160, 100);
        IconCompress.IconDealing(rectTmp);
        START.setBounds(rectTmp);
        rectTmp = new Rect(0,60,80,140);
        IconCompress.IconDealing(rectTmp);
        btnChooseMode.setCompoundDrawables(START,null,null,null);

        timerView=(CircularRingPercentageView)view1.findViewById(R.id.timer);
        timerView.setMode(CircularRingPercentageView.DAILYTASK);
        timerView.updateTextBottom(150);
//        Object tmp[]=Main2Activity.databaseService.getDataFromDailyRecordByDate(DateTool.getCurrentDate());
//        if(tmp!=null)
//            timerView.updateTextMiddle((int)tmp[2]);
//        else
//            timerView.updateTextMiddle(0);
        timerView.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(timerView.getState())
                        timerView.pause();
                    else
                        timerView.start();
                }
            }
        );
        //Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        //bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initTrainingPage();

        textView_TrainingTime=(TextView)view1.findViewById(R.id.textView_TrainingTime);
        textView_AvgSpeed=(TextView)view1.findViewById(R.id.textView_AvgSpeed);
        updateView();
        initMasonryLayout();
        initRankPage();
        initLoginPage();

        myListener();
    }
    private void initLoginPage()
    {
        //TODO
        LayoutInflater inflater=getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_login,null);
        final EditText editText_user= (EditText)view.findViewById(R.id.editText_user);
        final EditText editText_password= (EditText)view.findViewById(R.id.editText2_password);
        Button button_login=(Button) view.findViewById(R.id.login);
        Button button_register=(Button) view.findViewById(R.id.register);
        this.aiPingPongClientUsage.setClientResultCallback(
                new ClientResultCallback() {
                    @Override
                    public void onClientResult(String message) {
                        System.out.println("callback " +message);
                        Message mg =new Message();
                        Bundle bundle=mg.getData();
                        String text=""+message;
                        bundle.putString("showToast",text);
                        handler.sendMessage(mg);
                    }
                }
        );
        button_login.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String user=editText_user.getText().toString();
                        String password=editText_password.getText().toString();
                        System.out.println(user+" "+password);
                        aiPingPongClientUsage.login(user,password);
                    }
                }
        );

        inner_Relative_page2.removeAllViews();
        inner_Relative_page2.addView(view);
    }
    private void initTrainingPage() {
        inner_Relative_BackHandLongHit = (RelativeLayout) view5.findViewById(R.id.inner_Relative_BackHandLongHit);
        inner_Relative_BackHandNormalHit = (RelativeLayout) view5.findViewById(R.id.inner_Relative_BackHandNormalHit);
        inner_Relative_ForeHandLongHit = (RelativeLayout) view5.findViewById(R.id.inner_Relative_ForeHandLongHit);
        inner_Relative_ForeHandNormalHit = (RelativeLayout) view5.findViewById(R.id.inner_Relative_ForeHandNormalHit);
    }
    private void updateDataInRankPage()
    {
        databaseService.searchTopMessageFromUserRecord("hitnumber",100,this.userDataStructList_hitNumber);
        LinearLayout linearLayout=(LinearLayout) view_hitNumber.findViewById(R.id.userList);
        LayoutInflater inflater=getLayoutInflater();
        for(int i=1,len=this.userDataStructList_hitNumber.size();i<=len;i++)
        {
            UserDataStruct struct=this.userDataStructList_hitNumber.get(i-1);
            View view=inflater.inflate(R.layout.rank_item,null);
            TextView textView_rank=(TextView) view.findViewById(R.id.textView_rank);
            textView_rank.setText(i+"");
            //
            TextView userName=(TextView) view.findViewById(R.id.userName);
            userName.setText(struct.getUserName());
            TextView position=(TextView) view.findViewById(R.id.position);
            position.setText(struct.getPosition());
            TextView signature=(TextView) view.findViewById(R.id.signature);
            signature.setText(struct.getSignature());
            TextView data=(TextView) view.findViewById(R.id.rank_data);
            data.setText(struct.getHitNumber()+"个");
            final TextView number=(TextView) view.findViewById(R.id.number);
            number.setText(struct.getPraiseNumber()+"");

            final ImageView imageView=(ImageView) view.findViewById(R.id.state);
            imageView.setOnClickListener(
                    new View.OnClickListener() {
                        private boolean hasClicked=false;
                        @Override
                        public void onClick(View v) {
                            if(hasClicked)
                                return ;
                            hasClicked=true;
                            try
                            {
                                imageView.setImageDrawable(getResources().getDrawable(R.drawable.like));
                                number.setText((Integer.valueOf(number.getText().toString())+1)+"");
                            }
                            catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
            );

//            view.setLayoutParams(new LinearLayout.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//            LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) view.getLayoutParams();
//            linearParams.height = 300;
//            view.setLayoutParams(linearParams);
            linearLayout.addView(view);
        }

        databaseService.searchTopMessageFromUserRecord("sporttime",100,this.userDataStructList_sportTime);
        linearLayout=(LinearLayout) view_sportTime.findViewById(R.id.userList);
        for(int i=1,len=this.userDataStructList_sportTime.size();i<=len;i++)
        {
            UserDataStruct struct=this.userDataStructList_sportTime.get(i-1);
            View view=inflater.inflate(R.layout.rank_item,null);
            TextView textView_rank=(TextView) view.findViewById(R.id.textView_rank);
            textView_rank.setText(i+"");
            //
            TextView userName=(TextView) view.findViewById(R.id.userName);
            userName.setText(struct.getUserName());
            TextView position=(TextView) view.findViewById(R.id.position);
            position.setText(struct.getPosition());
            TextView signature=(TextView) view.findViewById(R.id.signature);
            signature.setText(struct.getSignature());
            TextView data=(TextView) view.findViewById(R.id.rank_data);
            data.setText(DensityUtil.floatPrecision(1,(float)struct.getSportTime())+" h");
            final TextView number=(TextView) view.findViewById(R.id.number);
            number.setText(struct.getPraiseNumber()+"");

            final ImageView imageView=(ImageView) view.findViewById(R.id.state);
            imageView.setOnClickListener(
                    new View.OnClickListener() {
                        private boolean hasClicked=false;
                        @Override
                        public void onClick(View v) {
                            if(hasClicked)
                                return ;
                            hasClicked=true;
                            try
                            {
                                imageView.setImageDrawable(getResources().getDrawable(R.drawable.like));
                                number.setText((Integer.valueOf(number.getText().toString())+1)+"");
                            }
                            catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
            );

//            view.setLayoutParams(new LinearLayout.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//            LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) view.getLayoutParams();
//            linearParams.height = 300;
//            view.setLayoutParams(linearParams);
            linearLayout.addView(view);
        }
    }

    /**
     * @param type 查询用户数据的时间范围，DateTool.WEEK or DateTool.MONTH
     * @param description 查询用户数据类型 “运动时间”or “击球次数”
     * @return
     */
    private float getUserHitnumberOrSporttime(int type,String description)
    {
        List<Float> floatList=new ArrayList<Float>();
        String colName=description.equals("运动时间")?"sport_time":"hit";
        String currentDate=DateTool.getCurrentDate();
        switch (type)
        {
            case DateTool.WEEK:
                String firstDayOfWeek=DateTool.getFirstDayOfWeek(currentDate);
                String lastDayOfWeek=DateTool.getLastDayOfWeek(currentDate);
                floatList=Main2Activity.databaseService.
                        getFloatDataFromDailyRecordByDate(firstDayOfWeek, lastDayOfWeek,colName);
                if(floatList.size()==0)
                    for(int i=0;i<7;i++)
                        floatList.add(0f);
                break;
            case DateTool.MONTH:
                String firstDayOfMonth=DateTool.getFirstDayOfMonth(currentDate);
                String lastDayOfMonth=DateTool.getLastDayOfMonth(currentDate);
                floatList=Main2Activity.databaseService.
                        getFloatDataFromDailyRecordByDate(firstDayOfMonth, lastDayOfMonth,colName);
                if(floatList.size()==0)
                    for(int i=0;i<DateTool.getDaysOfMonth(
                            DateTool.getDateMessage(currentDate,DateTool.YEAR),DateTool.getDateMessage(currentDate,DateTool.MONTH));i++)
                        floatList.add(0f);
                break;
        }
        float ans=0;
        for(float x:floatList)
            ans+=x;
        return ans;
    }
    private void updateUserRank()
    {
        //更新用户排名
        LayoutInflater inflater=getLayoutInflater();
        TextView textView_rank=(TextView) view3.findViewById(R.id.textView_rank);
        textView_rank.setText(" ");
        //
        TextView userName=(TextView) view3.findViewById(R.id.userName);
        userName.setText(databaseService.getUser_id());
        TextView position=(TextView) view3.findViewById(R.id.position);
        position.setText("北京");
        TextView signature=(TextView) view3.findViewById(R.id.signature);
        signature.setText("APP用户");
        TextView data=(TextView) view3.findViewById(R.id.rank_data);
        String dataText=null;
        if(viewPager_page3.getCurrentItem()==0)
            dataText=DensityUtil.floatPrecision(1,getUserHitnumberOrSporttime(DateTool.WEEK,"运动时间"))+" h";
        else
            dataText=(int)(float)Float.valueOf(DensityUtil.floatPrecision(1,getUserHitnumberOrSporttime(DateTool.WEEK,"击球次数")))+"个";
        data.setText(dataText);
        final TextView number=(TextView) view3.findViewById(R.id.number);
        number.setText(0+"");

        final ImageView imageView=(ImageView) view3.findViewById(R.id.state);
        imageView.setOnClickListener(
                new View.OnClickListener() {
                    private boolean hasClicked=false;
                    @Override
                    public void onClick(View v) {
                        if(hasClicked)
                            return ;
                        hasClicked=true;
                        try
                        {
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.like));
                            number.setText((Integer.valueOf(number.getText().toString())+1)+"");
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
        );
    }
    private void initRankPage()
    {
        scrollerBar_page3=(ScrollerBar) view3.findViewById(R.id.scrollBar_main2) ;
        scrollerBar_page3.setTabNum(2);
        this.textView_rank_sportTime=(TextView) view3.findViewById(R.id.textView_rank_sportTime);
        this.textView_rank_number=(TextView) view3.findViewById(R.id.textView_rank_number);
        this.textView_rankType=(TextView) view3.findViewById(R.id.rankType);

        LayoutInflater inflater=getLayoutInflater();
        view_hitNumber=inflater.inflate(R.layout.main2_page3_content,null);
        view_sportTime=inflater.inflate(R.layout.main2_page3_content,null);

        final List<View> viewList = new ArrayList<View>();
        viewList.add(view_sportTime);
        viewList.add(view_hitNumber);
        PagerAdapter pagerAdapter = new PagerAdapter() {
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
        viewPager_page3=(ViewPager) view3.findViewById(R.id.viewPager_page3) ;
        viewPager_page3.setAdapter(pagerAdapter);
        updateDataInRankPage();
        updateUserRank();
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
        Object tmp[]=Main2Activity.databaseService.getDataFromDailyRecordByDate(DateTool.getCurrentDate());
        if(tmp!=null)
            timerView.updateTextMiddle((int)tmp[2]);
        else
            timerView.updateTextMiddle(0);
        if(textView_TrainingTime!=null)
            textView_TrainingTime.setText(DensityUtil.floatPrecision(2,(float)tmp[5])+" s");
        if(textView_AvgSpeed!=null)
            textView_AvgSpeed.setText(DensityUtil.floatPrecision(2,(float)tmp[4])+" m/s");
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            //Log.d(TAG, "Connect request result=" + result);
        }
        //this.mediaPlayerManager.reStartVideo();
        if(textView_blueTooth==null)
            return ;
//        if (BluetoothLeService.getmConnectionState()==BluetoothLeService.STATE_CONNECTED)
//        {
//            textView_blueTooth.setText("已连接蓝牙设备:" + mDeviceName);
//            mConnected = true;
//        }
//        else
//        {
//            mConnected = false;
//            textView_blueTooth.setText("未连接蓝牙");
//        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(mConnected == true){
            mBluetoothLeService.disconnect();
            unbindService(mServiceConnection);
        }
        if(databaseService!=null)
            databaseService.close();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //this.blueToothItem=menu.findItem(R.id.nav_bluetooth);
        getMenuInflater().inflate(R.menu.menu_main2, menu);
        //this.blueToothItem.setTitle("连接蓝牙");
        //invalidateOptionsMenu();
        this.textView_blueTooth=(TextView)findViewById(R.id.textView_blueTooth);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int i=0; i<permissions.length; i++) {
                    if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        //Toast.makeText(Main2Activity.this, "权限申请成功！"+permissions[i], Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Main2Activity.this, "权限被拒绝： "+permissions[i], Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_bluetooth) {
            if(mConnected == false){
                Intent intent = new Intent();
                intent.setClass(Main2Activity.this,DeviceScanActivity.class);
                startActivityForResult(intent, REQUEST_BULETOOTH);
            }
            else{
                mConnected = false;
                if(mBluetoothLeService!=null)
                {
                    mBluetoothLeService.disconnect();
                    unbindService(mServiceConnection);
                }
                mBluetoothLeService = null;
                textView_blueTooth.setText("未连接蓝牙");
            }
        } else if (id == R.id.nav_data) {
            Intent intent = new Intent();
            intent.setClass(Main2Activity.this, DataStatisticsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_tendency) {
            Intent intent = new Intent();
            intent.setClass(Main2Activity.this, TrainingDataActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    private void updateView()
    {
        float time=0;
        List<Float> floatList=databaseService.getFloatDataFromDailyRecordByDate
                (DateTool.getCurrentDate(),DateTool.getCurrentDate(),"sport_time");
        if(floatList!=null&&floatList.size()!=0)
        {
            time=floatList.get(0);
            textView_TrainingTime.setText(DensityUtil.floatPrecision(2,time)+"s");
        }
        floatList=databaseService.getFloatDataFromDailyRecordByDate
                (DateTool.getCurrentDate(),DateTool.getCurrentDate(),"hit");
//        if(floatList!=null&&floatList.size()!=0)
//        {
//            time=(floatList.get(0)/(time==0?1:time));
//            textView_AvgBallNum.setText(DensityUtil.floatPrecision(2,time)+"个/min");
//            this.timerView.updateTextMiddle((int)((float)floatList.get(0)));
//        }
        floatList=databaseService.getFloatDataFromDailyRecordByDate
                (DateTool.getCurrentDate(),DateTool.getCurrentDate(),"max_speed");
        if(floatList!=null&&floatList.size()!=0)
            textView_AvgSpeed.setText(DensityUtil.floatPrecision(2,floatList.get(0))+"m/s");
    }
    //初始化瀑布式布局,调用此函数前请确保瀑布流布局所在界面已经初始化
    private void initMasonryLayout()
    {
        RecyclerView recyclerView=(RecyclerView)view4.findViewById(R.id.recyclerview);
        System.out.println(recyclerView.getWidth()+" "+recyclerView.getHeight());
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        List<ItemStruct> list=new ArrayList<ItemStruct>();

        ItemStruct struct=new ItemStruct(R.drawable.magazine,"乒乓历史", Color.parseColor("#ea7c38"),1.4f);
        ItemStruct struct1=new ItemStruct(R.drawable.bowling,"乒乓器材",Color.parseColor("#289ad6"));
        ItemStruct struct2=new ItemStruct(R.drawable.internet,"俱乐部",Color.parseColor("#9254a3"),0.7f);
        ItemStruct struct3=new ItemStruct(R.drawable.cinema,"乒乓视频",Color.parseColor("#ec4a42"),1.1f);
        list.add(struct);
        list.add(struct1);
        list.add(struct3);
        list.add(struct2);

        MasonryAdapter adapter=new MasonryAdapter(list,this);
        //设置点击事件
        adapter.setOnItemClickListener(
                new MasonryAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        //position与添加的顺序一致
                        switch (position)
                        {
                            case 0://乒乓历史
                                break;
                            case 1://乒乓器材
                                break;
                            case 2://乒乓视频
                                break;
                            case 3://精彩瞬间
                                Intent intent=new Intent();
                                intent.setClass(Main2Activity.this,WebPageActivity.class);
                                startActivity(intent);
                                break;
                        }
                    }
                }
        );
        recyclerView.setAdapter(adapter);
        SpacesItemDecoration decoration=new SpacesItemDecoration(5);
        recyclerView.addItemDecoration(decoration);
    }
}
