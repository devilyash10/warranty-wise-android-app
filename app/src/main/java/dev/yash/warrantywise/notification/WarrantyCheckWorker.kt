package dev.yash.warrantywise.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dev.yash.warrantywise.MainActivity
import dev.yash.warrantywise.model.Product
import dev.yash.warrantywise.model.WarrantyStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class WarrantyCheckWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val uid = auth.currentUser?.uid ?: return Result.success()
        return try {
            val snapshot = firestore.collection("users").document(uid)
                .collection("products").get().await()

            snapshot.documents.mapNotNull { it.toObject(Product::class.java) }
                .forEach { product ->
                    when (product.warrantyStatus) {
                        WarrantyStatus.EXPIRING_SOON -> notify(
                            title = "⚠️ Warranty Expiring Soon",
                            body = "${product.productName} expires in ${product.daysUntilExpiry} days!",
                            id = product.productId.hashCode()
                        )
                        WarrantyStatus.EXPIRED -> notify(
                            title = "❌ Warranty Expired",
                            body = "${product.productName}'s warranty has expired.",
                            id = (product.productId + "_expired").hashCode()
                        )
                        else -> {}
                    }
                }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun notify(title: String, body: String, id: Int) {
        val channelId = "warranty_alerts"
        val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.createNotificationChannel(
            NotificationChannel(channelId, "Warranty Alerts", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Alerts for expiring warranties" }
        )

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        manager.notify(id, NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        )
    }
}
