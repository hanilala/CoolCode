package com.hani.coolcode.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;


import com.hani.coolcode.R;
import com.hani.coolcode.utils.CommonUtil;
import com.hani.coolcode.utils.FileUtil;

import java.io.File;
import java.io.IOException;



/**
 * Created by admin on 2018/3/28.
 */

public class ScreenRecordService extends Service implements Handler.Callback{

    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private MediaRecorder mMediaRecorder;
    private VirtualDisplay mVirtualDisplay;

    private boolean mIsRunning;
    private int mRecordWidth = CommonUtil.getScreenWidth();
    private int mRecordHeight = CommonUtil.getScreenHeight();
    private int mScreenDpi = CommonUtil.getScreenDpi();


    private int mResultCode;
    private Intent mResultData;

    //录屏文件的保存地址
    private String mRecordFilePath;

    private Handler mHandler;
    //已经录制多少秒了
    private int mRecordSeconds = 0;

    private static final int MSG_TYPE_COUNT_DOWN = 110;

    @Override
    public IBinder onBind(Intent intent) {
        return new RecordBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        mIsRunning = false;
        mMediaRecorder = new MediaRecorder();
        mHandler = new Handler(Looper.getMainLooper(),this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public boolean isReady(){
        return  mMediaProjection != null && mResultData != null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void clearRecordElement(){
        clearAll();
        if (mMediaRecorder != null){
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        mResultData = null;
        mIsRunning =false;
    }

    public boolean ismIsRunning() {
        return mIsRunning;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setResultData(int resultCode, Intent resultData){
        mResultCode = resultCode;
        mResultData = resultData;

        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mProjectionManager != null){
            mMediaProjection = mProjectionManager.getMediaProjection(mResultCode,mResultData);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean startRecord() {
        if ( mIsRunning) {
            return false;
        }
        if (mMediaProjection == null){
            mMediaProjection = mProjectionManager.getMediaProjection(mResultCode,mResultData);

        }

        setUpMediaRecorder();
        createVirtualDisplay();
        mMediaRecorder.start();

        ScreenUtil.startRecord();
        //最多录制三分钟
        mHandler.sendEmptyMessageDelayed(MSG_TYPE_COUNT_DOWN,1000);

        mIsRunning = true;

//        Log.w("lala","startRecord ");
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean stopRecord(String tip) {
//        Log.w("lala","stopRecord: first ");

        if (!mIsRunning) {
            return false;
        }
        mIsRunning = false;
//        Log.w("lala","stopRecord  middle");

        try {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder = null;
            mVirtualDisplay.release();
            mMediaProjection.stop();

//            Log.w("lala","stopRecord ");

        }catch (Exception e){
            e.printStackTrace();
            mMediaRecorder.release();
            mMediaRecorder = null;
//            Log.w("lala","stopRecord  exception");

        }


        mMediaProjection = null;

        mHandler.removeMessages(MSG_TYPE_COUNT_DOWN);
        ScreenUtil.stopRecord(tip);

        if (mRecordSeconds <= 2 ){

            FileUtil.deleteSDFile(mRecordFilePath);
        }else {
            //通知系统图库更新
            FileUtil.fileScanVideo(this,mRecordFilePath,mRecordWidth,mRecordHeight,mRecordSeconds);
        }

//        mRecordFilePath = null;
        mRecordSeconds = 0;

        return true;
    }



    public void pauseRecord(){
        if (mMediaRecorder != null ){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mMediaRecorder.pause();
            }
        }

    }

    public void resumeRecord(){
        if (mMediaRecorder != null ){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mMediaRecorder.resume();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createVirtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("MainScreen", mRecordWidth, mRecordHeight, mScreenDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mMediaRecorder.getSurface(), null, null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setUpMediaRecorder() {

        mRecordFilePath = getSaveDirectory()  + File.separator+  System.currentTimeMillis() + ".mp4";
        if (mMediaRecorder == null){
            mMediaRecorder = new MediaRecorder();
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile( mRecordFilePath );
        mMediaRecorder.setVideoSize(mRecordWidth, mRecordHeight);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setVideoEncodingBitRate((int) (mRecordWidth * mRecordHeight * 3.6));
        mMediaRecorder.setVideoFrameRate(20);

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void clearAll(){
        if (mMediaProjection != null){
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    public String getRecordFilePath(){
        return mRecordFilePath;
    }

    public String getSaveDirectory() {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            return Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){

            case MSG_TYPE_COUNT_DOWN:{

                String str = null;
               boolean enough = FileUtil.getSDFreeMemory() / (1024* 1024) < 4;
               if (enough){
                   //空间不足，停止录屏
                   str = getString(R.string.record_space_tip);
                   stopRecord(str);
                   mRecordSeconds = 0;
                   break;
               }

                mRecordSeconds++;
                int minute = 0, second = 0;
                if (mRecordSeconds >= 60 ){
                    minute = mRecordSeconds / 60;
                    second = mRecordSeconds % 60;
                } else {
                    second = mRecordSeconds;
                }
                ScreenUtil.onRecording("0"+minute+":"+ (second < 10 ? "0"+second :second+"") );

                if (mRecordSeconds < 3 * 60 ){
                    mHandler.sendEmptyMessageDelayed(MSG_TYPE_COUNT_DOWN,1000);
                } else if (mRecordSeconds == 3 * 60 ){
                    str = getString(R.string.record_time_end_tip);
                    stopRecord(str);
                    mRecordSeconds = 0;
                }

                break;
            }
        }
        return true;
    }

    public class RecordBinder extends Binder {
        public ScreenRecordService getRecordService() {
            return ScreenRecordService.this;
        }
    }


}
