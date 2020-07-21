package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Created by 叶明林 on 2017/8/22.
 */
interface GameTaskCallBack
{
    //任务结束时调用
    public void taskFinish(int correctNumber,int totalNumber,boolean hasFallen);
    //难度改变时调用
    public void onDifficultChange(float difficulty);
}
public abstract class BallGameTask {
    protected Timer timer;
    protected int correctNumber=0;
    protected int totalNumber=0;
    protected int score=0;            // 任务颠一个球得分
    protected long gameTime=-1;     //任务持续时间
    protected int hitNumber=-1;       //指定击球个数
    protected List<Integer> task=new ArrayList<Integer>();    //任务列表
    protected GameTaskCallBack callBack;
    protected boolean hasFallen =false;                //当前任务成功或者失败
    public void setCallBack(GameTaskCallBack callBack){this.callBack=callBack;}
    public abstract boolean acceptAction(int hand);      //接受玩家动作
    protected abstract void setGameTime();
    protected abstract void setHitNumber();           //根据难度生成颠球次数
    protected abstract void setScore();             //根据难度生成颠球得分
    protected abstract void getPoint();             //得分
    protected void initAttrs()
    {
        setGameTime();
        setScore();
        setHitNumber();
    }
}
