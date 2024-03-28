package com.sp.mechanictracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Collections;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

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

public class Ongoing_Orders extends AppCompatActivity {

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

        // Create and set adapter
        OrderAdapter adapter = new OrderAdapter(ordersList, statusList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }



}