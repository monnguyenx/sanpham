package com.example.sanpham.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.sanpham.model.Product;

import java.util.ArrayList;
import java.util.List;

// DBHelper.java
public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "product_manager.db";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE Product (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT, " +
                "price REAL, " +
                "description TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Product");
        onCreate(db);
    }

    public void insertProduct(String name, double price, String desc) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("price", price);
        values.put("description", desc);
        db.insert("Product", null, values);
    }

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM Product", null);
        while (c.moveToNext()) {
            list.add(new Product(
                    c.getInt(0),
                    c.getString(1),
                    c.getDouble(2),
                    c.getString(3)
            ));
        }
        c.close();
        return list;
    }

    // Sửa sản phẩm
    public void updateProduct(Product p) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", p.getName());
        values.put("price", p.getPrice());
        values.put("description", p.getDescription());
        db.update("Product", values, "id=?", new String[]{String.valueOf(p.getId())});
    }

    // Xoá sản phẩm
    public void deleteProduct(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("Product", "id=?", new String[]{String.valueOf(id)});
    }
}


