package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.io.File;

public class ModeChoose extends Activity {

    private Button btnBallGame;
    private Button btnFight;
    private Button btnPlayBack;
    private RelativeLayout relativeLayout;
    private String DeviceName;
    private String DeviceAddress;

    public void myListener(){
        btnBallGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ModeChoose.this, BallGameMainActivity.class);
                intent.putExtra("DeviceName", DeviceName);
                intent.putExtra("DeviceAddress", DeviceAddress);
                startActivity(intent);
                finish();
            }
        });
        btnFight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ModeChoose.this, FightActivity.class);
                intent.putExtra("DeviceName", DeviceName);
                intent.putExtra("DeviceAddress", DeviceAddress);
                startActivity(intent);
                finish();
            }
        });
        btnPlayBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ModeChoose.this, MediaActivity.class);
                intent.putExtra("path", Environment.getExternalStorageDirectory().getAbsolutePath()
                        + File.separator+"ballGame"+File.separator+"Collection");
                startActivity(intent);
                finish();
            }
        });
        relativeLayout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_choose);

        Intent intent = getIntent();
        DeviceName = intent.getStringExtra("DeviceName");
        DeviceAddress = intent.getStringExtra("DeviceAddress");

        relativeLayout =(RelativeLayout)findViewById(R.id.relativelayout_mc);

        {
            btnBallGame = (Button) findViewById(R.id.btnBallGame);
            btnFight = (Button) findViewById(R.id.btnFight);
            btnPlayBack = (Button) findViewById(R.id.btnPlayBack);
            Drawable BALLGAME = getResources().getDrawable(R.drawable.ballgame);
            Drawable FIGHT = getResources().getDrawable(R.drawable.fight);
            Drawable PLAYBACK = getResources().getDrawable(R.drawable.playback);
            Rect rectTmp = new Rect(60, 0, 160, 100);
            IconCompress.IconDealing(rectTmp);
            FIGHT.setBounds(rectTmp);
            PLAYBACK.setBounds(rectTmp);
            rectTmp = new Rect(70, 0, 120, 50);
            IconCompress.IconDealing(rectTmp);
            BALLGAME.setBounds(rectTmp);

            btnBallGame.setCompoundDrawables(BALLGAME, null, null, null);
            btnFight.setCompoundDrawables(FIGHT, null, null, null);
            btnPlayBack.setCompoundDrawables(PLAYBACK, null, null, null);
        }

        myListener();
    }
}
