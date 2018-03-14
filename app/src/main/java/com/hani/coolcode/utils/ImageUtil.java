package com.hani.coolcode.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.lang.reflect.Field;

/**
 * Created by Administrator on 2018/3/12 0012.
 */

public class ImageUtil {

    public static void setPicToImageView(ImageView imageView, String imagePath){

        BitmapFactory.Options opts = new BitmapFactory.Options();

        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, opts);

        ImageSize imageSize = getImageViewSize(imageView);

        opts.inSampleSize = caculateInSampleSize(opts,imageSize.width,imageSize.height);

        opts.inPurgeable = true;
        opts.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, opts);
        imageView.setImageBitmap(bitmap);
    }


    public static void setPicToImageView(Context context, ImageView imageView, int resId){

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(),resId,opts);

        ImageSize imageSize = getImageViewSize(imageView);

        opts.inSampleSize = caculateInSampleSize(opts,imageSize.width,imageSize.height);

        opts.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),resId,opts);
        imageView.setImageBitmap(bitmap);

    }

    /**
     * 根据需求的宽和高以及图片实际的宽和高计算SampleSize
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int caculateInSampleSize(BitmapFactory.Options options, int reqWidth,
                                           int reqHeight)
    {
        int width = options.outWidth;
        int height = options.outHeight;

        int inSampleSize = 1;

        if (width > reqWidth || height > reqHeight)
        {
            int widthRadio = Math.round(width * 1.0f / reqWidth);
            int heightRadio = Math.round(height * 1.0f / reqHeight);

            inSampleSize = Math.max(widthRadio, heightRadio);
        }

        return inSampleSize;
    }




    /**
     * 根据ImageView获适当的压缩的宽和高
     *
     * @param imageView
     * @return
     */
    public static ImageSize getImageViewSize(ImageView imageView)
    {

        ImageSize imageSize = new ImageSize();
        DisplayMetrics displayMetrics = imageView.getContext().getResources()
                .getDisplayMetrics();


        ViewGroup.LayoutParams lp = imageView.getLayoutParams();

        int width = imageView.getWidth();// 获取imageview的实际宽度
        if (width <= 0)
        {
            width = lp.width;// 获取imageview在layout中声明的宽度
        }
        if (width <= 0)
        {
            //width = imageView.getMaxWidth();// 检查最大值
            width = getImageViewFieldValue(imageView, "mMaxWidth");
        }
        if (width <= 0)
        {
            width = displayMetrics.widthPixels;
        }

        int height = imageView.getHeight();// 获取imageview的实际高度
        if (height <= 0)
        {
            height = lp.height;// 获取imageview在layout中声明的宽度
        }
        if (height <= 0)
        {
            height = getImageViewFieldValue(imageView, "mMaxHeight");// 检查最大值
        }
        if (height <= 0)
        {
            height = displayMetrics.heightPixels;
        }
        imageSize.width = width;
        imageSize.height = height;

        /*Log.w("screen","imageSize width: "+imageSize.width);
        Log.w("screen","imageSize height: "+imageSize.height);

        Log.w("screen","screen width : "+ ShareData.m_screenWidth);
        Log.w("screen","screen height : "+ ShareData.m_screenHeight);

        Log.w("screen","displayMetrics.widthPixels : "+displayMetrics.widthPixels);
        Log.w("screen","displayMetrics.heightPixels : "+displayMetrics.heightPixels);*/

        return imageSize;
    }

    public static class ImageSize
    {
        int width;
        int height;
    }

    /**
     * 通过反射获取imageview的某个属性值
     *
     * @param object
     * @param fieldName
     * @return
     */
    private static int getImageViewFieldValue(Object object, String fieldName)
    {
        int value = 0;
        try
        {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = field.getInt(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE)
            {
                value = fieldValue;
            }
        } catch (Exception e)
        {
        }
        return value;

    }

    /**
     * 根据要求的宽高，从本地路径得到bitmap
     * @param picPath
     * @param width
     * @param height
     * @return
     */
    public static Bitmap createBitmap(String picPath, int width, int height){
        if(!TextUtils.isEmpty(picPath)){
            BitmapFactory.Options opts = new BitmapFactory.Options();

            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(picPath, opts);

            opts.inSampleSize = caculateInSampleSize(opts,width,height);

            opts.inJustDecodeBounds = false;


            Bitmap bitmap = BitmapFactory.decodeFile(picPath, opts);
            return bitmap;
        }
        return null;
    }

    public static Bitmap createBitmap(Context context, int resId, int width, int height){
        if( resId != 0){
            BitmapFactory.Options opts = new BitmapFactory.Options();

            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(context.getResources(),resId,opts);

            opts.inSampleSize = caculateInSampleSize(opts,width,height);

            opts.inJustDecodeBounds = false;


            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),resId,opts);
            return bitmap;
        }
        return null;
    }

    public static Bitmap getBitmap(Context context, Object path, int width, int height){

        Bitmap bitmap = null;

        if (path instanceof String){
            bitmap = createBitmap((String) path,width,height);


        } else if (path instanceof Integer){
            if (context != null ){
                bitmap = createBitmap(context, (Integer) path,width,height);
            }
        }
        return bitmap;
    }

    /**
     * 得到图片的尺寸
     * @param picPath
     * @return size[0] width; size[1] height
     */
    public static int[] getPicSize(String picPath){

        int[] size = new int[]{0,0};
        File file = new File(picPath);
        if(!TextUtils.isEmpty(picPath) && file.exists() && file.isFile() ){
            BitmapFactory.Options opts = new BitmapFactory.Options();

            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(picPath, opts);
            size[0] = opts.outWidth;
            size[1] = opts.outHeight;

            return size;
        }
        return size;
    }

}
