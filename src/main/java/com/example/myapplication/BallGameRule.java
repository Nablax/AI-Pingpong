package com.example.myapplication;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;

import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 叶明林 on 2017/8/22.
 */

public class BallGameRule extends Thread {
    private float difficulty=0;                      //难度 0-100
    private int totalCorrect=0;                       //正确数
    private int totalHit=0;                           //总颠球数
    private int totalScore=0;                        //得分
    private final float minCorrectRate=0.5f;        //最低准确率
    private boolean isRunning=false;                 //是否在运行
    private boolean isDifficultyLocked;             //是否锁定难度
    private final int kindsOfTask=4;                //任务总类
    private int currentTask=-1;
    //任务类型
    private final int FOREHAND=0;
    private final int BACKHAND=1;
    private final int SPECIALHAND=2;
    private final int ARBITRARILYHAND=3;
    private BallGameTask ballGameTask;
    //数据分析
    private BasicAnalysis basicAnalysis=new BasicAnalysis();
    //音频相关
    private SoundPool soundPool;
    private final int soundNumber=5;          //音频数量
    private float volume=0.3f;                 //音量
    private String audio_fore;                  //音频-正
    private String audio_back;                  //音频-反
    //更新UI对象
    private Handler uiHandler;
    public BallGameRule(Context context,Handler handler)
    {
        this.uiHandler=handler;
        //三个参数分别为支持的声音数量、声音类型、声音品质
        soundPool=new SoundPool(soundNumber, AudioManager.STREAM_SYSTEM,5);
        //soundPool.load(context,R.raw.ccc,1);//第三个参数为优先级
        //参数含义依次为音频放入soundpool的顺序、左右声道的音量(0~1)、优先级，是否循环（0为不循环，-1微循环）
        //、播放比率一般为1，表示正常播放
        //soundPool.play(1,1, 1, 0, 0, 1);
    }
    public boolean getLockSate()
    {
        return this.isDifficultyLocked;
    }
    public void changeDifficultyLocked()
    {
        this.isDifficultyLocked=!this.isDifficultyLocked;
    }
    public void setDifficulty(float value)
    {
        this.difficulty=value;
    }
    public void setVolume(float value)
    {
        if(value>1||value<0)
            return;
        this.volume=value;
    }
    private class ForeHandTask extends BallGameTask
    {
        public ForeHandTask()
        {
            initAttrs();
            //speakText("keep up");
            timer=new Timer();
            timer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            callBack.taskFinish(correctNumber,totalNumber,hasFallen);
                        }
                    },gameTime
            );
        }
        @Override
        public boolean acceptAction(int hand) {
            totalNumber++;
            if(hand==1)
            {
                correctNumber++;
                getPoint();
                return true;
            }
            else if(hand==0)
            {
                timer.cancel();
                hasFallen=true;
                callBack.taskFinish(correctNumber,totalNumber,hasFallen);
            }
            //speakText("hehe");
            return false;
        }
        @Override
        protected void setGameTime() {
            Random random =new Random();
            gameTime=4000+random.nextInt(3000);
        }
        @Override
        protected void setHitNumber() {
            hitNumber=-1;
        }
        @Override
        protected void setScore() {
            score=(int)(1*(1+difficulty/90));
        }
        @Override
        protected void getPoint() {
            totalScore+=score;
            if(!isDifficultyLocked&&((int)(totalScore/5)!=(int)((totalScore-score)/5)))
            {
                difficulty+=1;
                callBack.onDifficultChange(difficulty);
                setScore();
            }
        }
    }
    private class BackHandTask extends BallGameTask
    {
        public BackHandTask()
        {
            initAttrs();
            //speakText("down");
            timer=new Timer();
            timer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            callBack.taskFinish(correctNumber,totalNumber,hasFallen);
                        }
                    },gameTime
            );
        }
        @Override
        public boolean acceptAction(int hand) {
            totalNumber++;
            if(hand==2)
            {
                correctNumber++;
                getPoint();
                return true;
            }
            else if(hand==0)
            {
                timer.cancel();
                hasFallen=true;
                callBack.taskFinish(correctNumber,totalNumber,hasFallen);
            }
            //speakText("hehe");
            return false;
        }
        @Override
        protected void setGameTime() {
            Random random =new Random();
            gameTime=4000+random.nextInt(3000);
            setHitNumber();
            setScore();
        }
        @Override
        protected void setHitNumber() {
            hitNumber=-1;
        }

        @Override
        protected void setScore() {
            score=(int)(1*(1+difficulty/90));
        }
        @Override
        protected void getPoint() {
            totalScore+=score;
            if(!isDifficultyLocked&&((int)(totalScore/5)!=(int)((totalScore-score)/5)))
            {
                difficulty+=1;
                callBack.onDifficultChange(difficulty);
                setScore();
            }
        }
    }
    private class ArbitrarilyHandTask extends BallGameTask
    {
        public ArbitrarilyHandTask(long time)
        {
            gameTime=time;
            initAttrs();
            //speakText("as you wish");
            timer=new Timer();
            timer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            callBack.taskFinish(correctNumber,totalNumber,hasFallen);
                        }
                    },gameTime
            );
        }
        @Override
        public boolean acceptAction(int hand) {
            totalNumber++;
            if(hand==0)
            {
                timer.cancel();
                hasFallen=true;
                callBack.taskFinish(correctNumber,totalNumber,hasFallen);
            }
            else
            {
                correctNumber++;
                getPoint();
            }
            return true;
        }
        @Override
        protected void setGameTime() {}
        @Override
        protected void setHitNumber() {}
        @Override
        protected void setScore() {
            score=1;
        }
        @Override
        protected void getPoint() {
            totalScore+=score;
            if(!isDifficultyLocked&&((int)(totalScore/5)!=(int)((totalScore-score)/5)))
            {
                difficulty+=1;
                callBack.onDifficultChange(difficulty);
            }
        }
    }
    private class SpecialHandTask extends BallGameTask
    {
        private int taskIndex=0;
        public SpecialHandTask()
        {
            initAttrs();
            Random random=new Random();
            String order="";
            for(int i=0;i<hitNumber;i++)
            {
                int keep=1+random.nextInt(2);
                task.add(keep);
                order+=keep==1?"up ":"down ";
            }
            //speakText(order);
            timer=new Timer();
            timer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            callBack.taskFinish(correctNumber,totalNumber,hasFallen);
                        }
                    },gameTime
            );
        }
        @Override
        public boolean acceptAction(int hand) {
            totalNumber++;
            if(task.get(taskIndex)==hand)
            {
                taskIndex++;
                correctNumber++;
                totalScore+=score;
                if(taskIndex==task.size())
                {
                    timer.cancel();
                    callBack.taskFinish(correctNumber,totalNumber,hasFallen);
                }
                return true;
            }
            timer.cancel();
            //speakText("hehe");
            if(hand==0)
                hasFallen =true;
            callBack.taskFinish(correctNumber,totalNumber,hasFallen);
            return false;
        }
        @Override
        protected void setGameTime() {
            gameTime=5000;
        }
        @Override
        protected void setHitNumber() {
            if(difficulty<=30)
                hitNumber=3;
            else if(difficulty>=90)
                hitNumber=8;
            else
                hitNumber=(int)(difficulty/12);
        }
        @Override
        protected void setScore() {
            score=(int)(1*(1+difficulty/25));
        }
        @Override
        protected void getPoint() {
            totalScore+=score;
            if(!isDifficultyLocked&&((int)(totalScore/5)!=(int)((totalScore-score)/5)))
            {
                difficulty+=1;
                callBack.onDifficultChange(difficulty);
                setScore();
            }
        }
    }
    @Override
    public void run() {
        isRunning=true;
        nextTask();
        super.run();
    }
    private void gameEnd()
    {
        isRunning=false;
        //speakText("total score "+totalScore);
    }
    public void playerAction(int hand)
    {
        this.ballGameTask.acceptAction(hand);
        updateUI();
    }
    private void updateUI()
    {
        Message message=new Message();
        Bundle bundle=message.getData();
        bundle.putString("UPDATETYPE","VALUE");
        bundle.putString("UPDATERATE",((float)totalCorrect/totalHit)+"");
        bundle.putString("UPDATESCORE",totalScore+"");
        uiHandler.sendMessage(message);
    }
    private void updateDifficulty()
    {
        Message message=new Message();
        Bundle bundle=message.getData();
        bundle.putString("UPDATETYPE","DIFFICULTY");
        bundle.putString("UPDATEDIFFICULTY",difficulty+"");
        uiHandler.sendMessage(message);
    }
    private void nextTask()
    {
        //防止和上一个任务相同
        Random random=new Random();
        int order=random.nextInt(kindsOfTask);
        while(order==currentTask)
            order=random.nextInt(kindsOfTask);
        currentTask=order;
        switch (order)
        {
            case FOREHAND:
                ballGameTask =new ForeHandTask();
                ballGameTask.setCallBack(
                        new GameTaskCallBack() {
                            @Override
                            public void taskFinish(int corretNumber,int totalNumber,boolean hasFallen) {
                                totalHit+=totalNumber;
                                totalCorrect+=corretNumber;
                                if((float)totalCorrect/totalHit<minCorrectRate||hasFallen)
                                    gameEnd();
                                else
                                    nextTask();
                            }
                            @Override
                            public void onDifficultChange(float difficulty) {
                                updateDifficulty();
                            }
                        }
                );
                break;
            case BACKHAND:
                ballGameTask =new BackHandTask();
                ballGameTask.setCallBack(
                        new GameTaskCallBack() {
                            @Override
                            public void taskFinish(int corretNumber,int totalNumber,boolean hasFallen) {
                                totalHit+=totalNumber;
                                totalCorrect+=corretNumber;
                                if((float)totalCorrect/totalHit<minCorrectRate||hasFallen)
                                    gameEnd();
                                else
                                    nextTask();
                            }

                            @Override
                            public void onDifficultChange(float difficulty) {
                                updateDifficulty();
                            }
                        }
                );
                break;
            case SPECIALHAND:
                ballGameTask =new SpecialHandTask();
                ballGameTask.setCallBack(
                        new GameTaskCallBack() {
                            @Override
                            public void taskFinish(int corretNumber,int totalNumber,boolean hasFallen) {
                                totalHit+=totalNumber;
                                totalCorrect+=corretNumber;
                                if((float)totalCorrect/totalHit<minCorrectRate||hasFallen)
                                    gameEnd();
                                else
                                    nextTask();
                            }
                            @Override
                            public void onDifficultChange(float difficulty) {
                                updateDifficulty();
                            }
                        }
                );
                break;
            case ARBITRARILYHAND:
            ballGameTask =new ArbitrarilyHandTask(random.nextInt(4000)+5000);
            ballGameTask.setCallBack(
                    new GameTaskCallBack() {
                        @Override
                        public void taskFinish(int corretNumber,int totalNumber,boolean hasFallen) {
                            totalHit+=totalNumber;
                            totalCorrect+=corretNumber;
                            if(hasFallen)
                                gameEnd();
                            else
                                nextTask();
                        }
                        @Override
                        public void onDifficultChange(float difficulty) {
                            updateDifficulty();
                        }
                    }
            );
            break;
        }
    }
}