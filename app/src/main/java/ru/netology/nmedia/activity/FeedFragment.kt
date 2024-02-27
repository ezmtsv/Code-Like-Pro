package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnIteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dialogs.DialogAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.PostArg
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.AuthViewModel.Companion.DIALOG_IN
import ru.netology.nmedia.viewmodel.AuthViewModel.Companion.userAuth
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class FeedFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    companion object {
        //var Bundle.longArg: Long by LongArg
        var Bundle.uriArg: String? by StringArg
        var Bundle.postArg: Post by PostArg
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
                if (userAuth) viewModel.like(post)
                else {
                    DialogAuth.newInstance(DIALOG_IN).show(childFragmentManager, "TAG")
                }
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(
                    R.id.action_feedFragment_to_editPostFragment,
                    Bundle().apply {
                        //longArg = post.id
                        postArg = post
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
                    R.id.fragmentCard,
//                    Bundle().apply {
//                        longArg = post.id
//                    }
                    Bundle().apply {
                        postArg = post
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

        fun reload() {
            Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
//                .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                .setAction(R.string.retry_loading) { adapter.refresh() }
                .show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest(adapter::submitData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { state ->
                    binding.swipeRefreshLayout.isRefreshing =
                        state.refresh is LoadState.Loading ||
                                state.prepend is LoadState.Loading ||
                                state.append is LoadState.Loading
                    if (state.refresh is LoadState.Error) reload()
                }
            }
        }

        viewModel.dataInvisible.observe(viewLifecycleOwner) {
//            if (it.posts.isNotEmpty()) {
//                println("invisible posts ${it.posts.size}")
//                binding.newPostsGroup.visibility = View.VISIBLE
//            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swipeRefreshLayout.isRefreshing = state.refreshing
            adapter.refresh()
            if (state.error) {
                reload()
            }

            if (state.error403) {
                Snackbar.make(
                    binding.root,
                    "Ошибка авторизации, выполните вход",
                    Snackbar.LENGTH_LONG
                )
                    .setAction("ВХОД") {
                        appAuth.removeAuth()
                        userAuth = false
                        findNavController().navigate(
                            R.id.authFragment
                        )
                    }
                    .show()
            }

        }

        binding.fab.setOnClickListener {
            if (userAuth) findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            else DialogAuth.newInstance(DIALOG_IN).show(childFragmentManager, "TAG")
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            //viewModel.refreshPosts()
            adapter.refresh()
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
