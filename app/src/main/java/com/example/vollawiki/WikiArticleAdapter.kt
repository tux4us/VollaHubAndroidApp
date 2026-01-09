package com.example.vollawiki

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.volla.wiki.databinding.ItemWikiArticleBinding

class WikiArticleAdapter(
    private val onItemClick: (WikiArticle) -> Unit
) : ListAdapter<WikiArticle, WikiArticleAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWikiArticleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemWikiArticleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(article: WikiArticle) {
            binding.titleText.text = article.title
            binding.descriptionText.text = article.description.ifEmpty { "Keine Beschreibung verfügbar" }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<WikiArticle>() {
        override fun areItemsTheSame(oldItem: WikiArticle, newItem: WikiArticle): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: WikiArticle, newItem: WikiArticle): Boolean {
            return oldItem == newItem
        }
    }
}