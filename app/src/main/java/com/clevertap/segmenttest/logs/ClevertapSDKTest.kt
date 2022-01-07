package com.clevertap.segmenttest.logs

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.core.widget.NestedScrollView
//import com.clevertap.android.geofence.CTGeofenceAPI
//import com.clevertap.android.geofence.CTGeofenceSettings
//import com.clevertap.android.geofence.interfaces.CTGeofenceEventsListener
import com.clevertap.android.sdk.ActivityLifecycleCallback
import com.clevertap.android.sdk.CTInboxStyleConfig
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.pushnotification.CTPushNotificationListener
import com.clevertap.android.sdk.pushnotification.amp.CTPushAmpListener
import java.util.*
import kotlin.collections.HashMap

class ClevertapSDKTest() {
    private var ctCoreApi: CleverTapAPI? = null

    private var testActivity: AppCompatActivity? = null

    private var appCtx: Context? = null

    private val PERMISSIONS_REQUEST_CODE = 34
    private val idActionsMap = linkedMapOf(
        "01. test Event Push Amplification" to {
            ctCoreApi?.pushEvent("testEventPushAmp")
        },
        "02. Record an event with properties" to {
            val prodViewedAction = mapOf(
                "Product Name" to "Casio Chronograph Watch",
                "Category" to "Mens Accessories", "Price" to 59.99, "Date" to Date()
            )
            ctCoreApi?.pushEvent("Product viewed", prodViewedAction)
            //cleverTapAPI?.pushEvent("video-inapp")
            //cleverTapAPI?.pushEvent("video-inbox")
            ctCoreApi?.pushEvent("caurousel-inapp")
            ctCoreApi?.pushEvent("icon-inbox")
        },
        "03. Record a Charged event" to {
            val chargeDetails = hashMapOf<String, Any>("Amount" to 300, "Payment Mode" to "Credit card", "Charged ID" to 24052013)
            val item1 = hashMapOf<String, Any>("Product category" to "books", "Book name" to "The Millionaire next door", "Quantity" to 1)
            val item2 = hashMapOf<String, Any>("Product category" to "books", "Book name" to "Achieving inner zen", "Quantity" to 1)
            val item3 = hashMapOf<String, Any>("Product category" to "books", "Book name" to "Chuck it, let's do it", "Quantity" to 5)
            val items = arrayListOf<HashMap<String, Any>>()
            items.apply { add(item1); add(item2); add(item3) }
            ctCoreApi?.pushChargedEvent(chargeDetails, items)
        },
        "04. Record Screen" to {
            ctCoreApi?.recordScreen("Cart Screen Viewed")
        },
        "05. Profile Update Via Push Profile(Signup)" to {
            val profileUpdate = HashMap<String, Any>()
            profileUpdate["Name"] = "User Name" // String
            profileUpdate["Email"] = "User@gmail.com" // Email address of the user
            profileUpdate["Phone"] = "+14155551234" // Phone (with the country code, starting with +)
            profileUpdate["Gender"] = "M" // Can be either M or F
            profileUpdate["Employed"] = "Y" // Can be either Y or N
            profileUpdate["Education"] = "Graduate" // Can be either Graduate, College or School
            profileUpdate["Married"] = "Y" // Can be either Y or N
            profileUpdate["DOB"] = Date() // Date of Birth. Set the Date object to the appropriate value first
            profileUpdate["Age"] = 28 // Not required if DOB is set
            profileUpdate["MSG-email"] = false // Disable email notifications
            profileUpdate["MSG-push"] = true // Enable push notifications
            profileUpdate["MSG-sms"] = false // Disable SMS notifications

            profileUpdate["MyStuffList"] = arrayListOf("bag", "shoes") //ArrayList of Strings
            profileUpdate["MyStuffArray"] = arrayOf("Jeans", "Perfume")

            ctCoreApi?.pushProfile(profileUpdate)
        },
        "06. Profile Update Via Push Profile (Update Single-Value User Profile Properties)" to {
            val profileUpdate = HashMap<String, Any>()
            profileUpdate["Name"] = "Updated User Name" // String
            profileUpdate["Email"] = "UpdatedUser@gmail.com" // Email address of the user
            profileUpdate["Gender"] = "F" // Can be either M or F
            profileUpdate["Employed"] = "N" // Can be either Y or N
            profileUpdate["Education"] = "College" // Can be either Graduate, College or School
            profileUpdate["Married"] = "N" // Can be either Y or N
            profileUpdate["MSG-push"] = false // Disable push notifications

            ctCoreApi?.pushProfile(profileUpdate)
        },
        "07. Profile Update Via  Push Profile (Add new Single-Value User Profile Properties)" to {
            val profileUpdate = mapOf("Customer Type" to "Silver", "Preferred Language" to "English")
            ctCoreApi?.pushProfile(profileUpdate)
        },
        "08. Profile Update Via Push Profile (Remove Single-Value User Profile Properties)" to {
            ctCoreApi?.removeValueForKey("Customer Type")
        },
        "09. Profile Update Via  setMultiValuesForKey (Update multi-value User Profile Properties)" to {
            ctCoreApi?.setMultiValuesForKey("MyStuffList", arrayListOf("Updated Bag", "Updated Shoes"))
        },
        "10. Profile Update Via  addMultiValueForKey (Add multi-value User Profile Properties)" to {
            ctCoreApi?.addMultiValueForKey("MyStuffList", "Coat")
            ctCoreApi?.addMultiValuesForKey("MyStuffList", arrayListOf("Socks", "Scarf"))
        },
        "11. Profile Update Via  removeMultiValueForKey (Remove multi-value User Profile Properties)" to {
            ctCoreApi?.removeMultiValueForKey("MyStuffList", "Coat") // or
            ctCoreApi?.removeMultiValuesForKey("MyStuffList", arrayListOf("Socks", "Scarf"))
        },
        "12. Profile Update Via incrementValue" to {
            ctCoreApi?.incrementValue("score", 50)
        },
        "13. Profile Update Via decrementValue" to {
            ctCoreApi?.decrementValue("score", 30)
        },
        "14. Profile Update Via location" to {
            ctCoreApi?.location = ctCoreApi?.location
        },
        "15. Get profile info(like name, id, identifiers , etc)" to {
            logIt("Profile Name = ${ctCoreApi?.getProperty("Name")}")
            logIt("Profile CleverTapId = ${ctCoreApi?.cleverTapID}")
            logIt("Profile CleverTap AttributionIdentifier = ${ctCoreApi?.cleverTapAttributionIdentifier}")
        },
        "16. Profile Update Via onUserLogin(login)" to {
            val newProfile = HashMap<String, Any>()
            val n = (0..10_000).random()
            val p = (10_000..99_999).random()
            newProfile["Name"] = "Don Joe $n}" // String
            newProfile["Email"] = "donjoe$n@gmail.com" // Email address of the user
            newProfile["Phone"] = "+141566$p" // Phone (with the country code, starting with +)
            // add any other key value pairs.....
            ctCoreApi?.onUserLogin(newProfile)
        },
        "17. Open Inbox" to {
            CTInboxStyleConfig().apply {
                tabs = arrayListOf("Promotions", "Offers", "Others")//Anything after the first 2 will be ignored. //Do not use this if you don't want to use tabs
                tabBackgroundColor = "#FF0000"
                selectedTabIndicatorColor = "#0000FF"
                selectedTabColor = "#000000"
                unselectedTabColor = "#FFFFFF"
                backButtonColor = "#FF0000"
                navBarTitleColor = "#FF0000"
                navBarTitle = "MY INBOX"
                navBarColor = "#FFFFFF"
                inboxBackgroundColor = "#00FF00"
                firstTabTitle = "First Tab"
                ctCoreApi?.showAppInbox(this) //Opens activity With Tabs
            }
        },
        "18. show total inbox message count" to {
            logIt("Total inbox message count = ${ctCoreApi?.inboxMessageCount}")
        },
        "19. show unread inbox message count" to {
            logIt("Unread inbox message count = ${ctCoreApi?.inboxMessageUnreadCount}")
        },
        "20. All inbox messages" to {
            ctCoreApi?.allInboxMessages?.forEach {
                logIt("All inbox messages ID = ${it.messageId}")
            }
        },
        "21. All unread inbox messages" to {
            ctCoreApi?.unreadInboxMessages?.forEach {
                logIt("All unread inbox messages ID = ${it.messageId}")
            }
        },
        "22. Get message object" to {
            val firstMessageId = ctCoreApi?.allInboxMessages?.firstOrNull()?.messageId
            firstMessageId?.also {
                val inboxMessageForId = ctCoreApi?.getInboxMessageForId(it)
                logIt("inboxMessage For Id $it = ${inboxMessageForId?.data}")
            } ?: logIt("inboxMessage Id is null")
        },
        "23. Deleted inboxMessage For msg Id" to {
            val firstMessageId = ctCoreApi?.allInboxMessages?.firstOrNull()?.messageId
            //Delete message object belonging to the given message id only. Message id should be a String
            firstMessageId?.also {
                ctCoreApi?.deleteInboxMessage(it)
                logIt("Deleted inboxMessage For Id = $it")
            } ?: logIt("inboxMessage Id is null")
        },
        "24. Deleted inboxMessage For msg object" to {
            val firstMessage = ctCoreApi?.allInboxMessages?.firstOrNull()
            //Delete message object belonging to the given CTInboxMessage.
            firstMessage?.also {
                ctCoreApi?.deleteInboxMessage(it)
                logIt("Deleted inboxMessage = ${it.messageId}")
            } ?: logIt("inboxMessage is null")
        },
        "25. Mark inbox message as read(by id)" to {
            val firstMessageId = ctCoreApi?.unreadInboxMessages?.firstOrNull()?.messageId
            //Mark Message as Read. Message id should be a String
            firstMessageId?.also {
                ctCoreApi?.markReadInboxMessage(it)
                logIt("Marked Message as Read For Id = $it")
            } ?: logIt("inboxMessage Id is null")
        },
        "26. Mark inbox message as read(by id) " to {
            val firstMessage = ctCoreApi?.unreadInboxMessages?.firstOrNull()
            //Mark message as Read. Message should object of CTInboxMessage
            firstMessage?.also {
                ctCoreApi?.markReadInboxMessage(it)
                logIt("Marked Message as Read = ${it.messageId}")
            } ?: logIt("inboxMessage is null")
        },
        "27. Raise notification viewed event for inbox message by id " to {
            val firstMessageId = ctCoreApi?.allInboxMessages?.firstOrNull()?.messageId
            //Raise Notification Viewed event for Inbox Message. Message id should be a String
            firstMessageId?.also {
                ctCoreApi?.pushInboxNotificationViewedEvent(it)
                logIt("Raised Notification Viewed event For Id = $it")
            } ?: logIt("inboxMessage Id is null")
        },
        "28. Raise notification clicked event for inbox message by id " to {
            val firstMessageId = ctCoreApi?.allInboxMessages?.firstOrNull()?.messageId
            //Raise Notification Clicked event for Inbox Message. Message id should be a String
            firstMessageId?.also {
                ctCoreApi?.pushInboxNotificationClickedEvent(it)
                logIt("Raised Notification Clicked event For Id = $it")
            } ?: logIt("inboxMessage Id is null")
        },
        "29. Get DisplayUnit by unit id" to {
            val displayUnitID = ctCoreApi?.allDisplayUnits?.firstOrNull()?.unitID
            //Get DisplayUnit by unit id. unit id should be a String
            displayUnitID?.also {
                val displayUnitForId = ctCoreApi?.getDisplayUnitForId(it)
                logIt("DisplayUnit for Id $it = $displayUnitForId")
            } ?: logIt("DisplayUnit Id is null")
        },
        "30. get all display units" to {
            logIt("All Display Units = ${ctCoreApi?.allDisplayUnits}") // get all display units
        },
        "31. Raise Notification Viewed event for DisplayUnit id" to {
            val displayUnitID = ctCoreApi?.allDisplayUnits?.firstOrNull()?.unitID
            //Raise Notification Viewed event for DisplayUnit. Message id should be a String
            displayUnitID?.also {
                ctCoreApi?.pushDisplayUnitViewedEventForID(it)
                logIt("Raised Notification Viewed event For DisplayUnit Id = $it")
            } ?: logIt("DisplayUnit Id is null")
        },
        "32. Raise Notification Clicked event for DisplayUnit id" to {
            val displayUnitID = ctCoreApi?.allDisplayUnits?.firstOrNull()?.unitID
            //Raise Notification Clicked event for DisplayUnit. Message id should be a String
            displayUnitID?.also {
                ctCoreApi?.pushDisplayUnitClickedEventForID(it)
                logIt("Raised Notification Clicked event For DisplayUnit Id = $it")
            } ?: logIt("DisplayUnit Id is null")
        },
        "33. set product config defaults" to {
            val hashMap = hashMapOf<String, Any>(
                "text color" to "red", "msg count" to 100, "price" to 100.50, "is shown" to true,
                "json" to """{"key":"val","key2":50}"""
            )
            ctCoreApi?.productConfig()?.setDefaults(hashMap)
        },
        "34. productConfig fetch()" to {
            ctCoreApi?.productConfig()?.fetch()
        },
        "35. productConfig activate()" to {
            ctCoreApi?.productConfig()?.activate()
        },
        "36. productConfig fetchAndActivate()" to {
            ctCoreApi?.productConfig()?.fetchAndActivate()
        },
        "37. productConfig reset()" to {
            ctCoreApi?.productConfig()?.reset()
        },
        "38. productConfig fetch(< min interval 60 >)" to {
            ctCoreApi?.productConfig()?.fetch(60)
        },
        "39. productConfig getting all values" to {
            //get all product config values
            ctCoreApi?.productConfig()?.apply {
                logIt("Product Config text color val in string : ${getString("text color")}")
                logIt("Product Config is shown val in boolean : ${getBoolean("is shown")}")
                logIt("Product Config msg count val in long : ${getLong("msg count")}")
                logIt("Product Config price val in double : ${getDouble("price")}")
                logIt("Product Config json val in string : ${getString("json")}")
            }
        },
        "40. productConfig lastFetchTimeStampInMillis" to {
            logIt("Product Config lastFetchTimeStampInMillis = ${ctCoreApi?.productConfig()?.lastFetchTimeStampInMillis}")
        },
        "41. Feature Flags `is shown`" to {
            logIt("Feature Flags is shown val in boolean = ${ctCoreApi?.featureFlag()?.get("is shown", true)}")
        },
        "42. user cleverTapAttributionIdentifier" to {
            logIt("CleverTapAttribution Identifier = ${ctCoreApi?.cleverTapAttributionIdentifier}")
        },
        "43. getCleverTapID" to {
            ctCoreApi?.getCleverTapID {
                logIt("CleverTap DeviceID from Application class= $it, thread=${if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) "mainthread" else "bg thread"}")
            }
        },
        "44. Push Templates: Send Basic Push " to {
            ctCoreApi?.pushEvent("Send Basic Push")
        },
        "45. Push Templates: Send Carousel Push" to {
            ctCoreApi?.pushEvent("Send Carousel Push")
        },
        "46. Push Templates: Send Manual Carousel Push " to {
            ctCoreApi?.pushEvent("Send Manual Carousel Push")
        },
        "47. Push Templates: Send Filmstrip Carousel Push " to {
            ctCoreApi?.pushEvent("Send Filmstrip Carousel Push")
        },
        "48. Push Templates: Send Rating Push " to {
            ctCoreApi?.pushEvent("Send Rating Push")
        },
        "49. Push Templates: Send Product Display Notification " to {
            ctCoreApi?.pushEvent("Send Product Display Notification")
        },
        "50. Push Templates: Send Linear Product Display Push " to {
            ctCoreApi?.pushEvent("Send Linear Product Display Push")
        },
        "51. Push Templates: Send CTA Notification " to {
            ctCoreApi?.pushEvent("Send CTA Notification")
        },
        "52. Push Templates: Send Zero Bezel Notification " to {
            ctCoreApi?.pushEvent("Send Zero Bezel Notification")
        },
        "53. Push Templates: Send Zero Bezel Text Only Notification " to {
            ctCoreApi?.pushEvent("Send Zero Bezel Text Only Notification")
        },
        "54. Push Templates: Send Timer Notification " to {
            ctCoreApi?.pushEvent("Send Timer Notification")
        },
        "55. Push Templates: Send Input Box Notification " to {
            ctCoreApi?.pushEvent("Send Input Box Notification")
        },
        "56. Push Templates:  Send Input Box Reply with Event Notification" to {
            ctCoreApi?.pushEvent("Send Input Box Reply with Event Notification")
        },
        "57. Push Templates:  Send Input Box Reply with Auto Open Notification" to {
            ctCoreApi?.pushEvent("Send Input Box Reply with Auto Open Notification")
        },
        "58. Push Templates:  Send Input Box Remind Notification DOC FALSE" to {
            ctCoreApi?.pushEvent("Send Input Box Remind Notification DOC FALSE")
        },
        "59. Push Templates:  Send Input Box CTA DOC true" to {
            ctCoreApi?.pushEvent("Send Input Box CTA DOC true")
        },
        "60. Push Templates:  Send Input Box CTA DOC false" to {
            ctCoreApi?.pushEvent("Send Input Box CTA DOC false")
        },
        "61. Push Templates:  Send Input Box Reminder DOC true" to {
            ctCoreApi?.pushEvent("Send Input Box Reminder DOC true")
        },
        "62. Push Templates: Send Input Box Reminder DOC false " to {
            ctCoreApi?.pushEvent("Send Input Box Reminder DOC false")
        },
        "63. open webview" to {
            // startActivity(Intent(activity, WebViewActivity::class.java))
        },
        "64. init Geofence API" to {
            when {
                // proceed only if cleverTap instance is not null
                ctCoreApi == null -> logIt("cleverTapInstance is null")
                !checkPermissions() -> requestPermissions()
                else -> initCTGeofenceApi(false)
            }
        },
        "65. geofence trigger location" to { //
            try {
                initCTGeofenceApi(true)
            }
            catch (e: IllegalStateException) {
                // geofence not initialized
                e.printStackTrace()
                // init geofence
                initCTGeofenceApi(false)
            }
        },
        "66. deactivate geofence" to {
            initCTGeofenceApi(false, true)
        },
    )


    fun initApplicationBeforeOnCreate(application: Application) {
        log("ctInitLogging: ")
        if (/*BuildConfig.DEBUG*/ true) CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE)
        else CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.OFF)

        //TemplateRenderer.debugLevel = 3;
        //CleverTapAPI.setNotificationHandler(PushTemplateNotificationHandler() as NotificationHandler);
        //application.registerActivityLifecycleCallbacks(
        //    object : Application.ActivityLifecycleCallbacks {
        //        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        //        override fun onActivityStarted(activity: Activity) {}
        //        override fun onActivityResumed(activity: Activity) {}
        //        override fun onActivityPaused(activity: Activity) {}
        //        override fun onActivityStopped(activity: Activity) {}
        //        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        //        override fun onActivityDestroyed(activity: Activity) {}
        //    }
        //)

        log("ctAttachLifeCycleListener: ")
        ActivityLifecycleCallback.register(application)

    }

    fun initApplicationAfterOnCreate(application: Application) {
        log("ctInitGlobalInstance: ")

        appCtx = application.applicationContext


        //ProviderInstaller.installIfNeededAsync(this, object : ProviderInstaller.ProviderInstallListener {
        //    override fun onProviderInstalled() {}
        //    override fun onProviderInstallFailed(i: Int, intent: Intent?) { Log.i("ProviderInstaller", "Provider install failed ($i) : SSL Problems may occurs") }
        //})

        //val defaultInstance = CleverTapAPI.getDefaultInstance(this)
        //defaultInstance?.syncListener = object : SyncListener {
        //    override fun profileDataUpdated(updates: JSONObject?) {no op }
        //    override fun profileDidInitialize(CleverTapID: String?) { println("CleverTap DeviceID from Application class= $CleverTapID") }
        //}


        ctCoreApi = CleverTapAPI.getDefaultInstance(appCtx)

        log("ctInBoxInitialise: ")
        ctCoreApi?.initializeInbox()


        log("called ctNotifCreateChannels: ")
        val importance = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) NotificationManager.IMPORTANCE_MAX else 5
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CleverTapAPI.createNotificationChannelGroup(appCtx, "id_my_word_group", "MyWord")
            CleverTapAPI.createNotificationChannel(appCtx, "id_got", "GOT", "GOT channel notifications description", importance, true, null)

            CleverTapAPI.deleteNotificationChannel(appCtx, "testChannel")
            CleverTapAPI.deleteNotificationChannelGroup(appCtx, "testGroup");
        }
        CleverTapAPI.createNotificationChannel(appCtx, "c_general_id", "c_general_name", "", importance, true)
        CleverTapAPI.createNotificationChannel(appCtx, "GSTTesting", "GSTTesting", "", importance, true)
        CleverTapAPI.createNotificationChannel(appCtx, "BRTesting", "Core", "Core notifications", NotificationManager.IMPORTANCE_MAX, true)
        CleverTapAPI.createNotificationChannel(appCtx, "PTTesting", "Push templates", "All push templates", NotificationManager.IMPORTANCE_MAX, true)


        log("called ctNotifAttachListeners: ")
        ctCoreApi?.ctPushNotificationListener = CTPushNotificationListener { receivedNotifInfo ->
            log("received notification: $receivedNotifInfo")

            //hack  controlling in app notifications from status bar notifications
            if (receivedNotifInfo["discard"] == 1) ctCoreApi?.discardInAppNotifications()
            if (receivedNotifInfo["resume"] == 1) ctCoreApi?.resumeInAppNotifications()
            if (receivedNotifInfo["suspend"] == 1) ctCoreApi?.suspendInAppNotifications()
        }

        log("called ctNotifPushAmpInitListener: ")
        ctCoreApi?.ctPushAmpListener = CTPushAmpListener { extras: Bundle? ->
            CleverTapAPI.processPushNotification(appCtx, extras)
            appCtx.showNotif(title = "push amplification notification", channelId = "forced_notif_channel")
        }
    }


    fun setupTestActivity(activity: AppCompatActivity, llID: Int) {
        testActivity = activity
        appCtx = activity.applicationContext
        if (ctCoreApi == null) ctCoreApi = CleverTapAPI.getDefaultInstance(appCtx)

        activity.findViewById<LinearLayout>(llID).addView(getInitView())
    }

    private fun getInitView(): View {
        val appCtx = this.appCtx ?: error("appCtx is null")
        val nsv = NestedScrollView(appCtx)
        val grid = GridLayout(appCtx).also {
            it.columnCount = 3
            it.useDefaultMargins = true
            it.orientation = GridLayout.HORIZONTAL
        }

        idActionsMap.forEach { content: Map.Entry<String, () -> Any?> ->
            val textView = TextView(appCtx)
            textView.background = ContextCompat.getDrawable(appCtx, android.R.drawable.editbox_dropdown_light_frame)
            textView.setPadding(8)
            textView.maxWidth = 120.dpValue
            textView.minWidth = 120.dpValue
            textView.gravity = Gravity.CENTER_HORIZONTAL
            textView.setOnClickListener {
                logIt("clicked:${content.key}")
                content.value.invoke()
            }
            textView.text = content.key
            textView.minLines = 8

            grid.addView(textView)

        }

        nsv.addView(grid)
        return nsv


    }


    private fun checkPermissions(): Boolean {
        val testActivity = this.testActivity ?: return false

        val applicationContext = testActivity.applicationContext ?: return false
        val fineLocationPermissionState = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)

        val backgroundLocationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        else PackageManager.PERMISSION_GRANTED

        return fineLocationPermissionState == PackageManager.PERMISSION_GRANTED && backgroundLocationPermissionState == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("InlinedApi")
    private fun requestPermissions() {
        val testActivity = this.testActivity ?: return
        val applicationContext = testActivity.applicationContext ?: return

        val permissionAccessFineLocationApproved = (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)

        val backgroundLocationPermissionApproved = (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)

        val shouldProvideRationale = permissionAccessFineLocationApproved && backgroundLocationPermissionApproved

        if (shouldProvideRationale) {
            // Provide an additional rationale to the user. This would happen if the user denied the request previously, but didn't check the "Don't ask again" checkbox.
            Toast.makeText(this.appCtx, "pretty please?", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(testActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION), PERMISSIONS_REQUEST_CODE)
        } else {
            // Request permission. It's possible this can be auto answered if device policy sets the permission in a given state or the user denied the permission  previously and checked "Never ask again".
            ActivityCompat.requestPermissions(testActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION), PERMISSIONS_REQUEST_CODE)
        }
    }

    private fun initCTGeofenceApi(triggerUpdates: Boolean, deactivate: Boolean = false) {
        /*
        val testActivity = this.testActivity ?: return

        val cleverTapInstance = ctCoreApi ?: return
        val context = testActivity.applicationContext ?: return

        CTGeofenceAPI.getInstance(context).apply {
            init(
                CTGeofenceSettings.Builder()
                    .enableBackgroundLocationUpdates(true)
                    .setLogLevel(com.clevertap.android.geofence.Logger.DEBUG)
                    .setLocationAccuracy(CTGeofenceSettings.ACCURACY_HIGH)
                    .setLocationFetchMode(CTGeofenceSettings.FETCH_CURRENT_LOCATION_PERIODIC)
                    .setGeofenceMonitoringCount(99)
                    .setInterval(3600000) // 1 hour
                    .setFastestInterval(1800000) // 30 minutes
                    .setSmallestDisplacement(1000f) // 1 km
                    .setGeofenceNotificationResponsiveness(300000) // 5 minute
                    .build(), cleverTapInstance
            )
            setOnGeofenceApiInitializedListener {
                Toast.makeText(context, "Geofence API initialized", Toast.LENGTH_SHORT).show()
            }
            setCtGeofenceEventsListener(object : CTGeofenceEventsListener {
                override fun onGeofenceEnteredEvent(jsonObject: JSONObject) {
                    Toast.makeText(context, "Geofence Entered", Toast.LENGTH_SHORT).show()
                }

                override fun onGeofenceExitedEvent(jsonObject: JSONObject) {
                    Toast.makeText(context, "Geofence Exited", Toast.LENGTH_SHORT).show()
                }
            })
            setCtLocationUpdatesListener { Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show() }
            if (triggerUpdates) triggerLocation()
            if (deactivate) deactivate()
        }
        */

    }


    private fun logIt(value: Any) {
        Toast.makeText(appCtx, value.toString(), Toast.LENGTH_SHORT).show()
        Log.e("CUSTOM_TAG", value.toString())
        System.out.println(value)
    }


    private val Number.dpValue get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()

    private fun Context?.showNotif(
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


    fun log(key: String, value: Any? = null, tag: String = "CUSTOM_LOGS") {
        if (value == null) Log.e(tag, key)
        else Log.e(tag, "$key:$value ")
    }
}