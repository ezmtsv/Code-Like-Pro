## Домашнее задание к занятию «12. Flow»

#### Задача №1. New Posts

Легенда

    1. Посты, загружаемые в фоне через getNewer, не должны отображаться сразу в RecyclerView. Вместо этого должна появляться плашка, как в ВКонтакте

    2. При нажатии на плашку происходит плавный скролл RecyclerView к самому верху. Должны отображаться загруженные посты. Сама плашка после этого удаляется.

#### Реализация

Посмотрите гайдлайны Material Design: есть ли там элементы со схожим поведением. Если есть, используйте их, если нет, предложите свою реализацию.


#### Подсказки

Самый простой вариант «отображать / не отображать» — это добавить в Entity поле и переделать SELECT так, чтобы он показывал только те, у которых поле выставлено. Нажав на плашку, вы можете сделать UPDATE и выставить поле всем в «показывать»).

Попробуйте предусмотреть реализацию, при которой в getNewer не будут запрашиваться посты, которые уже есть у вас в локальной БД. Возможно, вам придётся по-другому считать количество: например, с помощью запроса в БД. Там как раз есть пример с COUNT.




[Описание задания 12](https://github.com/netology-code/andin-homeworks/tree/ANDIN-36/11_flow)