package com.volla.hub

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.volla.hub.databinding.ActivityStartBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartBinding
    private val vollaParser = VollaParser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Volla Hub"

        setupButtons()
        setupNavigation()
        loadLatestBlog()
    }

    private fun setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Bereits auf Home
                }
                R.id.nav_report -> {
                    startActivity(Intent(this, DeviceReportActivity::class.java))
                }
            }
            true
        }
        // Selektiere Home standardmäßig
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun setupButtons() {
        binding.btnNavOnline.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("view_type", MainActivity.VIEW_ONLINE)
            })
        }
        binding.btnNavBlog.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("view_type", MainActivity.VIEW_BLOG)
            })
        }
        binding.btnNavWiki.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("view_type", MainActivity.VIEW_WIKI)
            })
        }
        binding.btnNavForum.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("view_type", MainActivity.VIEW_FORUM)
            })
        }
        binding.btnNavReport.setOnClickListener {
            startActivity(Intent(this, DeviceReportActivity::class.java))
        }
    }

    private fun loadLatestBlog() {
        lifecycleScope.launch {
            try {
                val blog = withContext(Dispatchers.IO) {
                    vollaParser.parseBlog()
                }
                if (blog.isNotEmpty()) {
                    val latest = blog.first()
                    binding.latestBlogTitle.text = latest.title
                    binding.latestBlogExcerpt.text = latest.excerpt
                    binding.latestBlogCard.visibility = View.VISIBLE
                    binding.btnReadLatest.setOnClickListener {
                        val intent = Intent(this@StartActivity, ContentActivity::class.java).apply {
                            putExtra("url", latest.url)
                            putExtra("title", latest.title)
                        }
                        startActivity(intent)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("StartActivity", "Blog-Fehler: ${e.message}")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_developer -> {
                showDeveloperInfo()
                true
            }
            R.id.action_theme -> {
                toggleTheme()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeveloperInfo() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Entwickler")
        builder.setMessage("Entwickler der App: tux4us\nGitHub: https://github.com/tux4us/VollaHubAndroidApp")
        builder.setPositiveButton("GitHub öffnen") { _, _ ->
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/tux4us/VollaHubAndroidApp"))
            startActivity(intent)
        }
        builder.setNegativeButton("Schließen", null)
        builder.show()
    }

    private fun toggleTheme() {
        val prefs = getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
        val currentDark = prefs.getBoolean("dark_theme", false)
        prefs.edit().putBoolean("dark_theme", !currentDark).apply()
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            if (!currentDark) androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            else androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        )
        recreate()
    }
}