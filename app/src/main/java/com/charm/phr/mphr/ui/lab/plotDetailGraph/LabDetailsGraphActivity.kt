package com.charm.phr.mphr.ui.lab.plotDetailGraph

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityLabDetailsGraphBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.LabParamHistoryPOJO
import com.charm.phr.mphr.model.SOAPTemplateTableCell
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.ui.components.compose.MDTable
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import javax.inject.Inject

class LabDetailsGraphActivity : MDBaseActivity() {


    private lateinit var activityLabDetailsGraphBinding: ActivityLabDetailsGraphBinding

    private lateinit var labDetailsGraphViewModel: LabDetailsGraphViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLabDetailsGraphBinding = ActivityLabDetailsGraphBinding.inflate(layoutInflater)
        setContentView(activityLabDetailsGraphBinding.root)
        labDetailsGraphViewModel = ViewModelProvider(this, viewModelFactory)[LabDetailsGraphViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(activityLabDetailsGraphBinding.labResultDetailToolbar)
        UIUtil.applyViewInsertForBottomView(activityLabDetailsGraphBinding.labResultMainLayout)

        setOnClickListener()
        labDetailsGraphViewModel.labParamName = intent.getStringExtra(ModelUtil.labParamNameIntent)
        labDetailsGraphViewModel.labParamReferenceValue = intent.getStringExtra(ModelUtil.labParamReferenceIntent)
        labDetailsGraphViewModel.labParamUnit = intent.getStringExtra(ModelUtil.labParamUnitIntent)
        getLabParamHistory(labDetailsGraphViewModel.labParamName ?: "")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun setOnClickListener() {
        activityLabDetailsGraphBinding.labResultDetailBackBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getLabParamHistory(paramName: String) {
        activityLabDetailsGraphBinding.labResultDetailProgress.root.visibility = View.VISIBLE
        labDetailsGraphViewModel.getLabParamHistory(paramName).observe(this) { resp ->
            if (resp != null) {
                activityLabDetailsGraphBinding.labResultDetailProgress.root.visibility = View.GONE
                activityLabDetailsGraphBinding.labResultDetailCV.visibility = View.VISIBLE
                activityLabDetailsGraphBinding.labResultDetailTV.text = paramName
                if (!labDetailsGraphViewModel.labParamReferenceValue.isNullOrBlank()) {
                    activityLabDetailsGraphBinding.labResultDetailReferenceTV.visibility = View.VISIBLE
                    activityLabDetailsGraphBinding.labResultDetailReferenceTV.text = labDetailsGraphViewModel.labParamReferenceValue
                }
                if (resp is LabParamHistoryPOJO) {
                    labDetailsGraphViewModel.labParamHistoryPOJO = resp
                    if (labDetailsGraphViewModel.labParamHistoryPOJO?.isStringValue == false) {
                        activityLabDetailsGraphBinding.labResultDetailChart.visibility = View.VISIBLE
                        val entries = labDetailsGraphViewModel.labParamHistoryPOJO?.paramHistoryEntryList ?: arrayListOf()
                        val graphData = UIUtil.buildLabParamGraphData(
                            this,
                            entries,
                            labDetailsGraphViewModel.labParamHistoryPOJO?.paramName ?: "",
                            labDetailsGraphViewModel.labParamUnit
                        )
                        UIUtil.drawLabParamLineChart(this, activityLabDetailsGraphBinding.labResultDetailChart, graphData)
                    } else {
                        activityLabDetailsGraphBinding.labResultDetailChart.visibility = View.GONE
                        renderTableView()
                    }
                } else if (resp is APIResponseError) {
                    UIUtil.showRetryAndCancelDialog(this, R.string.requestFailedTryAgain, R.string.retry_str, false) { isRetry ->
                        if (isRetry) {
                            getLabParamHistory(paramName)
                        } else {
                            onBackPressed()
                        }
                    }
                }
            }
        }
    }


    private fun renderTableView() {
        val tableData = getTableData(labDetailsGraphViewModel.labParamHistoryPOJO)
        activityLabDetailsGraphBinding.labResultDetailLL.addView(ComposeView(this).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                MPHRTheme {
                    MDTable(
                        Modifier.padding(start = 10.dp, top = 10.dp, bottom = 10.dp, end = 10.dp), tableData, 2, false
                    )
                }
            }
        })
    }

    private fun getTableData(labParamHistoryPOJO: LabParamHistoryPOJO?): ArrayList<ArrayList<SOAPTemplateTableCell>> {
        val tableData: ArrayList<ArrayList<SOAPTemplateTableCell>> = arrayListOf()
        val labelData: ArrayList<SOAPTemplateTableCell> = arrayListOf()
        labelData.add(SOAPTemplateTableCell("Label", getString(R.string.date_str)))
        labelData.add(SOAPTemplateTableCell("Label", getString(R.string.value_str)))
        tableData.add(labelData)
        labParamHistoryPOJO?.paramHistoryEntryList?.forEach {
            val columnData: ArrayList<SOAPTemplateTableCell> = arrayListOf()
            columnData.add(
                SOAPTemplateTableCell(
                    "text", it.date?.toString(ModelUtil.genericDateDisplayFormattor()) ?: ""
                )
            )
            columnData.add(SOAPTemplateTableCell("text", it.value ?: ""))
            tableData.add(columnData)
        }
        return tableData
    }
}
