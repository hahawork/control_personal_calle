package com.gv.haha.supervisor;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import static com.gv.haha.supervisor.clases.interfVariablesGenerales.HOST_NAME;

public class MensajeViewer extends AppCompatActivity {

    private WebView webView;

    SharedPreferences setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensaje_viewer);

        setting = PreferenceManager.getDefaultSharedPreferences(this);

        webView = (WebView) findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(HOST_NAME + "admin/pages/VerMensajeUsuario.php?IdUsuario=" + setting.getInt("codUsuario", 0));
        //webView.loadUrl("http://www.google.com");
        //String customHtml = "<html><body><h1>Hello, WebView</h1></body></html>";
        //webView.loadData(customHtml, "text/html", "UTF-8");
    }
}
