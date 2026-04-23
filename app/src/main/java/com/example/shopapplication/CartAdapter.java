package com.example.shopapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface OnChangeListener {
        void onChange();
    }

    private List<CartItem> list;
    private OnChangeListener listener;

    public CartAdapter(List<CartItem> list, OnChangeListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        CartItem item = list.get(position);
        h.tvName.setText(item.getName());
        h.tvPrice.setText("Rs " + item.getPrice() + " each");
        h.tvQty.setText(String.valueOf(item.getQty()));

        h.btnPlus.setOnClickListener(v -> {
            item.incrementQty();
            h.tvQty.setText(String.valueOf(item.getQty()));
            listener.onChange();
        });

        h.btnMinus.setOnClickListener(v -> {
            item.decrementQty();
            if (item.getQty() == 0) {
                list.remove(position);
                notifyDataSetChanged();
            } else {
                h.tvQty.setText(String.valueOf(item.getQty()));
            }
            listener.onChange();
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQty;
        Button btnPlus, btnMinus;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvPrice = v.findViewById(R.id.tvPrice);
            tvQty = v.findViewById(R.id.tvQty);
            btnPlus = v.findViewById(R.id.btnPlus);
            btnMinus = v.findViewById(R.id.btnMinus);
        }
    }
}
