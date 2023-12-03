package ru.netology.nmedia.dto


data class Post(
    val id: Long,
    val author: String,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
//    val countRepost: Int = 0,
//    val countViews: Int = 0,
//    val linkVideo: String = ""
)