//package com.charm.phr.mphr.ui.videocall
//
//import android.content.Intent
//import android.content.res.Configuration
//import android.os.Bundle
//import com.charm.phr.mphr.R
//import us.zoom.sdk.NewMeetingActivity
//
//class VideoCallMeetingActivity : NewMeetingActivity() {
//
//    val TAG = "VideoCallMeetActivity"
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        disableFullScreenMode()
//    }
//
//
//
//
//    override fun onBackPressed() {
//
//        /*val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
//            when (which) {
//
//                DialogInterface.BUTTON_POSITIVE -> {
//                    super.onBackPressed()
//                    dialog.dismiss()
//                    finish()
//                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
//                }
//
//                DialogInterface.BUTTON_NEGATIVE -> {
//                    dialog.dismiss()
//                }
//
//            }
//
//        }
//
//        val dialogBuilder = AlertDialog.Builder(this)
//        val msg = this.resources.getString(R.string.zoom_exit_msg)
//        dialogBuilder.setMessage(msg)
//            .setPositiveButton(R.string.yes_str, dialogClickListener)
//            .setNegativeButton(R.string.no_str, dialogClickListener)
//            .show()*/
//
////        showLeaveDialog()
//
//    }
//
//    override fun getLayout(): Int {
//        return R.layout.activity_video_call_meeting
//    }
//
////    override fun isAlwaysFullScreen(): Boolean {
////        return false
////    }
//
//    override fun isSensorOrientationEnabled(): Boolean {
//        return false
//    }
//
//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//
//        disableFullScreenMode()
//    }
//
//    private fun disableFullScreenMode() {
////        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN.inv(), WindowManager.LayoutParams.FLAG_FULLSCREEN)
//    }
//
//    private fun switchToMainActivity(tab: Int) {
//
//        val intent = Intent(this, VideoCallActivity::class.java)
//        intent.action = VideoCallActivity.ACTION_RETURN_FROM_MEETING
//        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
//        intent.putExtra(VideoCallActivity.EXTRA_TAB_ID, tab)
//
//        startActivity(intent)
//    }
//
//}
