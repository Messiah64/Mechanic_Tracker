package com.sp.mechanictracker.screens;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.sp.mechanictracker.adapters.Edit_Image_RecyclerAdapter;
import com.sp.mechanictracker.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Edit_Order extends AppCompatActivity {

    // Declare variables
    private RecyclerView recyclerView;
    private ArrayList<Uri> uriList = new ArrayList<>();
    private List<String> imageURLs = new ArrayList<>();
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private ImageView pick;
    private Edit_Image_RecyclerAdapter adapter;
    private List<String> documentNames = new ArrayList<>();
    private EditText phoneEdit, bicycleEdit, dateEdit, timeEdit, mechanicEdit, notesEdit;
    private TextView pidEdit, phoneNumToReplace, orderIDEdit;
    private Button buttonEdit;
    private Spinner deliveryModeEdit, packageTypeEdit;
    private String spinnerDelivery = " ", spinnerPackage = " ";
    private static final int READ_PERMISSION = 101;
    private List<String> deliveryOptions = new ArrayList<>();
    private List<String> packageOptions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_order);

        // Initialize Firebase components
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind views
        recyclerView = findViewById(R.id.edit_recyclerview_gallery_images);
        pick = findViewById(R.id.editpickImages);
        phoneEdit = findViewById(R.id.phoneNumberEdit);
        phoneNumToReplace = findViewById(R.id.PhoneNumberToReplace);
        bicycleEdit = findViewById(R.id.bicycleNumberEdit);
        dateEdit = findViewById(R.id.datePickEdit);
        timeEdit = findViewById(R.id.timePickEdit);
        mechanicEdit = findViewById(R.id.mechanicEdit);
        notesEdit = findViewById(R.id.notesEdit);
        pidEdit = findViewById(R.id.PIDEdit);
        orderIDEdit = findViewById(R.id.OrderIDEdit);
        buttonEdit = findViewById(R.id.submitBtnEdit);
        deliveryModeEdit = findViewById(R.id.DeliveryPickEdit);
        packageTypeEdit = findViewById(R.id.PackagePickEdit);

        // Set listeners
        dateEdit.setOnClickListener(view -> openCalendarDialog());
        pick.setOnClickListener(view -> pickImages());

        // Initialize RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new Edit_Image_RecyclerAdapter(uriList);
        recyclerView.setAdapter(adapter);

        // Set spinner adapters and listeners
        setupSpinnerAdapters();

        // Retrieve intent extras and populate fields
        populateFieldsFromIntent();

        // Set button click listener
        buttonEdit.setOnClickListener(view -> uploadImagesToFirebaseStorage());
    }

    private void setupSpinnerAdapters() {
        deliveryOptions.add("Delivery");
        deliveryOptions.add("Meet Up");
        ArrayAdapter<String> adapterDeliveryPicker = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, deliveryOptions);
        adapterDeliveryPicker.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deliveryModeEdit.setAdapter(adapterDeliveryPicker);

        packageOptions.add("Gold");
        packageOptions.add("Premium");
        packageOptions.add("Safety");
        packageOptions.add("Rec");
        packageOptions.add("Nil");
        ArrayAdapter<String> adapterPackagePicker = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, packageOptions);
        adapterPackagePicker.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        packageTypeEdit.setAdapter(adapterPackagePicker);
    }

    private void populateFieldsFromIntent() {
        Intent intent = getIntent();
        phoneEdit.setText(intent.getStringExtra("phone"));
        phoneNumToReplace.setText(intent.getStringExtra("phone"));
        bicycleEdit.setText(intent.getStringExtra("bicycle"));
        dateEdit.setText(intent.getStringExtra("date"));
        timeEdit.setText(intent.getStringExtra("time"));
        mechanicEdit.setText(intent.getStringExtra("mechanic"));
        notesEdit.setText(intent.getStringExtra("notes"));
        pidEdit.setText(intent.getStringExtra("pid"));
        orderIDEdit.setText(intent.getStringExtra("orderID"));
        // Get the order ID from intent
        String orderId = intent.getStringExtra("orderID");

        // Retrieve image URLs from Firestore
        FirebaseFirestore.getInstance().collection("Orders")
                .document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> imageUrls = (List<String>) documentSnapshot.get("Images");
                        if (imageUrls != null) {
                            for (String url : imageUrls) {
                                uriList.add(Uri.parse(url));
                            }
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch image data", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error fetching image data", e);
                });
    }

    private void openCalendarDialog() {
        DatePickerDialog dialog = new DatePickerDialog(this, (datePicker, year, month, day) -> {
            dateEdit.setText(String.valueOf(day) + "." + String.valueOf(month + 1) + "." + String.valueOf(year));
        }, 2024, 0, 15);

        dialog.show();
    }

    private void pickImages() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), 1);
    }

    private void uploadImagesToFirebaseStorage() {
        /*
        for (Uri imageUri : uriList) {
            String filename = UUID.randomUUID().toString();
            StorageReference imageRef = storage.getReference().child("images/" + filename);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            imageURLs.add(uri.toString());
                            adapter.notifyDataSetChanged();

                        });
                    })
                    .addOnFailureListener(exception -> {
                        Toast.makeText(Edit_Order.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        }

         */

        getOldCustomerData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            if (data != null) {
                if (data.getClipData() != null) {
                    ClipData clipData = data.getClipData();

                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        Uri imageUri = clipData.getItemAt(i).getUri();
                        uriList.add(imageUri);
                    }
                } else if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    uriList.add(imageUri);
                }

                adapter.notifyDataSetChanged();
            }
        }
    }

    private void getOldCustomerData() {
        String phoneNumber = phoneNumToReplace.getText().toString();
        CollectionReference pastOrdersRef = db.collection("Existing Customers").document(phoneNumber).collection("Past OrderID");

        pastOrdersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    documentNames.add(document.getId());
                }
                updateToNewCustomer();
            } else {
                Toast.makeText(Edit_Order.this, "Can't get documents", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateToNewCustomer() {
        DocumentReference customerRef = db.collection("Existing Customers").document(phoneEdit.getText().toString());

        customerRef.set(Collections.singletonMap("PID", pidEdit.getText().toString()))
                .addOnSuccessListener(aVoid -> {
                    CollectionReference pastOrderRef = customerRef.collection("Past OrderID");
                    Map<String, Object> orderData = new HashMap<>();
                    orderData.put("IGNORE", "");

                    for (String docName : documentNames) {
                        pastOrderRef.document(docName).set(orderData)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        updateNewValuesToDatabase();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Edit_Order.this, "Can't update new data", Toast.LENGTH_LONG).show();
                });
    }

    private void updateNewValuesToDatabase() {
        String phoneString = phoneEdit.getText().toString();
        String pidString = pidEdit.getText().toString();
        String bicycleString = bicycleEdit.getText().toString();
        String dateString = dateEdit.getText().toString();
        String timeString = timeEdit.getText().toString();
        String mechanicString = mechanicEdit.getText().toString();
        String notesString = notesEdit.getText().toString();
        String orderString = orderIDEdit.getText().toString();
        String status = getIntent().getStringExtra("status");

        Map<String, Object> data = new HashMap<>();
        data.put("Phone", phoneString);
        data.put("BicycleDetails", bicycleString);
        data.put("DeliveryMethod", spinnerDelivery);
        data.put("Date", dateString);
        data.put("Time", timeString);
        data.put("Mechanic", mechanicString);
        data.put("Package", spinnerPackage);
        data.put("Notes", notesString);
        data.put("PID", pidString);
        data.put("OrderID", orderString);
        data.put("Images", imageURLs);
        data.put("Status", status);

        db.collection("Orders")
                .document(orderString)
                .update(data)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Edit_Order.this, "OrderID " + orderString + " has been updated. Status is " + status, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Edit_Order.this, Ongoing_Orders.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Log.e("Error", "Error updating document", e);
                    Toast.makeText(Edit_Order.this, "Error updating document", Toast.LENGTH_LONG).show();
                });
    }
}
