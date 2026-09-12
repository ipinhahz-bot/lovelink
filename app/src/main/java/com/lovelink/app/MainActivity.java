package com.lovelink.app;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.InputStream;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupPermissions();
        setupWebView();
        setupBackNavigation();

        // Show quick guide dialog on first open
        showGuideDialog(false);
    }

    private void initViews() {
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        heartsContainer = findViewById(R.id.heartsContainer);

        com.google.android.material.floatingactionbutton.FloatingActionButton fabLove = findViewById(R.id.fabLove);
        if (fabLove != null) {
            fabLove.setOnClickListener(v -> {
                spawnFloatingHearts();
                Toast.makeText(this, getString(R.string.love_sent), Toast.LENGTH_SHORT).show();
            });
            fabLove.setOnLongClickListener(v -> {
                Toast.makeText(this, "Memuat ulang WhatsApp...", Toast.LENGTH_SHORT).show();
                webView.reload();
                return true;
            });
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();

        // 100% Pas di layar HP: matikan scroll horizontal & cegah overscroll
        webView.setHorizontalScrollBarEnabled(false);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        // Desktop User Agent agar tombol pairing muncul
        settings.setUserAgentString(DESKTOP_USER_AGENT);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Accept persistent cookies
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
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                injectCustomTheme();
                view.postDelayed(MainActivity.this::injectCustomTheme, 800);
                view.postDelayed(MainActivity.this::injectCustomTheme, 2000);
                view.postDelayed(MainActivity.this::injectCustomTheme, 4000);
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
        try {
            // 1. Inject custom CSS (theme and mobile 1-column layout)
            InputStream is = getAssets().open("custom.css");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String encodedCss = Base64.encodeToString(buffer, Base64.NO_WRAP);
            String cssJs = "var style = document.getElementById('custom-lovelink-css') || document.createElement('style');" +
                    "style.id = 'custom-lovelink-css';" +
                    "style.type = 'text/css';" +
                    "style.innerHTML = window.atob('" + encodedCss + "');" +
                    "if (!document.getElementById('custom-lovelink-css')) { document.head.appendChild(style); }";
            webView.evaluateJavascript(cssJs, null);

            // 2. Inject mobile adapter JS (viewport and back navigation)
            InputStream isJs = getAssets().open("mobile_adapter.js");
            byte[] jsBuffer = new byte[isJs.available()];
            isJs.read(jsBuffer);
            isJs.close();
            String jsCode = new String(jsBuffer);
            webView.evaluateJavascript(jsCode, null);
        } catch (Exception ignored) {
        }
    }

    private void spawnFloatingHearts() {
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

    private void showGuideDialog(boolean force) {
        if (!force) {
            // Bisa dilewati saat otomatis buka jika tidak diinginkan
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.guide_title))
                .setMessage(getString(R.string.guide_message))
                .setPositiveButton(getString(R.string.btn_close), (dialog, which) -> dialog.dismiss())
                .show();
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
                        "  if (document.body.classList.contains('chat-open')) { " +
                        "    document.body.classList.remove('chat-open'); " +
                        "    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', code: 'Escape', keyCode: 27, which: 27, bubbles: true })); " +
                        "    return 'handled'; " +
                        "  } " +
                        "  return 'none'; " +
                        "})();",
                        value -> {
                            if (!"\"handled\"".equals(value)) {
                                if (webView != null && webView.canGoBack()) {
                                    webView.goBack();
                                } else {
                                    finish();
                                }
                            }
                        }
                );
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
