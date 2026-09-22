package com.ginkhao.caisse;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.app.Activity;

/**
 * Gin Khao Caisse — un cadre Android plein écran autour de la caisse web (pos.html).
 *
 *  • charge URL_CAISSE (une URL par restaurant : changer la constante, relancer le build) ;
 *  • autorise le contenu mixte : la page (https) parle au Pi d'impression (http, réseau local) ;
 *  • plein écran immersif, écran toujours allumé, bouton Retour neutralisé (pas de sortie accidentelle) ;
 *  • la page vient toujours de Netlify : les mises à jour de la caisse sont automatiques.
 */
public class MainActivity extends Activity {

    // ← UNE SEULE LIGNE À CHANGER PAR RESTAURANT
    private static final String URL_CAISSE = "https://gin-khao-la-capelette.netlify.app/pos.html?kiosque=1";

    private WebView web;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);   // 🖨️ https → http (Pi)

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(web, true);

        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // tout reste dans le cadre (pas de navigateur externe)
                return false;
            }
        });

        web.loadUrl(URL_CAISSE);
    }

    private void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUi();
    }

    @Override
    public void onBackPressed() {
        // Retour = jamais quitter la caisse. Retour arrière dans la page si possible, sinon rien.
        if (web != null && web.canGoBack()) web.goBack();
    }
}
