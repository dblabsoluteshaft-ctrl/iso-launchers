package com.isolauncher;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class PlayerActivity extends Activity {
    private WebView web;
    private ValueCallback<Uri[]> chooserCb;
    private static final int PICK = 7;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView w, ValueCallback<Uri[]> cb,
                                             FileChooserParams p) {
                if (chooserCb != null) chooserCb.onReceiveValue(null);
                chooserCb = cb;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                startActivityForResult(i, PICK);
                return true;
            }
        });
        String html = "";
        try {
            InputStream in = getAssets().open("player.html");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            in.close();
            html = out.toString("UTF-8");
        } catch (Exception e) {
            html = "<h1>Could not load player</h1>";
        }
        web.loadDataWithBaseURL("https://appassets.local/", html, "text/html", "utf-8", null);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == PICK && chooserCb != null) {
            Uri[] r = null;
            if (res == RESULT_OK && data != null && data.getData() != null) {
                r = new Uri[]{data.getData()};
            }
            chooserCb.onReceiveValue(r);
            chooserCb = null;
        }
    }
          }
