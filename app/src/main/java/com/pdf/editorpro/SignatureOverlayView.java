package com.pdf.editorpro;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.*;

public class SignatureOverlayView extends View {

    private Path path = new Path();
    private Paint paint = new Paint();

    private float posX = 200, posY = 400;
    private float scale = 1f;

    private boolean drawingMode = true;
    private boolean dragging = false;

    public SignatureOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint.setColor(Color.RED);
        paint.setStrokeWidth(6f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
    }

    public void setColor(int color) {
        paint.setColor(color);
        invalidate();
    }

    public void clear() {
        path.reset();
        invalidate();
    }

    public Bitmap getSignatureBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.save();
        canvas.translate(posX, posY);
        canvas.scale(scale, scale);
        canvas.drawPath(path, paint);
        canvas.restore();
        return bitmap;
    }

    public float getPosX() { return posX; }
    public float getPosY() { return posY; }
    public float getScale() { return scale; }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(posX, posY);
        canvas.scale(scale, scale);
        canvas.drawPath(path, paint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (drawingMode) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(event.getX() - posX, event.getY() - posY);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    path.lineTo(event.getX() - posX, event.getY() - posY);
                    invalidate();
                    return true;
            }
        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dragging = true;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (dragging) {
                        posX = event.getX();
                        posY = event.getY();
                        invalidate();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    dragging = false;
                    return true;
            }
        }

        return false;
    }

    public void setDrawingMode(boolean enabled) {
        drawingMode = enabled;
    }

    public void setScale(float scale) {
        this.scale = scale;
        invalidate();
    }
}