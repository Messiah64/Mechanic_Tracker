package com.sp.mechanictracker;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private List<Order> ordersList;
    public List<String> statusList;
    private boolean isFirstItem = true;

    public OrderAdapter(List<Order> ordersList, List<String> statusList) {
        this.ordersList = ordersList;
        this.statusList = statusList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = ordersList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView firstImage;

        private TextView retrievedPhoneNumber;
        private Spinner retrievedStatus;
        private TextView retrievedOrderID;
        private TextView retrievedPID;
        private TextView retrievedDate;
        private TextView retrievedTime;
        private ImageView retrievedDeliveryMode;
        private Button retrievedPackage;
        private TextView retrievedMechanic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            firstImage = itemView.findViewById(R.id.firstImage);
            retrievedPID = itemView.findViewById(R.id.retrievedPID);
            retrievedPhoneNumber = itemView.findViewById(R.id.retrievedPhoneNumber);
            retrievedStatus = itemView.findViewById(R.id.retrievedStatus);
            retrievedOrderID = itemView.findViewById(R.id.retrievedOrderID);
            retrievedDate = itemView.findViewById(R.id.retrievedDate);
            retrievedTime = itemView.findViewById(R.id.retrievedTime);
            retrievedDeliveryMode = itemView.findViewById(R.id.retrievedDeliveryMode);
            retrievedPackage = itemView.findViewById(R.id.retrievedPackage);
            retrievedMechanic = itemView.findViewById(R.id.retrievedMechanic);

            // Create ArrayAdapter for status Spinner
            ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(itemView.getContext(),
                    android.R.layout.simple_spinner_item, statusList);
            statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            retrievedStatus.setAdapter(statusAdapter);
        }

        public void bind(Order order) {
            // Set the first image (if available)
            if (order.getImages() != null && !order.getImages().isEmpty()) {
                Picasso.get().load(order.getImages().get(0)).into(firstImage);
            } else {
                // Set a placeholder image if no images available
                firstImage.setImageResource(R.drawable.ic_launcher_background);
            }

            retrievedPID.setText(order.getPID());

            // Set retrievedPhoneNumber
            retrievedPhoneNumber.setText(order.getPhone());

            // Set retrievedOrderID
            retrievedOrderID.setText(order.getOrderID());

            // Set retrievedDate
            retrievedDate.setText(order.getDate());

            // Set retrievedTime
            retrievedTime.setText(order.getTime());

            // Set retrievedDeliveryMode based on DeliveryMethod
            if ("Delivery".equals(order.getDeliveryMethod())) {
                retrievedDeliveryMode.setImageResource(R.drawable.fast);
            } else {
                retrievedDeliveryMode.setImageResource(R.drawable.handshake);
            }

            // Set retrievedPackage text
            if (order.getPackage().equals("Gold")) {
                retrievedPackage.setText(order.getPackage());
                retrievedPackage.setBackgroundColor(Color.parseColor("#FFD700"));
            }
            if (order.getPackage().equals("Premium")) {
                retrievedPackage.setText(order.getPackage());
                retrievedPackage.setBackgroundColor(Color.parseColor("#C0C0C0"));
            }
            if (order.getPackage().equals("Safety")) {
                retrievedPackage.setText(order.getPackage());
                retrievedPackage.setBackgroundColor(Color.parseColor("#CD7F32"));
            }
            if (order.getPackage().equals("Rec")) {
                retrievedPackage.setText(order.getPackage());
                retrievedPackage.setBackgroundColor(Color.parseColor("#C0C0C0"));
            }
            if (order.getPackage().equals("Nil")) {
                retrievedPackage.setText(order.getPackage());
                retrievedPackage.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }

            // Set retrievedMechanic
            retrievedMechanic.setText("by " + order.getMechanic());
            // Set status for the first item from getStatus() method
            if (isFirstItem) {
                retrievedStatus.setSelection(statusList.indexOf(order.getStatus()));
                isFirstItem = false;
            }
        }
    }
}
