package com.simon.imagewaveloading;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * auther: elliott zhang
 * Emaill:18292967668@163.com
 */

public class ImageWaveLoadView extends View  {
    private Paint mWavePaint;
    private Paint mPointPaint;
    private Bitmap mImageBitmap;
    private int mBitmapWidth;
    private int mBitmapHeight;
    private Path mPath;

    private PorterDuffXfermode mPorterDuffXfermode;

    /**
     * 绘制2阶bezier曲线，需要两个控制点
     */
    private float controlX,controlY;
    private float waveY;

    private float mRadius =3;

    private int mWidth,mHeight;

    // 在该画布上绘制目标图片
    private Canvas mCanvas;
    // 目标图片
    private Bitmap bg;

    // 用于控制控制点水平移动
    private boolean isIncrease;

    public ImageWaveLoadView(Context context) {
        super(context);
        init();
    }

    public ImageWaveLoadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageWaveLoadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setmImageBitmap(Bitmap mImageBitmap) {
        this.mImageBitmap = mImageBitmap;
    }


    private void init() {
        mWavePaint = new Paint();
        mWavePaint.setAntiAlias(true);
        mWavePaint.setDither(true);
        mWavePaint.setColor(Color.parseColor("#303F9F"));
        mImageBitmap= BitmapFactory.decodeResource(getResources(), R.drawable.rabbit);
        mBitmapWidth=mImageBitmap.getWidth();
        mBitmapHeight=mImageBitmap.getHeight();

        mPath=new Path();

        mPorterDuffXfermode=new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

        mPointPaint= new Paint();
        mPointPaint.setColor(Color.BLACK);
        mPointPaint.setStrokeWidth(6);
        mPointPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        // 初始状态值
        waveY = 7 / 8F * mHeight;
        controlY = 17 / 16F * mHeight;

        // 初始化画布
        mCanvas = new Canvas();
        // 创建bitmap
        bg = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, Bitmap.Config.ARGB_8888);
        // 将新建的bitmap注入画布
        mCanvas.setBitmap(bg);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mBitmapWidth,mBitmapHeight);
        mWidth=mBitmapWidth;
        mHeight=mBitmapHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTargetBitmap(canvas);
        canvas.drawBitmap(bg,getPaddingLeft(),getPaddingTop(),mWavePaint);
        invalidate();
    }

    private void drawTargetBitmap(Canvas canvas) {
        // 重置path
        mPath.reset();
        // 擦除像素
        bg.eraseColor(Color.parseColor("#00ffffff"));
        // 当控制点的x坐标大于或等于终点x坐标时更改标识值
        if (controlX >= mWidth + 1 / 2 * mWidth) {
            isIncrease = false;
        }
        // 当控制点的x坐标小于或等于起点x坐标时更改标识值
        else if (controlX <= 0) {
            isIncrease = true;
        }
        // 根据标识值判断当前的控制点x坐标是该加还是减
        controlX = isIncrease ? controlX + 10 : controlX - 10;
        if (controlY >= 0) {
            // 波浪上移
            controlY -= 1;
            waveY -= 1;
        } else {
            // 超出则重置位置
            waveY = 7 / 8F * mHeight;
            controlY = 17 / 16F * mHeight;
        }

        // 贝塞尔曲线的生成
        mPath.moveTo(0, waveY);
//       canvas.drawCircle(0, waveY,mRadius,mPointPaint);
        // 两个控制点通过controlX，controlY生成
        mPath.cubicTo(controlX / 2, waveY - (controlY - waveY),(controlX + mWidth) / 2, controlY, mWidth, waveY);
/*        // 与下下边界闭合
        canvas.drawCircle(controlX / 2, waveY - (controlY - waveY),mRadius,mPointPaint);
        canvas.drawCircle((controlX + mWidth) / 2, controlY,mRadius,mPointPaint);
        canvas.drawCircle(mWidth, waveY,mRadius,mPointPaint);*/
        mPath.lineTo(mWidth, mHeight);
        mPath.lineTo(0, mHeight);
        mPath.close();

        // 画慕课网logo
        mCanvas.drawBitmap(mImageBitmap, 0, 0, mWavePaint);
        // 设置Xfermode
        mWavePaint.setXfermode(mPorterDuffXfermode);
//         画三阶贝塞尔曲线
        mCanvas.drawPath(mPath, mWavePaint);
        // 重置Xfermode
        mWavePaint.setXfermode(null);
    }

}