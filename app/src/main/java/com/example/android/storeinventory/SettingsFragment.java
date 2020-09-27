package com.example.android.storeinventory;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment {

    private Dialog dialog;

    Spinner searchMode;
    Spinner searchBy;

    TextView slotName1;
    TextView slotName2;
    TextView slotName3;
    TextView slotName4;
    TextView slotName5;

    TextView mainText;

    boolean inSaveScreen = true;

    ListPreference defaultSearchStyle;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        defaultSearchStyle = (ListPreference) findPreference(getString(R.string.search_style_key));

        Preference defaultSearchOptionsKey = findPreference(getString(R.string.default_search_options_key));
        defaultSearchOptionsKey.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                performDSOK();
                return true;
            }
        });

        Preference saveCurrentSettings = findPreference(getString(R.string.save_current_settings_key));
        saveCurrentSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                performSCS();
                return true;
            }
        });

        Preference defaultSettingsKey = findPreference(getString(R.string.default_settings_key));
        defaultSettingsKey.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                performDSK();
                return true;
            }
        });
    }

    private void performDSOK(){
        dialog = new Dialog(Settings.context);

        dialog.setContentView(R.layout.custom_dialog);

        searchMode = (Spinner) dialog.findViewById(R.id.search_mode);
        searchBy = (Spinner) dialog.findViewById(R.id.search_by);

        SharedPreferences searchOptions = PreferenceManager.getDefaultSharedPreferences(Settings.context);
        int options = Integer.parseInt(searchOptions.getString(getString(R.string.default_search_options_key), "1"));

        switch (options){
            case 1:
                searchMode.setSelection(0);
                searchBy.setSelection(0);
                break;
            case 2:
                searchMode.setSelection(0);
                searchBy.setSelection(1);
                break;
            case 3:
                searchMode.setSelection(0);
                searchBy.setSelection(2);
                break;
            case 4:
                searchMode.setSelection(0);
                searchBy.setSelection(3);
                break;
            case 5:
                searchMode.setSelection(1);
                searchBy.setSelection(0);
                break;
            case 6:
                searchMode.setSelection(1);
                searchBy.setSelection(1);
                break;
            case 7:
                searchMode.setSelection(1);
                searchBy.setSelection(2);
                break;
            case 8:
                searchMode.setSelection(1);
                searchBy.setSelection(3);
                break;
        }

        Button save = (Button) dialog.findViewById(R.id.save);
        Button discard = (Button) dialog.findViewById(R.id.discard);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int modePosition = searchMode.getSelectedItemPosition();
                int byPosition = searchBy.getSelectedItemPosition();

                int caseNumber = getCase(modePosition, byPosition);

                SharedPreferences searchOptions = PreferenceManager.getDefaultSharedPreferences(Settings.context);
                SharedPreferences.Editor changeDefaultOptions = searchOptions.edit();
                changeDefaultOptions.putString(getString(R.string.default_search_options_key), String.valueOf(caseNumber));
                changeDefaultOptions.apply();

                dialog.dismiss();
                Toast.makeText(Settings.context, "Settings Saved", Toast.LENGTH_SHORT).show();
            }
        });

        discard.setOnClickListener(new View.OnClickListener() {
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

    private int getCase(int modePosition, int byPosition){
        int value;

        if (modePosition == 0 && byPosition == 0){
            value = 1;
        }else if (modePosition == 0 && byPosition == 1){
            value = 2;
        }else if (modePosition == 0 && byPosition == 2){
            value = 3;
        }else if (modePosition == 0 && byPosition == 3){
            value = 4;
        }else if (modePosition == 1 && byPosition == 0){
            value = 5;
        }else if (modePosition == 1 && byPosition == 1){
            value = 6;
        }else if (modePosition == 1 && byPosition == 2){
            value = 7;
        }else value = 8;

        return value;
    }

    private void performSCS(){
        dialog = new Dialog(Settings.context);

        dialog.setContentView(R.layout.save_settings_dialog);

        slotName1 = (TextView) dialog.findViewById(R.id.text_1);
        slotName2 = (TextView) dialog.findViewById(R.id.text_2);
        slotName3 = (TextView) dialog.findViewById(R.id.text_3);
        slotName4 = (TextView) dialog.findViewById(R.id.text_4);
        slotName5 = (TextView) dialog.findViewById(R.id.text_5);

        getCurrentSavedSettings();

        LinearLayout linearLayout1 = (LinearLayout) dialog.findViewById(R.id.first_container);
        LinearLayout linearLayout2 = (LinearLayout) dialog.findViewById(R.id.second_container);
        LinearLayout linearLayout3 = (LinearLayout) dialog.findViewById(R.id.third_container);
        LinearLayout linearLayout4 = (LinearLayout) dialog.findViewById(R.id.fourth_container);
        LinearLayout linearLayout5 = (LinearLayout) dialog.findViewById(R.id.fifth_container);

        ImageView imageView1 = (ImageView) dialog.findViewById(R.id.first_delete);
        ImageView imageView2 = (ImageView) dialog.findViewById(R.id.second_delete);
        ImageView imageView3 = (ImageView) dialog.findViewById(R.id.third_delete);
        ImageView imageView4 = (ImageView) dialog.findViewById(R.id.fourth_delete);
        ImageView imageView5 = (ImageView) dialog.findViewById(R.id.fifth_delete);

        mainText = (TextView) dialog.findViewById(R.id.main_text);

        final Button deleteAll = (Button) dialog.findViewById(R.id.delete_all);
        final Button load = (Button) dialog.findViewById(R.id.load);

        if (slotName1.getText().toString().equals(slotName2.getText().toString())
                && slotName2.getText().toString().equals(slotName3.getText().toString())
                && slotName3.getText().toString().equals(slotName4.getText().toString())
                && slotName4.getText().toString().equals(slotName5.getText().toString())
                && slotName5.getText().toString().equals("Empty Slot")) {
            load.setEnabled(false);
            deleteAll.setEnabled(false);
        }else{
            load.setEnabled(true);
            deleteAll.setEnabled(true);
        }

        final LinearLayout[] allLinearLayouts = {linearLayout1, linearLayout2, linearLayout3, linearLayout4, linearLayout5};
        ImageView[] allImageViews = {imageView1, imageView2, imageView3, imageView4, imageView5};

        for (LinearLayout slot : allLinearLayouts){
            slot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LinearLayout linearLayout = (LinearLayout) view;
                    TextView textView = (TextView) linearLayout.getChildAt(1);

                    if (inSaveScreen){
                        if (!textView.getText().toString().equals("Empty Slot"))
                            showSaveAlertDialog(textView, view.getId(), load, deleteAll);
                        else showSaveAsDialog(textView, view.getId(), load, deleteAll);
                    }else {
                        if (textView.getText().toString().equals("Empty Slot")){
                            Toast.makeText(Settings.context, "There's no settings to be Loaded", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int position;

                        switch (view.getId()){
                            case R.id.first_container:
                                position = 1;
                                break;
                            case R.id.second_container:
                                position = 2;
                                break;
                            case R.id.third_container:
                                position = 3;
                                break;
                            case R.id.fourth_container:
                                position = 4;
                                break;
                            default:
                                position = 5;
                                break;
                        }

                        ContextWrapper cw = new ContextWrapper(Settings.context);

                        File directory = cw.getDir("saveSettingsDir", Context.MODE_PRIVATE);

                        File[] files = directory.listFiles();

                        for (File file : files){
                            if (file.getName().substring(0,1).equals( String.valueOf(position) )){
                                int first = Integer.parseInt( file.getName().substring(1,2) );
                                int second = Integer.parseInt( file.getName().substring(2,3) );

                                SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(Settings.context);
                                SharedPreferences.Editor editor = sh.edit();
                                editor.putString(getString(R.string.search_style_key), String.valueOf(first));
                                editor.putString(getString(R.string.default_search_options_key), String.valueOf(second));
                                editor.apply();

                                defaultSearchStyle.setValue(String.valueOf(first));

                                break;
                            }
                        }

                        Toast.makeText(Settings.context, "Settings Changes Loaded", Toast.LENGTH_SHORT).show();

                        inSaveScreen = true;

                        dialog.dismiss();
                    }
                }
            });
        }

        for (ImageView delete : allImageViews){
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position;

                    switch (view.getId()){
                        case R.id.first_delete:
                            position = 1;
                            break;
                        case R.id.second_delete:
                            position = 2;
                            break;
                        case R.id.third_delete:
                            position = 3;
                            break;
                        case R.id.fourth_delete:
                            position = 4;
                            break;
                        default:
                            position = 5;
                            break;
                    }

                    LinearLayout linearLayout = (LinearLayout) view.getParent();
                    TextView textView = (TextView) linearLayout.getChildAt(1);
                    if (!textView.getText().toString().equals(getString(R.string.empty_slot)))
                        showDeleteAlertDialog(textView, position, load, deleteAll);
                }
            });
        }

        deleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteAllAlertDialog(load, deleteAll);
            }
        });

        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (load.getText().equals("Load")){
                    inSaveScreen = false;
                    mainText.setText(getString(R.string.load_settings));
                    load.setText(getString(R.string.save));
                    // hide all delete icons
                    for (LinearLayout linearLayout : allLinearLayouts){
                        linearLayout.getChildAt(2).setVisibility(View.INVISIBLE);
                    }
                }else {
                    inSaveScreen = true;
                    mainText.setText(getString(R.string.save_current_settings));
                    load.setText(getString(R.string.load));
                    // reveal all delete icons
                    for (LinearLayout linearLayout : allLinearLayouts){
                        linearLayout.getChildAt(2).setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        Button quit = (Button) dialog.findViewById(R.id.quit);
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inSaveScreen = true;
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

    private void showSaveAsDialog(final TextView textView, final int id, final Button load, final Button deleteAll){

        final Dialog saveAs = new Dialog(Settings.context);

        saveAs.setContentView(R.layout.save_as_dialog);

        final EditText name = (EditText) saveAs.findViewById(R.id.name);
        Button save = (Button) saveAs.findViewById(R.id.save);
        Button cancel = (Button) saveAs.findViewById(R.id.cancel);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                load.setEnabled(true);

                deleteAll.setEnabled(true);

                int position;

                if (name.getText().toString().isEmpty())
                    Toast.makeText(Settings.context, "Invalid Style/User Name", Toast.LENGTH_SHORT).show();
                else {
                    textView.setText(name.getText());

                    // get position and all current pref sett
                    switch (id){
                        case R.id.first_container:
                            position = 1;
                            break;
                        case R.id.second_container:
                            position = 2;
                            break;
                        case R.id.third_container:
                            position = 3;
                            break;
                        case R.id.fourth_container:
                            position = 4;
                            break;
                        default:
                            position = 5;
                            break;
                    }

                    // get all current pref sett
                    SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(Settings.context);
                    int first = Integer.parseInt(sh.getString(getString(R.string.search_style_key), "1"));
                    int second = Integer.parseInt(sh.getString(getString(R.string.default_search_options_key), "1"));

                    // file name to be saved is
                    String fileName = String.valueOf(position)
                            + String.valueOf(first)
                            + String.valueOf(second)
                            + name.getText().toString();

                    // delete default or old one and save the new one
                    if (switchFiles(fileName, position))
                        Toast.makeText(Settings.context, "Settings Saved Successfully", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(Settings.context, "Error Occurred Nothing is saved", Toast.LENGTH_SHORT).show();

                    saveAs.dismiss();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAs.dismiss();
            }
        });

        //Grab the window of the dialog, and change the width
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = saveAs.getWindow();
        lp.copyFrom(window.getAttributes());
        //This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        saveAs.show();
    }

    private void showDeleteAlertDialog(final TextView textView,final int position, final Button load, final Button deleteAll){
        AlertDialog.Builder builder = new AlertDialog.Builder(Settings.context);
        builder.setMessage(R.string.delete_warning);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                boolean done = false;

                textView.setText(getString(R.string.empty_slot));

                ContextWrapper cw = new ContextWrapper(Settings.context);

                File directory = cw.getDir("saveSettingsDir", Context.MODE_PRIVATE);

                File[] files = directory.listFiles();

                for (File file : files){
                    if (file.getName().substring(0,1).equals( String.valueOf(position) )){
                        done = file.delete();
                        break;
                    }
                }

                File file = new File(directory, String.valueOf(position) + "0");

                FileOutputStream fos;

                if (done){
                    try {
                        fos = new FileOutputStream(file);

                        fos.close();
                    }catch (IOException io){
                        Toast.makeText(Settings.context, "Error", Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(Settings.context, "Slot deleted successfully", Toast.LENGTH_SHORT).show();
                }

                if (slotName1.getText().toString().equals(slotName2.getText().toString())
                        && slotName2.getText().toString().equals(slotName3.getText().toString())
                        && slotName3.getText().toString().equals(slotName4.getText().toString())
                        && slotName4.getText().toString().equals(slotName5.getText().toString())
                        && slotName5.getText().toString().equals("Empty Slot")) {
                    load.setEnabled(false);
                    deleteAll.setEnabled(false);
                }else{
                    load.setEnabled(true);
                    deleteAll.setEnabled(true);
                }

                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteAllAlertDialog(final Button load, final Button deleteAll){
        AlertDialog.Builder builder = new AlertDialog.Builder(Settings.context);
        builder.setMessage(R.string.delete_all_warning);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                boolean done = false;

                ContextWrapper cw = new ContextWrapper(Settings.context);

                File directory = cw.getDir("saveSettingsDir", Context.MODE_PRIVATE);

                File[] files = directory.listFiles();

                for (File file : files){
                    done = file.delete();
                }

                if (done) {
                    Toast.makeText(Settings.context, "All saved settings are deleted", Toast.LENGTH_SHORT).show();

                    slotName1.setText(getString(R.string.empty_slot));
                    slotName2.setText(getString(R.string.empty_slot));
                    slotName3.setText(getString(R.string.empty_slot));
                    slotName4.setText(getString(R.string.empty_slot));
                    slotName5.setText(getString(R.string.empty_slot));

                    load.setEnabled(false);
                    deleteAll.setEnabled(false);

                    dialog.dismiss();

                    if (!inSaveScreen){
                        inSaveScreen = true;
                        load.callOnClick();
                    }

                    createInitialFiles();
                }else Toast.makeText(Settings.context, "Error in deleting all saved settings", Toast.LENGTH_SHORT).show();
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

    private void performDSK(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Settings.context);
        builder.setMessage(R.string.back_to_default_settings);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {


                SharedPreferences defaultSettings = PreferenceManager.getDefaultSharedPreferences(Settings.context);
                SharedPreferences.Editor reset = defaultSettings.edit();
                reset.putString(getString(R.string.search_style_key), "1");
                reset.putString(getString(R.string.default_search_options_key), "1");
                reset.apply();

                defaultSearchStyle.setValue("1");

                dialog.dismiss();
                Toast.makeText(Settings.context, "All settings has been reset to default", Toast.LENGTH_SHORT).show();
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

    private void getCurrentSavedSettings(){
        ContextWrapper cw = new ContextWrapper(Settings.context);

        File directory = cw.getDir("saveSettingsDir", Context.MODE_PRIVATE);

        if (directory.listFiles().length == 0)
            createInitialFiles();
        else {
            File[] files = directory.listFiles();

            setSlotNames(files);
        }
    }

    private void setSlotNames(File[] files){
        String slotName;

        int slotPosition;

        String[] listOfEmptyNames = {"10", "20", "30", "40", "50"};

        for (File file : files){
            String fileName = file.getName();

            slotPosition = Integer.parseInt(fileName.substring(0,1));

            if (!fileName.equals(listOfEmptyNames[slotPosition-1])) {
                // 3 because first index indicates position of slot (+1) + then No. of Preference (+2) + slotName in editText
                slotName = fileName.substring(3);

                if (slotPosition == 1)
                    slotName1.setText(slotName);
                else if (slotPosition == 2)
                    slotName2.setText(slotName);
                else if (slotPosition == 3)
                    slotName3.setText(slotName);
                else if (slotPosition == 4)
                    slotName4.setText(slotName);
                else slotName5.setText(slotName);
            }
        }
    }

    private boolean switchFiles(String fileName, int position){
        boolean done = false;

        ContextWrapper cw = new ContextWrapper(Settings.context);

        File directory = cw.getDir("saveSettingsDir", Context.MODE_PRIVATE);

        File[] files = directory.listFiles();

        for (File file : files){
            if (file.getName().substring(0,1).equals( String.valueOf(position) )){
                done = file.delete();
                break;
            }
        }

        File file = new File(directory, fileName);

        FileOutputStream fos;

        try {
            fos = new FileOutputStream(file);

            fos.close();
        }catch (IOException io){
            Toast.makeText(Settings.context, "Error", Toast.LENGTH_SHORT).show();
        }

        return done;
    }

    private void showSaveAlertDialog(final TextView textView, final int ID, final Button load, final Button deleteAll){
        AlertDialog.Builder builder = new AlertDialog.Builder(Settings.context);
        builder.setMessage(R.string.override_saved_slot);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                showSaveAsDialog(textView, ID, load, deleteAll);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void createInitialFiles(){
        ContextWrapper cw = new ContextWrapper(Settings.context);

        File directory = cw.getDir("saveSettingsDir", Context.MODE_PRIVATE);

        for (int i = 1; i <= 5; i++){
            File file = new File(directory, String.valueOf(i) + "0");

            FileOutputStream fos;

            try {
                fos = new FileOutputStream(file);

                fos.close();
            }catch (IOException io){
                Toast.makeText(Settings.context, "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
