package com.example.mariogame;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.appcompat.app.AlertDialog;

public class GameView extends SurfaceView implements Runnable {
    private Thread gameThread;
    private boolean isPlaying = true;
    private boolean levelCompleted = false;
    private Paint paint;
    private SurfaceHolder surfaceHolder;
    private Bitmap marioBitmap, obstacleBitmap;
    private int marioX = 100, marioY = 800, marioVelocityY = 0;
    private int obstacleX, obstacleY, obstacleSpeed = 5;
    private int score = 0, level = 1;
    private boolean scored = false;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private MediaPlayer victorySound;

    public GameView(Context context) {
        super(context);
        surfaceHolder = getHolder();
        paint = new Paint();
        marioBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mario);
        obstacleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.obstacle);

        obstacleX = getWidth() + 500;
        obstacleY = getHeight() - 200; // Đặt chướng ngại vật ở đáy màn hình

        prefs = context.getSharedPreferences("game_data", Context.MODE_PRIVATE);
        editor = prefs.edit();
        level = prefs.getInt("level", 1);

        victorySound = MediaPlayer.create(context, R.raw.victory);
    }

    @Override
    public void run() {
        while (isPlaying) {
            if (!surfaceHolder.getSurface().isValid()) continue;
            update();
            draw();
        }
    }

    private void update() {
        // Cập nhật vị trí Mario
        marioY += marioVelocityY;

        // Giới hạn Mario trong màn hình
        if (marioY < 0) {
            marioY = 0; // Giới hạn phía trên màn hình
        } else if (marioY > getHeight() - marioBitmap.getHeight()) {
            marioY = getHeight() - marioBitmap.getHeight(); // Giới hạn phía dưới màn hình
        }

        // Cập nhật vị trí chướng ngại vật
        obstacleX -= obstacleSpeed;
        if (obstacleX + obstacleBitmap.getWidth() < 0) {
            obstacleX = getWidth();
            scored = false;
        }

        // Kiểm tra va chạm
        if (Rect.intersects(getRectMario(), getRectObstacle())) {
            isPlaying = false;
            saveGameData();
        }

        // Cập nhật điểm và tăng cấp độ
        if (obstacleX + obstacleBitmap.getWidth() < marioX && !scored) {
            score += 10;
            scored = true;
            if (score % 50 == 0) {
                level++;
                editor.putInt("level", level);
                editor.apply();
                obstacleSpeed += 2;
            }
        }

        // Xử lý khi hoàn thành cấp độ
        if (score >= level * 100 && !levelCompleted) {
            levelCompleted = true;
            victorySound.start();
            new Handler(Looper.getMainLooper()).post(() -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("🎉 Hoàn thành Level " + level)
                        .setMessage("Chúc mừng! Bạn đã vượt qua level " + level)
                        .setCancelable(false)
                        .setPositiveButton("Chơi tiếp", (dialog, which) -> {
                            level++;
                            obstacleSpeed += 2;
                            score = 0;
                            editor.putInt("level", level);
                            editor.apply();
                            levelCompleted = false;
                            obstacleX = getWidth();
                        })
                        .show();
            });
        }
        if (Rect.intersects(getRectMario(), getRectObstacle())) {
            isPlaying = false;
            new Handler(Looper.getMainLooper()).post(() -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("💥 Bạn đã thua!")
                        .setMessage("Điểm số: " + score + "\nBắt đầu lại từ Level " + level)
                        .setCancelable(false)
                        .setPositiveButton("Chơi lại", (dialog, which) -> resetLevel())
                        .setNeutralButton("Thoát", (dialog, which) -> exitToMainMenu())
                        .setNegativeButton("Reset tất cả", (dialog, which) -> resetAll())
                        .show();
            });
        }

    }


    private void draw() {
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas != null) {
            // Nền trời
            canvas.drawColor(Color.CYAN);

            // Vẽ Mario
            if (marioBitmap != null) {
                canvas.drawBitmap(marioBitmap, marioX, marioY, paint);
            }

            // Vẽ chướng ngại vật
            if (obstacleBitmap != null) {
                obstacleY = getHeight() - obstacleBitmap.getHeight() ; // Đặt luôn ở đáy
                canvas.drawBitmap(obstacleBitmap, obstacleX, obstacleY, paint);
            }

            // Hiển thị điểm và cấp độ
            paint.setColor(Color.BLACK);
            paint.setTextSize(60);
            canvas.drawText("Score: " + score, 50, 100, paint);
            canvas.drawText("Level: " + level, 50, 180, paint);

            // Kết thúc vẽ
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void pause() {
        isPlaying = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            marioVelocityY = -30;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            marioVelocityY = 10;
        }
        return true;
    }

    private Rect getRectMario() {
        return new Rect(marioX, marioY, marioX + marioBitmap.getWidth(), marioY + marioBitmap.getHeight());
    }

    private Rect getRectObstacle() {
        return new Rect(obstacleX, obstacleY, obstacleX + obstacleBitmap.getWidth(), obstacleY + obstacleBitmap.getHeight());
    }

    private void saveGameData() {
        editor.putInt("level", level);
        editor.apply();
    }private void resetLevel() {
        // Đặt lại vị trí Mario
        marioY = getHeight() - marioBitmap.getHeight();
        marioVelocityY = 0;

        // Đặt lại vị trí chướng ngại vật
        obstacleX = getWidth();
        scored = false;

        // Đặt lại điểm
        score = 0;

        // Tiếp tục trò chơi
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
    private void exitToMainMenu() {
        // Dừng game thread
        isPlaying = false;
        try {
            if (gameThread != null) {
                gameThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Quay lại màn hình chính
        Context context = getContext();
        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).finish();
        }
    }
    private void resetAll() {
        // Đặt lại cấp độ và điểm trong SharedPreferences
        editor.putInt("level", 1); // Cấp độ bắt đầu
        editor.apply();

        // Đặt lại các biến trò chơi
        level = 1;
        score = 0;
        obstacleSpeed = 5; // Tốc độ ban đầu
        resetLevel(); // Khởi động lại từ đầu
    }

}
