package com.charm.phr.mphr.service.reminders

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.charm.phr.mphr.R
import com.charm.phr.mphr.ui.reminder.update_reminder.UpdateReminderHistoryActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.NotificationUtil
import com.charm.phr.mphr.util.NotificationUtil.showNotification


class NotificationBroadcastReceiver : BroadcastReceiver() {
    private val TAG = "NotificationBroadcastReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(ModelUtil.notificationId, 0)
        val title = intent.getStringExtra(ModelUtil.reminderTitle) ?: context.getString(R.string.notification_title)
        val message = intent.getStringExtra(ModelUtil.reminderMessage) ?: context.getString(R.string.notification_message)
        //Intent to ParentActivity
//        val intent = Intent(context, TourPageActivity::class.java)
//        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
//        stackBuilder.addParentStack(TourPageActivity::class.java)
//        stackBuilder.addNextIntent(intent)

        //intent to reminderHistoryUpdate
        val updateHistoryIntent = Intent(context, UpdateReminderHistoryActivity::class.java)
        updateHistoryIntent.putExtra(ModelUtil.ReminderId, intent.getStringExtra(ModelUtil.ReminderId))
        updateHistoryIntent.putExtra(ModelUtil.ReminderTime, intent.getStringExtra(ModelUtil.ReminderTime))
        updateHistoryIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

//        val pendingIntent: PendingIntent = TaskStackBuilder.create(context).run {
//            // Add the intent, which inflates the back stack.
//            addNextIntentWithParentStack(updateHistoryIntent)
//            // Get the PendingIntent containing the entire back stack.
//            getPendingIntent(notificationId,
//                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//            )
//        }

//        val pendingIntent: PendingIntent =   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            PendingIntent.getActivity(context, notificationId, updateHistoryIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT )
//        } else {
//            PendingIntent.getActivity(context, notificationId, updateHistoryIntent,  PendingIntent.FLAG_UPDATE_CURRENT)
//        }

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, notificationId, updateHistoryIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)


        NotificationUtil.REMINDER_CHANNEL_ID.showNotification(context, notificationId, title, message, pendingIntent)

    }

}