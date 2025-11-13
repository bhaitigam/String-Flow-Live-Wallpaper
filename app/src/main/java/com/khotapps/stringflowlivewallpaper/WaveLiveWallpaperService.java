package com.khotapps.stringflowlivewallpaper;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.os.Handler;

import java.util.Random;

public class WaveLiveWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new WaveEngine();
    }

    private class WaveEngine extends Engine {

        private final Paint paint = new Paint();
        private final Handler handler = new Handler();
        private boolean visible = false;
        private float t = 0f;
        private final float dt = 0.03f;

        // Wave parameters
        private final int MAX_LINES = 10;
        private final int numModes = 5;
        private final float[][] A = new float[MAX_LINES][numModes];
        private final float[][] phi = new float[MAX_LINES][numModes];
        private final Random random = new Random();

        private final Runnable drawRunner = this::drawFrame;

        private final SharedPreferences prefs;

        WaveEngine() {
            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);

            // Initialize wave modes
            for (int line = 0; line < MAX_LINES; line++) {
                for (int i = 0; i < numModes; i++) {
                    A[line][i] = 0.3f + random.nextFloat() * 0.7f;
                    phi[line][i] = random.nextFloat() * 2f * (float) Math.PI;
                }
            }

            // Load SharedPreferences
            prefs = getSharedPreferences("WavePrefs", MODE_PRIVATE);
        }

        private float omega(int n) {
            return n * (float) Math.PI * 0.1f;
        }

        private float u_string(float x, float t, int width, int lineIndex) {
            float u = 0;
            for (int n = 1; n <= numModes; n++) {
                u += A[lineIndex][n - 1] * Math.sin(n * (float) Math.PI * x / width) * Math.cos(omega(n) * t + phi[lineIndex][n - 1]);
            }
            return u * 100;
        }

        void drawFrame() {
            final SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;

            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    int width = canvas.getWidth();
                    int height = canvas.getHeight();

                    // Load settings
                    int style = prefs.getInt("wave_style", 0);           // 0 = None
                    float speed = prefs.getInt("wave_speed", 50) / 50f;
                    int numLines = Math.max(1, Math.min(MAX_LINES, prefs.getInt("wave_lines", 1)));
                    int thickness = prefs.getInt("wave_thickness", 4);
                    int padding = prefs.getInt("wave_padding", 15);
                    int customColor = prefs.getInt("wave_color", Color.CYAN);

                    paint.setStrokeWidth(thickness);

                    // Determine color
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

                    canvas.drawColor(Color.BLACK);
                    canvas.save();
                    canvas.translate(0, height / 2f);

                    float totalSpacing = (numLines - 1) * padding;
                    float startY = -totalSpacing / 2f;

                    for (int line = 0; line < numLines; line++) {
                        Path path = new Path();
                        boolean first = true;
                        float baseY = startY + line * padding;

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

                    t += dt * speed;
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }

            handler.removeCallbacks(drawRunner);
            if (visible) {
                handler.postDelayed(drawRunner, 16); // ~60 FPS
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                drawFrame();
                handler.post(drawRunner);
            } else {
                handler.removeCallbacks(drawRunner);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            drawFrame();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            visible = false;
            handler.removeCallbacks(drawRunner);
        }
    }
}