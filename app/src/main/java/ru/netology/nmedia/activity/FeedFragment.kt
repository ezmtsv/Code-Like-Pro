package ru.netology.nmedia.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnIteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.PostEditArg
import ru.netology.nmedia.viewmodel.PostViewModel


class FeedFragment : Fragment() {
    companion object {
        var Bundle.postEditArg: Long by PostEditArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentFeedBinding.inflate(inflater, container, false)
        val viewModel: PostViewModel by activityViewModels()

        viewModel.cancelEdit()
        val adapter = PostsAdapter(object : OnIteractionListener {
            override fun onLike(post: Post) {
                viewModel.like(post)
            }

//            override fun onShare(post: Post) {
//                val intent = Intent().apply {
//                    action = Intent.ACTION_SEND
//                    putExtra(Intent.EXTRA_TEXT, post.content)
//                    type = "text/plain"
//                }
//
//                val shareIntent =
//                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
//                startActivity(shareIntent)
//                viewModel.share(post.id)
//            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(
                    R.id.action_feedFragment_to_editPostFragment,
                    Bundle().apply {
                        postEditArg = post.id
                    }
                )
            }

            override fun onRemove(post: Post) {
                viewModel.remove(post)
            }

//            override fun openLinkVideo(post: Post) {
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.linkVideo))
//                startActivity(intent)
//            }

            override fun openCardPost(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_fragmentCard,
                    Bundle().apply {
                        postEditArg = post.id
                    }
                )
            }

        })
        binding.list.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner) { state ->
            val newPost = adapter.currentList.size < state.posts.size
            adapter.submitList(state.posts) {
                if (newPost) binding.list.smoothScrollToPosition(0)
            }
            binding.emptyText.isVisible = state.empty
        }

        viewModel.dataInvisible.observe(viewLifecycleOwner) {
            if (it.posts.isNotEmpty()) {
                binding.newPostsGroup.visibility = View.VISIBLE
//                println("Get invisible ${it.posts.size} posts ")
            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swipeRefreshLayout.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                    .show()
            }
        }

        viewModel.newerCount.observe(viewLifecycleOwner) {
            if (it != 0) {
                binding.newPostsGroup.visibility = View.VISIBLE
            }
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshPosts()
        }
        binding.swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_red_light,
        )
        binding.btnAddNewPosts.setOnClickListener {
            viewModel.showPosts()
            binding.newPostsGroup.visibility = View.GONE
        }
        return binding.root
    }

    private fun Context.toast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}