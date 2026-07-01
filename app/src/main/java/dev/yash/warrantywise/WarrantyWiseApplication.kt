package dev.yash.warrantywise

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import dagger.hilt.android.HiltAndroidApp
import dev.yash.warrantywise.notification.WarrantyCheckWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class WarrantyWiseApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleWarrantyCheck()
    }

    private fun scheduleWarrantyCheck() {
        val request = PeriodicWorkRequestBuilder<WarrantyCheckWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "warranty_daily_check",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
