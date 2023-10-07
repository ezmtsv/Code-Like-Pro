package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post

interface OnIteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onEdit(post: Post)
    fun onRemove(post: Post)
}

class PostsAdapter(
    private val onIteractionListener: OnIteractionListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onIteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onIteractionListener: OnIteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            icLike.isChecked = post.likeByMe
            icLike.text = getCountClick(post.countLike)
            icShare.text = getCountClick(post.countRepost)
            //icView.text = getCountClick(post.countViews)
            icView.text = getCountClick(4500)
            icLike.setOnClickListener {
                onIteractionListener.onLike(post)
            }
            icShare.setOnClickListener {
                onIteractionListener.onShare(post)
            }
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.edit -> {
                                onIteractionListener.onEdit(post)
                                true
                            }
                            R.id.remove -> {
                                onIteractionListener.onRemove(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.edit -> {
                                onIteractionListener.onEdit(post)
                                true
                            }

                            R.id.remove -> {
                                onIteractionListener.onRemove(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }
    }

    private fun getCountClick(cnt: Int): String {
        val tmp: Int
        val result: String = when {
            cnt < 1000 -> {
                "+$cnt"
            }

            cnt < 10_000 -> {
                tmp = (cnt.toFloat() / 100.0f).toInt()
                "+${String.format("%.1f", tmp.toFloat() / 10.0)}K"
            }

            cnt < 1_000_000 -> {
                "+${(cnt.toFloat() / 1000.0f).toInt()}K"
            }

            else -> {
                tmp = (cnt.toFloat() / 100_000.0f).toInt()
                "+${String.format("%.1f", tmp.toFloat() / 10.0)}M"
            }
        }
        return result
    }
}


class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }

}
