package com.khotapps.stringflowlivewallpaper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import java.util.Random;

public class WavePreviewView extends View {

    private final Paint paint = new Paint();
    private float t = 0f;
    private final float dt = 0.03f;

    private int style = 0;           // 0 = None, 1-4 = presets
    private float speedMultiplier = 1f;
    private int numLines = 1;
    private int lineThickness = 4;   // dp
    private int linePadding = 15;    // dp
    private int customColor = Color.CYAN;

    private final int MAX_LINES = 10;
    private final int numModes = 5;
    private final float[][] A = new float[MAX_LINES][numModes];
    private final float[][] phi = new float[MAX_LINES][numModes];
    private final Random random = new Random();

    public WavePreviewView(Context context) { super(context); init(); }
    public WavePreviewView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public WavePreviewView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(lineThickness);
        randomizeWaves();
        post(drawRunnable);
    }

    private void randomizeWaves() {
        for (int line = 0; line < MAX_LINES; line++) {
            for (int i = 0; i < numModes; i++) {
                A[line][i] = 0.3f + random.nextFloat() * 0.7f;
                phi[line][i] = random.nextFloat() * 2f * (float) Math.PI;
            }
        }
    }

    private float omega(int n) {
        return n * (float) Math.PI * 0.1f;
    }

    private float u_string(float x, float t, int width, int lineIndex) {
        float u = 0;
        for (int n = 1; n <= numModes; n++) {
            u += A[lineIndex][n - 1] * Math.sin(n * (float) Math.PI * x / width)
                    * Math.cos(omega(n) * t + phi[lineIndex][n - 1]);
        }
        return u * 100;
    }

    public void setStyle(int style) {
        this.style = style;
        invalidate();
    }

    public void setSpeed(float speed) {
        this.speedMultiplier = speed;
    }

    public void setNumLines(int lines) {
        this.numLines = Math.max(1, Math.min(MAX_LINES, lines));
        invalidate();
    }

    public void setLineThickness(int thicknessDp) {
        this.lineThickness = thicknessDp;
        paint.setStrokeWidth(thicknessDp);
        invalidate();
    }

    public void setLinePadding(int paddingDp) {
        this.linePadding = paddingDp;
        invalidate();
    }

    public void setCustomColor(int color) {
        this.customColor = color;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth(), height = getHeight();
        float centerY = height / 2f;

        canvas.drawColor(Color.BLACK);

        int lineColor = customColor;
        if (style == 1) {
            lineColor = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        } else if (style == 2 || style == 3) {
            lineColor = Color.WHITE;
        } else if (style == 4) {
            float pulse = (float) (Math.sin(t * 3) * 0.5 + 0.5);
            int alpha = (int) (255 * pulse);
            lineColor = Color.argb(alpha, 0, 212, 255);
        }
        paint.setColor(lineColor);

        canvas.save();

        float totalSpacing = (numLines - 1) * linePadding;
        float startY = centerY - totalSpacing / 2f;

        for (int line = 0; line < numLines; line++) {
            Path path = new Path();
            boolean first = true;
            float baseY = startY + line * linePadding;

            for (int x = 0; x <= width; x++) {
                float y = baseY - u_string(x, t, width, line);
                if (first) {
                    path.moveTo(x, y);
                    first = false;
                } else {
                    path.lineTo(x, y);
                }
            }
            canvas.drawPath(path, paint);
        }

        canvas.restore();

        t += dt * speedMultiplier;
        postInvalidateOnAnimation();
    }

    private final Runnable drawRunnable = this::invalidate;
}