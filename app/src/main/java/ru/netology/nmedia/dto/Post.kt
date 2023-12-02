package ru.netology.nmedia.dto


data class Post(
    val id: Long,
    val author: String,
    val published: String,
    val content: String,
    val likeByMe: Boolean,
    val countLike: Int = 0,
    val countRepost: Int = 0,
    val countViews: Int = 0,
    val linkVideo: String = ""
)