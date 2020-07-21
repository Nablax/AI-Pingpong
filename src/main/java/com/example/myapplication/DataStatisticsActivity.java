package com.example.myapplication;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.RadarChart;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DataStatisticsActivity extends AppCompatActivity {
    private BarChartManager barChartManager_contest;
    private RadarChartManager radarChartManager_contest;
    private RadarChartManager radarChartManager_daily;
    private TextView chartTitleForContest;
    private final int CONTEST=0;

    private TextView textView_time;
    private TextView textView_energy;

    private void addListener()
    {
        this.chartTitleForContest.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String []keep={};
                        Intent intent=new Intent();
                        intent.putExtra("paths",keep);
                        intent.setClass(DataStatisticsActivity.this,ComboBoxActivity.class);
                        startActivityForResult(intent,CONTEST);
                    }
                }
        );
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        setContentView(R.layout.datastatisticsactivity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        //选项卡
        TabHost tabHost=(TabHost)findViewById(R.id.tabhost);
        tabHost.setup();
        TabRender.TabHostRender(new int[]{R.id.tab1, R.id.tab2},
                new int[]{R.drawable.contest,R.drawable.contest_click,
                        R.drawable.dayreport,R.drawable.dayreport_click},tabHost,this);
        try
        {
            BarChart barChart=(BarChart)findViewById(R.id.barChart_contest);
            barChartManager_contest=new BarChartManager(barChart);
            List<Float> floatList=new ArrayList<Float>();
            Random random=new Random();
            for(int i=1;i<=19;i++)
                floatList.add(random.nextFloat()*50);
            barChartManager_contest.setData(floatList);

            PieChart pieChart=(PieChart)findViewById(R.id.pieChart_contest);
            Map<String ,Float> map=new HashMap<String,Float>();
            Object tmp[]=Main2Activity.databaseService.getDataFromDailyRecordByDate(DateTool.getCurrentDate());
            if(tmp!=null)
            {
                float rateOfZheng=DensityUtil.getRandomFloat((float)(int)tmp[2],0);
                map.put("正手攻球",rateOfZheng);
                map.put("反手攻球",(float)(int)tmp[2]-rateOfZheng);
            }
            else
            {
                map.put("正手攻球",0.8f);
                map.put("反手攻球",0.2f);
            }

            PieChartManager.setPieChart(pieChart,map,"击球次数",true);


            RadarChart radarChart=(RadarChart)findViewById(R.id.radarChart_contest);
            radarChartManager_contest=new RadarChartManager(radarChart);
            floatList.clear();
            for(int i=1;i<=6;i++)
                floatList.add(random.nextFloat()*50);
            radarChartManager_contest.setData(floatList);

            PieChart pieChart_daily=(PieChart)findViewById(R.id.pieChart_daily);
            Map<String ,Float> map_daily=new HashMap<String,Float>();
            if(tmp!=null)
            {
                float rateOfZheng=DensityUtil.getRandomFloat((float)(int)tmp[2],0);
                map_daily.put("正手攻球",rateOfZheng);
                map_daily.put("反手攻球",(float)(int)tmp[2]-rateOfZheng);
            }
            else
            {
                map_daily.put("正手攻球",0.8f);
                map_daily.put("反手攻球",0.2f);
            }
            PieChartManager.setPieChart(pieChart_daily,map_daily,"击球次数",true);

            RadarChart radarChart_daily=(RadarChart)findViewById(R.id.radarChart_daily);
            radarChartManager_daily=new RadarChartManager(radarChart_daily);
            floatList.clear();
            for(int i=1;i<=6;i++)
                floatList.add(random.nextFloat()*50);
            radarChartManager_daily.setData(floatList);

            Object data[]=Main2Activity.databaseService.getDataFromDailyRecordByDate(DateTool.getCurrentDate());
            this.textView_time=(TextView)findViewById(R.id.textView_TrainingTime);
            this.textView_time.setText(DensityUtil.floatPrecision(2,(float)data[5])+" s");

            this.textView_energy=(TextView)findViewById(R.id.textView_energy);
            this.textView_energy.setText(DensityUtil.floatPrecision(2,0.1f*(float)data[5])+" Cal");

            TextView textView = (TextView)findViewById(R.id.textView_averhit) ;
            textView.setText(DensityUtil.getRandomInt(30,10)+" 个/min");

            textView = (TextView)findViewById(R.id.textView_maxhit) ;
            textView.setText(DensityUtil.getRandomInt(80,40)+" 个/min");

            this.chartTitleForContest=(TextView) findViewById(R.id.textView_chartTitle1);
            addListener();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    //右上角的按钮
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK)
        {
            switch (requestCode)
            {
                case CONTEST:
                    String path= Environment.getExternalStorageDirectory()
                            + File.separator +"ballGame"+ File.separator+"Vedio"
                            +File.separator+data.getExtras().getString("String");
                    this.updateDataForContest(path);
                    break;
            }
        }
    }
    private void updateDataForContest(String path)
    {
        //更新顶部柱状图
        ReadDataFromFile readDataFromFile=new ReadDataFromFile(path+File.separator+"data.txt",false);

    }
}
