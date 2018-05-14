package com.hani.coolcode;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hani.coolcode.service.ScreenRecordService;
import com.hani.coolcode.service.ScreenUtil;
import com.hani.coolcode.utils.CommonUtil;
import com.hani.coolcode.utils.ToastUtil;

public class ScreenRecordActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTvStart;
    private TextView mTvEnd;

    private TextView mTvTime;

    private int REQUEST_CODE = 1;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_record_activity);

        CommonUtil.init(this);
        mTvStart = findViewById(R.id.tv_start);
        mTvStart.setOnClickListener(this);

        mTvTime = findViewById(R.id.tv_record_time);

        mTvEnd = findViewById(R.id.tv_end);
        mTvEnd.setOnClickListener(this);

       startScreenRecordService();

    }

    private ServiceConnection mServiceConnection;

    /**
     * 开启录制 Service
     */
    private void startScreenRecordService(){

        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                ScreenRecordService.RecordBinder recordBinder = (ScreenRecordService.RecordBinder) service;
                ScreenRecordService screenRecordService = recordBinder.getRecordService();
                ScreenUtil.setScreenService(screenRecordService);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        Intent intent = new Intent(this, ScreenRecordService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

        ScreenUtil.addRecordListener(recordListener);
    }

    private ScreenUtil.RecordListener recordListener = new ScreenUtil.RecordListener() {
        @Override
        public void onStartRecord() {

        }

        @Override
        public void onPauseRecord() {

        }

        @Override
        public void onResumeRecord() {

        }

        @Override
        public void onStopRecord(String stopTip) {
            ToastUtil.show(ScreenRecordActivity.this,stopTip);
        }

        @Override
        public void onRecording(String timeTip) {
            mTvTime.setText(timeTip);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){

            try {
                ScreenUtil.setUpData(resultCode,data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            ToastUtil.show(this,"拒绝录屏");
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.tv_start:{
                ScreenUtil.startScreenRecord(this,REQUEST_CODE);
                break;
            }
            case R.id.tv_end:{
                ScreenUtil.stopScreenRecord(this);
                break;
            }
        }

    }
}
