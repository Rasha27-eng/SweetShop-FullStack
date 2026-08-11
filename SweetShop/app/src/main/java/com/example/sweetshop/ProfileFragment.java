package com.example.sweetshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TextView tvUserName, tvEmail, tvPhone;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvUserName = view.findViewById(R.id.textViewUserName);
        tvEmail = view.findViewById(R.id.textViewEmail);
        tvPhone = view.findViewById(R.id.textViewPhone);
        Button logoutButton = view.findViewById(R.id.buttonLogout);

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();

            // يمكنك توجيه المستخدم إلى LoginActivity
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();

            // عرض الايميل من Authentication مباشرة
            tvEmail.setText(currentUser.getEmail());

            // جلب الاسم ورقم الهاتف من قاعدة بيانات (مثلاً Realtime Database)
            userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.child("firstName").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);

                    if (name != null) tvUserName.setText(name);
                    if (phone != null) tvPhone.setText(phone);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

        } else {
            tvUserName.setText("Guest");
            tvEmail.setText("-");
            tvPhone.setText("-");
        }

        return view;
    }
}
