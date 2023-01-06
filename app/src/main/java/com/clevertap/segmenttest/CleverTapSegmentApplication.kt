package com.clevertap.segmenttest

import android.app.Application
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CTInboxListener
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.SyncListener
import com.segment.analytics.Analytics
import com.segment.analytics.android.integrations.clevertap.CleverTapIntegration
import org.json.JSONObject

class CleverTapSegmentApplication : Application() {
    companion object {
        private const val TAG = "CT_SG_SAMPLE_APP>>"
        const val SOURCE_ANDROID_KEY = "VVsCywzFPA8wbGLyNFxwYnixchTUknJy"
        private const val DESTIN_CLEVERTAP_KEY = "CleverTap"
    }
    var ctCoreApi: CleverTapAPI? = null
        set(value) {
            Log.e(TAG, "ctCoreApi called with field = $field | value: $value")
            field = value
        }

    var segmentAnalytics : Analytics? = null

    override fun onCreate() {
        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE)
        super.onCreate()
        segmentCTIntegration()
    }

    private fun segmentCTIntegration() {
        val analytics: Analytics = Analytics.Builder(applicationContext, SOURCE_ANDROID_KEY)
            .logLevel(Analytics.LogLevel.VERBOSE)
            .use(CleverTapIntegration.FACTORY)
            .build()
        analytics.onIntegrationReady(DESTIN_CLEVERTAP_KEY, ::initCleverTap)  ///<----
        Analytics.setSingletonInstance(analytics)
    }



    private fun initCleverTap(ctInstance: CleverTapAPI?) {
        Log.e(TAG, "initCleverTap() called with: instance = $ctInstance. call onresume to update button")
        ctInstance ?: return
        ctInstance.enablePersonalization()
        ctInstance.syncListener = object : SyncListener {
            override fun profileDidInitialize(CleverTapID: String) { Log.e(TAG, "profileDidInitialize() called with: CleverTapID = $CleverTapID") }
            override fun profileDataUpdated(updates: JSONObject) { Log.e(TAG, "profileDataUpdated() called with: updates = \n ${updates.toString(4)}") }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CleverTapAPI.createNotificationChannel(applicationContext, "BRTesting", "YourChannelName", "YourChannelDescription", NotificationManager.IMPORTANCE_MAX, true)
        }

        //on initial app install, a call to getCleverTapAttributionIdentifier will return NULL until the sdk is fully initialized
        Log.e(TAG, "CT_ATTRIBUTION_ID: ${ctInstance.cleverTapAttributionIdentifier}")

        ctInstance.initializeInbox()
        ctInstance.ctNotificationInboxListener = object : CTInboxListener {
            override fun inboxDidInitialize() { Log.e(TAG, "inboxDidInitialize() called. call onresume to update button") }
            override fun inboxMessagesDidUpdate() { Log.e(TAG, "inboxMessagesDidUpdate() called. call onresume to update button") }
        }

        ctCoreApi = ctInstance
        segmentAnalytics = Analytics.with(applicationContext)

    }
}
