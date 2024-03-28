package com.sp.mechanictracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Edit_Order extends AppCompatActivity {


    private FirebaseStorage storage;
    private FirebaseFirestore db;

    public  List<String> documentNames = new ArrayList<>();


    // Variables: populate thru pushExtra

    EditText phoneEdit, bicycleEdit, dateEdit, timeEdit, mechanicEdit, notesEdit;
    String phoneString=" ", pidString=" ", bicyleString=" ", dateString=" ", timeString=" ", mechanicString=" ", notesString=" ", orderString=" ", status = " ";
    TextView pidEdit, phoneNumToReplace, orderIDEdit;
    Button buttonEdit;
    Spinner deliveryModeEdit, packageTypeEdit;

    String spinnerDelivery = " ", spinnerPackage = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_order);

        orderIDEdit = findViewById(R.id.OrderIDEdit);
        phoneEdit = findViewById(R.id.phoneNumberEdit);
        phoneNumToReplace = findViewById(R.id.PhoneNumberToReplace); // will be invisible
        bicycleEdit = findViewById(R.id.bicycleNumberEdit);

        dateEdit = findViewById(R.id.datePickEdit);
        dateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCalenderDialog();
            }
        });

        timeEdit = findViewById(R.id.timePickEdit);
        mechanicEdit = findViewById(R.id.mechanicEdit);
        notesEdit = findViewById(R.id.notesEdit);
        pidEdit = findViewById(R.id.PIDEdit);

        // Set text from putExtra
        // Retrieve the phone number passed from the previous activity
        Intent intent = getIntent();
        phoneEdit.setText(intent.getStringExtra("phone"));
        phoneNumToReplace.setText(intent.getStringExtra("phone")); // Duplicate key to match original DB
        bicycleEdit.setText(intent.getStringExtra("bicycle"));
        dateEdit.setText(intent.getStringExtra("date"));
        timeEdit.setText(intent.getStringExtra("time"));
        mechanicEdit.setText(intent.getStringExtra("mechanic"));
        notesEdit.setText(intent.getStringExtra("notes"));
        pidEdit.setText(intent.getStringExtra("pid"));
        orderIDEdit.setText(intent.getStringExtra("orderID"));
        status = intent.getStringExtra("status");


        buttonEdit = findViewById(R.id.submitBtnEdit);
        deliveryModeEdit = findViewById(R.id.DeliveryPickEdit);
        packageTypeEdit = findViewById(R.id.PackagePickEdit);

        List<String> deliveryOptions = new ArrayList<>();
        deliveryOptions.add("Delivery");
        deliveryOptions.add("Meet Up");
        ArrayAdapter<String> adapterDeliveryPicker = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, deliveryOptions);
        adapterDeliveryPicker.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        deliveryModeEdit.setAdapter(adapterDeliveryPicker);
        // Set an OnItemSelectedListener to handle the selection event
        deliveryModeEdit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                // Retrieve the selected item
                spinnerDelivery = (String) parent.getItemAtPosition(position);
                // Display a toast with the selected item
                // Toast.makeText(Add_Order.this, "Selected: " + spinnerDeliveryModeValue, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        List<String> PackageOptions = new ArrayList<>();
        PackageOptions.add("Gold");
        PackageOptions.add("Premium");
        PackageOptions.add("Safety");
        PackageOptions.add("Rec");
        PackageOptions.add("Nil");
        CustomSpinnerAdapter adapterPackagePicker = new CustomSpinnerAdapter(this, android.R.layout.simple_spinner_item, PackageOptions);
        adapterPackagePicker.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        packageTypeEdit.setAdapter(adapterPackagePicker);
        // Set an OnItemSelectedListener to handle the selection event
        packageTypeEdit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                // Retrieve the selected item
                spinnerPackage = (String) parent.getItemAtPosition(position);
                // Display a toast with the selected item
                // Toast.makeText(Add_Order.this, "Selected: " + spinnerPackageTypeValue, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });


        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOldCustomerData();
            }
        });

    }

    public void openCalenderDialog() {
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                dateEdit.setText(String.valueOf(day) + "." + String.valueOf(month+1) + "." + String.valueOf(year));
            }
        }, 2024, 0, 15);

        dialog.show();
    }

    public void updateNewValuesToDatabase() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //String phoneString=" ", pidString=" ", bicyleString=" ", dateString=" ", timeString=" ", mechanicString=" ", notesString=" ", orderString=" ";

        phoneString = phoneEdit.getText().toString();
        pidString = pidEdit.getText().toString();
        bicyleString = bicycleEdit.getText().toString();
        dateString = dateEdit.getText().toString();
        timeString = timeEdit.getText().toString();
        mechanicString = mechanicEdit.getText().toString();
        notesString = notesEdit.getText().toString();
        orderString = orderIDEdit.getText().toString();


        // Go to Orders -> OrderID -> dump shit here
        Map<String, Object> data = new HashMap<>();
        data.put("Phone", phoneString);
        data.put("BicycleDetails", bicyleString);
        data.put("DeliveryMethod", spinnerDelivery);
        data.put("Date", dateString);
        data.put("Time", timeString);
        data.put("Mechanic", mechanicString);
        data.put("Package", spinnerPackage);
        data.put("Notes", notesString);
        data.put("PID", pidString);
        data.put("OrderID", orderString); // Just in case putting OrderID inside for easy referencing later on
        // data.put("Images", arrayList);
        data.put("Status", status); // Default status

        // Add the subcollection, its document, and fields to Firestore
        db.collection("Orders")
                .document(orderString)
                .update(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Edit_Order.this, "OrderID " + orderString + " has been updated", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Edit_Order.this, Ongoing_Orders.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Log.e("Somethings' fucked up", "Error updating document", e);
                    }
                });

    }
    public void getOldCustomerData() {
        String phoneNumber = phoneNumToReplace.getText().toString(); // Assuming phoneNumToReplace is your EditText
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the subcollection "Past OrderID" under the document with the phone number
        CollectionReference pastOrdersRef = db.collection("Existing Customers").document(phoneNumber).collection("Past OrderID");

        // Retrieve the documents in the subcollection
        pastOrdersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        // Add the document ID to the list
                        documentNames.add(document.getId());
                    }
                    // Here you have the ArrayList containing the document names from the subcollection
                    // You can do further processing with this list as needed
                    // For example, you can pass it to another function or display it in your UI

                    updateToNewCustomer();
                } else {
                    // Handle errors

                    Toast.makeText(Edit_Order.this, "cant get documents", Toast.LENGTH_LONG).show();


                }
            }
        });
    }

    public void updateToNewCustomer() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the document in the "Existing Customers" collection identified by the phone number
        DocumentReference customerRef = db.collection("Existing Customers").document(phoneEdit.getText().toString());

        // Update the "PID" field
        customerRef
                .set(Collections.singletonMap("PID", pidEdit.getText().toString()))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Field update successful
                        // Now, create the subcollection "Past OrderID" and add documents

                        // Reference to the "Past OrderID" subcollection
                        CollectionReference pastOrderRef = customerRef.collection("Past OrderID");

                        // Map to hold the data for each document to be added
                        Map<String, Object> orderData = new HashMap<>();
                        orderData.put("IGNORE", "");

                        // Add each document from the documentNames list
                        for (String docName : documentNames) {
                            pastOrderRef
                                    .document(docName)
                                    .set(orderData) // Add the document with the "IGNORE" field
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // Document added successfully
                                                updateNewValuesToDatabase();

                                            } else {
                                                // Handle the error
                                            }
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failures
                        Toast.makeText(Edit_Order.this, "Can't update new data", Toast.LENGTH_LONG).show();
                    }
                });
    }



}