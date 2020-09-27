package com.example.android.storeinventory;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.storeinventory.data.StoreContract.ProductEntry;

public class ProductCursorAdapter extends CursorAdapter {

    public boolean changeLayout = true;

    private int quantityInt;

    private double priceDouble;

    private TextView quantity;
    private TextView price;

    private String quantityDefault;
    private String priceDefault;

    private Context mContext;

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.product_list_item, parent, false);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (changeLayout) {
            LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            if (convertView == null)
                convertView = mInflater.inflate(R.layout.product_list_item, parent, false);

            return super.getView(position, convertView, parent);
        } else {
            LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            if (convertView == null)
                convertView = mInflater.inflate(R.layout.product_list_item_2, parent, false);

            TextView checkBox = (TextView) convertView.findViewById(R.id.checkbox_delete);

            if (ProductActivity.checkedItems.size() == 0 || !ProductActivity.checkedItems.contains(position)){
                checkBox.setBackgroundResource(R.drawable.check_box_not_selected);
            }else {
                checkBox.setBackgroundResource(R.drawable.check_box_selected);
            }

            return super.getView(position, convertView, parent);
        }
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        // Below 2 lines of code just to get appropriate cursor position for trackSale button
        TextView uniqueIdCursor = (TextView) view.findViewById(R.id.unique_cursor_id);
        uniqueIdCursor.setText(String.valueOf(cursor.getPosition()));

        TextView name = (TextView) view.findViewById(R.id.name);
        price = (TextView) view.findViewById(R.id.price);
        quantity = (TextView) view.findViewById(R.id.quantity);

        priceDefault = context.getString(R.string.price);
        quantityDefault = context.getString(R.string.quantity);

        price.setText(priceDefault);
        quantity.setText(quantityDefault);

        String nameString = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME));
        priceDouble = cursor.getDouble(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE));
        quantityInt = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY));
        String quantityString = String.valueOf(quantityInt);
        String priceString = String.valueOf(priceDouble);

        name.setText(nameString);
        price.append(" " + priceString);
        quantity.append(" " + quantityString);

        TextView trackSale = (TextView) view.findViewById(R.id.track_sale);
        trackSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                quantity = (TextView) view.findViewById(R.id.quantity);
                price = (TextView) view.findViewById(R.id.price);

                quantityInt = Integer.parseInt( quantity.getText().toString().substring(11) );
                priceDouble = Double.parseDouble( price.getText().toString().substring(8) );

                try {
                    if (quantityInt-1 > 0){
                        quantityInt -= 1;
                        double temp;
                        temp = priceDouble * 5 / 100;
                        temp = Math.round(temp*100)/100;
                        if (temp == 0)
                            throw new IllegalArgumentException("No sale for this product\nAs it's price is already cheap");
                        quantity.setText(quantityDefault);
                        quantity.append( " " + String.valueOf(quantityInt) );
                        priceDouble -= temp;
                        price.setText(priceDefault);
                        price.append( " " + String.valueOf(priceDouble) );

                        TextView unique = (TextView) view.findViewById(R.id.unique_cursor_id);
                        int currentPos = Integer.parseInt(unique.getText().toString().trim());
                        cursor.moveToPosition(currentPos);

                        long id = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));

                        ContentValues values = new ContentValues();
                        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceDouble);
                        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityInt);

                        long updatedRow = context.getContentResolver().update(ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id),
                                values, null, null);
                        if (updatedRow == 0){
                            Toast.makeText(context, "NOT Updated", Toast.LENGTH_SHORT).show();
                        }else Toast.makeText(context, "updated", Toast.LENGTH_SHORT).show();
                    }else Toast.makeText(context, "No sale for this product\nAs it's quantity is 1", Toast.LENGTH_SHORT).show();
                }catch (IllegalArgumentException exception){
                    Toast.makeText(context, exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
