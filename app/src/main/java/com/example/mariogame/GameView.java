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
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameView extends SurfaceView  implements Runnable{
    private Thread gameThread;
    private boolean isPlaying = true;
    private boolean levelCompleted = false;
    private Paint paint;
    private SurfaceHolder surfaceHolder;
    private Bitmap marioBitmap, obstacleBitmap;
    private int marioX = 100, marioY = 800, marioVelocityY = 0;
    private int obstacleX, obstacleY, obstacleSpeed = 20;
    private int score = 0, level = 1;
    private boolean scored = false;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private GameDatabaseHelper dbHelper;
    private MediaPlayer victorySound;
    public GameView(Context context) {
        super(context);
        surfaceHolder = getHolder();
        paint = new Paint();
        marioBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mario);
        obstacleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.obstacle);
        if (obstacleX == 0 || obstacleY == 0) {
            obstacleX = getWidth() + 200;
            obstacleY = getHeight() - 200;
        }

        prefs = getContext().getSharedPreferences("game_data", Context.MODE_PRIVATE);
        editor = prefs.edit();
        level = prefs.getInt("level", 1);

        dbHelper = new GameDatabaseHelper(getContext());
        victorySound = MediaPlayer.create(getContext(), R.raw.victory);
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
        marioY += marioVelocityY;
        if (marioY > getHeight() - marioBitmap.getHeight()) {
            marioY = getHeight() - marioBitmap.getHeight();
        }

        obstacleX -= obstacleSpeed;
        if (obstacleX + obstacleBitmap.getWidth() < 0) {
            obstacleX = getWidth();
            scored = false;
        }

        if (Rect.intersects(getRectMario(), getRectObstacle())) {
            isPlaying = false;
            dbHelper.saveResult(score, level);
        }

        if (obstacleX + obstacleBitmap.getWidth() < marioX && !scored) {
            score += 10;
            scored = true;
            if (score % 50 == 0) {
                level++;
                editor.putInt("level", level);
                editor.apply();
                obstacleSpeed += 5;
            }
        }

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
                            obstacleSpeed += 5;
                            score = 0;
                            editor.putInt("level", level);
                            editor.apply();
                            levelCompleted = false;
                            obstacleX = getWidth();
                        })
                        .show();
            });
        }
    }

    private void draw() {
        Canvas canvas = surfaceHolder.lockCanvas();
        canvas.drawColor(Color.CYAN);
        canvas.drawBitmap(marioBitmap, marioX, marioY, paint);
        canvas.drawBitmap(obstacleBitmap, obstacleX, obstacleY, paint);
        paint.setColor(Color.BLACK);
        paint.setTextSize(60);
        canvas.drawText("Score: " + score, 50, 100, paint);
        canvas.drawText("Level: " + level, 50, 180, paint);
        surfaceHolder.unlockCanvasAndPost(canvas);
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
}

