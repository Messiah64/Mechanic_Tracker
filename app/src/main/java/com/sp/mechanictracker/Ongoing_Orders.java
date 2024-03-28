package com.sp.mechanictracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Collections;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Ongoing_Orders extends AppCompatActivity implements OrderAdapter.OnInfoOptionClickListener{

    private ImageView repair, add, complete;
    private FirebaseStorage storage;
    private FirebaseFirestore db;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        subscribeToNewsTopic();

        // Firebase Firestore & Storage Instances
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Bottom Nav Bar Begin
        repair = findViewById(R.id.home_repair);
        add = findViewById(R.id.home_add);
        complete = findViewById(R.id.home_complete);

        recyclerView = findViewById(R.id.recyclerView);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Ongoing_Orders.this, Add_Order.class);
                startActivity(intent);
            }
        });

        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Ongoing_Orders.this, Completed_Orders.class);
                startActivity(intent);
            }
        });
        // Bottom Nav Bar Ends

        fetchOrdersFromFirestore();
    }

    public static void subscribeToNewsTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("News")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Subscription successful
                        // You may want to notify the user or update UI accordingly
                    } else {
                        // Subscription failed
                        // Handle the error
                    }
                });
    }

    private void fetchOrdersFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference ordersRef = db.collection("Orders");

        ordersRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<Order> ordersList = new ArrayList<>();
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Order order = documentSnapshot.toObject(Order.class);
                    ordersList.add(order);
                    Log.d("Order", order.toString());
                }
                // Hardcoded example values
                List<String> exampleValues = new ArrayList<>();
                exampleValues.add("OTW");
                exampleValues.add("Pending");
                exampleValues.add("Repairing");
                exampleValues.add("Arrange");
                exampleValues.add("Issues");
                exampleValues.add("Completed");

                populateRecyclerView(ordersList, exampleValues);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Firestore", "Error fetching orders", e);
            }
        });
    }

    private void populateRecyclerView(List<Order> ordersList, List<String> statusList) {
        // Sort ordersList by date in ascending order
        Collections.sort(ordersList, new Comparator<Order>() {
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

            @Override
            public int compare(Order order1, Order order2) {
                try {
                    Date date1 = dateFormat.parse(order1.getDate());
                    Date date2 = dateFormat.parse(order2.getDate());
                    return date1.compareTo(date2);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });

        // Create and set adapter with info option click listener
        OrderAdapter adapter = new OrderAdapter(ordersList, statusList, recyclerView, new OrderAdapter.OnInfoOptionClickListener() {
            @Override
            public void onInfoOptionClicked(Order order) {
                // Show popup dialog with order information
                showInfoPopupDialog(order);
            }

            @Override
            public void onDeleteOptionClicked(Order order, int position) {
                // Delete the order from Firestore and notify adapter
                deleteOrderFromOrders(order.getOrderID(), position);
            }
        });


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onInfoOptionClicked(Order order) {
        // Show popup dialog with order information
        showInfoPopupDialog(order);
    }

    @Override
    public void onDeleteOptionClicked(Order order, int position) {
        // Call the method to delete the order
        deleteOrderFromOrders(order.getOrderID(), position);
    }

    private void deleteOrderFromOrders(String orderID, int position) {
        // Your existing code to delete the order
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference ordersRef = db.collection("Orders");

        ordersRef.document(orderID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("OrderAdapter", "Order deleted from Orders collection");
                        // Notify adapter about item removal
                        recyclerView.getAdapter().notifyItemRemoved(position);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("OrderAdapter", "Error deleting order from Orders collection", e);
                    }
                });
    }



    private void showInfoPopupDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Order Information");

        // Create a ScrollView and wrap the message inside it
        ScrollView scrollView = new ScrollView(this);
        TextView messageTextView = new TextView(this);
        messageTextView.setText("Order ID: " + order.getOrderID() + "\nOrder PID: " + order.getPID() + "\nPhone Number: " + order.getPhone() + "\nNotes: " + order.getNotes());
        // Set padding to provide some space around the text
        int padding = (int) getResources().getDimension(R.dimen.alert_dialog_padding);
        messageTextView.setPadding(padding, padding, padding, padding);
        scrollView.addView(messageTextView);

        builder.setView(scrollView);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}