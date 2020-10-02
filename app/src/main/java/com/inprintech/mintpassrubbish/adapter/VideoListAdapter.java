package com.inprintech.mintpassrubbish.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inprintech.mintpassrubbish.R;
import com.inprintech.mintpassrubbish.model.EntityVideo;

import java.util.ArrayList;
import java.util.List;

public class VideoListAdapter extends BaseAdapter {
    private static final String TAG = "VideoListAdapter";

    private List<EntityVideo> list = new ArrayList<>();
    private Context context;

    public VideoListAdapter(Context context, List<EntityVideo> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        EntityVideo entityVideo = list.get(i);
        if (view == null) {
            viewHolder = new ViewHolder();
            view= LayoutInflater.from(context).inflate(R.layout.video_item,null);
            viewHolder.path=  view.findViewById(R.id.tv_path);
            viewHolder.time=  view.findViewById(R.id.tv_time);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)view.getTag();
        }
        Log.i(TAG, "getView: ---" + entityVideo.getPath());
        viewHolder.path.setText(entityVideo.getPath());
        viewHolder.time.setText(entityVideo.getDuration()/1000 + "s");
        return view;
    }

    class ViewHolder {
        TextView path;
        TextView time;
    }
}
