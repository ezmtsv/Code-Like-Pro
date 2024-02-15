package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnIteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.dialogs.DialogAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.PostEditArg
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.AuthViewModel.Companion.DIALOG_IN
import ru.netology.nmedia.viewmodel.AuthViewModel.Companion.userAuth
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.viewmodel.ViewModelFactory

@OptIn(ExperimentalCoroutinesApi::class)
class FeedFragment : Fragment() {
    private val dependencyContainer = DependencyContainer.getInstance()

    companion object {
        var Bundle.postEditArg: Long by PostEditArg
        var Bundle.uriArg: String? by StringArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentFeedBinding.inflate(inflater, container, false)
        val viewModel: PostViewModel by viewModels(
            ownerProducer = ::requireParentFragment,
            factoryProducer = {
                ViewModelFactory(
                    dependencyContainer.repository,
                    dependencyContainer.appAuth,
                    dependencyContainer.apiService
                )
            }
        )

        viewModel.cancelEdit()
        val adapter = PostsAdapter(object : OnIteractionListener {
            override fun onLike(post: Post) {
                if (userAuth) viewModel.like(post)
                else {
                    DialogAuth.newInstance(DIALOG_IN).show(childFragmentManager, "TAG")
                }
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
//                findNavController().navigate(
//                    R.id.action_feedFragment_to_fragmentCard,
//                    Bundle().apply {
//                        postEditArg = post.id
//                    }
//                )

                findNavController().navigate(
                    R.id.fragmentCard,
                    Bundle().apply {
                        postEditArg = post.id
                    }
                )
            }

            override fun openSpacePhoto(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_spacePhoto,
                    Bundle().apply {
                        uriArg = post.attachment?.url
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

            if (state.error403) {
                Snackbar.make(
                    binding.root,
                    "Ошибка авторизации, выполните вход",
                    Snackbar.LENGTH_LONG
                )
                    .setAction("ВХОД") {
                        dependencyContainer.appAuth.removeAuth()
                        userAuth = false
                        findNavController().navigate(
                            R.id.authFragment
                        )
                    }
                    .show()
            }

        }

        viewModel.newerCount.observe(viewLifecycleOwner) {
            if (it != 0) {
                binding.newPostsGroup.visibility = View.VISIBLE
            }
        }

        binding.fab.setOnClickListener {
            if (userAuth) findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            else DialogAuth.newInstance(DIALOG_IN).show(childFragmentManager, "TAG")
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

}