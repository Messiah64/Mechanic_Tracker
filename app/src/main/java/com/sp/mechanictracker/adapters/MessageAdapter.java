package com.sp.mechanictracker.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.sp.mechanictracker.R;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> messages;
    private List<String> imageUrls;

    public MessageAdapter(@NonNull Context context, @NonNull List<String> messages, @NonNull List<String> imageUrls) {
        super(context, 0, messages);
        this.context = context;
        this.messages = messages;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.message_item, parent, false);
        }

        String message = messages.get(position);
        String imageUrl = imageUrls.get(position);

        TextView messageTextView = listItem.findViewById(R.id.messageTextView);
        ImageView imageView = listItem.findViewById(R.id.messageImageView);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            messageTextView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(Uri.parse(imageUrl))
                    .into(imageView);
        } else {
            messageTextView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            messageTextView.setText(message);
        }

        return listItem;
    }
}