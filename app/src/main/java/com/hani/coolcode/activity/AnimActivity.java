package com.hani.coolcode.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.hani.coolcode.MyView.AnimationView;
import com.hani.coolcode.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/3/14 0014.
 */

public class AnimActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView mTvPlay;
    private AnimationView mAnimView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animation_layout);
        mTvPlay = (TextView) findViewById(R.id.tv_play);
        mTvPlay.setOnClickListener(this);

        mAnimView = (AnimationView) findViewById(R.id.anim);
        mAnimView.setData(getAnimationData());
    }

    private ArrayList<AnimationView.AnimData> getAnimationData()
    {
        ArrayList<AnimationView.AnimData> datas = new ArrayList<>();
        AnimationView.AnimData data;
        int resId;
        String fileName = "loading";
        for (int i = 1; i <= 75; i++) {
            if (i < 10) {
                fileName += "0";
            }
            resId = getResources().getIdentifier(fileName + i, "mipmap", getPackageName());
//            data = new AnimationDialog.AnimFrameData(resId, 100, false);
            data = new AnimationView.AnimData();
            data.filePath = resId;
            datas.add(data);
            fileName = "loading";
        }
        return datas;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAnimView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAnimView.resume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_play:{
                mAnimView.start();
                break;
            }
        }
    }
}
