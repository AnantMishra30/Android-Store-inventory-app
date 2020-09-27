package com.example.android.storeinventory;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.storeinventory.data.StoreContract.ProductEntry;


public class SearchCursorAdapter extends CursorAdapter {

    public SearchCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.search_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView price = (TextView) view.findViewById(R.id.price);
        TextView quantity = (TextView) view.findViewById(R.id.quantity);
        TextView supplierName = (TextView) view.findViewById(R.id.supplier_name);

        String nameString = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME));
        String priceString = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE));
        String quantityString = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY));
        String supplierNameString = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME));

        nameString = "Name : " + nameString;
        priceString = "Price : " + priceString;
        quantityString = "Quantity : " + quantityString;
        supplierNameString = "Supplier Name : " + supplierNameString;

        name.setText(nameString);
        price.setText(priceString);
        quantity.setText(quantityString);
        supplierName.setText(supplierNameString);
    }
}
