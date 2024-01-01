package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException


class PostRepositoryImpl(private val dao: PostDao) : PostRepository {
    override val data: LiveData<List<Post>> = dao.getAll().map(List<PostEntity>::toDto)

    override suspend fun getAllAsync() {
        try {
            val response = PostsApi.retrofitService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val posts = response.body() ?: throw ApiError(response.code(), response.message())

            dao.insert(
                posts.map {
                    PostEntity.fromDto(it)
                })

//            val postsDao = data.value
//            postsDao?.forEach{post ->
//                if (!posts.contains(post)) {
//                    dao.removeById(post.id)
//                }
//            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveAsync(post: Post) {
        try {
            val response = PostsApi.retrofitService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeByIdAsync(id: Long) {
        val post = data.value?.find { it.id == id}!!
        try {
            dao.removeById(id)
            val response = PostsApi.retrofitService.removeById(id)
            if (!response.isSuccessful) {
                dao.insert(PostEntity.fromDto(post))
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            dao.insert(PostEntity.fromDto(post))
            throw NetworkError
        } catch (e: Exception) {
            dao.insert(PostEntity.fromDto(post))
            throw UnknownError
        }
    }

    override suspend fun likeAsync(post: Post) {
        try {
            dao.likeById(post.copy(likedByMe = !post.likedByMe).id)
            if (!post.likedByMe) {
                val response = PostsApi.retrofitService.likeById(post.id)
                if (!response.isSuccessful) {
                    dao.likeById(post.id)
                    throw ApiError(response.code(), response.message())
                }
            } else {
                val response = PostsApi.retrofitService.dislikeById(post.id)
                if (!response.isSuccessful) {
                    dao.likeById(post.id)
                    throw ApiError(response.code(), response.message())
                }
            }

        } catch (e: IOException) {
            dao.likeById(post.id)
            throw NetworkError
        } catch (e: Exception) {
            dao.likeById(post.id)
            throw UnknownError
        }
    }

    override suspend fun updateContentAsync(post: Post, content: String) {
        dao.insert(PostEntity.fromDto(post.copy(content = content)))
        try {
            val response = PostsApi.retrofitService.save(post.copy(content = content))
            if (!response.isSuccessful) {
                dao.insert(PostEntity.fromDto(post))
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            dao.insert(PostEntity.fromDto(post))
            throw NetworkError
        } catch (e: Exception) {
            dao.insert(PostEntity.fromDto(post))
            throw UnknownError
        }
    }
}