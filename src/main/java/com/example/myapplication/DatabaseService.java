package com.example.myapplication;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 叶明林 on 2017/2/16.
 */

public class DatabaseService {
    public static final int INT=0;
    public static final int FLOAT=1;
    private AIPingPongHelper dbOpenHelper;
    private String user_id;
    public void setUser_id(String id)
    {
        this.user_id=id;
    }
    public String getUser_id(){return this.user_id;}
     //通过构造方法，实例化DBOpenHelper
    public DatabaseService(Context context,String user) {
        dbOpenHelper = new AIPingPongHelper(context);
        dbOpenHelper.onCreate(dbOpenHelper.getWritableDatabase());
        this.user_id=user;
    }
    //删表
    public void dropTable(String taleName) {
        dbOpenHelper.getWritableDatabase().execSQL(
                "DROP TABLE IF EXISTS " + taleName);

    }
    public boolean ifExistDataOfOneDay(String day)
    {
        if(day==null)
            return false;

        return dbOpenHelper.getWritableDatabase().rawQuery(
                "select * from DailyRecord where day = '"+ day +"' ",
                null).moveToFirst();
    }
    //插入信息
    public void InsertDataToDailyRecord(String day,int hit,float aver_speed,float max_speed,float sport_time)
    {
        try
        {
            dbOpenHelper.getWritableDatabase().execSQL(
                    "insert into DailyRecord " +
                            "(user_id,day,hit,aver_speed,max_speed,sport_time) " +
                            "values" +
                            "(?,?,?,?,?,?)",
                    new Object[] {
                            this.user_id,day,hit,aver_speed,max_speed,sport_time
                    });
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    //取得所有数据
    public void getAllFromDailyRecord()
    {
        Cursor cursor = dbOpenHelper.getWritableDatabase().rawQuery(
                "select * from DailyRecord",
                null);
        if(cursor.moveToFirst()==false)
            return;
        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
        {
            Object[] keep = new Object[]{cursor.getString(0), cursor.getString(1), cursor.getInt(2),
                    cursor.getFloat(3), cursor.getFloat(4), cursor.getFloat(5),
            };
            for (Object x : keep)
                System.out.println(x.toString());
        }
    }
    /**
     * 计算表的大小
     * */
    public long getDataCount() {
        Cursor cursor = dbOpenHelper.getReadableDatabase().rawQuery(
                "select count(*) from accData" , null);
        cursor.moveToFirst();
        return cursor.getLong(0);
    }
    public String getMinDayFromDatabase(String databaseName)
    {
        Cursor cursor = dbOpenHelper.getReadableDatabase().rawQuery(
                "select min(day) from "+databaseName , null);
        cursor.moveToFirst();
        return cursor.getString(0);
    }
    //查找dailyrecord表中指定时间范围内指定列的和
    public int searchDataByTime(String begin ,String end,String att,int type)
    {
        String order="select "+att+" from DailyRecord where user_id = '"+user_id+
                "' and day between '"+begin+"' and '"+end+"'";
        Cursor cursor = dbOpenHelper.getWritableDatabase().rawQuery(order, null);
        if(cursor.moveToFirst()==false)
            return 0;
        int result=0;
        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
            result+=cursor.getInt(cursor.getColumnIndex(att));
        return result;
    }
    //计算指定日期之间指定列的和
    public float searchDataByTime(String begin ,String end,String att)
    {
        String order="select "+att+" from DailyRecord where user_id = '"+user_id+
                "' and day between '"+begin+"' and '"+end+"'";
        Cursor cursor = dbOpenHelper.getWritableDatabase().rawQuery(order, null);
        if(cursor.moveToFirst()==false)
            return 0;
        float result=0;
        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
            result+=cursor.getFloat(cursor.getColumnIndex(att));
        return result;
    }
    //查询日期之间指定列的所有值
    public List<Float> getFloatDataFromDailyRecordByDate(String beginDate, String endDate, String colName)
    {
        String order="select "+colName+" from DailyRecord where user_id = '"+user_id+
                "' and day between '"+beginDate+"' and '"+endDate+"'";
        Cursor cursor = dbOpenHelper.getWritableDatabase().rawQuery(order, null);
        List<Float> ans=new ArrayList<Float>();
        if(cursor.moveToFirst()==false)
            return ans;
        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
            ans.add(cursor.getFloat(cursor.getColumnIndex(colName)));
        return ans;
    }
    //查询日期之间指定列的所有值
    public List<Integer> getIntDataFromDailyRecordByDate(String beginDate, String endDate, String colName)
    {
        String order="select "+colName+" from DailyRecord where user_id = '"+user_id+
                "' and day between '"+beginDate+"' and '"+endDate+"'";
        Cursor cursor = dbOpenHelper.getWritableDatabase().rawQuery(order, null);
        List<Integer> ans=new ArrayList<Integer>();
        if(cursor.moveToFirst()==false)
            return ans;
        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
            ans.add(cursor.getInt(cursor.getColumnIndex(colName)));
        return ans;
    }
    //计算能量消耗
    public float calculateEnergy(String beginDate, String endDate)
    {
        float ans=0;
        String order="select sport_time from DailyRecord where user_id = '"+user_id+
                "' and day between '"+beginDate+"' and '"+endDate+"'";
        Cursor cursor = dbOpenHelper.getWritableDatabase().rawQuery(order, null);
        if(cursor.moveToFirst()==false)
            return ans;
        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
            ans+=cursor.getFloat(cursor.getColumnIndex("sport_time"))*800;
        return ans;
    }
    //计算平均速度
    public float getAverageSpeedBetweenDate(String beginDate, String endDate)
    {
        float ans=0;
        String order="select aver_speed from DailyRecord where user_id = '"+user_id+
                "' and day between '"+beginDate+"' and '"+endDate+"'";
        Cursor cursor = dbOpenHelper.getWritableDatabase().rawQuery(order, null);
        if(cursor.moveToFirst()==false)
            return ans;
        int counter=DateTool.daysBetween(beginDate,endDate);
        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
            ans+=cursor.getFloat(cursor.getColumnIndex("aver_speed"));
        return ans/counter;
    }
    //计算最大速度
    public float getMaxSpeedBetweenDate(String beginDate, String endDate)
    {
        float ans=0;
        String order="select max_speed from DailyRecord where user_id = '"+user_id+
                "' and day between '"+beginDate+"' and '"+endDate+"'";
        Cursor cursor = dbOpenHelper.getWritableDatabase().rawQuery(order, null);
        if(cursor.moveToFirst()==false)
            return ans;
        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
        {
            float keep=cursor.getFloat(cursor.getColumnIndex("max_speed"));
            ans=(keep>ans?keep:ans);
        }
        return ans;
    }
    public void insertMessageIntoUserRecord(UserDataStruct struct)
    {
        try
        {
            dbOpenHelper.getWritableDatabase().execSQL(
                    "insert into UserRecord " +
                            "(user_name,position,signature,praisenumber,hitnumber,sporttime) " +
                            "values" +
                            "(?,?,?,?,?,?)",
                    new Object[] {
                            struct.getUserName(),struct.getPosition(),
                            struct.getSignature(),struct.getPraiseNumber(),struct.getHitNumber(),(float)struct.getSportTime()
                    });
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void updateMessageOfDailyRecord(int hit,float averageSpeed,float maxSpeed,float sportTime)
    {
        try
        {
            String date=DateTool.getCurrentDate();
            Object[] data=getDataFromDailyRecordByDate(date);
            if(data != null)
            {
                hit+=(int)data[2];
                averageSpeed=(averageSpeed+(float)data[3])/2;
                maxSpeed=Math.max(maxSpeed,(float)data[4]);
                sportTime+=(float)data[5];
            }
            dbOpenHelper.getWritableDatabase().execSQL(
                    "update DailyRecord " +
                            "set hit = ? , aver_speed = ? , max_speed = ? ,sport_time = ? where " +
                            "user_id = '" +user_id +"' and " +
                            "day = '"+date+"';",
                    new Object[] {
                            hit,averageSpeed,maxSpeed,sportTime
                    });
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    //取得所有数据
    public Object[] getDataFromDailyRecordByDate(String date)
    {
        Cursor cursor = dbOpenHelper.getWritableDatabase().rawQuery(
                "select * from DailyRecord where day = '"+date +"' and user_id = '"+user_id+"';",
                null);
        if(cursor.moveToFirst()==false)
            return null;
        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
        {
            return new Object[]{cursor.getString(0), cursor.getString(1), cursor.getInt(2),
                    cursor.getFloat(3), cursor.getFloat(4), cursor.getFloat(5),
            };
        }
        return null;
    }


    public boolean ifUserRecordExistUser(String userName)
    {
        if(userName==null)
            return false;

        return dbOpenHelper.getWritableDatabase().rawQuery(
                "select * from UserRecord where user_name = '"+ userName +"' ",
                null).moveToFirst();
    }
    public void searchTopMessageFromUserRecord(String colName,int top,List<UserDataStruct> list)
    {
        if(list==null||colName==null||top<=0)
            return ;
        list.clear();

        Cursor cursor = dbOpenHelper.getWritableDatabase().rawQuery(
                "select * from UserRecord order by "+colName +" desc limit 0,"+top,
                null);
        if(cursor.moveToFirst()==false)
            return;
        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
        {
            //String userName, String position, String signature,
            // int praiseNumber, int hitNumber, double sportTime
            UserDataStruct user=new UserDataStruct(cursor.getString(1),cursor.getString(2),
                    cursor.getString(3),cursor.getInt(4),cursor.getInt(5), cursor.getFloat(6));
            list.add(user);
        }
    }
    public void close() {
        dbOpenHelper.close();
    }
}
