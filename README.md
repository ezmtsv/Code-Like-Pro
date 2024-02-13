## Домашнее задание к занятию «15. Рассылка и приём push-уведомлений»

#### Задача №1. RecipientId

Описание

Реализуйте на клиентской стороне при получении push-уведомления проверку recipientId. Сервер будет присылать вам его в Push'.

Для этого сравнивайте полученный recipientId с тем, что хранится у вас в AppAuth, и выполняйте одно из действий:

- если recipientId = тому, что в AppAuth, то всё ok, показываете Notification;
- если recipientId = 0 (и не равен вашему), сервер считает, что у вас анонимная аутентификация и вам нужно переотправить свой push token;
- если recipientId != 0 (и не равен вашему), значит сервер считает, что на вашем устройстве другая аутентификация и вам нужно переотправить свой push token;
- если recipientId = null, то это массовая рассылка, показываете Notification.

Для тестирования отправляйте запрос вида:

POST http://localhost:9999/api/pushes?token=<put your token here>
Content-Type: application/json

{
  "recipientId": null,
  "content": "Wow!"
}
Используйте для этого любое средство: Postman, cURL или любое другое, включая OkHttp, которые мы рассматривали на лекции.


[Описание задания 15](https://github.com/netology-code/andin-homeworks/tree/ANDIN-36/14_pushes)