package com.even.getimage.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Class Discription :获取所有手机图片及视频，拍照及摄影，适配所有机型的手机，解决部分手机获取不到资源的问题
 * 这个工具类中所包含的方法有：
 * 1.将拍摄的文件保存到指定的位置，并返回Uri
 * 2.判断拍摄的视频或图片是否选择角度，以及旋转的角度
 * 3.将旋转角度的文件旋转会正常角度
 * 4.将Uri路径转换成bitmap图片
 * 5.将Bitmap图片转换成Uri路径
 * 6.将可能已经旋转角度的Uri转回正确显示的Uri
 * 7.将uri路径转换成String类型，在Android4.4之后，通过Uri.getPath()获取的路径是有问题的
 * <p>
 * Created User : Even
 * Created Time : 2017/03/03.
 */

public class CameraUtils {

    private static final String TAG = "CameraUtils";

    /**
     * 获取图片，视频原文件的Uri
     *
     * @return
     */
    public static Uri getFilePath(boolean isVideo) {
        Uri fileUri = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {//判断SD卡是否存在
            String filepath = Environment.getExternalStorageDirectory() + "/EvenTest/camera/";//用来存放图片视频路径
            File file = new File(filepath);
            if (!file.exists()) {
                file.mkdirs();
            }
            if (isVideo) {
                fileUri = Uri.fromFile(new File(file, "eventest.mp4"));
            } else
                fileUri = Uri.fromFile(new File(file, "eventest.png"));
            return fileUri;
        }
        return null;
    }

    /**
     * 获取拍摄的文件有没有选择角度
     *
     * @param path
     * @return
     */
    public static int getFileDegree(String path) {

        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);//获取指定路径的文件Exif信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);//获取图片旋转角度
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 将bitmap进行旋转相对应的角度
     *
     * @param bitmap
     * @param degree
     * @return
     */
    public static Bitmap getRotateBitmap(Bitmap bitmap, int degree) {
        //使用矩阵旋转角度
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        //获取旋转后的图片
        Bitmap rtBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (rtBitmap == null) {
            rtBitmap = bitmap;
        }
        if (bitmap != null && bitmap != rtBitmap) {
            bitmap.recycle();
        }
        return rtBitmap;
    }

    /**
     * 将Uri转换bitmap,并将需要选择的图片旋转到正确角度
     *
     * @param context
     * @param imageUri
     * @return
     */
    public static Bitmap getBitmap(@NonNull Context context, @NonNull Uri imageUri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        if (bitmap != null) {
            Bitmap rtbitmap = getRotateBitmap(bitmap, getFileDegree(imageUri.getPath()));
            return rtbitmap;
        } else
            return null;
    }

    /**
     * 将bitmap转换成Uri
     *
     * @param context
     * @param bitmap
     * @return
     */
    public static Uri bitmapToUri(Context context, Bitmap bitmap) {
        Log.i(TAG, "bitmapToUri: " + bitmap);
        Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, null, null));
        return uri;
    }

    /**
     * 将拍摄所得的Uri旋转到正确显示，返回正确显示的Uri路径
     *
     * @param context
     * @param imageUri
     * @return
     */
    public static Uri getCorrectUri(Context context, Uri imageUri) {
        return bitmapToUri(context, getBitmap(context, imageUri));
    }

    /**
     * 解决在Android4.4以上获取路径的方法，通过Uri.getPath()获取的路径是有问题的
     *
     * @param contentURI
     * @return
     */
    public static String getRealPathFromURI(Context context, Uri contentURI) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }


}
