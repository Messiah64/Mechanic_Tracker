
package com.sp.mechanictracker;
import android.app.Notification;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        private TextView status;
        private CardView statusColour;
        private ImageButton imageButtonMenu;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            firstImage = itemView.findViewById(R.id.firstImage);
            retrievedPID = itemView.findViewById(R.id.retrievedPID);
            retrievedPhoneNumber = itemView.findViewById(R.id.retrievedPhoneNumber);

            imageButtonMenu = itemView.findViewById(R.id.imageButtonMenu);
            imageButtonMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menuButtons(view);
                }
            });


            status = itemView.findViewById(R.id.StatusText);
            statusColour = itemView.findViewById(R.id.StatusCard);

            // Injected Logic, into class LMAO
            retrievedStatus = itemView.findViewById(R.id.retrievedStatus);
            retrievedStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    String selectedStatus = statusList.get(position);
                    if ("Completed".equals(selectedStatus)) {
                        String orderID = retrievedOrderID.getText().toString();
                        updateStatusForCompletedOnly(orderID, selectedStatus);
                        Log.d("STATUS CHANGED", selectedStatus);

                    } if ("Issues".equals(selectedStatus)) {
                        String orderID = retrievedOrderID.getText().toString();
                        updateStatus(orderID, selectedStatus);
                        Log.d("STATUS CHANGED", selectedStatus);

                    } if ("Arrange".equals(selectedStatus)) {
                        String orderID = retrievedOrderID.getText().toString();
                        updateStatus(orderID, selectedStatus);
                        Log.d("STATUS CHANGED", selectedStatus);

                    } if ("Repairing".equals(selectedStatus)) {
                        String orderID = retrievedOrderID.getText().toString();
                        updateStatus(orderID, selectedStatus);
                        Log.d("STATUS CHANGED", selectedStatus);

                    } if ("Pending".equals(selectedStatus)) {
                        String orderID = retrievedOrderID.getText().toString();
                        updateStatus(orderID, selectedStatus);
                        Log.d("STATUS CHANGED", selectedStatus);
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

        public void menuButtons(View v) {

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
                    } else if (itemId == R.id.delete_option) {
                        Log.d("Menu", "Delete Option");
                        // Handle delete option
                    } else if (itemId == R.id.info_option) {
                        Log.d("Menu", "Info Option");

                    } else if (itemId == R.id.whatsapp_option) {
                        Log.d("Menu", "Whatsapp Option");
                        // Handle WhatsApp option
                    }

                    return true;
                });

                // Show the popup menu
                popupMenu.show();
            }

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
                    .update("status", "Completed")
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

        private void updateStatus(String orderID, String newStatus) {
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
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("TAG", "Error updating document status", e);
                        }
                    });
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
                                    deleteOrderFromOrders(orderID);
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

            status.setText(order.getStatus());

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
    }
}
