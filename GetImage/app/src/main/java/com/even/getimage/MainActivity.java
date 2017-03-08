package com.even.getimage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.even.getimage.utils.CameraUtils;

import java.io.File;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "SystemCamreaActivity";
    private Button btn_photo, btn_takephoto, btn_video, btn_takevideo;
    private ImageView image;
    private Context mContext;

    private static final int TAKEPHOTO = 10000;
    private static final int PHOTO = 10001;
    private static final int TAKEVIDEO = 10002;
    private static final int VIDEO = 10003;
    private static final int CROP = 10004;
    private Uri fileUri;//获取的图片路径

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initView();

    }

    private void initView() {
        btn_photo = (Button) findViewById(R.id.activity_syscamrea_photo);
        btn_takephoto = (Button) findViewById(R.id.activity_syscamrea_takephoto);
        btn_takevideo = (Button) findViewById(R.id.activity_syscamrea_takevideo);
        btn_video = (Button) findViewById(R.id.activity_syscamrea_video);
        image = (ImageView) findViewById(R.id.activity_syscamrea_image);

        btn_video.setOnClickListener(OnClick);
        btn_takevideo.setOnClickListener(OnClick);
        btn_photo.setOnClickListener(OnClick);
        btn_takephoto.setOnClickListener(OnClick);

    }

    private Button.OnClickListener OnClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.activity_syscamrea_photo:
                    getPhoto();
                    break;
                case R.id.activity_syscamrea_takephoto:
                    takePhoto();
                    break;
                case R.id.activity_syscamrea_video:
                    getVideo();
                    break;
                case R.id.activity_syscamrea_takevideo:
                    takeVideo();
                    break;
            }
        }
    };

    /**
     * 进行拍照
     */
    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = CameraUtils.getFilePath(false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, TAKEPHOTO);
    }

    /**
     * 进行摄像
     */
    private void takeVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        fileUri = CameraUtils.getFilePath(true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, TAKEVIDEO);

    }

    /**
     * 获取系统图库中的图片
     */
    private void getPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PHOTO);

    }

    /**
     * 获取系统图库中的视频
     */
    private void getVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, VIDEO);

    }

    /**
     * 将相册选择的或是拍摄所得的图片进行裁剪，并且得到不被压缩和失真的图片
     *
     * @param uri
     */
    private void setCrop(Uri uri) {

        // 裁剪图片意图
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");

        intent.putExtra("crop", "true");
        // 裁剪框的比例，1.5：1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 1000);
        intent.putExtra("outputY", 1000);
        //如果想上传原图，最好不要设置这个，因为当JPEG压缩的时候会失真，最好用png或是不设置也可以,具体了解两者的区别
        // intent.putExtra("outputFormat", "JPEG");// 图片格式
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);//设置之后可以保证获取到的为原图，而不是压缩之后的图片，这个为图片的Uri即可
        startActivityForResult(intent, CROP);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            switch (requestCode) {
                case TAKEPHOTO://表示拍照
                    Log.i(TAG, "onActivityResult: " + fileUri);
                    if (fileUri != null) {
                        Bitmap bitmap = CameraUtils.getBitmap(mContext, fileUri);//图片的选择角度
                        //如果上传的话，只要将correctUri转换成File上传即可
//                        Uri correctUri = CameraUtils.getCorrectUri(mContext, fileUri);
                        image.setImageBitmap(bitmap);
                    }

                    break;
                case TAKEVIDEO://表示摄影
                    Bitmap videoThumbnail = ThumbnailUtils.createVideoThumbnail(fileUri.getPath(),
                            MediaStore.Images.Thumbnails.MINI_KIND);
                    image.setImageBitmap(videoThumbnail);
                    break;

                case PHOTO:
                    if (uri != null) {
                        Bitmap bitmap = CameraUtils.getBitmap(mContext, uri);
                        image.setImageBitmap(bitmap);

                        //如果需要裁剪图片，则可以用下面的代码，这里有可能图片会旋转，所以要用旋转之后的Uri

//                        fileUri = uri;//因为裁剪的路径要设置成fileUri，所以这里先给fileUri赋值
//                        Uri correctUri = CameraUtils.getCorrectUri(mContext, uri);
//                        setCrop(correctUri);
                    }
                    break;
                case VIDEO:
                    if (uri != null) {
                        //比如在这里我们就不能用Uri.getPath方法来获取路径，这样获取的路径是有问题的
//                        File file = new File(uri.getPath());
//                        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(uri.getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
//                        image.setImageBitmap(bitmap);
                        //正确的方法
                        String path = CameraUtils.getRealPathFromURI(mContext, uri);
                        File file = new File(path);
                        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
                        image.setImageBitmap(bitmap);

                    }
                    break;
                case CROP:
                    if (fileUri != null) {

                        Bitmap bitmap = CameraUtils.getBitmap(mContext, fileUri);
                        image.setImageBitmap(bitmap);
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
