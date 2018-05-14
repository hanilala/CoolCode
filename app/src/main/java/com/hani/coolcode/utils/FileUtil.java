package com.hani.coolcode.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

public class FileUtil {

    /**
     * 删除SD卡中的文件或目录
     *
     * @param path
     * @return
     */
    public static boolean deleteSDFile(String path) {
        return deleteSDFile(path, false);
    }

    /**
     * 删除SD卡中的文件或目录
     *
     * @param path
     * @param deleteParent true为删除父目录
     * @return
     */
    public static boolean deleteSDFile(String path, boolean deleteParent) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }

        File file = new File(path);
        if (!file.exists()) {
            //不存在
            return true;
        }
        return deleteFile(file, deleteParent);
    }

    /**
     * @param file
     * @param deleteParent true为删除父目录
     * @return
     */
    public static boolean deleteFile(File file, boolean deleteParent) {
        boolean flag = false;
        if (file == null) {
            return flag;
        }
        if (file.isDirectory()) {
            //是文件夹
            File[] files = file.listFiles();
            if (files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    flag = deleteFile(files[i], true);
                    if (!flag) {
                        return flag;
                    }
                }
            }
            if (deleteParent) {
                flag = file.delete();
            }
        } else {
            flag = file.delete();
        }
        file = null;
        return flag;
    }

    /**
     * 添加到媒体数据库
     *
     * @param context 上下文
     */
    public static Uri fileScanVideo(Context context, String videoPath, int videoWidth, int videoHeight,
                                    int videoTime) {

        File file = new File(videoPath);
        if (file.exists()) {

            Uri uri = null;

            long size = file.length();
            String fileName = file.getName();
            long dateTaken = System.currentTimeMillis();

            ContentValues values = new ContentValues(11);
            values.put(MediaStore.Video.Media.DATA, videoPath); // 路径;
            values.put(MediaStore.Video.Media.TITLE, fileName); // 标题;
            values.put(MediaStore.Video.Media.DURATION, videoTime * 1000); // 时长
            values.put(MediaStore.Video.Media.WIDTH, videoWidth); // 视频宽
            values.put(MediaStore.Video.Media.HEIGHT, videoHeight); // 视频高
            values.put(MediaStore.Video.Media.SIZE, size); // 视频大小;
            values.put(MediaStore.Video.Media.DATE_TAKEN, dateTaken); // 插入时间;
            values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);// 文件名;
            values.put(MediaStore.Video.Media.DATE_MODIFIED, dateTaken / 1000);// 修改时间;
            values.put(MediaStore.Video.Media.DATE_ADDED, dateTaken / 1000); // 添加时间;
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");

            ContentResolver resolver = context.getContentResolver();

            if (resolver != null) {
                try {
                    uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                } catch (Exception e) {
                    e.printStackTrace();
                    uri = null;
                }
            }

            if (uri == null) {
                MediaScannerConnection.scanFile(context, new String[]{videoPath}, new String[]{"video/*"}, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {

                    }
                });
            }

            return uri;
        }

        return null;
    }

    /**
     * SD卡存在并可以使用
     */
    public static boolean isSDExists() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡的剩余容量，单位是Byte
     *
     * @return
     */
    public static long getSDFreeMemory() {
        try {
            if (isSDExists()) {
                File pathFile = Environment.getExternalStorageDirectory();
                // Retrieve overall information about the space on a filesystem.
                // This is a Wrapper for Unix statfs().
                StatFs statfs = new StatFs(pathFile.getPath());
                // 获取SDCard上每一个block的SIZE
                long nBlockSize = statfs.getBlockSize();
                // 获取可供程序使用的Block的数量
                // long nAvailBlock = statfs.getAvailableBlocksLong();
                long nAvailBlock = statfs.getAvailableBlocks();
                // 计算SDCard剩余大小Byte
                long nSDFreeSize = nAvailBlock * nBlockSize;
                return nSDFreeSize;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }
}
