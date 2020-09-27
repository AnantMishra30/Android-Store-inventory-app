package com.example.android.storeinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.example.android.storeinventory.data.StoreContract.ProductEntry;

public class StoreProvider extends ContentProvider {

    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;
    private static final int SUPPLIERS = 200;
    private static final int SUPPLIER_ID = 201;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    private StoreDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new StoreDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        final int match = sUriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{ String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case SUPPLIERS:
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case SUPPLIER_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{ String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        if (getContext() != null)
            cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

    }

    private Uri insertProduct(Uri uri, ContentValues values){

        String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (name.isEmpty())
            throw new IllegalArgumentException("Product requires a name");

        Double price = values.getAsDouble(ProductEntry.COLUMN_PRODUCT_PRICE);
        if (price <= 0)
            throw new IllegalArgumentException("Product requires a valid price");

        Integer quantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity <= 0)
            throw new IllegalArgumentException("Product requires a valid quantity");

        String pic = values.getAsString(ProductEntry.COLUMN_PRODUCT_PIC);
        if (pic == null || pic.isEmpty())
            throw new IllegalArgumentException("Product requires a valid picture of it");

        String supplierName = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
        if (supplierName.isEmpty())
            throw new IllegalArgumentException("Product requires a valid supplier name");

        // There must be at least an e-mail or a phone num to contact the supplier when we press 'Order More'
        String supplierEMail = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_E_MAIL);
        String supplierPhone = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);
        if (supplierEMail.isEmpty() && supplierPhone.isEmpty())
            throw new IllegalArgumentException("Provide e-mail OR phone\nSo that you can contact with supplier");

        // No need to check for info as it is optional

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(ProductEntry.TABLE_NAME,
                null,
                values);

        if (getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs){

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)){
            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (name.isEmpty())
                throw new IllegalArgumentException("Product requires a name");
        }

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)){
            Double price = values.getAsDouble(ProductEntry.COLUMN_PRODUCT_PRICE);
            if (price <= 0)
                throw new IllegalArgumentException("Product requires a valid price");
        }

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_QUANTITY)){
            Integer quantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity <= 0)
                throw new IllegalArgumentException("Product requires a valid quantity");
        }

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PIC)){
            String pic = values.getAsString(ProductEntry.COLUMN_PRODUCT_PIC);
            if (pic == null || pic.isEmpty())
                throw new IllegalArgumentException("Product requires a valid picture of it");
        }

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME)){
            String supplierName = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            if (supplierName.isEmpty())
                throw new IllegalArgumentException("Product requires a valid supplier name");
        }

        // There must be at least an e-mail or a phone num to contact the supplier when we press 'Order More'
        String supplierEMail = "", supplierPhone = "";
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_SUPPLIER_E_MAIL))
            supplierEMail = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_E_MAIL);
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE))
            supplierPhone = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);
        if (supplierEMail.isEmpty() && supplierPhone.isEmpty() && values.containsKey(ProductEntry.COLUMN_PRODUCT_SUPPLIER_E_MAIL)
                && values.containsKey(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE))
            throw new IllegalArgumentException("Provide e-mail OR phone\nSo that you can contact with supplier");

        // No need to check for info as it is optional

        if (values.size() == 0)
            return 0;

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0  && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0 && getContext()!= null)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;

    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
