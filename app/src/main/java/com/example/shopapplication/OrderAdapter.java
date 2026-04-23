package com.example.shopapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    public interface OnStatusChange {
        void onChange(String orderId, String status);
    }

    private List<JSONObject> list;
    private OnStatusChange listener;
    private boolean isOwner;

    public OrderAdapter(List<JSONObject> list, OnStatusChange listener, boolean isOwner) {
        this.list = list;
        this.listener = listener;
        this.isOwner = isOwner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        try {
            JSONObject order = list.get(position);
            String orderId = order.getString("_id");
            String status = order.getString("status");
            int total = order.getInt("total");
            int pointsEarned = order.optInt("pointsEarned", 0);

            h.tvOrderId.setText("Order #" + orderId.substring(orderId.length() - 6).toUpperCase());
            h.tvStatus.setText(status);
            h.tvTotal.setText("Rs " + total);
            h.tvPoints.setText("Earn: " + pointsEarned + " pts");

            // Color status
            switch (status) {
                case "Confirmed":
                    h.tvStatus.setTextColor(0xFF2196F3);
                    h.tvStatus.setBackgroundResource(0);
                    break;
                case "Completed":
                    h.tvStatus.setTextColor(0xFF4CAF50);
                    h.tvStatus.setBackgroundResource(0);
                    break;
                case "Cancelled":
                    h.tvStatus.setTextColor(0xFFF44336);
                    h.tvStatus.setBackgroundResource(0);
                    break;
                default:
                    h.tvStatus.setTextColor(0xFFFF9800);
                    h.tvStatus.setBackgroundResource(0);
            }

            // Build items text
            JSONArray items = order.getJSONArray("items");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                sb.append(item.getInt("qty") + "x ")
                  .append(item.getString("name"))
                  .append(" - Rs ").append(item.getInt("price") * item.getInt("qty"));
                if (i < items.length() - 1) sb.append("\n");
            }
            h.tvItems.setText(sb.toString());

            // Show owner buttons if owner
            if (isOwner) {
                h.layoutOwnerButtons.setVisibility(View.VISIBLE);
                h.btnConfirm.setOnClickListener(v -> listener.onChange(orderId, "Confirmed"));
                h.btnComplete.setOnClickListener(v -> listener.onChange(orderId, "Completed"));
                h.btnCancel.setOnClickListener(v -> listener.onChange(orderId, "Cancelled"));
            } else {
                h.layoutOwnerButtons.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvItems, tvTotal, tvPoints;
        Button btnConfirm, btnComplete, btnCancel;
        LinearLayout layoutOwnerButtons;

        ViewHolder(View v) {
            super(v);
            tvOrderId = v.findViewById(R.id.tvOrderId);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvItems = v.findViewById(R.id.tvItems);
            tvTotal = v.findViewById(R.id.tvTotal);
            tvPoints = v.findViewById(R.id.tvPoints);
            btnConfirm = v.findViewById(R.id.btnConfirm);
            btnComplete = v.findViewById(R.id.btnComplete);
            btnCancel = v.findViewById(R.id.btnCancel);
            layoutOwnerButtons = v.findViewById(R.id.layoutOwnerButtons);
        }
    }
}
