package com.example.sanpham;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sanpham.adapter.ProductAdapter;
import com.example.sanpham.database.DBHelper;
import com.example.sanpham.model.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ProductAdapter adapter;
    List<Product> productList;
    DBHelper dbHelper;
    FloatingActionButton btnAdd;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        btnAdd = findViewById(R.id.btnAdd);
        searchView = findViewById(R.id.searchView);
        dbHelper = new DBHelper(this);

        productList = dbHelper.getAllProducts(); // lấy từ DB
        adapter = new ProductAdapter(productList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        createNotificationChannel();

        adapter.setOnProductClickListener(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onEdit(Product product) {
                showAddOrEditDialog(product);
            }

            @Override
            public void onDelete(Product product) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Xoá sản phẩm")
                        .setMessage("Bạn có chắc muốn xoá?")
                        .setPositiveButton("Xoá", (dialog, which) -> {
                            dbHelper.deleteProduct(product.getId());
                            reloadData();
                        })
                        .setNegativeButton("Huỷ", null)
                        .show();
            }
        });

        btnAdd.setOnClickListener(v -> showAddOrEditDialog(null));
        btnAdd.setOnClickListener(v -> showAddDialog());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
    }

    private void showAddDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        EditText edtName = view.findViewById(R.id.edtName);
        EditText edtPrice = view.findViewById(R.id.edtPrice);
        EditText edtDesc = view.findViewById(R.id.edtDesc);

        new AlertDialog.Builder(this)
                .setTitle("Thêm sản phẩm")
                .setView(view)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = edtName.getText().toString();
                    String priceStr = edtPrice.getText().toString();
                    String desc = edtDesc.getText().toString();
                    if (name.isEmpty() || priceStr.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double price = Double.parseDouble(priceStr);
                    dbHelper.insertProduct(name, price, desc);
                    productList.clear();
                    productList.addAll(dbHelper.getAllProducts());
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Đã thêm!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showAddOrEditDialog(Product pEdit) {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        EditText edtName = view.findViewById(R.id.edtName);
        EditText edtPrice = view.findViewById(R.id.edtPrice);
        EditText edtDesc = view.findViewById(R.id.edtDesc);

        boolean isEdit = (pEdit != null);
        if (isEdit) {
            edtName.setText(pEdit.getName());
            edtPrice.setText(String.valueOf(pEdit.getPrice()));
            edtDesc.setText(pEdit.getDescription());
        }

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Sửa sản phẩm" : "Thêm sản phẩm")
                .setView(view)
                .setPositiveButton(isEdit ? "Cập nhật" : "Thêm", (dialog, which) -> {
                    String name = edtName.getText().toString();
                    String priceStr = edtPrice.getText().toString();
                    String desc = edtDesc.getText().toString();

                    if (name.isEmpty() || priceStr.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double price = Double.parseDouble(priceStr);
                    if (isEdit) {
                        Product newP = new Product(pEdit.getId(), name, price, desc);
                        dbHelper.updateProduct(newP);
                    } else {
                        dbHelper.insertProduct(name, price, desc);
                        sendNotification(name);
                    }
                    reloadData();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void reloadData() {
        productList.clear();
        productList.addAll(dbHelper.getAllProducts());
        if (productList.isEmpty()) {
            findViewById(R.id.tvEmpty).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.tvEmpty).setVisibility(View.GONE);
        }
        adapter = new ProductAdapter(productList); // tạo adapter mới
        recyclerView.setAdapter(adapter);
        // Gắn lại tìm kiếm và callback
        // Gắn lại callback sửa/xoá
        adapter.setOnProductClickListener(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onEdit(Product product) {
                showAddOrEditDialog(product);
            }

            @Override
            public void onDelete(Product product) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Xoá sản phẩm")
                        .setMessage("Bạn có chắc muốn xoá?")
                        .setPositiveButton("Xoá", (dialog, which) -> {
                            dbHelper.deleteProduct(product.getId());
                            reloadData();
                        })
                        .setNegativeButton("Huỷ", null)
                        .show();
            }
        });

        // Gắn lại tìm kiếm
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
        adapter.notifyDataSetChanged();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ProductChannel";
            String description = "Kênh thông báo sản phẩm mới";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("product_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void sendNotification(String productName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "product_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Bạn có thể thay icon
                .setContentTitle("Đã thêm sản phẩm")
                .setContentText("Sản phẩm \"" + productName + "\" đã được thêm vào!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify((int) System.currentTimeMillis(), builder.build()); // ID random
    }
}
