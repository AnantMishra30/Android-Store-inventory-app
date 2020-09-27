package com.example.android.storeinventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class StoreContract {

    private StoreContract(){}

    public static final String CONTENT_AUTHORITY = "com.example.android.storeinventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PRODUCTS = "products";

    public static final class ProductEntry implements BaseColumns{

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final String TABLE_NAME = "products";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "name";
        public static final String COLUMN_PRODUCT_PRICE = "price";
        public static final String COLUMN_PRODUCT_QUANTITY = "quantity";
        public static final String COLUMN_PRODUCT_PIC = "pic";
        public static final String COLUMN_PRODUCT_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_PRODUCT_SUPPLIER_E_MAIL = "supplier_e_mail";
        public static final String COLUMN_PRODUCT_SUPPLIER_PHONE = "supplier_phone";
        public static final String COLUMN_PRODUCT_INFO = "info";

    }
}
