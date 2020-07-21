package com.example.myapplication;

/**
 * Created by 叶明林 on 2017/7/28.
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.support.design.widget.CollapsingToolbarLayout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.io.InputStream;

public class CircularRingPercentageView extends View {
    //总体变量
    private float viewCenterX;
    private float viewCenterY;
    private Paint paint;
    private Paint textPaint;
    private int circleWidth=getDpValue(180);                                         //圆直径
    private int roundBackgroundColor= Color.parseColor("#383838");                 //背景颜色
    private int barColor=Color.parseColor("#FDE3C7");
    private int textColor=0xfde3c799;                                               //字体颜色
    private float textSize= getDpValue(8);                                          //字体大小
    private float roundWidth=getDpValue(5);                                         //环宽度
    private float backGroundWith=circleWidth+getDpValue(7);                       //背景圆直径
    private float progress = 0;                                                     //当前着色数
    private float newProgress =0;
    private float holeRadius=getDpValue(6);
    private int[] colors = {0xffff4639, 0xffCDD513, 0xff3CDF5F};
    private int radius;                                                             //圆环半径
    private static final int gradientRadius=36;                                     //渲染圈半径
    private int out_radius=circleWidth+getDpValue(gradientRadius);
    private RectF oval;
    private RectF oval_out;
    private Paint mPaintText;
    private int maxColorNumber=600;                                                //圆等分数
    private float singlPoint = 6;                                                  //最小角，数值上等于360/maxColorNumber
    private int circleCenter;                                                      //圆心
    private LinearGradient linearGradient;
    private LinearGradient BackgroundLinerGradient;
    private SweepGradient sweepGradient;
    private RadialGradient radialGradient;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    private float outCircleRadius=50;                                               //最外层六个圆的半径
    private float offsetDegrees=0;                                                  //六个圆的角度偏移量
    //文字绘制
    private Rect targetRect_Top = null;
    private Rect targetRect_Middle = null;
    private Rect targetRect_Bottom = null;
    private String textTop="当前时间";
    private String textMiddle="00:00";
    private String textBottom="暂停";
    //计时变量
    private long startTime=0;
    private long saveTime=0;
    private boolean isRunning=false;
    private int minite=0;
    private int second=0;
    private boolean isChanged=true;
    public int timeOfRank = 0;
    //今日击球变量
    private int nowCount=0;
    private int totalCount=1;

    private int []hitTypeCounter=new int[]{0,0,0,0,0,0};
    //更新模式选择
    private int currentMode=-1;
    public final static int TIMER=0;               //计时界面
    public final static int BALLGAME=1;            //颠球界面
    public final static int DAILYTASK=2;           //今日击球界面
    //进度条选择
    private int currentBarMode=0;
    public final static int GRADUAL=0;
    public final static int REAL=1;

    //颜色设置
    private static final int centerCircle_TopColor=Color.parseColor("#111111");            //中心圆顶部颜色262626
    private static final int centerCircle_BottomColor=Color.parseColor("#111111");         //中心园底部颜色262626
    private static final int centerTextColor=Color.parseColor("#FFA770");                  //字体颜色
    private static final int progressBackground_Top=Color.parseColor("#484848");          //进度条上端背景颜色383838
    private static final int progressBackground_Bottom=Color.parseColor("#484848");       //进度条下端背景颜色383838
    private static final int progressColor=Color.parseColor("#FFA770");                    //进度条颜色
    private static final int holeColor=Color.parseColor("#FFA770");                         //进度条前端的圆的颜色
    private static final int gradientIn=Color.parseColor("#FF8247");                                //靠近中心圆内层层的渲染颜色"#262626"
    private static final int gradientOut=Color.parseColor("#383838");                                //靠近中心圆外层的渲染颜色
    private static final int threeCircleColor=Color.parseColor("#33FFA770");                //三环颜色
    private static final int dashColor=Color.parseColor("#CBAB9D");                       //虚线颜色
    private static final int circleColor=Color.parseColor("#DD000000");                            //最外层六个圆的颜色

    public void updateTextMiddle(int value)
    {
        switch (currentMode)
        {
            case BALLGAME:
                this.textMiddle=value+"";
                break;
            case DAILYTASK:
                this.nowCount=(value>totalCount?totalCount:value);
                if(checkIntValid())
                    this.growAllTypeHit(value-Integer.valueOf(textMiddle));
                else
                    this.growAllTypeHit(value);
                this.textMiddle=value+"";
                updateProgress();
                break;
        }
        invalidate();
    }
    private void growAllTypeHit(int val)
    {
        int index=0;
        while(val>0)
        {
            int offset=DensityUtil.getRandomInt(val,1);
            this.hitTypeCounter[index]+=offset;
            val-=offset;
            index=(index+1)%6;
        }
    }

    private boolean checkIntValid()
    {
        boolean ans=true;
        try {
            Integer.valueOf(this.textMiddle);
        }
        catch (Exception e)
        {
            ans=false;
        }
        return ans;
    }

    public void updateTextBottom(int value)
    {
        switch (currentMode)
        {
            case DAILYTASK:
                this.totalCount=value;
                this.textBottom=value+"次";
                updateProgress();
                invalidate();
                break;
        }
    }
    public void updateTop(String text)
    {
        this.textTop=text;
        invalidate();
    }
    private void updateTime(long currentTime)
    {
        if((this.saveTime+currentTime-this.startTime)/1000%60!=second)
        {
            this.minite=(int)(this.saveTime+currentTime-this.startTime)/60000;
            this.second=(int)(this.saveTime+currentTime-this.startTime)/1000%60;
            this.isChanged=true;
        }
        switch (currentMode)
        {
            case BALLGAME:
                this.textBottom=this.getTime()+"/"+(isRunning?"开始":"停止");
                break;
            case TIMER:
                this.textMiddle=this.getTime();
                this.textBottom=(isRunning?"开始":"暂停");
                break;
        }
    }
    public String getTime()
    {
        String time="";
        if(minite>=10)
            time+=minite+":";
        else
            time+="0"+minite+":";
        if(second>=10)
            time+=""+second;
        else
            time+="0"+second;
        return time;
    }
    /*public void setCollapsingToolbarLayout(CollapsingToolbarLayout c)
    {
        this.collapsingToolbarLayout=c;
        this.collapsingToolbarLayout.setExpandedTitleGravity(Gravity.CENTER);
        this.collapsingToolbarLayout.setExpandedTitleMarginStart(Gravity.CENTER);
        //this.collapsingToolbarLayout.setTitle(getTime());
    }

    public int getTimeByInt(){
        if(second == 0)
        {
            if(minite == 1)
                return 1;
            else if(minite == 3)
                return 2;
            else if(minite == 5)
                return 3;
        }
        return 0;
    }*/

    /*public void setMaxColorNumber(int maxColorNumber) {
        this.maxColorNumber = maxColorNumber;
        singlPoint = (float) 360 / (float) maxColorNumber;
        invalidate();
    }*/

    public void setMode(int mode)
    {
        if(currentMode != -1)
            return ;
        currentMode = mode;
        switch (currentMode)
        {
            case TIMER:
                this.textTop="当前时间";
                this.textMiddle="00:00";
                this.textBottom=(isRunning?"开始":"暂停");
                this.currentBarMode= CircularRingPercentageView.GRADUAL;
                break;
            case BALLGAME:
                this.textTop="当前颠球";
                this.textMiddle="0";
                this.textBottom="00:00/停止";
                this.currentBarMode= CircularRingPercentageView.GRADUAL;
                break;
            case DAILYTASK:
                this.textTop="今日击球";
                this.textBottom=this.totalCount+"次";
                this.currentBarMode= CircularRingPercentageView.REAL;
                break;
        }
        invalidate();
    }
    public void sweepGradientInit()
    {
        //渐变颜色
        sweepGradient = new SweepGradient(this.circleWidth / 2, this.circleWidth / 2, colors, null);
        //旋转 不然是从0度开始渐变
        Matrix matrix = new Matrix();
        matrix.setRotate(-90, this.circleWidth / 2, this.circleWidth / 2);
        sweepGradient.setLocalMatrix(matrix);
    }
    public CircularRingPercentageView(Context context) {
        this(context, null);
    }

    public CircularRingPercentageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public CircularRingPercentageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initView();
    }

    /**
     * 刻度字体颜色
     *
     * @param textColor
     */
    public void setTextColor(int textColor) {
        this.textColor = textColor;
        mPaintText.setColor(textColor);
        invalidate();
    }

    private int getDpValue(int w) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, w, getContext().getResources().getDisplayMetrics());
    }

    /**
     * 圆环宽度
     *
     * @param roundWidth 宽度
     */
    public void setRoundWidth(float roundWidth) {
        this.roundWidth = roundWidth;
        if (roundWidth > circleCenter) {
            this.roundWidth = circleCenter;
        }
        radius = (int) (circleCenter - this.roundWidth / 2); // 圆环的半径
        oval.left = circleCenter - radius;
        oval.right = circleCenter + radius;
        oval.bottom = circleCenter + radius;
        oval.top = circleCenter - radius;
        paint.setStrokeWidth(this.roundWidth);
        invalidate();
    }

    public void initView()
    {
        circleCenter = circleWidth / 2;//半径
        singlPoint = (float) 360 / (float) maxColorNumber;
        radius = (int) (circleCenter - roundWidth / 2); // 圆环的半径
        /*mPaintText = new Paint();
        mPaintText.setColor(textColor);
        mPaintText.setTextAlign(Paint.Align.CENTER);
        mPaintText.setTextSize(textSize);
        mPaintText.setAntiAlias(true);*/

        paint = new Paint(this.barColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(roundWidth);
        paint.setAntiAlias(true);
        this.linearGradient=new LinearGradient
                (this.circleCenter,0,this.circleCenter,
                        this.circleCenter+radius,this.centerCircle_TopColor,
                        this.centerCircle_BottomColor, Shader.TileMode.MIRROR);
        textPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(this.centerTextColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //View中心坐标
        if(this.viewCenterX==0||viewCenterY==0)
        {
            viewCenterX=(float)getWidth()/2;
            viewCenterY=(float)getHeight()/2;
            targetRect_Top = new Rect((int)(viewCenterX-getDpValue(30)), (int)(viewCenterY-radius/2-getDpValue(10)), (int)(viewCenterX+getDpValue(30)), (int)(viewCenterY-radius/2+getDpValue(10)));
            targetRect_Middle = new Rect((int)(viewCenterX-radius), (int)(viewCenterY-getDpValue(30)), (int)(viewCenterX+radius), (int)(viewCenterY+getDpValue(40)));
            targetRect_Bottom = new Rect((int)(viewCenterX-getDpValue(30)), (int)(viewCenterY+getDpValue(60)), (int)(viewCenterX+getDpValue(30)), (int)(viewCenterY+getDpValue(70)));
            this.BackgroundLinerGradient=new LinearGradient
                    (this.viewCenterX,this.viewCenterY-radius,this.viewCenterX,
                            this.viewCenterY+radius,this.progressBackground_Top,
                            this.progressBackground_Bottom, Shader.TileMode.MIRROR);
        }
        if(this.currentMode == DAILYTASK)
        {
            //环外的一层渐变
            if(oval_out==null)
            {
                oval_out = new RectF(10,10,10,10);
                this.radialGradient = new RadialGradient(this.viewCenterX,this.viewCenterY,out_radius*0.61f,gradientIn,gradientOut, Shader.TileMode.MIRROR);
            }
            paint.setStrokeWidth(2*(out_radius-circleWidth));//out_radius/2
            paint.setShader(this.radialGradient);
            canvas.drawCircle(viewCenterX,viewCenterY,this.circleWidth/2,paint);
            paint.setShader(null);
            paint.setStrokeWidth(roundWidth);
        }

        //画背景圆
        Paint myPaint=new Paint();
        myPaint.setShader(this.linearGradient);
        canvas.drawCircle(viewCenterX,viewCenterY,this.backGroundWith/2,myPaint);
        if(oval==null)
            oval = new RectF(viewCenterX - radius, viewCenterY - radius,
                    viewCenterX + radius, viewCenterY + radius);
        //进度条背景
        paint.setShader(this.BackgroundLinerGradient);
        canvas.drawArc(oval, -90, 360, false, paint);
        paint.setShader(null);

        if(this.currentMode == DAILYTASK)
        {
            //三圈
            paint.setColor(threeCircleColor);
            paint.setStrokeWidth(2.5f);
            final int unit = getDpValue(gradientRadius) / 3;
            for (int i = 1; i <= 3; i++) {
                RectF oval = new RectF(viewCenterX - radius - unit * i, viewCenterY - radius - unit * i,
                        viewCenterX + radius + unit * i, viewCenterY + radius + unit * i);
                canvas.drawArc(oval, -90, 360, false, paint);
            }
            paint.setStrokeWidth(roundWidth);

            //虚线
            paint.setColor(dashColor);
            paint.setStrokeWidth(7f);
            paint.setPathEffect(new DashPathEffect(new float[]{25, 25}, 0));
            RectF oval_temp = new RectF(viewCenterX - radius - unit * 5, viewCenterY - radius - unit * 5,
                    viewCenterX + radius + unit * 5, viewCenterY + radius + unit * 5);
            canvas.drawArc(oval_temp, -90, 360, false, paint);
            paint.setPathEffect(null);
            paint.setStrokeWidth(roundWidth);

            //六个圆圈
            //TODO
            //图片变量
            final Resources resources = this.getResources();
            final int imageSrc[] = {
                    R.drawable.forehand_ji,
                    R.drawable.backhand_ji,
                    R.drawable.forehand_cuo,
                    R.drawable.backhand_cuo,
                    R.drawable.forehand_fa,
                    R.drawable.backhand_fa
            };
            paint.setColor(circleColor);
            paint.setStrokeWidth(2 * outCircleRadius);
            Point centerPoint = new Point(viewCenterX, viewCenterY, 0);
            Point point = new Point(viewCenterX, viewCenterY - radius - unit * 5, 0);
            for (int i = 0; i < 6; i++) {
                canvas.drawCircle(point.x, point.y, outCircleRadius, paint);
                InputStream inputStream = resources.openRawResource(imageSrc[i]);
                canvas.drawBitmap(BitmapFactory.decodeStream(inputStream), null,
                        new Rect((int) (point.x - 2 * outCircleRadius), (int) (point.y - 2 * outCircleRadius),
                                (int) (point.x + outCircleRadius), (int) (point.y + 2 * outCircleRadius)), paint);//

                Rect targetRect = new Rect((int) (point.x), (int) (point.y - 2 * outCircleRadius),
                        (int) (point.x + 2 * outCircleRadius), (int) (point.y + 2 * outCircleRadius));
                textPaint.setStrokeWidth(3);
                textPaint.setTextSize(radius / 3);
                Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
                int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;

                String data=String.valueOf(this.hitTypeCounter[i]);
                canvas.drawText(data, targetRect.centerX(), baseline, textPaint);

                point = DensityUtil.pointRote(point, centerPoint, offsetDegrees + 60);
            }
            paint.setStrokeWidth(roundWidth);
        }

        switch (this.currentBarMode)
        {
            case GRADUAL:
                //画进度条
                paint.setColor(this.progressColor);
                for(int i=(int)(progress-255*2)>=0?(int)(progress-255*2):0;i<=(int)progress;i++)
                {
                    paint.setAlpha((int)((2*255-progress+i)/2));//(int)progress-i>255?0:255-(int)progress+i
                    canvas.drawArc(oval, -90+(i*singlPoint),singlPoint, false, paint);
                }
                break;
            case REAL:
                //进度条背景
                //canvas.drawArc(oval, -90, (float) (-(maxColorNumber - progress%maxColorNumber) * singlPoint), false, paint);
                //画进度条
                paint.setColor(this.progressColor);
                canvas.drawArc(oval, -90, (float) (progress%maxColorNumber * singlPoint), false, paint);
                break;
        }
        //更新progress
        updateProgress();
        //画圆点
        //TODO
        Paint paint1=new Paint();
        paint1.setColor(this.holeColor);
        float holeCenterX=(float)(viewCenterX+radius*Math.sin(progress*singlPoint*Math.PI/180));
        float holeCenterY=(float)(viewCenterY-radius*Math.cos(progress*singlPoint*Math.PI/180));
        canvas.drawCircle(holeCenterX,holeCenterY,this.holeRadius,paint1);
        //中间信息显示部分
        //绘制文字刻度
        textPaint.setStrokeWidth(3);
        textPaint.setTextSize(radius/6);
        //paint.setColor(Color.CYAN);
        //canvas.drawRect(targetRect, paint);
        Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
        int baseline = (targetRect_Top.bottom + targetRect_Top.top - fontMetrics.bottom - fontMetrics.top) / 2;
        canvas.drawText(textTop, targetRect_Top.centerX(), baseline, textPaint);

//        Paint paint2=new Paint();
//        paint.setColor(Color.CYAN);
//        canvas.drawRect(targetRect_Middle, paint2);
        textPaint.setStrokeWidth(3);
        textPaint.setTextSize(2*radius/3);
        fontMetrics = textPaint.getFontMetricsInt();
        baseline = (targetRect_Middle.bottom + targetRect_Middle.top - fontMetrics.bottom - fontMetrics.top) / 2;
        canvas.drawText(textMiddle, targetRect_Middle.centerX(), baseline, textPaint);

        if(currentMode==TIMER)
            textBottom=(isRunning?"开始":"暂停");
        else if(currentMode == BALLGAME)
            textBottom=getTime()+"/"+(isRunning?"开始":"停止");

        textPaint.setStrokeWidth(3);
        textPaint.setTextSize(radius/6);
        fontMetrics = textPaint.getFontMetricsInt();
        baseline = (targetRect_Bottom.bottom + targetRect_Bottom.top - fontMetrics.bottom - fontMetrics.top) / 2;
        canvas.drawText(textBottom, targetRect_Bottom.centerX(), baseline, textPaint);

        textPaint.setStrokeWidth(1);
        canvas.drawLine(viewCenterX-radius/2, viewCenterY+getDpValue(55),viewCenterX+radius/2,viewCenterY+getDpValue(55),textPaint);
    }
    private void updateProgress()
    {
        switch (this.currentMode)
        {
            case TIMER:
            case BALLGAME:
                if(isRunning)
                {
                    long currenTime=System.currentTimeMillis();
                    if(startTime==0)
                        startTime=currenTime;
                    this.progress=(float)((this.saveTime+currenTime-this.startTime)/100);
                    this.updateTime(currenTime);
                    if(isChanged)
                    {
                        //timeOfRank = getTimeByInt();
                        //this.collapsingToolbarLayout.setTitle(getTime());
                        isChanged=false;
                    }
                    invalidate();
                }
                break;
            case DAILYTASK:
                if(progress<maxColorNumber*this.nowCount/this.totalCount-0.001f)
                {
                    long currenTime=System.currentTimeMillis();
                    if(startTime==0)
                        startTime=currenTime;
                    this.progress+=(float)((this.saveTime+currenTime-this.startTime)/100);
                    invalidate();
                }
                else if(progress>maxColorNumber*this.nowCount/this.totalCount)
                {
                    this.progress=maxColorNumber*this.nowCount/this.totalCount-0.0005f;
                    invalidate();
                }
                else
                {
                    this.startTime=0;
                    this.saveTime=0;
                }
                break;
        }
    }
    public void start()
    {
        if(!this.isRunning)
        {
            long keep=System.currentTimeMillis();
            if(this.startTime!=0)
                this.saveTime+=keep-this.startTime;
            this.startTime=keep;
            this.isRunning=true;
            invalidate();
        }
    }
    public void pause()
    {
        if(this.isRunning)
        {
            this.isRunning=false;
            this.saveTime+=System.currentTimeMillis()-this.startTime;
            this.startTime=0;
            invalidate();
        }
    }
    public void reset()
    {
        this.pause();
        this.startTime=0;
        this.saveTime=0;
        this.progress=0;
        invalidate();
    }
    //TODO test
    public boolean getState()
    {
        return this.isRunning;
    }
}
