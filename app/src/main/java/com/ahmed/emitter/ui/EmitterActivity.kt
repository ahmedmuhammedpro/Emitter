package com.ahmed.emitter.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import com.ahmed.emitter.R
import timber.log.Timber

class EmitterActivity : AppCompatActivity() {

    private lateinit var middleManReceiver: MiddleManReceiver
    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emitter)

        middleManReceiver = MiddleManReceiver()
        val intentFilter = IntentFilter(EMITTER_APP_ACTION)
        registerReceiver(middleManReceiver, intentFilter)
    }

    override fun onResume() {
        super.onResume()
        if (intent != null && intent.hasExtra(EXTRA_INSERTING_RESULT_KEY) && intent.getBooleanExtra(EXTRA_NEW_REQUEST_KEY, false)) {
            showInsertingUserResult()
        }
    }

    override fun onStop() {
        super.onStop()
        alertDialog?.dismiss()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { this.intent = it }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::middleManReceiver.isInitialized) {
            unregisterReceiver(middleManReceiver)
        }
    }

    private fun showInsertingUserResult() {
        alertDialog?.dismiss()
        val result = intent.getStringExtra(EXTRA_INSERTING_RESULT_KEY)
        val userName = intent.getStringExtra(EXTRA_USER_NAME_KEY)
        val message =
            if (result == "OK")
                getString(R.string.saving_result_success_message1) + " $userName " + getString(R.string.saving_result_success_message2)
            else
                getString(R.string.saving_result_failed_message) + " $userName!"
        alertDialog = AlertDialog.Builder(this)
            .setTitle(R.string.saving_result_title)
            .setMessage(message)
            .setPositiveButton(R.string.ok_button) { _, _ -> }
            .create()

        alertDialog!!.show()
        intent.putExtra(EXTRA_NEW_REQUEST_KEY, false)
    }

    class MiddleManReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Timber.i("called")
            val isNotificationClosed = intent.getBooleanExtra(EXTRA_CLOSE_NOTIFICATION_KEY, false)
            if (!isNotificationClosed) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    showNotification(context)
                }
                val result = intent.getStringExtra(EXTRA_INSERTING_RESULT_KEY)
                val userName = intent.getStringExtra(EXTRA_USER_NAME_KEY)
                val emitterIntent = Intent(context, EmitterActivity::class.java)
                emitterIntent.putExtra(EXTRA_INSERTING_RESULT_KEY, result)
                emitterIntent.putExtra(EXTRA_USER_NAME_KEY, userName)
                emitterIntent.putExtra(EXTRA_NEW_REQUEST_KEY, true)
                emitterIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(emitterIntent)
            } else {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(NOTIFICATION_MAIN_ID_NUMBER)
            }
        }

        private fun showNotification(context: Context) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(NOTIFICATION_MAIN_CHANNEL_ID, "", NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(NOTIFICATION_MAIN_ID_NUMBER, createNotification(context))
        }

        private fun createNotification(context: Context): Notification {
            val closeIntent = Intent(context, MiddleManReceiver::class.java)
            closeIntent.putExtra(NOTIFICATION_MAIN_ID_KEY, NOTIFICATION_MAIN_ID)
            closeIntent.putExtra(EXTRA_CLOSE_NOTIFICATION_KEY, true)
            val closePendingIntent = PendingIntent.getBroadcast(context, 0, closeIntent, 0)

            val builder = NotificationCompat.Builder(context, NOTIFICATION_MAIN_ID)
                    .setSmallIcon(R.drawable.ic_baseline_person_24)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(context.getString(R.string.main_notification_content))
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .addAction(0, context.getString(R.string.close_button), closePendingIntent)

            return builder.build()
        }

    }

    companion object {
        const val EMITTER_APP_ACTION = "com.ahmed.emitter.ACTION"
        const val EXTRA_INSERTING_RESULT_KEY = "com.ahmed.emitter.EXTRA_INSERTING_RESULT"
        const val EXTRA_USER_NAME_KEY = "com.ahmed.emitter.EXTRA_USER_NAME"
        const val EXTRA_NEW_REQUEST_KEY = "com.ahmed.emitter.EXTRA_NEW_REQUEST"
        const val NOTIFICATION_MAIN_ID =  "com.ahmed.emitter.NOTIFICATION_MAIN_ID"
        const val NOTIFICATION_MAIN_ID_KEY =  "com.ahmed.emitter.NOTIFICATION_MAIN_ID_KEY"
        const val NOTIFICATION_MAIN_ID_NUMBER =  11
        const val NOTIFICATION_MAIN_CHANNEL_ID =  "com.ahmed.emitter.NOTIFICATION_MAIN_CHANNEL_ID"
        const val EXTRA_CLOSE_NOTIFICATION_KEY =  "com.ahmed.emitter.EXTRA_CLOSE_NOTIFICATION_KEY"
    }
}