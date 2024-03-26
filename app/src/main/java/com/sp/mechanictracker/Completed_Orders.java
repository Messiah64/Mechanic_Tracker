package com.sp.mechanictracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class Completed_Orders extends AppCompatActivity {

    private ImageView repair, add, complete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_orders);

        // Bottom Nav Bar Begin
        repair = findViewById(R.id.completed_repair);
        add = findViewById(R.id.completed_add);
        complete = findViewById(R.id.completed_complete);

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
    }
}