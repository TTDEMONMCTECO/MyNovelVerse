package com.example.novelverse.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.R
import com.example.novelverse.data.local.NovelVerseDatabase
import kotlinx.coroutines.delay

class DownloadChapterWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val novelId = inputData.getString("NOVEL_ID") ?: return Result.failure()
        val chapterId = inputData.getString("CHAPTER_ID") ?: return Result.failure()
        val novelTitle = inputData.getString("NOVEL_TITLE") ?: "Novel"
        val chapterTitle = inputData.getString("CHAPTER_TITLE") ?: "Chapter"

        val database = NovelVerseDatabase.getInstance(applicationContext)
        val downloadDao = database.downloadDao()
        val chapterDao = database.chapterDao()

        createNotificationChannel()
        showNotification("Downloading $novelTitle", chapterTitle)

        // Simulate network download progression
        for (progress in listOf(25, 50, 75, 100)) {
            delay(300)
            setProgress(workDataOf("PROGRESS" to progress))
        }

        // Mark chapter downloaded
        val existing = chapterDao.getChapterByIdOnce(chapterId)
        if (existing != null) {
            val content = existing.content.ifBlank {
                "Offline chapter content for $chapterTitle ($novelTitle).\n\nDownloaded successfully via NovelVerse Background Worker."
            }
            chapterDao.updateDownloadedContent(chapterId, content)
        }

        showNotification("Download Complete", "$novelTitle: $chapterTitle is ready for offline reading.")
        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "novelverse_downloads",
                "NovelVerse Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Offline chapter download notifications"
            }
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, message: String) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(applicationContext, "novelverse_downloads")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()
        manager.notify(101, notification)
    }
}
