package com.example.myapplication;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class GameResultActivity extends AppCompatActivity {

    private Toolbar toolbar_GameOver;
    private Button btnBackToGame;
    private TextView textView_GameHitBall;
    private TextView textView_GameTime;
    private TextView textView_GameAccuracy;
    private Drawable BACKGAME;
    public void init(){
        toolbar_GameOver = (Toolbar)findViewById(R.id.toolbar_GameOver);
        btnBackToGame = (Button)findViewById(R.id.btnBackToGame);
        textView_GameHitBall = (TextView)findViewById(R.id.textView_GameHitBall);
        textView_GameTime = (TextView)findViewById(R.id.textView_GameTime);
        textView_GameAccuracy = (TextView)findViewById(R.id.textView_GameAccuracy);
        BACKGAME = getResources().getDrawable(R.drawable.playback);
        BACKGAME.setBounds(60, 0, 160, 100);
        btnBackToGame.setCompoundDrawables(BACKGAME,null,null,null);
        btnBackToGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(GameResultActivity.this, BallGameMainActivity.class);
                intent.putExtra("GameMode", "GAME");
                startActivity(intent);
                finish();
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        setContentView(R.layout.activity_game_result);
        init();
    }
}
