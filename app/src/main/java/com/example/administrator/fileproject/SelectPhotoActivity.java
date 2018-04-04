package com.example.administrator.fileproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2018/3/30.
 */

public class SelectPhotoActivity extends Activity implements View.OnClickListener {


    public static final int REQUESTCODE = 0x123;
    private static final int TAKE_CAMERA_PICTURE = 1000;// 拍照
    private static final String TOTALSIZE_KEY = "totalSize";
    public static final String SELECTPATH_KEY = "selectPath";
    private GridView gridview;
    //    private int totalCount;
    private int totalSize;
    private ProgressDialog pDialog;
    private View rl_title;
    private File mImgDir = new File("");// 图片数量最多的文件夹
    private int mPicsSize;// 存储文件夹中的图片数
    private List<ImageFloder> imageFloderList = new ArrayList<>();
    private List<String> mImgs = new ArrayList<>();
    private String path;
    private File dir;
    private PopupWindow pop;
    private SelectPhotoAdapter photoAdapter;
    private ArrayList<String> selectPath;
    private String takePhotoName;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            path = Environment.getExternalStorageDirectory() + "/" + "selecttake/photo/";
            dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();
            setData();
            pDialog.dismiss();
        }
    };


    public static void startActivity(Activity activity, int totalSize, ArrayList<String> selectPath) {
        Intent intent = new Intent(activity, SelectPhotoActivity.class);
        intent.putExtra(TOTALSIZE_KEY, totalSize);
        intent.putStringArrayListExtra(SELECTPATH_KEY, selectPath);
        activity.startActivityForResult(intent, REQUESTCODE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_selectphoto);
        totalSize = getIntent().getIntExtra(TOTALSIZE_KEY, 0);
        selectPath = getIntent().getStringArrayListExtra(SELECTPATH_KEY);
        gridview = (GridView) findViewById(R.id.gridview);
        rl_title = findViewById(R.id.rl_title);
        findViewById(R.id.confirm).setOnClickListener(this);
        findViewById(R.id.finish).setOnClickListener(this);
        findViewById(R.id.tv_title).setOnClickListener(this);
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("加载中...");
        pDialog.setCancelable(false);
        try {
            getImages();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void getImages() {
        pDialog.show();
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            showToast("暂无内部存储！");
            return;
        }
        new Thread() {
            @Override
            public void run() {
                super.run();
                List<String> parentListPath = new ArrayList<>(); //防止扫描多次扫描同一个文件夹
                Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver contentResolver = SelectPhotoActivity.this.getContentResolver();
                Cursor mCursor = contentResolver.query(imageUri,
                        null,
                        String.format("%s=? or %s=?", MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.MIME_TYPE),
                        new String[]{"image/jpeg", "image/png"},
                        MediaStore.Images.Media.DATE_MODIFIED);
                if (mCursor == null) return;
                if (mCursor.getCount() == 0) return;
                while (mCursor.moveToNext()) {
                    // 获取图片的路径
                    String path = mCursor.getString(mCursor
                            .getColumnIndex(MediaStore.Images.Media.DATA));
                    File parentFile = new File(path).getParentFile();
                    if (parentFile == null) continue;
                    String dirPath = parentFile.getAbsolutePath();
                    if (parentListPath.contains(dirPath)) continue;
                    parentListPath.add(dirPath);
                    ImageFloder imageFloder = new ImageFloder();
                    imageFloder.setDir(dirPath);
                    imageFloder.setFirstImagePath(path);
                    if (parentFile.list() == null) continue;
                    int picSize = parentFile.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.endsWith(".jpg")
                                    || name.endsWith(".png")
                                    || name.endsWith(".jpeg");
                        }
                    }).length;
//                    totalCount += picSize;
                    imageFloder.setCount(picSize);
                    imageFloderList.add(imageFloder);
                    if (picSize > mPicsSize) {
                        mPicsSize = picSize;
                        mImgDir = parentFile;
                    }

                }
                mCursor.close();
                mHandler.sendEmptyMessage(0);

            }
        }.start();

    }

    void setData() {
        if (mImgDir == null) {
            Toast.makeText(SelectPhotoActivity.this, "未搜索到图片", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mImgDir.exists()) {
            mImgs = Arrays.asList(mImgDir.list());
        }

        if (photoAdapter == null) {
            photoAdapter = new SelectPhotoAdapter(mImgs, mImgDir.getAbsolutePath());
            gridview.setAdapter(photoAdapter);
        }

    }

    void setResult() {
        setResult(RESULT_OK, getFinishIntent());
        finish();
    }

    Intent getFinishIntent() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(SELECTPATH_KEY, selectPath);
        return intent;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.finish:
                finish();
                break;
            case R.id.confirm:
                setResult();
                break;
            case R.id.tv_title:
                if (pop == null) {
                    View view = LayoutInflater.from(this).inflate(R.layout.pop_layout, gridview, false);
                    pop = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, getSrcreen().heightPixels / 2);
                    pop.setBackgroundDrawable(getResources().getDrawable(R.drawable.drawable_pop));
                    pop.setTouchable(true);
                    pop.setOutsideTouchable(true);
                    ListView lv_photo = (ListView) view.findViewById(R.id.lv_photo);
                    lv_photo.setAdapter(new PhotoListAdapter(imageFloderList, this, mHandler));
                    lv_photo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            ImageFloder imageFloder = imageFloderList.get(position);
                            String dir = imageFloder.getDir();
                            mImgDir = new File(dir);
                            mImgs = Arrays.asList(mImgDir.list(new FilenameFilter() {
                                @Override
                                public boolean accept(File dir, String name) {
                                    return name.endsWith(".jpg")
                                            || name.endsWith(".png")
                                            || name.endsWith(".jpeg");
                                }
                            }));

                            photoAdapter.setData(mImgs, mImgDir.getAbsolutePath());
                            pop.dismiss();

                        }
                    });
                }
                pop.showAsDropDown(rl_title);
                break;
        }
    }

    DisplayMetrics getSrcreen() {
        WindowManager wm = (WindowManager)
                getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics;
    }

    private class SelectPhotoAdapter extends BaseAdapter {

        private static final int TYPE_TAKEPHOTO = 0;
        private static final int TYPE_SELECTPHOTO = 1;

        private final DisplayMetrics outMetrics;
        private List<String> list;
        private String dirPath;
        private BitmapFactory.Options options = new BitmapFactory.Options();

        private SelectPhotoAdapter(List<String> list, String dirPath) {
            this.list = list;
            this.dirPath = dirPath;
            outMetrics = getSrcreen();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inSampleSize = 4;
        }

        private void setData(List<String> list, String dirPath) {
            this.list = list;
            this.dirPath = dirPath;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? TYPE_TAKEPHOTO : TYPE_SELECTPHOTO;
        }

        @Override
        public int getCount() {
            return (list == null ? 0 : list.size()) + 1;
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
        public int getViewTypeCount() {
            return 2;
        }

        String getImagePath(String s) {
            return dirPath.concat("/").concat(s);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            int itemViewType = getItemViewType(position);
            if (convertView == null) {
                if (TYPE_TAKEPHOTO == itemViewType) {
                    holder = new ViewHolder(null);
                    ImageView view = new ImageView(SelectPhotoActivity.this);
                    view.setImageResource(R.drawable.compose_photo_photograph_imageselect);
                    convertView = view;
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(
                                    android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            File f = new File(dir, takePhotoName = String.valueOf(System.currentTimeMillis()));// localTempImgDir和localTempImageFileName是自己定义的名字
                            Uri u = Uri.fromFile(f);
                            intent.putExtra(
                                    MediaStore.Images.Media.ORIENTATION, 0);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, u);
                            startActivityForResult(intent, TAKE_CAMERA_PICTURE);
                        }
                    });
                    int width = (outMetrics.widthPixels - dip2px(SelectPhotoActivity.this, 10)) / 4;
                    GridView.LayoutParams lp = new GridView.LayoutParams(width, width);
                    view.setLayoutParams(lp);
                } else {
                    convertView = LayoutInflater.from(SelectPhotoActivity.this)
                            .inflate(R.layout.item_selectphoto, parent, false);
                    holder = new ViewHolder(convertView);
                    ImageLoaderRunnable imageLoaderRunnable = new ImageLoaderRunnable();
                    holder.img.setTag(imageLoaderRunnable);
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.img.getLayoutParams();
                    int wid = (outMetrics.widthPixels - dip2px(SelectPhotoActivity.this, 10)) / 4;
                    lp.width = wid;
                    lp.height = wid;
                    holder.img.setLayoutParams(lp);
                }

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (position != 0) {
                String imagePath = getImagePath(list.get(position - 1));
                if (selectPath.contains(imagePath)) {
                    holder.imageButton.setImageResource(R.drawable.pictures_selected_imageselect);
                    holder.img.setColorFilter(Color.parseColor("#77000000"));
                } else {
                    holder.imageButton.setImageResource(R.drawable.picture_unselected_imageselect);
                    holder.img.clearColorFilter();
                }
            }

            holder.setPosition(position);
            if (itemViewType == TYPE_SELECTPHOTO) {
                String imagePath = getImagePath(list.get(position - 1));
                ImageLoaderRunnable tag = (ImageLoaderRunnable) holder.img.getTag();
                tag.setPath(imagePath, holder.img, options, mHandler);
                ThreadPoolUtil.getInstance().sumbit(tag);
            }

            return convertView;
        }

        class ViewHolder {
            ImageView img;
            ImageButton imageButton;

            private void setPosition(int position) {
                this.position = position;
            }

            private int position = -1;

            private ViewHolder(View view) {
                if (view == null) return;
                this.img = (ImageView) view.findViewById(R.id.imageview);
                this.imageButton = (ImageButton) view.findViewById(R.id.id_item_select);
                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String imagePath = getImagePath(list.get(position - 1));
                        if (selectPath.contains(imagePath)) {
                            selectPath.remove(selectPath.indexOf(imagePath));
                            imageButton.setImageResource(R.drawable.picture_unselected_imageselect);
                            ((ImageView) v).setColorFilter(Color.parseColor("#00000000"));
                        } else {
                            if (selectPath.size() >= totalSize) {
                                showToast(String.format("最多只能选择%s张图片", String.valueOf(totalSize)));
                                return;
                            }
                            selectPath.add(imagePath);
                            imageButton.setImageResource(R.drawable.pictures_selected_imageselect);
                            ((ImageView) v).setColorFilter(Color.parseColor("#77000000"));
                        }
                    }
                });
            }
        }

    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        super.onDestroy();
    }

    int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == TAKE_CAMERA_PICTURE) {
            selectPath.add(path.concat(takePhotoName));
            setResult();
        }
    }
}
