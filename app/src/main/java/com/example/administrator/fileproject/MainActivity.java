package com.example.administrator.fileproject;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ImageView iv_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv_img = (ImageView) findViewById(R.id.iv_img);
    }

    public void onClick(View view) {
        SelectPhotoActivity.startActivity(this, 12, new ArrayList<String>());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SelectPhotoActivity.REQUESTCODE && resultCode == RESULT_OK) {
            ArrayList<String> arrayListExtra = data.getStringArrayListExtra(SelectPhotoActivity.SELECTPATH_KEY);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            iv_img.setImageBitmap(BitmapFactory.decodeFile(arrayListExtra.get(0), options));
        }
    }
}
