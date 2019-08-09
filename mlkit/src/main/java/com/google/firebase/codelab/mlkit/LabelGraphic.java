
package com.google.firebase.codelab.mlkit;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;

import java.util.List;

/**
 * Graphic instance for rendering image labels.
 */
public class LabelGraphic extends GraphicOverlay.Graphic {

    private final Paint textPaint;
    private final Paint bgPaint;
    private final GraphicOverlay overlay;

    private List<String> labels;

    LabelGraphic(GraphicOverlay overlay, List<String> labels) {
        super(overlay);
        this.overlay = overlay;
        this.labels = labels;
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(60.0f);
        bgPaint = new Paint();
        bgPaint.setColor(Color.BLACK);
        bgPaint.setAlpha(50);
    }

    @Override
    public synchronized void draw(Canvas canvas) {
        float x = overlay.getWidth() / 4.0f;
        float y = overlay.getHeight() / 4.0f;

        for (String label : labels) {
            drawTextWithBackground(label, x, y, new TextPaint(textPaint), bgPaint, canvas);
            y = y - 62.0f;
        }
    }

    private void drawTextWithBackground(String text, float x, float y, TextPaint paint,
                                        Paint bgPaint, Canvas canvas) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        canvas.drawRect(new Rect((int) (x), (int) (y + fontMetrics.top),
                (int) (x + paint.measureText(text)), (int) (y + fontMetrics.bottom)), bgPaint);
        canvas.drawText(text, x, y, textPaint);
    }
}
