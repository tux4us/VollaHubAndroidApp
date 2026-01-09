package com.example.vollawiki

import android.util.Log
import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

data class WikiArticle(
    val title: String,
    val url: String,
    val description: String
)

class WikiParser {
    private val baseUrl = "http://wiki.volla.online"

    suspend fun parseWiki(): List<WikiArticle> {
        val articles = mutableListOf<WikiArticle>()

        try {
            Log.d("WikiParser", "Lade Hauptseite...")
            val doc = Jsoup.connect("$baseUrl/index.php?title=Hauptseite")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get()

            Log.d("WikiParser", "Hauptseite geladen")

            // Hauptseite hinzufügen
            articles.add(WikiArticle(
                "Hauptseite",
                "$baseUrl/index.php?title=Hauptseite",
                "Herzlich willkommen im Volla Phone Wiki"
            ))

            // Alle Wiki-Links sammeln (MediaWiki-Format)
            val links = doc.select("a[href*='index.php?title=']")
            Log.d("WikiParser", "Gefundene Wiki-Links: ${links.size}")

            val seenTitles = mutableSetOf<String>()
            seenTitles.add("Hauptseite")

            for (link in links) {
                val href = link.attr("abs:href")
                val linkText = link.text()

                // Nur echte Artikel-Links, keine Spezial/Diskussions-Seiten
                if (href.contains("index.php?title=") &&
                    !href.contains("Spezial:") &&
                    !href.contains("Diskussion:") &&
                    !href.contains("action=") &&
                    !href.contains("&") &&
                    linkText.isNotEmpty() &&
                    linkText !in seenTitles) {

                    seenTitles.add(linkText)

                    try {
                        // Lade Artikel-Seite
                        val articleDoc = Jsoup.connect(href)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .timeout(10000)
                            .get()

                        // Erste Textzeile als Beschreibung
                        val description = articleDoc.select("p").firstOrNull()?.text()?.take(150) ?: ""

                        articles.add(WikiArticle(linkText, href, description))
                        Log.d("WikiParser", "Artikel hinzugefügt: $linkText")

                        // Kleine Pause zwischen Requests
                        delay(200)

                    } catch (e: Exception) {
                        Log.e("WikiParser", "Fehler bei $linkText: ${e.message}")
                        // Artikel trotzdem hinzufügen, aber ohne Beschreibung
                        articles.add(WikiArticle(linkText, href, ""))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("WikiParser", "Fehler beim Wiki-Crawling: ${e.message}")
            e.printStackTrace()
        }

        Log.d("WikiParser", "Crawling abgeschlossen. ${articles.size} Artikel gefunden")
        return articles.sortedBy { it.title }
    }

    fun parseArticle(url: String): String {
        return try {
            val doc: Document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get()

            val content = doc.select("article, .content, main").firstOrNull()
                ?: doc.select("body").first()

            content?.html() ?: "<p>Kein Inhalt gefunden</p>"
        } catch (e: Exception) {
            "<p>Fehler beim Laden des Artikels: ${e.message}</p>"
        }
    }

    fun formatArticleHtml(content: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: -apple-system, system-ui, sans-serif;
                        padding: 16px;
                        line-height: 1.6;
                        color: #333;
                    }
                    img {
                        max-width: 100%;
                        height: auto;
                    }
                    a {
                        color: #2196F3;
                        text-decoration: none;
                    }
                    pre {
                        background: #f5f5f5;
                        padding: 12px;
                        border-radius: 4px;
                        overflow-x: auto;
                    }
                    code {
                        background: #f5f5f5;
                        padding: 2px 6px;
                        border-radius: 3px;
                        font-family: monospace;
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin: 16px 0;
                    }
                    th, td {
                        border: 1px solid #ddd;
                        padding: 8px;
                        text-align: left;
                    }
                    th {
                        background: #f5f5f5;
                    }
                </style>
            </head>
            <body>
                $content
            </body>
            </html>
        """.trimIndent()
    }
}
