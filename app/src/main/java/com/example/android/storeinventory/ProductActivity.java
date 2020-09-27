package com.example.android.storeinventory;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.storeinventory.data.StoreContract.ProductEntry;

import java.io.File;
import java.util.ArrayList;

public class ProductActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

    ProductCursorAdapter pCursorAdapter;

    private static final int PRODUCT_LOADER = 0;

    private boolean changeFunctionality = true;

    private LinearLayout cancelDelete;
    private FloatingActionButton fab2;

    private TextView checkBox;
    private TextView deleteNum;
    private TextView cancelCaps;

    public static ArrayList<Integer> checkedItems;

    private boolean changeProductActionBar = false;
    private boolean changeSelectAll;
    private boolean changeDeselectAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        setTitle(getString(R.string.products));

        checkedItems = new ArrayList<>();

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProductActivity.this, EditorActivity.class));
            }
        });

        ListView productsList = (ListView) findViewById(R.id.product_list_view);
        productsList.setEmptyView(findViewById(R.id.product_empty_view));

        pCursorAdapter = new ProductCursorAdapter(this, null);
        productsList.setAdapter(pCursorAdapter);

        productsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (changeFunctionality){
                    Uri data = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                    Intent intent = new Intent(ProductActivity.this, EditorActivity.class);
                    intent.setData(data);
                    startActivity(intent);
                }else {
                    checkBox = (TextView) view.findViewById(R.id.checkbox_delete);
                    deleteNum = (TextView) findViewById(R.id.delete_num);

                    if (checkedItems.size() == 0 || !checkedItems.contains(position)){
                        checkBox.setBackgroundResource(R.drawable.check_box_selected);

                        checkedItems.add(position);

                        deleteNum.setAlpha(1);
                    }else {
                        checkBox.setBackgroundResource(R.drawable.check_box_not_selected);

                        checkedItems.remove(checkedItems.indexOf(position));

                        if (checkedItems.size() == 0)
                            deleteNum.setAlpha(0.5f);
                    }

                    String deleteMsg = "DELETE(" + String.valueOf(checkedItems.size()) + ")";
                    deleteNum.setText(deleteMsg);

                    changeSelectAll = pCursorAdapter.getCursor().getCount() == checkedItems.size();

                    changeDeselectAll = checkedItems.size() == 0;

                    invalidateOptionsMenu();
                }
            }
        });

        cancelCaps = (TextView) findViewById(R.id.cancel_caps);
        deleteNum = (TextView) findViewById(R.id.delete_num);

        cancelCaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab2.setVisibility(View.VISIBLE);
                cancelDelete.setVisibility(View.GONE);

                checkedItems = new ArrayList<>();
                deleteNum.setText(getString(R.string.delete_num));
                deleteNum.setAlpha(0.5f);

                pCursorAdapter.changeLayout = true;

                ListView listView = (ListView) findViewById(R.id.product_list_view);
                listView.setAdapter(pCursorAdapter);

                changeFunctionality = true;

                changeProductActionBar = false;
                invalidateOptionsMenu();
            }
        });

        deleteNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkedItems.size() != 0){
                    Cursor cursor;
                    String[] projection = {ProductEntry._ID, ProductEntry.COLUMN_PRODUCT_PIC};

                    long id = -1;
                    String pictureString = "";

                    ArrayList<Long> deletedIDS = new ArrayList<>();
                    ArrayList<String> picsPath = new ArrayList<>();

                    for (int position : checkedItems){
                        cursor = getContentResolver().query(ProductEntry.CONTENT_URI, projection, null, null, null);

                        if (cursor != null){
                            cursor.moveToPosition(position);

                            id = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));

                            pictureString = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PIC));

                            cursor.close();
                        }

                        deletedIDS.add(id);
                        picsPath.add(pictureString);
                    }

                    deletedIDS = arrangeDescendingOrder(deletedIDS);

                    for (long deleteID : deletedIDS){
                        getContentResolver().delete(ContentUris.withAppendedId(ProductEntry.CONTENT_URI, deleteID), null, null);
                    }

                    for (String picPath : picsPath){
                        deleteImgFile(picPath);
                    }

                    Toast.makeText(getBaseContext(), "Deletion has been completed ", Toast.LENGTH_SHORT).show();

                    cancelCaps.callOnClick();
                }
            }
        });

        getLoaderManager().initLoader(PRODUCT_LOADER , null, this);
    }

    private static ArrayList<Long> arrangeDescendingOrder(ArrayList<Long> arrayList){
        ArrayList<Long> temp = new ArrayList<>();

        for (int i = 0; i < arrayList.size(); i++){
            if (i != 0){
                for (int j = 0; j < i; j++){
                    if (arrayList.get(i) > temp.get(j)){
                        temp.add(temp.indexOf(temp.get(j)), arrayList.get(i));
                        break;
                    }else if (i-1 == j)
                        temp.add(arrayList.get(i));
                }
            }else temp.add(arrayList.get(0));
        }

        return temp;
    }

    private boolean deleteImgFile(String path){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());

        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

        try {
            File file = new File(directory.getAbsolutePath(), path);

            return file.delete();
        }catch (Exception e){
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ListView listView = (ListView) findViewById(R.id.product_list_view);
        String deleteMsg;

        switch (item.getItemId()){
            case R.id.search_product:
                SharedPreferences searchStyle = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                int style = Integer.parseInt(searchStyle.getString(getString(R.string.search_style_key), "1"));

                if (((ListView) findViewById(R.id.product_list_view)).getAdapter().getCount() != 0){
                    if (style == 1)
                        startActivity(new Intent(ProductActivity.this, SearchProductActivity.class));
                    else startActivity(new Intent(ProductActivity.this, SearchProductActivity2.class));
                } else Toast.makeText(getBaseContext(), "There are no products to search for", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.settings_product:
                startActivity(new Intent(ProductActivity.this, Settings.class));
                return true;
            case R.id.add_product:
                startActivity(new Intent(ProductActivity.this, EditorActivity.class));
                return true;
            case R.id.delete_product:
                if (((ListView) findViewById(R.id.product_list_view)).getAdapter().getCount() != 0){
                    cancelDelete = (LinearLayout) findViewById(R.id.product_cancel_delete);
                    fab2 = (FloatingActionButton) findViewById(R.id.fab);

                    fab2.setVisibility(View.GONE);
                    cancelDelete.setVisibility(View.VISIBLE);

                    pCursorAdapter.changeLayout = false;

                    listView.setAdapter(pCursorAdapter);

                    changeFunctionality = false;

                    changeProductActionBar = true;
                    changeSelectAll = false;
                    changeDeselectAll = true;
                    invalidateOptionsMenu();
                } else Toast.makeText(getBaseContext(), "There are no products to delete", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.delete_all_products:
                if (((ListView) findViewById(R.id.product_list_view)).getAdapter().getCount() != 0){
                    showDeleteConfirmationDialog();
                } else Toast.makeText(getBaseContext(), "There are no products to delete", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.select_all:
                checkedItems = new ArrayList<>();
                for (int i = 0; i < pCursorAdapter.getCursor().getCount(); i++)
                    checkedItems.add(i);
                listView.setAdapter(pCursorAdapter);
                deleteMsg = "DELETE(" + String.valueOf(checkedItems.size()) + ")";
                deleteNum.setText(deleteMsg);
                deleteNum.setAlpha(1);

                changeSelectAll = true;
                changeDeselectAll = false;
                invalidateOptionsMenu();
                return true;
            case R.id.deselect_all:
                checkedItems = new ArrayList<>();
                listView.setAdapter(pCursorAdapter);
                deleteMsg = "DELETE(" + String.valueOf(checkedItems.size()) + ")";
                deleteNum.setText(deleteMsg);
                deleteNum.setAlpha(0.5f);

                changeDeselectAll = true;
                changeSelectAll = false;
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem searchProduct = menu.findItem(R.id.search_product);
        MenuItem settingsProduct = menu.findItem(R.id.settings_product);
        MenuItem addProduct = menu.findItem(R.id.add_product);
        MenuItem delete = menu.findItem(R.id.delete_product);
        MenuItem deleteAll = menu.findItem(R.id.delete_all_products);

        MenuItem selectAll = menu.findItem(R.id.select_all);
        MenuItem deselectAll = menu.findItem(R.id.deselect_all);

        MenuItem[] defaultActionBar = {searchProduct, settingsProduct, addProduct, delete, deleteAll};
        MenuItem[] changedActionBar = {selectAll, deselectAll};

        if (changeProductActionBar) {
            for (MenuItem item : defaultActionBar)
                item.setVisible(false);
            for (MenuItem item : changedActionBar)
                item.setVisible(true);
        }

        selectAll.setEnabled(!changeSelectAll);
        deselectAll.setEnabled(!changeDeselectAll);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (!changeFunctionality)
            cancelCaps.callOnClick();
        else super.onBackPressed();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_products);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
                deleteAllImages();
                Toast.makeText(getBaseContext(), R.string.all_products_deleted, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean deleteAllImages(){
        boolean deleted = false;

        ContextWrapper cw = new ContextWrapper(getApplicationContext());

        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

        File[] files = directory.listFiles();

        for (File file : files){
            deleted = file.delete();
        }

        return deleted;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY};

        return new CursorLoader(this,
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        pCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        pCursorAdapter.swapCursor(null);
    }
}
