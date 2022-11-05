package com.hieplh.imageapp.dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.hieplh.imageapp.model.ImageModel;
import com.hieplh.imageapp.utils.DBConnection;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageDAO {

    private SQLiteDatabase db;

    public String tempLocation = "";

    public ImageDAO(Context context) {
        DBConnection dbConnection = new DBConnection(context);
        db = dbConnection.getWritableDatabase();
    }

    @SuppressLint("Range")
    public List<ImageModel> get(String sql, String ...selectArgs) {
        List<ImageModel> listImageModel = new ArrayList<>();
        Cursor cursor = db.rawQuery(sql, selectArgs);

        while (cursor.moveToNext()) {
            ImageModel imageModel = new ImageModel();
            imageModel.setId(cursor.getInt(cursor.getColumnIndex("id")));
            imageModel.setImage(byteToBitmap(cursor.getBlob(cursor.getColumnIndex("imageData"))));
            imageModel.setLocation(cursor.getString(cursor.getColumnIndex("location")));

            listImageModel.add(imageModel);
        }
        return listImageModel;
    }

    public List<ImageModel> getAllImage() {
        String sql = "SELECT * FROM Image ORDER BY id DESC";
        return get(sql);
    }

    public ImageModel getImageById(int id) {
        String sql = "SELECT * FROM Image WHERE id = ?";
        List<ImageModel> listImageModel = get(sql, String.valueOf(id));
        return listImageModel.get(0);
    }

    public long insert(ImageModel imageModel) {
        ContentValues values = new ContentValues();
        values.put("imageData", bitmapToByte(imageModel.getImage()));
        values.put("location", imageModel.getLocation());

        return db.insertOrThrow("Image", null, values);
    }

    public int getLastId() {
        String sql = "SELECT MAX(id) FROM Image";
        String args[] = {};
        Cursor cursor = db.rawQuery(sql, args);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public long update(ImageModel imageModel) {
        ContentValues values = new ContentValues();
        values.put("imageData", bitmapToByte(imageModel.getImage()));
        values.put("location", imageModel.getLocation());

        return db.update("Image", values, "id=?", new String[]{String.valueOf(imageModel.getId())});
    }

    public Bitmap byteToBitmap(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    public byte[] bitmapToByte(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

}
