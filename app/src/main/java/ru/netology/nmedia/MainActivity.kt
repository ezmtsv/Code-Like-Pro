package ru.netology.nmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post

class MainActivity : AppCompatActivity() {
    val tag:String = "TAG"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val post = Post(
            id = 1,
            likeByMe = false
        )

        with(binding) {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            countLike.text = post.countLike.toString()
            countRepost.text = "+${post.countRepost}"
            countViews.text = post.countViews.toString()
            icLike.setImageResource(
                if (post.likeByMe) R.drawable.ic_favorite_red_24
                else R.drawable.ic_favorite_24
            )
            icLike.setOnClickListener {
                post.likeByMe = !post.likeByMe
                icLike.setImageResource(
                    if (post.likeByMe) {
                        post.countLike++
                        R.drawable.ic_favorite_red_24
                    } else {
                        post.countLike--
                        R.drawable.ic_favorite_24
                    }
                )
                countLike.text = post.countLike.toString()
            }
            icShare.setOnClickListener {
                countRepost.text = post.getCountClick(++post.countRepost)
                //countRepost.text = post.getCountClick(999)
            }
//            root.setOnClickListener {
//                Log.d(tag, "click root!")
//            }
//            avatar.setOnClickListener {
//                Log.d(tag, "click avatar!")
//            }
        }
    }
}