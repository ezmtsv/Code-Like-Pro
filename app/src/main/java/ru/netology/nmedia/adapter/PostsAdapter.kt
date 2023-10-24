package ru.netology.nmedia.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.databinding.SinglePostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.AndroidUtils

interface OnIteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onEdit(post: Post)
    fun onRemove(post: Post)
    fun openLinkVideo(post: Post)

    fun openCardPost(post: Post)
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
    @SuppressLint("SuspiciousIndentation")
    fun bind(post: Post) {

        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            icLike.isChecked = post.likeByMe
            icLike.text = AndroidUtils.getCountClick(post.countLike)
            icShare.text = AndroidUtils.getCountClick(post.countRepost)
            icView.text = AndroidUtils.getCountClick(4500)

            if (post.linkVideo != "") groupVideo.visibility = View.VISIBLE
            else groupVideo.visibility = View.GONE
            icLike.setOnClickListener {
                onIteractionListener.onLike(post)
            }
            idVideo.setOnClickListener {
                onIteractionListener.openLinkVideo(post)
            }
            play.setOnClickListener {
                onIteractionListener.openLinkVideo(post)
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
            root.setOnClickListener {
                onIteractionListener.openCardPost(post)
            }
        }
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
