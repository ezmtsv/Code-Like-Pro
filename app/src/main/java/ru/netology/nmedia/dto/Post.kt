package ru.netology.nmedia.dto

import android.util.Log


data class Post(
    val id: Int = 1,
    val author: String = "Нетология. Университет интернет-профессий будущего",
    val authorAvatar: String = "@sample/posts_avatars",
    val published: String = "21 мая в 18:36",
    val content: String = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
    var likeByMe:Boolean = false,
    var countLike:Int = 45,
    var countRepost:Int = 75,
    var countViews:Int = 101
) {
    fun getCountClick (cnt:Int): String {
        var tmp:Int = 0
        val result:String = when {
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