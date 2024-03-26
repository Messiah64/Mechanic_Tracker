package com.sp.mechanictracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Add_Order extends AppCompatActivity {

    // Initialize Firebase Firestore


    private ImageView repair, add, complete;
    RecyclerView recyclerView;

    private ArrayList<Uri> uriList = new ArrayList<>();
    public List<String> imageURLs = new ArrayList<>(); // Shit is very important
    private FirebaseStorage storage;
    private FirebaseFirestore db;

    TextView textView, OrderNumber;
    ImageView pick;
    ArrayList<Uri> uriArrayList = new ArrayList<>();
    RecyclerAdapter adapter;

    // storing for retrived info
    String phoneString="", pidString="", bicyleString="", dateString="", timeString="", mechanicString="", notesString="", orderString="";

    boolean newCustomer = false; // Flag if new Customer

    // Input Variables
    EditText phoneNumber, PIDNumber, bicycleDetails, date, time, mechanicName, notes;
    Button submitBtn;
    String spinnerDeliveryModeValue="", spinnerPackageTypeValue="";
    Spinner deliveryMode, packageType;

    private static final int Read_Permission = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        // Firebase Firestore & Storage Instances
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Link form data variables (EditTexts) and order number (TextViews)
        phoneNumber = findViewById(R.id.phoneNumber);
        phoneNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    // If focus lost, fetch existing data
                    phoneString = phoneNumber.getText().toString().trim();
                    FetchExistingData(phoneString);
                }
            }
        });

        PIDNumber = findViewById(R.id.PID);
        OrderNumber = findViewById(R.id.OrderID);
        bicycleDetails = findViewById(R.id.bicycleNumber);
        date = findViewById(R.id.datePick);
        time = findViewById(R.id.timePick);
        mechanicName = findViewById(R.id.mechanic);
        notes = findViewById(R.id.notes);

        // Link form data variables (Spinners)
        deliveryMode = findViewById(R.id.DeliveryPick);
        List<String> deliveryOptions = new ArrayList<>();
        deliveryOptions.add("Delivery");
        deliveryOptions.add("Meet Up");
        ArrayAdapter<String> adapterDeliveryPicker = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, deliveryOptions);
        adapterDeliveryPicker.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        deliveryMode.setAdapter(adapterDeliveryPicker);
        // Set an OnItemSelectedListener to handle the selection event
        deliveryMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                // Retrieve the selected item
                spinnerDeliveryModeValue = (String) parent.getItemAtPosition(position);
                // Display a toast with the selected item
                Toast.makeText(Add_Order.this, "Selected: " + spinnerDeliveryModeValue, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        packageType = findViewById(R.id.PackagePick);
        List<String> PackageOptions = new ArrayList<>();
        PackageOptions.add("Gold");
        PackageOptions.add("Premium");
        PackageOptions.add("Safety");
        PackageOptions.add("Rec");
        PackageOptions.add("Nil");
        CustomSpinnerAdapter adapterPackagePicker = new CustomSpinnerAdapter(this, android.R.layout.simple_spinner_item, PackageOptions);
        adapterPackagePicker.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        packageType.setAdapter(adapterPackagePicker);
        // Set an OnItemSelectedListener to handle the selection event
        packageType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                // Retrieve the selected item
                spinnerPackageTypeValue = (String) parent.getItemAtPosition(position);
                // Display a toast with the selected item
                Toast.makeText(Add_Order.this, "Selected: " + spinnerPackageTypeValue, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Submit Button
        submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { onConfirm(); }
        });

        // Bottom Nav Bar Begin
        repair = findViewById(R.id.add_repair);
        add = findViewById(R.id.add_add);
        complete = findViewById(R.id.add_complete);
        repair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Add_Order.this, Ongoing_Orders.class);
                startActivity(intent);
            }
        });
        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Add_Order.this, Completed_Orders.class);
                startActivity(intent);
            }
        });
        // Bottom Nav Bar Ends

        textView = findViewById(R.id.totalPhotos);
        recyclerView = findViewById(R.id.recyclerview_gallery_images);
        pick = findViewById(R.id.pick);

        adapter = new RecyclerAdapter(uriArrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);

        if (ContextCompat.checkSelfPermission(Add_Order.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Add_Order.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Read_Permission);
        }

        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Select Pictures"), 1);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    uriArrayList.add(data.getClipData().getItemAt(i).getUri());
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    uriList.add(uri);
                    // Upload each image individually
                    uploadImageToStorage(uri);
                }

                adapter.notifyDataSetChanged();
                textView.setText(uriArrayList.size() + " photos selected");

            } else if (data.getData() != null) {
                Uri uri = data.getData();
                uriList.add(uri);
                String imageURL = data.getData().getPath();
                uriArrayList.add(Uri.parse(imageURL));
                // Upload the selected image
                uploadImageToStorage(uri);
            }
        }
    }


    private void FetchExistingData(String phoneNumberString) {

        db.collection("Existing Customers")
                .document(phoneNumberString)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Phone number document found
                                handleExistingCustomer(document);
                            } else {
                                // Phone number document not found
                                handleNewCustomer();
                            }
                        } else {
                            // Handle errors
                        }
                    }
                });
    }

    private void handleExistingCustomer(DocumentSnapshot document) {
        // Retrieve the value of the "PID" field from the document
        String pid = document.getString("PID");
        pidString = pid;

        // Then, proceed to retrieve the orderID from the "PID ORDERID" document
        db.collection("PID ORDERID")
                .document("current")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                String orderID = documentSnapshot.getString("orderID");
                                updateOrderUI(orderID);
                            } else {
                                // Handle the case if the document doesn't exist
                            }
                        } else {
                            // Handle errors
                        }
                    }
                });
    }

    private void handleNewCustomer() {
        db.collection("PID ORDERID")
                .document("current")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                String orderID = documentSnapshot.getString("orderID");
                                String pID = documentSnapshot.getString("pID");
                                updateOrderUI(orderID, pID);
                            } else {
                                // Handle the case if the document doesn't exist
                            }
                        } else {
                            // Handle errors
                        }
                    }
                });
    }

    private void updateOrderUI(String orderID) { // Existing Customer
        orderString = incrementOrderID(orderID);
        PIDNumber.setText(pidString);
        OrderNumber.setVisibility(View.VISIBLE);
        OrderNumber.setText(orderString);

        // new Customer flag is already false
    }

    private void updateOrderUI(String orderID, String pID) { // New Customer
        pidString = incrementPID(pID);
        orderString = incrementOrderID(orderID);
        PIDNumber.setText(pidString);
        OrderNumber.setVisibility(View.VISIBLE);
        OrderNumber.setText(orderString);

        newCustomer = true;
    }


    public String incrementPID(String input) {
        // Check if the input string is not null and has at least two characters
        if (input != null && input.length() >= 2) {
            // Extract the first character (e.g., 'P')
            char firstChar = input.charAt(0);

            // Extract the numeric part of the string
            String numericPart = input.substring(1);

            try {
                // Convert the numeric part to an integer
                int numericValue = Integer.parseInt(numericPart);

                // Increment the numeric value
                numericValue++;

                // Format the numeric value back to a three-digit string
                String formattedNumericPart = String.format("%03d", numericValue);

                // Recombine the first character with the incremented numeric part
                return firstChar + formattedNumericPart;
            } catch (NumberFormatException e) {
                // If the numeric part cannot be parsed as an integer, return the original string
                return input;
            }
        } else {
            // If the input is null or too short, return the original string
            return input;
        }
    }
    public String incrementOrderID(String input) {
        // Check if the input string is not null and has at least three characters
        if (input != null && input.length() >= 3) {
            // Extract the first two characters (e.g., 'BC')
            String firstTwoChars = input.substring(0, 2);

            // Extract the numeric part of the string
            String numericPart = input.substring(2);

            try {
                // Convert the numeric part to an integer
                int numericValue = Integer.parseInt(numericPart);

                // Increment the numeric value
                numericValue++;

                // Format the numeric value back to a three-digit string
                String formattedNumericPart = String.format("%03d", numericValue);

                // Recombine the first two characters with the incremented numeric part
                return firstTwoChars + formattedNumericPart;
            } catch (NumberFormatException e) {
                // If the numeric part cannot be parsed as an integer, return the original string
                return input;
            }
        } else {
            // If the input is null or too short, return the original string
            return input;
        }
    }

    private void uploadImageToStorage(Uri uri) {
        String fileName = "image_" + System.currentTimeMillis(); // Unique filename
        StorageReference storageRef = storage.getReference().child(fileName);

        UploadTask uploadTask = storageRef.putFile(uri);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return storageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                imageURLs.add(downloadUri.toString());

                // If all images have been uploaded, do something with imageURLs
                if (imageURLs.size() == uriList.size()) {
                    // imageURLs array is ready, you can use it here
                    Log.d("Image URLs: ", imageURLs.toString());
                }
            } else {
                // Handle failures
            }
        });
    }


    public void onConfirm() {
        // get all the values, throw inside map

        /*
        // storing for retrived info
        String phoneString, pidString, bicyleString, dateString, timeString, mechanicString, notesString, orderString;
        String spinnerDeliveryModeValue, spinnerPackageTypeValue;
         */
        // phoneString, pidString, orderString, spinnerDeliveryModeValue, spinnerPackageTypeValue are empty

        if (phoneString.isEmpty() || pidString.isEmpty() || orderString.isEmpty() || spinnerDeliveryModeValue.isEmpty() || spinnerPackageTypeValue.isEmpty()) {
            Toast.makeText(Add_Order.this, "Fill in Phone Number!", Toast.LENGTH_SHORT).show();
        } else {
            // phoneString, pidString, orderString, spinnerDeliveryModeValue, spinnerPackageTypeValue alr have values
            bicyleString = bicycleDetails.getText().toString().trim();
            dateString = date.getText().toString().trim();
            timeString = time.getText().toString().trim();
            mechanicString = mechanicName.getText().toString().trim();
            notesString = notes.getText().toString().trim();

            Add_Data(orderString, pidString, phoneString, bicyleString, spinnerDeliveryModeValue, dateString, timeString, mechanicString, spinnerPackageTypeValue, notesString, imageURLs);

            Add_Data_BackUp(orderString, pidString, phoneString, bicyleString, spinnerDeliveryModeValue, dateString, timeString, mechanicString, spinnerPackageTypeValue, notesString, imageURLs);

        }
    }

    private void Add_Data_BackUp(String subcollectionName, String pidOrder,
                                 String phonenumber, String bicycleDetails, String deliveryMethod, String date,
                                 String time, String mechanic, String packageInfo, String notes, List<String> arrayList) {
        // Create a Map to store the fields to be added to the document
        Map<String, Object> data = new HashMap<>();
        data.put("Phone", phonenumber);
        data.put("BicycleDetails", bicycleDetails);
        data.put("DeliveryMethod", deliveryMethod);
        data.put("Date", date);
        data.put("Time", time);
        data.put("Mechanic", mechanic);
        data.put("Package", packageInfo);
        data.put("Notes", notes);
        data.put("PID", pidOrder);
        data.put("OrderID", subcollectionName); // Just in case putting OrderID inside for easy referencing later on
        data.put("Images", arrayList);
        data.put("Status", "OTW"); // Default status

        // Add the subcollection, its document, and fields to Firestore
        db.collection("Orders")
                .document(subcollectionName)
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Document with fields added successfully
                        if (newCustomer) { // New Customer
                            New_Customer(phoneString, pidString);
                        } else {
                            Existing_Customer(phoneString, orderString);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Log.e("Somethings' fucked up", "Error adding document", e);
                    }
                });

    }

    private void Add_Data(String subcollectionName, String pidOrder,
                          String phonenumber, String bicycleDetails, String deliveryMethod, String date,
                          String time, String mechanic, String packageInfo, String notes, List<String> arrayList) {

        // Create a Map to store the fields to be added to the document
        Map<String, Object> data = new HashMap<>();
        data.put("Phone", phonenumber);
        data.put("BicycleDetails", bicycleDetails);
        data.put("DeliveryMethod", deliveryMethod);
        data.put("Date", date);
        data.put("Time", time);
        data.put("Mechanic", mechanic);
        data.put("Package", packageInfo);
        data.put("Notes", notes);
        data.put("PID", pidOrder);
        data.put("Images", arrayList);
        data.put("Status", "OTW"); // Default status

        // Add the subcollection, its document, and fields to Firestore
        db.collection("All Orders")
                .document("On Going")
                .collection(subcollectionName)
                .document("OrderData")
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Document with fields added successfully
                        if (newCustomer) { // New Customer
                            New_Customer(phoneString, pidString);
                        } else {
                            Existing_Customer(phoneString, orderString);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Log.e("Somethings' fucked up", "Error adding document", e);
                    }
                });
    }

    public void Existing_Customer(String phone, String order) {
        DocumentReference orderIDDocRef = db.collection("Existing Customers")
                .document(phone)
                .collection("Past OrderID")
                .document(order);

        Map<String, Object> data = new HashMap<>();
        // You can add more fields to the data map if needed
        // For now, we are adding an empty field just to create the document
        data.put("ignore data", "");

        orderIDDocRef.set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Document created successfully
                            updateFields(newCustomer, orderString, pidString);
                            Intent intent = new Intent(Add_Order.this, Ongoing_Orders.class);
                            startActivity(intent);

                        } else {
                            // Document creation failed
                            // Handle failure
                        }
                    }
                });
    }

    public void New_Customer(String phoneNum, String pid) {
        DocumentReference phoneNumberDocRef = db.collection("Existing Customers")
                .document(phoneNum);

        Map<String, Object> data = new HashMap<>();
        data.put("PID", pid);

        phoneNumberDocRef.set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Document set successful
                            Existing_Customer(phoneString, orderString);
                        } else {
                            // Document set failed
                            // Handle failure
                        }
                    }
                });
    }


    public void updateFields(boolean CheckNewCustomer, String newOrderID, String newPID) {
        DocumentReference docRef = db.collection("PID ORDERID").document("current");

        if (CheckNewCustomer) {
            // Update both orderID and pID
            docRef.update("orderID", newOrderID, "pID", newPID)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Fields updated successfully
                                // Handle success
                            } else {
                                // Field update failed
                                // Handle failure
                            }
                        }
                    });
        } else {
            // Update only orderID
            docRef.update("orderID", newOrderID)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Field updated successfully
                                // Handle success
                            } else {
                                // Field update failed
                                // Handle failure
                            }
                        }
                    });
        }
    }



}