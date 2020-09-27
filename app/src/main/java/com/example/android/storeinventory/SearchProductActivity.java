package com.example.android.storeinventory;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.storeinventory.customClasses.OnTextAndSpinnerChanged;
import com.example.android.storeinventory.data.StoreContract.ProductEntry;

import java.util.ArrayList;

public class SearchProductActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

    Spinner spinnerSearchMode;
    Spinner spinnerName;
    Spinner spinnerPrice;
    Spinner spinnerQuantity;
    Spinner spinnerSupplierName;

    EditText name;
    EditText price;
    EditText quantity;
    EditText supplierName;

    ArrayList<String> names;
    ArrayList<Double> prices;
    ArrayList<Integer> quantities;
    ArrayList<String> supplierNames;

    public static boolean nameTextChanged = false;
    public static boolean priceTextChanged = false;
    public static boolean quantityTextChanged = false;
    public static boolean supplierNameTextChanged = false;

    public static boolean spinnerNameChanged = false;
    public static boolean spinnerSupplierNameChanged = false;

    private static final int SEARCH_LOADER = 0;

    private DialogInterface.OnClickListener discardButtonClickListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    findViewById(R.id.back_to_main_screen).callOnClick();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_product);
        setTitle(getString(R.string.search_products));

        init();

        setUpOnTextAndSpinnerChanged();

        getLoaderManager().initLoader(SEARCH_LOADER , null, this);

        name.setText("");
        price.setText("");
        quantity.setText("");
        supplierName.setText("");

        SharedPreferences searchOptions = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        int options = Integer.parseInt(searchOptions.getString(getString(R.string.default_search_options_key), "1"));

        if (options < 5)
            spinnerSearchMode.setSelection(0);
        else spinnerSearchMode.setSelection(1);

        spinnerName.setSelection(0);
        spinnerPrice.setSelection(0);
        spinnerQuantity.setSelection(0);
        spinnerSupplierName.setSelection(0);

        SharedPreferences searchStyle = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        int style = Integer.parseInt(searchStyle.getString(getString(R.string.search_style_key), "1"));

        SearchProductActivity2.quickSearchVisible = style == 1;
        SearchProductActivity2.detailedSearchVisible = !SearchProductActivity2.quickSearchVisible;
    }

    private void setUpOnTextAndSpinnerChanged(){
        Button searchButton = (Button) findViewById(R.id.search);
        searchButton.setTextColor(Color.RED);

        OnTextAndSpinnerChanged.searchRootView = findViewById(R.id.root_view_search);

        name.addTextChangedListener(new OnTextAndSpinnerChanged("Name"));
        price.addTextChangedListener(new OnTextAndSpinnerChanged("Price"));
        quantity.addTextChangedListener(new OnTextAndSpinnerChanged("Quantity"));
        supplierName.addTextChangedListener(new OnTextAndSpinnerChanged("Supplier Name"));
        spinnerName.setOnItemSelectedListener(new OnTextAndSpinnerChanged("Spinner Name"));
        spinnerSupplierName.setOnItemSelectedListener(new OnTextAndSpinnerChanged("Spinner Supplier Name"));
    }

    private void init(){
        spinnerSearchMode = (Spinner) findViewById(R.id.spinner_search_mode);
        spinnerName = (Spinner) findViewById(R.id.spinner_name);
        spinnerPrice = (Spinner) findViewById(R.id.spinner_price);
        spinnerQuantity = (Spinner) findViewById(R.id.spinner_quantity);
        spinnerSupplierName = (Spinner) findViewById(R.id.spinner_supplier_name);

        name = (EditText) findViewById(R.id.name);
        price = (EditText) findViewById(R.id.price);
        quantity = (EditText) findViewById(R.id.quantity);
        supplierName = (EditText) findViewById(R.id.supplier_name);

        names = new ArrayList<>();
        prices = new ArrayList<>();
        quantities = new ArrayList<>();
        supplierNames = new ArrayList<>();
    }

    private void setUpSpinners(){
        ArrayAdapter<String> nameSpinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, names);

        nameSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        spinnerName.setAdapter(nameSpinnerAdapter);

        ArrayAdapter<String> supplierNameSpinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, supplierNames);

        supplierNameSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        spinnerSupplierName.setAdapter(supplierNameSpinnerAdapter);
    }

    public void search(View view){
        try {
            String currentSearchMode = spinnerSearchMode.getSelectedItem().toString();
            String currentName = spinnerName.getSelectedItem().toString();
            String currentPrice = spinnerPrice.getSelectedItem().toString();
            String currentQuantity = spinnerQuantity.getSelectedItem().toString();
            String currentSupplierName = spinnerSupplierName.getSelectedItem().toString();

            String productName = name.getText().toString().trim();
            String productPrice = price.getText().toString().trim();
            String productQuantity = quantity.getText().toString().trim();
            String productSupplierName = supplierName.getText().toString().trim();

            boolean searchName;
            boolean searchPrice;
            boolean searchQuantity;
            boolean searchSupplierName;

            Button search = (Button) findViewById(R.id.search);

            if (search.getCurrentTextColor() == Color.RED){
                Toast.makeText(getBaseContext(), "Provide at least 1 field to search", Toast.LENGTH_SHORT).show();
                return;
            }

            names.remove(getString(R.string.empty));
            supplierNames.remove(getString(R.string.empty));

            ArrayList<Long> idsSearched = new ArrayList<>();
            ArrayList<String> namesSearched = new ArrayList<>();
            ArrayList<Double> pricesSearched = new ArrayList<>();
            ArrayList<Integer> quantitiesSearched = new ArrayList<>();
            ArrayList<String> supplierNamesSearched = new ArrayList<>();
            ArrayList<String> supplierNamesSearchedHelper = new ArrayList<>();

            Cursor cursor;
            String[] projection;
            String selection;
            String[] selectionArgs;

            boolean maybeNoMatch = false;

            if (currentSearchMode.equals(getString(R.string.and))){

                if (!currentName.equals(getString(R.string.empty))){
                    namesSearched.add(currentName);
                }else {
                    if (!productName.isEmpty()){
                        for (String name : names){
                            if (containsIgnoreCase(name, productName))
                                namesSearched.add(name);
                        }

                        maybeNoMatch = true;
                    }
                }

                searchName = namesSearched.size() != 0;

                if (!searchName && maybeNoMatch)
                    throw new IllegalArgumentException("There is no matching product");
                else maybeNoMatch = false;

                if (searchName)
                    idsSearched = getIdsFromNames(namesSearched);

                if (!productPrice.isEmpty()){
                    double priceInEditText = Double.parseDouble(productPrice);

                    if (searchName){
                        prices = new ArrayList<>();

                        for (String name : namesSearched){
                            projection = new String[] {ProductEntry.COLUMN_PRODUCT_PRICE};
                            selection = ProductEntry.COLUMN_PRODUCT_NAME + "=?";
                            selectionArgs = new String[] { name };
                            cursor = getContentResolver().query(
                                    ProductEntry.CONTENT_URI,
                                    projection,
                                    selection,
                                    selectionArgs, null);
                            if (cursor != null){
                                while (cursor.moveToNext())
                                    prices.add( cursor.getDouble(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE)) );

                                cursor.close();
                            }
                        }
                    }

                    if (prices.size() != 0){
                        if (currentPrice.equals(getString(R.string.or_more))){

                            for (double price : prices){
                                if (priceInEditText <= price)
                                    pricesSearched.add(price);
                            }

                        }else if (currentPrice.equals(getString(R.string.or_less))){

                            for (double price : prices){
                                if (priceInEditText >= price)
                                    pricesSearched.add(price);
                            }

                        }else {

                            for (double price : prices){
                                if (priceInEditText == price)
                                    pricesSearched.add(price);
                            }

                        }
                    }

                    maybeNoMatch = true;
                }

                searchPrice = pricesSearched.size() != 0;

                if (!searchPrice && maybeNoMatch)
                    throw new IllegalArgumentException("There is no matching product");
                else maybeNoMatch = false;

                if (searchPrice)
                    idsSearched = getIdsFromPrices(pricesSearched);

                if (!productQuantity.isEmpty()){
                    int QuantityInEditText = Integer.parseInt(productQuantity);

                    if (searchPrice){
                        quantities = new ArrayList<>();

                        for (double price : pricesSearched){
                            projection = new String[] {ProductEntry.COLUMN_PRODUCT_QUANTITY};
                            selection = ProductEntry.COLUMN_PRODUCT_PRICE + "=?";
                            selectionArgs = new String[] { String.valueOf(price) };
                            cursor = getContentResolver().query(
                                    ProductEntry.CONTENT_URI,
                                    projection,
                                    selection,
                                    selectionArgs, null);
                            if (cursor != null){
                                while (cursor.moveToNext())
                                    quantities.add( cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY)) );

                                cursor.close();
                            }
                        }
                    }else if (searchName){
                        quantities = new ArrayList<>();

                        for (String name : namesSearched){
                            projection = new String[] {ProductEntry.COLUMN_PRODUCT_QUANTITY};
                            selection = ProductEntry.COLUMN_PRODUCT_NAME + "=?";
                            selectionArgs = new String[] { name };
                            cursor = getContentResolver().query(
                                    ProductEntry.CONTENT_URI,
                                    projection,
                                    selection,
                                    selectionArgs, null);
                            if (cursor != null){
                                while (cursor.moveToNext())
                                    quantities.add( cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY)) );

                                cursor.close();
                            }
                        }
                    }

                    if (quantities.size() != 0){
                        if (currentQuantity.equals(getString(R.string.or_more))){

                            for (int quantity : quantities){
                                if (QuantityInEditText <= quantity)
                                    quantitiesSearched.add(quantity);
                            }

                        }else if (currentQuantity.equals(getString(R.string.or_less))){

                            for (int quantity : quantities){
                                if (QuantityInEditText >= quantity)
                                    quantitiesSearched.add(quantity);
                            }

                        }else {

                            for (int quantity : quantities){
                                if (QuantityInEditText == quantity)
                                    quantitiesSearched.add(quantity);
                            }

                        }
                    }

                    maybeNoMatch = true;
                }

                searchQuantity = quantitiesSearched.size() != 0;

                if (!searchQuantity && maybeNoMatch)
                    throw new IllegalArgumentException("There is no matching product");
                else maybeNoMatch = false;

                if (searchQuantity)
                    idsSearched = getIdsFromQuantities(quantitiesSearched);

                if (!currentSupplierName.equals(getString(R.string.empty)) || !productSupplierName.isEmpty()){
                    if (!currentSupplierName.equals(getString(R.string.empty))){
                        supplierNamesSearched.add(currentSupplierName);
                    }else {
                        for (String supplierName : supplierNames){
                            if (containsIgnoreCase(supplierName, productSupplierName))
                                supplierNamesSearched.add(supplierName);
                        }
                    }

                    if (searchQuantity){
                        for (int quantity : quantitiesSearched){
                            projection = new String[] {ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME};
                            selection = ProductEntry.COLUMN_PRODUCT_QUANTITY + "=?";
                            selectionArgs = new String[] { String.valueOf(quantity) };
                            cursor = getContentResolver().query(
                                    ProductEntry.CONTENT_URI,
                                    projection,
                                    selection,
                                    selectionArgs, null);
                            if (cursor != null){
                                while (cursor.moveToNext())
                                    supplierNamesSearchedHelper.add( cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME)) );

                                cursor.close();
                            }
                        }
                    }else if (searchPrice){
                        for (double price : pricesSearched){
                            projection = new String[] {ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME};
                            selection = ProductEntry.COLUMN_PRODUCT_PRICE + "=?";
                            selectionArgs = new String[] { String.valueOf(price) };
                            cursor = getContentResolver().query(
                                    ProductEntry.CONTENT_URI,
                                    projection,
                                    selection,
                                    selectionArgs, null);
                            if (cursor != null){
                                while (cursor.moveToNext())
                                    supplierNamesSearchedHelper.add( cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME)) );

                                cursor.close();
                            }
                        }
                    }else if (searchName){
                        for (String name : namesSearched){
                            projection = new String[] {ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME};
                            selection = ProductEntry.COLUMN_PRODUCT_NAME + "=?";
                            selectionArgs = new String[] { name };
                            cursor = getContentResolver().query(
                                    ProductEntry.CONTENT_URI,
                                    projection,
                                    selection,
                                    selectionArgs, null);
                            if (cursor != null){
                                while (cursor.moveToNext())
                                    supplierNamesSearchedHelper.add( cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME)) );

                                cursor.close();
                            }
                        }
                    }

                    if (supplierNamesSearchedHelper.size() != 0)
                        supplierNamesSearched = getCommonNames(supplierNamesSearched, supplierNamesSearchedHelper);

                    maybeNoMatch = true;
                }

                searchSupplierName = supplierNamesSearched.size() != 0;

                if (!searchSupplierName && maybeNoMatch)
                    throw new IllegalArgumentException("There is no matching product");

                if (searchSupplierName)
                    idsSearched = getIdsFromSupplierNames(supplierNamesSearched);
            }else {

                if (!currentName.equals(getString(R.string.empty))){
                    namesSearched.add(currentName);
                }else {
                    if (!productName.isEmpty()){
                        for (String name : names){
                            if (containsIgnoreCase(name, productName))
                                namesSearched.add(name);
                        }
                    }
                }

                searchName = namesSearched.size() != 0;

                if (searchName)
                    idsSearched = getIdsFromNames(namesSearched);

                if (!productPrice.isEmpty()){
                    double priceInEditText = Double.parseDouble(productPrice);

                    if (prices.size() != 0){
                        if (currentPrice.equals(getString(R.string.or_more))){
                            for (double price : prices){
                                if (priceInEditText <= price)
                                    pricesSearched.add(price);
                            }
                        }else if (currentPrice.equals(getString(R.string.or_less))){
                            for (double price : prices){
                                if (priceInEditText >= price)
                                    pricesSearched.add(price);
                            }
                        }else {
                            for (double price : prices){
                                if (priceInEditText == price)
                                    pricesSearched.add(price);
                            }
                        }
                    }
                }

                searchPrice = pricesSearched.size() != 0;

                if (searchPrice && searchName)
                    idsSearched = removeDuplicatedIds(getIdsFromPrices(pricesSearched), idsSearched);
                else if (searchPrice)
                    idsSearched = getIdsFromPrices(pricesSearched);

                if (!productQuantity.isEmpty()){
                    int QuantityInEditText = Integer.parseInt(productQuantity);

                    if (quantities.size() != 0){
                        if (currentQuantity.equals(getString(R.string.or_more))){
                            for (int quantity : quantities){
                                if (QuantityInEditText <= quantity)
                                    quantitiesSearched.add(quantity);
                            }
                        }else if (currentQuantity.equals(getString(R.string.or_less))){
                            for (int quantity : quantities){
                                if (QuantityInEditText >= quantity)
                                    quantitiesSearched.add(quantity);
                            }
                        }else {
                            for (int quantity : quantities){
                                if (QuantityInEditText == quantity)
                                    quantitiesSearched.add(quantity);
                            }
                        }
                    }
                }

                searchQuantity = quantitiesSearched.size() != 0;

                if (searchQuantity && (searchPrice || searchName))
                    idsSearched = removeDuplicatedIds(getIdsFromQuantities(quantitiesSearched), idsSearched);
                else if (searchQuantity)
                    idsSearched = getIdsFromQuantities(quantitiesSearched);

                if (!currentSupplierName.equals(getString(R.string.empty))){
                    supplierNamesSearched.add(currentSupplierName);
                }else {
                    if (!productSupplierName.isEmpty()){
                        for (String supplierName : supplierNames){
                            if (containsIgnoreCase(supplierName, productSupplierName))
                                supplierNamesSearched.add(supplierName);
                        }
                    }
                }

                searchSupplierName = supplierNamesSearched.size() != 0;

                if (searchSupplierName && (searchQuantity || searchPrice || searchName))
                    idsSearched = removeDuplicatedIds(getIdsFromSupplierNames(supplierNamesSearched), idsSearched);
                else if (searchSupplierName)
                    idsSearched = getIdsFromSupplierNames(supplierNamesSearched);

                if (idsSearched.size() == 0)
                    throw new IllegalArgumentException("There is no matching product");
            }

            Button anotherSearch = (Button) findViewById(R.id.perform_another_search);
            Button mainScreen = (Button) findViewById(R.id.back_to_main_screen);
            ListView searchListView = (ListView)findViewById(R.id.search_list_view);
            LinearLayout searchLayout = (LinearLayout) findViewById(R.id.search_layout);

            searchLayout.setVisibility(View.GONE);
            anotherSearch.setVisibility(View.VISIBLE);
            mainScreen.setVisibility(View.VISIBLE);
            searchListView.setVisibility(View.VISIBLE);

            SearchCursorAdapter searchCursorAdapter = new SearchCursorAdapter(this, null);

            searchListView.setAdapter(searchCursorAdapter);

            searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    EditorActivity.backToSearch = true;
                    Uri data = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                    Intent intent = new Intent(SearchProductActivity.this, EditorActivity.class);
                    intent.setData(data);
                    startActivity(intent);
                }
            });

            projection = new String[] {
                    ProductEntry._ID,
                    ProductEntry.COLUMN_PRODUCT_NAME,
                    ProductEntry.COLUMN_PRODUCT_PRICE,
                    ProductEntry.COLUMN_PRODUCT_QUANTITY,
                    ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME};

            selection = ProductEntry._ID + "=?" + additionalSelection(idsSearched.size());

            String[] argsArr = new String[idsSearched.size()];
            argsArr = longToStringArrayLists(idsSearched).toArray(argsArr);

            selectionArgs = argsArr;

            cursor = getContentResolver().query(
                    ProductEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs, null);

            if (cursor != null)
                searchCursorAdapter.swapCursor(cursor);

        }catch (IllegalArgumentException e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

            names.add(0, getString(R.string.empty));
            supplierNames.add(0, getString(R.string.empty));

            prices = new ArrayList<>();
            quantities = new ArrayList<>();

            String[] projection = {ProductEntry.COLUMN_PRODUCT_PRICE, ProductEntry.COLUMN_PRODUCT_QUANTITY};

            Cursor cursor = getContentResolver().query(ProductEntry.CONTENT_URI, projection, null, null, null);

            if (cursor != null){
                while (cursor.moveToNext()){
                    prices.add( cursor.getDouble(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE)) );
                    quantities.add( cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY)) );
                }

                cursor.close();
            }
        }
    }

    private ArrayList<Long> removeDuplicatedIds(ArrayList<Long> ids1, ArrayList<Long> ids2){
        if (ids2.size() == 0)
            return ids1;
        else if (ids1.size() == 0)
            return ids2;
        else if (ids1.size() == 0 && ids2.size() == 0)
            return new ArrayList<>();

        ArrayList<Long> ids = new ArrayList<>();

        int counter = 0;

        for (long id1 : ids1){
            for (long id2 : ids2){
                if (id1 == id2)
                    counter ++;
            }

            if (counter == 0)
                ids.add(id1);
            else {
                ids.add(id1);
                ids2.remove(id1);
                counter = 0;
            }
        }

        if (ids2.size() != 0){
            for (long id2 : ids2)
                ids.add(id2);
        }

        return ids;
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

    private ArrayList<String> getCommonNames(ArrayList<String> names, ArrayList<String> namesHelper){
        ArrayList<String> commonNames = new ArrayList<>();

        for (String name : names){
            for (String nameHelper : namesHelper){
                if (name.equals(nameHelper)){
                    commonNames.add(name);
                    break;
                }
            }
        }

        return commonNames;
    }

    @Override
    protected void onResume() {
        ListView searchListView = (ListView) findViewById(R.id.search_list_view);
        Button anotherSearch = (Button) findViewById(R.id.perform_another_search);

        if (searchListView.getVisibility() == View.VISIBLE)
            anotherSearch.callOnClick();

        super.onResume();
    }

    private String additionalSelection(int repeatedSentence){
        String sentence = "";

        for (int i = 1; i < repeatedSentence; i++)
            sentence += " OR " + ProductEntry._ID + "=?";

        return sentence;
    }

    private ArrayList<String> longToStringArrayLists(ArrayList<Long> ids){
        ArrayList<String> stringIds = new ArrayList<>();

        for (long id : ids)
            stringIds.add(String.valueOf(id));

        return stringIds;
    }

    private ArrayList<Long> getIdsFromNames(ArrayList<String> names){
        ArrayList<Long> ids = new ArrayList<>();

        Cursor cursor;
        String[] projection;
        String selection;
        String[] selectionArgs;

        for (String name : names){
            projection = new String[] {ProductEntry._ID};
            selection = ProductEntry.COLUMN_PRODUCT_NAME + "=?";
            selectionArgs = new String[] { name };
            cursor = getContentResolver().query(
                    ProductEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs, null);
            if (cursor != null){
                while (cursor.moveToNext())
                    ids.add( cursor.getLong(cursor.getColumnIndex(ProductEntry._ID)) );

                cursor.close();
            }
        }

        return ids;
    }

    private ArrayList<Long> getIdsFromPrices(ArrayList<Double> prices){

        ArrayList<Long> ids = new ArrayList<>();

        Cursor cursor;
        String[] projection;
        String selection;
        String[] selectionArgs;

        for (double price : prices){
            projection = new String[] {ProductEntry._ID};
            selection = ProductEntry.COLUMN_PRODUCT_PRICE + "=?";
            selectionArgs = new String[] { String.valueOf(price) };
            cursor = getContentResolver().query(
                    ProductEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs, null);
            if (cursor != null){
                while (cursor.moveToNext())
                    ids.add( cursor.getLong(cursor.getColumnIndex(ProductEntry._ID)) );

                cursor.close();
            }
        }

        return ids;
    }

    private ArrayList<Long> getIdsFromQuantities(ArrayList<Integer> quantities){

        ArrayList<Long> ids = new ArrayList<>();

        Cursor cursor;
        String[] projection;
        String selection;
        String[] selectionArgs;

        for (int quantity : quantities){
            projection = new String[] {ProductEntry._ID};
            selection = ProductEntry.COLUMN_PRODUCT_QUANTITY + "=?";
            selectionArgs = new String[] { String.valueOf(quantity) };
            cursor = getContentResolver().query(
                    ProductEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs, null);
            if (cursor != null){
                while (cursor.moveToNext())
                    ids.add( cursor.getLong(cursor.getColumnIndex(ProductEntry._ID)) );

                cursor.close();
            }
        }

        return ids;
    }

    private ArrayList<Long> getIdsFromSupplierNames(ArrayList<String> supplierNames){
        ArrayList<Long> ids = new ArrayList<>();

        Cursor cursor;
        String[] projection;
        String selection;
        String[] selectionArgs;

        for (String supplierName : supplierNames){
            projection = new String[] {ProductEntry._ID};
            selection = ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME + "=?";
            selectionArgs = new String[] { supplierName };
            cursor = getContentResolver().query(
                    ProductEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs, null);

            if (cursor != null){
                while (cursor.moveToNext())
                    ids.add( cursor.getLong(cursor.getColumnIndex(ProductEntry._ID)) );

                cursor.close();
            }
        }

        return ids;
    }

    public void anotherSearch(View view){
        Button anotherSearch = (Button) findViewById(R.id.perform_another_search);
        Button mainScreen = (Button) findViewById(R.id.back_to_main_screen);
        ListView searchListView = (ListView)findViewById(R.id.search_list_view);
        LinearLayout searchLayout = (LinearLayout) findViewById(R.id.search_layout);

        searchListView.setAdapter(null);
        searchListView.setVisibility(View.GONE);
        anotherSearch.setVisibility(View.GONE);
        mainScreen.setVisibility(View.GONE);
        searchLayout.setVisibility(View.VISIBLE);

        name.setText("");
        price.setText("");
        quantity.setText("");
        supplierName.setText("");

        names.add(0, getString(R.string.empty));
        supplierNames.add(0, getString(R.string.empty));

        spinnerSearchMode.setSelection(0);
        spinnerName.setSelection(0);
        spinnerPrice.setSelection(0);
        spinnerQuantity.setSelection(0);
        spinnerSupplierName.setSelection(0);
    }

    public void toMainScreen(View view){
        name.setText("");
        price.setText("");
        quantity.setText("");
        supplierName.setText("");

        names.add(0, getString(R.string.empty));
        supplierNames.add(0, getString(R.string.empty));

        spinnerSearchMode.setSelection(0);
        spinnerName.setSelection(0);
        spinnerPrice.setSelection(0);
        spinnerQuantity.setSelection(0);
        spinnerSupplierName.setSelection(0);

        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME};

        return new CursorLoader(this,
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        names = new ArrayList<>();
        prices = new ArrayList<>();
        quantities = new ArrayList<>();
        supplierNames = new ArrayList<>();

        names.add(getString(R.string.empty));
        supplierNames.add(getString(R.string.empty));

        while (cursor.moveToNext()){
            names.add( cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME)) );
            prices.add( cursor.getDouble(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE)) );
            quantities.add( cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY)) );
            supplierNames.add( cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME)) );
        }

        setUpSpinners();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        names = new ArrayList<>();
        prices = new ArrayList<>();
        quantities = new ArrayList<>();
        supplierNames = new ArrayList<>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (((Button) findViewById(R.id.search)).getCurrentTextColor() == Color.BLUE){
            switch (item.getItemId()){
                case android.R.id.home:
                    if (findViewById(R.id.back_to_main_screen).getVisibility() == View.VISIBLE){
                        findViewById(R.id.perform_another_search).callOnClick();
                        return true;
                    }

                    showUnsavedChangesDialog(discardButtonClickListener);
                    return true;
            }
        }

        switch (item.getItemId()){
            case R.id.quick_search:
                SearchProductActivity2.detailedSearchVisible = true;
                SearchProductActivity2.quickSearchVisible = false;

                SharedPreferences searchStyle = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor changeDefaultSearch = searchStyle.edit();
                changeDefaultSearch.putString(getString(R.string.search_style_key), "2");
                changeDefaultSearch.apply();

                startActivity(new Intent(SearchProductActivity.this, SearchProductActivity2.class));
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

        detailedSearch.setEnabled(SearchProductActivity2.detailedSearchVisible);
        quickSearch.setEnabled(SearchProductActivity2.quickSearchVisible);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.back_to_main_screen).getVisibility() == View.VISIBLE){
            findViewById(R.id.perform_another_search).callOnClick();
            return;
        }

        if (((Button) findViewById(R.id.search)).getCurrentTextColor() == Color.BLUE)
            showUnsavedChangesDialog(discardButtonClickListener);
        else super.onBackPressed();
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
}
