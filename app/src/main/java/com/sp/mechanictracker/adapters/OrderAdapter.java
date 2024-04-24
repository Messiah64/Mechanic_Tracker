
package com.sp.mechanictracker.adapters;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.sp.mechanictracker.R;
import com.sp.mechanictracker.screens.Edit_Order;
import com.sp.mechanictracker.screens.Ongoing_Orders;
import com.sp.mechanictracker.screens.OrderMessages;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private OnOrderClickListener onOrderClickListener;

    private List<Order> ordersList;
    public List<String> statusList;
    private boolean isFirstItem = true;

    private RecyclerView recyclerView;

    private OnInfoOptionClickListener infoOptionClickListener;

    // Constructor
    public OrderAdapter(List<Order> ordersList, List<String> statusList, RecyclerView recyclerView, OnInfoOptionClickListener infoOptionClickListener, OnOrderClickListener onOrderClickListener) {
        this.ordersList = ordersList;
        this.statusList = statusList;
        this.recyclerView = recyclerView;
        this.infoOptionClickListener = infoOptionClickListener;
        this.onOrderClickListener = onOrderClickListener;
    }
    // Interface for Info option click listener
    public interface OnInfoOptionClickListener {
        void onInfoOptionClicked(Order order);
        void onDeleteOptionClicked(Order order, int position);
    }
    public interface OnOrderClickListener {
        void onOrderClicked(Order order);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            Order currentOrder = ordersList.get(position);
            holder.bind(currentOrder);
        } catch (NullPointerException e) {
            // Handle the null Order object here
            Log.e("OrderAdapter", "Null Order object at position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


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
        private TextView status;
        private CardView statusColour;
        private ImageButton imageButtonMenu;
        private TextView bicycleDetails;

        String Prev_status = " ";


        public ViewHolder(@NonNull View itemView) {



            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(itemView.getContext(), OrderMessages.class);
                    intent.putExtra("orderID", retrievedOrderID.getText().toString());
                    itemView.getContext().startActivity(intent);
                }
            });

            firstImage = itemView.findViewById(R.id.firstImage);
            retrievedPID = itemView.findViewById(R.id.retrievedPID);
            retrievedPhoneNumber = itemView.findViewById(R.id.retrievedPhoneNumber);

            bicycleDetails = itemView.findViewById(R.id.retrievedBicycleDetails);


            imageButtonMenu = itemView.findViewById(R.id.imageButtonMenu);
            imageButtonMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menuButtons(view, ordersList.get(getAdapterPosition()));
                }
            });


            status = itemView.findViewById(R.id.StatusText);
            status.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            statusColour = itemView.findViewById(R.id.StatusCard);

            Prev_status = status.getText().toString();


            // Injected Logic, into class LMAO
            retrievedStatus = itemView.findViewById(R.id.retrievedStatus);

            final AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            builder.setMessage("Are you sure you want to proceed?")
                    .setPositiveButton("Confirm", null) // Set the positive button click listener to null initially
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User Cancelled
                            String orderID = retrievedOrderID.getText().toString();
                            updateStatus(orderID, Prev_status);
                            Prev_status = "";
                        }
                    });

            final AlertDialog alertDialog = builder.create();

// Set the positive button click listener after creating the dialog
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String orderID = retrievedOrderID.getText().toString();
                    updateStatusForCompletedOnly(orderID, "Completed");
                    Log.d("STATUS CHANGED", "Completed");
                }
            });

            retrievedStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {


                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    String selectedStatus = statusList.get(position);
                    if ("Completed".equals(selectedStatus)) {
                        alertDialog.show(); // Show the dialog when "Completed" is selected
                    }
                    if (" ".equals(selectedStatus)) {
                        // Do nothing
                    } else {
                        String orderID = retrievedOrderID.getText().toString();
                        updateStatus(orderID, selectedStatus);

                        // Find the ViewHolder instance for the updated order and update the UI
                        int orderPosition = getAdapterPosition();
                        if (orderPosition != RecyclerView.NO_POSITION) {
                            OrderAdapter.ViewHolder viewHolder = (OrderAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(orderPosition);
                            if (viewHolder != null) {
                                // viewHolder.updateStatusUI(selectedStatus);
                            }
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    // Do nothing
                }
            });


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




        public void menuButtons(View v, Order order) {
            if (v.getId() == R.id.imageButtonMenu) {
                // Show popup menu
                PopupMenu popupMenu = new PopupMenu(itemView.getContext(), imageButtonMenu);
                popupMenu.inflate(R.menu.popup_menu);

                // Set listener for menu item clicks
                popupMenu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.edit_option) {
                        Log.d("Menu", "Edit Option");
                        // Handle edit option
                        // editOrder(order);
                    } else if (itemId == R.id.delete_option) {
                        Log.d("Menu", "Delete Option");
                        // Handle delete option
                        deleteOrderFromOrders(order.getOrderID());
                    } else if (itemId == R.id.info_option) {
                        Log.d("Menu", "Info Option");
                        // Handle info option
                        if (infoOptionClickListener != null) {
                            infoOptionClickListener.onInfoOptionClicked(order);
                        }
                    } else if (itemId == R.id.whatsapp_option) {
                        Log.d("Menu", "WhatsApp Option");
                        // Handle WhatsApp option
                    }
                    return true;
                });

                // Show the popup menu
                popupMenu.show();
            }
        }


        private void editOrder(Order order) {
            Intent intent = new Intent(itemView.getContext(), Edit_Order.class);
            intent.putExtra("phone", order.getPhone());
            intent.putExtra("bicycle", order.getBicycleDetails());
            intent.putExtra("date", order.getDate());
            intent.putExtra("time", order.getTime());
            intent.putExtra("mechanic", order.getMechanic());
            intent.putExtra("notes", order.getNotes());
            intent.putExtra("pid", order.getPID());
            intent.putExtra("phone_replace", order.getPhone());
            intent.putExtra("delivery", order.getDeliveryMethod());
            intent.putExtra("package", order.getPackage());
            intent.putExtra("orderID", order.getOrderID());
            intent.putExtra("status", order.getStatus());

            // Image Data URLS
            Gson gson = new Gson();
            String jsonString = gson.toJson(order.getImages());
            intent.putExtra("images", jsonString);
            itemView.getContext().startActivity(intent);
        }


        public boolean onMenuItemClick(MenuItem item) {
            int itemId = item.getItemId();
            if (itemId == R.id.edit_option) {
                // Handle edit option
            } else if (itemId == R.id.delete_option) {
                // Handle delete option
            } else if (itemId == R.id.info_option) {
                // Handle info option
                if (infoOptionClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Order order = ordersList.get(position);
                        infoOptionClickListener.onInfoOptionClicked(order);
                    }
                }
            } else if (itemId == R.id.whatsapp_option) {
                // Handle WhatsApp option
            }
            return true;
        }


        private void updateStatusForCompletedOnly(String orderID, String newStatus) {


            // Get instance of Firebase Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Reference to the document in the "Orders" collection
            DocumentReference orderRef = db.collection("Orders").document(orderID);

            // Create a map with the updated status field
            Map<String, Object> updates = new HashMap<>();
            updates.put("Status", newStatus);

            // Update the document with the new status field
            orderRef.update(updates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("TAG", "Document status updated successfully");
                            sendOrderToCompleted(orderID);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("TAG", "Error updating document status", e);
                        }
                    });
        }

        public void hardUpdateCompletedStatus(String orderID) {
            // Get reference to the document in the "Completed" collection
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference completedOrderRef = db.collection("Completed").document(orderID);

            // Update the "Status" field to "Completed"
            completedOrderRef
                    .update("Status", "Completed")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Status updated successfully
                                // You can handle success as per your requirements
                            } else {
                                // Failed to update status
                                Exception e = task.getException();
                                if (e != null) {
                                    Log.e("YourClassName", "Error updating status: " + e.getMessage());
                                }
                            }
                        }
                    });
        }

        private void updateStatusForOTW(String orderID, String newStatus) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Reference to the document in the "Orders" collection
            DocumentReference orderRef = db.collection("Orders").document(orderID);

            // Create a map with the updated status field
            Map<String, Object> updates = new HashMap<>();
            updates.put("Status", newStatus);

            // Update the document with the new status field
            orderRef.update(updates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("TAG", "Document status updated successfully");

                            // Update the Order object in the ordersList
                            int orderPosition = getAdapterPosition();
                            if (orderPosition != RecyclerView.NO_POSITION) {
                                Order order = ordersList.get(orderPosition);
                                order.setStatus(newStatus); // Update the status in the Order object

                                // Notify the adapter about the data change
                                notifyItemChanged(orderPosition);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("TAG", "Error updating document status", e);
                        }
                    });
        }

        private void updateStatus(String orderID, String newStatus) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            // Reference to the document in the "Orders" collection
            DocumentReference orderRef = db.collection("Orders").document(orderID);

            // Create a map with the updated status field
            Map<String, Object> updates = new HashMap<>();
            updates.put("Status", newStatus);

            // Update the document with the new status field
            orderRef.update(updates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("TAG", "Document status updated successfully");

                            // Update the Order object in the ordersList
                            int orderPosition = getAdapterPosition();
                            if (orderPosition != RecyclerView.NO_POSITION) {
                                Order order = ordersList.get(orderPosition);
                                order.setStatus(newStatus); // Update the status in the Order object

                                // Notify the adapter about the data change
                                notifyItemChanged(orderPosition);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("TAG", "Error updating document status", e);
                        }
                    });


        }

        private void refreshScreen() {
            Intent intent = new Intent(itemView.getContext(), Ongoing_Orders.class);
            itemView.getContext().startActivity(intent);
        }

        private void sendOrderToCompleted(String orderID) {
            // Here you can send the order details to Firebase Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference completedRef = db.collection("Completed");

            // Get the order details from the ordersList using the orderID
            for (Order order : ordersList) {
                if (order.getOrderID().equals(orderID)) {
                    // Create a new document with the orderID as the document name
                    completedRef.document(orderID)
                            .set(order)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("OrderAdapter", "Order sent to Completed collection");
                                    // Optionally, you can remove the order from the RecyclerView
                                    changeorderCompletionDate(order);

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("OrderAdapter", "Error sending order to Completed collection", e);
                                }
                            });
                    break;
                }
            }
        }


        public String getFormattedDate() {
            // Get today's date
            Date today = new Date();

            // Define the format
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.ENGLISH);

            // Format the date
            String formattedDate = dateFormat.format(today);

            return formattedDate.toUpperCase(); // Convert to uppercase as per your example
        }


        public void changeorderCompletionDate(Order order) {
            String currentDate = order.getDate();
            if(!currentDate.isEmpty()) {
                // get todays current date DD.MM
                currentDate = getFormattedDate();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Reference to the document in the "Orders" collection
                DocumentReference orderRef = db.collection("Orders").document(order.getOrderID().toString());

                // Create a map with the updated status field
                Map<String, Object> updates = new HashMap<>();
                updates.put("Date", currentDate);

                // Update the document with the new status field
                orderRef.update(updates)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("TAG", "Document Date Changed Successfully");
                                deleteOrderFromOrders(order.getOrderID().toString());
                                refreshScreen();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("TAG", "Error updating document status", e);
                            }
                        });
            }

        }



        private void deleteOrderFromOrders(String orderID) {
            // Here you can delete the document from the "Orders" collection
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference ordersRef = db.collection("Orders");

            ordersRef.document(orderID)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hardUpdateCompletedStatus(orderID);
                            Log.d("OrderAdapter", "Order deleted from Orders collection");
                            // Optionally, you can remove the order from the RecyclerView
                            int adapterPosition = getAdapterPosition();
                            if (adapterPosition != RecyclerView.NO_POSITION) {
                                ordersList.remove(adapterPosition);
                                notifyItemRemoved(adapterPosition);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("OrderAdapter", "Error deleting order from Orders collection", e);
                        }
                    });
        }

        public void bind(Order order) {
            // Set the first image (if available)
            if (order.getImages() != null && !order.getImages().isEmpty()) {
                Picasso.get().load(order.getImages().get(0)).into(firstImage);
            } else {
                // Set a placeholder image if no images available
                firstImage.setImageResource(R.drawable.nopictures);
            }

            if (order.getUpdatedStatus() != null) {
                status.setText(order.getUpdatedStatus());
            } else {
                status.setText(order.getStatus());
            }

            bicycleDetails.setText(order.getBicycleDetails());

            status.setText(order.getStatus());

            if (order.getStatus().toString().equals("OTW")) {
                // Create a GradientDrawable to set the border color and width
                GradientDrawable gradientDrawable1 = new GradientDrawable();
                gradientDrawable1.setShape(GradientDrawable.RECTANGLE);
                gradientDrawable1.setStroke(10, Color.parseColor("#FFFFA0")); // Border width and color
                gradientDrawable1.setColor(Color.parseColor("#FFFFA0")); // Background color (set to transparent to show border)
                statusColour.setBackground(gradientDrawable1);
                statusColour.invalidate();
            }
            if (order.getStatus().toString().equals("Pending")) {
                // Create a GradientDrawable to set the border color and width
                GradientDrawable gradientDrawable2 = new GradientDrawable();
                gradientDrawable2.setShape(GradientDrawable.RECTANGLE);
                gradientDrawable2.setStroke(10, Color.parseColor("#FFD7A0")); // Border width and color
                gradientDrawable2.setColor(Color.parseColor("#FFD7A0")); // Background color (set to transparent to show border)
                statusColour.setBackground(gradientDrawable2);
                statusColour.invalidate();
            }
            if (order.getStatus().toString().equals("Repairing")) {
                // Create a GradientDrawable to set the border color and width
                GradientDrawable gradientDrawable3 = new GradientDrawable();
                gradientDrawable3.setShape(GradientDrawable.RECTANGLE);
                gradientDrawable3.setStroke(10, Color.parseColor("#D6B4FC")); // Border width and color
                gradientDrawable3.setColor(Color.parseColor("#D6B4FC")); // Background color (set to transparent to show border)
                statusColour.setBackground(gradientDrawable3);
                statusColour.invalidate();
            }
            if (order.getStatus().toString().equals("Arrange")) {
                // Create a GradientDrawable to set the border color and width

                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                gradientDrawable.setStroke(10, Color.parseColor("#83f28f")); // Border width and color
                gradientDrawable.setColor(Color.parseColor("#83f28f")); // Background color (set to transparent to show border)
                statusColour.setBackground(gradientDrawable);
                statusColour.invalidate();
            }
            if (order.getStatus().toString().equals("Issues")) {
                // Create a GradientDrawable to set the border color and width
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                gradientDrawable.setStroke(10, Color.parseColor("#FFC0CB")); // Border width and color  FFC0CB
                gradientDrawable.setColor(Color.parseColor("#FFC0CB")); // Background color (set to transparent to show border)
                statusColour.setBackground(gradientDrawable);
                statusColour.invalidate();
            }
            if (order.getStatus().toString().equals("Completed")) {
                // Create a GradientDrawable to set the border color and width
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                gradientDrawable.setStroke(10, Color.parseColor("#90EE90")); // Border width and color  90EE90
                gradientDrawable.setColor(Color.parseColor("#90EE90")); // Background color (set to transparent to show border)
                statusColour.setBackground(gradientDrawable);
                statusColour.invalidate();
            }
            if (order.getStatus().toString().equals(null)) {
                // do nothing
            }


            retrievedPID.setText(order.getPID());

            // Set retrievedPhoneNumber
            retrievedPhoneNumber.setText(order.getPhone());

            // Set retrievedOrderID
            retrievedOrderID.setText(order.getOrderID());

            // Set retrievedDate
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
                Date date = inputFormat.parse((order.getDate()));

                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
                String outputDate = outputFormat.format(date).toUpperCase(); // Convert month to uppercase
                retrievedDate.setText(outputDate);

            } catch (ParseException e) {
                e.printStackTrace();
            }


            // Set retrievedTime
            retrievedTime.setText(order.getTime());

            // Set retrievedDeliveryMode based on DeliveryMethod
            if ("Delivery".equals(order.getDeliveryMethod())) {
                retrievedDeliveryMode.setImageResource(R.drawable.fast);
            } else {
                retrievedDeliveryMode.setImageResource(R.drawable.handshake);
                retrievedDeliveryMode.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
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
                retrievedPackage.setBackgroundColor(Color.parseColor("#FFC0CB"));
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

        @Override
        public void onClick(View v) {
            if (onOrderClickListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Order order = ordersList.get(position);
                    onOrderClickListener.onOrderClicked(order);
                }
            }
        }
    }
}
