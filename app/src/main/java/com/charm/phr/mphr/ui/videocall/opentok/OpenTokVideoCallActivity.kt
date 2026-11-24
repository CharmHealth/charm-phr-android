package com.charm.phr.mphr.ui.videocall.opentok


import com.charm.phr.mphr.util.MDBaseActivity
/*
import com.opentok.android.*
*/

class OpenTokVideoCallActivity : MDBaseActivity() {


//    UIUtil.applyViewInsertForToolbar(binding.aboutToolbar)
}


/*
class OpenTokVideoCallActivity : MDBaseActivity(), Session.SessionListener, PublisherKit.PublisherListener, SubscriberKit.SubscriberListener {
    // Suppressing this warning. mWebServiceCoordinator will get GarbageCollected if it is local.
    private var mSession: Session? = null
    private var mPublisher: Publisher? = null
    private var mSubscriber: Subscriber? = null

    lateinit var openTokVideoCallViewModel: OpenTokVideoCallViewModel


    private var isPubisherAttached = false

    private var connectionsBeforeUs = 0

    val runnableForCloseTools = Runnable {
        otToolsLayout.animate().alpha(0f).withEndAction { otToolsLayout.visibility = View.GONE }.setDuration(400).start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_tok_video_call)

        openTokVideoCallViewModel = ViewModelProvider(this).get(OpenTokVideoCallViewModel::class.java)

        openTokVideoCallViewModel.apiKey = intent.getStringExtra(ModelUtil.otAPIKeyIntent) ?: ""
        openTokVideoCallViewModel.sessionId = intent.getStringExtra(ModelUtil.otSessionIdIntent) ?: ""
        openTokVideoCallViewModel.token = intent.getStringExtra(ModelUtil.otTokenIntent) ?: ""

        initializeSession()


        otCamImg.setOnClickListener {
            mPublisher?.cycleCamera()
            showTools()
        }

        otSubscriberContainer.setOnClickListener {
            showTools()
        }

        otCutCallFab.setOnClickListener {
            onBackPressed()
        }

        otMicImg.setOnClickListener {
            if (mPublisher != null) {
                showTools()
                 mPublisher!!.publishAudio = !mPublisher!!.publishAudio

                if (mPublisher!!.publishAudio){
                    otMicImg.setImageResource(R.drawable.ic_mic_on)
                }
                else{
                    otMicImg.setImageResource(R.drawable.ic_mic_off)
                }
            }

        }

        otVideoImg.setOnClickListener {
            if (mPublisher != null) {

                showTools()
                mPublisher!!.publishVideo = !mPublisher!!.publishVideo

                if (mPublisher!!.publishVideo){
                    otVideoImg.setImageResource(R.drawable.ic_videocam_on)
                }
                else{
                    otVideoImg.setImageResource(R.drawable.ic_videocam_off)
                }
            }
        }

       */
/* mPublisher?.setCameraListener(object : Publisher.CameraListener{
            override fun onCameraError(p0: Publisher?, p1: OpentokError?) {

            }

            override fun onCameraChanged(p0: Publisher?, p1: Int) {
                p0?.
            }
        })*//*

    }

    */
/* Activity lifecycle methods *//*

    override fun onPause() {
        super.onPause()
        mSession?.onPause()


        if (isFinishing()) {
            disconnectSession();
        }

    }

    override fun onResume() {
        super.onResume()
        mSession?.onResume()

    }

    override fun onBackPressed() {
        try {
            super.onBackPressed()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        catch (e:Exception){
            Log.d(LOG_TAG , e.toString())
        }
     }

    private fun initializeSession() {
        otPublisherContainer.visibility = View.GONE
        otProgress.visibility = View.VISIBLE
        mSession = Session.Builder(this, openTokVideoCallViewModel.apiKey, openTokVideoCallViewModel.sessionId).build()
        mSession?.setSessionListener(this)
        mSession?.connect(openTokVideoCallViewModel.token)
    }

    override fun onDestroy() {
        disconnectSession()
        super.onDestroy()
    }


    */
/* Session Listener methods *//*

    override fun onConnected(session: Session) {


        Log.d(LOG_TAG, "onConnected: Connected to session: " + session.getSessionId())
        if (!isPubisherAttached) {
            // initialize Publisher and set this object to listen to Publisher events
            mPublisher = Publisher.Builder(this).build()
            mPublisher?.setPublisherListener(this)

            // set publisher video style to fill view
            mPublisher?.getRenderer()?.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                    BaseVideoRenderer.STYLE_VIDEO_FILL)
            otPublisherContainer.addView(mPublisher?.getView())
            if (mPublisher?.getView() is GLSurfaceView) {
                (mPublisher?.getView() as GLSurfaceView).setZOrderOnTop(true)
            }

            mSession?.publish(mPublisher)

            if (mPublisher != null) {
                if (mPublisher!!.publishAudio) {
                    otMicImg.setImageResource(R.drawable.ic_mic_on)
                } else {
                    otMicImg.setImageResource(R.drawable.ic_mic_off)
                }

                if (mPublisher!!.publishVideo) {
                    otVideoImg.setImageResource(R.drawable.ic_videocam_on)
                } else {
                    otVideoImg.setImageResource(R.drawable.ic_videocam_off)
                }
            }
            isPubisherAttached = true
        }


        if (session.connection?.connectionId !== mSession?.connection?.connectionId &&
                session.connection?.creationTime?.before( mSession?.connection?.creationTime) == true) {
            connectionsBeforeUs++

            if (connectionsBeforeUs >= 2){
                disconnectSession()
                onBackPressed()
            }

        }
    }

    override fun onDisconnected(session: Session) {
        Log.d(LOG_TAG, "onDisconnected: Disconnected from session: " + session.getSessionId())
    }

    override fun onStreamReceived(session: Session, stream: Stream) {
        Log.d(LOG_TAG, "onStreamReceived: New Stream Received " + stream.getStreamId().toString() + " in session: " + session.getSessionId())
        if (mSubscriber == null) {
            mSubscriber = Subscriber.Builder(this, stream).build()
            mSubscriber?.renderer?.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL)
            mSubscriber?.setSubscriberListener(this)
            mSession?.subscribe(mSubscriber)
            otSubscriberContainer.addView(mSubscriber?.view)

            otPublisherContainer.visibility = View.VISIBLE
            otProgress.visibility = View.GONE
            showTools()
        }
    }

    override fun onStreamDropped(session: Session, stream: Stream) {
        Log.d(LOG_TAG, "onStreamDropped: Stream Dropped: " + stream.getStreamId().toString() + " in session: " + session.getSessionId())
        if (mSubscriber != null) {
            mSubscriber?.destroy()
            mSubscriber = null
            otSubscriberContainer?.removeAllViews()
            onBackPressed()
        }
    }

    override fun onError(session: Session, opentokError: OpentokError) {
        Log.e(LOG_TAG, "onError: " + opentokError.errorDomain + " : " +
                opentokError.errorCode + " - " + opentokError.message + " in session: " + session.getSessionId())
        showOpenTokError(opentokError)
    }

    */
/* Publisher Listener methods *//*

    override fun onStreamCreated(publisherKit: PublisherKit?, stream: Stream) {
        Log.d(LOG_TAG, "onStreamCreated: Publisher Stream Created. Own stream " + stream.getStreamId())
    }

    override fun onStreamDestroyed(publisherKit: PublisherKit?, stream: Stream) {
        Log.d(LOG_TAG, "onStreamDestroyed: Publisher Stream Destroyed. Own stream " + stream.getStreamId())
    }

    override fun onError(publisherKit: PublisherKit, opentokError: OpentokError) {
        Log.e(LOG_TAG, "onError: " + opentokError.errorDomain + " : " +
                opentokError.errorCode + " - " + opentokError.message)
        showOpenTokError(opentokError)
    }

    override fun onConnected(subscriberKit: SubscriberKit) {
        Log.d(LOG_TAG, "onConnected: Subscriber connected. Stream: " + subscriberKit.stream.streamId)
    }

    override fun onDisconnected(subscriberKit: SubscriberKit) {
        Log.d(LOG_TAG, "onDisconnected: Subscriber disconnected. Stream: " + subscriberKit.stream.streamId)
    }

    override fun onError(subscriberKit: SubscriberKit, opentokError: OpentokError) {
        Log.e(LOG_TAG, "onError: " + opentokError.errorDomain + " : " +
                opentokError.errorCode + " - " + opentokError.message)
        showOpenTokError(opentokError)
    }

    private fun showOpenTokError(opentokError: OpentokError) {
        UIUtil.showToast(this, opentokError.errorDomain.name + ": " + opentokError.message + " Please, see the logcat.", Toast.LENGTH_LONG)
        onBackPressed()
    }

    private fun showConfigError(alertTitle: String, errorMessage: String) {
        Log.e(LOG_TAG, "Error $alertTitle: $errorMessage")
        AlertDialog.Builder(this)
                .setTitle(alertTitle)
                .setMessage(errorMessage)
                .setPositiveButton("ok") { dialog, which -> onBackPressed() }
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
    }

    private fun showTools(){
        if (otToolsLayout.visibility != View.VISIBLE) {
            otToolsLayout.animate().alpha(1f).withStartAction { otToolsLayout.visibility = View.VISIBLE }.setDuration(400).start()
        }
        handler.removeCallbacks(runnableForCloseTools)
        handler.postDelayed(runnableForCloseTools , 5000)
    }

    private fun disconnectSession() {
        if (mSession == null ) {
            return
        }
        if (mSubscriber != null) {
            otSubscriberContainer.removeAllViews()
            mSession?.unsubscribe(mSubscriber)
            mSubscriber?.destroy()
            mSubscriber = null

        }
        if (mPublisher != null) {
            otPublisherContainer.removeAllViews()
            mSession!!.unpublish(mPublisher)
            mPublisher!!.destroy()
            mPublisher = null
        }
        mSession!!.disconnect()
    }

    companion object {
        private val LOG_TAG = "MDOpenTokActivity"
        private const val RC_SETTINGS_SCREEN_PERM = 123
        private const val RC_VIDEO_APP_PERM = 124
    }
}*/
