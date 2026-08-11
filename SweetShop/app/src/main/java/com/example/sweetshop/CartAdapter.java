package com.example.sweetshop;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<SweetsAdapter.CartItem> cartList;
    private DatabaseReference cartRef;

    public CartAdapter(List<SweetsAdapter.CartItem> cartList, DatabaseReference cartRef) {
        this.cartList = cartList;
        this.cartRef = cartRef;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        SweetsAdapter.CartItem item = cartList.get(position);

        holder.nameTextView.setText(item.name);
        holder.quantityTextView.setText("x" + item.quantity);

        double totalPrice = item.price * item.quantity;
        holder.priceTextView.setText(String.format("₪%.2f", totalPrice));

        // ربط زر الحذف ليحذف العنصر من الـ Firebase
        holder.deleteButton.setOnClickListener(v -> {
            cartRef.child(item.name).removeValue();
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, quantityTextView, priceTextView;
        Button deleteButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewCartName);
            quantityTextView = itemView.findViewById(R.id.textViewCartQuantity);
            priceTextView = itemView.findViewById(R.id.textViewCartPrice);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
