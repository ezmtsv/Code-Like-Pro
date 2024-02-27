package ru.netology.nmedia.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.viewmodel.AuthViewModel.Companion.userAuth

interface OnIteractionListener {
    fun onLike(post: Post)

    //    fun onShare(post: Post)
    fun onEdit(post: Post)
    fun onRemove(post: Post)
//    fun openLinkVideo(post: Post)

    fun openCardPost(post: Post)
    fun openSpacePhoto(post: Post)
}

class PostsAdapter(
    private val onIteractionListener: OnIteractionListener
) : PagingDataAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onIteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position) ?: return
        holder.bind(post)

    }

}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onIteractionListener: OnIteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SuspiciousIndentation", "SimpleDateFormat", "CheckResult")
    fun bind(post: Post) {

        binding.apply {
            author.text = post.author

            val sdf = java.text.SimpleDateFormat("dd MMMM yyyy, HH:mm")
            val date = java.util.Date(post.published * 1000)
//            published.text = sdf.format(date).toString()
            published.text = post.id.toString()

            content.text = post.content
            icLike.isChecked = post.likedByMe
            icLike.text = AndroidUtils.getCountClick(post.likes)
//            icShare.text = AndroidUtils.getCountClick(post.countRepost)
            icShare.text = AndroidUtils.getCountClick(1200)
            icView.text = AndroidUtils.getCountClick(450)

//            if (post.linkVideo != "") groupVideo.visibility = View.VISIBLE
//            else groupVideo.visibility = View.GONE
            groupVideo.visibility = View.GONE
            icLike.setOnClickListener {
                if (!userAuth) icLike.isChecked = post.likedByMe
                onIteractionListener.onLike(post)
            }
            //println("avatar ${post.authorAvatar}")
            Glide.with(avatar)
                .load("http://10.0.2.2:9999/avatars/${post.authorAvatar}")
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.ic_error_100dp)
                .timeout(10_000)
                .circleCrop()
                .into(avatar)

            post.attachment?.url?.let { url ->
                groupVideo.visibility = View.VISIBLE
                Glide.with(idVideo)
                    .load("http://10.0.2.2:9999/media/$url")
                    .placeholder(R.drawable.ic_loading_100dp)
                    .timeout(10_000)
                    .into(idVideo)
            } ?: run {
                groupVideo.visibility = View.GONE
            }

            idVideo.setOnClickListener {
                onIteractionListener.openSpacePhoto(post)
            }
//            idVideo.setOnClickListener {
//                onIteractionListener.openLinkVideo(post)
//            }
//            play.setOnClickListener {
//                onIteractionListener.openLinkVideo(post)
//            }
//            icShare.setOnClickListener {
//                onIteractionListener.onShare(post)
//            }

            menu.isVisible = post.ownedByMe
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
