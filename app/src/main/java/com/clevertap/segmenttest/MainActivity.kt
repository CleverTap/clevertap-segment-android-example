package com.clevertap.segmenttest

import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import net.andreinc.mockneat.MockNeat
import android.text.TextUtils
import android.content.Intent
import android.os.SystemClock
import androidx.appcompat.widget.Toolbar
import com.clevertap.android.sdk.CTInboxStyleConfig
import com.segment.analytics.Traits
import com.segment.analytics.Properties
import java.util.*


class MainActivity : AppCompatActivity() {

    //private val clevertap by lazy { getCtCoreApi() }// cannot do so because its initially null and segment sdk takes time to asynchronously update application's global instance
    private var mLastInboxClickTime: Long = 0

    private  val ctTest = ClevertapSDKTest()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))

        findViewById<Button>(R.id.inboxButton).setOnClickListener { ctShowAppInbox() }
        findViewById<Button>(R.id.identifyButton).setOnClickListener { segmentIO_Alias_or_Identify(false) }
        findViewById<Button>(R.id.aliasButton).setOnClickListener { segmentIO_Alias_or_Identify(true) }
        findViewById<Button>(R.id.resetButton).setOnClickListener { segmentIO_AnalyticsReset() }
        findViewById<Button>(R.id.trackButton).setOnClickListener { segmentIO_AnalyticsTrack() }
        findViewById<Button>(R.id.screenButton).setOnClickListener { segmentIO_Screen() }
        handleIntent(intent)

        ctTest.setupTestActivity(this,llID = R.id.llCT, getCtCoreApi())
    }


    override fun onResume() {
        super.onResume()
        val clevertap = getCtCoreApi()
        log( "onResume() called. ctCore = $clevertap")
        val ctCore = clevertap ?: return
        val messageCount = ctCore.inboxMessageCount
        val unreadMessageCount = ctCore.inboxMessageUnreadCount
        findViewById<Button>(R.id.inboxButton).text = String.format(Locale.getDefault(), "Inbox: %d messages /%d unread", messageCount, unreadMessageCount)

        ctTest.forceUpdateCTCore(clevertap)
    }

    private fun segmentIO_AnalyticsReset() {
        getSegmentAnalyticsApi()?.reset()
        Toast.makeText(applicationContext, "reset() called ", Toast.LENGTH_LONG).show()
    }

    private fun segmentIO_AnalyticsTrack() {
        log("calling : segmentIO_AnalyticsTrack",context = this)

        val properties1 = Properties()
            .putValue("value", "testValue")
            .putValue("testDate", Date(System.currentTimeMillis()))

        val properties2 = Properties()
            .putValue("orderId", "123456")
            .putValue("revenue", 100)
            .putProducts(
                Properties.Product("id1", "sku1", 100.0),
                Properties.Product("id2", "sku2", 200.0)
            )

        getSegmentAnalyticsApi()?.track("testEvent", properties1)
        getSegmentAnalyticsApi()?.track("Order Completed", properties2)
    }


    fun sgGetIdAndTraits(): Pair<String, Traits> {
        val etEmail = findViewById<EditText>(R.id.eTEmail).text.toString()
        val eTId = findViewById<EditText>(R.id.eTId).text.toString()

        val finalEmail = if (!TextUtils.isEmpty(etEmail.trim { it <= ' ' })) etEmail else MockNeat.threadLocal().emails().get()
        val traits = Traits()
        traits.putEmail(finalEmail)
        traits.putName(MockNeat.threadLocal().names().full().get())
        traits.putGender(MockNeat.threadLocal().genders().get())
        traits.putPhone("+14155551234")
        traits["boolean"] = true
        traits["integer"] = 50
        traits["float"] = 1.5
        traits["long"] = 12345L
        traits["string"] = "hello"
        traits["stringInt"] = "1"
        traits["testStringArr"] = arrayListOf("one", "two", "three")

        val finalID: String = if (!TextUtils.isEmpty(eTId.trim { it <= ' ' })) eTId else MockNeat.threadLocal().intSeq().get().toString() //Integer.toString(Math.abs(mRandom.nextInt()));

        return finalID to traits
    }

    private fun segmentIO_Alias_or_Identify(isMergeProfile: Boolean) {
        log("segmentIO_Alias_or_Identify() called with: isMergeProfile = $isMergeProfile",context = this)

        val (id,traits) = sgGetIdAndTraits()
        log("id",id)
        log("traits",traits)
        if (isMergeProfile) getSegmentAnalyticsApi()?.alias(id) //similar to onUserLogin
        else getSegmentAnalyticsApi()?.identify(id, traits, null) // similar to pushProfile
    }

    private fun segmentIO_Screen(){
        log("segmentIO_Screen() called",context = this)
        getSegmentAnalyticsApi()?.screen("Home Screen")
    }


    private fun ctShowAppInbox() {
        log( "ctShowAppInbox() called")
        if (SystemClock.elapsedRealtime() - mLastInboxClickTime < 1000) return
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
        getCtCoreApi()?.showAppInbox(styleConfig) //With Tabs // orclevertap.showAppInbox();//Opens Activity with default style configs
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_VIEW == intent.action) {
            val data = intent.data ?: return
            log("INTENT_URI", data.toString())
            val scheme = data.scheme
            log("DEEP_LINK", scheme!!)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
}