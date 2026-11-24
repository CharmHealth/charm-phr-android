package com.charm.phr.mphr.util


import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.MPHRApplication
import com.charm.phr.mphr.R
import com.charm.phr.mphr.ui.components.MDClickToSelectEdittext
import com.charm.phr.mphr.ui.components.ProgressDialog
import com.charm.phr.mphr.ui.login.LoginActivity
import com.charm.phr.mphr.ui.passcode.PasscodeActivity
import com.charm.phr.mphr.ui.tourpage.TourPageActivity
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerAppCompatActivity
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.withSign
import kotlin.system.exitProcess

enum class AuthStatus {
    USER_ONLINE, USER_OFFLINE
}

abstract class MDBaseActivity : DaggerAppCompatActivity() {

    companion object {

        var isBackPressed = false
        var isPassCodeAllowed = true
        var hideOnScrollIsAllowed = true

        var exitTime: Long = 0
        var timeDifference: Long = 0

        var lastToastedStatus: AuthStatus? = null

    }

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var app: MPHRApplication

    private lateinit var mGesture: GestureDetector

    var currentFocusView: View? = null  //For input method manager

    private var lastUserBackPressedTime: Long = 0L

    val LTAG = "MDBaseActivity" //cause it base class

    //region life cycle methods

    //region lifecycle methods

    lateinit var progressDialog: ProgressDialog

    lateinit var inputManager: InputMethodManager

    val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        progressDialog = ProgressDialog(this)

        //region Handling Exception

        val mdExceptionHandler = MDExceptionHandler(this)
        /* mdExceptionHandler.setCrashDialogListener(this)*/
//        Thread.setDefaultUncaughtExceptionHandler(mdExceptionHandler)
        inputManager = this.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager

        //endregion

        enableEdgeToEdge()

        //region Gesture

        mGesture = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {

                if (distanceY.withSign(1) > 0 && hideOnScrollIsAllowed) {
                    try {
                        inputManager.hideSoftInputFromWindow(currentFocusView?.windowToken, 0)
                    } catch (e: NullPointerException) {
                        Log.d(LTAG, "$e")
                    }
                }

                // Log.d("MDBaseActivity ", "Gesture:onScroll distanceX=  $distanceX  distanceY= $distanceY")
                return super.onScroll(e1, e2, distanceX, distanceY)

            }
        })

        // endregion

        Log.d(LTAG, "OnCreate - $this")

    }


    override fun onBackPressed() {

        val imm = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)

        isBackPressed = true

        Log.d(LTAG, "OnBackPressed - $this")

        if (isTaskRoot) {

            if (lastUserBackPressedTime + TimeUnit.SECONDS.toMillis(ModelUtil.bufferTimeForBackPress) > System.currentTimeMillis()) {
                //When User pressed back button again with in 10 sec
                super.onBackPressed()

            } else UIUtil.showToast(baseContext, getString(R.string.press_once_again_to_exit), Toast.LENGTH_SHORT)

            lastUserBackPressedTime = System.currentTimeMillis()

        } else super.onBackPressed()

    }

    override fun onPause() {

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        clearReferences()
        if (isPassCodeAllowed) {

            exitTime = System.currentTimeMillis() / 1000

            pref.edit().putLong(ModelUtil.exitTimeInSeconds, exitTime).commit()

        } else pref.edit().putLong(ModelUtil.exitTimeInSeconds, 0).commit()

        Log.d(LTAG, "OnPause - $this")
        Log.d(LTAG, "OnPause :isPassCodeAllowed:$isPassCodeAllowed")

        super.onPause()

    }

    override fun onResume() {
        super.onResume()

        app.currentActivity = this



        Log.d(LTAG, "onResume - $this")
        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        //region passcode

        if (isPassCodeAllowed && this !is PasscodeActivity &&
            this !is LoginActivity && this !is TourPageActivity
        ) {

            val reEnterTime = System.currentTimeMillis() / 1000
            timeDifference = (exitTime.minus(reEnterTime)).unaryMinus()

            exitTime = pref.getLong(ModelUtil.exitTimeInSeconds, 0)

            if (exitTime != 0L && timeDifference !in 0..TimeUnit.SECONDS.toSeconds(10) && timeDifference >= 0) { // time difference should not be in range of 0 to 1800

                val passcode = pref.getString(ModelUtil.PASSCODE, null)

                if (passcode != null) {

                    val intent = Intent(applicationContext, PasscodeActivity::class.java)
                    intent.putExtra(ModelUtil.passCodeType, PasscodeActivity.checkPasscode)
                    intent.putExtra(ModelUtil.isFirst, true)
                    intent.putExtra(ModelUtil.blockBackFunction, true)
                    startActivity(intent)
                    Log.d(LTAG, " Passcode activity appears")

                }
            }

            Log.d(LTAG, "onResume reEnterTime = $reEnterTime , ExitTime $exitTime")
            Log.d(LTAG, "onResume timeDifference = $timeDifference")

        } else {

            exitTime = 0
            isPassCodeAllowed = true
        }

        //endregion

        //region isFreeUserData Synced

        authManager.authStatusLive.observe(this, object : Observer<AuthStatus> {
            override fun onChanged(it: AuthStatus) {
                if (it != lastToastedStatus) {

                    lastToastedStatus = it


                    // region snackbar

                    if (this@MDBaseActivity !is PasscodeActivity && this@MDBaseActivity !is TourPageActivity && this@MDBaseActivity !is LoginActivity) {

                        val str: String = if (it == AuthStatus.USER_ONLINE)
                            getString(R.string.connected_to_internet)
                        else
                            getString(R.string.internet_connection_error)

                        val snack = Snackbar.make(
                            findViewById(android.R.id.content),
                            str,
                            Snackbar.LENGTH_SHORT
                        )

                        if (it == AuthStatus.USER_ONLINE) snack.config(connected = true)
                        else snack.config(connected = false)

                        snack.show()

                    }

                    //endregion

                }

            }

        })

        //endregion


    }

    override fun onDestroy() {
        clearReferences()
        super.onDestroy()

        Log.d(LTAG, "onDestroy - $this")
    }

    //endregion

    //region other methods

    private fun Snackbar.config(connected: Boolean) {

        val params = this.view.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(0, 0, 0, 0)
        this.view.layoutParams = params

        if (connected)
            this.view.setBackgroundResource(R.color.onlineGreen)
        else
            this.view.setBackgroundResource(R.color.offlineBlack)

        /* Snackbar.make(view,
                 Html.fromHtml("<font color=\"#ffffff\">Tap to open</font>").show()*/

        ViewCompat.setElevation(this.view, 6f)
    }

    private fun checkAndStartBackgroundSync() {

//        val pref = PreferenceManager.getDefaultSharedPreferences(this)
//        val isFreeUserDataSynced = pref.getString(ModelUtil.IS_FREE_USER_INITIAL_DATA_SYNCED, null)
//        val isAnySyncServiceRunning = pref.getBoolean(ModelUtil.isAnySyncServiceRunning, false)
//
//        Log.d(LTAG, "isAnySyncServiceRunning:$isAnySyncServiceRunning")
//
//        if (isFreeUserDataSynced != ModelUtil.False && authManager.reachability &&
//            !isAnySyncServiceRunning && authManager.authToken != null) {
//
//            if (authManager.authStatusLive.value == AuthStatus.SIGNEDUP_ONLINE) {
//
//                try {
//                    val intent = Intent(this, BackgroundSyncService::class.java)
//                    startService(intent)
//
//                } catch (e: IllegalStateException) {
//                    Log.d(LTAG, "IllegalStateException while starting service")
//                }
//
//
//            }
//            else Log.d(LTAG, "checkAndStartBackgroundSync: NOT SIGNED_ONLINE")
//
//        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // isWindowFocused
        try {
            currentFocusView = this.currentFocus
            mGesture.onTouchEvent(ev)
        } catch (e: Exception) {
            Log.d(LTAG, "dispatchTouchEvent")
        }
        return super.dispatchTouchEvent(ev)
    }

    //endregion

    private fun clearReferences() {
        val currActivity: Activity? = app.currentActivity
        if (this == currActivity) app.currentActivity = null
    }

    fun showMdProgressDialog() {
        try {
            runOnUiThread {
                if (!progressDialog.isShowing) {
                    progressDialog.show()
                }
            }
        } catch (e: Exception) {
            Log.d("showMdProgressDialog", e.toString())
        }
    }


    fun showFocus(view: View, editText: EditText?, scrollView: ScrollView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            scrollView.scrollToDescendant(view)
        } else {
            scrollView.scrollToView(view)
        }
        focus(editText)
    }

    fun showFocus(view: View, scrollView: ScrollView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            scrollView.scrollToDescendant(view)
        } else {
            scrollView.scrollToView(view)
        }
        focus(view)
    }

    //Method used to scroll to the view
    fun ScrollView.scrollToView(view: View) {
        // Get deepChild Offset
        val childOffset = Point()
        getDeepChildOffset(this, view.parent, view, childOffset)
        // Scroll to child.
        this.smoothScrollTo(0, childOffset.y)
    }

    //We need to scroll to child in scrollview, but the child may not the direct child to scrollview.
    //So to get correct child position to scroll, we need to iterate through all of its parent views till the main parent.
    private fun getDeepChildOffset(mainParent: ViewGroup, parent: ViewParent, child: View, accumulatedOffset: Point) {
        val parentGroup = parent as ViewGroup
        accumulatedOffset.x += child.left
        accumulatedOffset.y += child.top
        if (parentGroup == mainParent) {
            return
        }
        getDeepChildOffset(mainParent, parentGroup.parent, parentGroup, accumulatedOffset)
    }

    private fun focus(view: View) {

        handler.postDelayed({
            view.requestFocus()
        }, ModelUtil.addErrorAnimationDelayDuration)
    }

    private fun focus(editText: EditText?) {

        handler.postDelayed({
            editText?.requestFocus()

            if (editText?.inputType != InputType.TYPE_NULL) {
                if (editText is MDClickToSelectEdittext) {
                    editText.performClick()
                } else {
                    inputManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
                }
            }

        }, ModelUtil.addErrorAnimationDelayDuration)
    }

}


interface ExceptionHandlerCallBack {

    fun showCrashDialog()
}

class MDExceptionHandler(private val presentActivity: Activity) : Thread.UncaughtExceptionHandler {

    private val LTAG = "MDExceptionHandler"

    private var delegateListener: ExceptionHandlerCallBack? = null

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        delegateListener?.showCrashDialog()

        restartApplication()
        Log.d(LTAG, "OnCreate - $this")

        /*val builder = AlertDialog.Builder(presentActivity)
        builder.setMessage(R.string.unfortunatelyAppStopped)
        builder.setPositiveButton(R.string.alertDialogTextOk) { dialog, which ->*/

    }

    private fun restartApplication() {

        val intent = Intent(presentActivity, TourPageActivity::class.java)
        intent.putExtra(ModelUtil.isAppCrashed, true)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TASK
                    or Intent.FLAG_ACTIVITY_NEW_TASK
        )

        val pendingIntent = PendingIntent.getActivity(
            presentActivity.baseContext,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = presentActivity.baseContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //Note:This alarm does not wake the device up;
        // if it goes off while the device is asleep, it will not be delivered until the next time the device wakes up.

        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent)
        presentActivity.finish()
        presentActivity.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_down)
        exitProcess(2)

    }

    /*val dialog: AlertDialog = builder.create()
    dialog.show()*/

    /* fun setCrashDialogListener(exceptionHandlerCallBack: ExceptionHandlerCallBack) {
     this.delegateListener = exceptionHandlerCallBack
 }*/

}

class InfiniteScrollViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    companion object {

        fun getNewInstance(parent: ViewGroup): InfiniteScrollViewHolder {
            return InfiniteScrollViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_progress_infinite_scroll, parent, false)
            )
        }
    }
}