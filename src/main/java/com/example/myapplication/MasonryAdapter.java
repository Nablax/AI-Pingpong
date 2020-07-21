package com.example.myapplication;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.example.myapplication.Main2Activity.iconScaling;

/**
 * Created by 叶明林 on 2017/9/4.
 */
//瀑布式布局适配器
public class MasonryAdapter extends RecyclerView.Adapter <MasonryAdapter.MasonryView>{
    private List<ItemStruct> list;
    private Context context;
    private OnItemClickListener onItemClickListener=null;
    private List<Drawable> drawables=new ArrayList<Drawable>();
    public MasonryAdapter(List<ItemStruct> list,Context context)
    {
        this.list=list;
        this.context=context;
        for(int i=0;i<list.size();i++)
        {
            Drawable drawable=DensityUtil.scaleImage(context,list.get(i).imageID,
                    250,250,DensityUtil.DP);
            drawables.add(drawable);
        }
    }
    public static interface OnItemClickListener {
        void onItemClick(View view , int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener)
    {
        this.onItemClickListener=listener;
    }
    @Override
    public MasonryView onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.masonry_item, parent, false);
        return new MasonryView(view);
    }
    @Override
    public void onBindViewHolder(MasonryView holder, int position) {
        holder.imageView.setImageDrawable(drawables.get(position));
        RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rp.addRule(RelativeLayout.CENTER_IN_PARENT);
        rp.setMargins(20,(int)(100*list.get(position).heightScale),20,(int)(200/iconScaling*list.get(position).heightScale));
        holder.imageView.setLayoutParams(rp);
        holder.textView.setText(list.get(position).text);
        holder.textView.setTextColor(Color.parseColor("#ffffff"));
        holder.relativeLayout.setBackgroundColor(list.get(position).backgroundColor);//
    }
    @Override
    public int getItemCount() {
        return this.list.size();
    }

    public class MasonryView extends  RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView imageView;
        TextView textView;
        RelativeLayout relativeLayout;
        public MasonryView(View itemView){
            super(itemView);
            imageView= (ImageView) itemView.findViewById(R.id.masonry_item_image );
            textView=(TextView) itemView.findViewById(R.id.masonry_item_text );
            relativeLayout=(RelativeLayout) itemView.findViewById(R.id.masonry_bg_color);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(onItemClickListener!=null)
                onItemClickListener.onItemClick(v,getPosition());
        }
    }
}
class ItemStruct
{
    public int imageID;
    public String text;
    public int backgroundColor;
    float heightScale=1;
    public ItemStruct(int id, String str, int color)
    {
        this.imageID=id;
        this.text=str;
        this.backgroundColor=color;
    }
    public ItemStruct(int id, String str, int color,float height)
    {
        this.imageID=id;
        this.text=str;
        this.backgroundColor=color;
        this.heightScale=height;
    }
}