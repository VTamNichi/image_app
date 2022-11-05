package com.hieplh.imageapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.hieplh.imageapp.adapter.Adaptery;
import com.hieplh.imageapp.dao.ImageDAO;
import com.hieplh.imageapp.model.ImageModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    FusedLocationProviderClient fusedLocationProviderClient;
    String[] testData = {"https://upload.wikimedia.org/wikipedia/commons/4/47/PNG_transparency_demonstration_1.png",
            "https://images.ctfassets.net/hrltx12pl8hq/7yQR5uJhwEkRfjwMFJ7bUK/dc5" +
                    "2a0913e8ff8b5c276177890eb0129/offset_comp_772626-opt.jpg",
            };
    RecyclerView recyclerView;
    Button takePhotoBtn;
    Button addBtn;
    boolean flag = true;
    List<ImageModel> listImageModel = new ArrayList<>();
    ImageDAO imageDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageDAO = new ImageDAO(this);

        recyclerView = findViewById(R.id.recycleView);

        takePhotoBtn = findViewById(R.id.takePhotoBtn);
        addBtn = findViewById(R.id.addImage);

        takePhotoBtn.setOnClickListener(view -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 100);
        });
        addBtn.setOnClickListener(view -> {
            openAddDialog();
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        loadListImage();
    }

    private void openAddDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_dialog);

        Window window = dialog.getWindow();
        if(window == null) {
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);

        dialog.setCancelable(true);

        EditText textURL = dialog.findViewById(R.id.textURL);
        Button addImageBtn = dialog.findViewById(R.id.addImageBtn);

        addImageBtn.setOnClickListener(view -> {
            CountDownLatch latchDialog = new CountDownLatch(1);
            String url = textURL.getText().toString();
            if(url.length() == 0) {
                textURL.setError("This field cans not null");
            } else {
                new FetchImage(url, latchDialog).start();
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Getting your picture");
                progressDialog.setCancelable(false);
                progressDialog.show();
                try {
                    latchDialog.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                if(flag) {
                    Toast.makeText(MainActivity.this, "Add image successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, url + " is invalid url", Toast.LENGTH_SHORT).show();
                }
                loadListImage();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void loadListImage() {
        listImageModel = imageDAO.getAllImage();
        Adaptery adaptery = new Adaptery(this, listImageModel, imageDAO);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adaptery);
    }

    private void getImageLocation() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<Location> lastLocation = fusedLocationProviderClient.getLastLocation();

            lastLocation.addOnSuccessListener(location -> {
                if(location != null) {
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        imageDAO.tempLocation = addresses.get(0).getAddressLine(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == -1) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            getImageLocation();
            ImageModel imageModel = new ImageModel();
            imageModel.setImage(photo);
            imageDAO.insert(imageModel);
            loadListImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 100) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImageLocation();
            } else {
                Toast.makeText(MainActivity.this, "Required permission", Toast.LENGTH_SHORT).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    class FetchImage extends Thread{
        String URL;
        Bitmap bitmap;
        CountDownLatch startSignal;

        FetchImage(String URL, CountDownLatch startSignal){
            this.URL = URL;
            this.startSignal = startSignal;
        }

        @Override
        public void run() {
            flag = true;
            InputStream inputStream;
            try {
                inputStream = new URL(URL).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
                if(bitmap != null) {
                    ImageModel imageModel = new ImageModel();
                    imageModel.setImage(bitmap);
                    imageDAO.insert(imageModel);
                } else {
                    flag = false;
                    Toast.makeText(MainActivity.this, URL + " is invalid url", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                flag = false;
            }
            startSignal.countDown();
        }
    }
}