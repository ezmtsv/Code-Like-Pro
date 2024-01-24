package ru.netology.nmedia.dto

import ru.netology.nmedia.enumeration.AttachmentType


data class Post(
    val id: Long,
    val authorId: Long,
    val author: String,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val authorAvatar: String = "",
    val visibility: Boolean = true,
    var attachment: Attachment? = null,
    val ownedByMe: Boolean = false
//    val countRepost: Int = 0,
//    val countViews: Int = 0,
//    val linkVideo: String = ""
)

data class Attachment(
    val url: String,
    val type: AttachmentType,
)