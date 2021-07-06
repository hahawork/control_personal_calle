package com.gv.haha.supervisor;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class Ayuda extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_ayuda);


        WebView we = new WebView(this);
        we.setBackgroundColor(Color.parseColor("#f6f3f2"));
        we.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        we.getSettings().setBuiltInZoomControls(true);
        we.getSettings().setSupportZoom(true);
        we.getSettings().setDisplayZoomControls(false);
        we.loadUrl("file:///android_asset/ayuda.html");
        setContentView(we);
    }
}
