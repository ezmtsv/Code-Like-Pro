package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

class PostRepositoryImpl(
    //private val dao: PostDao,
) : PostRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}
    private var posts = emptyList<Post>()

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }

    override fun getAll(): List<Post> {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts")
            .build()
        return client.newCall(request)
            .execute()
            .let { it.body?.string() ?: throw RuntimeException("body is null") }
            .let {
                posts = gson.fromJson(it, typeToken.type)
                posts
            }

    }

    override fun updateContent(id: Long, content: String) {
        //dao.updateContentById(id, content, published)
        val post = posts.find { post -> post.id == id }
        val request: Request = Request.Builder()
            .post(gson.toJson(post?.copy(content = content)).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts")
            .build()
        val call = client.newCall(request)
        call.execute()
//        val body = requireNotNull(response.body)
//        val responseText = body.string()
//        return gson.fromJson(responseText, Post::class.java)
    }

    override fun like(post: Post): Post {
        //dao.likeById(id)
        val urlLink = "${BASE_URL}/api/posts/${post.id}/likes"
        var responseText: String
        val request = if (!post.likedByMe) {
            Request.Builder()
                .post(gson.toJson("").toRequestBody(jsonType))
                .url(urlLink)
                .build()
        } else {
            Request.Builder()
                .delete(gson.toJson("").toRequestBody(jsonType))
                .url(urlLink)
                .build()
        }
        request.let {
            val call = client.newCall(request)
            call.execute().use { response ->
                val body = requireNotNull(response.body)
                responseText = body.string()
            }
        }
        return gson.fromJson(responseText, Post::class.java)
    }

//    override fun share(id: Long): Post {
//        //dao.share(id)
//        val urlLink = "${BASE_URL}/api/posts/$id/share"
//        val request: Request = Request.Builder()
//            .post(gson.toJson("").toRequestBody(jsonType))
//            .url(urlLink)
//            .build()
//
//        val call = client.newCall(request)
//        var responseText: String
//        call.execute().use { response ->
//            val body = requireNotNull(response.body)
//            responseText = body.string()
//        }
//
//        return gson.fromJson(responseText, Post::class.java)
//    }

    override fun save(post: Post): Post {
        //dao.save(PostEntity.fromDto(post))
        val request: Request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts")
            .build()
        val call = client.newCall(request)
        val response = call.execute()
        val body = requireNotNull(response.body)
        val responseText = body.string()
        return gson.fromJson(responseText, Post::class.java)

    }

    override fun removeById(id: Long) {
        //dao.removeById(id)
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }
}