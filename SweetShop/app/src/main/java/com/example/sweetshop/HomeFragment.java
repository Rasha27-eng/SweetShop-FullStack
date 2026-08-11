package com.example.sweetshop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView categoryRecyclerView, clothesRecyclerView;
    private CategoryAdapter categoryAdapter;
    private SweetsAdapter sweetsAdapter;
    private List<String> categoryList = new ArrayList<>();
    private List<SweetsItem> clothesList = new ArrayList<>();
    private DatabaseReference dbRef;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView);
        clothesRecyclerView = view.findViewById(R.id.clothesRecyclerView);

        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        clothesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        categoryAdapter = new CategoryAdapter(categoryList, selectedCategory -> loadClothesData(selectedCategory));
        sweetsAdapter = new SweetsAdapter(clothesList);

        categoryRecyclerView.setAdapter(categoryAdapter);
        clothesRecyclerView.setAdapter(sweetsAdapter);

        dbRef = FirebaseDatabase.getInstance().getReference("sweets").child("categories");

        fetchCategories();

        return view;
    }

    private void fetchCategories() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot categorySnap : snapshot.getChildren()) {
                    categoryList.add(categorySnap.getKey());
                }

                categoryAdapter.notifyDataSetChanged();

                if (!categoryList.isEmpty()) {
                    loadClothesData(categoryList.get(0)); // أول تصنيف تلقائيًا
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadClothesData(String categoryName) {
        dbRef.child(categoryName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                clothesList.clear();
                for (DataSnapshot itemSnap : snapshot.getChildren()) {
                    String name = itemSnap.child("name").getValue(String.class);
                    Double price = itemSnap.child("price").getValue(Double.class);

                    if (name != null && price != null) {
                        clothesList.add(new SweetsItem(name, price));
                    }
                }
                sweetsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load clothes", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
