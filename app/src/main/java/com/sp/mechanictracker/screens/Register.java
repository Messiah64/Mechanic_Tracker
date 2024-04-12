package com.sp.mechanictracker.screens;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sp.mechanictracker.R;

public class Register extends AppCompatActivity {
    private EditText email, password, password_check;
    private Button signInButton;
    private TextView logInIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }
}