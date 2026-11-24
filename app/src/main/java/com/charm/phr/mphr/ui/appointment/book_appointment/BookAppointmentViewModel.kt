package com.charm.phr.mphr.ui.appointment.book_appointment

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.AppointmentConfig
import com.charm.phr.mphr.model.AppointmentRequest
import com.charm.phr.mphr.model.Facility
import com.charm.phr.mphr.model.Member
import com.charm.phr.mphr.model.PaymentGatewayToken
import com.charm.phr.mphr.model.PracticeList
import com.charm.phr.mphr.model.SlotMetaData
import com.charm.phr.mphr.model.TransactionInfo
import com.charm.phr.mphr.model.VisitTypes
import com.charm.phr.mphr.model.repository.AppointmentRepository
import com.charm.phr.mphr.model.repository.PaymentRepository
import com.charm.phr.mphr.util.ModelUtil
import com.kizitonwose.calendarview.model.CalendarMonth
import org.joda.time.DateTime

class BookAppointmentViewModel(private val appointmentRepository: AppointmentRepository, private val paymentRepository: PaymentRepository) : ViewModel() {

    val TAG = "BookAppointmentViewModel"

    var memberList = arrayListOf<Member>()
    var facilityList = arrayListOf<Facility>()
    var practiceList = arrayListOf<PracticeList>()

    var selectedFacility = Facility()
    var selectedMember = Member()
    var selectedPractice = PracticeList()

    var isFromDashboard = false

    var appointmentConfig: AppointmentConfig? = null

    var selectedVisitType: VisitTypes? = null
    var selectedDate: DateTime? = DateTime.now()
    var selectedCalendarMonth: CalendarMonth? = null

    var lastSelectedSlotStr: String? = null

    var slotsHashMap: HashMap<String, SlotMetaData> = HashMap()
    var isGettingSlots: Boolean = false
    var razorPayPublicToken: String? = null
    var paymentTransaction: TransactionInfo? = null

    var memberListLiveResponse: MediatorLiveData<Any> = MediatorLiveData()
    var memberListStartIndex = 0

    var appointmentRequest = AppointmentRequest()

    var errorInAPI = -1

    /**
     *      First:Selected Pair(slotViewTag,slotTime) slotViewTag -> Int, slotTime -> String
     *      Second:Selected Session ->Int i.e - Morning,Noon,Evening,Night
     *      Third:Selected Date -> String (only for future appointment)
     */
    var selectedTimeSlotAndDate: Triple<Int?, Int?, String?> = Triple(null, null, null)

    var cardOnFileList: ArrayList<PaymentGatewayToken> = arrayListOf()

    var selectedCardDetail: PaymentGatewayToken? = null

    var isFromEasyTeleCareWebView:Boolean=false

    fun getAppointmentConfig(): LiveData<Any> {
        return appointmentRepository.getAppointmentConfig(selectedMember.memberId ?: "", selectedFacility)
    }

    fun getSlots(selectedDateForSlot: DateTime): LiveData<Any> {

        var interval = appointmentConfig?.interval ?: 0
        var visitTypeId = ""

        if (selectedVisitType != null) {
            visitTypeId = selectedVisitType?.visittypeId ?: ""
            interval = selectedVisitType?.duration ?: 0
        }

        return appointmentRepository.getAppointmentSlots(selectedMember.memberId ?: "", selectedFacility, selectedDateForSlot, visitTypeId, interval)
    }

    fun getMemberList() {

        memberListLiveResponse.addSource(appointmentRepository.getMembersList(selectedFacility, memberListStartIndex)) { response ->
            if (response != null) {
                if (response is ArrayList<*>) {
                    Log.d(TAG, "response.count ${response.count()}")
                    Log.d(TAG, "memberListStartIndex ${memberListStartIndex}")
                    if (response.isEmpty()) {
                        memberListStartIndex = 0
                        memberListLiveResponse.postValue(memberList)
                    } else {
                        memberList.addAll(response as ArrayList<Member>)
                        if (memberListStartIndex == 0) { //TODO:Test after api update
                            memberListStartIndex += 1
                        }
                        memberListStartIndex += ModelUtil.getMembersListCountPerPage
                        getMemberList()
                    }
                } else {
                    memberListStartIndex = 0
                    memberListLiveResponse.postValue(response)
                }
            }
        }
    }

    fun getFacilityList(): LiveData<Any> {
        return appointmentRepository.getFacilityList(selectedPractice)
    }

    fun bookAppointment(appointmentRequest: AppointmentRequest): LiveData<Any> {
        var interval = appointmentConfig?.interval ?: 0

        if (selectedVisitType != null) {
            interval = selectedVisitType?.duration ?: 0
        }

        appointmentRequest.duration = interval


        /*if (appointmentRequest.paymentInfo != null){
           paymentRepository.makeAppointmentPayment(appointmentRequest.paymentInfo!!)
       }
       else{*/
        return appointmentRepository.bookAppointment(appointmentRequest)

        // }

    }

    suspend fun createPaymentForBluefin(): Any?{
        return paymentRepository.makeBluefinPayment(appointmentRequest.paymentInfo!!)
    }

    suspend fun saveBluefinCardDetails():Any? {
        return paymentRepository.saveBluefinCardDetails(appointmentRequest.paymentInfo!!, null)
    }

    suspend fun checkTransactionStatus(): Any? {
        return paymentRepository.checkTransactionStatus(appointmentRequest.paymentInfo!!)
    }

    fun createTransactionForRazorpay(transactionInfo: TransactionInfo): LiveData<Any> {
        return paymentRepository.createPaymentTransaction(transactionInfo)
    }

    fun updateTransactionForRazorpay(transactionID: String): LiveData<Any> {
        return paymentRepository.updatePaymentTransaction(transactionID, selectedFacility.practiceId ?: "")
    }

    fun getCardOnFileForPractice(practiceList: PracticeList, merchantAccountId: String?): LiveData<Any> {
        return paymentRepository.getCardOnFileForPractice(practiceList, merchantAccountId)
    }


}