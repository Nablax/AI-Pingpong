package com.example.myapplication;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by 叶明林 on 2017/8/24.
 */
interface RendererCallBack
{
    void onDataReset();
    void onDataInitComplete();
    void onDrawComplete();
}
public class OpenGLRenderer implements GLSurfaceView.Renderer {
    private RendererCallBack rendererCallBack;
    private List<Double> doubles=new ArrayList<Double>();
    private List<Point> points =Collections.synchronizedList(new ArrayList<Point>());
    private float[] lines;
    private Ball startPoint=new Ball(1,0.84f,0),endPoint=new Ball(1,0,0);
    private Map<String,Integer> offsetMap=new HashMap<String,Integer>();
    private final int attrsNumber=15;
    private final float distancePerSingle=70;   //旋转一度所需移动距离
    public static final int GRADIENT_SPEED=0;
    public static final int GRADIENT_FORCE=1;
    private int mode=GRADIENT_SPEED;
    //球拍模型相关
    private final String modelFileName="huba.stl";
    private Model batModel;
    private Point mCenterPoint;
    private Point eye = new Point(0, 0, -3);
    private Point up = new Point(0, 1, 0);
    private Point center = new Point(0, 0, 0);
    private Point offset=new Point(-0.2f,0,0);
    private float mScalef = 0.5f;
    private float mDegree = 90;
    //运动
    private int timeFade=0;                //消逝时间，以1/30 s为单位
    private final float speed=0.01f;     // 每1/30秒的前进距离
    public void changeMode(int mode)
    {
        this.mode=mode;
    }
    final String rootPath= Environment.getExternalStorageDirectory().getAbsolutePath()+
            File.separator+"ballGame"+File.separator;
    public OpenGLRenderer(Context context,@NonNull RendererCallBack rendererCallBack)
    {
        this.rendererCallBack = rendererCallBack;

        offsetMap.put("px",0);
        offsetMap.put("roll",1);
        offsetMap.put("py",2);
        offsetMap.put("pitch",3);
        offsetMap.put("pz",4);
        offsetMap.put("yaw",5);
        offsetMap.put("v",6);
        offsetMap.put("a",7);
        offsetMap.put("w",8);
        offsetMap.put("wx",9);
        offsetMap.put("wy",10);
        offsetMap.put("wz",11);
        offsetMap.put("ax",12);
        offsetMap.put("ay",13);
        offsetMap.put("az",14);

        try
        {
            //batModel =new STLReader().parserBinStlInAssets(context,modelFileName);
            batModel = new STLReader().parserAscStlInAssets(rootPath + "pingpongbat.stl");
        }
        catch (Exception e)
        {
            System.out.println("缺失模型文件");
            e.printStackTrace();
        }
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glShadeModel(GL10.GL_SMOOTH);
        int color[]=DensityUtil.colorToRGBFromInt(0x4b4b4b);
        gl.glClearColor((float)color[0]/255, (float)color[1]/255, (float)color[2]/255, 0f);
        lines=generateGridLines();

        float r = batModel.getR();
        mScalef = 1f / r;          //球拍防缩
        mCenterPoint = batModel.getCentrePoint();
    }
    public void setDoubles(List<Double> doubles)
    {
        this.timeFade=0;
        this.doubles.clear();
        for(double x:doubles)
            this.doubles.add(x);
        if(this.doubles!=null&&this.doubles.size()!=0)
            generateLinesByData();
        this.rendererCallBack.onDataInitComplete();
    }
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0,0,width,height);
        float ratio = (float)width/(float)height;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        //gl.glFrustumf(-2,2,-ratio,ratio,3,7);
        gl.glFrustumf(-1, 1, -ratio, ratio, 2, 10);
    }

    public void scaling(float rate)
    {
        if(rate<1)
        {
            this.eyeX*=0.993;
            this.eyeY*=0.993;
            this.eyeZ*=0.993;
        }
        else if(rate>1)
        {
            this.eyeX/=0.993;
            this.eyeY/=0.993;
            this.eyeZ/=0.993;
        }
    }
    public void moveCameraUpDown(float rate)
    {
        if(rate>0)
            eyeZ+=0.2;
        else
            eyeZ-=0.2;
    }
    public void moveCameraRotation(float rate)
    {
        float newEyeX=(float)(eyeX*Math.cos(-rate*Math.PI/180/distancePerSingle)-eyeY*Math.sin(-rate*Math.PI/180/distancePerSingle));
        float newEyeY=(float)(eyeX*Math.sin(-rate*Math.PI/180/distancePerSingle)+eyeY*Math.cos(-rate*Math.PI/180/distancePerSingle));
        eyeX=newEyeX;
        eyeY=newEyeY;
    }
    private float eyeX=0.00736001f;
    private float eyeY=3.1229703f;//-5
    private float eyeZ=2.1117926f;

    private float testx,testy,testz;
    @Override
    public void onDrawFrame(GL10 gl) {
        //System.out.println("onDraw "+BluetoothLeService.getmConnectionState());
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        GLU.gluLookAt(gl, eyeX,eyeY ,eyeZ, 0, 0, 0, 0,0,1);//gl, 0, 0, 5, 0, 0, 0, 0,1,0
        gl.glFinish();

//        gl.glColor4f(1f, 1.0f, 1.0f, 1.0f);
//        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
//        gl.glLineWidth(20);
//        gl.glVertexPointer(3,GL10.GL_FLOAT,0,BufferUtil.floatToBuffer(lines));
//        gl.glLineWidth(5);
//        gl.glDrawArrays(GL10.GL_LINES,0,lines.length/3);
//        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
//        gl.glFinish();

        PingPongTable pingPongTable = new PingPongTable();
        pingPongTable.draw(gl);

        float[] que={
                0,0,0,
                0,0,0.2f,
                -0.2f,0,0,
                0.2f,0,0,
                0,0.2f,0,
                0,-0.2f,0
        } ;
        //gl.glColor4f((float)0x7C/255, (float)0xfc/255, (float)0x00/255, 1.0f);
        gl.glColor4f((float)1, (float)1, (float)1, 1.0f);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3,GL10.GL_FLOAT,0,BufferUtil.floatToBuffer(que));
        gl.glLineWidth(5);
        gl.glDrawArrays(GL10.GL_LINES,0,que.length/3);
        //运动轨迹
        float max_mark=Float.MIN_VALUE;
        synchronized (this.points)
        {
            if(this.points!=null&&this.points.size()!=0)//this.doubles!=null&&this.doubles.size()!=0&&
            {
                for(Point point:points)
                    if(point.mark>max_mark)
                        max_mark=point.mark;
                for(int i=0,len=points.size()-1;i<len;i++)
                {
                    int j=0;
                    float[] ans=new float[6];
                    Point tmp=null,tmpNext=null;
                    tmp=points.get(i);
                    tmpNext=points.get(i+1);
                    ans[j++]=tmp.x;
                    ans[j++]=tmp.y;
                    ans[j++]=tmp.z;
                    ans[j++]=tmpNext.x;
                    ans[j++]=tmpNext.y;
                    ans[j++]=tmpNext.z;
                    float colos[]=new float[8];
                    colos[0]=tmp.mark/(max_mark==0?tmp.mark:max_mark);
                    colos[1]=0;
                    colos[2]=0;
                    colos[3]=1;
                    colos[4]=tmpNext.mark/(max_mark==0?tmpNext.mark:max_mark);
                    colos[5]=0;
                    colos[6]=0;
                    colos[7]=1;
                    //gl.glColor4f(1f, 0f, 0f, 1.0f);
                    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
                    gl.glColorPointer(4,GL10.GL_FLOAT,0,BufferUtil.floatToBuffer(colos));
                    gl.glVertexPointer(3,GL10.GL_FLOAT,0,BufferUtil.floatToBuffer(ans));
                    gl.glLineWidth(10);
                    gl.glDrawArrays(GL10.GL_LINES,0,ans.length/3);
                    gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
                }
            }

            this.timeFade++;
            if(this.points.size()==0)
                return ;
            if(timeFade>=this.points.size())
                timeFade=this.points.size()-1;
            //反转
            int fact=this.points.size()-1-timeFade;
            Point position=this.points.get(fact);

            gl.glTranslatef(position.x, position.y,
                    position.z);
            //将模型放缩到View刚好装下
            gl.glScalef(mScalef, mScalef, mScalef);
            gl.glRotatef((float)(position.pitch*180/Math.PI), 1, 0, 0);//x 俯仰角，y横向角,z 航向角
            gl.glRotatef((float)(position.roll*180/Math.PI), 0, 1, 0);
            gl.glRotatef((float)(position.yaw*180/Math.PI), 0, 0, 1);
            //System.out.println(fact+" "+(float)(position.pitch*180/Math.PI)+" "+(float)(position.roll*180/Math.PI)+" "+(float)(position.yaw*180/Math.PI));
        }
        //===================begin==============================//
        //允许给每个顶点设置法向量
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
        // 允许设置顶点
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        // 允许设置颜色
        //设置法向量数据源
        gl.glNormalPointer(GL10.GL_FLOAT, 0, batModel.getVnormBuffer());
        // 设置三角形顶点数据源
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, batModel.getVertBuffer());
        // 绘制三角形
        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, batModel.getFacetCount() * 3);
        // 取消顶点设置
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        //取消法向量设置
        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
        //=====================end============================//
//        try
//        {
//            Thread.sleep(10000);
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }

    }
    private float[] generateGridLines()
    {
        final float interval=0.2f;
        final float begin=-4;
        List<Float> floatList=new ArrayList<Float>();
        //生成横向网格线
        for(int i=0;i<(int)(2*(-begin)/interval);i++)
        {
            floatList.add(begin);
            floatList.add(begin+i*interval);
            floatList.add(0f);
            floatList.add(-begin);
            floatList.add(begin+i*interval);
            floatList.add(0f);
        }
        //生成竖向网格线
        for(int i=0;i<(int)(2*(-begin)/interval);i++)
        {
            floatList.add(begin+i*interval);
            floatList.add(begin);
            floatList.add(0f);
            floatList.add(begin+i*interval);
            floatList.add(-begin);
            floatList.add(0f);
        }
        float []ans=new float[floatList.size()];
        for(int i=0;i<floatList.size();i++)
            ans[i]=(float)floatList.get(i);
        return ans;
    }
    private void generateLinesByData()
    {
        this.points.clear();
        int offSet=0;
        List<Double> doubleList=this.doubles;
        if(doubleList.size()%15!=0||doubleList.size()<15)
            return;
        float x_min=Float.MAX_VALUE,
                x_max=Float.MIN_VALUE,
                y_min=Float.MAX_VALUE,
                y_max=Float.MIN_VALUE,
                z_min=Float.MAX_VALUE,
                z_max=Float.MIN_VALUE;
        Point point=null;
        for(int i=0,j=0;i<doubleList.size();i+=attrsNumber,j++)
        {
            point=new Point();
            point.x=(float)((double)doubleList.get(i+offsetMap.get("px")));
            if(point.x<x_min)
                x_min=point.x;
            else if(point.x>x_max)
                x_max=point.x;
            point.y=(float)((double)doubleList.get(i+offsetMap.get("py")));
            if(point.y<y_min)
                y_min=point.y;
            else if(point.y>y_max)
                y_max=point.y;
            point.z=(float)((double)doubleList.get(i+offsetMap.get("pz")));
            if(point.z<z_min)
                z_min=point.z;
            else if(point.z>z_max)
                z_max=point.z;
            point.pitch=(float)((double)doubleList.get(i+offsetMap.get("pitch")));
            point.roll=(float)((double)doubleList.get(i+offsetMap.get("roll")));
            point.yaw=(float)((double)doubleList.get(i+offsetMap.get("yaw")));
            if(mode==GRADIENT_SPEED)
            {
//                double vx=(double)doubleList.get(i+offsetMap.get("vx"));
//                double vy=(double)doubleList.get(i+offsetMap.get("vy"));
//                double vz=(double)doubleList.get(i+offsetMap.get("vz"));
//                points[j].mark=(float)Math.sqrt(vx*vx+vy+vy+vz*vz);
                point.mark=(float)(double)doubleList.get(i+offsetMap.get("v"));
            }
            else if(mode==GRADIENT_FORCE)
            {
                double ax=(double)doubleList.get(i+offsetMap.get("ax"));
                double ay=(double)doubleList.get(i+offsetMap.get("ay"));
                double az=(double)doubleList.get(i+offsetMap.get("az"));
                point.mark=(float)Math.sqrt(ax*ax+ay+ay+az*az);
            }
            this.points.add(point);
        }
        float rate_x=(x_max-x_min)/4;
        float rate_y=(y_max-y_min)/4;
        float rate_z=(z_max-z_min)/2;
        float rate_max=(rate_x>rate_y?rate_x:rate_y);
        rate_max=(rate_max>rate_z?rate_max:rate_z);
        x_max/=rate_max;
        x_min/=rate_max;
        y_max/=rate_max;
        y_min/=rate_max;
        z_max/=rate_max;
        z_min/=rate_max;
//        float offset_x=0-(x_max+x_min)/2;
//        float offset_y=-2-(y_max+y_min)/2;
//        float offset_z=0-z_min;
        float offset_x=0-point.x/rate_max;
        float offset_y=0-point.y/rate_max;
        float offset_z=0-point.z/rate_max;
        for (int i=0,len=this.points.size();i<len;i++)
        {
            Point tmp=this.points.get(i);
            tmp.x=tmp.x/rate_max+offset_x;
            tmp.y=tmp.y/rate_max+offset_y;
            tmp.z=tmp.z/rate_max+offset_z;
            //System.out.println("form "+tmp.x+" "+tmp.y+" "+tmp.z);
        }
    }
//    private class Point
//    {
//        public float x,y,z;
//        public float mark;
//    }
    private class Ball
    {
        private int slices = 36; //越大越圆滑
        private int stacks = 24; //同↑
//        private FloatBuffer[] normalsBuffers;
        private FloatBuffer[] slicesBuffers= new FloatBuffer[slices];
        private float yAngle;
        private float zAngle;

        private float center_x,center_y,center_z;
        private float color_r,color_b,color_g;
        private boolean hadChangedCneter=false;
        public void setCenter(float x,float y,float z)
        {
            if(this.center_x==x&&this.center_y==y&&this.center_z==z)
                return ;
            this.center_x=x;
            this.center_y=y;
            this.center_z=z;
            hadChangedCneter=true;
        }
        public Ball(float r,float b,float g)
        {
            this.color_b=b;
            this.color_g=g;
            this.color_r=r;
        }

        float radius = 0.04f;
        private void initBall()
        {
            if(!hadChangedCneter)
                return ;
            //            normalsBuffers = new FloatBuffer[slices];
            for (int i = 0; i < slices; i++) {

                float[] vertexCoords = new float[6 * (stacks + 1)];
                float[] normalCoords = new float[6 * (stacks + 1)];

                double alpha0 = i * (2 * Math.PI) / slices;
                double alpha1 = (i + 1) * (2 * Math.PI) / slices;

                float cosAlpha0 = (float) Math.cos(alpha0);
                float sinAlpha0 = (float) Math.sin(alpha0);
                float cosAlpha1 = (float) Math.cos(alpha1);
                float sinAlpha1 = (float) Math.sin(alpha1);

                for (int j = 0; j <= stacks; j++)
                {
                    double beta = j * Math.PI / stacks - Math.PI / 2;

                    float cosBeta = (float) Math.cos(beta);
                    float sinBeta = (float) Math.sin(beta);


                    setXYZ(vertexCoords, 6 * j,
                            radius * cosBeta * cosAlpha1,
                            radius * sinBeta,
                            radius * cosBeta * sinAlpha1);
                    setXYZ(vertexCoords, 6 * j + 3,
                            radius * cosBeta * cosAlpha0,
                            radius * sinBeta,
                            radius * cosBeta * sinAlpha0);
//                    setXYZ(normalCoords, 6 * j,
//                            cosBeta * cosAlpha1,
//                            sinBeta,
//                            cosBeta * sinAlpha1);
//                    setXYZ(normalCoords, 6 * j + 3,
//                            cosBeta * cosAlpha0,
//                            sinBeta,
//                            cosBeta * sinAlpha0);
                }
                slicesBuffers[i] = BufferUtil.floatToBuffer(vertexCoords);
//                normalsBuffers[i] = BufferUtil.floatToBuffer(normalCoords);
            }
        }

        public void setXYZ(float[] vector, int offset, float x, float y, float z) {
            vector[offset] = center_x+x;
            vector[offset + 1] = center_y+y;
            vector[offset + 2] = center_z+z;
        }

        public void draw(GL10 gl)
        {
            if(gl ==null)
                return ;
            initBall();
//            gl.glLoadIdentity();
//            gl.glTranslatef(0.0f, 0.0f, -7.0f);
            gl.glColor4f(color_r, color_b, color_g, 1.0f);
            gl.glRotatef(yAngle, 1.0f, 0.0f, 0.0f);
            gl.glRotatef(zAngle, 0.0f, 1.0f, 0.0f);

            for (int i = 0; i < slices; i++) {
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, slicesBuffers[i]);
                gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 2 * (stacks + 1));

            }
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        }

        public void setyAngle(float yAngle)
        {
            this.yAngle = yAngle;
        }
        public void setzAngle(float zAngle)
        {
            this.zAngle = zAngle;
        }
    }

    private class PingPongTable
    {
        private final float length=2.74f;
        private final float width=1.525f;
        private final float height=0.76f;
        private float center_x,center_y,center_z;
        private final float[] VERTEX = {   // in counterclockwise order:
                width/2,length/2,height,
                -width/2,length/2,height,
                -width/2,-length/2,height,
                width/2,length/2,height,
                -width/2,-length/2,height,
                width/2,-length/2,height
//                1, 1, 0,   // top right
//                -1, 1, 0,  // top left
//                -1, -1, 0, // bottom left
//                1, 1, 0,   // top right
//                -1, -1, 0, // bottom left
//                1, -1, 0,  // bottom right
        };
        private final float []tableLines={
                width/2,length/2,height,
                -width/2,length/2,height,
                -width/2,-length/2,height,
                width/2,-length/2,height,
                width/2,length/2,height,
                width/2,-length/2,height,
                -width/2,length/2,height,
                -width/2,-length/2,height,
        };
        private FloatBuffer mVertexBuffer;

        private void initFloatBuffer()
        {

        }
        public void setCenter(float x,float y,float z)
        {
            if(this.center_x==x&&this.center_y==y&&this.center_z==z)
                return ;
            this.center_x=x;
            this.center_y=y;
            this.center_z=z;
        }
        public PingPongTable()
        {
            mVertexBuffer = BufferUtil.floatToBuffer(VERTEX);
        }

        float radius = 0.04f;
        private void initPingPongTable()
        {

        }

        public void setXYZ(float[] vector, int offset, float x, float y, float z) {
            vector[offset] = center_x+x;
            vector[offset + 1] = center_y+y;
            vector[offset + 2] = center_z+z;
        }

        public void draw(GL10 gl)
        {
            if(gl ==null)
                return ;
            initPingPongTable();

            // 用 glDrawElements 来绘制，mVertexIndexBuffer 指定了顶点绘制顺序
            //gl.glLoadIdentity();
            //gl.glTranslatef(1.5f, 0.0f, -6.0f);
            gl.glColor4f(((float)0x7b/0xff),((float)0x68/0xff),((float)0xee/0xff),1.0f);
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 6);
            gl.glFinish();

            float []middleLines={
                    0,length/2,height,
                    0,-length/2,height,
            };
            gl.glColor4f(1f, 1.0f, 1.0f, 1.0f);
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glLineWidth(20);
            gl.glVertexPointer(3,GL10.GL_FLOAT,0,BufferUtil.floatToBuffer(tableLines));
            gl.glLineWidth(10);
            gl.glDrawArrays(GL10.GL_LINES,0,tableLines.length/3);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glFinish();

            gl.glColor4f(1f, 1.0f, 1.0f, 1.0f);
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3,GL10.GL_FLOAT,0,BufferUtil.floatToBuffer(middleLines));
            gl.glLineWidth(3);
            gl.glDrawArrays(GL10.GL_LINES,0,middleLines.length/3);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glFinish();
        }
    }
}
