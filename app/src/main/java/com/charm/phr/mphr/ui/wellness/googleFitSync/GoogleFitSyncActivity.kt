package com.charm.phr.mphr.ui.wellness.googleFitSync

//
//import android.Manifest
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.util.Log
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.ViewModelProvider
//import com.charm.phr.mphr.R
//import com.charm.phr.mphr.model.Module
//import com.charm.phr.mphr.model.PatientVitalTracker
//import com.charm.phr.mphr.model.PatientVitals
//import com.charm.phr.mphr.model.TrackerMetricMap
//import com.charm.phr.mphr.util.MDBaseActivity
//import com.charm.phr.mphr.util.MDJsonBuilder
//import com.charm.phr.mphr.util.MDParsingHelper
//import com.charm.phr.mphr.util.ModelUtil
//import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.fitness.Fitness
//import com.google.android.gms.fitness.FitnessOptions
//import com.google.android.gms.fitness.data.DataSet
//import com.google.android.gms.fitness.data.DataType.*
//import com.google.android.gms.fitness.data.Field
//import com.google.android.gms.fitness.data.HealthDataTypes.*
//import com.google.android.gms.fitness.request.DataReadRequest
//import com.google.android.gms.fitness.request.SessionReadRequest
//import com.google.android.gms.fitness.result.DataReadResponse
//import com.google.android.gms.tasks.Task
//import com.google.gson.JsonObject
//import com.google.gson.JsonParser
//import org.joda.time.DateTime
//import java.text.DateFormat
//import java.util.*
//import java.util.concurrent.TimeUnit
//import javax.inject.Inject
//
//class GoogleFitSyncActivity : MDBaseActivity() {
//
//
//    val TAG = "GoogleFitSyncActivity"
//
//    private val googleFitModules = arrayListOf(
//        Module(ModelUtil.wellnessBPModelId, R.string.wellness_bp_txt, R.drawable.ic_bp_wellness, position = 0),
//        Module(ModelUtil.wellnessPulseModelId, R.string.wellness_pulse_txt, R.drawable.ic_pulse_rate, position = 1),
//        Module(ModelUtil.wellnessTemperatureModelId, R.string.wellness_temperature_txt, R.drawable.ic_temperature, position = 2),
//        Module(ModelUtil.wellnessBloodSugarModelId, R.string.wellness_blood_sugar_txt, R.drawable.ic_blood_sugar, position = 3),
//        Module(ModelUtil.metricSleep, R.string.wellness_sleep_txt, R.drawable.ic_fitness_sleep, position = 4),
//        //TODO newly added need to change icon
//        Module(ModelUtil.metricStep, R.string.wellness_step_txt, R.drawable.ic_fitness_sleep, position = 5),
//        Module(ModelUtil.metricOxygenSaturation, R.string.wellness_oxygen_saturation_txt, R.drawable.ic_fitness_sleep, position = 6),
//    )
//
//    private val googleFitSelectedModule = arrayListOf<TrackerMetricMap>()
//
//    private val dateFormat = DateFormat.getDateInstance()
//
//    private lateinit var fitnessOptions: FitnessOptions
//
//    @Inject
//    lateinit var viewModelFactory: ViewModelProvider.Factory
//
//    lateinit var googleFitSyncModel:GoogleFitSyncModel
//
//    enum class FitActionRequestCode {
//        READ_ALL_DATA,
//        READ_BLOOD_GLUCOSE,
//        READ_BLOOD_PRESSURE,
//        READ_BODY_TEMPERATURE
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_google_fit_sync)
//        googleFitSyncModel= ViewModelProvider(this , viewModelFactory).get(GoogleFitSyncModel::class.java)

//        UIUtil.applyViewInsertForToolbar(binding.aboutToolbar)//Todo:Change view contextually

//        val trackerMetricListStr = intent.getStringExtra(ModelUtil.wellnessTrackerDetailIntent)
//        val trackerMetricListJson = trackerMetricListStr?.parseStringAsJsonObject()
//        val trackerMetricList = trackerMetricListJson?.let { MDParsingHelper.getVitalTrackerList(it) }
//        getSelectedVitals(trackerMetricList)
//        setGoogleFitDataOptions()
//        setOnClickListener()
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == 0) {
//            fitSignIn(FitActionRequestCode.READ_ALL_DATA)
//        }
//    }
//
//    fun setOnClickListener() {
//        googleFitBackBtn.setOnClickListener {
//            onBackPressed()
//        }
//        googleFitSyncBtn.setOnClickListener {
//
//            //To get steps count need to provide ACTIVITY_RECOGNITION permission if not already granted
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                // Permission is not granted
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
//                    0
//                )
//            } else {
//                fitSignIn(FitActionRequestCode.READ_ALL_DATA)
//            }
//        }
//    }
//
//    //Parsing the vital list and getting the list of enabled vitals
//    private fun getSelectedVitals(trackerMetricList: ArrayList<TrackerMetricMap>?) {
//        trackerMetricList?.forEach { patientVitalTracker ->
//            if (patientVitalTracker.tracker?.isDefaultTracker == true) {
//                if (patientVitalTracker.tracker?.trackerName?.equals(ModelUtil.wellnessOthersModelId, true) == true) {
//                    patientVitalTracker.metricList.forEach { metric ->
//                        val googleFitData = googleFitModules.firstOrNull { it.moduleId.equals(metric.metricName, true) }
//                        if (googleFitData != null) {
//                            val tempPatientVitalTracker = TrackerMetricMap()
//                            tempPatientVitalTracker.tracker = patientVitalTracker.tracker
//                            tempPatientVitalTracker.metricList.add(metric)
//                            tempPatientVitalTracker.toString = patientVitalTracker.toString
//                            googleFitSelectedModule.add(tempPatientVitalTracker)
//                        }
//                    }
//                } else {
//                    val googleFitData = googleFitModules.firstOrNull { it.moduleId.equals(patientVitalTracker.tracker?.trackerName, true) }
//                    if (googleFitData != null) {
//                        googleFitSelectedModule.add(patientVitalTracker)
//                    }
//                }
//            }
//        }
//    }
//
//    //Creating a FitnessOptions instance, declaring the data types and access type (read and/or write) app needs
//    private fun setGoogleFitDataOptions() {
//        val builder = FitnessOptions.builder()
//        googleFitSelectedModule.forEach { patientVitalTracker ->
//            val vitalName: String = if (patientVitalTracker.tracker?.trackerName?.equals(ModelUtil.wellnessOthersModelId, true) == true) {
//                patientVitalTracker.metricList.firstOrNull()?.metricName ?: ""
//            } else {
//                patientVitalTracker.tracker?.trackerName ?: ""
//            }
//            when (vitalName) {
//                ModelUtil.wellnessBPModelId -> {
//                    builder.addDataType(TYPE_BLOOD_PRESSURE, FitnessOptions.ACCESS_READ)
//                }
//                ModelUtil.wellnessPulseModelId -> {
//                    builder.addDataType(TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
//                }
//                ModelUtil.wellnessTemperatureModelId -> {
//                    builder.addDataType(TYPE_BODY_TEMPERATURE, FitnessOptions.ACCESS_READ)
//                }
//                ModelUtil.wellnessBloodSugarModelId -> {
//                    builder.addDataType(TYPE_BLOOD_GLUCOSE, FitnessOptions.ACCESS_READ)
//                }
//                ModelUtil.metricSleep -> {
//                    builder.addDataType(TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
//                }
//                ModelUtil.metricStep -> {
//                    builder.addDataType(AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
//                }
//                ModelUtil.metricOxygenSaturation -> {
//                    builder.addDataType(TYPE_OXYGEN_SATURATION, FitnessOptions.ACCESS_READ)
//                }
//                else -> {
//
//                }
//            }
//
//        }
//        fitnessOptions = builder.build()
//    }
//
//    /**
//     * Checks that the user is signed in, and if so, executes the specified function. If the user is
//     * not signed in, initiates the sign in flow, specifying the post-sign in function to execute.
//     *
//     */
//    private fun fitSignIn(requestCode: FitActionRequestCode) {
//        if (oAuthPermissionsApproved()) {
//            readFitnessData()
//            readSessionData()
//        } else {
//            requestCode.let {
//                GoogleSignIn.requestPermissions(
//                    this,
//                    requestCode.ordinal,
//                    getGoogleAccount(), fitnessOptions
//                )
//            }
//        }
//    }
//
//    private fun oAuthPermissionsApproved() = GoogleSignIn.hasPermissions(getGoogleAccount(), fitnessOptions)
//
//    /**
//     * Gets a Google account for use in creating the Fitness client. This is achieved by either
//     * using the last signed-in account, or if necessary, prompting the user to sign in.
//     *
//     */
//    private fun getGoogleAccount() = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
//
//
//    private fun readSessionData() {
//
//        val dayInMillis = 86400000
//        val endTime = Date().time
//        val startTime = endTime - dayInMillis
//
//        val SLEEP_STAGE_NAMES = arrayOf(
//            "Unused",
//            "Awake (during sleep)",
//            "Sleep",
//            "Out-of-bed",
//            "Light sleep",
//            "Deep sleep",
//            "REM sleep"
//        )
//
//        val request = SessionReadRequest.Builder()
//            .readSessionsFromAllApps()
//            // By default, only activity sessions are included, so it is necessary to explicitly
//            // request sleep sessions. This will cause activity sessions to be *excluded*.
//            .includeSleepSessions()
//            // Sleep segment data is required for details of the fine-granularity sleep, if it is present.
//            .read(TYPE_SLEEP_SEGMENT)
//            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
//            .build()
//        Fitness.getSessionsClient(this, getGoogleAccount()).readSession(request)
//            .addOnSuccessListener { response ->
//                for (session in response.sessions) {
//                    val sessionStart = session.getStartTime(TimeUnit.MILLISECONDS)
//                    val sessionEnd = session.getEndTime(TimeUnit.MILLISECONDS)
//                    val diff = sessionEnd - sessionStart
//                    val hr = TimeUnit.MILLISECONDS.toHours(diff)
//                    Log.d(TAG, "Sleep between $sessionStart and $sessionEnd")
//                    Log.d(TAG, "Sleep between $hr")
//                    // If the sleep session has finer granularity sub-components, extract them:
//                    val dataSets = response.getDataSet(session)
//                    for (dataSet in dataSets) {
//                        for (point in dataSet.dataPoints) {
//                            val sleepStageVal = point.getValue(Field.FIELD_SLEEP_SEGMENT_TYPE).asInt()
//                            val sleepStage = SLEEP_STAGE_NAMES[sleepStageVal]
//                            val segmentStart = point.getStartTime(TimeUnit.MILLISECONDS)
//                            val segmentEnd = point.getEndTime(TimeUnit.MILLISECONDS)
//                            Log.d(TAG, "\t* Type $sleepStage between $segmentStart and $segmentEnd")
//                        }
//                    }
//                }
//            }
//    }
//
//    //Need to remove
//    private fun readDailystepCount() {
//        Fitness.getHistoryClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
//            .readDailyTotal(TYPE_STEP_COUNT_DELTA)
//            .addOnSuccessListener { result ->
//                val totalSteps = result.dataPoints.firstOrNull()?.getValue(Field.FIELD_STEPS)?.asInt() ?: 0
//                Log.d(TAG, "total step $totalSteps")
//            }
//            .addOnFailureListener { e ->
//                Log.d(TAG, "There was a problem getting steps.", e)
//            }
//    }
//
//    /**
//     * Asynchronous task to read the Fitness data
//     */
//
//    private fun readFitnessData(): Task<DataReadResponse> {
//        val readRequest = queryFitnessData()
//        // Invoke the History API to fetch the data with the query
//        return Fitness.getHistoryClient(this, getGoogleAccount())
//            .readData(readRequest)
//            .addOnSuccessListener { dataReadResponse ->
//                loadResult(dataReadResponse)
//            }
//            .addOnFailureListener { e ->
//                Log.e(TAG, "There was a problem reading the data." + e.message, e)
//            }
//    }
//
//    private fun queryFitnessData(): DataReadRequest {
////To get last one month data
////        val monthInMillis = 2592000000
////         val endTime = Date().time
////        val startTime = endTime - monthInMillis
//        //To get for specific Range
////         val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
////         val now = Date()
////         calendar.time = now
////         val endTime = calendar.timeInMillis
////         calendar.add(Calendar.WEEK_OF_YEAR, -1)
////         val startTime = calendar.timeInMillis
//
//        //to get last 24hr data
//        val dayInMillis = 86400000
//        val endTime = Date().time
//        val startTime = endTime - dayInMillis
//
//
//        return DataReadRequest.Builder()
//            .read(TYPE_BLOOD_GLUCOSE)
//            .read(TYPE_BODY_TEMPERATURE)
//            .read(TYPE_BLOOD_PRESSURE)
//            .read(TYPE_HEART_RATE_BPM)
//            .read(TYPE_OXYGEN_SATURATION)
//            .read(TYPE_STEP_COUNT_DELTA)
//            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
//            //.setLimit(1)
//            .build()
//    }
//
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        when (resultCode) {
//            RESULT_OK -> {
//                val postSignInAction = FitActionRequestCode.values()[requestCode]
//                postSignInAction.let {
//                    readFitnessData()
//                    readSessionData()
//                }
//            }
//        }
//    }
//
//    private fun loadResult(dataReadResult: DataReadResponse) {
//        //Need to check any possible case to get bucket in response
////        if (dataReadResult.buckets.isNotEmpty()) {
////            Log.i(TAG, "Number of returned buckets of DataSets is: " + dataReadResult.buckets.size)
////            for (bucket in dataReadResult.buckets) {
////                bucket.dataSets.forEach { dumpDataSet(it) }
////            }
////        } else
//        if (dataReadResult.dataSets.isNotEmpty()) {
//            Log.i(TAG, "Number of returned DataSets is: " + dataReadResult.dataSets.size)
//            dataReadResult.dataSets.forEach { readDataSet(it) }
//            Log.i(TAG, "\tFinal Json ${MDJsonBuilder.formAddMultipleVitalObj(googleFitSyncModel.googleFitSyncVitalDataList,"")}")
//        }
//    }
//
//    private fun readDataSet(dataSet: DataSet) {
//        Log.i(TAG, "Data returned for Data type: ${dataSet.dataType.name}")
//
//        when(dataSet.dataType.name)
//        {
//             "com.google.body.temperature" ->{
//                 val googleFitData = googleFitSelectedModule.firstOrNull { it.tracker?.trackerName.equals(ModelUtil.wellnessTemperatureModelId, true) }
//                 if(googleFitData != null)
//                 {
//                     val patientVitalTracker=PatientVitalTracker()
//                     patientVitalTracker.tracker= googleFitData.tracker
//                     for (dp in dataSet.dataPoints) {
//                         val patientVitals= PatientVitals()
//                         val recordedDateTime=DateTime(dp.getTimestamp(TimeUnit.MILLISECONDS))
//                         patientVitals.dateOfEntry=recordedDateTime
//                         dp.dataType.fields.forEach {
//                             if(it.name == "body_temperature")
//                             {
//                                 patientVitals.metricId=googleFitData.metricList.firstOrNull()?.metricId
//                                 val value=dp.getValue(it)
//                                 if(googleFitData.metricList.firstOrNull()?.metricUnit.equals("C"))
//                                 {
//                                     patientVitals.trackedValue=value.asFloat().toString()
//                                 }
//                                 else
//                                 {
//                                     val convertValue=convertCelsiusTofahrenheit(value.asFloat())
//                                     patientVitals.trackedValue= convertValue.toString()
//                                 }
//                             }
//                         }
//                         patientVitalTracker.patientVitalsList.add(patientVitals)
//                     }
//                     if(dataSet.dataPoints.isNotEmpty())
//                     {
//                         googleFitSyncModel.googleFitSyncVitalDataList.add(patientVitalTracker)
//                     }
//                 }
//             }
//            "com.google.step_count.delta" ->{
//                val googleFitData = googleFitSelectedModule.firstOrNull { it.tracker?.trackerName.equals(ModelUtil.metricStep, true) }
//                if(googleFitData != null)
//                {
//                    val patientVitalTracker=PatientVitalTracker()
//                    patientVitalTracker.tracker=googleFitData.tracker
//                    val patientVitals= PatientVitals()
//                    //TODO Date of entry and get metric
//                    patientVitals.dateOfEntry=DateTime.now().minusDays(1)
//                    var value=0
//                    for (dp in dataSet.dataPoints) {
//                        dp.dataType.fields.forEach {
//                            if(it.name == "steps")
//                            {
//                                value += dp.getValue(it).asInt()
//                            }
//                        }
//                    }
//                    patientVitals.trackedValue= value.toString()
//                    patientVitalTracker.patientVitalsList.add(patientVitals)
//                    if(dataSet.dataPoints.isNotEmpty())
//                    {
//                        googleFitSyncModel.googleFitSyncVitalDataList.add(patientVitalTracker)
//                    }
//                }
//            }
//            "com.google.oxygen_saturation"->{
//                val googleFitData = googleFitSelectedModule.firstOrNull { it.tracker?.trackerName.equals(ModelUtil.metricOxygenSaturation, true) }
//                if(googleFitData != null)
//                {
//                    val patientVitalTracker=PatientVitalTracker()
//                    patientVitalTracker.tracker=googleFitData.tracker
//                    for (dp in dataSet.dataPoints) {
//                        val patientVitals = PatientVitals()
//                        val recordedDateTime = DateTime(dp.getTimestamp(TimeUnit.MILLISECONDS))
//                        patientVitals.dateOfEntry = recordedDateTime
//                        //TODO get metric set metricId
//                        var value = ""
//                        dp.dataType.fields.forEach {
//                            if(it.name == "oxygen_saturation")
//                            {
//                                value=dp.getValue(it).toString()
//                            }
//                        }
//                        patientVitals.trackedValue=value
//                        patientVitalTracker.patientVitalsList.add(patientVitals)
//                    }
//                    if(dataSet.dataPoints.isNotEmpty())
//                    {
//                        googleFitSyncModel.googleFitSyncVitalDataList.add(patientVitalTracker)
//                    }
//                }
//            }
//            "com.google.blood_glucose" ->{
//                val googleFitData = googleFitSelectedModule.firstOrNull { it.tracker?.trackerName.equals(ModelUtil.wellnessBloodSugarModelId, true) }
//                if(googleFitData != null)
//                {
//                    val patientVitalTracker=PatientVitalTracker()
//                    patientVitalTracker.tracker=googleFitData.tracker
//                    for (dp in dataSet.dataPoints) {
//                        val patientVitals= PatientVitals()
//                        val recordedDateTime=DateTime(dp.getTimestamp(TimeUnit.MILLISECONDS))
//                        patientVitals.dateOfEntry=recordedDateTime
//                        var value=""
//                        var metricId=""
//                        dp.dataType.fields.forEach { it ->
//                            if(it.name == "blood_glucose_level")
//                            {
//                                value=convertFromMmolToMG(dp.getValue(it).asFloat()).toString()
//                            }
//                            else if(it.name == "temporal_relation_to_meal")
//                            {
//                                when(dp.getValue(it).toString())
//                                {
//                                    "2"->{
//                                        val metric=googleFitData.metricList.firstOrNull{it.metricName.equals(ModelUtil.metricFasting) }
//                                        metricId=metric?.metricId?:""
//                                    }
//                                    "3"->{
//                                        val metric=googleFitData.metricList.firstOrNull{it.metricName.equals(ModelUtil.metricBeforeMeal) }
//                                        metricId=metric?.metricId?:""
//                                    }
//                                    "4"->{
//                                        val metric=googleFitData.metricList.firstOrNull{it.metricName.equals(ModelUtil.metricPostPrandial) }
//                                        metricId=metric?.metricId?:""
//                                    }
//                                    else ->{
//                                        val metric=googleFitData.metricList.firstOrNull{it.metricName.equals(ModelUtil.metricRandom) }
//                                        metricId=metric?.metricId?:""
//                                    }
//                                }
//                            }
//                        }
//                        patientVitals.metricId=metricId
//                        patientVitals.trackedValue=value
//                        patientVitalTracker.patientVitalsList.add(patientVitals)
//                    }
//                    if(dataSet.dataPoints.isNotEmpty())
//                    {
//                        googleFitSyncModel.googleFitSyncVitalDataList.add(patientVitalTracker)
//                    }
//                }
//            }
//            "com.google.blood_pressure" ->{
//                val googleFitData = googleFitSelectedModule.firstOrNull { it.tracker?.trackerName.equals(ModelUtil.wellnessBPModelId, true) }
//                if(googleFitData != null)
//                {
//                    val patientVitalTracker=PatientVitalTracker()
//                    patientVitalTracker.tracker=googleFitData.tracker
//                    for (dp in dataSet.dataPoints) {
//                        dp.dataType.fields.forEach { it ->
//                            if(it.name == "blood_pressure_systolic")
//                            {
//                                val patientVitals= PatientVitals()
//                                val recordedDateTime=DateTime(dp.getTimestamp(TimeUnit.MILLISECONDS))
//                                patientVitals.dateOfEntry=recordedDateTime
//                                val metric=googleFitData.metricList.firstOrNull{it.metricName.equals(ModelUtil.metricSystolic) }
//                                patientVitals.metricId=metric?.metricId
//                                patientVitals.trackedValue= dp.getValue(it).toString()
//                                patientVitalTracker.patientVitalsList.add(patientVitals)
//                            }
//                            else if(it.name == "blood_pressure_diastolic")
//                            {
//                                val patientVitals= PatientVitals()
//                                val recordedDateTime=DateTime(dp.getTimestamp(TimeUnit.MILLISECONDS))
//                                patientVitals.dateOfEntry=recordedDateTime
//                                val metric=googleFitData.metricList.firstOrNull{it.metricName.equals(ModelUtil.metricDiastolic) }
//                                patientVitals.metricId=metric?.metricId
//                                patientVitals.trackedValue= dp.getValue(it).toString()
//                                patientVitalTracker.patientVitalsList.add(patientVitals)
//                            }
//                        }
//                    }
//                    if(dataSet.dataPoints.isNotEmpty())
//                    {
//                        googleFitSyncModel.googleFitSyncVitalDataList.add(patientVitalTracker)
//                    }
//                }
//            }
//            "com.google.heart_rate.bpm"->{
//                val googleFitData = googleFitSelectedModule.firstOrNull { it.tracker?.trackerName.equals(ModelUtil.wellnessPulseModelId, true) }
//                if(googleFitData != null)
//                {
//                    val patientVitalTracker=PatientVitalTracker()
//                    patientVitalTracker.tracker=googleFitData.tracker
//                    for (dp in dataSet.dataPoints) {
//                        val patientVitals= PatientVitals()
//                        val recordedDateTime=DateTime(dp.getTimestamp(TimeUnit.MILLISECONDS))
//                        patientVitals.dateOfEntry=recordedDateTime
//                        dp.dataType.fields.forEach {
//                            if(it.name == "bpm")
//                            {
//                                patientVitals.metricId=googleFitData.metricList.firstOrNull()?.metricId
//                                patientVitals.trackedValue= dp.getValue(it).toString()
//                            }
//                        }
//                        patientVitalTracker.patientVitalsList.add(patientVitals)
//                    }
//                    if(dataSet.dataPoints.isNotEmpty())
//                    {
//                        googleFitSyncModel.googleFitSyncVitalDataList.add(patientVitalTracker)
//                    }
//                }
//            }
//            else->{
//
//            }
//        }
//        //Code need to be removed
////        for (dp in dataSet.dataPoints) {
////            Log.i(TAG, "Data point:")
////            Log.i(TAG, "\tType: ${dp.dataType.name}")
////            Log.i(TAG, "\tstartTime: ${dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS))}")
////            Log.i(TAG, "\tendTime: ${dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS))}")
////            val recordedDate=DateTime(dp.getTimestamp(TimeUnit.MILLISECONDS))
////            Log.i(TAG, "\tTime: ${recordedDate.toString(ModelUtil.sqlDateTimeFormatForNormalTiming())}")
////            val tempDate=DateTime.parse(recordedDate.toString(ModelUtil.sqlDateTimeFormatForNormalTiming()),ModelUtil.sqlDateTimeFormatForNormalTiming())
////            Log.i(TAG, "\ttempDate: ${tempDate}")
////            Log.i(TAG, "\tTime: ${Date(dp.getTimestamp(TimeUnit.MILLISECONDS))}")
////            dp.dataType.fields.forEach {
////                Log.i(TAG, "\tField: ${it.name} Value: ${dp.getValue(it)}")
////            }
////        }
//    }
//
//    private fun convertCelsiusTofahrenheit(temperature:Float): Double {
//        return (temperature*1.8) + 32
//    }
//
//    private fun convertFromMmolToMG(value : Float): Float {
//        return value*18
//    }
//}