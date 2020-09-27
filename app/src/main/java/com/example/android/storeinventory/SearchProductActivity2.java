package com.example.android.storeinventory;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.storeinventory.data.StoreContract.ProductEntry;

import java.util.ArrayList;

public class SearchProductActivity2 extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

    android.widget.SearchView searchView;

    ListView listView;

    StringListAdapter adapter;

    private static final int SEARCH_LOADER = 0;

    ArrayList<String> productNames;
    ArrayList<String> suppNames;
    ArrayList<String> suppEMails;
    ArrayList<String> suppPhones;

    ArrayList<String> currentList;

    Spinner searchBy;

    Context context;

    String tempString;

    public static boolean detailedSearchVisible;
    public static boolean quickSearchVisible;

    private DialogInterface.OnClickListener discardButtonClickListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    searchView.setQuery("", true);
                    onBackPressed();
                }
            };

    Cursor cursor;
    String[] projection;
    String selection;
    String[] selectionArgs;

    String searchByString;
    String clickedString;
    String columnName;
    long ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_product_2);
        setTitle(getString(R.string.search_products));

        searchBy = (Spinner) findViewById(R.id.search_by);

        SharedPreferences searchOptions = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        int options = Integer.parseInt(searchOptions.getString(getString(R.string.default_search_options_key), "1"));

        if (options == 1 || options == 5)
            searchBy.setSelection(0);
        else if (options == 2 || options == 6)
            searchBy.setSelection(1);
        else if (options == 3 || options == 7)
            searchBy.setSelection(2);
        else searchBy.setSelection(3);

        productNames = new ArrayList<>();
        suppNames = new ArrayList<>();
        suppEMails = new ArrayList<>();
        suppPhones = new ArrayList<>();

        context = this;

        currentList = new ArrayList<>();

        getLoaderManager().initLoader(SEARCH_LOADER , null, this);

        listView = (ListView) findViewById(R.id.searchable_list_view);
        listView.setEmptyView(findViewById(R.id.no_results));
        // Making the Default List ==> Product Names
        adapter = new StringListAdapter(this, productNames);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                searchByString = searchBy.getSelectedItem().toString();

                clickedString = ((TextView) view.findViewById(R.id.single_text)).getText().toString();

                if (searchByString.equals( getString(R.string.name) ))
                    columnName = ProductEntry.COLUMN_PRODUCT_NAME;
                else if (searchByString.equals( getString(R.string.supplier_name_2) ))
                    columnName = ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME;
                else if (searchByString.equals( getString(R.string.supplier_e_mail_2) ))
                    columnName = ProductEntry.COLUMN_PRODUCT_SUPPLIER_E_MAIL;
                else columnName = ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE;

                projection = new String[] {ProductEntry._ID};
                selection = columnName + "=?";
                selectionArgs = new String[] { clickedString };
                cursor = getContentResolver().query(
                        ProductEntry.CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs, null);
                if (cursor != null){
                    // use below code (commented) only when u wanna show multiple data from DB it is not now anyway
                    /*while (cursor.moveToNext())
                        prices.add( cursor.getDouble(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE)) );*/
                    if (cursor.moveToNext())
                        ID = cursor.getLong(cursor.getColumnIndex(ProductEntry._ID));

                    cursor.close();
                }

                EditorActivity.backToSearch = true;
                Uri data = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, ID);
                Intent intent = new Intent(SearchProductActivity2.this, EditorActivity.class);
                intent.setData(data);
                startActivity(intent);
            }
        });

        searchBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String currentItem = searchBy.getSelectedItem().toString();

                if (currentItem.equals(getString(R.string.name)))
                    currentList = productNames;
                else if (currentItem.equals(getString(R.string.supplier_name_2)))
                    currentList = suppNames;
                else if (currentItem.equals(getString(R.string.supplier_e_mail_2)))
                    currentList = suppEMails;
                else currentList = suppPhones;

                adapter = new StringListAdapter(context, currentList);
                listView.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        searchView = (android.widget.SearchView) findViewById(R.id.search_view_1);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setIconified(false);
            }
        });
        searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String string) {
                performSearch(string, currentList);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String string) {
                performSearch(string, currentList);
                return true;
            }
        });

        SharedPreferences searchStyle = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        int style = Integer.parseInt(searchStyle.getString(getString(R.string.search_style_key), "1"));

        quickSearchVisible = style == 1;
        detailedSearchVisible = !quickSearchVisible;
    }

    private void performSearch(String query, ArrayList<String> originalList){
        ArrayList<String> searchedList = new ArrayList<>();

        for (String word: originalList){
            if (containsIgnoreCase(word, query))
                searchedList.add(word);
        }

        adapter = new StringListAdapter(getBaseContext(), searchedList);
        listView.setAdapter(adapter);
    }

    private boolean containsIgnoreCase(String main, String part) {

        int size = main.length();

        if (main.contains(part))
            return true;

        if (main.equalsIgnoreCase(part))
            return true;

        for (int j = 1; j <= size; j++){
            for (int i = 0; i < size; i++){
                if (main.substring(i, i+j).equalsIgnoreCase(part))
                    return true;

                if (j > 1 && i == size-j) {
                    break;
                }
            }
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if (searchView.getQuery().toString().isEmpty())
                    super.onBackPressed();
                else showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            case R.id.detailed_search:
                detailedSearchVisible = false;
                quickSearchVisible = true;

                SharedPreferences searchStyle = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor changeDefaultSearch = searchStyle.edit();
                changeDefaultSearch.putString(getString(R.string.search_style_key), "1");
                changeDefaultSearch.apply();

                startActivity(new Intent(SearchProductActivity2.this, SearchProductActivity.class));
                finish();
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem detailedSearch = menu.findItem(R.id.detailed_search);
        MenuItem quickSearch = menu.findItem(R.id.quick_search);

        detailedSearch.setEnabled(detailedSearchVisible);
        quickSearch.setEnabled(quickSearchVisible);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (searchView.getQuery().toString().isEmpty())
            super.onBackPressed();
        else showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_E_MAIL,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE};

        return new CursorLoader(this,
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        productNames = new ArrayList<>();
        suppNames = new ArrayList<>();
        suppEMails = new ArrayList<>();
        suppPhones = new ArrayList<>();

        while (cursor.moveToNext()){
            productNames.add( cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME)) );
            suppNames.add( cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME)) );
            tempString = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_E_MAIL));
            if (!tempString.equals(""))
                suppEMails.add( tempString );
            tempString = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE));
            if (!tempString.equals(""))
                suppPhones.add( tempString );
        }

        String currentItem = searchBy.getSelectedItem().toString();

        if (currentItem.equals(getString(R.string.name)))
            currentList = productNames;
        else if (currentItem.equals(getString(R.string.supplier_name_2)))
            currentList = suppNames;
        else if (currentItem.equals(getString(R.string.supplier_e_mail_2)))
            currentList = suppEMails;
        else currentList = suppPhones;

        adapter = new StringListAdapter(context, currentList);
        listView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productNames = new ArrayList<>();
        suppNames = new ArrayList<>();
        suppEMails = new ArrayList<>();
        suppPhones = new ArrayList<>();
    }
}
