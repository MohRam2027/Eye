package com.example.archaeoexplorer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;
    private PermissionRequest pendingCamera;
    private GeolocationPermissions.Callback geoCallback;
    private String geoOrigin;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        webView = new WebView(this);
        setContentView(webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, android.webkit.WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("https".equals(uri.getScheme()) && uri.getHost() != null &&
                    (uri.getHost().equals("google.com") || uri.getHost().endsWith(".google.com"))) {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    return true;
                }
                return false;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    pendingCamera = request;
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 41);
                } else request.grant(request.getResources());
            }
            @Override public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                if (Build.VERSION.SDK_INT >= 23 &&
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    geoOrigin = origin; geoCallback = callback;
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 42);
                } else callback.invoke(origin, true, false);
            }
        });
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(code, permissions, results);
        if (code == 41 && pendingCamera != null) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) pendingCamera.grant(pendingCamera.getResources());
            else pendingCamera.deny();
            pendingCamera = null;
        } else if (code == 42 && geoCallback != null) {
            boolean granted = false;
            for (int result : results) if (result == PackageManager.PERMISSION_GRANTED) granted = true;
            geoCallback.invoke(geoOrigin, granted, false);
            geoCallback = null; geoOrigin = null;
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
