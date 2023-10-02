package ru.netology.nmedia.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnIteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.AndroidUtils.focusAndShowKeyboard
import ru.netology.nmedia.viewmodel.PostViewModel


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewModel: PostViewModel by viewModels()
        val adapter = PostsAdapter(object : OnIteractionListener {
            override fun onLike(post: Post) {
                viewModel.like(post.id)
            }

            override fun onShare(post: Post) {
                viewModel.share(post.id)
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onRemove(post: Post) {
                viewModel.remove(post.id)
            }

        })
        binding.list.adapter = adapter
        viewModel.data.observe(this) { posts ->
            val newPost = adapter.currentList.size < posts.size
            adapter.submitList(posts) {
                if (newPost) binding.list.smoothScrollToPosition(0)
            }
        }
        viewModel.edited.observe(this) { post ->
            if (post.id != 0) {
                binding.groupEdit.visibility = View.VISIBLE
                binding.textEdit.text = post.content
                binding.content.setText(post.content)
                binding.content.focusAndShowKeyboard()
            }
        }
        binding.save.setOnClickListener {
            val text = binding.content.text.toString()
            if (text.isBlank()) Toast.makeText(
                this,
                R.string.error_emty_content,
                Toast.LENGTH_LONG
            ).show()
            else {
                viewModel.savePost(text)
            }
            binding.content.clearFocus()
            binding.content.setText("")
            AndroidUtils.hideKeyBoard(it)
            binding.groupEdit.visibility = View.GONE
        }
        binding.icClose.setOnClickListener {
            viewModel.cancelEdit()
            binding.content.clearFocus()
            binding.content.setText("")
            AndroidUtils.hideKeyBoard(it)
            binding.groupEdit.visibility = View.GONE
        }
    }
}