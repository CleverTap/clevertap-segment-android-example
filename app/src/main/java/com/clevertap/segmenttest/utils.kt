package com.clevertap.segmenttest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.res.Resources
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.clevertap.android.sdk.CleverTapAPI
import com.segment.analytics.Analytics


fun AppCompatActivity.getCtCoreApi(): CleverTapAPI? {
    return (application as CleverTapSegmentApplication).ctCoreApi
}


fun AppCompatActivity.getSegmentAnalyticsApi(): Analytics? {
    return (application as CleverTapSegmentApplication).segmentAnalytics
}



val Number.dpValue get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()

fun Context?.showNotif(
    title: String = "title",
    body: String? = "body",
    @DrawableRes smallIcon: Int = android.R.drawable.ic_notification_clear_all,
    channelId: String = "defualt_channel",
    channelInfo: String = "channel info",
    priorityFromBundle: Int? = null,
    notificationId: Int = 0,
    soundUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
    autoCancel: Boolean = true,
    onClickPendingIntent: PendingIntent? = null,
) {
    this ?: return
    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
    log("showNotif() called with: body = $body, smallIcon = $smallIcon, channelId = $channelId, channelInfo = $channelInfo, priorityFromBundle = $priorityFromBundle, title = $title, notificationId = $notificationId, soundUri = $soundUri, autoCancel = $autoCancel, onClickPendingIntent = $onClickPendingIntent")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val priorityFinal = priorityFromBundle ?: NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelInfo, priorityFinal)
        manager.createNotificationChannel(channel)
    }

    val notificationBuilder = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(smallIcon)
        .setContentTitle(title)
        .setAutoCancel(autoCancel)

    if (soundUri != null) notificationBuilder.setSound(soundUri)
    if (body != null) notificationBuilder.setContentText(body)
    if (onClickPendingIntent != null) notificationBuilder.setContentIntent(onClickPendingIntent)

    manager.notify(notificationId, notificationBuilder.build())
}


fun log(key: String, value: Any? = null, tag: String = "CUSTOM_LOGS", context: Context? = null, print:Boolean = false) {
    val msg = if(value ==null) key else "$key:$value"
    Log.e(tag,msg )
    if(print)println(msg)
    if(context!=null) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}