package com.example.myapplication;

import java.nio.Buffer;
import java.nio.FloatBuffer;

/**
 * Package com.hc.opengl
 * Created by HuaChao on 2016/7/28.
 */
public class Model {
    //三角面个数
    private int facetCount;
    //顶点坐标数组
    private float[] verts;
    //每个顶点对应的法向量数组
    private float[] vnorms;
    //每个三角面的属性信息
    private short[] remarks;

    //顶点数组转换而来的Buffer
    private FloatBuffer vertBuffer;

    //每个顶点对应的法向量转换而来的Buffer
    private FloatBuffer vnormBuffer;
    //以下分别保存所有点在x,y,z方向上的最大值、最小值
    float maxX;
    float minX;
    float maxY;
    float minY;
    float maxZ;
    float minZ;
    private Point center=null;

    //返回模型的中心点
    public Point getCentrePoint() {
        float cx = (maxX - minX) / 2;
        float cy = (maxY - minY) / 2;
        float cz = (maxZ - minZ) / 2;
        this.center=new Point(1, 1,1);
        return this.center;
    }

    //包裹模型的最大半径
    public float getR() {
        float dx = (maxX - minX);
        float dy = (maxY - minY);
        float dz = (maxZ - minZ);
        float max = dx;
        if (dy > max)
            max = dy;
        if (dz > max)
            max = dz;
        return max;
    }

    public int getFacetCount() {
        return facetCount;
    }

    public void setFacetCount(int facetCount) {
        this.facetCount = facetCount;
    }

    public float[] getVerts() {
        return verts;
    }

    public void setVerts(float[] verts) {
        this.verts = verts;
        vertBuffer = BufferUtil.floatToBuffer(verts);
    }

    public FloatBuffer getVertBuffer() {

        return vertBuffer;
    }

    public FloatBuffer getVnormBuffer() {
        return vnormBuffer;
    }

    public float[] getVnorms() {
        return vnorms;
    }

    public void setVnorms(float[] vnorms) {
        this.vnorms = vnorms;
        vnormBuffer = BufferUtil.floatToBuffer(vnorms);
    }

    public short[] getRemarks() {
        return remarks;
    }

    public void setRemarks(short[] remarks) {
        this.remarks = remarks;
    }


}
