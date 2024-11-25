package za.co.varsitycollege.st10090442.egovernmentserviceportal.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import za.co.varsitycollege.st10090442.egovernmentserviceportal.MainActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.R

class EGovFirebaseMessagingService : FirebaseMessagingService() {
    private val CHANNEL_ID = "egov_notifications"
    private val NOTIFICATION_ID = 100

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendRegistrationToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Handle notification payload
        remoteMessage.notification?.let { notification ->
            showNotification(
                notification.title ?: "New Notification",
                notification.body ?: "You have a new notification"
            )
        }

        // Handle data payload
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "New Notification"
            val message = remoteMessage.data["message"] ?: "You have a new notification"
            showNotification(title, message)
        }
    }

    private fun sendRegistrationToServer(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .update("fcmToken", token)
            .addOnFailureListener { e ->
                // Log the error or handle it appropriately
                android.util.Log.e("FCM", "Failed to update token: ${e.message}")
            }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "E-Government Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "E-Government Services Notifications"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 300, 400, 500)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create pending intent for notification click
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent,
            pendingIntentFlags
        )

        // Build the notification
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        // Show the notification
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
}