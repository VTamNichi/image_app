package com.hieplh.imageapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hieplh.imageapp.ImageDetailActivity;
import com.hieplh.imageapp.R;
import com.hieplh.imageapp.dao.ImageDAO;
import com.hieplh.imageapp.model.ImageModel;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class Adaptery extends RecyclerView.Adapter<Adaptery.MyViewHolder> {

    private Context mContext;
    private List<ImageModel> mImageModel;
    private RecyclerView recyclerView;
    private ImageDAO imageDAO;

    public Adaptery(Context mContext, List<ImageModel> mImageModel, ImageDAO imageDAO) {
        this.mContext = mContext;
        this.mImageModel = mImageModel;
        this.imageDAO = imageDAO;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        v = inflater.inflate(R.layout.image_item, parent, false);

        recyclerView = (RecyclerView) parent;
        v.setOnClickListener(mOnClickListener);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.img.setImageBitmap(mImageModel.get(position).getImage());
    }

    @Override
    public int getItemCount() {
        return mImageModel.size();
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int itemPosition = recyclerView.getChildLayoutPosition(view);
            if(imageDAO.tempLocation != "" && !imageDAO.tempLocation.isEmpty()) {
                System.out.println("kkk");
                ImageModel imageModel = imageDAO.getImageById(imageDAO.getLastId());
                imageModel.setLocation(imageDAO.tempLocation);
                imageDAO.update(imageModel);
                imageDAO.tempLocation = "";
            }
            Intent intent = new Intent(mContext, ImageDetailActivity.class);
            intent.putExtra("IMAGE_ID", mImageModel.get(itemPosition).getId());
            mContext.startActivity(intent);
        }
    };

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imageViewItem);
        }
    }
}
