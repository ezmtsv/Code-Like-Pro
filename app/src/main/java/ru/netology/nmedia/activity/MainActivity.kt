package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnIteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
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
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "")
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
                viewModel.share(post.id)
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                editPostLauncher.launch(post.content)
            }

            override fun onRemove(post: Post) {
                viewModel.remove(post.id)
            }

            override fun openLinkVideo(post: Post) {
                playVideo(Uri.parse(post.linkVideo))
            }

            val editPostLauncher =
                registerForActivityResult(NewPostResultContract(getString(R.string.KEY_EDIT_POST))) { result ->
                    result ?: return@registerForActivityResult
                    viewModel.savePost(result.toString())
                }

        })
        binding.list.adapter = adapter
        viewModel.data.observe(this) { posts ->
            val newPost = adapter.currentList.size < posts.size
            adapter.submitList(posts) {
                if (newPost) binding.list.smoothScrollToPosition(0)
            }
        }

        val newPostLauncher =
            registerForActivityResult(NewPostResultContract(getString(R.string.KEY_NEW_POST))) { result ->
                result ?: return@registerForActivityResult
                viewModel.savePost(result.toString())
            }

        binding.fab.setOnClickListener {
            newPostLauncher.launch("")
        }

    }

    private fun playVideo(contactUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, contactUri)
        if (intent.resolveActivity(packageManager) != null) {
            println("package Name  - ${intent.resolveActivity(packageManager).packageName}")
            startActivity(intent)
        } else {
            Toast.makeText(this, getString(R.string.notApp), Toast.LENGTH_SHORT).show()
        }
    }
}