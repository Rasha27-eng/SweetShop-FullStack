package com.example.sweetshop;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sweetshop.CartAdapter;
import com.example.sweetshop.SweetsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private List<SweetsAdapter.CartItem> cartList;

    private DatabaseReference cartRef;
    private String userId;

    private Button orderButton;
    private TextView totalTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewCart);
        orderButton = view.findViewById(R.id.buttonOrder);
        totalTextView = view.findViewById(R.id.textViewTotal);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            userId = "anonymous";
        }

        cartRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("cart");

        cartList = new ArrayList<>();
        adapter = new CartAdapter(cartList, cartRef);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadCartData();

        orderButton.setOnClickListener(v -> {
            if (cartList.isEmpty()) {
                Toast.makeText(getContext(), "Cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            showPaymentDialog();
        });

        return view;
    }

    private void loadCartData() {
        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    SweetsAdapter.CartItem item = child.getValue(SweetsAdapter.CartItem.class);
                    if (item != null) {
                        cartList.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
                updateTotalPrice();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotalPrice() {
        double total = 0.0;
        for (SweetsAdapter.CartItem item : cartList) {
            total += item.getPrice() * item.getQuantity();
        }
        totalTextView.setText(String.format("Total: ₪%.2f", total));
    }

    private void showPaymentDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_payment, null);
        EditText cardNumberET = dialogView.findViewById(R.id.editTextCardNumber);
        EditText expiryDateET = dialogView.findViewById(R.id.editTextExpiryDate);
        EditText cvvET = dialogView.findViewById(R.id.editTextCVV);

        new AlertDialog.Builder(requireContext())
                .setTitle("Enter Visa Details")
                .setView(dialogView)
                .setPositiveButton("Pay", (dialog, which) -> {
                    String cardNumber = cardNumberET.getText().toString().trim();
                    String expiryDate = expiryDateET.getText().toString().trim();
                    String cvv = cvvET.getText().toString().trim();

                    if (TextUtils.isEmpty(cardNumber) || TextUtils.isEmpty(expiryDate) || TextUtils.isEmpty(cvv)) {
                        Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    placeOrder();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void placeOrder() {
        String orderId = cartRef.push().getKey();
        if (orderId == null) {
            Toast.makeText(getContext(), "Failed to create order ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Order order = new Order(cartList, System.currentTimeMillis());

        DatabaseReference ordersRef = FirebaseDatabase.getInstance()
                .getReference("orders")
                .child(userId)
                .child(orderId);

        ordersRef.setValue(order)
                .addOnSuccessListener(aVoid -> {
                    cartRef.removeValue()
                            .addOnSuccessListener(aVoid1 -> {
                                cartList.clear();
                                adapter.notifyDataSetChanged();
                                updateTotalPrice();
                                Toast.makeText(getContext(), "Order placed successfully", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to place order", Toast.LENGTH_SHORT).show()
                );
    }
}
