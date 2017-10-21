package rswh.jony.com.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.LinearInterpolator;

import rswh.jony.com.myapplication.R;

/**
 * Created by Administrator on 2017/10/15 0015.
 */

public class WaveView  extends View{
    private static final String TAG = "WaveView";
    private Path mPath;
    private Paint mPaint;
    private ValueAnimator mAnimator;

    private int waveLength = 600;//波纹长度

    private int dx = 0;
    private int originY = 200;//Y轴起始点开始绘制
    private int width = 1000;//屏幕的宽度
    private int height = 1920;//屏幕的高度
    private int waveHeight = 80;

    //默认水波纹在Y轴的起始位置 200px
    private final static int DEFAULT_ORIGIN_Y = 200;
    //用户头像ICON的引用
    private int userIcon;
    private int duration;
    //原始图片
    private Bitmap mBitmap;
    //处理后的圆角图片
    private Bitmap mCircleBitmap;

    //
    private Region region;


    public WaveView(Context context) {
        super(context);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 初始化数据
     */
    private void init(Context context, AttributeSet attrs) {
        TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.wave_view);
        originY = typeArray.getInteger(R.styleable.wave_view_originY,DEFAULT_ORIGIN_Y);
        userIcon = typeArray.getResourceId(R.styleable.wave_view_imageBitmap,0);
        duration = typeArray.getInteger(R.styleable.wave_view_duration,2000);
        waveHeight = typeArray.getInteger(R.styleable.wave_view_waveHeight,100);
        waveLength = typeArray.getInteger(R.styleable.wave_view_waveLength,500);
        typeArray.recycle();
        //初始化ICON
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;//设置图片压缩倍数
        if (userIcon > 0){
            mBitmap = BitmapFactory.decodeResource(getResources(),userIcon,options);
        }else {
            mBitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
        }
        mCircleBitmap  = getCircleBitmap(mBitmap);
        //初始化画笔
        mPaint = new Paint();
        mPaint.setColor(Color.CYAN);
        mPaint.setAntiAlias(true);//去除锯齿
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        //初始化路径
        mPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取宽高模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        //获取屏幕的宽高
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY){
            width = widthSize;
        }
        if (heightMode == MeasureSpec.EXACTLY){
            height = heightSize;
        }else {
            height = 1000;
        }
        Log.e(TAG,"width:" + width +  "  height: " + height);
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setPathData();
        canvas.drawPath(mPath,mPaint);

        Rect bounds = region.getBounds();
        //绘制随波逐流的头像
        if (bounds.top > 0 || bounds.right > 0){
            if(bounds.top < originY){
                canvas.drawBitmap(mCircleBitmap,bounds.right - mCircleBitmap.getWidth()/2,bounds.top-mCircleBitmap.getHeight(),mPaint);
            }else {
                canvas.drawBitmap(mCircleBitmap,bounds.right - mCircleBitmap.getWidth()/2,bounds.bottom-mCircleBitmap.getHeight(),mPaint);
            }
        }else {
            float x = width/2 - mCircleBitmap.getWidth()/2;
            canvas.drawBitmap(mCircleBitmap,x,originY-mCircleBitmap.getHeight(),mPaint);
        }
        //canvas.drawBitmap(mCircleBitmap,width/2,400,mPaint);
    }

    private void setPathData() {
        //绘制一个封闭的曲线填充颜色
        mPath.reset();
        int halfWaveLength = waveLength/2;
        //绘制路径的起始点
        mPath.moveTo(-waveLength+dx,originY);
        for (int i=-waveLength;i<width+waveLength;i+=waveLength){
            //绘制一个完整的波长——拆分成2部分绘制：波峰和波谷
            //采用二阶贝塞尔曲线进行绘制
            //采用相对坐标进行绘制
            mPath.rQuadTo(halfWaveLength/2,-waveHeight,halfWaveLength,0);
            mPath.rQuadTo(halfWaveLength/2,waveHeight,halfWaveLength,0);
        }
        /*
        *矩形切割一定要在路径关闭之前调用（最好在贝塞尔曲线绘制完之后立即调用）
        * 采用矩形切割方式找到交点（矩形无限接近一条直线）
         */
        float x = width/2;//屏幕X轴中点
        region = new Region();
        Region clip = new Region((int)(x-0.1),0,(int)x,height);//切割矩形宽度无限逼近，类似一条直线
        region.setPath(mPath,clip);//切割出来的矩形区域无限接近一个点
        //补充成一个封闭的曲线路径
        mPath.lineTo(width,height);
        mPath.lineTo(0,height);
        mPath.close();//会直接找到起始点绘制成一个封闭的路径
    }

    /*
    * 将图片裁剪成圆形
    * */
    public Bitmap getCircleBitmap(Bitmap bitmap){
        if(bitmap == null) return null;
        Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circleBitmap);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
        final RectF rectF = new RectF(new Rect(0,0,bitmap.getWidth(),bitmap.getHeight()));
        float roundPx = 0.0f;
        //以较短的边为标准绘制
        if (bitmap.getWidth() > bitmap.getHeight()){
            roundPx = bitmap.getHeight() / 2.0f;
        }else{
            roundPx = bitmap.getWidth() / 2.0f;
        }
        paint.setAntiAlias(true);
        canvas.drawARGB(0,0,0,0);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(rectF,roundPx,roundPx,paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        final Rect src = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
        canvas.drawBitmap(bitmap,src,rect,paint);
        return circleBitmap;
    }

    /*
    * 设置属性动画 沿着X轴平移
    *
    * */
    public void startAnimation(){
        mAnimator = ValueAnimator.ofFloat(0,1);
        //设置动画单次执行的时间
        mAnimator.setDuration(2000);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);//无限循环
        //设置插值器，定义动画的变化率
        mAnimator.setInterpolator(new LinearInterpolator());//以常量速率进行改变
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = (float) animation.getAnimatedValue();
                dx = (int)(waveLength * fraction);
                postInvalidate();//调用view函数，进行重绘
            }
        });
        mAnimator.start();
    }
}
