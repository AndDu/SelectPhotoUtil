package com.example.administrator.fileproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;
import org.w3c.dom.ls.LSInput;

import java.util.List;

/**
 * Created by Administrator on 2018/4/3.
 */

public class PhotoListAdapter extends BaseAdapter {


    private final BitmapFactory.Options options;
    private List<ImageFloder> list;
    private Context context;
    private Handler mHandler;

    public PhotoListAdapter(List<ImageFloder> list, Context context, Handler handler) {
        this.list = list;
        this.context = context;
        options = new BitmapFactory.Options();
//        options.inSampleSize = 8;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        this.mHandler = handler;
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list == null ? null : list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
            holder = new ViewHolder(convertView);
            ImageLoaderRunnable imageLoaderRunnable = new ImageLoaderRunnable();
            holder.iv_img.setTag(imageLoaderRunnable);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ImageFloder imageFloder = list.get(position);
//        holder.iv_img.setImageBitmap(BitmapFactory.decodeFile(imageFloder.getFirstImagePath(), options));
        holder.tv_name.setText(String.format("%s(%d)", imageFloder.getName(), imageFloder.getCount()));
        ImageLoaderRunnable tag = (ImageLoaderRunnable) holder.iv_img.getTag();
        tag.setPath(imageFloder.getFirstImagePath(), holder.iv_img, options, mHandler);
        ThreadPoolUtil.getInstance().sumbit(tag);
        return convertView;
    }

    class ViewHolder {
        TextView tv_name;
        ImageView iv_img;

        public ViewHolder(View v) {
            tv_name = (TextView) v.findViewById(R.id.tv_name);
            iv_img = (ImageView) v.findViewById(R.id.iv_img);
        }
    }
}
