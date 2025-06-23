package com.example.mariogame;

import static com.example.mariogame.R.id.listView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class HistroryActivity extends AppCompatActivity {
    ListView listView;
    GameDatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        listView = findViewById(R.id.listView);
        dbHelper = new GameDatabaseHelper(this);
        showData();
    }

    private void showData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        ArrayList<String> records = new ArrayList<>();

        try {
            cursor = db.rawQuery("SELECT * FROM play_history ORDER BY id DESC", null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int score = cursor.getInt(cursor.getColumnIndexOrThrow("score"));
                    int level = cursor.getInt(cursor.getColumnIndexOrThrow("level"));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow("play_date"));
                    records.add("Score: " + score + " | Level: " + level + " | " + date);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, records);
        listView.setAdapter(adapter);
    }

}
