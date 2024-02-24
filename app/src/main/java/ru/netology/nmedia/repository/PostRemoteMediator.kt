package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError

//@OptIn(ExperimentalPagingApi::class)
//class PostRemoteMediator(
//    private val apiService: PostsApiService,
//    private val postDao: PostDao,
//    private val postRemoteKeyDao: PostRemoteKeyDao,
//    private val appDb: AppDb,
//) : RemoteMediator<Int, PostEntity>() {
//    override suspend fun load(
//        loadType: LoadType,
//        state: PagingState<Int, PostEntity>
//    ): MediatorResult {
//        try {
//            val response = when (loadType) {
//                LoadType.REFRESH -> {
//                    println("REFRESH")
//                    apiService.getLatest(state.config.initialLoadSize)
//                }
//                LoadType.PREPEND -> {
//                    val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(
//                        endOfPaginationReached = false
//                    )
//                    println("PREPEND id = $id")
//                    apiService.getAfter(id, state.config.pageSize)
//                }
//                LoadType.APPEND -> {
//                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(
//                        endOfPaginationReached = false
//                    )
//                    println("APPEND id = $id")
//                    apiService.getBefore(id, state.config.pageSize)
//                }
//            }
//
//            if (!response.isSuccessful) {
//                throw ApiError(response.code(), response.message())
//            }
//
//            val body = response.body() ?: throw ApiError(
//                response.code(),
//                response.message(),
//            )
//
//            println("body.first().id ${body.first().id}")
//            println("body.last().id ${body.last().id}")
//
//            appDb.withTransaction {
//                when (loadType) {
//                    LoadType.REFRESH -> {
//                        postRemoteKeyDao.clear()
//                        postRemoteKeyDao.insert(
//                            listOf(
//                                PostRemoteKeyEntity(
//                                    PostRemoteKeyEntity.KeyType.AFTER,
//                                    body.first().id
//                                ),
//                                PostRemoteKeyEntity(
//                                    PostRemoteKeyEntity.KeyType.BEFORE,
//                                    body.last().id
//                                )
//                            )
//                        )
//                        postDao.clear()
//                    }
//
//                    LoadType.PREPEND -> {
//                        postRemoteKeyDao.insert(
//                            PostRemoteKeyEntity(
//                                PostRemoteKeyEntity.KeyType.AFTER,
//                                body.first().id
//                            )
//
//                        )
//                    }
//
//                    LoadType.APPEND -> {
//                        postRemoteKeyDao.insert(
//                            PostRemoteKeyEntity(
//                                PostRemoteKeyEntity.KeyType.BEFORE,
//                                body.last().id
//                            )
//                        )
//                    }
//                }
//
//                postDao.insert(body.map(PostEntity::fromDto))
//            }
//            return MediatorResult.Success(body.isEmpty())
//        } catch (e: Exception) {
//            return MediatorResult.Error(e)
//        }
//    }
//
//
//}

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val service: PostsApiService,
    private val db: AppDb,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    val id = postRemoteKeyDao.max()
                    if (id == null) {
                        service.getLatest(state.config.initialLoadSize)
                    } else service.getAfter(id, state.config.pageSize)
                }

                LoadType.PREPEND -> {
//                    val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(
//                        endOfPaginationReached = false
//                    )
//
//                    println("PREPEND id = $id")
//                    service.getAfter(id, state.config.pageSize)

                    return MediatorResult.Success(true)
                }

                LoadType.APPEND -> {

                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    service.getBefore(id, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message(),
            )

            db.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        if (postRemoteKeyDao.isEmpty()) {
                            postRemoteKeyDao.clear()
                            postRemoteKeyDao.insert(
                                listOf(
                                    PostRemoteKeyEntity(
                                        type = PostRemoteKeyEntity.KeyType.AFTER,
                                        id = body.first().id,
                                    ),
                                    PostRemoteKeyEntity(
                                        type = PostRemoteKeyEntity.KeyType.BEFORE,
                                        id = body.last().id,
                                    ),
                                )
                            )
                        } else {
                            postRemoteKeyDao.insert(
                                listOf(
                                    PostRemoteKeyEntity(
                                        type = PostRemoteKeyEntity.KeyType.AFTER,
                                        id = body.first().id,
                                    ),
                                )
                            )
                        }
                        //postDao.clear()
                    }

                    LoadType.PREPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.AFTER,
                                id = body.first().id,
                            )
                        )
                    }

                    LoadType.APPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.BEFORE,
                                id = body.last().id,
                            )
                        )
                    }
                }
                postDao.insert(body.toEntity())
            }
            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}