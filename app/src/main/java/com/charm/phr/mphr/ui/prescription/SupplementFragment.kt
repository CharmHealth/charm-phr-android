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
import android.widget.Toast
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
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.FragmentSupplementBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.PatSupplementsGroupPOJO
import com.charm.phr.mphr.model.PatientSupplements
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.ui.components.MDMedicationRefillDialog
import com.charm.phr.mphr.ui.components.ProgressDialog
import com.charm.phr.mphr.ui.prescription.add_supplement.AddSupplementActivity
import com.charm.phr.mphr.ui.prescription.compose.GroupedSupplementCardLayout
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


class SupplementFragment : DaggerFragment(), DisableMultiSelectListener, ChangeFilterListener, RefreshDataListener {


    lateinit var prescriptionViewModel: PrescriptionViewModel


    @Inject
    lateinit var authManager: AuthManager

    lateinit var progressDialog: ProgressDialog

    val handler = Handler(Looper.getMainLooper())

    private lateinit var supplementBinder: FragmentSupplementBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        supplementBinder = FragmentSupplementBinding.inflate(inflater, container, false)
        return supplementBinder.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prescriptionViewModel = ViewModelProvider(requireActivity())[PrescriptionViewModel::class.java]

        supplementBinder.supplementListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        progressDialog = (this.activity as MDBaseActivity).progressDialog

        if (!prescriptionViewModel.isFromReminder) {

            supplementBinder.supplementListNoDataTV.nodataTextViewHint.visibility = View.VISIBLE
            supplementBinder.supplementListNoDataTV.nodataTextViewHint.setText(R.string.supplement_add_hint)
            UIUtil.makePlusColorPrimary(supplementBinder.supplementListNoDataTV.nodataTextViewHint)
        }

        fetchSupplementObserver()


        if (!prescriptionViewModel.isSupplementListFetched) {
            changeStatusTxt()
            prescriptionViewModel.fetchSupplements(true)
        }


        supplementBinder.supplementListSwipeRefreshLayout.setOnRefreshListener {

            supplementBinder.supplementListSwipeRefreshLayout.isRefreshing = true
            prescriptionViewModel.fetchSupplements(true)
        }

        supplementBinder.supplementListErrorPage.errorPageTryAgainBtn.setOnClickListener {
            supplementBinder.supplementListErrorPage.root.visibility = View.GONE
            refreshData()
        }
    }

    private fun fetchSupplementObserver() {
        prescriptionViewModel.supplementListLive.observe(viewLifecycleOwner) { resp ->

            if (resp != null) {


                if (resp is Pair<*, *>) {
                    val messagePairList = resp as Pair<ArrayList<PatientSupplements>, Boolean>

                    if (messagePairList.second) {


                        supplementBinder.supplementListProgress.root.visibility = View.GONE

                        if (supplementBinder.supplementListSwipeRefreshLayout.isRefreshing) {
                            supplementBinder.supplementListSwipeRefreshLayout.isRefreshing = false
                        }
                        //Commented as we can have multiple reminder for same supplements
//                        val tempMessageList = arrayListOf<PatientSupplements>()
//                        if (prescriptionViewModel.isFromReminder) {
//                            messagePairList.first.forEach {
//                                if (it.reminders.isNotEmpty()) {
//                                    tempMessageList.add(it)
//                                }
//                            }
//                        }
//                        messagePairList.first.removeAll(tempMessageList.toSet())
                        if (messagePairList.first.size == 0) {
                            supplementBinder.supplementListNoDataTV.root.visibility = View.VISIBLE
                            supplementBinder.supplementListRVLayout.visibility = View.GONE
                        } else {
                            supplementBinder.supplementListNoDataTV.root.visibility = View.GONE
                            supplementBinder.supplementListRVLayout.visibility = View.VISIBLE
                            // Group supplements by supplement ID
                            val groupedSupplements = messagePairList.first.groupBy { it.supplement?.supplementId ?: "" }
                                .map { (supplementId, supplements) -> PatSupplementsGroupPOJO(supplementId, ArrayList(supplements)) }

                            prescriptionViewModel.supplementsGroupedList.clear()
                            prescriptionViewModel.supplementsGroupedList.addAll(groupedSupplements)
                            supplementBinder.supplementListRVComposeView.setContent {
                                MPHRTheme {
                                    val listState = rememberLazyListState()
                                    val isAtTop by remember {
                                        derivedStateOf {
                                            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                                        }
                                    }
                                    supplementBinder.supplementListSwipeRefreshLayout.isEnabled = isAtTop
                                    RenderSupplementList(
                                        Modifier,
                                        listState,
                                        prescriptionViewModel.supplementsGroupedList,
                                        prescriptionViewModel.isFromReminder
                                    )
                                }
                            }
                            checkToShowBgImage()
                        }
                    }
                } else if (resp is APIResponseError) {
                    if (!prescriptionViewModel.isSupplementListFetched) {
                        supplementBinder.supplementListNoDataTV.root.visibility = View.GONE
                        supplementBinder.supplementListRVLayout.visibility = View.GONE
                        supplementBinder.supplementListErrorPage.root.visibility = View.VISIBLE
                    }
                }

                prescriptionViewModel.isSupplementFetchComplete = true
            }
        }
    }

    @Composable
    fun RenderSupplementList(
        modifier: Modifier = Modifier,
        listState: LazyListState,
        supplementsGroupedList: SnapshotStateList<PatSupplementsGroupPOJO>,
        fromReminder: Boolean
    ) {
        var elementAdded by remember { mutableStateOf(false) }
        LazyColumn(state = listState) {
            items(supplementsGroupedList.size, key = { index -> supplementsGroupedList[index].supplementId }) { index ->
                val groupedSupplements = supplementsGroupedList[index]
                GroupedSupplementCardLayout(
                    groupedSupplements.patientSupplementList,
                    fromReminder,
                    onRefillClick = { supplement ->
                        refillSupplement(supplement)
                    },
                    onEditClick = { supplements ->
                        onSupplementEditClick(supplements)
                    },
                    onDeleteClick = { supplements ->
                        deleteSupplement(supplements, groupedSupplements, index)
                        elementAdded = false
                    },
                    onReminderClick = { supplements ->
                        onReminderClick(supplements)
                    },
                    ModelUtil.isPHREntryEnabled(authManager, ModelUtil.supplementModelId),
                    groupedSupplements.patientSupplementList.firstOrNull()?.isAddedByPatient ?: false
                )
            }
        }
        LaunchedEffect(supplementsGroupedList.size) {
            if (elementAdded) {
                listState.scrollToItem(0)
            }
            elementAdded = true
        }
    }

    private fun onSupplementEditClick(supplements: PatientSupplements) {
        val intent = Intent(this@SupplementFragment.context, AddSupplementActivity::class.java)
        intent.putExtra(ModelUtil.supplementToEditIntent, true)
        intent.putExtra(ModelUtil.supplementEditDataIntent, supplements.toString)
        intent.putExtra(ModelUtil.practiceIdIntent, supplements.practiceId)
        activity?.startActivityForResult(intent, 0)
        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun onReminderClick(supplements: PatientSupplements) {
        val prescriptionActivity = activity
        if (prescriptionActivity is PrescriptionActivity) {
            prescriptionActivity.reminderResultCallBack(supplements.toString, supplements.practiceId)
        }
    }

    override fun disableMultiSelect() {
        supplementBinder.supplementListSwipeRefreshLayout.isEnabled = true
    }

    override fun onFilterChange() {
        changeStatusTxt()
        supplementBinder.supplementListSwipeRefreshLayout.isEnabled = true
        supplementBinder.supplementListProgress.root.visibility = View.VISIBLE
        supplementBinder.supplementListRVLayout.visibility = View.GONE
        prescriptionViewModel.fetchSupplements(true)

    }

    override fun refreshData() {
        prescriptionViewModel.isSupplementListFetched = false
        prescriptionViewModel.isSupplementFetchComplete = false
        supplementBinder.supplementListProgress.root.visibility = View.VISIBLE
        changeStatusTxt()
        prescriptionViewModel.fetchSupplements(true)
    }

    private fun changeStatusTxt() {

        val status = when (prescriptionViewModel.supplementStatus) {
            ModelUtil.medicationViewTypeAll -> R.string.prescription_status_all
            ModelUtil.medicationViewTypeActive -> R.string.prescription_status_active
            else -> R.string.prescription_status_inactive
        }

        if (prescriptionViewModel.isFromReminder) {
            supplementBinder.supplementListStatusTV.text = getString(R.string.supplements_str)
        } else {
            supplementBinder.supplementListStatusTV.text =
                getString(R.string.prescription_status_template, getString(status), getString(R.string.supplements_str).lowercase())
        }

    }

    private fun refillSupplement(patientSupplements: PatientSupplements) {
        val screenWidth = UIUtil.getScreenWidth(this.activity)
        val width = screenWidth - (2 * requireContext().resources.getDimensionPixelSize(R.dimen.common_dialog_horizontal_margin))

        val medicationRefillDialog = MDMedicationRefillDialog(requireActivity(), width)

        medicationRefillDialog.setMedicationRefillClickListener(object : MDMedicationRefillDialog.MedicationRefillClickListener {
            override fun onMedicationRefillClick(refill: String) {

                patientSupplements.reasonForRefill = refill

                handler.postDelayed({
                    progressDialog.show()
                    prescriptionViewModel.refillSupplement(patientSupplements).observe(viewLifecycleOwner) { resp ->
                        if (resp != null) {
                            progressDialog.dismiss()
                            if (resp is Boolean) {
                                UIUtil.showToast(requireActivity(), R.string.medication_refill_request_success, Toast.LENGTH_LONG)
                            } else if (resp is APIResponseError) {
                                UIUtil.showToast(requireActivity(), R.string.error_medication_refill_request, Toast.LENGTH_LONG)
                            }
                        }
                    }
                }, 200)


            }
        })

        medicationRefillDialog.show()
    }

    private fun deleteSupplement(supplement: PatientSupplements, groupedSupplementsGroupPOJO: PatSupplementsGroupPOJO, index: Int) {
        val builder = AlertDialog.Builder(requireActivity())

        builder.setMessage(R.string.supplement_delete_confirmation_msg)
        builder.setPositiveButton(R.string.yes_str) { _, _ ->
            progressDialog.show()
            prescriptionViewModel.deleteSupplement(supplement).observe(viewLifecycleOwner) { resp ->
                if (resp != null) {
                    progressDialog.dismiss()
                    if (resp is Boolean) {
                        prescriptionViewModel.supplementsGroupedList.remove(groupedSupplementsGroupPOJO)
                        groupedSupplementsGroupPOJO.patientSupplementList.remove(supplement)
                        if (groupedSupplementsGroupPOJO.patientSupplementList.isNotEmpty()) {
                            prescriptionViewModel.supplementsGroupedList.add(index, groupedSupplementsGroupPOJO)
                        }
                        if (prescriptionViewModel.supplementsGroupedList.isEmpty()) {
                            supplementBinder.supplementListNoDataTV.root.visibility = View.VISIBLE
                            supplementBinder.supplementListRVLayout.visibility = View.GONE
                        } else {
                            supplementBinder.supplementListNoDataTV.root.visibility = View.GONE
                            supplementBinder.supplementListRVLayout.visibility = View.VISIBLE

                            checkToShowBgImage()
                        }

                        UIUtil.showToast(requireActivity(), R.string.supplement_delete_success_msg, Toast.LENGTH_LONG)
                    } else if (resp is APIResponseError) {
                        UIUtil.showToast(requireActivity(), R.string.error_supplement_delete__msg, Toast.LENGTH_LONG)
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

            val footerTreeObserver = supplementBinder.supplementListRVComposeView.viewTreeObserver

            footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    try {

                        supplementBinder.supplementListRVComposeView.viewTreeObserver?.removeOnGlobalLayoutListener(this)

                        val supplementHeight = supplementBinder.supplementListRVComposeView.height

                        Log.d("MDHPBgHeights", "viewHeight -> $viewHeight ; rvHeight -> $supplementHeight")

                        if (supplementHeight < viewHeight) {
                            supplementBinder.supplementListHPBgLayout.visibility = View.VISIBLE
                        } else {
                            supplementBinder.supplementListHPBgLayout.visibility = View.GONE
                        }

                    } catch (e: Exception) {

                    }
                }
            })
        } else {
            supplementBinder.supplementListHPBgLayout.visibility = View.GONE
        }
    }
}

