package com.volla.hub

import org.jsoup.Jsoup

data class ContentItem(
    val title: String,
    val url: String,
    val excerpt: String = "",
    val date: String = "",
    val level: Int = 0
)

class VollaParser {
    private val baseUrl = "https://volla.online"
    private val wikiBaseUrl = "http://wiki.volla.online"

    suspend fun parseOnlinePages(): List<ContentItem> {
        val pages = mutableListOf<ContentItem>()

        try {
            android.util.Log.d("VollaParser", "Lade Volla Online Seiten...")
            val doc = Jsoup.connect("$baseUrl/de/")
                .userAgent("Mozilla/5.0")
                .timeout(15000)
                .get()

            val menuLinks = doc.select("nav a, .menu a, header a")
            val seenUrls = mutableSetOf<String>()

            for (link in menuLinks) {
                val href = link.attr("abs:href")
                val title = link.text()

                if (href.startsWith("$baseUrl/de/") &&
                    !href.contains("/blog") &&
                    !href.contains("#") &&
                    title.length > 2 &&
                    href !in seenUrls) {

                    seenUrls.add(href)
                    val level = calculateLevel(link)
                    pages.add(ContentItem(title, href, "", "", level))
                    android.util.Log.d("VollaParser", "Seite: $title")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("VollaParser", "Fehler: ${e.message}")
        }

        return pages.sortedBy { it.level }
    }

    suspend fun parseBlog(): List<ContentItem> {
        val posts = mutableListOf<ContentItem>()

        try {
            android.util.Log.d("VollaParser", "Lade Blog...")
            val doc = Jsoup.connect("$baseUrl/de/blog/")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(30000)
                .followRedirects(true)
                .get()

            android.util.Log.d("VollaParser", "Blog-Seite geladen: ${doc.title()}")

            // Blog-Einträge finden
            val blogEntries = doc.select("div.blog-entry")
            android.util.Log.d("VollaParser", "Gefundene Blog-Einträge: ${blogEntries.size}")

            for (entry in blogEntries) {
                // Titel aus h1
                val title = entry.select("h1").firstOrNull()?.text() ?: continue

                // URL - suche nach einem Link im Entry
                val linkElem = entry.select("a[href]").firstOrNull()
                val url = linkElem?.attr("abs:href") ?: ""

                // Falls kein Link gefunden, versuche aus h1 einen zu bauen
                val finalUrl = if (url.isEmpty() || !url.contains("/de/blog/")) {
                    // Erstelle URL aus Titel
                    val slug = title.lowercase()
                        .replace(Regex("[^a-z0-9äöüß\\s-]"), "")
                        .replace(Regex("\\s+"), "-")
                        .replace("ä", "ae")
                        .replace("ö", "oe")
                        .replace("ü", "ue")
                        .replace("ß", "ss")
                    "$baseUrl/de/blog/$slug/"
                } else {
                    url
                }

                // Datum
                val date = entry.select(".blog-entry-date").firstOrNull()?.text() ?: ""

                // Excerpt aus blog-entry-body
                val excerpt = entry.select(".blog-entry-body p").firstOrNull()?.text()?.take(150) ?: ""

                posts.add(ContentItem(title, finalUrl, excerpt, date))
                android.util.Log.d("VollaParser", "Blog-Beitrag: $title -> $finalUrl")
            }

            android.util.Log.d("VollaParser", "${posts.size} Blog-Beiträge gefunden")
        } catch (e: Exception) {
            android.util.Log.e("VollaParser", "Fehler beim Blog-Laden: ${e.message}")
            e.printStackTrace()
        }

        return posts
    }

    suspend fun parseWiki(pageName: String): List<ContentItem> {
        val articles = mutableListOf<ContentItem>()

        try {
            android.util.Log.d("VollaParser", "Lade Wiki: $pageName...")
            val doc = Jsoup.connect("$wikiBaseUrl/index.php?title=$pageName")
                .userAgent("Mozilla/5.0")
                .timeout(15000)
                .get()

            articles.add(ContentItem(
                pageName.replace("%C4%8C", "Č").replace("%C3%A1", "á").replace("%C3%B1", "ñ"),
                "$wikiBaseUrl/index.php?title=$pageName"
            ))

            val links = doc.select("a[href*='index.php?title=']")
            val seenTitles = mutableSetOf(pageName)

            for (link in links) {
                val href = link.attr("abs:href")
                val title = link.text()

                if (href.contains("index.php?title=") &&
                    !href.contains("Spezial:") &&
                    !href.contains("Diskussion:") &&
                    !href.contains("action=") &&
                    !href.contains("&") &&
                    title.isNotEmpty() &&
                    title !in seenTitles) {

                    seenTitles.add(title)
                    articles.add(ContentItem(title, href))
                    android.util.Log.d("VollaParser", "Wiki-Artikel: $title")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("VollaParser", "Fehler: ${e.message}")
        }

        return articles
    }

    private fun calculateLevel(link: org.jsoup.nodes.Element): Int {
        var level = 0
        var parent = link.parent()

        while (parent != null) {
            if (parent.tagName() == "ul" || parent.tagName() == "ol") level++
            parent = parent.parent()
        }

        return level.coerceAtMost(3)
    }
}