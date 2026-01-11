package com.volla.hub

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.volla.hub.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var onlineAdapter: ContentAdapter
    private lateinit var blogAdapter: ContentAdapter
    private lateinit var wikiAdapter: ContentAdapter
    private val vollaParser = VollaParser()

    private var allOnlinePages = listOf<ContentItem>()
    private var allBlogPosts = listOf<ContentItem>()
    private var allWikiArticles = listOf<ContentItem>()
    private var currentView = VIEW_ONLINE
    private var currentWikiLanguage = "de"

    companion object {
        const val VIEW_ONLINE = 0
        const val VIEW_BLOG = 1
        const val VIEW_WIKI = 2
        const val VIEW_FORUM = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadThemePreference()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Volla Hub"
        setupRecyclerViews()
        setupButtons()
        loadContent()

        binding.swipeRefresh.setOnRefreshListener {
            refreshContent()
        }
    }

    private fun loadThemePreference() {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_theme", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun setupRecyclerViews() {
        onlineAdapter = ContentAdapter { item -> openContent(item) }
        blogAdapter = ContentAdapter { item -> openContent(item) }
        wikiAdapter = ContentAdapter { item -> openContent(item) }

        binding.onlineRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.onlineRecyclerView.adapter = onlineAdapter

        binding.blogRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.blogRecyclerView.adapter = blogAdapter

        binding.wikiRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.wikiRecyclerView.adapter = wikiAdapter
    }

    private fun setupButtons() {
        binding.btnOnline.setOnClickListener { showOnlineView() }
        binding.btnBlog.setOnClickListener { showBlogView() }
        binding.btnWiki.setOnClickListener { showWikiView() }
        binding.btnForum.setOnClickListener { showForumView() }
        binding.btnLoadMore.setOnClickListener { loadMoreBlogPosts() }

        // Wiki-Sprach-Buttons
        binding.btnWikiDe.setOnClickListener { loadWikiLanguage("de", "Hauptseite") }
        binding.btnWikiEn.setOnClickListener { loadWikiLanguage("en", "English") }
        binding.btnWikiCs.setOnClickListener { loadWikiLanguage("cs", "%C4%8Cesky_Slovensk%C3%A1") }
        binding.btnWikiIt.setOnClickListener { loadWikiLanguage("it", "Italiano") }
        binding.btnWikiEs.setOnClickListener { loadWikiLanguage("es", "Espa%C3%B1ol") }
        binding.btnWikiDa.setOnClickListener { loadWikiLanguage("da", "Dansk") }
        binding.btnWikiNo.setOnClickListener { loadWikiLanguage("no", "Norsk") }
        binding.btnWikiSv.setOnClickListener { loadWikiLanguage("sv", "Svensk") }

        // Forum-Sprach-Buttons
        binding.btnForumDe.setOnClickListener { openForumUrl("https://forum.volla.online/viewforum.php?f=94", "Deutsch Forum") }
        binding.btnForumEn.setOnClickListener { openForumUrl("https://forum.volla.online/viewforum.php?f=26", "English Forum") }
        binding.btnForumEs.setOnClickListener { openForumUrl("https://forum.volla.online/viewforum.php?f=119", "Español Forum") }
        binding.btnForumCs.setOnClickListener { openForumUrl("https://forum.volla.online/viewforum.php?f=145", "Česky Slovenská Forum") }
        binding.btnForumIt.setOnClickListener { openForumUrl("https://forum.volla.online/viewforum.php?f=148", "Italiano Forum") }

        showOnlineView()
    }

    private fun openForumUrl(url: String, title: String) {
        val intent = Intent(this, ContentActivity::class.java).apply {
            putExtra("url", url)
            putExtra("title", title)
        }
        startActivity(intent)
    }

    private fun showOnlineView() {
        currentView = VIEW_ONLINE
        updateButtonStates()
        binding.onlineRecyclerView.visibility = View.VISIBLE
        binding.blogRecyclerView.visibility = View.GONE
        binding.wikiContainer.visibility = View.GONE
        binding.forumContainer.visibility = View.GONE
        binding.btnLoadMore.visibility = View.GONE
    }

    private fun showBlogView() {
        currentView = VIEW_BLOG
        updateButtonStates()
        binding.onlineRecyclerView.visibility = View.GONE
        binding.blogRecyclerView.visibility = View.VISIBLE
        binding.wikiContainer.visibility = View.GONE
        binding.forumContainer.visibility = View.GONE
        binding.btnLoadMore.visibility = View.VISIBLE
    }

    private fun showWikiView() {
        currentView = VIEW_WIKI
        updateButtonStates()
        binding.onlineRecyclerView.visibility = View.GONE
        binding.blogRecyclerView.visibility = View.GONE
        binding.wikiContainer.visibility = View.VISIBLE
        binding.forumContainer.visibility = View.GONE
        binding.btnLoadMore.visibility = View.GONE
    }

    private fun showForumView() {
        currentView = VIEW_FORUM
        updateButtonStates()
        binding.onlineRecyclerView.visibility = View.GONE
        binding.blogRecyclerView.visibility = View.GONE
        binding.wikiContainer.visibility = View.GONE
        binding.forumContainer.visibility = View.VISIBLE
        binding.btnLoadMore.visibility = View.GONE
    }

    private fun updateButtonStates() {
        binding.btnOnline.isSelected = currentView == VIEW_ONLINE
        binding.btnBlog.isSelected = currentView == VIEW_BLOG
        binding.btnWiki.isSelected = currentView == VIEW_WIKI
        binding.btnForum.isSelected = currentView == VIEW_FORUM
    }

    private fun loadWikiLanguage(lang: String, title: String) {
        currentWikiLanguage = lang
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val articles = withContext(Dispatchers.IO) {
                    vollaParser.parseWiki(title)
                }
                allWikiArticles = articles
                wikiAdapter.submitList(articles)
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "${articles.size} Wiki-Artikel geladen", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Fehler: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadContent() {
        binding.progressBar.visibility = View.VISIBLE
        binding.errorText.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val (online, wiki) = withContext(Dispatchers.IO) {
                    val online = vollaParser.parseOnlinePages()
                    val wiki = vollaParser.parseWiki("Hauptseite")
                    listOf(online, wiki)
                }

                allOnlinePages = online
                allWikiArticles = wiki

                onlineAdapter.submitList(online)
                wikiAdapter.submitList(wiki)

                // Blog später laden
                loadBlog()

                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false

                Toast.makeText(this@MainActivity,
                    "Inhalte geladen",
                    Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                binding.errorText.text = "Fehler: ${e.message}"
                binding.errorText.visibility = View.VISIBLE
            }
        }
    }

    private fun loadBlog() {
        lifecycleScope.launch {
            try {
                val blog = withContext(Dispatchers.IO) {
                    vollaParser.parseBlog()
                }
                allBlogPosts = blog
                blogAdapter.submitList(blog.take(20))
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Blog-Fehler: ${e.message}")
            }
        }
    }

    private fun loadMoreBlogPosts() {
        val currentSize = blogAdapter.itemCount
        val nextBatch = allBlogPosts.drop(currentSize).take(20)

        if (nextBatch.isEmpty()) {
            Toast.makeText(this, "Keine weiteren Beiträge", Toast.LENGTH_SHORT).show()
            return
        }

        blogAdapter.submitList(allBlogPosts.take(currentSize + 20))
        Toast.makeText(this, "${nextBatch.size} weitere Beiträge geladen", Toast.LENGTH_SHORT).show()
    }

    private fun refreshContent() {
        when (currentView) {
            VIEW_WIKI -> loadWikiLanguage(currentWikiLanguage,
                when(currentWikiLanguage) {
                    "en" -> "English"
                    "cs" -> "%C4%8Cesky_Slovensk%C3%A1"
                    "it" -> "Italiano"
                    "es" -> "Espa%C3%B1ol"
                    "da" -> "Dansk"
                    "no" -> "Norsk"
                    "sv" -> "Svensk"
                    else -> "Hauptseite"
                })
            else -> loadContent()
        }
    }

    private fun openContent(item: ContentItem) {
        val intent = Intent(this, ContentActivity::class.java).apply {
            putExtra("url", item.url)
            putExtra("title", item.title)
        }
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterContent(newText ?: "")
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                refreshContent()
                true
            }
            R.id.action_theme -> {
                toggleTheme()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleTheme() {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val currentDark = prefs.getBoolean("dark_theme", false)
        prefs.edit().putBoolean("dark_theme", !currentDark).apply()

        AppCompatDelegate.setDefaultNightMode(
            if (!currentDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        recreate()
    }

    private fun filterContent(query: String) {
        when (currentView) {
            VIEW_ONLINE -> {
                val filtered = if (query.isEmpty()) allOnlinePages
                else allOnlinePages.filter { it.title.contains(query, ignoreCase = true) }
                onlineAdapter.submitList(filtered)
            }
            VIEW_BLOG -> {
                val filtered = if (query.isEmpty()) allBlogPosts.take(blogAdapter.itemCount)
                else allBlogPosts.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.excerpt.contains(query, ignoreCase = true)
                }
                blogAdapter.submitList(filtered)
            }
            VIEW_WIKI -> {
                val filtered = if (query.isEmpty()) allWikiArticles
                else allWikiArticles.filter { it.title.contains(query, ignoreCase = true) }
                wikiAdapter.submitList(filtered)
            }
            VIEW_FORUM -> {
                // Forum hat keine Suchliste mehr, nur Buttons
            }
        }
    }
}
