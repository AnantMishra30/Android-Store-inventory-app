package com.example.android.storeinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.storeinventory.data.StoreContract.ProductEntry;

public class StoreDbHelper extends SQLiteOpenHelper {

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    private static final String NOT_NULL_TYPE = " NOT NULL";
    private static final String PRIMARY_KEY_TYPE = " PRIMARY KEY";
    private static final String AUTOINCREMENT_TYPE = " AUTOINCREMENT";
    private static final String SEP_COMMA = ", ";

    private static final String SQL_CREATE_PRODUCTS_ENTRIES = "CREATE TABLE " + ProductEntry.TABLE_NAME + " ("
            + ProductEntry._ID + INTEGER_TYPE + PRIMARY_KEY_TYPE + AUTOINCREMENT_TYPE + SEP_COMMA
            + ProductEntry.COLUMN_PRODUCT_NAME + TEXT_TYPE + NOT_NULL_TYPE + SEP_COMMA
            + ProductEntry.COLUMN_PRODUCT_PRICE + REAL_TYPE + NOT_NULL_TYPE + SEP_COMMA
            + ProductEntry.COLUMN_PRODUCT_QUANTITY + INTEGER_TYPE + NOT_NULL_TYPE + SEP_COMMA
            + ProductEntry.COLUMN_PRODUCT_PIC + TEXT_TYPE + NOT_NULL_TYPE + SEP_COMMA
            + ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME + TEXT_TYPE + NOT_NULL_TYPE + SEP_COMMA
            + ProductEntry.COLUMN_PRODUCT_SUPPLIER_E_MAIL + TEXT_TYPE + SEP_COMMA
            + ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE + TEXT_TYPE + SEP_COMMA
            + ProductEntry.COLUMN_PRODUCT_INFO + TEXT_TYPE
            + ");";
    private static final String SQL_DELETE_PRODUCTS_ENTRIES =
            "DELETE TABLE IF EXISTS " + ProductEntry.TABLE_NAME;

    private static int DATA_BASE_VERSION = 1;
    private static final String DATA_BASE_NAME = "products.db";

    public StoreDbHelper(Context context) {
        super(context, DATA_BASE_NAME, null, DATA_BASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PRODUCTS_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_PRODUCTS_ENTRIES);

        onCreate(db);
    }

}
