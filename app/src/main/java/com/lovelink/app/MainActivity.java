package com.lovelink.app;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private FrameLayout heartsContainer;
    private ValueCallback<Uri[]> uploadMessage;
    private static final int FILE_CHOOSER_RESULT_CODE = 1001;
    private static final int PERMISSION_REQUEST_CODE = 2001;

    private static final String DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";
    private static final String WA_WEB_URL = "https://web.whatsapp.com";

    private String cachedCss = "";
    private String cachedJs = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        loadAssetsContent();
        initViews();
        setupPermissions();
        setupWebView();
        setupBackNavigation();
    }

    private void loadAssetsContent() {
        try {
            InputStream is = getAssets().open("custom.css");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            cachedCss = new String(buffer, StandardCharsets.UTF_8);
        } catch (Exception e) {
            cachedCss = "";
        }

        try {
            InputStream isJs = getAssets().open("mobile_adapter.js");
            byte[] jsBuffer = new byte[isJs.available()];
            isJs.read(jsBuffer);
            isJs.close();
            cachedJs = new String(jsBuffer, StandardCharsets.UTF_8);
        } catch (Exception e) {
            cachedJs = "";
        }
    }

    private void initViews() {
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        heartsContainer = findViewById(R.id.heartsContainer);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();

        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(true);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        // Kunci lebar desktop ke 820px dan hitung scale awal agar 100% pas di layar HP
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenWidthDp = (int) (dm.widthPixels / dm.density);
        int initialScale = (int) ((screenWidthDp / 820.0) * 100.0);
        if (initialScale > 100) initialScale = 100;
        if (initialScale < 25) initialScale = 25;
        webView.setInitialScale(initialScale);

        settings.setUserAgentString(DESKTOP_USER_AGENT);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                request.grant(request.getResources());
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                }
                uploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, FILE_CHOOSER_RESULT_CODE);
                } catch (Exception e) {
                    uploadMessage = null;
                    Toast.makeText(MainActivity.this, "Gagal membuka file picker", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            // STRIP CSP HEADER agar injeksi CSS & JS 100% diterima oleh browser!
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                injectCustomTheme();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                injectCustomTheme();
                
                // Panggil ulang secara berkala saat React WA Web mulai me-render DOM
                view.postDelayed(MainActivity.this::injectCustomTheme, 500);
                view.postDelayed(MainActivity.this::injectCustomTheme, 1200);
                view.postDelayed(MainActivity.this::injectCustomTheme, 2500);
                view.postDelayed(MainActivity.this::injectCustomTheme, 4500);
                view.postDelayed(MainActivity.this::injectCustomTheme, 8000);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.contains("whatsapp.com")) {
                    return false;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }
        });

        webView.loadUrl(WA_WEB_URL);
    }

    private void injectCustomTheme() {
        if (webView == null) return;

        // 1. Injeksi CSS via JavaScript menggunakan TextNode untuk tembus semua proteksi
        if (!cachedCss.isEmpty()) {
            String encodedCss = Base64.encodeToString(cachedCss.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
            String cssJs = "(function() {" +
                    "try {" +
                    "  var id = 'lovelink-custom-style';" +
                    "  var el = document.getElementById(id);" +
                    "  if (!el) {" +
                    "    el = document.createElement('style');" +
                    "    el.id = id;" +
                    "    el.type = 'text/css';" +
                    "    var head = document.head || document.documentElement;" +
                    "    head.appendChild(el);" +
                    "  }" +
                    "  var cssText = decodeURIComponent(escape(window.atob('" + encodedCss + "')));" +
                    "  if (el.textContent !== cssText) {" +
                    "    el.textContent = cssText;" +
                    "  }" +
                    "} catch(e) {}" +
                    "})();";
            webView.evaluateJavascript(cssJs, null);
        }

        // 2. Injeksi Mobile Adapter JS
        if (!cachedJs.isEmpty()) {
            webView.evaluateJavascript(cachedJs, null);
        }
    }

    public void spawnFloatingHearts() {
        String[] emojis = BuildConfig.IS_GIRL ?
                new String[]{"💕", "💖", "🥰", "🌸", "✨", "❤️", "🌹"} :
                new String[]{"💙", "✨", "🤍", "⭐", "💫"};
        Random random = new Random();

        for (int i = 0; i < 7; i++) {
            final TextView heart = new TextView(this);
            heart.setText(emojis[random.nextInt(emojis.length)]);
            heart.setTextSize(24 + random.nextInt(16));

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            int width = heartsContainer.getWidth() > 0 ? heartsContainer.getWidth() : 600;
            params.leftMargin = random.nextInt(Math.max(100, width - 120));
            int height = heartsContainer.getHeight() > 0 ? heartsContainer.getHeight() : 1000;
            params.topMargin = height - 150;

            heart.setLayoutParams(params);
            heartsContainer.addView(heart);

            heart.animate()
                    .translationYBy(-(height - 200 + random.nextInt(150)))
                    .translationXBy(-80 + random.nextInt(160))
                    .alpha(0f)
                    .setDuration(1800 + random.nextInt(1000))
                    .setInterpolator(new AccelerateInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            heartsContainer.removeView(heart);
                        }
                    })
                    .start();
        }
    }

    private void setupPermissions() {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };
        boolean needRequest = false;
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                needRequest = true;
                break;
            }
        }
        if (needRequest) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                webView.evaluateJavascript(
                        "(function() { " +
                        "  document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', code: 'Escape', keyCode: 27, which: 27, bubbles: true })); " +
                        "})();",
                        null
                );
                if (webView != null && webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (uploadMessage == null) return;
            Uri[] results = null;
            if (resultCode == RESULT_OK && data != null) {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
            uploadMessage.onReceiveValue(results);
            uploadMessage = null;
        }
    }
}
