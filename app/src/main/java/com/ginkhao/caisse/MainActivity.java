package com.ginkhao.caisse;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Gin Khao Caisse v2.1 — cadre Android plein écran autour de la caisse web (pos.html).
 *
 *  • charge URL_CAISSE (une URL par restaurant : changer la constante, relancer le build) ;
 *  • autorise le contenu mixte : la page (https) parle au Pi d'impression (http, réseau local) ;
 *  • plein écran immersif, écran toujours allumé, bouton Retour neutralisé ;
 *  • 🖥️ v2.1 : si la caisse a un SECOND ÉCRAN (côté client), l'appli y affiche ecran-client.html
 *    (même site, temps réel). Sans second écran, rien ne change.
 */
public class MainActivity extends Activity {

    // ← UNE SEULE LIGNE À CHANGER PAR RESTAURANT
    private static final String URL_CAISSE = "https://gin-khao-manager.netlify.app/pos.html?kiosque=1&relais=http://192.168.1.68:9100";
    private WebView web;
    private EcranClient ecranClient;
    private DisplayManager displayManager;

    private static String urlEcranClient() {
        // même site que la caisse : .../pos.html?... → .../ecran-client.html
        int i = URL_CAISSE.indexOf("/pos.html");
        return (i > 0 ? URL_CAISSE.substring(0, i) : URL_CAISSE) + "/ecran-client.html";
    }

    @SuppressLint("SetJavaScriptEnabled")
    static void reglerWebView(WebView w) {
        WebSettings s = w.getSettings();
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
        cm.setAcceptThirdPartyCookies(w, true);
        w.setWebChromeClient(new WebChromeClient());
        w.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;   // tout reste dans le cadre
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        web = new WebView(this);
        setContentView(web);
        reglerWebView(web);
        web.loadUrl(URL_CAISSE);

        // 🖥️ second écran (côté client)
        displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        ouvrirEcranClient();
        displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
            @Override public void onDisplayAdded(int displayId) { ouvrirEcranClient(); }
            @Override public void onDisplayRemoved(int displayId) { fermerEcranClient(); }
            @Override public void onDisplayChanged(int displayId) { }
        }, null);
    }

    private void ouvrirEcranClient() {
        try {
            if (ecranClient != null && ecranClient.isShowing()) return;
            Display[] ecrans = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
            if (ecrans == null || ecrans.length == 0) return;
            ecranClient = new EcranClient(this, ecrans[0]);
            ecranClient.show();
        } catch (Exception e) {
            ecranClient = null;   // un second écran capricieux ne doit jamais gêner la caisse
        }
    }

    private void fermerEcranClient() {
        try { if (ecranClient != null) ecranClient.dismiss(); } catch (Exception e) { /* ignore */ }
        ecranClient = null;
    }

    /** La fenêtre affichée sur le second écran : ecran-client.html, plein écran. */
    private static class EcranClient extends Presentation {
        EcranClient(Context ctx, Display d) { super(ctx, d); }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            WebView w = new WebView(getContext());
            setContentView(w);
            reglerWebView(w);
            w.loadUrl(urlEcranClient());
        }
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
    protected void onDestroy() {
        fermerEcranClient();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack();
    }
}
