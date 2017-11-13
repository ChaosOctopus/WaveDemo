package com.chaos.wavedemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import static java.lang.Thread.sleep;

/**
 * Created by yc.Zhao on 2017/11/6 0006.
 */

public class WaveView extends FrameLayout {

    private final int MESH_WIDTH = 20;
    private final int MESH_HEIGHT = 20;

    private final int VERTS_COUNT = (MESH_WIDTH + 1) * (MESH_HEIGHT + 1);

    private final float[] staticVerts = new float[VERTS_COUNT * 2];

    private final float[] targetVerts = new float[VERTS_COUNT * 2];

    private Bitmap bitmap;

    private float rippleWidth = 100f;

    private float rippleSpeed = 15f;

    private float rippleRadius;

    private boolean isRippling;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.arg1){
                case 0:
                    showRipple(500, 1000);
                    break;
            }
        }
    };
    public WaveView(@NonNull Context context) {
        this(context,null);
    }

    public WaveView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public WaveView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }
    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (isRippling && bitmap != null) {
            canvas.drawBitmapMesh(bitmap, MESH_WIDTH, MESH_HEIGHT, targetVerts, 0, null, 0, null);
        } else {
            super.dispatchDraw(canvas);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Message message = handler.obtainMessage();
        message.arg1=0;
        handler.sendMessageDelayed(message,1000);
//        handler.sendMessage(message);


    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                showRipple(ev.getX(), ev.getY());
                Log.e("TAG", "dispatchTouchEvent: "+ev.getX()+"----"+ev.getY() );
                break;
        }
        return super.dispatchTouchEvent(ev);
    }


    public void showRipple(final float originX, final float originY) {
        if (isRippling) {
            return;
        }
        initData();
        if (bitmap == null) {
            return;
        }
        isRippling = true;

        int viewLength = (int) getLength(bitmap.getWidth(), bitmap.getHeight());
        final int count = (int) ((viewLength + rippleWidth) / rippleSpeed);
        CountDownTimer cdt = new CountDownTimer(count * 10, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                rippleRadius = (count - millisUntilFinished / 10) * rippleSpeed;
                warp(originX, originY);
            }

            @Override
            public void onFinish() {
                isRippling = false;
            }
        };
        cdt.start();
    }


    private void initData() {
        bitmap = getCacheBitmapFromView(this);
        if (bitmap == null) {
            return;
        }
        float bitmapWidth = bitmap.getWidth();
        float bitmapHeight = bitmap.getHeight();
        int index = 0;
        for (int height = 0; height <= MESH_HEIGHT; height++) {
            float y = bitmapHeight * height / MESH_HEIGHT;
            for (int width = 0; width <= MESH_WIDTH; width++) {
                float x = bitmapWidth * width / MESH_WIDTH;
                staticVerts[index * 2] = targetVerts[index * 2] = x;
                staticVerts[index * 2 + 1] = targetVerts[index * 2 + 1] = y;
                index += 1;
            }
        }
    }


    private void warp(float originX, float originY) {
        for (int i = 0; i < VERTS_COUNT * 2; i += 2) {
            float staticX = staticVerts[i];
            float staticY = staticVerts[i + 1];
            float length = getLength(staticX - originX, staticY - originY);
            if (length > rippleRadius - rippleWidth && length < rippleRadius + rippleWidth) {
                PointF point = getRipplePoint(originX, originY, staticX, staticY);
                targetVerts[i] = point.x;
                targetVerts[i + 1] = point.y;
            } else {
                targetVerts[i] = staticVerts[i];
                targetVerts[i + 1] = staticVerts[i + 1];
            }
        }
        invalidate();
    }


    private PointF getRipplePoint(float originX, float originY, float staticX, float staticY) {
        float length = getLength(staticX - originX, staticY - originY);

        float angle = (float) Math.atan(Math.abs((staticY - originY) / (staticX - originX)));

        float rate = (length - rippleRadius) / rippleWidth;
        float offset = (float) Math.cos(rate) * 40f;
        float offsetX = offset * (float) Math.cos(angle);
        float offsetY = offset * (float) Math.sin(angle);

        float targetX;
        float targetY;
        if (length < rippleRadius + rippleWidth && length > rippleRadius) {

            if (staticX > originX) {
                targetX = staticX + offsetX;
            } else {
                targetX = staticX - offsetX;
            }
            if (staticY > originY) {
                targetY = staticY + offsetY;
            } else {
                targetY = staticY - offsetY;
            }
        } else {

            if (staticX > originY) {
                targetX = staticX - offsetX;
            } else {
                targetX = staticX + offsetX;
            }
            if (staticY > originY) {
                targetY = staticY - offsetY;
            } else {
                targetY = staticY + offsetY;
            }
        }
        return new PointF(targetX, targetY);
    }


    private float getLength(float width, float height) {
        return (float) Math.sqrt(width * width + height * height);
    }


    private Bitmap getCacheBitmapFromView(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        final Bitmap drawingCache = view.getDrawingCache();
        Bitmap bitmap;
        if (drawingCache != null) {
            bitmap = Bitmap.createBitmap(drawingCache);
            view.setDrawingCacheEnabled(false);
        } else {
            bitmap = null;
        }
        return bitmap;
    }
}
