package com.example.sweetshop;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class SweetsAdapter extends RecyclerView.Adapter<SweetsAdapter.ClothesViewHolder> {

    private List<SweetsItem> clothesList;
    private DatabaseReference cartRef;
    private String userId;

    public SweetsAdapter(List<SweetsItem> clothesList) {
        this.clothesList = clothesList;

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            userId = "anonymous";
        }

        cartRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("cart");
    }

    @NonNull
    @Override
    public ClothesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sweets, parent, false);
        return new ClothesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClothesViewHolder holder, int position) {
        SweetsItem item = clothesList.get(position);

        holder.nameTextView.setText(item.getName());
        holder.priceTextView.setText("₪" + item.getPrice());
        holder.quantityTextView.setText(String.valueOf(item.getQuantity()));

        holder.buttonPlus.setOnClickListener(v -> {
            int qty = item.getQuantity() + 1;
            item.setQuantity(qty);
            holder.quantityTextView.setText(String.valueOf(qty));
        });

        holder.buttonMinus.setOnClickListener(v -> {
            int qty = item.getQuantity();
            if (qty > 1) {
                qty--;
                item.setQuantity(qty);
                holder.quantityTextView.setText(String.valueOf(qty));
            }
        });

        holder.addToCartIcon.setOnClickListener(v -> {
            int qty = item.getQuantity();
            if (qty <= 0) {
                Toast.makeText(v.getContext(), "must more than 1", Toast.LENGTH_SHORT).show();
                return;
            }

            cartRef.child(item.getName()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        CartItem existingItem = snapshot.getValue(CartItem.class);
                        if (existingItem != null) {
                            int newQty = existingItem.quantity + qty;
                            CartItem updatedItem = new CartItem(item.getName(), item.getPrice(), newQty);
                            cartRef.child(item.getName()).setValue(updatedItem);
                            Toast.makeText(v.getContext(), item.getName() + " updated to " + newQty, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        CartItem cartItem = new CartItem(item.getName(), item.getPrice(), qty);
                        cartRef.child(item.getName()).setValue(cartItem);
                        Toast.makeText(v.getContext(), item.getName() + " added to cart", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(v.getContext(), "failed to add", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return clothesList.size();
    }

    static class ClothesViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, priceTextView, quantityTextView;
        Button buttonPlus, buttonMinus;
        ImageView addToCartIcon;

        public ClothesViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewName);
            priceTextView = itemView.findViewById(R.id.textViewPrice);
            quantityTextView = itemView.findViewById(R.id.textViewQuantity);
            buttonPlus = itemView.findViewById(R.id.buttonPlus);
            buttonMinus = itemView.findViewById(R.id.buttonMinus);
            addToCartIcon = itemView.findViewById(R.id.imageAddToCart);
        }
    }

    // 🛒 كلاس عنصر السلة
    public static class CartItem {
        public String name;
        public double price;
        public int quantity;

        public CartItem() {}

        public CartItem(String name, double price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}
