package com.sp.mechanictracker.screens;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sp.mechanictracker.adapters.MessageAdapter;
import com.sp.mechanictracker.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderMessages extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private ListView messageListView;
    private EditText messageEditText;
    private Button sendButton, uploadButton;
    private ImageView imageView;
    private List<String> messages;
    private List<String> imageUrls;
    private MessageAdapter messageAdapter;
    private static final int REQUEST_CODE_IMAGE_PICK = 1;
    private Uri selectedImageUri;
    private String uploadedImageUrl;

    String orderID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_messages);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        messageListView = findViewById(R.id.messagelistView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.messagesendButton);
        uploadButton = findViewById(R.id.messageuploadButton);
        imageView = findViewById(R.id.messageimageView);

        messages = new ArrayList<>();
        imageUrls = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messages, imageUrls);
        messageListView.setAdapter(messageAdapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        // Hashmap for user ID -> User Name
        HashMap<String, String> userName = new HashMap<String, String>();
        userName.put("F1PbksAPpSSL0sYPudUodBtjKrg2", "Marcus");
        userName.put("cEAJNeJSfRge0GJHw8OfDCCsKVi2", "Handsome");
        userName.put("ZgMy8asuvUewqi06JjG3JFL18Dx2", "Matha");
        userName.put("kysEJbgwAuZhAe0LZK3cCDLQVn42", "Bob");

        // Get OrderID
        Intent intent = getIntent();

        if (intent.hasExtra("orderID")){
            orderID = intent.getStringExtra("orderID");

            // Listen for new messages and images
            db.collection("messages")
                    .document(orderID)
                    .collection("messages")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        if (e != null) {
                            return;
                        }

                        messages.clear();
                        imageUrls.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String sender = document.getString("sender");
                            // inject user name from UUID
                            sender = userName.get(sender);
                            String text = document.getString("text");
                            String imageUrl = document.getString("imageUrl");
                            messages.add(sender + ": " + text);
                            imageUrls.add(imageUrl);
                        }
                        messageAdapter.notifyDataSetChanged();
                    });
        }


    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (!messageText.isEmpty() || uploadedImageUrl != null) {
            Map<String, Object> message = new HashMap<>();
            message.put("text", messageText);
            message.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
            message.put("timestamp", System.currentTimeMillis());
            message.put("imageUrl", uploadedImageUrl != null ? uploadedImageUrl : "");

            db.collection("messages")
                    .document(orderID)
                    .collection("messages")
                    .add(message)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            messageEditText.setText("");
                            selectedImageUri = null;
                            uploadedImageUrl = null;
                            imageView.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(OrderMessages.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            uploadImage(selectedImageUri);
        }
    }

    private void uploadImage(Uri imageUri) {
        StorageReference storageRef = storage.getReference().child("images/" + System.currentTimeMillis() + ".jpg");
        UploadTask uploadTask = storageRef.putFile(imageUri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return storageRef.getDownloadUrl();
            }
        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                uploadedImageUrl = uri.toString();
                imageView.setImageURI(selectedImageUri);
                imageView.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OrderMessages.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            }
        });
    }
}