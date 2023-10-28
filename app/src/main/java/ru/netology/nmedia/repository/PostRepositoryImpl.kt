package ru.netology.nmedia.repository

import androidx.lifecycle.map
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity

class PostRepositoryImpl(
    private val dao: PostDao,
) : PostRepository {
    override fun getAll() = dao.getAll().map { list ->
        list.map {
            it.toDto()
        }
    }

    override fun updateContent(id: Int, content: String, published: String) {
        dao.updateContentById(id, content, published)
    }

    override fun like(id: Int) {
        dao.likeById(id)
    }

    override fun share(id: Int) {
        dao.share(id)
    }

    override fun save(post: Post) {
        dao.save(PostEntity.fromDto(post))
    }

    override fun removeById(id: Int) {
        dao.removeById(id)
    }
}