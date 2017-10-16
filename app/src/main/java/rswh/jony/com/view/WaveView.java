package rswh.jony.com.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
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
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by Administrator on 2017/10/15 0015.
 */

public class WaveView  extends View{
    private Path mPath;
    private Paint mPaint;
    private int waveLength = 600;//波纹长度
    private Region region = null;

    private int dx = 0;
    private int originY = 200;//Y轴起始点开始绘制
    private int width = 1000;//屏幕的宽度
    private int height = 1920;//屏幕的高度
    private int waveHeight = 80;
    private ValueAnimator mAnimator;

    public WaveView(Context context) {
        super(context);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
}

    /*
    * 初始化数据
    *
    * */
    private void init(){
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);//去除锯齿
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPath = new Path();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setPathData();
        canvas.drawPath(mPath,mPaint);
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
        mAnimator.setDuration(2000);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = (float) animation.getAnimatedValue();
                dx = (int)(waveLength * fraction);
                postInvalidate();
            }
        });
        mAnimator.start();
    }
}
