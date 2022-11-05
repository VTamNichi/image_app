package com.hieplh.imageapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hieplh.imageapp.dao.ImageDAO;
import com.hieplh.imageapp.model.ImageModel;

public class ImageDetailActivity extends AppCompatActivity {

    ImageView imageView;
    TextView textLocation;
    Button backBtn;
    ImageDAO imageDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        imageDAO = new ImageDAO(this);

        imageView = findViewById(R.id.imageView);
        textLocation = findViewById(R.id.textLocation);
        backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(view -> {
            finish();
        });

        Intent intent = getIntent();
        int id = Integer.parseInt(intent.getExtras().get("IMAGE_ID").toString());
        ImageModel imageModel = imageDAO.getImageById(id);
        imageView.setImageBitmap(imageModel.getImage());
        if(imageModel.getLocation() == "") {
            textLocation.setText("Location: undefined");
        } else {
            textLocation.setText("Location: " + imageModel.getLocation());
        }
    }
}