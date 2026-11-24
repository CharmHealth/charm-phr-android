package com.charm.phr.mphr.ui.prescription


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.FragmentMedicationBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.PatMedGroupByDrugPOJO
import com.charm.phr.mphr.model.PatMedPOJO
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.ui.components.MDMedicationRefillDialog
import com.charm.phr.mphr.ui.components.ProgressDialog
import com.charm.phr.mphr.ui.prescription.add_medication.AddMedicationActivity
import com.charm.phr.mphr.ui.prescription.compose.DrugGroupedMedicationCardLayout
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.ChangeFilterListener
import com.charm.phr.mphr.util.DisableMultiSelectListener
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.RefreshDataListener
import com.charm.phr.mphr.util.UIUtil
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */


class MedicationFragment : DaggerFragment(), DisableMultiSelectListener, ChangeFilterListener, RefreshDataListener {


    lateinit var prescriptionViewModel: PrescriptionViewModel

    lateinit var progressDialog: ProgressDialog

    @Inject
    lateinit var authManager: AuthManager

    lateinit var imm: InputMethodManager

    val handler = Handler(Looper.getMainLooper())

    private lateinit var medicationListBinder: FragmentMedicationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        medicationListBinder = FragmentMedicationBinding.inflate(inflater, container, false)
        return medicationListBinder.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prescriptionViewModel = ViewModelProvider(requireActivity())[PrescriptionViewModel::class.java]

        medicationListBinder.medicationListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        if (!prescriptionViewModel.isFromReminder) {

            medicationListBinder.medicationListNoDataTV.nodataTextViewHint.visibility = View.VISIBLE
            medicationListBinder.medicationListNoDataTV.nodataTextViewHint.setText(R.string.medication_add_hint)
            UIUtil.makePlusColorPrimary(medicationListBinder.medicationListNoDataTV.nodataTextViewHint)
        }

        imm = this.activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager

        progressDialog = (this.activity as MDBaseActivity).progressDialog


        fetchMedicationObserver()

        if (!prescriptionViewModel.isMedicationListFetched) {

            changeStatusTxt()
            prescriptionViewModel.fetchMedicationList(true)
        }


        medicationListBinder.medicationListSwipeRefreshLayout.setOnRefreshListener {
            medicationListBinder.medicationListSwipeRefreshLayout.isRefreshing = true
            prescriptionViewModel.fetchMedicationList(true)
        }


        medicationListBinder.medicationListErrorPage.errorPageTryAgainBtn.setOnClickListener {
            medicationListBinder.medicationListErrorPage.root.visibility = View.GONE
            refreshData()
        }

    }

    private fun onReminderClick(medications: PatMedPOJO) {
        val prescriptionActivity = activity
        if (prescriptionActivity is PrescriptionActivity) {
            prescriptionActivity.reminderResultCallBack(medications.patMed?.dataInString ?: "", medications.practiceId)
        }
    }

    private fun onEditMedication(medications: PatMedPOJO) {
        val intent = Intent(this@MedicationFragment.context, AddMedicationActivity::class.java)
        intent.putExtra(ModelUtil.medicationToEditIntent, true)
        intent.putExtra(ModelUtil.medicationEditDataIntent, medications.patMed?.dataInString)
        intent.putExtra(ModelUtil.practiceIdIntent, medications.practiceId)
        intent.putExtra(ModelUtil.isHealthPassportIntent, medications.isHealthPassport)
        activity?.startActivityForResult(intent, 0)
        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun onRefillMedication(medications: PatMedPOJO) {
        val screenWidth = UIUtil.getScreenWidth(this@MedicationFragment.activity)
        val width = screenWidth - (2 * requireContext().resources.getDimensionPixelSize(R.dimen.common_dialog_horizontal_margin))

        val medicationRefillDialog = MDMedicationRefillDialog(requireActivity(), width)

        medicationRefillDialog.setMedicationRefillClickListener(object : MDMedicationRefillDialog.MedicationRefillClickListener {
            override fun onMedicationRefillClick(refill: String) {

                medications.reasonForRefill = refill

                handler.postDelayed({
                    progressDialog.show()
                    prescriptionViewModel.refillMedication(medications).observe(viewLifecycleOwner) { resp ->
                        if (resp != null) {
                            progressDialog.dismiss()
                            if (resp is Boolean) {
                                UIUtil.showToast(this@MedicationFragment.activity, R.string.medication_refill_request_success, Toast.LENGTH_LONG)
                            } else if (resp is APIResponseError) {
                                UIUtil.showToast(this@MedicationFragment.activity, R.string.error_medication_refill_request, Toast.LENGTH_LONG)
                            }
                        }
                    }
                }, 200)


            }
        })

        medicationRefillDialog.show()
    }

    private fun fetchMedicationObserver() {
        prescriptionViewModel.medicationListLive.observe(viewLifecycleOwner) { resp ->

            if (resp != null) {


                if (resp is Pair<*, *>) {
                    val messagePairList = resp as Pair<ArrayList<PatMedPOJO>, Boolean>
                    //else case not handled as the value set to true default
                    if (messagePairList.second) {
                        medicationListBinder.medicationListProgress.root.visibility = View.GONE
                        if (medicationListBinder.medicationListSwipeRefreshLayout.isRefreshing) {
                            medicationListBinder.medicationListSwipeRefreshLayout.isRefreshing = false
                        }
                        if (messagePairList.first.size == 0) {
                            medicationListBinder.medicationListNoDataTV.root.visibility = View.VISIBLE
                            medicationListBinder.medicationListRVLayout.visibility = View.GONE
                        } else {
                            medicationListBinder.medicationListNoDataTV.root.visibility = View.GONE
                            medicationListBinder.medicationListRVLayout.visibility = View.VISIBLE
                            // Group medications by drug ID
                            val groupedMedications = messagePairList.first.groupBy { it.drugDetail.first().drugDetailsId ?: "" }
                                .map { (drugId, medications) -> PatMedGroupByDrugPOJO(drugId, ArrayList(medications)) }
                            prescriptionViewModel.medicationListDrugGrouped.clear()
                            prescriptionViewModel.medicationListDrugGrouped.addAll(groupedMedications)
                            medicationListBinder.medicationListRVComposeView.setContent {
                                MPHRTheme {
                                    val listState = rememberLazyListState()
                                    val isAtTop by remember {
                                        derivedStateOf {
                                            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                                        }
                                    }
                                    medicationListBinder.medicationListSwipeRefreshLayout.isEnabled = isAtTop
                                    RenderMedicationList(listState, prescriptionViewModel.medicationListDrugGrouped, prescriptionViewModel.isFromReminder)
                                }
                            }
                            checkToShowBgImage()
                        }
                    }
                } else if (resp is APIResponseError) {
                    if (!prescriptionViewModel.isMedicationListFetched) {
                        medicationListBinder.medicationListNoDataTV.root.visibility = View.GONE
                        medicationListBinder.medicationListRVLayout.visibility = View.GONE
                        medicationListBinder.medicationListErrorPage.root.visibility = View.VISIBLE
                    }
                }

                prescriptionViewModel.isMedicationFetchComplete = true
            }
        }
    }

    @Composable
    fun RenderMedicationList(listState: LazyListState, rememberedMedicationList: SnapshotStateList<PatMedGroupByDrugPOJO>, fromReminder: Boolean) {
        var elementAdded by remember { mutableStateOf(false) }
        LazyColumn(state = listState) {
            items(rememberedMedicationList.size, key = { index -> rememberedMedicationList[index].drugId }) { index ->
                val groupedMedication = rememberedMedicationList[index]
                DrugGroupedMedicationCardLayout(
                    groupedMedication.patMedList,
                    ModelUtil.isPHREntryEnabled(authManager, ModelUtil.medicationModelId), onRefillClick = { medication ->
                        onRefillMedication(medication)
                    }, onEditClick = { medications ->
                        onEditMedication(medications)
                    }, onDeleteClick = {
                        deleteMedication(it, groupedMedication, index)
                        elementAdded = false
                    }, fromReminder, groupedMedication.patMedList.firstOrNull()?.patMed?.isAddedByPatient ?: false,
                    onReminderClick = { medications ->
                        onReminderClick(medications)
                    }
                )
            }
        }
        LaunchedEffect(rememberedMedicationList.size) {
            if (elementAdded) {
                listState.scrollToItem(0)
            }
            elementAdded = true
        }
    }


    override fun disableMultiSelect() {
        medicationListBinder.medicationListSwipeRefreshLayout.isEnabled = true
    }

    override fun onFilterChange() {
        medicationListBinder.medicationListSwipeRefreshLayout.isEnabled = true
        medicationListBinder.medicationListProgress.root.visibility = View.VISIBLE
        medicationListBinder.medicationListRVLayout.visibility = View.GONE
        changeStatusTxt()
        prescriptionViewModel.fetchMedicationList(true)

    }

    override fun refreshData() {
        changeStatusTxt()
        prescriptionViewModel.isMedicationListFetched = false
        prescriptionViewModel.isMedicationFetchComplete = false
        medicationListBinder.medicationListProgress.root.visibility = View.VISIBLE
        prescriptionViewModel.fetchMedicationList(true)
    }


    private fun changeStatusTxt() {

        /*if (prescriptionViewModel.isHealthPassport){
            medicationListStatusTV.visibility = View.GONE
        }
        else {*/
        medicationListBinder.medicationListStatusTV.visibility = View.VISIBLE
        val status = when (prescriptionViewModel.medicationStatus) {
            ModelUtil.medicationViewTypeAll -> R.string.prescription_status_all
            ModelUtil.medicationViewTypeActive -> R.string.prescription_status_active
            else -> R.string.prescription_status_inactive
        }

        if (prescriptionViewModel.isFromReminder) {
            medicationListBinder.medicationListStatusTV.text = getString(R.string.medications_str)
        } else {
            medicationListBinder.medicationListStatusTV.text =
                getString(R.string.prescription_status_template, getString(status), getString(R.string.medications_str).lowercase())
        }
        // }


    }

    private fun deleteMedication(medication: PatMedPOJO, groupedMedication: PatMedGroupByDrugPOJO, index: Int) {
        val builder = AlertDialog.Builder(this@MedicationFragment.activity)

        builder.setMessage(R.string.medication_delete_confirmation_msg)
        builder.setPositiveButton(R.string.yes_str) { _, _ ->
            progressDialog.show()
            prescriptionViewModel.deleteMedication(medication).observe(viewLifecycleOwner) { resp ->
                if (resp != null) {
                    progressDialog.dismiss()
                    if (resp is Boolean) {

                        prescriptionViewModel.isMedicationUpdated = true
                        prescriptionViewModel.medicationListDrugGrouped.remove(groupedMedication)
                        groupedMedication.patMedList.remove(medication)
                        if (groupedMedication.patMedList.isNotEmpty()) {
                            prescriptionViewModel.medicationListDrugGrouped.add(index, groupedMedication)
                        }
                        if (prescriptionViewModel.medicationListDrugGrouped.isEmpty()) {
                            medicationListBinder.medicationListNoDataTV.root.visibility = View.VISIBLE
                            medicationListBinder.medicationListRVLayout.visibility = View.GONE
                        } else {
                            medicationListBinder.medicationListNoDataTV.root.visibility = View.GONE
                            medicationListBinder.medicationListRVLayout.visibility = View.VISIBLE
                            checkToShowBgImage()
                        }

                        UIUtil.showToast(this@MedicationFragment.activity, R.string.medication_delete_success_msg, Toast.LENGTH_LONG)
                    } else if (resp is APIResponseError) {
                        UIUtil.showToast(this@MedicationFragment.activity, R.string.error_medication_delete__msg, Toast.LENGTH_LONG)
                    }
                }
            }
        }
        builder.setNegativeButton(R.string.no_str) { _, _ ->

        }

        builder.show()

    }

    private fun checkToShowBgImage() {

        if (!BuildUtil.isCustomApp) {
            val height = UIUtil.getScreenHeight(this.activity)

            val tv = TypedValue()
            var actionBarHeight = 0
            if (requireActivity().theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
            }

            val viewHeight = ((height - actionBarHeight) * 2) / 3

            val footerTreeObserver = medicationListBinder.medicationListRVComposeView.viewTreeObserver

            footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    try {

                        medicationListBinder.medicationListRVComposeView.viewTreeObserver?.removeOnGlobalLayoutListener(this)

                        val medicationHeight = medicationListBinder.medicationListRVComposeView.height

                        Log.d("MDHPBgHeights", "viewHeight -> $viewHeight ; rvHeight -> $medicationHeight")

                        if (medicationHeight < viewHeight) {
                            medicationListBinder.medicationListHPBgLayout.visibility = View.VISIBLE
                        } else {
                            medicationListBinder.medicationListHPBgLayout.visibility = View.GONE
                        }

                    } catch (e: Exception) {

                    }
                }
            })
        } else {
            medicationListBinder.medicationListHPBgLayout.visibility = View.GONE
        }
    }


}

