package com.example.sweetshop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText firstNameEditText, lastNameEditText, phoneEditText, emailEditText, passwordEditText;
    private Button registerButton;
    private TextView goToLoginText;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        firstNameEditText = findViewById(R.id.firstName);
        lastNameEditText = findViewById(R.id.lastName);
        phoneEditText = findViewById(R.id.phone);
        emailEditText = findViewById(R.id.registerEmail);
        passwordEditText = findViewById(R.id.registerPassword);
        registerButton = findViewById(R.id.registerBtn);
        goToLoginText = findViewById(R.id.goToLoginText);

        registerButton.setOnClickListener(view -> {
            String firstName = firstNameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Input validation
            if (firstName.isEmpty()) {
                firstNameEditText.setError("First name is required");
                return;
            }
            if (lastName.isEmpty()) {
                lastNameEditText.setError("Last name is required");
                return;
            }
            if (phone.isEmpty()) {
                phoneEditText.setError("Phone number is required");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError("Please enter a valid email");
                return;
            }
            if (password.length() < 6) {
                passwordEditText.setError("Password must be at least 6 characters");
                return;
            }

            // Check if email already exists
            mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    boolean emailExists = !task.getResult().getSignInMethods().isEmpty();
                    if (emailExists) {
                        emailEditText.setError("Email is already in use");
                        emailEditText.requestFocus();
                    } else {
                        // Create user in Firebase Authentication
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(registrationTask -> {
                                    if (registrationTask.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null) {
                                            String uid = user.getUid();
                                            Log.d("RegisterActivity", "User created with UID: " + uid);

                                            User newUser = new User(firstName, lastName, phone, email);

                                            // Save user info in Realtime Database
                                            usersRef.child(uid).setValue(newUser)
                                                    .addOnCompleteListener(dbTask -> {
                                                        if (dbTask.isSuccessful()) {
                                                            Log.d("RegisterActivity", "User data saved in Realtime Database");
                                                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(this, LoginActivity.class));
                                                            finish();
                                                        } else {
                                                            Log.e("RegisterActivity", "Failed to save user data: " + dbTask.getException().getMessage());
                                                            Toast.makeText(this, "Database error: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                        } else {
                                            Log.e("RegisterActivity", "FirebaseUser is null after registration");
                                            Toast.makeText(this, "Unexpected error occurred", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Log.e("RegisterActivity", "Registration failed: " + registrationTask.getException().getMessage());
                                        Toast.makeText(this, "Registration failed: " + registrationTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                } else {
                    Toast.makeText(this, "Error checking email existence", Toast.LENGTH_SHORT).show();
                }
            });
        });

        goToLoginText.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }

    // User data model class
    public static class User {
        public String firstName;
        public String lastName;
        public String phone;
        public String email;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String firstName, String lastName, String phone, String email) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.phone = phone;
            this.email = email;
        }
    }
}
