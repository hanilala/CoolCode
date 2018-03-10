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


/** 仿 iOS 左边缘右滑关闭页面
 * Created by admin on 2017/8/14.
 */

public class SliderCloseView extends FrameLayout {

    //当前有效的PointerId,默认为第一个按下屏幕的手指
    private int mActivePointerId;
    //true,mSliderView 当前正被拖拽
    private boolean mIsBeingDrag;
    private float mInitDownX;
    private float mInitDownY;
//    private float mLastDownX;

    private Context mContext;

    private static final float INVALID_VALUE = -1f;
    private int mTouchSlop;

    private OnSliderListener mSliderListener;

    private View mSliderView;

    private float mCurTranslationX;
    private static final int DEFAULT_ANIM_TIME = 300;
    private static final float HORIZANTAL_SPEED = 2500f;
    private boolean mIsAnimating;

    //true,mSliderView显示出来
    private boolean mIsSliderShowing;

    //true,除非动画关闭mSliderView
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

        mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        setBackgroundColor(Color.TRANSPARENT);
    }

    public void setSliderListener(OnSliderListener listener){
        mSliderListener = listener;
    }

    public void addViewToLayout(View view, int screenWidth){

        if(view != null){
            //需要设置Clickable，子view必须消费掉Down事件，不然
            //后续的 move,up 事件是接收不到的
            view.setClickable(true);
            mSliderView = view;
            LayoutParams frParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            this.addView(mSliderView,frParams);

            mCurTranslationX = screenWidth;
            //先设置 X 方向的偏移，再开启动画
            //视觉上就可以看到View是从右到左进入页面的
            mSliderView.setTranslationX(mCurTranslationX);
            actionEnd(false);
        }
    }

    public void clearView(){

        if(mSliderView != null){
            removeView(mSliderView);
            mSliderView = null;

        }
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
                //Down 事件触发时，表示有第一个手指接触到屏幕了
                //获取第一个手指Down 的PointerId
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

                //手指按下的初始位置在屏幕左侧的 十分之一的范围里，并且 X 方向的距离
                //比 Y 方向上的多，也超过最小的 mTouchSlop，就可以认为已经开始拖拽了
                if( mInitDownX < getWidth() / 10 && Math.abs(diffX) >= mTouchSlop
                        && Math.abs(diffX) > Math.abs(diffY)){
                    mIsBeingDrag = true;
                }

                break;

            }
            case MotionEvent.ACTION_POINTER_UP:{
                //当有多个手指按在屏幕上，其中一个手指抬起时会进入此方法
                onSecondaryPointerUp(ev);
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:{
                //最后一个手指抬起，或者事件被父view 拦截时，恢复到初始状态
                mIsBeingDrag = false;
                mInitDownX = 0;
                mInitDownY = 0;
//                mLastDownX = 0;
                mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                break;
            }
        }

        //如果 mIsBeingDrag 为 true ，说明已经触发了滑动的条件
        //事件会被拦截，交给 onTouchEvent 处理
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

                //初始化速度追踪器，用以追踪手指的滑动速度
                if(mVelocityTracker == null){
                    mVelocityTracker = VelocityTracker.obtain();
                }
                mVelocityTracker.addMovement(event);

                float x = getMotionEventX(event);
                float diffX = x - mInitDownX;

                if( diffX >= 0 ){
                    //手指是向右滑动的，偏移 SliderView
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
            case MotionEvent.ACTION_POINTER_UP:
                //当有多个手指按在屏幕上，其中一个手指抬起时会进入此方法
                onSecondaryPointerUp(event);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:{

                if(mVelocityTracker != null  && mActivePointerId != MotionEvent.INVALID_POINTER_ID){
                    //获取手指抬起的一瞬间，获取 X 方向上的速度
                    mVelocityTracker.computeCurrentVelocity(1000);
                    float xVelocity = mVelocityTracker.getXVelocity(mActivePointerId);
                    Log.w("tracker","X velocity: "+xVelocity);

                    mVelocityTracker.clear();
                    mVelocityTracker = null;
                    if( xVelocity >= HORIZANTAL_SPEED  && mSliderView != null){
                        //如果水平的速度超过了特定值，可以认为是手指 fling 操作
                        //让 sliderview 做向右的动画操作，关闭页面
                        mCurTranslationX = mSliderView.getTranslationX();

                        actionEnd(true);
                        break;

                    }

                }

                // 根据手指释放时的位置决定回弹还是关闭
                float x = getMotionEventX(event);
                float diffX = x - mInitDownX;
                if( diffX == 0 ){
                    //手指滑动了 sliderview,但是最后手指抬起时，让它回到了原来的位置
                    if(mSliderListener != null){
                        mSliderListener.onSliderShow(mSliderView);
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
                        //sliderview 在 水平方向的偏移少于父布局的宽度的一半
                        //则让其回到原位,否则做动画打开
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


    /**
     * 开启动画，
     * @param toRight true,mSliderView滑向右边,否则，滑向左边
     */
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

                    clearView();
                    if(mIsToHiddlenPage){
                        mIsToHiddlenPage = false;
                    }

                }
                else {

                    if(mSliderListener != null){
                        mSliderListener.onSliderShow(mSliderView);
                    }
                    mIsSliderShowing = true;
                    mCurTranslationX = 0;
                }

                resetValue();


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


    /**
     * 清楚一些记录的变量
     */
    private void resetValue(){

        mInitDownX = 0;
        mInitDownY = 0;

        mIsBeingDrag = false;
        mIsAnimating = false;
        mActivePointerId = MotionEvent.INVALID_POINTER_ID;

    }

    /**
     * 当屏幕上有手指抬起时，判断是不是 Down 事件触发时记录的 PointerId
     * 如果是的话，选其他手指的 PointerId 作为 mActivePointerId
     * @param event
     */
    private void onSecondaryPointerUp(MotionEvent event){
        int pointerIndex = MotionEventCompat.getActionIndex(event);
        int pointerId = event.getPointerId(pointerIndex);
        if(pointerId == mActivePointerId){
            int newPointerIndex = pointerIndex == 0 ? 1: 0;
            mActivePointerId = event.getPointerId(newPointerIndex);
        }

    }


    /**
     * 获取当前有效PointerId 的 X 值
     * @param event
     * @return
     */
    private float getMotionEventX(MotionEvent event){
        int pointerIndex = event.findPointerIndex(mActivePointerId);
        return pointerIndex < 0 ? INVALID_VALUE: event.getX(pointerIndex);
    }

    /**
     * 获取当前有效PointerId 的 Y 值
     * @param event
     * @return
     */
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
