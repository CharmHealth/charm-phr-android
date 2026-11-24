package com.charm.phr.mphr.ui.document.share_document

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.util.Rfc822Tokenizer
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityShareDocumentBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Member
import com.charm.phr.mphr.model.Pharmacy
import com.charm.phr.mphr.model.ShareDocRestriction
import com.charm.phr.mphr.ui.appointment.book_appointment.BookAppointmentMemberListAdapter
import com.charm.phr.mphr.ui.components.MDChipsMultiSelctAutoCompleteEdittext
import com.charm.phr.mphr.ui.contact.add_contact.AddContactActivity
import com.charm.phr.mphr.ui.health_passport.GridSpacingItemDecoration
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.charm.phr.mphr.util.UIUtil
import com.google.gson.JsonObject
import org.joda.time.DateTime
import javax.inject.Inject

class ShareDocumentActivity : MDBaseActivity() {

    lateinit var shareDocumentViewModel: ShareDocumentViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    lateinit var memberListAdapter: BookAppointmentMemberListAdapter

    lateinit var pharmacyMemberListAdapter: ShareDocumentPharmacyMemberListAdapter

    @Inject
    lateinit var shareDocumentListAdapter: ShareDocumentListAdapter

    private lateinit var backPressAlertDialogBuilder : AlertDialog.Builder

    lateinit var binding: ActivityShareDocumentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        shareDocumentViewModel = ViewModelProvider(this, viewModelFactory).get(ShareDocumentViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.documentShareToolbar)
        UIUtil.applyViewInsertForBottomView(binding.documentShareMainLayout)


        memberListAdapter = BookAppointmentMemberListAdapter(this, arrayListOf())
        pharmacyMemberListAdapter = ShareDocumentPharmacyMemberListAdapter(this, arrayListOf())
        val documentListToShareString = intent.getStringExtra(ModelUtil.documentShareString) ?: ""
        val documentListToShareJson = documentListToShareString.parseStringAsJsonObject()
        shareDocumentViewModel.shareDocumentList = MDParsingHelper.formDocumentList(documentListToShareJson ?: JsonObject(), false)
        binding.documentShareRV.layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        binding.documentShareRV.addItemDecoration(
            GridSpacingItemDecoration(
                2,
                resources.getDimensionPixelSize(R.dimen.common_margin_horizontal),
                resources.getDimensionPixelSize(R.dimen.common_margin_horizontal),
                includeEdge = true
            )
        )
        binding.documentShareRV.adapter = shareDocumentListAdapter
        shareDocumentListAdapter.setSharedDocumentList(shareDocumentViewModel.shareDocumentList)
        setAutoCompleteMemberList()
        setAutoCompletePharmacyMemberList()
        getShareDocumentRestriction()
        getPharmacyMemberList()
        getMemberList()
        setOnClickListener()
        setShareDocumentListener()
        setBackPressDialogue()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            val returnedResult = data?.data?.toString()
            if(returnedResult != null)
            {
                val newContacts = MDParsingHelper.formContactListForSearch(returnedResult.parseStringAsJsonObject() ?: JsonObject())
                newContacts?.forEach {
                    binding.documentShareToContactValueTV.append(it.pharmacyName + ":${it.patientPharmaMapId},")
                }
                shareDocumentViewModel.selectedPharmacyMemberList.addAll(newContacts)
                shareDocumentViewModel.pharmacyMemberList.addAll(newContacts)
            }
        }
    }

    private fun setShareDocumentListener() {
        shareDocumentListAdapter.setShareDocumentListener(object : ShareDocumentListAdapter.ShareDocumentListener {

            override fun onDocumentRemove(position: Int) {
                if(shareDocumentViewModel.shareDocumentList.size == 1)
                {
                    onBackClick()
                }
                else
                {
                    val builder = AlertDialog.Builder(this@ShareDocumentActivity)

                    builder.setMessage(R.string.share_document_remove_str)
                    builder.setPositiveButton(R.string.yes_str) { _, _ ->

                        shareDocumentListAdapter.deleteItemFromSharedDocumentList(position)
                        shareDocumentViewModel.shareDocumentList.removeAt(position)
                    }
                    builder.setNegativeButton(R.string.no_str) { _, _ ->

                    }

                    builder.show()
                }
            }

        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    fun onBackClick()
    {
        backPressAlertDialogBuilder.show()
    }
    fun setBackPressDialogue()
    {
        val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    onBackPressed()
                }

                DialogInterface.BUTTON_NEGATIVE -> {

                }
            }
        }
        backPressAlertDialogBuilder = AlertDialog.Builder(this)
        backPressAlertDialogBuilder.setMessage(R.string.do_you_exit_sharing).setPositiveButton(R.string.yes_str, dialogClickListener).setNegativeButton(R.string.no_str, dialogClickListener)
    }

    fun validateData()
    {
        if(binding.shareDocumentTypeMember.isChecked && shareDocumentViewModel.selectedMemberList.isEmpty())
        {
            UIUtil.showToast(applicationContext, getString(R.string.person_select_info), Toast.LENGTH_LONG)
            return
        }
        else if(binding.shareDocumentTypeOthers.isChecked && shareDocumentViewModel.selectedPharmacyMemberList.isEmpty())
        {
            UIUtil.showToast(applicationContext, getString(R.string.person_select_info), Toast.LENGTH_LONG)
            return
        }
        shareData()
    }
    fun shareData()
    {
        if (binding.shareDocumentTypeMember.isChecked) {
            shareDocumentForMember()
        } else if (binding.shareDocumentTypeOthers.isChecked) {
            val expiryDay = binding.documentShareExpiryET.text.toString()
            var expiryDate = DateTime.now()
            when (expiryDay) {
                ModelUtil.documentExpiryFivedays -> {
                    expiryDate = expiryDate.plusDays(5)
                }
                ModelUtil.documentExpiryTendays -> {
                    expiryDate = expiryDate.plusDays(10)
                }
                else -> {
                    expiryDate = expiryDate.plusDays(1)
                }
            }
            shareDocumentForOthers(expiryDate)
        }
    }
    private fun setOnClickListener() {
        binding.documentShareBtn.setOnClickListener {
            validateData()
        }
        binding.shareDocumentTypeOthers.setOnClickListener {
            binding.documentShareToValueTV.visibility = View.GONE
            binding.practiceMemberLabel.visibility = View.GONE

            binding.documentShareToContactValueTV.visibility = View.VISIBLE
            binding.otherMemberLabel.visibility = View.VISIBLE
            binding.documentShareLayout.visibility=View.VISIBLE
            binding.documentShareRV.visibility=View.VISIBLE
            binding.documentShareBtn.visibility=View.VISIBLE
            binding.documentShareNoPractice.visibility=View.GONE

            if (shareDocumentViewModel.pharmacyMemberList.isEmpty())
                getPharmacyMemberList()

            val expiryList =
                arrayListOf(ModelUtil.documentExpiryOneday, ModelUtil.documentExpiryFivedays, ModelUtil.documentExpiryTendays)
            binding.documentShareExpiryET.setItems(expiryList, this)
            binding.documentShareExpiryET.visibility = View.VISIBLE
            binding.documentShareExpiryTV.visibility = View.VISIBLE
        }

        binding.shareDocumentTypeMember.setOnClickListener {
            setTypeMemberView()
        }
        binding.documentShareBackBtn.setOnClickListener {
            onBackClick()
        }
    }

    fun setTypeMemberView()
    {
        if(shareDocumentViewModel.memberList.isNotEmpty())
        {
            binding.documentShareToValueTV.visibility = View.VISIBLE
            binding.practiceMemberLabel.visibility = View.VISIBLE

            binding.documentShareToContactValueTV.visibility = View.GONE
            binding.otherMemberLabel.visibility = View.GONE
            binding.documentShareExpiryET.visibility = View.GONE
            binding.documentShareExpiryTV.visibility = View.GONE
            binding.documentShareNoPractice.visibility=View.GONE
        }
        else
        {
            binding.documentShareLayout.visibility=View.GONE
            binding.documentShareRV.visibility=View.GONE
            binding.documentShareNoPractice.visibility=View.VISIBLE
            binding.documentShareBtn.visibility=View.GONE
            binding.docNoDataRoot.nodataTextView.setText(R.string.share_option_disabled_for_practice)
        }

    }
    private fun shareDocumentForMember() {
        progressDialog.show()
        progressDialog.setMessage(R.string.sharing_str)
        shareDocumentViewModel.shareDocumentForMember().observe(this) { resp ->
            if (resp != null) {
                progressDialog.dismiss()
                if (resp is Boolean) {
                    UIUtil.showToast(applicationContext, getString(R.string.sharing_successfully__str), Toast.LENGTH_LONG)
                    onBackPressed()
                } else if (resp is APIResponseError) {
                    if(resp.errorMsg.equals(ModelUtil.socketException))
                    {
                        UIUtil.showRetryAndCancelDialog(this, R.string.socket_timeout_exception, R.string.retry_str, false) { isRetry ->
                            if (isRetry) {
                                shareDocumentForMember()
                            } else {
                                onBackPressed()
                            }
                        }
                    }
                    else
                    {
                        UIUtil.showRetryAndCancelDialog(this, R.string.requestFailedTryAgain, R.string.retry_str, false) { isRetry ->
                            if (isRetry) {
                                shareDocumentForMember()
                            } else {
                                onBackPressed()
                            }
                        }
                    }

                }
            }
        }
    }

    private fun shareDocumentForOthers(expiryDate: DateTime) {
        progressDialog.show()
        progressDialog.setMessage(R.string.sharing_str)
        shareDocumentViewModel.shareDocumentForOthers(expiryDate).observe(this) { resp ->
            if (resp != null) {
                progressDialog.dismiss()
                if (resp is Boolean) {
                    UIUtil.showToast(applicationContext, getString(R.string.sharing_successfully__str), Toast.LENGTH_LONG)
                    onBackPressed()
                } else if (resp is APIResponseError) {
                    if(resp.errorMsg.equals(ModelUtil.socketException))
                    {
                        UIUtil.showRetryAndCancelDialog(this, R.string.socket_timeout_exception, R.string.retry_str, false) { isRetry ->
                            if (isRetry) {
                                shareDocumentForOthers(expiryDate)
                            } else {
                                onBackPressed()
                            }
                        }
                    }
                    else{
                        UIUtil.showRetryAndCancelDialog(this, R.string.requestFailedTryAgain, R.string.retry_str, false) { isRetry ->
                            if (isRetry) {
                                shareDocumentForOthers(expiryDate)
                            } else {
                                onBackPressed()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setAutoCompletePharmacyMemberList() {
        binding.documentShareToContactValueTV.setAdapter(pharmacyMemberListAdapter)

        binding.documentShareToContactValueTV.setTokenizer(Rfc822Tokenizer())

        binding.documentShareToContactValueTV.setOnTokenChangeListner(object : MDChipsMultiSelctAutoCompleteEdittext.OnTokenChangeListener {
            override fun onTokenAdded(chipEntry: MDChipsMultiSelctAutoCompleteEdittext.ChipEntry?) {
                val memberId = chipEntry?.id ?: ""
                val member = getPharmacyMember(memberId)
                if (member != null) {

                    val filteredList = shareDocumentViewModel.selectedPharmacyMemberList.filter { it.patientPharmaMapId == memberId }


                    filteredList.forEach { filteredMember ->

                        if (!shareDocumentViewModel.selectedPharmacyMemberList.any { it.patientPharmaMapId == filteredMember.patientPharmaMapId }) {
                            shareDocumentViewModel.selectedPharmacyMemberList.add(filteredMember)
                        }
                    }

                    if (!shareDocumentViewModel.selectedPharmacyMemberList.any { it.patientPharmaMapId == member.patientPharmaMapId }) {
                        shareDocumentViewModel.selectedPharmacyMemberList.add(member)
                    }
                    updatePharmacyMemberList()
                }
            }

            override fun onTokenDelete(chipEntry: MDChipsMultiSelctAutoCompleteEdittext.ChipEntry?) {
                val memberId = chipEntry?.id ?: ""
                val member = getPharmacyMember(memberId)

                if (member != null) {

                    val filteredMemberList = shareDocumentViewModel.selectedPharmacyMemberList.filter { it.patientPharmaMapId == member.patientPharmaMapId }

                    filteredMemberList.forEach {
                        shareDocumentViewModel.selectedPharmacyMemberList.remove(it)
                    }

                    if (shareDocumentViewModel.selectedPharmacyMemberList.isEmpty()) {
                        shareDocumentViewModel.pharmacyMemberListForList.clear()
                        shareDocumentViewModel.pharmacyMemberListForList.addAll(shareDocumentViewModel.pharmacyMemberList)
                    } else {
                        updatePharmacyMemberList()
                    }
                    pharmacyMemberListAdapter.setPharmacyMemberList(shareDocumentViewModel.pharmacyMemberListForList)

                }
            }

            override fun onTokensClear() {
                shareDocumentViewModel.selectedPharmacyMemberList.clear()
                shareDocumentViewModel.pharmacyMemberListForList.clear()
                shareDocumentViewModel.pharmacyMemberListForList.addAll(shareDocumentViewModel.pharmacyMemberList)

                pharmacyMemberListAdapter.setPharmacyMemberList(shareDocumentViewModel.pharmacyMemberListForList)
            }

        })

        binding.documentShareToContactValueTV.setAddNewContactListener(object : MDChipsMultiSelctAutoCompleteEdittext.AddNewContactListener {
            override fun onAddContactClick(name: String?, email: String?) {
                val intent = Intent(applicationContext, AddContactActivity::class.java)
                intent.putExtra(ModelUtil.documentShareAddContactFullName, name)
                intent.putExtra(ModelUtil.documentShareAddContactEmail, email)
                this@ShareDocumentActivity.startActivityForResult(intent, 0)
                this@ShareDocumentActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

        })
    }


    private fun setAutoCompleteMemberList() {
        binding.documentShareToValueTV.setAdapter(memberListAdapter)

        binding.documentShareToValueTV.setTokenizer(Rfc822Tokenizer())

        binding.documentShareToValueTV.setOnTokenChangeListner(object : MDChipsMultiSelctAutoCompleteEdittext.OnTokenChangeListener {
            override fun onTokenAdded(chipEntry: MDChipsMultiSelctAutoCompleteEdittext.ChipEntry?) {

                val memberId = chipEntry?.id ?: ""
                val member = getMember(memberId)

                if (member != null) {

                    val filteredList = shareDocumentViewModel.selectedMemberList.filter { it.memberId == memberId }


                    filteredList.forEach { filteredMember ->

                        if (!shareDocumentViewModel.selectedMemberList.any { it.memberId == filteredMember.memberId }) {
                            shareDocumentViewModel.selectedMemberList.add(filteredMember)
                        }
                    }

                    if (!shareDocumentViewModel.selectedMemberList.any { it.memberId == member.memberId }) {
                        shareDocumentViewModel.selectedMemberList.add(member)
                    }
                    updateMemberList()
                    memberListAdapter.setSelectedMemberList(shareDocumentViewModel.selectedMemberList)
                }

            }

            override fun onTokenDelete(chipEntry: MDChipsMultiSelctAutoCompleteEdittext.ChipEntry?) {

                val memberId = chipEntry?.id ?: ""
                val member = getMember(memberId)

                if (member != null) {

                    val filteredMemberList = shareDocumentViewModel.selectedMemberList.filter { it.memberId == member.memberId }

                    filteredMemberList.forEach {
                        shareDocumentViewModel.selectedMemberList.remove(it)
                    }

                    if (shareDocumentViewModel.selectedMemberList.isEmpty()) {
                        shareDocumentViewModel.memberListForList.clear()
                        shareDocumentViewModel.memberListForList.addAll(shareDocumentViewModel.memberList)
                    } else {
                        updateMemberList()
                    }
                    memberListAdapter.setMemberList(shareDocumentViewModel.memberListForList)
                    memberListAdapter.setSelectedMemberList(shareDocumentViewModel.selectedMemberList)

                }
            }

            override fun onTokensClear() {

                shareDocumentViewModel.selectedMemberList.clear()
                shareDocumentViewModel.memberListForList.clear()
                shareDocumentViewModel.memberListForList.addAll(shareDocumentViewModel.memberList)

                memberListAdapter.setMemberList(shareDocumentViewModel.memberListForList)
                memberListAdapter.setSelectedMemberList(shareDocumentViewModel.selectedMemberList)


            }
        })
    }

    private fun getPharmacyMemberList() {
        binding.documentShareScroll.visibility = View.GONE
        binding.documentShareErrorPage.root.visibility = View.GONE
        binding.documentShareProgress.root.visibility = View.VISIBLE
        binding.documentShareBtn.visibility = View.GONE
        shareDocumentViewModel.getPharmacyMembers().observe(this) { resp ->
            if (resp != null) {
                binding.documentShareProgress.root.visibility = View.GONE
                if (resp is ArrayList<*>) {
                    binding.documentShareScroll.visibility = View.VISIBLE
                    binding.documentShareErrorPage.root.visibility = View.GONE
                    shareDocumentViewModel.pharmacyMemberList = resp as ArrayList<Pharmacy>
                    shareDocumentViewModel.pharmacyMemberListForList.clear()
                    shareDocumentViewModel.pharmacyMemberListForList.addAll(shareDocumentViewModel.pharmacyMemberList)
                    pharmacyMemberListAdapter.setPharmacyMemberList(shareDocumentViewModel.pharmacyMemberListForList)
                } else if (resp is APIResponseError) {
                    UIUtil.showRetryAndCancelDialog(this, R.string.requestFailedTryAgain, R.string.retry_str, false) { isRetry ->
                        if (isRetry) {
                            getPharmacyMemberList()
                        } else {
                            onBackPressed()
                        }
                    }
                }
            }
        }
    }

    private fun getMemberList() {
        binding.documentShareScroll.visibility = View.GONE
        binding.documentShareErrorPage.root.visibility = View.GONE
        binding.documentShareProgress.root.visibility = View.VISIBLE
        binding.documentShareBtn.visibility = View.GONE
        shareDocumentViewModel.getMembers().observe(this) { resp ->
            if (resp != null) {
                binding.documentShareProgress.root.visibility = View.GONE
                if (resp is ArrayList<*>) {
                    binding.documentShareScroll.visibility = View.VISIBLE
                    binding.documentShareErrorPage.root.visibility = View.GONE
                    binding.documentShareBtn.visibility = View.VISIBLE
                    val memberlist=resp as ArrayList<Member>
                    shareDocumentViewModel.memberList= memberlist.filter {
                        shareDocumentViewModel.documentShareRestrictionList.contains(it.practiceId)
                    } as ArrayList<Member>
                    shareDocumentViewModel.memberListForList.clear()
                    shareDocumentViewModel.memberListForList.addAll(shareDocumentViewModel.memberList)
                    memberListAdapter.setMemberList(shareDocumentViewModel.memberListForList)
                    setTypeMemberView()
                } else if (resp is APIResponseError) {
                    UIUtil.showRetryAndCancelDialog(this, R.string.requestFailedTryAgain, R.string.retry_str, false) { isRetry ->
                        if (isRetry) {
                            getMemberList()
                        } else {
                            onBackPressed()
                        }
                    }
                }
            }
        }
    }

    private fun getMember(memberId: String): Member? {

        val filteredMemberList = shareDocumentViewModel.memberList.filter { it.memberId == memberId }

        if (filteredMemberList.isNotEmpty()) {
            return filteredMemberList.first()
        }
        return null
    }

    private fun getPharmacyMember(memberId: String): Pharmacy? {

        val filteredMemberList = shareDocumentViewModel.pharmacyMemberList.filter { it.patientPharmaMapId == memberId }

        if (filteredMemberList.isNotEmpty()) {
            return filteredMemberList.first()
        }
        return null
    }

    private fun updateMemberList() {


        val aSelectedMember = shareDocumentViewModel.selectedMemberList.firstOrNull()

        if (aSelectedMember != null) {

            val practiceId = aSelectedMember.practiceId

            val memberListForList =
                ArrayList(shareDocumentViewModel.memberList.filter { it.memberId != aSelectedMember.memberId })


            shareDocumentViewModel.selectedMemberList.forEach { selectedMemberTemp ->

                if (memberListForList.contains(selectedMemberTemp)) {
                    memberListForList.remove(selectedMemberTemp)
                }
            }

            shareDocumentViewModel.memberListForList = memberListForList

            memberListAdapter.setMemberList(memberListForList)

        }
    }

    private fun updatePharmacyMemberList() {


        val aSelectedMember = shareDocumentViewModel.selectedPharmacyMemberList.firstOrNull()

        if (aSelectedMember != null) {

            val memberListForList = ArrayList(shareDocumentViewModel.pharmacyMemberList.filter { it.patientPharmaMapId != aSelectedMember.patientPharmaMapId })


            shareDocumentViewModel.selectedPharmacyMemberList.forEach { selectedMemberTemp ->

                if (memberListForList.contains(selectedMemberTemp)) {
                    memberListForList.remove(selectedMemberTemp)
                }
            }

            shareDocumentViewModel.pharmacyMemberListForList = memberListForList

            pharmacyMemberListAdapter.setPharmacyMemberList(memberListForList)

        }

    }

    private fun getShareDocumentRestriction() {
        binding.documentShareScroll.visibility = View.GONE
        binding.documentShareErrorPage.root.visibility = View.GONE
        binding.documentShareProgress.root.visibility = View.VISIBLE
        binding.documentShareBtn.visibility = View.GONE
        shareDocumentViewModel.getDocumentShareRestriction().observe(this) { resp ->
            if (resp != null) {
                binding.documentShareProgress.root.visibility = View.GONE
                if (resp is ArrayList<*>) {
                    binding.documentShareScroll.visibility = View.VISIBLE
                    binding.documentShareErrorPage.root.visibility = View.GONE
                    val shareDocRestrictionList = resp as ArrayList<ShareDocRestriction>
                    shareDocRestrictionList.forEach {
                        if(it.isShareAllowed)
                        {
                            shareDocumentViewModel.documentShareRestrictionList.add(it.practiceId)
                        }
                    }
                    if(shareDocumentViewModel.documentShareRestrictionList.size >0)
                    {
                        binding.documentShareBtn.visibility = View.VISIBLE
                    }
                } else if (resp is APIResponseError) {
                    UIUtil.showRetryAndCancelDialog(this, R.string.requestFailedTryAgain, R.string.retry_str, false) { isRetry ->
                        if (isRetry) {
                            getShareDocumentRestriction()
                        } else {
                            onBackPressed()
                        }
                    }
                }
            }
        }
    }

}