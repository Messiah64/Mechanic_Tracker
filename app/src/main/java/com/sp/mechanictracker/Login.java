package com.sp.mechanictracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class Login extends AppCompatActivity {

    private EditText email, password;
    private Button signInButton, changeUserButton;
    private TextView signUpIntent;
    private FirebaseAuth fAuth;
    private int Count;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        fAuth = FirebaseAuth.getInstance();
        Count = 0; // needed to change demo user account

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signInButton = findViewById(R.id.loginBtn);
        signUpIntent = findViewById(R.id.signUp_textView);
        changeUserButton = findViewById(R.id.changeUser);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { loginUser(); }
        });

        signUpIntent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { goToSignUpPage(); }
        });

        changeUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { changeUser(); }
        });
    }

    private void loginUser() {

        String Email = email.getText().toString().trim();
        String Password = password.getText().toString().trim();

        //handle errors
        if (TextUtils.isEmpty(Email)) {
            email.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(Password)) {
            password.setError("Password is required");
            return;
        }

        if (password.length() < 6) { //change this if password lenght changes later on
            password.setError("Password must be greater than 6 characters");
            return;
        }

        fAuth.signInWithEmailAndPassword(Email + "@gmail.com", Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Login.this, "Logged In Succesfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Login.this, Ongoing_Orders.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(Login.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void goToSignUpPage() {
        Intent intent = new Intent(Login.this, Register.class);
        startActivity(intent);
    }

    private void changeUser() {
        /*
        Hard coded 4 example users - Marcus, Handsome_Me, Matha, Bob => All have same password: 123456
         */
        ArrayList<String> Demo_Users = new ArrayList<String>();
        Demo_Users.add("Marcus");
        Demo_Users.add("Handsome_Me");
        Demo_Users.add("Matha");
        Demo_Users.add("Bob");

        String Hard_Coded_User = ""; // Blank until assigned

        String Hard_Coded_Password = "123456";

        if (Count < 4) {
            Hard_Coded_User = Demo_Users.get(Count); // If Count == 0, Demo_Users(0) -> Marcus selected
            email.setText(Hard_Coded_User);
            password.setText(Hard_Coded_Password);
            Count ++;
        }

        else {
            Count = 0;
        }

    }
}