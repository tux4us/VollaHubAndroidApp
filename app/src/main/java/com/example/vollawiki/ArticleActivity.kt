package com.example.vollawiki

import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.volla.wiki.R
import com.volla.wiki.databinding.ActivityArticleBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArticleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArticleBinding
    private val wikiParser = WikiParser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = intent.getStringExtra("url") ?: ""
        val title = intent.getStringExtra("title") ?: ""

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = title

        setupWebView()
        loadArticle(url)
    }

    private fun setupWebView() {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                if (url.contains("wiki.volla.online")) {
                    loadArticle(url)
                    return true
                }
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun loadArticle(url: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.errorText.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val content = withContext(Dispatchers.IO) {
                    wikiParser.parseArticle(url)
                }

                val html = wikiParser.formatArticleHtml(content)
                binding.webView.loadDataWithBaseURL("https://wiki.volla.online/", html, "text/html", "UTF-8", null)
                binding.progressBar.visibility = View.GONE
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.errorText.text = getString(R.string.error_loading, e.message)
                binding.errorText.visibility = View.VISIBLE
                Toast.makeText(this@ArticleActivity, getString(R.string.error_loading, e.message), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}