package ru.netology.nmedia.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException


class PostRepositoryImpl(private val dao: PostDao) : PostRepository {
    private val postsFlow = MutableStateFlow(emptyList<PostEntity>())
    override val data = dao.getAllVisible()
        .map(List<PostEntity>::toDto)

    override val dataInvisible = dao.getAllInvisible()
        .map(List<PostEntity>::toDto)

    override suspend fun getAllAsync() {
        try {
            val response = PostsApi.retrofitService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val posts = response.body() ?: throw ApiError(response.code(), response.message())


            val _posts = posts.map { post ->
                if (postsFlow.value.toDto()
                        .find { it.id == post.id && it.visibility } != null
                ) post.copy(visibility = true)
                else post
            }



            dao.insert(
                _posts.map {
                    PostEntity.fromDto(it)
                })

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = PostsApi.retrofitService.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
            emit(body.size)
        }
    }
        .catch { e -> throw AppError.from(e) }


    override suspend fun saveAsync(post: Post) {
        try {
            val response = PostsApi.retrofitService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body.copy(visibility = true)))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeByIdAsync(post: Post) {
        try {
            dao.removeById(post.id)
            val response = PostsApi.retrofitService.removeById(post.id)
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
            response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            dao.insert(PostEntity.fromDto(post))
            throw NetworkError
        } catch (e: Exception) {
            dao.insert(PostEntity.fromDto(post))
            throw UnknownError
        }
    }


    override suspend fun savePosts(posts: List<Post>) {
        dao.insert(posts.toEntity())
    }

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload) {
        try {
            val media = upload(upload)
            val postWithAttachment =
                post.copy(attachment = Attachment(media.id, AttachmentType.IMAGE))
            saveAsync(postWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

            val response = PostsApi.retrofitService.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getPosts() {
        dao.getAllPosts().flowOn(Dispatchers.IO).collect { posts ->
            postsFlow.update { posts }
        }
    }

}