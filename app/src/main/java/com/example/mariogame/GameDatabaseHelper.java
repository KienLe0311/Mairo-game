package com.example.mariogame;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GameDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "game.db";
    private static final int DB_VERSION = 3;

    public GameDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS play_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "score INTEGER, " +
                "level INTEGER, " +
                "play_date TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS play_history");
        onCreate(db);
    }

    public void saveResult(int score, int level) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("score", score);
        values.put("level", level);
        values.put("play_date", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()));
        db.insert("play_history", null, values);
        db.close();
    }
}
