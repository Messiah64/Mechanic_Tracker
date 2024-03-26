package com.sp.mechanictracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Completed_Orders extends AppCompatActivity {

    private ImageView repair, add, complete;
    private FirebaseStorage storage;
    private FirebaseFirestore db;

    RecyclerView recyclerView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_orders);

        // Bottom Nav Bar Begin
        repair = findViewById(R.id.completed_repair);
        add = findViewById(R.id.completed_add);
        complete = findViewById(R.id.completed_complete);

        recyclerView = findViewById(R.id.recyclerViewCompleted);

        repair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Completed_Orders.this, Ongoing_Orders.class);
                startActivity(intent);
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Completed_Orders.this, Add_Order.class);
                startActivity(intent);
            }
        });
        // Bottom Nav Bar Ends

        fetchOrdersFromFirestore();
    }


    private void fetchOrdersFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference ordersRef = db.collection("Completed");

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
                exampleValues.add("Complete");

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