package com.example.administrator.fileproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

/**
 * Created by Administrator on 2018/4/4.
 */

public class ImageLoaderRunnable implements Runnable {

    private String path;
    private ImageView imageView;
    private BitmapFactory.Options options;
    private Handler mHandler;

    public void setPath(String path, ImageView imageView, BitmapFactory.Options options, Handler handler) {
        this.path = path;
        this.imageView = imageView;
        this.options = options;
        this.mHandler = handler;
    }


    Bitmap compressBitmap(Bitmap bitmap){



        return bitmap;
    }

    @Override
    public void run() {
        final Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bitmap);
            }
        });
    }

}
