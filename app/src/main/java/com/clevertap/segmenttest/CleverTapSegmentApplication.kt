package com.clevertap.segmenttest

import android.app.Application
import android.app.NotificationManager
import android.os.Build
import android.os.Handler
import android.util.Log
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.SyncListener
import com.segment.analytics.Analytics
import com.segment.analytics.android.integrations.clevertap.CleverTapIntegration
import org.json.JSONObject

class CleverTapSegmentApplication : Application() {
    private var clevertap: CleverTapAPI? = null

    private val TAG = String.format("%s.%s", "CLEVERTAP", CleverTapSegmentApplication::class.java.name)
    private val WRITE_KEY = "VVsCywzFPA8wbGLyNFxwYnixchTUknJy"
    private val CLEVERTAP_KEY = "CleverTap"
    private var sCleverTapSegmentEnabled = false
    private var handler: Handler? = null

    //on initial app install, a call to getCleverTapAttributionIdentifier will return NULL until the sdk is fully initialized
    private fun cleverTapAttributionIdentifier() {
        //on initial app install, a call to getCleverTapAttributionIdentifier will return NULL until the sdk is fully initialized
        val cleverTapID = clevertap!!.cleverTapAttributionIdentifier
        if (cleverTapID == null) {
            if (handler == null) {
                handler = Handler()
            }
            handler!!.postDelayed({ cleverTapAttributionIdentifier() }, 500)
        } else {
            Log.d("CT_ATTRIBUTION_ID", cleverTapID)
        }
    }

    override fun onCreate() {
        super.onCreate()
        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.DEBUG)
        val analytics = Analytics.Builder(applicationContext, WRITE_KEY)
            .logLevel(Analytics.LogLevel.VERBOSE)
            .use(CleverTapIntegration.FACTORY)
            .build()
        analytics.onIntegrationReady<CleverTapAPI>(CLEVERTAP_KEY) { instance: CleverTapAPI? ->
            Log.i(TAG, "analytics.onIntegrationReady() called")
            if (instance != null) {
                instance.enablePersonalization()
                sCleverTapSegmentEnabled = true
                clevertap = instance
                clevertap!!.syncListener = object : SyncListener {
                    override fun profileDidInitialize(CleverTapID: String) {
                        Log.d("CT_PROFILE_INITIALIZED", CleverTapID)
                    }

                    override fun profileDataUpdated(updates: JSONObject) {
                        Log.d("CT_PROFILE_UPDATES", updates.toString())
                    }
                }
                cleverTapAttributionIdentifier()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    CleverTapAPI.createNotificationChannel(
                        applicationContext, "BRTesting", "YourChannelName",
                        "YourChannelDescription",
                        NotificationManager.IMPORTANCE_MAX, true
                    )
                }
            }
        }
        Analytics.setSingletonInstance(analytics)
    }


}