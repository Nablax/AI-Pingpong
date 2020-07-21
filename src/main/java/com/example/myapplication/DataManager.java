package com.example.myapplication;


import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

interface DataCallBack
{
    public void onDataFinish();
}

public class DataManager {
    private static DataManager instance;
    private DataManager(){}
    static
    {
        instance=new DataManager();
    }
    public static DataManager getInstance()
    {
        return instance;
    }

    private byte fightForeFrameHeader = 0x7F;
    private byte fightBackFrameHeader = 0x71;
    private byte fightForeFrameEnder = 0x0d;
    private byte fightBackFrameEnder = 0x0a;
    private List<Double> fightEntireData = new ArrayList<Double>();
    private List<Byte> fightTmpData = new ArrayList<Byte>();
    private List<Byte> fightDataList = new ArrayList<Byte>();
    private boolean fightDetectedEnder = false;
    private int hitingPoint = 0;

    public List<Double> getFightEntireData(){
        return fightEntireData;
    }
    public int getHitingPoint(){
        return hitingPoint;
    }

    public void testDataByteSplit(byte[] date)
    {
        fightEntireData.clear();
        for(int i=0;i<20;i++)
            fightEntireData.add(1.0);
        this.dataCallBack.onDataFinish();
    }
    public void fightDataByteSplit(byte[] data){

        fightEntireData.clear();
        fightTmpData.clear();
        int tmpSize = data.length;
        for(int i = 0;i < tmpSize; i++){
            fightDataList.add(data[i]);
        }
        tmpSize = fightDataList.size();
        for(int i = tmpSize - 1; i > 0; i--){
            if(fightDataList.get(i) == fightBackFrameEnder && fightDataList.get(i-1) == fightForeFrameEnder){
                fightDetectedEnder = true;
                for(int j=0;j<i+1;j++)
                    fightTmpData.add(fightDataList.get(j));
                if(i+1 == tmpSize){
                    fightDataList.clear();
                }
                else
                    fightDataList.subList(0,i+1).clear();
                break;
            }
        }
        if(fightDetectedEnder == true){
            //TODO
            fightDataByteDispose(fightTmpData);
            fightDetectedEnder = false;
        }
    }
    private void fightDataByteDispose(List<Byte> data){
        if(data.get(0) == fightForeFrameHeader && data.get(1) == fightBackFrameHeader){
            int tmpLength = data.size();
            data.subList(0,2).clear();
            tmpLength -= 2;
            data.subList(tmpLength - 2,tmpLength).clear();
            tmpLength -= 2;
            if(data.get(tmpLength - 1)==0)
                return;
            if(tmpLength % 16 == 4){
                for(int i = 0;i < tmpLength - 4;i = i + 16){
                    for(int  j = 0; j < 16; j = j + 2){
                        short tmpFig = (short)(((((data.get(i+j))<<8))&0xffff)+((data.get(i+j+1))&0xff));
                        if(j == 14)
                            tmpFig *= 10;
                        fightEntireData.add(tmpFig/1000.0);
                    }
                    for(int j = 0;j < 7;j++){
                        fightEntireData.add(0.0);
                    }
                }
                hitingPoint = ((((data.get(tmpLength-4))<<8))&0xffff)+((data.get(tmpLength-3))&0xff);
                dataCallBack.onDataFinish();
            }
        }
    }

    private char[] frameHeaderByChar = {0x7f,0x71};
    private char[] frameEnderByChar = {0x7f,0x72};
    private String frameHeader = new String(frameHeaderByChar);
    private String frameEnder = new String(frameEnderByChar);
    private String frameHeaderNew = "7F71";
    private String frameEnderNew = "7F72";
    private String tmpHeaderNew = "7F";
    private String frameOrdNum = "2c";
    private String frameHeaderCheck = "97";
    private String frameEnderCheck = "66";
    private int crc = 0x00;
    private int crcFromBlueTooth = 0x00;
    private String totalData = "";
    private String tmpData = "";
    private String pureData = "";
    private int frameEndIndex = 0;
    private char tmpHeader = frameHeader.charAt(0);
    private char tmpEnder = frameEnder.charAt(0);

    private boolean detectedEnder = false;
    private boolean detectedHeader =false;

    private byte foreFrameHeader = 0x7F;
    private byte backFrameHeader = 0x71;
    private byte foreFrameEnder = 0x7F;
    private byte backFrameEnder = 0x72;
    private char[] crc_table= {
            0x00,0x31,0x62,0x53,0xc4,0xf5,0xa6,0x97,0xb9,0x88,0xdb,0xea,0x7d,0x4c,0x1f,0x2e,
            0x43,0x72,0x21,0x10,0x87,0xb6,0xe5,0xd4,0xfa,0xcb,0x98,0xa9,0x3e,0x0f,0x5c,0x6d,
            0x86,0xb7,0xe4,0xd5,0x42,0x73,0x20,0x11,0x3f,0x0e,0x5d,0x6c,0xfb,0xca,0x99,0xa8,
            0xc5,0xf4,0xa7,0x96,0x01,0x30,0x63,0x52,0x7c,0x4d,0x1e,0x2f,0xb8,0x89,0xda,0xeb,
            0x3d,0x0c,0x5f,0x6e,0xf9,0xc8,0x9b,0xaa,0x84,0xb5,0xe6,0xd7,0x40,0x71,0x22,0x13,
            0x7e,0x4f,0x1c,0x2d,0xba,0x8b,0xd8,0xe9,0xc7,0xf6,0xa5,0x94,0x03,0x32,0x61,0x50,
            0xbb,0x8a,0xd9,0xe8,0x7f,0x4e,0x1d,0x2c,0x02,0x33,0x60,0x51,0xc6,0xf7,0xa4,0x95,
            0xf8,0xc9,0x9a,0xab,0x3c,0x0d,0x5e,0x6f,0x41,0x70,0x23,0x12,0x85,0xb4,0xe7,0xd6,
            0x7a,0x4b,0x18,0x29,0xbe,0x8f,0xdc,0xed,0xc3,0xf2,0xa1,0x90,0x07,0x36,0x65,0x54,
            0x39,0x08,0x5b,0x6a,0xfd,0xcc,0x9f,0xae,0x80,0xb1,0xe2,0xd3,0x44,0x75,0x26,0x17,
            0xfc,0xcd,0x9e,0xaf,0x38,0x09,0x5a,0x6b,0x45,0x74,0x27,0x16,0x81,0xb0,0xe3,0xd2,
            0xbf,0x8e,0xdd,0xec,0x7b,0x4a,0x19,0x28,0x06,0x37,0x64,0x55,0xc2,0xf3,0xa0,0x91,
            0x47,0x76,0x25,0x14,0x83,0xb2,0xe1,0xd0,0xfe,0xcf,0x9c,0xad,0x3a,0x0b,0x58,0x69,
            0x04,0x35,0x66,0x57,0xc0,0xf1,0xa2,0x93,0xbd,0x8c,0xdf,0xee,0x79,0x48,0x1b,0x2a,
            0xc1,0xf0,0xa3,0x92,0x05,0x34,0x67,0x56,0x78,0x49,0x1a,0x2b,0xbc,0x8d,0xde,0xef,
            0x82,0xb3,0xe0,0xd1,0x46,0x77,0x24,0x15,0x3b,0x0a,0x59,0x68,0xff,0xce,0x9d,0xac
    };


    private List<Byte> dataList = new ArrayList<Byte>();
    private List<Byte> entireData = new ArrayList<Byte>();
    public List<Byte> getEntireData(){return this.entireData;}
    public void dataByteSplit(byte[] data){
        entireData.clear();
        int tmpSize = data.length;
        for(int i = 0;i < tmpSize; i++){
            dataList.add(data[i]);
        }
        tmpSize = dataList.size();
        for(int i = tmpSize - 1; i > 0; i--){
            if(dataList.get(i) == backFrameEnder && dataList.get(i-1) == foreFrameEnder){
                detectedEnder = true;
                for(int j=0;j<i+1;j++)
                    entireData.add(dataList.get(j));
                if(i+1 == tmpSize){
                    dataList.clear();
                }
                else
                    dataList.subList(0,i+1).clear();
                break;
            }
        }
        if(detectedEnder == true){
            dataByteDispose(entireData);
            detectedEnder = false;
        }
    }

    private void dataByteDispose(List<Byte> data){
        if(data.get(0) == foreFrameHeader && data.get(1) == backFrameHeader){
            int tmpLength = data.size();
            if(tmpLength<24)//数据长度不够
                return ;
            data.subList(0,2).clear();
            tmpLength -= 2;
            data.subList(tmpLength - 2,tmpLength).clear();
            tmpLength -= 3;
            crc = (data.get(tmpLength)&0xff);
            data.remove(tmpLength);
            for(int i = 2;i<tmpLength-1;i++){
                if(data.get(i) == foreFrameHeader){
                    data.remove(i+1);
                    tmpLength -=1;
                }
            }
            CRC8CheckByte(data);
            if(crcFromBlueTooth==crc)
                this.dataCallBack.onDataFinish();
        }
    }

    public void CRC8CheckByte(List<Byte> data){
        int tmpLength = data.size();
        crcFromBlueTooth = 0x00;
        for(int i=2;i<tmpLength;i++){
            crcFromBlueTooth = crc_table[crcFromBlueTooth^(data.get(i)&0xFF)];
        }
    }

//    //传入字符串处理
//    private int STRING_TIMES = 0;//字符串计数\
//
//    private double ACCELCONSTANT = 4096;
//    private double GRYOCONSTANT = 16.4;
//
//    public double ACCL_X = 0;
//    public double ACCL_Y = 0;
//    public double ACCL_Z = 0;
//    public double GRYO_X = 0;
//    public double GRYO_Y = 0;
//    public double GRYO_Z = 0;
//
//    public String fullData = null;//构成完整字符串
//    //传入字符串处理变量到此结束
//
//    public void DataManage(String data){
//        try
//        {
//            if(STRING_TIMES == 0){
//                STRING_TIMES = 1;
//                fullData = data;
//            }
//            else if(STRING_TIMES == 1){
//                STRING_TIMES = 0;
//                fullData += data;
//                for(int i = 2; i < 14; i = i + 4) {
//                    String tmp = "";
//                    int tmpInt = 0;
//                    for(int j = 0; j < 4; j++){
//                        tmp += fullData.charAt(i + j);
//                    }
//                    try {
//                        tmpInt = (short)Integer.parseInt(tmp,16);
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                    if(i == 2){
//                        ACCL_X = tmpInt/ACCELCONSTANT;
//                    }
//                    else if(i == 6){
//                        ACCL_Y = tmpInt/ACCELCONSTANT;
//                    }
//                    else if(i == 10){
//                        ACCL_Z = tmpInt/ACCELCONSTANT;
//                    }
//                }
//                for(int i = 14; i < 26; i = i + 4) {
//                    String tmp = "";
//                    int tmpInt = 0;
//                    for(int j = 0; j < 4; j++) {
//                        tmp += fullData.charAt(i + j);
//                    }
//                    try {
//                        tmpInt = (short)Integer.parseInt(tmp,16);
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                    if(i  == 14){
//                        GRYO_X = tmpInt/GRYOCONSTANT;
//                    }
//                    else if(i == 18){
//                        GRYO_Y = tmpInt/GRYOCONSTANT;
//                    }
//                    else if(i == 22){
//                        GRYO_Z = tmpInt/GRYOCONSTANT;
//                    }
//                }
//            }
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            this.STRING_TIMES=0;
//        }
//    }
    private DataCallBack dataCallBack;
    public void setDataCallBack(DataCallBack dataCallBack)
    {
        this.dataCallBack=dataCallBack;
    }
    private String value="";
    private String beginStr;
    private String endStr;
    public void setBeginAndEnd(String begin,String end)
    {
        this.beginStr=new String(begin);
        this.endStr=new String(end);
    }
    public void addData(String x)
    {
        value+=x;
        if(value.contains(endStr))
            dataCallBack.onDataFinish();
    }
    public List<Double> convertStrToInt(String rex)
    {
        String []strings=value.split(rex);
        value="";
        List<Double> doubleList=new ArrayList<Double>();
        boolean legal=false;
        boolean ended=false;
        for(int i=0;i<strings.length;i++)
        {
            if(strings[i].equals(beginStr))
                legal=true;
            else if(strings[i].equals(ended))
                ended=true;
            else if(legal&&!ended)
            {
                try
                {
                    doubleList.add(Double.valueOf(strings[i]));
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if(ended)
                value+=rex+strings[i];
        }
        return doubleList;
    }
    public List<Double[]> getDataList(List<Double> data)
    {
        if(data.size()%15!=0)
            return null;
        List<Double[]> doubles=new ArrayList<Double[]>();
        for(int i=0;i<data.size();i+=15)
        {
            Double[] keep=new Double[15];
            for(int j=i;j<i+15;j++)
                keep[i]=new Double(data.get(j));
            doubles.add(keep);
        }
        return doubles;
    }
}
