package charmzoom
import us.zoom.sdk.MobileRTCVideoUnitAspectMode

//Charm video lib utils
class CvlModelUtil {

    companion object {

        const val cvlMeetingNumIntent = "meetingNum"
        const val cvlUserIdIntent = "UserId"
        const val cvlUserDisplayName = "zmUserDisplayName"
        const val cvlZakIntent = "zak"
        const val cvlUserTypeIntent = "userType"
        const val cvlIsMinimizedIntent = "isMinimized"

        const val cvlUserTypeHost = "HOST"
        const val cvlUserTypeAttendee = "ATTENDEE"
        const val cvlaspectRationMode = MobileRTCVideoUnitAspectMode.VIDEO_ASPECT_PAN_AND_SCAN
        const val cvlStartTime = "zmStartTime"
        const val cvlTopic = "zmTopic"

        const val cvlSdkKey="zmSdkKey"
        const val cvlSdkSecret="zmSdkSecret"
        const val cvlWebDomain="zmWebDomain"
        const val cvlSdkJwt="cvlSdkJwt"
        const val cvlThemeColor="zmThemeColor"
        const val cvlMuteAudio="zmMuteAudio"
        const val cvlMuteVideo="zmMuteVideo"

        const val videoCallHost = "HOST"
        const val videoCallAttendee = "ATTENDEE"

//        @JvmStatic
//        fun showToast(context: Context?, @StringRes sourceId: Int, duration: Int): Toast? {
//
//            context ?: return null
//
//            val toast = Toast.makeText(context, sourceId, duration)
//
//            val offSet = context.resources?.getDimension(R.dimen)?.toInt()
//            offSet?.let {
//                toast.setGravity(Gravity.BOTTOM, 0, offSet)
//                toast.show()
//            }
//
//            return toast
//        }

//        @JvmStatic
//        fun showToast(context: Context?, source: String?, duration: Int): Toast? {
//
//            context ?: return null
//
//            val toast = Toast.makeText(context, source, duration)
//
//            val offSet = context.resources?.getDimension(R.dimen.cvl_commonToastBottomMargin)?.toInt()
//            offSet?.let {
//                toast.setGravity(Gravity.BOTTOM, 0, offSet)
//                toast.show()
//            }
//
//            return toast
//        }
        
    }

  

}