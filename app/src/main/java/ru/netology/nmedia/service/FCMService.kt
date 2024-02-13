package ru.netology.nmedia.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.AppActivity
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.PushMessage
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {
    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()


    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
//        message.data[action]?.let {
//            when (it) {
//                Action.LIKE.toString() -> handleLike(
//                    gson.fromJson(
//                        message.data[content],
//                        Like::class.java
//                    )
//                )
//
//                Action.NEWPOST.toString() -> handleNewPost(
//                    gson.fromJson(
//                        message.data[content],
//                        NewPost::class.java
//                    )
//                )
//
//                else -> println("Notification haven't handler!")
//            }
//        }

        val pushMessage = gson.fromJson(
            message.data[content],
            PushMessage::class.java
        )
        when (pushMessage.recipientId) {
            AppAuth.getInstance().authState.value.id, null -> {
                showNotification(pushMessage)
            }

            else -> {
                AppAuth.getInstance().sendPushToken(null)
            }

        }

    }

    private fun showNotification(content: PushMessage) {
        val intent = Intent(this, AppActivity::class.java)
        intent.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)


        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(content.content)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_large))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)                    // auto close
            .build()
        notify(notification)
    }

//    private fun handleNewPost(content: NewPost) {
//
//        val intent = Intent(this, AppActivity::class.java)
//        intent.apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
//
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentTitle(
//                getString(
//                    R.string.notification_new_post,
//                    content.userName,
//                )
//            )
//            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_large))
//            .setStyle(
//                NotificationCompat.BigTextStyle()
//                    .bigText(content.textPost.subSequence(0, 105).toString().plus("..."))
//            )
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true)                    // auto close
//            .build()
//        notify(notification)
//    }

    override fun onNewToken(token: String) {
        //println("FCM token = $token")
        AppAuth.getInstance().sendPushToken(token)
    }

//    private fun handleLike(content: Like) {
//        val intent = Intent(this, AppActivity::class.java)
//        println("FCM handleLike(content: Like)")
//        intent.apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
//
//
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentTitle(
//                getString(
//                    R.string.notification_user_liked,
//                    content.userName,
//                    content.postAuthor,
//                )
//            )
//            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_large))
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true)                    // auto close
//            .build()
//        notify(notification)
//    }

    private fun notify(notification: Notification) {
        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this)
                .notify(Random.nextInt(100_000), notification)
        }
    }
}

//enum class Action {
//    LIKE,
//    NEWPOST,
//}
//
//data class Like(
//    val userId: Long,
//    val userName: String,
//    val postId: Long,
//    val postAuthor: String,
//)
//
//data class NewPost(
//    val userId: Long,
//    val userName: String,
//    val postId: Long,
//    val textPost: String,
//    val postAuthor: String,
//)


