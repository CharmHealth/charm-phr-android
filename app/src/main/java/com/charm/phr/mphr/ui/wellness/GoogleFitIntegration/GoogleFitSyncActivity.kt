//package com.charm.phr.mphr.ui.wellness.GoogleFitIntegration
//
//import android.content.Intent
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.util.Log
//import com.charm.phr.mphr.R
//import com.charm.phr.mphr.util.ModelUtil
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.fitness.Fitness
//import com.google.android.gms.fitness.FitnessOptions
//import com.google.android.gms.fitness.data.DataSet
//import com.google.android.gms.fitness.data.DataType.*
//import com.google.android.gms.fitness.data.HealthDataTypes.*
//import com.google.android.gms.fitness.request.DataReadRequest
//import com.google.android.gms.fitness.result.DataReadResponse
//import com.google.android.gms.tasks.Task
//import org.joda.time.DateTime
//import java.text.DateFormat
//import java.util.*
//import java.util.concurrent.TimeUnit
//
//class GoogleFitSyncActivity : AppCompatActivity() {
//    val TAG="GoogleFitSyncActivity"
//    private val dateFormat = DateFormat.getDateInstance()
//
//    //Creating a FitnessOptions instance, declaring the data types and access type (read and/or write) app needs
//    private val fitnessOptions: FitnessOptions by lazy {
//        FitnessOptions.builder()
//            .addDataType(TYPE_BLOOD_GLUCOSE, FitnessOptions.ACCESS_READ)
//            .addDataType(TYPE_BLOOD_PRESSURE, FitnessOptions.ACCESS_READ)
//            .addDataType(TYPE_BODY_TEMPERATURE, FitnessOptions.ACCESS_READ)
//            .addDataType(TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
//            .addDataType(TYPE_HEIGHT, FitnessOptions.ACCESS_READ)
//            .addDataType(TYPE_WEIGHT, FitnessOptions.ACCESS_READ)
//            .addDataType(TYPE_BODY_FAT_PERCENTAGE, FitnessOptions.ACCESS_READ)
//            .build()
//    }
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
//
//        googleFitBackBtn.setOnClickListener{
//            onBackPressed()
//        }
//        googleFitSyncBtn.setOnClickListener{
//            fitSignIn(FitActionRequestCode.READ_ALL_DATA)
//        }
//    }
//
//    override fun onBackPressed() {
//        super.onBackPressed()
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
//        } else {
//            requestCode.let {
//                GoogleSignIn.requestPermissions(
//                    this,
//                    requestCode.ordinal,
//                    getGoogleAccount(), fitnessOptions)
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
//    /**
//     * Asynchronous task to read the Fitness data
//     */
//    private fun readFitnessData(): Task<DataReadResponse> {
//        val readRequest = queryFitnessData()
//
//        // Invoke the History API to fetch the data with the query
//        return Fitness.getHistoryClient(this, getGoogleAccount())
//            .readData(readRequest)
//            .addOnSuccessListener { dataReadResponse ->
//                printData(dataReadResponse)
//            }
//            .addOnFailureListener { e ->
//                Log.e(TAG, "There was a problem reading the data."+e.message, e)
//            }
//    }
//
//    private fun queryFitnessData(): DataReadRequest {
//
//        val monthInMillis = 2592000000
//        val endTime = Date().time
//        val startTime = endTime - monthInMillis
////        val startTime= DateTime.now().minusDays(1).millisOfDay().withMinimumValue().millis
////        val endTime =    DateTime.now().minusDays(1).millisOfDay().withMaximumValue().millis
////To get for specific Range
////         val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
////         val now = Date()
////         calendar.time = now
////         val endTime = calendar.timeInMillis
////         calendar.add(Calendar.WEEK_OF_YEAR, -1)
////         val startTime = calendar.timeInMillis
//
//
//        return DataReadRequest.Builder()
//            .read(TYPE_BLOOD_GLUCOSE)
//            .read(TYPE_BODY_TEMPERATURE)
//            .read(TYPE_BLOOD_PRESSURE)
//            .read(TYPE_HEART_RATE_BPM)
//            .read(TYPE_HEIGHT)
//            .read(TYPE_WEIGHT)
//            .read(TYPE_BODY_FAT_PERCENTAGE)
//            //Response Grouped by Day
//            .bucketByTime(1, TimeUnit.DAYS)
//            //.bucketByActivityType(1, TimeUnit.SECONDS)
//            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
//            //To get the last value
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
//                }
//            }}
//    }
//
//    private fun printData(dataReadResult: DataReadResponse) {
//        if (dataReadResult.buckets.isNotEmpty()) {
//            Log.i(TAG, "Number of returned buckets of DataSets is: " + dataReadResult.buckets.size)
//            for (bucket in dataReadResult.buckets) {
//                bucket.dataSets.forEach { dumpDataSet(it) }
//            }
//        } else if (dataReadResult.dataSets.isNotEmpty()) {
//            Log.i(TAG, "Number of returned DataSets is: " + dataReadResult.dataSets.size)
//            dataReadResult.dataSets.forEach { dumpDataSet(it) }
//        }
//    }
//
//    private fun dumpDataSet(dataSet: DataSet) {
//        Log.i(TAG, "Data returned for Data type: ${dataSet.dataType.name}")
//
//        for (dp in dataSet.dataPoints) {
//            Log.i(TAG, "Data point:")
//            Log.i(TAG, "\tType: ${dp.dataType.name}")
//            Log.i(TAG, "\tstartTime: ${dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS))}")
//            Log.i(TAG, "\tendTime: ${dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS))}")
//            Log.i(TAG, "\tTime: ${Date(dp.getTimestamp(TimeUnit.MILLISECONDS))}")
//            Log.i(TAG, "\tTime in : ${ModelUtil.charmFlowsheetDateTimeFieldFormat.format(dp.getTimestamp(TimeUnit.MILLISECONDS))}")
//            dp.dataType.fields.forEach {
//                Log.i(TAG, "\tField: ${it.name} Value: ${dp.getValue(it)}")
//            }
//        }
//    }
//}