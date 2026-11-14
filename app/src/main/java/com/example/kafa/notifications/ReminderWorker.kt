package com.example.kafa.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.kafa.KafaApp
import com.example.kafa.MainActivity
import com.example.kafa.R
import com.example.kafa.data.local.ReminderDeliveryEntity
import com.example.kafa.data.local.toDomain
import com.example.kafa.domain.reminder.ReminderPolicy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class ReminderWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        return try {
            val manager = NotificationManagerCompat.from(applicationContext)
            createChannel()
            if (!canNotify(manager)) return Result.success()

            val database = (applicationContext as KafaApp).appComponent.eventDatabase()
            val deliveries = database.reminderDeliveryDao()
            val now = System.currentTimeMillis()
            database.eventDao().observeAll().first().forEach { row ->
                val event = row.toDomain()
                val previous = deliveries.find(event.id)
                if (ReminderPolicy.shouldNotify(event, now, previous?.sentAt, previous?.occurrenceId)) {
                    if (!canNotify(manager)) return Result.success()
                    val intent = Intent(applicationContext, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra(MainActivity.OPEN_REMINDERS, true)
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val days = ReminderPolicy.elapsedDays(event, now)
                    val message = if (event.history.isEmpty()) "Вы ещё не отмечали это событие" else "Дней с последнего повторения: $days"
                    val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(event.title)
                        .setContentText(message)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build()
                    manager.notify(event.id, 0, notification)
                    deliveries.save(ReminderDeliveryEntity(event.id, ReminderPolicy.latestOccurrence(event)?.id, now))
                }
            }
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun createChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Напоминания о событиях", NotificationManager.IMPORTANCE_DEFAULT)
        applicationContext.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun canNotify(manager: NotificationManagerCompat): Boolean {
        if (!manager.areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                applicationContext, android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return false
        val channel = applicationContext.getSystemService(NotificationManager::class.java).getNotificationChannel(CHANNEL_ID)
        return channel?.importance != NotificationManager.IMPORTANCE_NONE
    }

    companion object {
        const val CHANNEL_ID = "event_reminders"
        private const val WORK_NAME = "event_reminder_checks"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(6, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
