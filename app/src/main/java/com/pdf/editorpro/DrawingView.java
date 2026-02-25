package com.pdf.editorpro;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.github.barteksc.pdfviewer.PDFView;

public class DrawingView extends View {

    private Paint paint;
    private Path currentPath;

    private Bitmap signatureBitmap;
    private float pdfX = 0f;
    private float pdfY = 0f;

    private boolean drawingEnabled = false;
    private boolean hasSignature = false;
    private boolean isDragging = false;

    private float lastX, lastY;

    private PDFView pdfView;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_HARDWARE, null);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(6f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);

        currentPath = new Path();
        setEnabled(false);
    }

    public void setPdfView(PDFView view) {
        this.pdfView = view;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (pdfView == null) return;

        float zoom = pdfView.getZoom();
        float offsetX = pdfView.getCurrentXOffset();
        float offsetY = pdfView.getCurrentYOffset();

        canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.scale(zoom, zoom);

        if (hasSignature && signatureBitmap != null) {
            canvas.drawBitmap(signatureBitmap, pdfX, pdfY, null);
        }

        if (drawingEnabled) {
            canvas.drawPath(currentPath, paint);
        }

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (pdfView == null) return false;
        if (!drawingEnabled && !hasSignature) return false;

        float zoom = pdfView.getZoom();
        float offsetX = pdfView.getCurrentXOffset();
        float offsetY = pdfView.getCurrentYOffset();

        float pdfTouchX = (event.getX() - offsetX) / zoom;
        float pdfTouchY = (event.getY() - offsetY) / zoom;

        if (drawingEnabled) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pdfView.setSwipeEnabled(false);
                    currentPath.moveTo(pdfTouchX, pdfTouchY);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    currentPath.lineTo(pdfTouchX, pdfTouchY);
                    invalidate();
                    return true;
                case MotionEvent.ACTION_UP:
                    RectF bounds = new RectF();
                    currentPath.computeBounds(bounds, true);

                    if (bounds.width() < 10 || bounds.height() < 10) {
                        currentPath.reset();
                        pdfView.setSwipeEnabled(true);
                        return true;
                    }

                    signatureBitmap = Bitmap.createBitmap(
                            (int) bounds.width() + 20,
                            (int) bounds.height() + 20,
                            Bitmap.Config.ARGB_8888
                    );

                    Canvas canvas = new Canvas(signatureBitmap);
                    Matrix matrix = new Matrix();
                    matrix.postTranslate(-bounds.left + 10, -bounds.top + 10);
                    Path finalPath = new Path();
                    currentPath.transform(matrix, finalPath);
                    canvas.drawPath(finalPath, paint);

                    pdfX = bounds.left - 10;
                    pdfY = bounds.top - 10;

                    currentPath.reset();
                    hasSignature = true;
                    drawingEnabled = false;
                    setEnabled(true);
                    pdfView.setSwipeEnabled(true);
                    invalidate();
                    return true;
            }
        }

        if (hasSignature && signatureBitmap != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = pdfTouchX;
                    lastY = pdfTouchY;
                    isDragging = true;
                    pdfView.setSwipeEnabled(false);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (isDragging) {
                        float dx = pdfTouchX - lastX;
                        float dy = pdfTouchY - lastY;

                        pdfX += dx;
                        pdfY += dy;

                        lastX = pdfTouchX;
                        lastY = pdfTouchY;

                        invalidate();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    isDragging = false;
                    pdfView.setSwipeEnabled(true);
                    return true;
            }
        }

        return false;
    }

    // PUBLIC
    public void setDrawingEnabled(boolean enabled) {
        drawingEnabled = enabled;
        setEnabled(enabled);
        invalidate();
    }

    public void setColor(int color) {
        paint.setColor(color);
    }

    public void clear() {
        signatureBitmap = null;
        hasSignature = false;
        currentPath.reset();
        invalidate();
    }

    public boolean hasRealSignature() {
        return hasSignature && signatureBitmap != null;
    }

    public Bitmap getBitmap() {
        return signatureBitmap;
    }

    public float getSignatureX() {
        return pdfX;
    }

    public float getSignatureY() {
        return pdfY;
    }

    public float getSignatureWidth() {
        return signatureBitmap != null ? signatureBitmap.getWidth() : 0;
    }

    public float getSignatureHeight() {
        return signatureBitmap != null ? signatureBitmap.getHeight() : 0;
    }
}