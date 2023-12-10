package ru.netology.nmedia.repository

import android.content.Context
import android.widget.Toast
import ru.netology.nmedia.activity.AppActivity
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post


class PostRepositoryImpl() : PostRepository {
    override fun getAllAsync(callback: PostRepository.Callback<List<Post>>) {
        PostApi.retrofitService.getAll().enqueue(object : retrofit2.Callback<List<Post>> {
            override fun onResponse(
                call: retrofit2.Call<List<Post>>,
                response: retrofit2.Response<List<Post>>
            ) {
                try {
                    if (!response.isSuccessful) {
                        val txt = errorServer(response.code())
                        callback.onError(java.lang.RuntimeException(response.message()), txt)
                        return
                    }
                    callback.onSuccess(
                        response.body() ?: throw java.lang.RuntimeException("Body is null")
                    )
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }

            override fun onFailure(call: retrofit2.Call<List<Post>>, t: Throwable) {
                callback.onError(Exception(t), "Проверьте ваше подключение к сети!")
            }
        })
    }

    override fun updateContentAsync(
        post: Post,
        content: String,
        callback: PostRepository.Callback<Post>
    ) {
        PostApi.retrofitService.save(post.copy(content = content))
            .enqueue(object : retrofit2.Callback<Post> {
                override fun onResponse(
                    call: retrofit2.Call<Post>,
                    response: retrofit2.Response<Post>
                ) {
                    try {
                        if (!response.isSuccessful) {
                            val txt = errorServer(response.code())
                            callback.onError(java.lang.RuntimeException(response.message()), txt)
                            return
                        }
                        callback.onSuccess(
                            response.body() ?: throw java.lang.RuntimeException("Post is null")
                        )
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

                override fun onFailure(call: retrofit2.Call<Post>, t: Throwable) {
                    println("Exception: updateContentAsync")
                    callback.onError(Exception(t))
                }
            })
    }

    override fun likeAsync(post: Post, callback: PostRepository.Callback<Post>) {

        if (!post.likedByMe) {
            PostApi.retrofitService.likeById(post.id).enqueue(object : retrofit2.Callback<Post> {
                override fun onResponse(
                    call: retrofit2.Call<Post>,
                    response: retrofit2.Response<Post>
                ) {
                    try {
                        if (!response.isSuccessful) {
                            val txt = errorServer(response.code())
                            callback.onError(java.lang.RuntimeException(response.message()), txt)
                            return
                        }
                        callback.onSuccess(
                            response.body() ?: throw java.lang.RuntimeException("Post is null")
                        )
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

                override fun onFailure(call: retrofit2.Call<Post>, t: Throwable) {
                    println("Exception: likeAsync")
                    callback.onError(Exception(t))
                }
            }

            )
        } else {
            PostApi.retrofitService.dislikeById(post.id).enqueue(object : retrofit2.Callback<Post> {
                override fun onResponse(
                    call: retrofit2.Call<Post>,
                    response: retrofit2.Response<Post>
                ) {
                    try {
                        if (!response.isSuccessful) {
                            val txt = errorServer(response.code())
                            callback.onError(java.lang.RuntimeException(response.message()), txt)
                            return
                        }
                        callback.onSuccess(
                            response.body() ?: throw java.lang.RuntimeException("Post is null")
                        )
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

                override fun onFailure(call: retrofit2.Call<Post>, t: Throwable) {
                    callback.onError(Exception(t))
                }
            }

            )
        }
    }


    override fun saveAsync(post: Post, callback: PostRepository.Callback<Post>) {
        PostApi.retrofitService.save(post).enqueue(object : retrofit2.Callback<Post> {
            override fun onResponse(
                call: retrofit2.Call<Post>,
                response: retrofit2.Response<Post>
            ) {
                try {
                    if (!response.isSuccessful) {
                        val txt = errorServer(response.code())
                        callback.onError(java.lang.RuntimeException(response.message()), txt)
                        return
                    }
                    callback.onSuccess(
                        response.body() ?: throw java.lang.RuntimeException("Post is null")
                    )
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }

            override fun onFailure(call: retrofit2.Call<Post>, t: Throwable) {
                callback.onError(Exception(t))
            }
        }

        )
    }


    override fun removeByIdAsync(id: Long, callback: PostRepository.Callback<Unit>) =

        PostApi.retrofitService.removeById(id).enqueue(object : retrofit2.Callback<Unit> {
            override fun onResponse(
                call: retrofit2.Call<Unit>,
                response: retrofit2.Response<Unit>
            ) {
                try {
                    if (!response.isSuccessful) {
                        val txt = errorServer(response.code())
                        callback.onError(java.lang.RuntimeException(response.message()), txt)
                        return
                    }
                    callback.onSuccess(
                        response.body() ?: throw java.lang.RuntimeException("Post is null")
                    )
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }

            override fun onFailure(call: retrofit2.Call<Unit>, t: Throwable) {

                callback.onError(Exception(t))
            }


        })

    fun errorServer(err: Int): String {
        val message = when (err) {
            500 -> {
                "Внутренняя ошибка сервера!"
            }
            501 ->{
                "Сервер не может распознать запрос!"
            }
            503 ->{
                "Сервер временно не доступен!"
            }
            else -> "Неизвестная ошибка сервера"
        }
        return message
    }


}