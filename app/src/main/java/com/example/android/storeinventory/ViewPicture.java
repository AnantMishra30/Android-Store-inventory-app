package com.example.android.storeinventory;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.RelativeLayout;

public class ViewPicture extends Activity {

    public static Drawable picture;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_picture);

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.view_picture_fullscreen);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            relativeLayout.setBackground(picture);
        else relativeLayout.setBackgroundDrawable(picture);
    }
}
