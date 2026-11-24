package com.charm.phr.mphr.ui.dashboard.user_profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import com.charm.phr.mphr.R
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.ui.Theme.MdColors
import com.charm.phr.mphr.ui.Theme.MdTheme
import com.charm.phr.mphr.ui.dashboard.compose.ProfileScreen
import com.charm.phr.mphr.ui.demographics.save_pat_demo.SavePatDemographicsActivity
import com.charm.phr.mphr.ui.localauth.app_lock.AppLockActivity
import com.charm.phr.mphr.ui.message.compose.ComposeMessageActivity
import com.charm.phr.mphr.ui.support.SupportActivity
import com.charm.phr.mphr.ui.tourpage.TourPageActivity
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.RbacUtil
import javax.inject.Inject

class UserProfileActivity : MDBaseActivity() {

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var userProfileViewModel: UserProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userProfileViewModel =
            ViewModelProvider(this, viewModelFactory)[UserProfileViewModel::class.java]
        userProfileViewModel.messageRestrictionList =
            intent.getStringExtra(ModelUtil.messageComposeMsgRestrictionListIntent) ?: ""


        val hasPrivilege =
            RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.VIEW_MESSAGES, this)
        val isMessageModuleEnabled =
            ModelUtil.isModuleEnabled(authManager.selectedPatientSection, ModelUtil.messageModuleId) && hasPrivilege

        setContent {
            MPHRTheme {
                Surface {

                    val selectedPatient by authManager.selectedPatientLive.observeAsState()
                    val patFilteredList by remember {
                        derivedStateOf {
                            authManager.patientList.filter {
                                it.practicePatientId != selectedPatient?.practicePatientId && !selectedPatient?.practicePatientId.isNullOrBlank()
                            }
                        }
                    }

                    val showLogOutDialog = remember { mutableStateOf(false) }

                    if (showLogOutDialog.value) {
                        LogOutAndIntentToTourPage(
                            authManager = authManager,
                            onDismiss = { showLogOutDialog.value = false }
                        )
                    }

                    ProfileScreen(
                        imageLoader = imageLoader,
                        selectedPatient = selectedPatient ?: return@Surface,
                        linkedPatientList = patFilteredList,
                        isMessageModuleEnabled = isMessageModuleEnabled,
                        onLinkedMembersClick = {
                            authManager.updateSelectedPatient(it)
                            authManager.questionnaireHPList = null
                            setResult(RESULT_OK)
                            finish()
                            overridePendingTransition(
                                android.R.anim.fade_in,
                                android.R.anim.fade_out
                            )
                        },
                        onMyInfoClick = { navigateTo(SavePatDemographicsActivity::class.java) },
                        onPatientImageClick = { navigateTo(SavePatDemographicsActivity::class.java) },
                        onAppFeedbackBackClick = { navigateTo(SupportActivity::class.java) },
                        onContactPracticeClick = {
                            navigateTo(ComposeMessageActivity::class.java) {
                                putExtra(
                                    ModelUtil.messageComposeMsgRestrictionListIntent,
                                    userProfileViewModel.messageRestrictionList
                                )
                                putExtra(
                                    ModelUtil.messageComposeFromDashboardIntent,
                                    true
                                ) //hardcoded because it is from dashboard
                            }
                        },
                        onOtherSettingsClick = { navigateTo(AppLockActivity::class.java) },
                        onLogOutClick = { showLogOutDialog.value = true },
                        onBackPressed = { onBackPressed() },
                        imageReloadKey = userProfileViewModel.imageReloadKey.intValue
                    )

                }
            }
        }

    }

    private fun navigateTo(activityClass: Class<*>, extras: Intent.() -> Unit = {}) {
        val intent = Intent(this, activityClass).apply(extras)
        if (activityClass == SavePatDemographicsActivity::class.java) {
            startActivityForResult(intent, 1)
        } else {
            startActivity(intent)
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    @Composable
    fun LogOutAndIntentToTourPage(authManager: AuthManager, onDismiss: () -> Unit) {
        val context = LocalContext.current

        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(
                    text = stringResource(R.string.alert_sign_out),
                    style = MdTheme.typography.medium16TextStyle,
                    color = MdColors.Black86
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                        handleLogout(authManager, context)
                    }) {
                    Text(
                        text = context.getString(R.string.yes_str),
                        style = MdTheme.typography.semiBold16TextStyle,
                        color = colorResource(R.color.colorPrimary)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onDismiss() }
                ) {
                    Text(
                        text = context.getString(R.string.no_str),
                        style = MdTheme.typography.semiBold16TextStyle,
                        color = colorResource(R.color.colorPrimary)
                    )
                }
            },
            containerColor = MdColors.White,
            shape = RoundedCornerShape(8.dp)
        )
    }

    private fun handleLogout(authManager: AuthManager, context: Context) {
        try {
            Log.d("MDLogout", "after call ")
            authManager.logout()
            val intent = Intent(context, TourPageActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            Log.d("MDLogout", "after call 2")
        } catch (e: Exception) {
            Log.d("MDLogout", " error 2 -> $e")
        }
        Log.d("MDLogout", "after call")
    }

    @OptIn(ExperimentalCoilApi::class)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 1) {

            val imageUpdated = data?.getBooleanExtra(ModelUtil.patientImageUpdated, false) == true
            if (imageUpdated) {
                imageLoader.diskCache?.clear()
                imageLoader.memoryCache?.clear()
                userProfileViewModel.imageReloadKey.intValue++
            }
            val resultIntent = Intent()
            resultIntent.putExtra(ModelUtil.patientImageUpdated, true)
            setResult(RESULT_OK, resultIntent)


        }
    }

}