package com.sp.mechanictracker.adapters;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sp.mechanictracker.R;

import java.util.ArrayList;
public class Edit_Image_RecyclerAdapter extends RecyclerView.Adapter<Edit_Image_RecyclerAdapter.ViewHolder>{
    private ArrayList<Uri> uriArrayList;

    public Edit_Image_RecyclerAdapter(ArrayList<Uri> uriArrayList) {
        this.uriArrayList = uriArrayList;
    }
    @NonNull
    @Override
    public Edit_Image_RecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.custom_edit_single_image,parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Edit_Image_RecyclerAdapter.ViewHolder holder, int position) {
        Uri imageUrl = uriArrayList.get(position);
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .into(holder.imageView);

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uriArrayList.remove(imageUrl);
                notifyDataSetChanged();
            }
        });
    }


    @Override
    public int getItemCount() {
        return uriArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView, delete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageToEdit);
            delete = itemView.findViewById(R.id.delete);
        }
    }
}
