package com.hani.coolcode.MyView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;


import com.hani.coolcode.utils.ImageUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/3/12 0012.
 */

public class AnimationView extends View implements Handler.Callback {

    public static final int DEFAULT_ANIM_TIME = 100;

    public static final int PROCESS_DATA = 1;
    public static final int PROCESS_ANIM_FINISH = 1 << 1;
    public static final int PROCESS_DELAY = 1 << 2;



    public AnimData mCurAnimData;
    public int mCurAnimPos;
    public boolean mIsRepeat;

    public int mAnimTime;

    private Handler mHandler ;
    private ProcessAnimThread mProcessThread;
    private Bitmap mCurShowBmp;

    private List<AnimData> mAnimDataList = new ArrayList<>();

    public AnimationView(Context context) {
        this(context,null);
    }

    public AnimationView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public AnimationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mHandler = new Handler(this);
        mProcessThread = new ProcessAnimThread(getContext(),mHandler);
        mAnimTime = DEFAULT_ANIM_TIME;
    }

    public void setIsRepeat(boolean repeat){
        mIsRepeat = repeat;
    }
    private int mGravity;
    public void SetGravity(int gravity)
    {
        mGravity = gravity;
        invalidate();
    }

    public void setData(List<AnimData> list){
        if (list != null ){
            mAnimDataList.addAll(list);
        }
    }

    private Matrix mTempMatrix = new Matrix();
    @Override
    protected void onDraw(Canvas canvas) {

        if(mCurShowBmp != null && !mCurShowBmp.isRecycled())
        {
            int x = 0;
            int y = 0;
            float scaleX = 1f;
            float scaleY = 1f;
            switch(mGravity & Gravity.HORIZONTAL_GRAVITY_MASK)
            {
                case Gravity.LEFT:
                    x = 0;
                    break;

                case Gravity.RIGHT:
                    x = this.getWidth() - mCurShowBmp.getWidth();
                    break;

                case Gravity.CENTER_HORIZONTAL:
                    x = (this.getWidth() - mCurShowBmp.getWidth()) / 2;
                    break;

                case Gravity.FILL_HORIZONTAL:
                {
                    int w = mCurShowBmp.getWidth();
                    if(w > 0)
                    {
                        scaleX = (float)this.getWidth() / (float)w;
                    }
                    break;
                }

                default:
                    break;
            }
            switch(mGravity & Gravity.VERTICAL_GRAVITY_MASK)
            {
                case Gravity.TOP:
                    y = 0;
                    break;

                case Gravity.BOTTOM:
                    y = this.getHeight() - mCurShowBmp.getHeight();
                    break;

                case Gravity.CENTER_VERTICAL:
                    y = (this.getHeight() - mCurShowBmp.getHeight()) / 2;
                    break;

                case Gravity.FILL_VERTICAL:
                {
                    int h = mCurShowBmp.getHeight();
                    if(h > 0)
                    {
                        scaleY = (float)this.getHeight() / (float)h;
                    }
                    break;
                }

                default:
                    break;
            }
            if(scaleX == 1 && scaleY != 1)
            {
                scaleX = scaleY;
                switch(mGravity & Gravity.HORIZONTAL_GRAVITY_MASK)
                {
                    case Gravity.RIGHT:
                        x = this.getWidth() - (int)(mCurShowBmp.getWidth() * scaleX);
                        break;
                    case Gravity.CENTER_HORIZONTAL:
                        x = (this.getWidth() - (int)(mCurShowBmp.getWidth() * scaleX)) / 2;
                        break;
                }
            }
            else if(scaleX != 1 && scaleY == 1)
            {
                scaleY = scaleX;
                switch(mGravity & Gravity.VERTICAL_GRAVITY_MASK)
                {
                    case Gravity.BOTTOM:
                        y = this.getHeight() - (int)(mCurShowBmp.getHeight() * scaleY);
                        break;
                    case Gravity.CENTER_VERTICAL:
                        y = (this.getHeight() - (int)(mCurShowBmp.getHeight() * scaleY)) / 2;
                        break;
                }
            }
            mTempMatrix.reset();
            mTempMatrix.postScale(scaleX, scaleY);
            mTempMatrix.postTranslate(x, y);
            canvas.drawBitmap(mCurShowBmp, mTempMatrix, null);
        }
    }

    private boolean mHasStarted = false;
    public void start(){

        mHasStarted = true;
        if (mWidth == 0 || mHeight == 0 ){
            return;
        }

        startPlay();

    }

    private void startPlay() {

        if ( mAnimDataList != null && mAnimDataList.size() > 0 ){

            mCurAnimPos = 0;
            AnimData animData = mAnimDataList.get(mCurAnimPos);
            mCurShowBmp = ImageUtil.getBitmap(getContext(),animData.filePath,mWidth,mHeight);
            invalidate();
            if (mListener != null ){
                mListener.onAnimChange(mCurAnimPos,mCurShowBmp);
            }
            checkIsPlayNext();
        }
    }

    private void playNext(final int curAnimPosition ){

        /*mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AnimData data = mAnimDataList.get(curAnimPosition);
                mProcessThread.processData(data);
            }
        }, mAnimTime);*/

        Message msg = Message.obtain();
        msg.what = PROCESS_DELAY;
        msg.arg1 = curAnimPosition;
        mHandler.sendMessageDelayed(msg,mAnimTime);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        quit();
    }

    private void quit(){

        mHasStarted = false;
        if (mProcessThread != null ){
            mProcessThread.clearAll();
        }
    }

    private int mWidth;
    private int mHeight;
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        if (mProcessThread != null ){
            mProcessThread.setSize(w,h);
        }
        if (mHasStarted){
            startPlay();
        }

    }

    private boolean mHavePause = false;
    public void pause(){
        mHavePause = true;
        mHandler.removeMessages(PROCESS_DELAY);
    }

    public void resume(){
        if (mHavePause && mHasStarted){
            checkIsPlayNext();

        }
    }

    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what){
            case PROCESS_ANIM_FINISH:{

                Bitmap bitmap = (Bitmap) msg.obj;
                if (bitmap != null){
                    if (mCurShowBmp != null ){
                        mCurShowBmp.recycle();
                        mCurShowBmp = null;
                    }
                    mCurShowBmp = bitmap;
                    if (mListener != null ){
                        mListener.onAnimChange(mCurAnimPos,bitmap);
                    }
                    invalidate();

                }
                checkIsPlayNext();
                break;
            }
            case PROCESS_DELAY:{
                int curAnimPosition = msg.arg1;
                AnimData data = mAnimDataList.get(curAnimPosition);
                mProcessThread.processData(data);
                break;
            }
        }
        return true;
    }

    private void checkIsPlayNext() {
        mCurAnimPos ++;
        if ( mCurAnimPos >= mAnimDataList.size() ){
            if (mIsRepeat){
                mCurAnimPos = 0;
                playNext(mCurAnimPos);
            } else {
                if ( mListener != null ){
                    mListener.onAnimEnd();
                }
            }
        } else {
            playNext(mCurAnimPos);
        }
    }

    private AnimCallBack mListener;
    public void setAnimCallBack(AnimCallBack callBack){
        mListener = callBack;
    }

    public interface AnimCallBack{

        void onAnimChange(int position, Bitmap bitmap);
        void onAnimEnd();
    }

    public static class AnimData{
         public Object filePath;
         public boolean isHasOwnTime;
         public long animTime;
    }

    public static class ProcessAnimThread{

        private HandlerThread mHandlerThread;
        private Handler mProcessHandler;
        private Handler mUiHandler;

        private AnimData mCurAnimData;

        private int mWidth;
        private int mHeight;
        private WeakReference<Context> mContext;

        public ProcessAnimThread(Context context, Handler handler){
            mUiHandler = handler;
            mContext = new WeakReference<Context>(context);
            init();
        }

        public void setSize(int width,int height){
            mWidth = width;
            mHeight = height;
        }

        private void init(){

            mHandlerThread = new HandlerThread("process_anim_thread");
            mHandlerThread.start();

            mProcessHandler = new Handler(mHandlerThread.getLooper(), new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {

                    switch (msg.what){
                        case PROCESS_DATA:{
                            AnimData animData = (AnimData) msg.obj;
                            Bitmap bitmap = ImageUtil.getBitmap(mContext.get(),animData.filePath,mWidth,mHeight);
                            if (bitmap != null ){
                                Message finishMsg = Message.obtain();
                                finishMsg.what = PROCESS_ANIM_FINISH;
                                finishMsg.obj = bitmap;
                                mUiHandler.sendMessage(finishMsg);
                            }
                            break;
                        }
                    }
                    return true;
                }
            });

        }



        public void processData(AnimData animData){

            if ( animData != null ){
                Message msg = Message.obtain();
                msg.what = PROCESS_DATA;
                msg.obj = animData;
                mProcessHandler.sendMessage(msg);
            }

        }

        public void clearAll(){

            mHandlerThread.quit();
            mHandlerThread = null;
        }


    }
}
