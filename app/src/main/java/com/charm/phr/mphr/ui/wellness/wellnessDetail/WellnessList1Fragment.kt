package com.charm.phr.mphr.ui.wellness.wellnessDetail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.FragmentWellnessList1Binding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.GraphData
import com.charm.phr.mphr.model.MDEntry
import com.charm.phr.mphr.model.PatientVitalTracker
import com.charm.phr.mphr.model.PatientVitals
import com.charm.phr.mphr.model.WellnessGridEntry
import com.charm.phr.mphr.ui.components.MDAxisStringFormatter
import com.charm.phr.mphr.ui.components.iconSwitch.GraphMarkerView
import com.charm.phr.mphr.ui.components.iconSwitch.IconSwitch
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.tabs.TabLayout
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class WellnessList1Fragment : DaggerFragment() {

    @Inject
    lateinit var wellnessDetailListAdapter: WellnessDetailListAdapter

    lateinit var wellnessDetailViewModel: WellnessDetailViewModel

    var listener: WellnessList2StartListener? = null

    var isZoomSet = false
    var prevDataSize = 0

    private val degree = "\u00B0"

    /*@Inject
    lateinit var wellnessDetailMetricListAdapter: WellnessDetailMetricListAdapter

    @Inject
    lateinit var wellnessDetailGridAdapter: WellnessDetailGridAdapter*/

    private val colorListForGraph = arrayListOf(
        R.color.firstAxisColor,
        R.color.secondAxisColor,
        R.color.thirdAxisColor,
        R.color.fourthAxisColor,
        R.color.fifthAxisColor,
        R.color.sixthAxisColor,
        R.color.seventhAxisColor,
        R.color.eighthAxisColor,
        R.color.ninthAxisColor,
        R.color.tenthAxisColor,
        R.color.eleventhAxisColor,
        R.color.twelfthAxisColor
    )

    private var _binding: FragmentWellnessList1Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWellnessList1Binding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        wellnessDetailViewModel =
            ViewModelProvider(requireActivity()).get(WellnessDetailViewModel::class.java)


        binding.wellnessRV.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.wellnessRV.adapter = wellnessDetailListAdapter

        isZoomSet = false

        if (wellnessDetailViewModel.isWellnessDetailFetch) {

            if (wellnessDetailViewModel.isWellnessDetailResponseSuccess) {

                if (wellnessDetailViewModel.wellnessDetailLive.hasObservers()) {
                    wellnessDetailViewModel.wellnessDetailLive.removeObservers(this)
                }

                wellnessDetailViewModel.wellnessDetailLive.observe(
                    viewLifecycleOwner,
                    Observer { resp ->
                        if (resp != null) {
                            updateWellnessTab(resp)
                        }

                    })
            } else {
                binding.wellnessProgress.root.visibility = View.GONE
                binding.wellnessErrorPage.root.visibility = View.VISIBLE
                binding.wellnessNoDataTV.root.visibility = View.GONE
                binding.wellnessScroll.visibility = View.GONE
            }
        } else {
            fetchWellnessDetail()
        }

        binding.wellnessErrorPage.errorPageTryAgainBtn.setOnClickListener {
            fetchWellnessDetail()
        }

        binding.wellnessRVShowMoreTV.setOnClickListener {
            wellnessDetailListAdapter.showMorePatVitalList()
            binding.wellnessRVShowMoreTV.visibility = View.GONE
        }

        binding.wellnessScroll.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->

            Log.d(
                "MDHomeScroll",
                "scrollX -> $scrollX , scrollY -> $scrollY , oldScrollX -> $oldScrollX , oldScrollY -> $oldScrollY"
            )

            try {

                val dashboardActivity = activity as WellnessDetailActivity
                if (scrollY > oldScrollY && dashboardActivity.floatingActionButtonVisiblity() == View.VISIBLE) {
                    dashboardActivity.hideFab()
                } else if (scrollY < oldScrollY && dashboardActivity.floatingActionButtonVisiblity() != View.VISIBLE) {
                    dashboardActivity.showFab()
                }
                /*
                                handler.removeCallbacks(runnable)
                                handler.postDelayed( runnable , 100)*/
            } catch (e: Exception) {

            }
        }

    }




    fun fetchWellnessDetail() {


        binding.wellnessProgress.root.visibility = View.VISIBLE
        binding.wellnessErrorPage.root.visibility = View.GONE
        binding.wellnessNoDataTV.root.visibility = View.GONE
        binding.wellnessScroll.visibility = View.GONE


        if (wellnessDetailViewModel.wellnessTracker == ModelUtil.wellnessOthersModelId) {
            wellnessDetailViewModel.getMetricDetail().observe(viewLifecycleOwner) { resp ->
                fetchWellnessDetailOnObserve(resp)
            }
        } else {
            wellnessDetailViewModel.getWellnessDetail().observe(viewLifecycleOwner, Observer { resp ->
                fetchWellnessDetailOnObserve(resp)
            })
        }
    }

    fun fetchWellnessDetailOnObserve(resp: Any?) {
        if (resp != null) {
            binding.wellnessProgress.root.visibility = View.GONE
            wellnessDetailViewModel.isWellnessDetailFetch = true
            if (resp is ArrayList<*>) {
                wellnessDetailViewModel.isWellnessDetailResponseSuccess = true
                wellnessDetailViewModel.wellnessDetailLive.postValue(resp as ArrayList<PatientVitalTracker>)
                updateWellnessTab(resp)
            } else if (resp is APIResponseError) {
                binding.wellnessErrorPage.root.visibility = View.VISIBLE
            }
        }
    }

    private fun updateWellnessTab(patientVitalTrackerList: ArrayList<PatientVitalTracker>) {

        if (patientVitalTrackerList.isNotEmpty()) {


            binding.wellnessScroll.visibility = View.VISIBLE


            if (wellnessDetailViewModel.isDefault && (wellnessDetailViewModel.wellnessTracker == ModelUtil.wellnessBPModelId)) {
                binding.wellnessTabLayout.visibility = View.GONE
                binding.wellnessTabLayout.clearOnTabSelectedListeners()
                setWellnessData(patientVitalTrackerList)
            } else {

                val size = wellnessDetailViewModel.trackerMetricMap?.metricList?.size ?: 0

                if (size == 1) {
                    binding.wellnessTabLayout.visibility = View.GONE
                    binding.wellnessTabLayout.clearOnTabSelectedListeners()
                    setWellnessData(patientVitalTrackerList, wellnessDetailViewModel.trackerMetricMap?.metricList?.firstOrNull()?.metricId)
                } else {
                    binding.wellnessTabLayout.visibility = View.VISIBLE
                    binding.wellnessTabLayout.clearOnTabSelectedListeners()

                    binding.wellnessTabLayout.removeAllTabs()

                    wellnessDetailViewModel.trackerMetricMap?.metricList?.forEach { metric ->

                        val tab = binding.wellnessTabLayout.newTab()
                        tab.text = metric.metricName
                        binding.wellnessTabLayout.addTab(tab)
                    }

                    setTabMode()

                    binding.wellnessTabLayout.addOnTabSelectedListener(object :
                        TabLayout.OnTabSelectedListener {
                        override fun onTabReselected(tab: TabLayout.Tab?) {

                        }

                        override fun onTabUnselected(tab: TabLayout.Tab?) {
                        }

                        override fun onTabSelected(tab: TabLayout.Tab?) {
                            val posn = tab?.position ?: 0
                            setWellnessData(
                                patientVitalTrackerList,
                                wellnessDetailViewModel.trackerMetricMap?.metricList?.get(posn)?.metricId
                            )
                        }
                    })


                    if (wellnessDetailViewModel.wellnessMetric.isBlank()) {
                        setWellnessData(
                            patientVitalTrackerList,
                            wellnessDetailViewModel.trackerMetricMap?.metricList?.firstOrNull()?.metricId
                        )
                    } else {
                        val metric =
                            wellnessDetailViewModel.trackerMetricMap?.metricList?.firstOrNull {
                                it.metricName == wellnessDetailViewModel.wellnessMetric
                            }


                        if (metric != null) {
                            val posn =
                                wellnessDetailViewModel.trackerMetricMap?.metricList?.indexOf(metric)
                                    ?: 0
                            binding.wellnessTabLayout.selectTab(binding.wellnessTabLayout.getTabAt(posn))
                            setWellnessData(
                                patientVitalTrackerList,
                                metric.metricId
                            )
                        } else {
                            setWellnessData(
                                patientVitalTrackerList,
                                wellnessDetailViewModel.trackerMetricMap?.metricList?.firstOrNull()?.metricId
                            )
                        }


                    }
                }

            }

            /*val tempHeadingList  =  getHeadingData(patientVitalTrackerList)
            val gridEntry =  getGridData(tempHeadingList, patientVitalTrackerList)
            wellnessDetailGridAdapter.setWellnessGridEntryList(gridEntry)


            val headingList = arrayListOf<WellnessGridEntry>()
            headingList.add(WellnessGridEntry(entry = getString(R.string.date_str) , isHeading = true))
            headingList.addAll(tempHeadingList.map { WellnessGridEntry(entry = it) })
            wellnessDetailMetricListAdapter.setWellnessGridEntryList(headingList)*/


        } else {
            binding.wellnessScroll.visibility = View.GONE
            binding.wellnessNoDataTV.root.visibility = View.VISIBLE
        }
    }

    private fun setWellnessData(
        patientVitalTrackerList: ArrayList<PatientVitalTracker>,
        metricId: String? = null
    ) {
        val wellnessDataList = arrayListOf<PatientVitalTracker>()
        patientVitalTrackerList.forEach { patientVitalTracker ->

            val wellnessData = PatientVitalTracker()
            wellnessData.tracker = patientVitalTracker.tracker
            wellnessData.notes = patientVitalTracker.notes
            wellnessData.dateOfEntry = patientVitalTracker.dateOfEntry
            wellnessData.patientId = patientVitalTracker.patientId
            wellnessData.patientVitalsList.addAll(patientVitalTracker.patientVitalsList.filter { metricId.isNullOrBlank() || it.metricId == metricId })

            if (wellnessData.patientVitalsList.isNotEmpty()) {
                wellnessDataList.add(wellnessData)
            }
        }

        if (wellnessDataList.isNotEmpty()) {

            setLatestVital(wellnessDataList.first())
            val size = wellnessDetailListAdapter.setPatVitalTrackerList(wellnessDataList)

            if (size > 5) {
                binding.wellnessRVShowMoreTV.visibility = View.VISIBLE
            } else {
                binding.wellnessRVShowMoreTV.visibility = View.GONE
            }

            val graphData = getGraphData(wellnessDataList)
            drawGraph(graphData)
            binding.wellnessListSwitch.setCheckedChangeListener(object : IconSwitch.CheckedChangeListener {
                override fun onCheckChanged(current: IconSwitch.CheckedEnum?) {
                    if (current != null) {
                        if (current == IconSwitch.CheckedEnum.RIGHT) {
                            binding.wellnessChartLayout.visibility = View.VISIBLE
                            binding.wellnessRVLayout.visibility = View.GONE
                        } else {
                            binding.wellnessChartLayout.visibility = View.GONE
                            binding.wellnessRVLayout.visibility = View.VISIBLE
                        }
                    }
                }
            })

            binding.wellnessScroll.visibility = View.VISIBLE
            binding.wellnessNoDataTV.root.visibility = View.GONE
        } else {
            binding.wellnessScroll.visibility = View.GONE
            binding.wellnessNoDataTV.root.visibility = View.VISIBLE
        }
    }


    private fun getGraphData(patVitalTrackerList: ArrayList<PatientVitalTracker>): GraphData {

        val graphData = GraphData()
        val nameList = arrayListOf<String>()
        val unitList = arrayListOf<String>()


        patVitalTrackerList.sortBy { it.dateOfEntry?.millis }


        val lineDataList = arrayListOf<ILineDataSet>()
        patVitalTrackerList.forEach { patVitalTracker ->

            patVitalTracker.patientVitalsList.forEach { patVitals ->
                if (!nameList.contains(patVitals.metricName) && !patVitals.metricName.isNullOrBlank()) {
                    nameList.add(patVitals.metricName ?: "")
                    unitList.add(patVitals.trackedValueUnit ?: "")
                }

                if (!unitList.contains(patVitals.trackedValueUnit) && !patVitals.trackedValueUnit.isNullOrBlank()) {
                    unitList.add(patVitals.trackedValueUnit ?: "")
                }
            }
        }


        var colorIndex = 0

        nameList.forEachIndexed { index, name ->

            var xAxisNum = 2f

            val entryList = arrayListOf<Entry>()

            patVitalTrackerList.forEach { patVitalTracker ->

                val patVital =
                    patVitalTracker.patientVitalsList.firstOrNull { it.metricName == name }

                if (patVital != null) {
                    val trackedValue = patVital.trackedValue?.toFloatOrNull()

                    if (trackedValue != null) {

                        val entry = MDEntry(xAxisNum, trackedValue, index)
                        entry.unitPosn =
                            unitList.indexOf(unitList.firstOrNull { it == patVital.trackedValueUnit })
                        entryList.add(entry)
                    }
                }

                xAxisNum += 4f
            }

            val lineDataSet = LineDataSet(entryList, name)
            lineDataSet.lineWidth = 2.5f
            lineDataSet.circleRadius = 4f

            val activity = requireActivity()

            when (wellnessDetailViewModel.wellnessTracker) {
                ModelUtil.wellnessBPModelId -> {
                    if (index == 0) {
                        lineDataSet.color =
                            ContextCompat.getColor(activity, R.color.systolicAxisColor)
                        lineDataSet.setCircleColor(ContextCompat.getColor(activity, R.color.systolicAxisColor))
                    } else {
                        lineDataSet.color =
                            ContextCompat.getColor(activity, R.color.diastolicAxisColor)
                        lineDataSet.setCircleColor(
                            ContextCompat.getColor(
                                activity,
                                R.color.diastolicAxisColor
                            )
                        )
                    }
                }
                /*  ModelUtil.wellnessTemperatureModelId -> {
                      lineDataSet.color = ContextCompat.getColor(activity, R.color.firstAxisColor)
                      lineDataSet.setCircleColor(ContextCompat.getColor(activity, R.color.firstAxisColor))
                  }

                  ModelUtil.wellnessPulseModelId -> {
                      lineDataSet.color = ContextCompat.getColor(activity, R.color.firstAxisColor)
                      lineDataSet.setCircleColor(ContextCompat.getColor(activity, R.color.firstAxisColor))
                  }

                  ModelUtil.wellnessGeneralModelId -> {
                      lineDataSet.color = ContextCompat.getColor(activity, R.color.firstAxisColor)
                      lineDataSet.setCircleColor(ContextCompat.getColor(activity, R.color.firstAxisColor))
                  }*/

                else -> {
                    lineDataSet.color =
                        ContextCompat.getColor(activity, colorListForGraph[colorIndex])
                    lineDataSet.setCircleColor(
                        ContextCompat.getColor(
                            activity,
                            colorListForGraph[colorIndex]
                        )
                    )

                    colorIndex++

                    if (colorIndex >= colorListForGraph.size) {
                        colorIndex = 0
                    }
                }

                /*
                                ModelUtil.wellnessBloodSugarModelId -> {

                                    if (name.contains(ModelUtil.metricFasting , true)){
                                        lineDataSet.color = ContextCompat.getColor(activity, R.color.fastingAxisColor)
                                        lineDataSet.setCircleColor(ContextCompat.getColor(activity, R.color.fastingAxisColor))
                                    }
                                    else if (name.contains(ModelUtil.metricPostPrandial , true)){
                                        lineDataSet.color = ContextCompat.getColor(activity, R.color.postPrandialAxisColor)
                                        lineDataSet.setCircleColor(ContextCompat.getColor(activity, R.color.postPrandialAxisColor))
                                    }
                                    else if (name.contains(ModelUtil.metricRandom , true)){
                                        lineDataSet.color = ContextCompat.getColor(activity, R.color.randomAxisColor)
                                        lineDataSet.setCircleColor(ContextCompat.getColor(activity, R.color.randomAxisColor))
                                    }
                                }
                */
            }


            lineDataSet.setDrawValues(false)

            lineDataList.add(lineDataSet)
        }

        graphData.dataList = lineDataList
        graphData.dateList = ArrayList(patVitalTrackerList.mapNotNull { it.dateOfEntry })
        graphData.unitTxt = unitList
        graphData.lineNameList = nameList

        patVitalTrackerList.sortByDescending { it.dateOfEntry?.millis }


        return graphData
    }

    private fun drawGraph(graphData: GraphData) {


        if (binding.wellnessChartLayout.visibility == View.VISIBLE) {
            binding.wellnessChartLayout.visibility = View.INVISIBLE
        }
        binding.wellnessChart.clear()
        val xAxis = binding.wellnessChart.xAxis
        val yAxis = binding.wellnessChart.axisLeft
        val xAxisFormatter = MDAxisStringFormatter()

        val activity = requireActivity()

        val typefaceRegular = UIUtil.getFont(activity, R.font.open_sans)
        val typefaceMedium = UIUtil.getFont(activity, R.font.open_sans_medium)


        val labelList =
            ArrayList(graphData.dateList.map { it.toString(ModelUtil.charmDisplayDateFormat()) })
        xAxisFormatter.labelList = labelList
        xAxis.valueFormatter = xAxisFormatter
        //  xAxis.setLabelCount(labelList.size , true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)

        /*  xAxis.axisMinimum = 0f
          xAxis.axisMaximum = ((graphData.dateList.size * 4) + 2).toFloat()
          xAxis.setGranularity(2f)*/
        val xSpan = ((graphData.dateList.size * 4) + 2).toFloat()
        val xi = xSpan / 4 // approximately 6 labels

        //val xGran: Float = Math.max(2f, (Math.round(xi / 2).toFloat() * 2))

        xAxis.granularity = 2f
        xAxis.axisMinimum = 0f
        xAxis.spaceMax
        xAxis.axisMaximum = xSpan
        xAxis.textColor = ContextCompat.getColor(activity, R.color.graphXAxisTxtColor)
        xAxis.textSize = 13f
        xAxis.typeface = typefaceMedium

        // xAxis.spaceMax = 10f

        /* if (wellnessViewModel.wellnessTracker == ModelUtil.wellnessBPModelId){

             val systolLimitLine = LimitLine(120f, "")
             systolLimitLine.lineWidth = 1f
             systolLimitLine.enableDashedLine(15f, 15f, 0f)
             systolLimitLine.lineColor = R.color.systolicAxisColor
             systolLimitLine.isEnabled = true
            *//* systolLimitLine.setLabelPosition(LimitLabelPosition.RIGHT_BOTTOM);
            systolLimitLine.setTextSize(10f);*//*

            val diastolLimitLine = LimitLine(80f, "")
            diastolLimitLine.lineWidth = 1f
            diastolLimitLine.enableDashedLine(15f, 15f, 0f)
            diastolLimitLine.lineColor = R.color.diastolicAxisColor
            diastolLimitLine.isEnabled = true
            *//*diastolLimitLine.labelPosition = LimitLabelPosition.RIGHT_BOTTOM;
            diastolLimitLine.textSize = 10f;*//*

            yAxis.setDrawLimitLinesBehindData(true)


            yAxis.addLimitLine(systolLimitLine)
            yAxis.addLimitLine(diastolLimitLine)
        }*/



        yAxis.setDrawAxisLine(false)
        yAxis.setDrawGridLines(true)


        val l = binding.wellnessChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.typeface = typefaceMedium
        l.textColor = ContextCompat.getColor(activity, R.color.black87)
        l.textSize = 13f
        l.setDrawInside(false)

        l.isEnabled = graphData.dataList.size > 1

        val graphMinMaxValue = ModelUtil.getGraphMinMaxValue(graphData)


        yAxis.axisMaximum = graphMinMaxValue.second
        yAxis.axisMinimum = graphMinMaxValue.first
        yAxis.gridColor = ContextCompat.getColor(activity, R.color.graphGridColor)
        yAxis.enableGridDashedLine(15f, 15f, 0f)

        yAxis.textColor = ContextCompat.getColor(activity, R.color.black87)
        yAxis.textSize = 13f
        yAxis.typeface = typefaceMedium

        val rightAxis = binding.wellnessChart.axisRight

        rightAxis.isEnabled = false
        rightAxis.setDrawAxisLine(false)
        rightAxis.setDrawGridLines(false)


        val unitList = arrayListOf<String>()

        graphData.unitTxt.forEach {
            if (!unitList.contains(it) && it.isNotBlank()) {
                unitList.add(it)
            }
        }

        /* if (unitList.isEmpty()){
             wellnessChartYAxisNameTV.visibility = View.GONE
         }
         else {
             wellnessChartYAxisNameTV.visibility = View.VISIBLE
             wellnessChartYAxisNameTV.text = unitList.joinToString(",")
         }*/


        binding.wellnessChart.invalidate()
        val data = LineData(graphData.dataList)
        //  data.isHighlightEnabled = false


        binding.wellnessChart.moveViewToX(data.xMax)
        val graphMarker = GraphMarkerView(activity, R.layout.item_chart_select_view)


        binding.wellnessChart.isHighlightPerTapEnabled = true

        if (wellnessDetailViewModel.isDefault && wellnessDetailViewModel.wellnessTracker == ModelUtil.wellnessTemperatureModelId) {
            graphMarker.unitTxt = ArrayList(graphData.unitTxt.map { degree + it })
        } else {
            graphMarker.unitTxt = graphData.unitTxt
        }
        graphMarker.labelList = labelList

        graphMarker.chartView = binding.wellnessChart
        binding.wellnessChart.marker = graphMarker

        binding.wellnessChart.description.isEnabled = false

        binding.wellnessChart.zoomOut()

        var prevScaleValue = 1f
        if (prevDataSize != 0) {
            val prevScale = (prevDataSize.toFloat() / 3)

            if (prevScale > 1) {
                val zoomOutFrequency = prevScale - 1
                prevScaleValue = 1 - zoomOutFrequency
            } else {
                val zoomInFrequency = 1 - prevScale
                prevScaleValue = 1 + zoomInFrequency
            }

        }

        prevDataSize = graphData.dateList.size
        binding.wellnessChart.zoom(prevScaleValue, 0f, 0f, 0f)
        binding.wellnessChart.zoom((prevDataSize.toFloat() / 3), 0f, 0f, 0f)

        Log.d(
            "MDCharts",
            "prevScale -> $prevScaleValue ; currentScale -> ${prevDataSize.toFloat() / 3}"
        )
        binding.wellnessChart.setScaleEnabled(false)

        binding.wellnessChart.data = data
        binding.wellnessChart.notifyDataSetChanged()

        binding.wellnessChart.extraBottomOffset = 10f
        binding.wellnessChart.invalidate()


        if (binding.wellnessChartLayout.visibility == View.INVISIBLE) {
            binding.wellnessChartLayout.visibility = View.VISIBLE
        }
    }

    private fun setLatestVital(patVitalTracker: PatientVitalTracker) {


        when (wellnessDetailViewModel.wellnessTracker) {

            ModelUtil.wellnessBPModelId -> {


                binding.wellnessLatestVitalDateTV.text =
                    patVitalTracker.dateOfEntry?.toString(ModelUtil.charmWellnessDateFormatter())
                val systolObj = patVitalTracker.patientVitalsList.firstOrNull {
                    it.metricName?.contains(ModelUtil.metricSystolic, true) ?: false
                }
                val diastolicObj = patVitalTracker.patientVitalsList.firstOrNull {
                    it.metricName?.contains(ModelUtil.metricDiastolic, true) ?: false
                }

                if (systolObj != null || diastolicObj != null) {

                    val systolFormatValue =
                        ModelUtil.getTwoDecimalStr(systolObj?.trackedValue?.toFloatOrNull())
                    val diastolFormatValue =
                        ModelUtil.getTwoDecimalStr(diastolicObj?.trackedValue?.toFloatOrNull())

                    val value = "${systolFormatValue}/${diastolFormatValue}"
                    binding.wellnessLatestVitalTV.text = value.trim()
                    binding.wellnessLatestVitalHint.text = systolObj?.trackedValueUnit
                        ?: diastolicObj?.trackedValueUnit ?: ""
                }
            }

            /* ModelUtil.wellnessTemperatureModelId -> {


                 val temperatureObj = patVitalTracker.patientVitalsList.firstOrNull()

                 if (temperatureObj != null){
                     val temperatureFormatValue = ModelUtil.getTwoDecimalStr(temperatureObj.trackedValue?.toFloatOrNull())

                     wellnessLatestVitalTV.text = temperatureFormatValue
                     wellnessLatestVitalHint.text =  degree + temperatureObj.trackedValueUnit

                 }
             }
             ModelUtil.wellnessPulseModelId -> {

                 val pulseObj = patVitalTracker.patientVitalsList.firstOrNull()

                 if (pulseObj != null){
                     val pulseObjFormatValue = ModelUtil.getTwoDecimalStr(pulseObj.trackedValue?.toFloatOrNull())
                     wellnessLatestVitalTV.text = pulseObjFormatValue
                     wellnessLatestVitalHint.text = pulseObj.trackedValueUnit
                 }
             }

             ModelUtil.wellnessGeneralModelId -> {

                 wellnessLatestVitalDateTV.visibility = View.GONE

                 val pulseObj = patVitalTracker.patientVitalsList.firstOrNull()

                 if (pulseObj != null){
                     val pulseObjFormatValue = ModelUtil.getTwoDecimalStr(pulseObj.trackedValue?.toFloatOrNull())
                     wellnessLatestVitalTV.text = pulseObjFormatValue
                     wellnessLatestVitalHint.text = pulseObj.trackedValueUnit
                 }
             }*/


            else -> {

                binding.wellnessLatestVitalDateTV.text =
                    patVitalTracker.dateOfEntry?.toString(ModelUtil.charmWellnessDateFormatter())

                val othersObj = patVitalTracker.patientVitalsList.firstOrNull()

                if (othersObj != null) {
                    val pulseObjFormatValue =
                        ModelUtil.getTwoDecimalStr(othersObj.trackedValue?.toFloatOrNull())
                    binding.wellnessLatestVitalTV.text = pulseObjFormatValue
                    if (wellnessDetailViewModel.wellnessTracker == ModelUtil.wellnessTemperatureModelId) {
                        binding.wellnessLatestVitalHint.text = degree + othersObj.trackedValueUnit
                    } else {

                        binding.wellnessLatestVitalHint.text = othersObj.trackedValueUnit
                    }
                }
            }
        }
    }


    /*
    private fun getHeadingData(patVitalTrackerList: ArrayList<PatientVitalTracker>): ArrayList<String> {

        val headingList = arrayListOf<String>()

        patVitalTrackerList.forEach {patVitalTracker ->

            patVitalTracker.patientVitalsList.forEach {patVitals ->

                val headingName = getHeading(patVitals)
                if (headingName.isNotBlank() && !headingList.contains(headingName)){
                    headingList.add(headingName)
                }
            }
        }

        return headingList
    }


    private fun getGridData(headingList:ArrayList<String> , patVitalTrackerList: ArrayList<PatientVitalTracker>): ArrayList<WellnessGridEntry>{

        val gridData = arrayListOf<WellnessGridEntry>()

        patVitalTrackerList.forEach {patVitalTracker ->

            val wellnessGridEntry = WellnessGridEntry()
            wellnessGridEntry.isHeading = true
            wellnessGridEntry.entry = patVitalTracker.dateOfEntry?.toString(ModelUtil.charmDisplayDateFormat())
            gridData.add(wellnessGridEntry)
        }

        headingList.forEach {heading ->

            patVitalTrackerList.forEach {patVitalTracker ->
                val patientVitals = patVitalTracker.patientVitalsList.firstOrNull { heading == getHeading(it) }

                val wellnessGridEntry = WellnessGridEntry()
                if (patientVitals != null){
                    wellnessGridEntry.entry = patientVitals.trackedValue
                }
                gridData.add(wellnessGridEntry)
            }
        }

        return gridData
    }
*/


    private fun getHeadingData(patVitalTrackerList: ArrayList<PatientVitalTracker>): ArrayList<String> {

        val headingList = arrayListOf<String>()

        /*  patVitalTrackerList.forEach {patVitalTracker ->

              patVitalTracker.patientVitalsList.forEach {patVitals ->

                  val headingName = getHeading(patVitals)
                  if (headingName.isNotBlank() && !headingList.contains(headingName)){
                      headingList.add(headingName)
                  }
              }
          }*/


        patVitalTrackerList.forEach { patVitalTracker ->
            headingList.add(
                patVitalTracker.dateOfEntry?.toString(ModelUtil.charmDisplayDateFormat()) ?: ""
            )
        }

        return headingList
    }


    private fun getGridData(
        headingList: ArrayList<String>,
        patVitalTrackerList: ArrayList<PatientVitalTracker>
    ): ArrayList<WellnessGridEntry> {

        val gridData = arrayListOf<WellnessGridEntry>()

        patVitalTrackerList.forEach { patVitalTracker ->

            patVitalTracker.patientVitalsList.forEach { patVitals ->

                val headingName = getHeading(patVitals)
                if (headingName.isNotBlank() && !gridData.map { it.entry }.contains(headingName)) {
                    val wellnessGridEntry = WellnessGridEntry()
                    wellnessGridEntry.isHeading = true
                    wellnessGridEntry.entry = headingName
                    gridData.add(wellnessGridEntry)
                }
            }
        }

        val gridHeadingList = arrayListOf<WellnessGridEntry>()
        gridHeadingList.addAll(gridData)
        wellnessDetailViewModel.wellnessGridViewSpan = gridHeadingList.size

        headingList.forEachIndexed { index, heading ->

            val patientVitalTracker = patVitalTrackerList[index]

            gridHeadingList.forEach { gridHeading ->
                val patientVitals = patientVitalTracker.patientVitalsList.firstOrNull {
                    gridHeading.entry == getHeading(it)
                }
                val wellnessGridEntry = WellnessGridEntry()
                if (patientVitals != null) {
                    wellnessGridEntry.entry = patientVitals.trackedValue
                }
                gridData.add(wellnessGridEntry)
            }


            /* patVitalTrackerList.forEach {patVitalTracker ->
                 val patientVitals = patVitalTracker.patientVitalsList.firstOrNull { heading == getHeading(it) }

                 val wellnessGridEntry = WellnessGridEntry()
                 if (patientVitals != null){
                     wellnessGridEntry.entry = patientVitals.trackedValue
                 }
                 gridData.add(wellnessGridEntry)
             }*/
        }



        return gridData
    }


    private fun getHeading(patVitals: PatientVitals): String {
        var headingName = patVitals.metricName ?: ""

        if (headingName.isNotBlank()) {

            if (!patVitals.trackedValueUnit.isNullOrBlank()) {
                headingName += "\n${patVitals.trackedValueUnit}"
            }

        }

        return headingName
    }


    fun setTabMode() {

        val midWidth = resources.getDimension(R.dimen.vitals_detail_tab_width)
        val sWidth = resources.displayMetrics.widthPixels

        //  Log.d("MDTabs" , "minwidth ->${midWidth * binding.wellnessTabLayout.tabCount}; Width -> 4")
        if (midWidth * binding.wellnessTabLayout.tabCount > sWidth || wellnessDetailViewModel.wellnessTracker == getString(R.string.wellness_temperature_toolbar_txt)) {
            //scroll
            binding.wellnessTabLayout.tabMode = TabLayout.MODE_SCROLLABLE
            binding.wellnessTabLayout.minimumWidth =
                resources.getDimension(R.dimen.vitals_detail_tab_width).toInt()

        } else {
            //fill
            binding.wellnessTabLayout.minimumWidth = 0
            binding.wellnessTabLayout.tabMode = TabLayout.MODE_FIXED
        }
    }

}