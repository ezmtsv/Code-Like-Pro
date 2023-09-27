package ru.netology.nmedia.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.viewmodel.PostViewModel


class MainActivity : AppCompatActivity() {
    private val tag: String = "TAG"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewModel: PostViewModel by viewModels()
        viewModel.data.observe(this) { post ->
            with(binding) {
                author.text = post.author
                published.text = post.published
                content.text = post.content
                countLike.text = getCountClick(post.countLike)
                countRepost.text = getCountClick(post.countRepost)
                countViews.text = getCountClick(post.countViews)
                icLike.setImageResource(
                    if (post.likeByMe) R.drawable.ic_favorite_red_24
                    else R.drawable.ic_favorite_24
                )
            }
        }
        with(binding) {
            icLike.setOnClickListener {
                Log.d(tag, "click icLike")
                viewModel.like()
            }
            icShare.setOnClickListener {
                Log.d(tag, "click icShare")
                viewModel.share()
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