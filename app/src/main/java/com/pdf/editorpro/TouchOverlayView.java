package com.pdf.editorpro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TouchOverlayView extends View {

    private final List<TextItem> textItems = new ArrayList<>();
    private TextItem selectedItem = null;
    private final Paint paint;

    private float lastTouchX;
    private float lastTouchY;

    public TouchOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(48f);
        paint.setColor(Color.BLACK);
    }

    // PDF kaydı ve dış kullanım için
    public List<TextItem> getTextItems() {
        return textItems;
    }

    // Seçili yazıyı sil
    public void deleteSelected() {
        if (selectedItem != null) {
            textItems.remove(selectedItem);
            selectedItem = null;
            invalidate();
            Toast.makeText(getContext(), "Silindi", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Seçili yok", Toast.LENGTH_SHORT).show();
        }
    }

    // Ortada ve seçili şekilde ekleme
    public void addTextCentered(String text, float textSize, int color) {
        post(() -> { // view ölçüleri hazır olunca ekleme
            paint.setTextSize(textSize);
            String[] lines = text.split("\n");

            float maxWidth = 0;
            for (String line : lines) maxWidth = Math.max(maxWidth, paint.measureText(line));

            float x = (getWidth() - maxWidth) / 2f;
            float y = (getHeight() - (lines.length - 1) * textSize) / 2f;

            TextItem item = new TextItem(text, x, y, textSize, color, 0);
            textItems.add(item);
            selectedItem = item;
            invalidate();
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (TextItem item : textItems) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(item.color);
            paint.setTextSize(item.textSize);

            String[] lines = item.text.split("\n");
            for (int i = 0; i < lines.length; i++) {
                canvas.drawText(lines[i], item.x, item.y + i * item.textSize, paint);
            }

            if (item == selectedItem) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.GRAY);
                paint.setStrokeWidth(3);

                float padding = 10;
                float maxWidth = 0;
                for (String line : lines) maxWidth = Math.max(maxWidth, paint.measureText(line));
                float height = lines.length * item.textSize;

                canvas.drawRect(
                        item.x - padding,
                        item.y - padding,
                        item.x + maxWidth + padding,
                        item.y + height + padding,
                        paint
                );
                paint.setStyle(Paint.Style.FILL);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                selectedItem = null;
                for (int i = textItems.size() - 1; i >= 0; i--) {
                    TextItem item = textItems.get(i);
                    paint.setTextSize(item.textSize);

                    String[] lines = item.text.split("\n");
                    float maxWidth = 0;
                    for (String line : lines) maxWidth = Math.max(maxWidth, paint.measureText(line));
                    float height = lines.length * item.textSize;

                    if (x >= item.x && x <= item.x + maxWidth && y >= item.y && y <= item.y + height) {
                        selectedItem = item;
                        lastTouchX = x;
                        lastTouchY = y;
                        invalidate();
                        return true; // PDF kaydırmayı engelle
                    }
                }
                invalidate();
                return false;

            case MotionEvent.ACTION_MOVE:
                if (selectedItem != null) {
                    float dx = x - lastTouchX;
                    float dy = y - lastTouchY;
                    selectedItem.x += dx;
                    selectedItem.y += dy;
                    lastTouchX = x;
                    lastTouchY = y;
                    invalidate();
                    return true;
                }
                return false;

            case MotionEvent.ACTION_UP:
                return selectedItem != null;
        }
        return false;
    }

    public void clearAll() {
        textItems.clear();
        selectedItem = null;
        invalidate();
    }
}

