package com.charm.phr.mphr.ui.videocall;

//import android.Manifest;
//import android.content.pm.PackageManager;
//import android.content.res.Configuration;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.WindowManager;
//import android.widget.ImageButton;
//
//import androidx.core.app.ActivityCompat;
//
//import com.charm.phr.mphr.R;
//
//import java.util.List;
//
//import us.zoom.sdk.CameraControlRequestResult;
//import us.zoom.sdk.CameraControlRequestType;
//import us.zoom.sdk.ChatMessageDeleteType;
//import us.zoom.sdk.FreeMeetingNeedUpgradeType;
//import us.zoom.sdk.ICameraControlRequestHandler;
//import us.zoom.sdk.IMeetingArchiveConfirmHandler;
//import us.zoom.sdk.IMeetingInputUserInfoHandler;
//import us.zoom.sdk.IRequestLocalRecordingPrivilegeHandler;
//import us.zoom.sdk.InMeetingAudioController;
//import us.zoom.sdk.InMeetingChatController;
//import us.zoom.sdk.InMeetingChatMessage;
//import us.zoom.sdk.InMeetingEventHandler;
//import us.zoom.sdk.InMeetingService;
//import us.zoom.sdk.InMeetingServiceListener;
//import us.zoom.sdk.LocalRecordingRequestPrivilegeStatus;
//import us.zoom.sdk.MeetingParameter;
//import us.zoom.sdk.MeetingServiceListener;
//import us.zoom.sdk.MeetingStatus;
//import us.zoom.sdk.MobileRTCFocusModeShareType;
//import us.zoom.sdk.NewMeetingActivity;
//import us.zoom.sdk.VideoQuality;
//import us.zoom.sdk.ZoomSDK;
//import us.zoom.sdk.ZoomSDKFileReceiver;
//import us.zoom.sdk.ZoomSDKFileSender;
//import us.zoom.sdk.ZoomSDKFileTransferInfo;
//
//public class MyMeetingActivity extends NewMeetingActivity {
//
//    private View mdVideoIV;
//    private View mdAudioIv;
//
//    private final String mTAG = "mymeetingact"; //No I18N
//
//    private InMeetingService inMeetingService;
//
//    @Override
//    protected int getLayout() {
//        return R.layout.my_meeting_layout;
//    }
//
////    @Override
////    protected boolean isAlwaysFullScreen() {
////        return false;
////    }
//
//    @Override
//    protected boolean isSensorOrientationEnabled() {
//        return false;
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (!ZoomSDK.getInstance().isInitialized()) {
//            finish();
//            return;
//        }
//
//        getPermissionForCameraAndAudio();
//
//        inMeetingService = ZoomSDK.getInstance().getInMeetingService();
//        initListeners();
//        disableFullScreenMode();
//
//
//    }
//
//    private void getPermissionForCameraAndAudio() {
//        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(MyMeetingActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 1001);
//        }
//
//    }
//
//    private void setViewsAndClickListeners() {
//
//        ImageButton mdLeaveZoomMeetingBtn = findViewById(R.id.mdLeaveZoomMeetingBtn);
//        View mdSwitchCameraLayout = findViewById(R.id.mdSwitchCameraLayout);
////        View mdSwitchCameraIv = findViewById(R.id.mdSwitchCameraIv);
//        View mdVideoLayout = findViewById(R.id.mdVideoLayout);
//        mdVideoIV = findViewById(R.id.mdVideoIV);
//        View mdAudioLayout = findViewById(R.id.mdAudioLayout);
//        mdAudioIv =  findViewById(R.id.mdAudioIv);
//        View mdParticipantsLayout = findViewById(R.id.mdParticipantsLayout);
////        View mdParticipantsIv = findViewById(R.id.mdParticipantsIv);
//
//
//        mdLeaveZoomMeetingBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ZoomSDK.getInstance().getInMeetingService().leaveCurrentMeeting(true);
//            }
//        });
//
//        mdSwitchCameraLayout.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                inMeetingService.getInMeetingVideoController().switchToNextCamera();
//            }
//        });
//
//        mdVideoLayout.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (inMeetingService.getInMeetingVideoController().isMyVideoMuted()) {
//                    Log.d(mTAG, "isMyVideoMuted true"); //No I18N
//                    inMeetingService.getInMeetingVideoController().muteMyVideo(false);
//                    mdVideoIV.setBackgroundResource(R.drawable.ic_videocam_white);
//                } else {
//                    Log.d(mTAG, "isMyVideoMuted false"); //No I18N
//                    mdVideoIV.setBackgroundResource(R.drawable.ic_videocam_off_white_24dp);
//                    inMeetingService.getInMeetingVideoController().muteMyVideo(true);
//                }
//
//            }
//        });
//
//        mdAudioLayout.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                InMeetingAudioController inMeetingAudioController = inMeetingService.getInMeetingAudioController();
//                if (inMeetingAudioController.isAudioConnected()) {
//                    Log.d(mTAG, "isAudioConnected"); //No I18N
//
//                    Log.d(mTAG, inMeetingService.getInMeetingAudioController().canUnmuteMyAudio() + "canUnmuteMyAudio "); //No I18N
//
//                    if (inMeetingAudioController.isMyAudioMuted()){
//                        Log.d(mTAG, "isMyAudioMuted true"); //No I18N
//                        mdAudioIv.setBackgroundResource(R.drawable.ic_mic_white_solid_24);
//                        inMeetingAudioController.muteMyAudio(false);
//                    } else {
//                        mdAudioIv.setBackgroundResource(R.drawable.ic_mic_off_white_solid_24);
//                        Log.d(mTAG, "isMyAudioMuted false"); //No I18N
//                        inMeetingAudioController.muteMyAudio(true);
//                    }
//
//                } else {
//                    Log.d(mTAG, "connectAudioWithVoIP"); //No I18N
//                    inMeetingAudioController.connectAudioWithVoIP();
//                }
//            }
//        });
//
//        mdParticipantsLayout.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                inMeetingService.showZoomParticipantsUI(MyMeetingActivity.this, 1100);
//            }
//        });
//
//
//    }
//
//    private void initListeners() {
//        ZoomSDK.getInstance().getMeetingService().addListener(new MeetingServiceListener() {
//            @Override
//            public void onMeetingStatusChanged(MeetingStatus meetingStatus, int i, int i1) {
//                if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING) {
//                    updateButtonsStatus();
//                }
//            }
//
//            @Override
//            public void onMeetingParameterNotification(MeetingParameter meetingParameter) {
//
//            }
//        });
//        ZoomSDK.getInstance().getInMeetingService().addListener(new InMeetingServiceListener() {
//
//            @Override
//            public void onMeetingNeedPasswordOrDisplayName(boolean b, boolean b1, InMeetingEventHandler inMeetingEventHandler) {}
//
//            @Override
//            public void onWebinarNeedRegister(String s) {}
//
//            @Override
//            public void onJoinMeetingNeedUserInfo(IMeetingInputUserInfoHandler iMeetingInputUserInfoHandler) {
//
//            }
//
//            @Override
//            public void onJoinWebinarNeedUserNameAndEmail(InMeetingEventHandler inMeetingEventHandler) {}
//
//            @Override
//            public void onWebinarNeedInputScreenName(InMeetingEventHandler inMeetingEventHandler) {
//
//            }
//
//            @Override
//            public void onMeetingNeedCloseOtherMeeting(InMeetingEventHandler handler) {}
//
//            @Override
//            public void onMeetingFail(int i, int i1) {}
//
//            @Override
//            public void onMeetingLeaveComplete(long l) {}
//
//            @Override
//            public void onMeetingUserJoin(List<Long> list) {}
//
//            @Override
//            public void onMeetingUserLeave(List<Long> list) {}
//
//            @Override
//            public void onMeetingUserUpdated(long l) {}
//
//            @Override
//            public void onInMeetingUserAvatarPathUpdated(long l) {
//
//            }
//
//            @Override
//            public void onMeetingHostChanged(long l) {}
//
//
//            @Override
//            public void onMeetingCoHostChange(long userId, boolean isCoHost) {
//            }
//
//            @Override
//            public void onActiveVideoUserChanged(long l) {
//            }
//
//            @Override
//            public void onActiveSpeakerVideoUserChanged(long l) {
//            }
//
//            @Override
//            public void onSpotlightVideoChanged(List<Long> userList) {
//            }
//
//            @Override
//            public void onUserVideoStatusChanged(long l, VideoStatus videoStatus) {
//            }
//
//
//            @Override
//            public void onSinkMeetingVideoQualityChanged(VideoQuality videoQuality, long userId) {
//
//            }
//
//            @Override
//            public void onMicrophoneStatusError(InMeetingAudioController.MobileRTCMicrophoneError mobileRTCMicrophoneError) {
//            }
//
//            @Override
//            public void onUserAudioStatusChanged(long l, AudioStatus audioStatus) {
//            }
//
//            @Override
//            public void onHostAskUnMute(long l) {
//            }
//
//            @Override
//            public void onHostAskStartVideo(long l) {
//            }
//
//            @Override
//            public void onUserAudioTypeChanged(long l) {
//            }
//
//            @Override
//            public void onMyAudioSourceTypeChanged(int i) {
//            }
//
//            @Override
//            public void onLowOrRaiseHandStatusChanged(long l, boolean b) {
//            }
//
//            @Override
//            public void onChatMessageReceived(InMeetingChatMessage inMeetingChatMessage) {
//            }
//
//            @Override
//            public void onChatMsgDeleteNotification(String msgID, ChatMessageDeleteType deleteBy) {
//            }
//
//            @Override
//            public void onShareMeetingChatStatusChanged(boolean b) {
//
//            }
//
//            @Override
//            public void onSilentModeChanged(boolean b) {
//                Log.d(mTAG, "onSilentModeChanged"); //No I18N
//                updateButtonsStatus();
//            }
//
//            @Override
//            public void onFreeMeetingReminder(boolean b, boolean b1, boolean b2) {
//            }
//
//            @Override
//            public void onMeetingActiveVideo(long l) {
//
//            }
//
//            @Override
//            public void onSinkAttendeeChatPriviledgeChanged(int i) {
//
//            }
//
//            @Override
//            public void onSinkAllowAttendeeChatNotification(int i) {
//
//            }
//
//            @Override
//            public void onSinkPanelistChatPrivilegeChanged(InMeetingChatController.MobileRTCWebinarPanelistChatPrivilege privilege) {}
//
//
//            @Override
//            public void onUserNamesChanged(List<Long> userList) {}
//
//            @Override
//            public void onPermissionRequested(String[] permissions) {}
//
//            @Override
//            public void onFreeMeetingNeedToUpgrade(FreeMeetingNeedUpgradeType freeMeetingNeedUpgradeType, String s) {}
//
//            @Override
//            public void onFreeMeetingUpgradeToGiftFreeTrialStart() {}
//
//            @Override
//            public void onFreeMeetingUpgradeToGiftFreeTrialStop() {}
//
//            @Override
//            public void onFreeMeetingUpgradeToProMeeting() {}
//
//            @Override
//            public void onClosedCaptionReceived(String message, long senderId) {}
//
//            @Override
//            public void onRecordingStatus(RecordingStatus recordingStatus) {}
//
//            @Override
//            public void onInvalidReclaimHostkey() {}
//
//            @Override
//            public void onHostVideoOrderUpdated(List<Long> orderList) {}
//
//            @Override
//            public void onFollowHostVideoOrderChanged(boolean bFollow) {}
//
//            @Override
//            public void onAllHandsLowered() {}
//
//            @Override
//            public void onLocalRecordingStatus(long userId, RecordingStatus status) {}
//
//            @Override
//            public void onLocalVideoOrderUpdated(List<Long> localOrderList) {}
//
//            @Override
//            public void onLocalRecordingPrivilegeRequested(IRequestLocalRecordingPrivilegeHandler iRequestLocalRecordingPrivilegeHandler) {
//
//            }
//
//            @Override
//            public void onSuspendParticipantsActivities() {
//
//            }
//
//            @Override
//            public void onAllowParticipantsStartVideoNotification(boolean b) {
//
//            }
//
//            @Override
//            public void onAllowParticipantsRenameNotification(boolean b) {
//
//            }
//
//            @Override
//            public void onAllowParticipantsUnmuteSelfNotification(boolean b) {
//
//            }
//
//            @Override
//            public void onAllowParticipantsShareWhiteBoardNotification(boolean b) {
//
//            }
//
//            @Override
//            public void onMeetingLockStatus(boolean b) {
//
//            }
//
//            @Override
//            public void onRequestLocalRecordingPrivilegeChanged(LocalRecordingRequestPrivilegeStatus localRecordingRequestPrivilegeStatus) {
//
//            }
//
//            @Override
//            public void onAICompanionActiveChangeNotice(boolean b) {
//
//            }
//
//            @Override
//            public void onParticipantProfilePictureStatusChange(boolean b) {
//
//            }
//
//            @Override
//            public void onCloudRecordingStorageFull(long l) {
//
//            }
//
//            @Override
//            public void onUVCCameraStatusChange(String s, UVCCameraStatus uvcCameraStatus) {
//
//            }
//
//            @Override
//            public void onFocusModeStateChanged(boolean b) {
//
//            }
//
//            @Override
//            public void onFocusModeShareTypeChanged(MobileRTCFocusModeShareType mobileRTCFocusModeShareType) {
//
//            }
//
//            @Override
//            public void onVideoAlphaChannelStatusChanged(boolean b) {
//
//            }
//
//            @Override
//            public void onAllowParticipantsRequestCloudRecording(boolean b) {
//
//            }
//
//            @Override
//            public void onSinkJoin3rdPartyTelephonyAudio(String s) {
//
//            }
//
//            @Override
//            public void onUserConfirmToStartArchive(IMeetingArchiveConfirmHandler iMeetingArchiveConfirmHandler) {
//
//            }
//
//            @Override
//            public void onCameraControlRequestReceived(long l, CameraControlRequestType cameraControlRequestType, ICameraControlRequestHandler iCameraControlRequestHandler) {
//
//            }
//
//            @Override
//            public void onCameraControlRequestResult(long l, boolean b) {
//
//            }
//
//            @Override
//            public void onCameraControlRequestResult(long l, CameraControlRequestResult cameraControlRequestResult) {
//
//            }
//
//            @Override
//            public void onFileSendStart(ZoomSDKFileSender zoomSDKFileSender) {
//
//            }
//
//            @Override
//            public void onFileReceived(ZoomSDKFileReceiver zoomSDKFileReceiver) {
//
//            }
//
//            @Override
//            public void onFileTransferProgress(ZoomSDKFileTransferInfo zoomSDKFileTransferInfo) {
//
//            }
//
//        });
//    }
//
//    @Override
//    public void onConfigurationChanged (Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//
//        disableFullScreenMode();
//    }
//
//    private void disableFullScreenMode() {
//        getWindow().setFlags(~WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        setViewsAndClickListeners();
//        updateButtonsStatus();
//
//        // disable animation
//        overridePendingTransition(0, 0);
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        try {
//            ZoomSDK.getInstance().getInMeetingService().leaveCurrentMeeting(true);
//        } catch (Exception ex) {
//            Log.d("MyMeetingActivity", "onDestroy: " + ex); //No I18N
//        }
//    }
//
//
//
//    private void updateButtonsStatus() {
//
//        View mdZoomBottomLayout = findViewById(R.id.mdZoomBottomLayout);
//
//        boolean enabled = (inMeetingService.isMeetingConnected() && !inMeetingService.isInWaitingRoom());
//        if (!enabled){
//            mdZoomBottomLayout.setVisibility(View.GONE);
//            return;
//        }
//
//        mdZoomBottomLayout.setVisibility(View.VISIBLE);
//
//        if (inMeetingService.getInMeetingVideoController().isMyVideoMuted()) {
//            Log.d(mTAG, "isMyVideoMuted true"); //No I18N
//            mdVideoIV.setBackgroundResource(R.drawable.ic_videocam_off_white_24dp);
//        } else {
//            Log.d(mTAG, "isMyVideoMuted false"); //No I18N
//            mdVideoIV.setBackgroundResource(R.drawable.ic_videocam_white);
//        }
//
//        Log.d(mTAG, inMeetingService.getInMeetingAudioController().canUnmuteMyAudio() + "canUnmuteMyAudio "); //No I18N
//
//        if (inMeetingService.getInMeetingAudioController().isMyAudioMuted()) {
//            Log.d(mTAG, "isMyAudioMuted true"); //No I18N
//            mdAudioIv.setBackgroundResource(R.drawable.ic_mic_off_white_solid_24);
//        } else {
//            Log.d(mTAG, "isMyAudioMuted false"); //No I18N
//            mdAudioIv.setBackgroundResource(R.drawable.ic_mic_white_solid_24);
//        }
//    }
//
//}
