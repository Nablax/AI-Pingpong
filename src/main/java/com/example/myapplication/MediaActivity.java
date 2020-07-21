package com.example.myapplication;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MediaActivity extends AppCompatActivity {
    private class ActionStruct
    {
        private long startTime,endTime;
        private float speed,force;
        private String hitType;
        private boolean ifHit;
    }
    private final int CHOOSEFILE=0;
    private final int RECORDING = 2;
    private final String VEDIONAME="real.mp4";
    private boolean shouldStart=true;
    private RelativeLayout rl;
    private int currentPosition=0;
    private boolean isSurfaceViewCreated=false;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    private SeekBar seekBar;
    private ImageView rePlay;
    private ImageView lastFile;
    private ImageView ivPauseOrStart;
    private ImageView nextFile;
    private LinearLayout llController;
    private TextView tvTime;
    private TextView tvTimeMax;
    private TextView textView_speed;
    private TextView textView_force;
    private TextView textView_type;
    private TextView textView_ifhit;
    private Button button_vedio;
    private Button button_choose;
    private Button button_lastAction;
    private Button button_nextAction;

    private MediaPlayer player;

    private final int CONTROLLERHEIGHT=60;
    private final String DEMO_PATH = Environment.getExternalStorageDirectory()
            + File.separator +"ballGame"+ File.separator+ "demo";

    private String videoPath = null;
    private List<MediaEntity> mList;

    private final static int SINGLE_MEDIA = 0;
    private final static int MULTI_MEDIA = 1;
    private int currentPlayType = SINGLE_MEDIA;
    private int currentMedia = 0;
    private final int REAL=0;
    private final int DEMO=1;
    private final int RECORD = 2;
    private int currentChoose=REAL;

    private int screenWidth;
    private int screenHeight;
    private int videoWidth;
    private int videoHeight;
    private boolean isPlaying = false;

    private List<String> fileName=new ArrayList<String>();
    private List<ActionStruct> actionStructList=new ArrayList<ActionStruct>();
    private String lastFilePath=null;
    private String nextFilePath=null;
    private int mediaFileIndex =0;
    private int actionIndex =0;
    private boolean actionEnd=false;
    private String rootPath;

    private Handler mHandler = new Handler();
    private final static int UPDATA_TIME_CODE = 1;
    private Handler mProHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == UPDATA_TIME_CODE){
                int current=player.getCurrentPosition();
                tvTime.setText( getPlayPro(current) );
                tvTimeMax.setText( getPlayPro(player.getDuration()-current) );
                //System.out.println(actionEnd+" "+MediaActivity.this.actionStructList.size());
                if(actionEnd||MediaActivity.this.actionStructList.size()==0)
                    return ;
                long begin=MediaActivity.this.actionStructList.get(MediaActivity.this.actionIndex).startTime;
                long end=MediaActivity.this.actionStructList.get(MediaActivity.this.actionIndex).endTime;
                System.out.println(current+" "+begin+" "+end+" "+actionIndex+" "
                +actionStructList.get(MediaActivity.this.actionIndex).force+" "
                +actionStructList.get(MediaActivity.this.actionIndex).speed);
                if(current>=begin&&current<end)
                {
                    System.out.println("in");
                    MediaActivity.this.textView_speed.setText(
                            ""+DensityUtil.floatPrecision(
                                    3,MediaActivity.this.actionStructList.get(MediaActivity.this.actionIndex).speed)+" m/s");
                    MediaActivity.this.textView_force.setText(
                            ""+DensityUtil.floatPrecision(
                                    3,MediaActivity.this.actionStructList.get(MediaActivity.this.actionIndex).force)+" N");
                    MediaActivity.this.textView_type.setText(
                            MediaActivity.this.actionStructList.get(MediaActivity.this.actionIndex).hitType);
                    if(MediaActivity.this.actionStructList.get(MediaActivity.this.actionIndex).ifHit)
                        MediaActivity.this.textView_ifhit.setText("是");
                    else
                        MediaActivity.this.textView_ifhit.setText("否");
                }
                else if(current>end)
                {
                    if(MediaActivity.this.actionStructList.get(MediaActivity.this.actionIndex).ifHit)
                        MediaActivity.this.textView_ifhit.setText("是");
                    else
                        MediaActivity.this.textView_ifhit.setText("否");
                    if(MediaActivity.this.actionIndex==MediaActivity.this.actionStructList.size()-1)
                        actionEnd=true;
                    else
                        MediaActivity.this.actionIndex++;
                }
            }
        }
    };

    private Timer timer;
    private boolean isShowController = true;
    private void changeIndexOfAction(long progress)
    {
        if(this.actionStructList.size()==0)
            return ;
        for(int i=0,len=this.actionStructList.size();i<len;i++)
        {
            //System.out.println(progress+" "+this.actionStructList.get(i).startTime+" "+this.actionStructList.get(i).endTime);
            if(progress>=this.actionStructList.get(i).startTime
                    &&progress<=this.actionStructList.get(i).endTime)
            {
                this.actionIndex=i;
                break;
            }
        }
        if(this.actionIndex==this.actionStructList.size()-1)
        {
            if(progress<this.actionStructList.get(0).startTime)
            {
                actionIndex=0;
                actionEnd=false;
            }
            else
                actionEnd=true;
        }
        else
            actionEnd=false;
        //System.out.println(this.actionIndex+" "+(this.actionStructList.size()-1)+" "+actionEnd);
    }
    private void addOnclickListener()
    {
        this.button_vedio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentChoose = RECORD;
                Intent intent = new Intent();
                intent.setClass(MediaActivity.this,PlayBack.class);
                startActivityForResult(intent, RECORDING);
            }
        });
        this.button_choose.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentChoose=REAL;
                        Intent intent = new Intent();
                        ExDialog.Mode=ExDialog.SPECIALTYPE;
                        intent.putExtra("title","选择视频");
                        intent.putExtra("rootdir",rootPath);
                        intent.putExtra("type","mp4");
                        intent.setClass(MediaActivity.this, ExDialog.class);
                        startActivityForResult(intent, CHOOSEFILE);
                    }
                }
        );
        this.button_lastAction.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(actionIndex!=0&&actionIndex<actionStructList.size())
                        {
                            actionIndex--;
                            long begin=MediaActivity.this.actionStructList.get(actionIndex).startTime;
                            player.seekTo((int)begin);
                        }
                    }
                }
        );
        this.button_nextAction.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!actionEnd&&actionIndex<actionStructList.size())
                        {
                            actionIndex++;
                            if(actionIndex==MediaActivity.this.actionStructList.size()-1)
                                actionEnd=true;
                            long begin=MediaActivity.this.actionStructList.get(actionIndex).startTime;
                            player.seekTo((int)begin);
                        }
                    }
                }
        );
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CHOOSEFILE) {
                try
                {
                    ///storage/emulated/0/ballGame/data/REAL.mp4
                    String filePath=data.getExtras().getString("path");
                    if (filePath==null)
                        return ;
                    File file=new File(filePath);
                    String name=file.getName();
                    for(int i=0,len=this.fileName.size();i<len;i++)
                        if(this.fileName.get(i).equals(name))
                            this.mediaFileIndex =i;
                    this.videoPath =filePath;
                    if(this.fileName.size()!=0&&this.fileName.size()!=1)
                    {
                        int size=this.fileName.size();
                        this.lastFilePath=this.rootPath+File.separator+this.fileName.get((this.mediaFileIndex -1+size)%size);
                        this.nextFilePath=this.rootPath+File.separator+this.fileName.get((this.mediaFileIndex +1)%size);
                    }
                    else
                    {
                        this.lastFilePath=null;
                        this.nextFilePath=null;
                    }
                    if(lastFilePath==null)
                        lastFile.setVisibility(View.INVISIBLE);
                    else
                        lastFile.setVisibility(View.VISIBLE);
                    if(nextFilePath==null)
                        nextFile.setVisibility(View.INVISIBLE);
                    else
                        nextFile.setVisibility(View.VISIBLE);
                    changeVideo();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }

            }
            else if(requestCode == RECORDING)
            {
                initFileList(rootPath);
                int size=fileName.size();
                videoPath =this.rootPath+File.separator+fileName.get(size-1);
                mediaFileIndex =size-1;
                changeVideo();
            }
        }
    }
    private void changeVideo()
    {
        try
        {
            player.release();
            //if(player!=null&&player.isPlaying())
            //player.pause();
            player=new MediaPlayer();
            player.setDataSource(videoPath);
            player.prepare();
            player.setDisplay(this.surfaceHolder);
            this.currentPosition=0;
            this.seekBar.setMax(player.getDuration());
            this.seekBar.setProgress(0);
            if(this.fileName.size()!=0&&this.fileName.size()!=1)
            {
                int size=this.fileName.size();
                this.lastFilePath=this.rootPath+File.separator+this.fileName.get((this.mediaFileIndex -1+size)%size);
                this.nextFilePath=this.rootPath+File.separator+this.fileName.get((this.mediaFileIndex +1)%size);
            }
            else
            {
                this.lastFilePath=null;
                this.nextFilePath=null;
            }
            if(lastFilePath==null)
                lastFile.setVisibility(View.INVISIBLE);
            else
                lastFile.setVisibility(View.VISIBLE);
            if(nextFilePath==null)
                nextFile.setVisibility(View.INVISIBLE);
            else
                nextFile.setVisibility(View.VISIBLE);
            if(shouldStart)
            {
                isPlaying=true;
                player.start();
            }
            updateDataList();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }
    private void initFileList(String rootPath)
    //rootPath=Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"ballGame"+File.separator+"Collection";
    {
        try
        {
            File f = new File(rootPath);
            List files = Arrays.asList(f.listFiles());
            if (files != null) {
                for (int i = 0; i < files.size(); i++) {
                    File file=((File)(files.get(i)));
                    if (!file.isDirectory())
                    {
                        String postfix=DensityUtil.getFilePostfix(file.getName());
                        if(postfix!=null&&postfix.equals("mp4"))
                            this.fileName.add(file.getName());
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        setContentView(R.layout.activity_media);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_media);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.main_back_color_deepdark));
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

        this.rootPath = getIntent().getStringExtra("path");
        File file=new File(this.rootPath);
        if(!file.exists())
            file.mkdir();
        initFileList(rootPath);
        if(this.fileName.size()==0)
            this.videoPath=null;
        else
            this.videoPath=this.rootPath+File.separator+this.fileName.get(0);
        if (videoPath == null){
            currentPlayType = MULTI_MEDIA;
            mList = (List<MediaEntity>) getIntent().getSerializableExtra("list");
        }

        //获得手机分辨率
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        rl = (RelativeLayout) findViewById(R.id.relativeLayout1);
        surfaceView = (SurfaceView) findViewById(R.id.surface);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        Drawable drawable = DensityUtil.scaleImage(this,R.drawable.seekbar,30,30,DensityUtil.DP);
        seekBar.setThumb(drawable);
        rePlay=(ImageView) findViewById(R.id.replay);
        rePlay.setImageDrawable(DensityUtil.scaleImage(this,R.drawable.playagain,60,60,DensityUtil.DP));
        lastFile=(ImageView) findViewById(R.id.lastfile);
        lastFile.setImageDrawable(DensityUtil.scaleImage(this,R.drawable.lastfile,60,60,DensityUtil.DP));
        ivPauseOrStart = (ImageView) findViewById(R.id.iv_pause_or_start);
        ivPauseOrStart.setImageDrawable(DensityUtil.scaleImage(this,R.drawable.video_pause,100,100,DensityUtil.DP));
        nextFile=(ImageView) findViewById(R.id.nextfile);
        nextFile.setImageDrawable(DensityUtil.scaleImage(this,R.drawable.nextfile,60,60,DensityUtil.DP));
        llController = (LinearLayout) findViewById(R.id.ll_control);
        tvTime = (TextView) findViewById(R.id.tv_time);
        tvTimeMax = (TextView) findViewById(R.id.tv_time_max);
        textView_speed=(TextView)findViewById(R.id.textView_speed);
        textView_force=(TextView)findViewById(R.id.textView_force);
        textView_type=(TextView)findViewById(R.id.textView_type);
        textView_ifhit=(TextView)findViewById(R.id.textView_ifhit);
        button_lastAction=(Button)findViewById(R.id.lastAction);
        button_nextAction=(Button)findViewById(R.id.nextAction);

        if(this.fileName.size()!=0&&this.fileName.size()!=1)
        {
            int size=this.fileName.size();
            this.lastFilePath=this.rootPath+File.separator+this.fileName.get((size-1+size)%size);
            this.nextFilePath=this.rootPath+File.separator+this.fileName.get((size+1)%size);
        }
        else
        {
            this.lastFilePath=null;
            this.nextFilePath=null;
        }

        if(lastFilePath==null)
            lastFile.setVisibility(View.INVISIBLE);
        if(nextFilePath==null)
            nextFile.setVisibility(View.INVISIBLE);

//        surfaceView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//
//                if (surfaceView.getWidth() > 0) {
//                    surfaceWidth = surfaceView.getWidth();
//                }
//
//            }
//        });

        ivPauseOrStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    currentPosition=player.getCurrentPosition();
                    player.pause();
                    ivPauseOrStart.setImageResource(R.drawable.video_start);
                    isPlaying = false;
                    shouldStart=false;
                } else {
                    player.seekTo(currentPosition);
                    player.start();
                    seekBar.setMax(player.getDuration());
                    ivPauseOrStart.setImageResource(R.drawable.video_pause);
                    isPlaying = true;
                    shouldStart=true;
                }
            }
        });

        rePlay.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeIndexOfAction(0);
                        MediaActivity.this.player.seekTo(0);
                        MediaActivity.this.player.start();
                        shouldStart=true;
                    }
                }
        );

        lastFile.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(lastFilePath!=null)
                        {
                            int size=fileName.size();
                            videoPath =lastFilePath;
                            mediaFileIndex =(mediaFileIndex -1+size)%size;
                            changeVideo();
                        }
                    }
                }
        );
        nextFile.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(nextFilePath!=null)
                        {
                            int size=fileName.size();
                            videoPath =nextFilePath;
                            mediaFileIndex =(mediaFileIndex +1)%size;
                            changeVideo();
                        }
                    }
                }
        );

        //显示或隐藏进度条
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowController = !isShowController;
                if (isShowController) {
                    startAnimShow();
                }else{
                    startAnimHide();
                }
            }
        });

        button_vedio=(Button) findViewById(R.id.startrecording);
        button_choose=(Button)findViewById(R.id.data);
        Drawable startRecording = getResources().getDrawable(R.drawable.startrecording);
        Rect rectTmp = new Rect(60, 0, 160, 100);
        IconCompress.IconDealing(rectTmp);
        startRecording.setBounds(rectTmp);
        button_vedio.setCompoundDrawables(startRecording, null, null, null);

        Drawable choose = getResources().getDrawable(R.drawable.choosefile);
        choose.setBounds(rectTmp);
        button_choose.setCompoundDrawables(choose, null, null, null);

        addOnclickListener();
        startPro();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
    /**
     * 连播
     */
    private Runnable mediaThread = new Runnable() {
        @Override
        public void run() {
            preparePlayer(mList.get(currentMedia).filePath);
        }
    };

    /**
     * update progress
     */
    private void startPro(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //System.out.println("timer running");
                try
                {
                    if(player==null)
                        return ;
                    int currentPosition1 = player.getCurrentPosition();
                    //update vedio progress
                    //seekBar.setMax(player.getDuration());
                    //System.out.println("startPro "+currentPosition1+" "+seekBar.getMax());
                    seekBar.setProgress(currentPosition1);
                    //update vedio time
                    mProHandler.removeMessages(UPDATA_TIME_CODE);
                    mProHandler.sendEmptyMessage(UPDATA_TIME_CODE);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }, 100, 100);

    }

    /**
     * pause update progress
     */
    private void pausePro(){
        //System.out.println("cancel timer");
        if(timer!=null)
            timer.cancel();
    }

    private void startPlayer()
    {
        player.seekTo(currentPosition);
        player.start();
        seekBar.setMax(player.getDuration());
        isPlaying = true;
        //max time
        tvTimeMax.setText(getPlayPro(player.getDuration()-player.getCurrentPosition()));

    }

    private void preparePlayer(String path){
        try {
            player.reset();
            player.setDataSource(path);

            player.prepareAsync();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    videoWidth = player.getVideoWidth();
                    videoHeight = player.getVideoHeight();
                    if(shouldStart)
                        startPlayer();
                }
            });

            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                    isPlaying = false;


//                    if (currentPlayType == MULTI_MEDIA) {
//
//                        currentMedia++;
//
//                        if (mList != null && currentMedia >= mList.size()) {
//                            mHandler.removeCallbacks(mediaThread);
//                            MediaActivity.this.finish();
//                            return;
//                        }
//
//                        mHandler.removeCallbacks(mediaThread);
//                        mHandler.post(mediaThread);
//                        return;
//                    }
//
//                    MediaActivity.this.finish();

                }
            });

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    player.seekTo(seekBar.getProgress());
                    int current=player.getCurrentPosition();
                    changeIndexOfAction(seekBar.getProgress());
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getPlayPro(long pro) {
        pro+=500;
        DateFormat format = new SimpleDateFormat("mm:ss");
        return format.format(new Date(pro));
    }

    private void startAnimHide(){
        ObjectAnimator.ofFloat(llController, View.TRANSLATION_Y,
                0, DensityUtil.dip2px(MediaActivity.this, this.CONTROLLERHEIGHT))
                .setDuration(200).start();
    }
    private void startAnimShow(){
        ObjectAnimator.ofFloat(llController, View.TRANSLATION_Y,
                DensityUtil.dip2px(MediaActivity.this, this.CONTROLLERHEIGHT), 0)
                .setDuration(200).start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
//            duration = player.getDuration();
            full(true);
        }


        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//            duration = player.getDuration();
            full(false);
        }

        int temp = screenWidth;
        screenWidth = screenHeight;
        screenHeight = temp;

        //setSurficeViewSize();

//        Log.e(TAG, "duration---->>" + duration);
        super.onConfigurationChanged(newConfig);
    }

    private void setSurficeViewSize(){

        if ((float) videoWidth / videoHeight >= (float) screenWidth / screenHeight) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
            layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = (int) ((float) screenWidth * videoHeight / videoWidth);
            surfaceView.setLayoutParams(layoutParams);
        } else {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
            layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            layoutParams.width = (int) ((float) screenHeight * videoWidth / videoHeight);
            surfaceView.setLayoutParams(layoutParams);
        }
    }

    /**
     * 控制状态栏 隐藏（全屏）/显示
     * @param enable
     */
    private void full(boolean enable) {
        if (enable) {

            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {

            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        isPlaying = false;
        currentPosition=player.getCurrentPosition();
        player.pause();
        //player.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isPlaying = false;
        pausePro();
        player.release();
    }
    @Override
    protected void onResume(){
        super.onResume();
        if (surfaceHolder == null)
        {
            surfaceHolder = surfaceView.getHolder();

            //当surface被显示时保持屏幕打开状态
            surfaceHolder.setKeepScreenOn(true);
            surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    //System.out.println("callBack");
                    isSurfaceViewCreated=true;
                    player = new MediaPlayer();
                    player.setOnErrorListener(
                            new MediaPlayer.OnErrorListener() {
                                @Override
                                public boolean onError(MediaPlayer mp, int what, int extra) {
                                    return false;
                                }
                            }
                    );
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    player.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
//                }
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    player.setDisplay(surfaceHolder);

                    player.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                        @Override
                        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

//                        if ((float)width / height >= (float)screenWidth / screenHeight) {
//                            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
//                            layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
//                            layoutParams.height = (int) ((float) screenWidth * height/ width);
//                            surfaceView.setLayoutParams(layoutParams);
//                        }else{
//                            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
//                            layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
//                            layoutParams.width = (int) ((float) screenHeight * width/ height);
//                            surfaceView.setLayoutParams(layoutParams);
//                        }
                        }
                    });

                    if (videoPath == null && (mList == null || mList.size() <= 0)){
                        return;
                    }

                    if (videoPath != null) {
                        preparePlayer(videoPath);

                    }else if (mList != null && mList.size() > 0){
                        mHandler.post(mediaThread);
                    }
                }
                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//                Log.e(TAG, "width--->>" + width + ",height--->>" + height);
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    isSurfaceViewCreated=false;
                }
            });
        }
        else if(isSurfaceViewCreated)
        {
            try
            {
                if(shouldStart)
                {
                    isPlaying = true;
                    player.seekTo(currentPosition);
                    player.start();
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        initFileList(rootPath);
        updateDataList();
    }
    private void updateDataList()
    {
        if(this.mediaFileIndex>=this.fileName.size())
            return ;
        try{
            String filePath=this.rootPath+File.separator+
                    DensityUtil.getFileNameWithoutPostfix(this.fileName.get(this.mediaFileIndex))+".txt";
            this.actionStructList.clear();
            //TODO 从文件中读取数据
            ReadDataFromFile readDataFromFile=new ReadDataFromFile(filePath,false);
            while(!readDataFromFile.endFlag)
            {
                String str=readDataFromFile.nextLineData();
                if(str==null||str.length()==0)
                    continue;
                String datas[]=str.split(",");
                if(datas.length<=2)
                    continue;
                ActionStruct actionStruct=new ActionStruct();
                long startTime=Long.valueOf(datas[datas.length-2]);
                long endTime=Long.valueOf(datas[datas.length-1]);
                float speed=0,force=0;
                int counter=datas.length/15;
                for(int i=0,len=datas.length-2;i<len;i+=15)
                {
                    speed+=Double.valueOf(datas[i+3]);
                    force+=Double.valueOf(datas[i+4]);
                }
                speed/=counter;
                force=force*0.185f/counter;
                boolean ifHit=false;
                if(Double.valueOf(datas[5])>0.000001)
                    ifHit=true;
                actionStruct.force=Math.abs(force);
                actionStruct.speed=Math.abs(speed);
                actionStruct.startTime=startTime;
                actionStruct.endTime=endTime;
                actionStruct.ifHit=ifHit;
                actionStruct.hitType="----";
                System.out.println("message: "+speed+" "+force+" "+startTime+" "+endTime+" "+ifHit);
                this.actionStructList.add(actionStruct);
            }
            this.actionIndex=0;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
