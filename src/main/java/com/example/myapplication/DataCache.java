package com.example.myapplication;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by 叶明林 on 2017/8/17.
 */
interface DataCacheCallBack
{
    public void onDataRemove();
}
public class DataCache {
    private class Entry
    {
        private long population;
        private String id;
        private Object data;
        public Entry(String id,Object dataSet,long population)
        {
            this.id=id;
            this.data=dataSet;
            this.population=population;
        }
    }
    private final int size;//size=-1时不限制队列长度
    //当mode=ASC时，队列优先级按访问时间升序排列，即队列头的上一次访问时间最早
    //当mode=DESC时，队列优先级按访问时间降序排列，即队列头的上一次访问时间最晚
    private final int mode;
    public final static int ASC=-1,DESC=1;
    private DataCacheCallBack dataCacheCallBack;
    private Queue<Entry> priorityQueue;
    public DataCache(int size,int mode)
    {
        this.size=size;
        this.mode=mode;
        this.priorityQueue=new PriorityQueue<Entry>(size,
                new Comparator<Entry>() {
                    @Override
                    public int compare(Entry o1, Entry o2) {
                        long population_o1=o1.population;
                        long population_o2=o2.population;
                        if(population_o1<population_o2)
                            return -1*DataCache.this.mode;
                        else if(population_o1>population_o2)
                            return 1*DataCache.this.mode;
                        return 0;
                    }
                }
        );
    }
    private void updatePopulation(String id,long newPopulation)
    {
        for(Entry x:priorityQueue)
        {
            if(x.id.equals(id))
            {
                x.population=newPopulation;
                break;
            }
        }
    }
    public boolean isEntryContained(String id)
    {
        for(Entry x:priorityQueue)
            if(x.id.equals(id))
                return true;
        return false;
    }
    public synchronized void addEntry(String id,Object data,long visitTime)
    {
        if(isEntryContained(id))
        {
            updatePopulation(id,visitTime);
            return ;
        }
        Entry entry=new Entry(id,data,visitTime);
        this.priorityQueue.add(entry);
        if(size!=-1&&priorityQueue.size()>this.size)
        {
            if(this.dataCacheCallBack!=null)
                this.dataCacheCallBack.onDataRemove();
            priorityQueue.remove();
        }
    }
    public synchronized Object getDataById(String id)
    {
        for(Entry x:priorityQueue)
            if(x.id.equals(id))
                return x.data;
        return null;
    }
    public synchronized Object getTopData()
    {
        return this.priorityQueue.poll();
    }
    public synchronized Object pollTopData()
    {
        return this.priorityQueue.peek();
    }
    public void displayAll()
    {
        for(Entry x:priorityQueue)
        {
            System.out.println( x.id+" "+x.population);
        }
        System.out.println("-----------");
    }

}
