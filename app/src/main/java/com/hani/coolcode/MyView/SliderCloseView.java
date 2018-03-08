package com.hani.coolcode.MyView;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

import java.util.HashMap;


/** 仿 iOS 左边缘右滑关闭页面
 * Created by admin on 2017/8/14.
 */

public class SliderCloseView extends FrameLayout {


    private int mActivePointerId;
    private boolean mIsBeingDrag;
    private float mInitDownX;
    private float mInitDownY;
//    private float mLastDownX;
    private Scroller mScroller;

    private Context mContext;

    private static final float INVALID_VALUE = -1f;
    private int mTouchSlop;

    private OnSliderListener mSliderListener;

    private View mSliderView;

    private float mCurTranslationX;

    private static final int DEFAULT_ANIM_TIME = 300;
    private static final float HORIZANTAL_SPEED = 2500f;
    private boolean mIsAnimating;
    private boolean mIsSliderShowing;

    private boolean mIsToHiddlenPage;

    private VelocityTracker mVelocityTracker;

    public SliderCloseView(Context context) {
        this(context,null);
    }

    public SliderCloseView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SliderCloseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init(){

        mScroller = new Scroller(mContext);
        mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        setBackgroundColor(Color.TRANSPARENT);
//        setClickable(true);
    }

    public void setSliderListener(OnSliderListener listener){
        mSliderListener = listener;
    }

    public void addViewToLayout(View view, int screenWidth){

        if(view != null){

            mSliderView = view;
            LayoutParams frParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            this.addView(mSliderView,frParams);

            mCurTranslationX = screenWidth;
            mSliderView.setTranslationX(mCurTranslationX);
            actionEnd(false);
        }
    }

    public void clearView(){

        if(mSliderView != null){
            mSliderView = null;
        }
        removeAllViews();
    }

    private void removeViewFromLayout(){

        if(mSliderView != null){

            mCurTranslationX = 0;
            mSliderView.setTranslationX(mCurTranslationX);
            actionEnd(true);

        }

    }
    public void hiddenSliderView(){
        mIsToHiddlenPage = true;
        removeViewFromLayout();

    }

    public boolean isSliderViewVisible(){
        return mSliderView != null && mIsSliderShowing;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        switch (action){
            case MotionEvent.ACTION_DOWN:{
                mActivePointerId = ev.getPointerId(0);
                mInitDownX = getMotionEventX(ev);
//                mLastDownX = mInitDownX;
                mInitDownY = getMotionEventY(ev);
                if(mInitDownX == INVALID_VALUE || mInitDownY == INVALID_VALUE){
                    mIsBeingDrag = false;
                    return super.onInterceptTouchEvent(ev);
                }
                break;
            }
            case MotionEvent.ACTION_MOVE:{
                float x = getMotionEventX(ev);
                float y = getMotionEventY(ev);

                float diffX = x - mInitDownX;
                float diffY = y - mInitDownY;

                if( mInitDownX < getWidth() / 10 && Math.abs(diffX) >= mTouchSlop
                        && Math.abs(diffX) > Math.abs(diffY)){
                    mIsBeingDrag = true;
                }

                break;

            }
            case MotionEvent.ACTION_POINTER_UP:{
                onSecondaryPointerUp(ev);
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:{
                mIsBeingDrag = false;
                mInitDownX = 0;
                mInitDownY = 0;
//                mLastDownX = 0;
                mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                break;
            }
        }

        return mIsBeingDrag ||  super.onInterceptTouchEvent(ev);
    }




    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (MotionEventCompat.getActionMasked(event)){

            case MotionEvent.ACTION_DOWN:{
                mInitDownX = getMotionEventX(event);
                mInitDownY = getMotionEventY(event);
                break;
            }
            case MotionEvent.ACTION_MOVE:{

                if(mVelocityTracker == null){
                    mVelocityTracker = VelocityTracker.obtain();
                }
                mVelocityTracker.addMovement(event);

                float x = getMotionEventX(event);
                float diffX = x - mInitDownX;
//                float diffX =   mLastDownX - x;


                if( diffX >= 0 ){
                    if(mSliderView != null){
                        mSliderView.setTranslationX(diffX);
                    }
                }

                if(mSliderListener != null){
                    mSliderListener.onProgress((int) diffX,diffX * 1.0f  / getWidth(),mSliderView);
                }

                Log.w("lala","getScrollX: "+diffX+" rate: "+ diffX * 1.0f / getWidth() );

                // 左侧即将滑出屏幕

                return true;
            }
            case MotionEvent.ACTION_POINTER_DOWN:
//                mActiviePointerId = event.getPointerId(MotionEventCompat.getActionIndex(event));
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:{

                if(mVelocityTracker != null  && mActivePointerId != MotionEvent.INVALID_POINTER_ID){

                    mVelocityTracker.computeCurrentVelocity(1000);
                    float xVelocity = mVelocityTracker.getXVelocity(mActivePointerId);
                    Log.w("tracker","X velocity: "+xVelocity);

                    mVelocityTracker.clear();
                    mVelocityTracker = null;
                    if( xVelocity >= HORIZANTAL_SPEED  && mSliderView != null){

                        mCurTranslationX = mSliderView.getTranslationX();

                        actionEnd(true);
                        break;

                    }

                }

                // 根据手指释放时的位置决定回弹还是关闭
                float x = getMotionEventX(event);
                float diffX = x - mInitDownX;
                if( diffX == 0 ){
                    if(mSliderListener != null){
//                        mSliderListener.onSliderShow(mSliderView);
                    }
                    resetValue();

                } else  if( diffX == getWidth()){
                    if(mSliderListener != null){
                        mSliderListener.onSliderHidden();
                    }
                    resetValue();

                } else {

                    if (mSliderView != null ){

                        mCurTranslationX = mSliderView.getTranslationX();
                        if(mCurTranslationX < getWidth() / 2){
                            actionEnd(false);
                        }
                        else {
                            actionEnd(true);
                        }
                    }

                }

                break;
            }

        }
        return super.onTouchEvent(event);
    }


    private void actionEnd(boolean toRight){

        ValueAnimator animator = getAnimator(toRight);
        animator.start();
    }

    private ValueAnimator getAnimator(final boolean toRight){

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(mCurTranslationX, toRight ? getWidth():0f);
        valueAnimator.setDuration(DEFAULT_ANIM_TIME);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                if(mSliderView != null){
                    mSliderView.setTranslationX(value);

                }

                if(mSliderListener != null){
                    mSliderListener.onProgress((int) value,value * 1.0f  / getWidth(),mSliderView);
                }

//                Log.w("lala","onAnimationUpdate: "+value+" rate: "+ value * 1.0f / getWidth() );


            }
        });

        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsAnimating = true;

            }

            @Override
            public void onAnimationEnd(Animator animation) {



                if(toRight){

                    if(mSliderListener != null){
                        mSliderListener.onSliderHidden();
                    }
                    mIsSliderShowing = false;
                    mCurTranslationX = getWidth();
                }
                else {

                    if(mSliderListener != null){
                        mSliderListener.onSliderShow(mSliderView);
                    }
                    mIsSliderShowing = true;
                    mCurTranslationX = 0;
                }

                resetValue();

                if(mIsToHiddlenPage){
                    clearView();
                    mIsToHiddlenPage = false;
                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        return valueAnimator;


    }


    private void resetValue(){

        mInitDownX = 0;
        mInitDownY = 0;

        mIsBeingDrag = false;
        mIsAnimating = false;
        mActivePointerId = MotionEvent.INVALID_POINTER_ID;

    }

    private void onSecondaryPointerUp(MotionEvent event){
        int pointerIndex = MotionEventCompat.getActionIndex(event);
        int pointerId = event.getPointerId(pointerIndex);
        if(pointerId == mActivePointerId){
            int newPointerIndex = pointerIndex == 0 ? 1: 0;
            mActivePointerId = event.getPointerId(newPointerIndex);
        }

    }


    private float getMotionEventX(MotionEvent event){
        int pointerIndex = event.findPointerIndex(mActivePointerId);
        return pointerIndex < 0 ? INVALID_VALUE: event.getX(pointerIndex);
    }

    private float getMotionEventY(MotionEvent event){
        int pointerIndex = event.findPointerIndex(mActivePointerId);
        return pointerIndex < 0 ? INVALID_VALUE: event.getY(pointerIndex);
    }



    public interface OnSliderListener{
        void onProgress(int current, float progress,View view);
        void onSliderHidden();
        void onSliderShow(View  page);
    }
}
