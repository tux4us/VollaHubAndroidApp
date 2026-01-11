package com.volla.hub

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.volla.hub.databinding.ActivityContentBinding

class ContentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = intent.getStringExtra("url") ?: ""
        val title = intent.getStringExtra("title") ?: ""

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = title

        setupWebView()
        loadContent(url)

        // Back-Button Handler
        onBackPressedDispatcher.addCallback(this) {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else {
                finish()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)

            // Optimale Mobile-Einstellungen
            useWideViewPort = true
            loadWithOverviewMode = true
            layoutAlgorithm = android.webkit.WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING

            // Bessere Schriftgrößen
            textZoom = 100  // Standard 100%
            minimumFontSize = 14
            defaultFontSize = 16

            domStorageEnabled = true
        }

        // Kein Initial Scale - lassen wir responsive arbeiten
    }

    private fun loadContent(url: String) {
        binding.progressBar.visibility = View.VISIBLE

        // Prüfe ob es eine Wiki-Seite ist
        if (url.contains("wiki.volla.online")) {
            // Lade Wiki-Seite mit mobilem Viewport
            binding.webView.loadUrl(url)

            // Injiziere CSS für mobile Darstellung nach dem Laden
            binding.webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    binding.progressBar.visibility = View.GONE

                    // Injiziere mobile CSS
                    view?.evaluateJavascript("""
                    (function() {
                        var meta = document.createElement('meta');
                        meta.name = 'viewport';
                        meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes';
                        document.getElementsByTagName('head')[0].appendChild(meta);
                        
                        var style = document.createElement('style');
                        style.innerHTML = `
                            body { 
                                font-size: 16px !important; 
                                line-height: 1.6 !important;
                                padding: 8px !important;
                            }
                            img { 
                                max-width: 100% !important; 
                                height: auto !important; 
                            }
                            table { 
                                width: 100% !important; 
                                font-size: 14px !important;
                            }
                            pre, code { 
                                font-size: 13px !important; 
                                overflow-x: auto !important;
                            }
                            #mw-navigation, .mw-jump-link { 
                                display: none !important; 
                            }
                        `;
                        document.head.appendChild(style);
                    })();
                """, null)
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        } else {
            // Normale Seiten
            binding.webView.loadUrl(url)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}