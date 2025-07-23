package com.example.sanpham.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sanpham.R;
import com.example.sanpham.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private final List<Product> productList;
    private final List<Product> productListFull;

    public ProductAdapter(List<Product> list) {
        this.productList = list;
        this.productListFull = new ArrayList<>(list); // dùng để tìm kiếm
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product p = productList.get(position);
        holder.tvName.setText(p.getName());
        holder.tvPrice.setText("Giá: " + p.getPrice());
        holder.tvDesc.setText(p.getDescription());

        holder.itemView.setOnLongClickListener(v -> {
            // Gợi ý hiển thị popup menu
            PopupMenu menu = new PopupMenu(v.getContext(), v);
            menu.getMenu().add("Sửa");
            menu.getMenu().add("Xoá");

            menu.setOnMenuItemClickListener(item -> {
                if (Objects.equals(item.getTitle(), "Sửa")) {
                    if (listener != null) listener.onEdit(p);
                } else if (Objects.equals(item.getTitle(), "Xoá")) {
                    if (listener != null) listener.onDelete(p);
                }
                return true;
            });
            menu.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // Tìm kiếm sản phẩm
    public void filter(String query) {
        productList.clear();
        if (query.isEmpty()) {
            productList.addAll(productListFull);
        } else {
            for (Product p : productListFull) {
                if (p.getName().toLowerCase().contains(query.toLowerCase())) {
                    productList.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvDesc;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDesc = itemView.findViewById(R.id.tvDesc);
        }
    }

    public interface OnProductClickListener {
        void onEdit(Product product);
        void onDelete(Product product);
    }

    private OnProductClickListener listener;

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }
}

