## Домашнее задание к занятию «11. Coroutines в Android»

#### Задача №1. Remove & likes

Легенда
Используя код и сервер из лекции, реализуйте в проекте функциональность удаления и проставления лайков. Для этого нужно отредактировать PostViewModel и PostRepositoryImpl:

// PostViewModel
fun likeById(id: Long) {
    TODO()
}

fun removeById(id: Long) {
    TODO()
}

// PostRepositoryImpl
override suspend fun removeById(id: Long) {
    TODO("Not yet implemented")
}

override suspend fun likeById(id: Long) {
    TODO("Not yet implemented")
}



[Описание задания 11](https://github.com/netology-code/andin-homeworks/tree/ANDIN-36/10_mainscope)