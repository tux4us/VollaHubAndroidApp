package com.example.vollawiki

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.volla.wiki.R
import com.volla.wiki.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: WikiArticleAdapter
    private val wikiParser = WikiParser()
    private var allArticles = listOf<WikiArticle>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Korrigierter Insets-Listener
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())

            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }

            WindowInsetsCompat.CONSUMED
        }

        setupRecyclerView()
        loadWikiContent()

        binding.swipeRefresh.setOnRefreshListener {
            loadWikiContent()
        }
    }

    private fun setupRecyclerView() {
        adapter = WikiArticleAdapter { article ->
            val intent = Intent(this, ArticleActivity::class.java).apply {
                putExtra("url", article.url)
                putExtra("title", article.title)
            }
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun loadWikiContent() {
        binding.progressBar.visibility = View.VISIBLE
        binding.errorText.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val articles = withContext(Dispatchers.IO) {
                    wikiParser.parseWiki()
                }

                allArticles = articles
                adapter.submitList(articles)
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                binding.recyclerView.visibility = View.VISIBLE

                if (articles.isEmpty()) {
                    binding.errorText.text = getString(R.string.no_articles)
                    binding.errorText.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                } else {
                    Toast.makeText(this@MainActivity,
                        "${articles.size} Artikel geladen",
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                binding.errorText.text = getString(R.string.error_loading, e.message)
                binding.errorText.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                Toast.makeText(this@MainActivity, getString(R.string.error_loading, e.message), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterArticles(newText ?: "")
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                loadWikiContent()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun filterArticles(query: String) {
        val filtered = if (query.isEmpty()) {
            allArticles
        } else {
            allArticles.filter { article ->
                article.title.contains(query, ignoreCase = true) ||
                        article.description.contains(query, ignoreCase = true)
            }
        }
        adapter.submitList(filtered)
    }
}
