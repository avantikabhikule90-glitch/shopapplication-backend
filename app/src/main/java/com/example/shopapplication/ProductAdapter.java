package com.example.shopapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    public interface OnAddClick {
        void onAdd(Product product);
    }

    private List<Product> list;
    private OnAddClick listener;

    public ProductAdapter(List<Product> list, OnAddClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Product p = list.get(position);
        h.tvEmoji.setText(p.getEmoji());
        h.tvName.setText(p.getName());
        h.tvPrice.setText(p.getPrice());
        h.btnAdd.setOnClickListener(v -> listener.onAdd(p));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvPrice;
        Button btnAdd;

        ViewHolder(View v) {
            super(v);
            tvEmoji = v.findViewById(R.id.tvEmoji);
            tvName = v.findViewById(R.id.tvName);
            tvPrice = v.findViewById(R.id.tvPrice);
            btnAdd = v.findViewById(R.id.btnAdd);
        }
    }
}