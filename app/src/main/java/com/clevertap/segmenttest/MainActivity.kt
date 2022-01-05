package com.clevertap.segmenttest

import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CTInboxListener
import com.clevertap.android.sdk.CleverTapAPI
import android.widget.Button
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import net.andreinc.mockneat.MockNeat
import android.text.TextUtils
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.clevertap.android.sdk.CTInboxStyleConfig
import com.segment.analytics.Analytics
import com.segment.analytics.Traits
import com.segment.analytics.Properties
import java.lang.Runnable
import java.util.*



class MainActivity : AppCompatActivity() {
    var mRandom = Random()
    private var clevertap: CleverTapAPI? = null
    var inboxButton: Button? = null
    private var mLastInboxClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))

        inboxButton = findViewById(R.id.inboxButton)
        if (inboxButton != null) inboxButton!!.visibility = View.GONE

        if (CleverTapAPI.getDefaultInstance(applicationContext) != null) cleverTapIntegrationReady()
        else {
            segmentIO_ClevertapIntegrate()
        }
        val eTEmail = findViewById<EditText>(R.id.eTEmail)
        val eTId = findViewById<EditText>(R.id.eTId)
        findViewById<Button>(R.id.identifyButton).setOnClickListener { segmentIO_CT_PushProfile(eTId, eTEmail, false) }
        findViewById<Button>(R.id.aliasButton).setOnClickListener { segmentIO_CT_PushProfile(eTId, eTEmail, true) }
        findViewById<Button>(R.id.resetButton).setOnClickListener {segmentIO_AnalyticsReset() }

        findViewById<Button>(R.id.trackButton).setOnClickListener {
           segmentIO_AnalyticsTrack()
        }
        handleIntent(intent)
    }



    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun segmentIO_ClevertapIntegrate() {
        val CLEVERTAP_KEY = "CleverTap"
        Analytics.with(applicationContext).onIntegrationReady<CleverTapAPI>(CLEVERTAP_KEY) {
            Log.i(TAG, "analytics.onIntegrationReady() called")
            cleverTapIntegrationReady()
        }
    }
    private fun segmentIO_AnalyticsReset() {
        Analytics.with(applicationContext).reset()
        Toast.makeText(applicationContext, "reset() called ", Toast.LENGTH_LONG).show()
    }
    private fun segmentIO_AnalyticsTrack() {
        Toast.makeText(applicationContext, "track() called for custom event 'testEvent'.", Toast.LENGTH_LONG).show()
        Analytics.with(applicationContext).track("testEvent", Properties().putValue("value", "testValue").putValue("testDate", Date(System.currentTimeMillis())))
        val properties = Properties()
        properties
            .putValue("orderId", "123456")
            .putValue("revenue", 100)
            .putProducts(Properties.Product("id1", "sku1", 100.0), Properties.Product("id2", "sku2", 200.0))
        Analytics.with(applicationContext).track("Order Completed", properties)
    }
    private fun segmentIO_CT_PushProfile(eTId: EditText, eTEmail: EditText, isMergeProfile: Boolean) {
        // segment equivalent of clevertap's pushProfile() and onUserLogin() .consist of functions : alias(), identify and screen()
        val mock = MockNeat.threadLocal()
        var newUser = mock.intSeq().get().toString() //Integer.toString(Math.abs(mRandom.nextInt()));
        val userId = eTId.text.toString()
        if (!TextUtils.isEmpty(userId.trim { it <= ' ' })) newUser = userId
        val testArr = ArrayList<String>()
        testArr.add("one")
        testArr.add("two")
        testArr.add("three")
        val traits = Traits()
        val email = eTEmail.text.toString()
        if (!TextUtils.isEmpty(email.trim { it <= ' ' })) traits.putEmail(email)
        else traits.putEmail(mock.emails().get())

        traits.putName(mock.names().full().get())
        traits.putGender(mock.genders().get())
        traits.putPhone("+14155551234")
        traits["boolean"] = true
        traits["integer"] = 50
        traits["float"] = 1.5
        traits["long"] = 12345L
        traits["string"] = "hello"
        traits["stringInt"] = "1"
        traits["testStringArr"] = testArr
        if (isMergeProfile) {
            Analytics.with(applicationContext).alias(newUser)
            Toast.makeText(applicationContext, "alias() called with user id: $newUser.", Toast.LENGTH_LONG).show()
        } else {
            //Analytics.with(getApplicationContext()).reset();
            Toast.makeText(applicationContext, "identify() called with user id: $newUser.", Toast.LENGTH_LONG).show()
            Analytics.with(applicationContext).identify(newUser, traits, null)
        }
        Analytics.with(applicationContext).screen("Home Screen")
    }


    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_VIEW == intent.action) {
            val data = intent.data
            if (data != null) {
                Log.d("INTENT_URI", data.toString())
                handleDeepLink(data)
            }
        }
    }
    private fun handleDeepLink(data: Uri) {
        //To get scheme.
        val scheme = data.scheme
        Log.d("DEEP_LINK", scheme!!)
    }
    private fun cleverTapIntegrationReady() {
        if (clevertap == null) {
            clevertap = CleverTapAPI.getDefaultInstance(applicationContext)
        }
        clevertap!!.ctNotificationInboxListener = object :CTInboxListener{
            override fun inboxDidInitialize() {
                if (inboxButton == null) {
                    return
                }
                inboxButton!!.setOnClickListener(View.OnClickListener {
                    if (clevertap == null) {
                        return@OnClickListener
                    }
                    if (SystemClock.elapsedRealtime() - mLastInboxClickTime < 1000) {
                        return@OnClickListener
                    }
                    mLastInboxClickTime = SystemClock.elapsedRealtime()
                    val tabs = ArrayList<String>()
                    tabs.add("Promotions")
                    tabs.add("Offers")
                    tabs.add("Will Not Show") //Anything after the first 2 will be ignored
                    val styleConfig = CTInboxStyleConfig()
                    styleConfig.tabs = tabs //Do not use this if you don't want to use tabs
                    styleConfig.tabBackgroundColor = "#FF0000"
                    styleConfig.selectedTabIndicatorColor = "#0000FF"
                    styleConfig.selectedTabColor = "#0000FF"
                    styleConfig.unselectedTabColor = "#FFFFFF"
                    styleConfig.backButtonColor = "#FF0000"
                    styleConfig.navBarTitleColor = "#FF0000"
                    styleConfig.navBarTitle = "MY INBOX"
                    styleConfig.navBarColor = "#FFFFFF"
                    styleConfig.inboxBackgroundColor = "#ADD8E6"
                    clevertap!!.showAppInbox(styleConfig) //With Tabs
                    //clevertap.showAppInbox();//Opens Activity with default style configs
                })
                updateInboxButton()
            }

            override fun inboxMessagesDidUpdate() {
                updateInboxButton()
            }

        }
        clevertap!!.initializeInbox()
    }
    private fun updateInboxButton() {
        if (clevertap == null) {
            return
        }
        runOnUiThread(Runnable {
            if (inboxButton == null) {
                return@Runnable
            }
            val messageCount = clevertap!!.inboxMessageCount
            val unreadMessageCount = clevertap!!.inboxMessageUnreadCount
            inboxButton!!.text = String.format(Locale.getDefault(), "Inbox: %d messages /%d unread", messageCount, unreadMessageCount)
            inboxButton!!.visibility = View.VISIBLE
        })
    }

    companion object {
        private val TAG = String.format("%s.%s", "CLEVERTAP", MainActivity::class.java.name)
    }
}