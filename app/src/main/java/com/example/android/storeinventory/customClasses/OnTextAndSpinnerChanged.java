package com.example.android.storeinventory.customClasses;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

import com.example.android.storeinventory.R;
import com.example.android.storeinventory.SearchProductActivity;

public class OnTextAndSpinnerChanged implements TextWatcher , AdapterView.OnItemSelectedListener {

    public static View searchRootView;

    private String currentText;

    public OnTextAndSpinnerChanged(String text){
        currentText = text;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence s, int i, int i1, int i2) {
        String string = String.valueOf(s);

        switch (currentText){
            case "Name":
                SearchProductActivity.nameTextChanged = !string.isEmpty();
                break;
            case "Price":
                SearchProductActivity.priceTextChanged = !string.isEmpty();
                break;
            case "Quantity":
                SearchProductActivity.quantityTextChanged = !string.isEmpty();
                break;
            case "Supplier Name":
                SearchProductActivity.supplierNameTextChanged = !string.isEmpty();
                break;
        }

        changeButtonColor();
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    private void changeButtonColor(){
        Button search = (Button) searchRootView.findViewById(R.id.search);

        if (!SearchProductActivity.nameTextChanged && !SearchProductActivity.priceTextChanged && !SearchProductActivity.quantityTextChanged &&
                !SearchProductActivity.supplierNameTextChanged && !SearchProductActivity.spinnerNameChanged &&
                !SearchProductActivity.spinnerSupplierNameChanged)
            search.setTextColor(Color.RED);
        else search.setTextColor(Color.BLUE);
    }

    @Override
    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        EditText name = (EditText) searchRootView.findViewById(R.id.name);
        EditText supplierName = (EditText) searchRootView.findViewById(R.id.supplier_name);

        switch (currentText){
            case "Spinner Name":
                SearchProductActivity.spinnerNameChanged = position != 0;
                if (SearchProductActivity.spinnerNameChanged)
                    name.setEnabled(false);
                else name.setEnabled(true);
                break;
            case "Spinner Supplier Name":
                SearchProductActivity.spinnerSupplierNameChanged = position != 0;
                if (SearchProductActivity.spinnerSupplierNameChanged)
                    supplierName.setEnabled(false);
                else supplierName.setEnabled(true);
                break;
        }

        changeButtonColor();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parentView) {

    }
}
