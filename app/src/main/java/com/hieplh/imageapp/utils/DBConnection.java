package com.hieplh.imageapp.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBConnection extends SQLiteOpenHelper {

    private static final String DB_NAME = "ImageDB";
    private static final int DB_VERSION = 1;

    public DBConnection(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String tableImage = "CREATE TABLE Image(id integer primary key autoincrement, imageData blob,location text)";
        db.execSQL(tableImage);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String tableImage = "DROP TABLE IF EXISTS Image";
        db.execSQL(tableImage);
        onCreate(db);
    }
}
