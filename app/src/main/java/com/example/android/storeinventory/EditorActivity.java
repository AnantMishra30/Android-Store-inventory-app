package com.example.android.storeinventory;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.android.storeinventory.data.StoreContract.ProductEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    int increaseDecreaseAmount = 0;

    EditText name;
    EditText price;
    EditText quantity;
    ImageView picture;
    EditText supplierName;
    EditText supplierEMail;
    EditText supplierPhone;
    EditText info;

    Button productPicture;
    Button productPicture2;

    private String pictureString;

    private final int REQUEST_CODE_GALLERY = 99;
    private final int REQUEST_IMAGE_CAPTURE = 88;

    private static final int EXISTING_PRODUCT_LOADER = 0;

    private Uri mCurrentProductUri;

    private boolean mProductHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    public static boolean backToSearch = false;

    private PackageManager packageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        Uri data = intent.getData();

        Button orderMore = (Button) findViewById(R.id.order_more);
        LinearLayout trackReceive = (LinearLayout) findViewById(R.id.track_and_receive);

        if (data == null) {
            setTitle(getString(R.string.add_a_product));
            invalidateOptionsMenu();

            orderMore.setVisibility(View.GONE);
            trackReceive.setVisibility(View.GONE);
        } else {
            setTitle(getString(R.string.edit_a_product));
            mCurrentProductUri = data;

            orderMore.setVisibility(View.VISIBLE);
            trackReceive.setVisibility(View.VISIBLE);
        }

        init();

        initOnTouchListenerSetUp();

        productPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
                    ActivityCompat.requestPermissions(
                            EditorActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_GALLERY);
                }
            }
        });

        productPicture2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)){
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (picture.getDrawable() != null){
                    ViewPicture.picture = picture.getDrawable();
                    startActivity(new Intent(EditorActivity.this, ViewPicture.class));
                }else Toast.makeText(getBaseContext(), "Add a Picture", Toast.LENGTH_SHORT).show();
            }
        });

        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_GALLERY){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
            }
            else {
                Toast.makeText(getApplicationContext(), "You don't have permission to access file location!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null){
            Uri uri = data.getData();

            try {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);

                options.inSampleSize = calculateInSampleSize(options, 200, 200);

                options.inJustDecodeBounds = false;

                picture.setImageBitmap(BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options));
            } catch (FileNotFoundException e) {
                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();

            try {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);

                options.inSampleSize = calculateInSampleSize(options, 200, 200);

                options.inJustDecodeBounds = false;

                picture.setImageBitmap(BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options));
            }catch (FileNotFoundException e){
                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        backToSearch = false;
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.done:
                try {
                    saveProduct();
                    finish();
                }catch (IllegalArgumentException exception){
                    Toast.makeText(getBaseContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (backToSearch && !mProductHasChanged){
                    super.onBackPressed();
                    return true;
                }

                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (backToSearch){
                                    finish();
                                }else NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String generateRandomUniqueString10Chars(File[] files){
        int a = 0;
        ArrayList<Integer> arrayList = new ArrayList<>();

        if (files != null){
            for (File file : files){
                String b = file.getName();
                int c = Integer.parseInt(b);
                arrayList.add(c);
            }

            for (;;){
                if (arrayList.contains(a))
                    a += 1;
                else break;
            }
        }

        return transform10Dig(a);
    }

    private String transform10Dig(int num){
        String numString = String.valueOf(num);

        int length = numString.length();

        if (length < 10){

            int lengthZeros = 10 - length;
            String zeros = "";

            for (int i=1; i<=lengthZeros; i++)
                zeros += "0";

            return zeros + numString;

        }else return numString;
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());

        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

        File[] files = directory.listFiles();

        String picName = generateRandomUniqueString10Chars(files);

        File path = new File(directory, picName);

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(path);

            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, fos);

            fos.close();
        } catch (IOException io) {
            Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_SHORT).show();
        }

        return picName;
    }

    private Bitmap loadImageFromStorage(String name) {

        ContextWrapper cw = new ContextWrapper(getApplicationContext());

        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

        try {
            File file = new File(directory.getAbsolutePath(), name);

            return decodeSampledBitmapFromResource(file);
        }catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private Bitmap decodeSampledBitmapFromResource(File file) {

        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 200, 200);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeStream(new FileInputStream(file), null, options);
        }catch (Exception e){
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void saveProduct(){
        String nameString = name.getText().toString().trim();

        String priceString = price.getText().toString().trim();
        double priceDouble = ( priceString.isEmpty() ) ? -1 : Double.parseDouble(priceString);

        String quantityString = quantity.getText().toString().trim();
        int quantityInt = ( quantityString.isEmpty() ) ? -1 : Integer.parseInt(quantityString);

        String pictureString;

        try {
            BitmapDrawable drawable = (BitmapDrawable) picture.getDrawable();
            Bitmap pic = drawable.getBitmap();
            pictureString = saveToInternalStorage(pic);
            Toast.makeText(getBaseContext(), "i suppose it is saved", Toast.LENGTH_LONG);
        }catch (Exception e){
            pictureString = null;
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG);
        }

        String supplierNameString = supplierName.getText().toString().trim();
        String supplierEMailString = supplierEMail.getText().toString().trim();
        if ( supplierEMailString.isEmpty() || !supplierEMailString.contains("@") )
            supplierEMailString = "";
        String supplierPhoneString = supplierPhone.getText().toString().trim();

        String infoString = info.getText().toString().trim();

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceDouble);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityInt);
        values.put(ProductEntry.COLUMN_PRODUCT_PIC, pictureString);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierNameString);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_E_MAIL, supplierEMailString);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, supplierPhoneString);
        values.put(ProductEntry.COLUMN_PRODUCT_INFO, infoString);

        if (getTitle() == getString(R.string.add_a_product)){
            Uri uri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            long newRowID = ContentUris.parseId(uri);

            if (newRowID == -1) {
                Toast.makeText(this, R.string.error_with_saving_product, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.product_saved, Toast.LENGTH_SHORT).show();
            }
        }else {
            String selection = ProductEntry._ID + "=?";
            String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(mCurrentProductUri))};
            int rowsUpdated = getContentResolver().update(mCurrentProductUri, values, selection, selectionArgs);

            if (rowsUpdated == 0) {
                Toast.makeText(this, R.string.error_with_updating_product, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.product_updated, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
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

    private void deleteProduct() {
        getContentResolver().delete(mCurrentProductUri, null, null);
        deleteImgFile(pictureString);
        Toast.makeText(getBaseContext(), R.string.product_deleted, Toast.LENGTH_SHORT).show();
        finish();
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

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

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

    private void init(){
        name = (EditText) findViewById(R.id.name);
        price = (EditText) findViewById(R.id.price);
        quantity = (EditText) findViewById(R.id.quantity);
        picture = (ImageView) findViewById(R.id.picture);
        supplierName = (EditText) findViewById(R.id.supplier_name);
        supplierEMail = (EditText) findViewById(R.id.supplier_e_mail);
        supplierPhone = (EditText) findViewById(R.id.supplier_phone);
        info = (EditText) findViewById(R.id.info);

        productPicture = (Button) findViewById(R.id.product_picture);
        productPicture2 = (Button) findViewById(R.id.product_picture_2);

        packageManager = getBaseContext().getPackageManager();
    }

    private void initOnTouchListenerSetUp(){
        name.setOnTouchListener(mTouchListener);
        price.setOnTouchListener(mTouchListener);
        quantity.setOnTouchListener(mTouchListener);
        productPicture.setOnTouchListener(mTouchListener);
        productPicture2.setOnTouchListener(mTouchListener);
        supplierName.setOnTouchListener(mTouchListener);
        supplierEMail.setOnTouchListener(mTouchListener);
        supplierPhone.setOnTouchListener(mTouchListener);
        info.setOnTouchListener(mTouchListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PIC,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_E_MAIL,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE,
                ProductEntry.COLUMN_PRODUCT_INFO};

        if (mCurrentProductUri != null) {
            return new CursorLoader(this,
                    mCurrentProductUri,
                    projection,
                    null,
                    null,
                    null);
        } else return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            name.setText(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME)));

            price.setText(String.valueOf( cursor.getDouble(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE)) ));

            quantity.setText(String.valueOf( cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY)) ));

            pictureString = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PIC));
            picture.setImageBitmap(loadImageFromStorage(pictureString));

            supplierName.setText(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME)));
            supplierEMail.setText(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_E_MAIL)));
            supplierPhone.setText(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE)));

            info.setText(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_INFO)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        name = null;
        price = null;
        quantity = null;
        picture = null;
        supplierName = null;
        supplierEMail = null;
        supplierPhone = null;
        info = null;

        mCurrentProductUri = null;
    }

    public void trackSale(View view){
        mProductHasChanged = true;

        reduceQuantity(true);
    }

    public void receiveShipment(View view){
        mProductHasChanged = true;

        reduceQuantity(false);
    }

    private void reduceQuantity(boolean reduce){
        String quantityValue = quantity.getText().toString();

        if (increaseDecreaseAmount == 0){
            if (!quantityValue.isEmpty() && reduce){
                if (Integer.parseInt(quantityValue) != 1)
                    quantity.setText( String.valueOf(Integer.parseInt(quantityValue) - 1) );
                else Toast.makeText(getBaseContext(), "There is only 1 of this product", Toast.LENGTH_SHORT).show();
            } else if (!quantityValue.isEmpty())
                quantity.setText( String.valueOf(Integer.parseInt(quantityValue) + 1) );
            else Toast.makeText(getBaseContext(), "Provide a valid quantity", Toast.LENGTH_SHORT).show();
        }else {
            if (!quantityValue.isEmpty() && reduce){
                if (Integer.parseInt(quantityValue) > increaseDecreaseAmount)
                    quantity.setText( String.valueOf(Integer.parseInt(quantityValue) - increaseDecreaseAmount) );
                else Toast.makeText(getBaseContext(), "Can't because this would result in\nzero or negative quantity", Toast.LENGTH_SHORT).show();
            }else if (!quantityValue.isEmpty())
                quantity.setText( String.valueOf(Integer.parseInt(quantityValue) + increaseDecreaseAmount) );
            else Toast.makeText(getBaseContext(), "Provide a valid quantity", Toast.LENGTH_SHORT).show();
        }
    }

    public void orderMore(View view){
        String eMail = supplierEMail.getText().toString().trim();
        String phone = supplierPhone.getText().toString().trim();

        if (eMail.isEmpty() && phone.isEmpty())
            Toast.makeText(getBaseContext(), "Provide an e-mail OR a phone\nTo contact the supplier", Toast.LENGTH_SHORT).show();
        else if (eMail.isEmpty()){
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            startActivity(intent);
        }else if (phone.isEmpty()){
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + eMail));
            startActivity(intent);
        }else {
            phoneOREMail(eMail, phone);
        }
    }

    private void phoneOREMail(final String eMail,final String phone) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Contact supplier with?");
        builder.setPositiveButton("E-Mail", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + eMail));
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Phone", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone));
                startActivity(intent);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void amountIncreasedOrDecreased(View view){
        final Button thisButton = (Button) view;

        final Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.amount);

        final EditText editTextAmount = (EditText) dialog.findViewById(R.id.edit_text_amount);

        Button ok = (Button) dialog.findViewById(R.id.ok);
        Button cancel = (Button) dialog.findViewById(R.id.cancel);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String num = editTextAmount.getText().toString();

                if (!num.isEmpty() && !num.equals("0")){
                    String text = "by " + num;

                    thisButton.setText(text);

                    increaseDecreaseAmount = Integer.parseInt(num);

                    dialog.dismiss();
                } else Toast.makeText(getBaseContext(), "Specify valid number", Toast.LENGTH_SHORT).show();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        //Grab the window of the dialog, and change the width
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
        //This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        dialog.show();
    }
}
