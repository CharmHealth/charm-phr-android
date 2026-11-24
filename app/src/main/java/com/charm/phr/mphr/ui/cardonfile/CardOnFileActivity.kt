package com.charm.phr.mphr.ui.cardonfile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityCardOnFileBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.CardOnFilePOJO
import com.charm.phr.mphr.model.PaymentGatewayToken
import com.charm.phr.mphr.model.PracticeList
import com.charm.phr.mphr.ui.cardonfile.addOrEditCof.AddOrEditCardOnFileActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.charm.phr.mphr.util.UIUtil
import com.charm.phr.mphr.util.getString
import com.google.android.material.tabs.TabLayout
import javax.inject.Inject

class CardOnFileActivity : MDBaseActivity() {

    val TAG = "CardOnFileActivity"

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var cardOnFileViewModel: CardOnFileViewModel
    lateinit var binding: ActivityCardOnFileBinding

    @Inject
    lateinit var cardOnFileListAdapter: CardOnFileListAdapter

    private var popUpMenu: PopupMenu? = null

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                val isEdit = data.getBooleanExtra(ModelUtil.cardEditIntent, false)
                if (isEdit) {
                    val responseStr = data.getStringExtra(ModelUtil.cardEditDataIntent)
                    val responseJson = responseStr?.parseStringAsJsonObject()
                    if (responseJson != null) {
                        val responseObj = MDParsingHelper.getPaymentGatewayToken(responseJson, responseJson.getString("practice_id") ?: "")
                        val position = data.getIntExtra(ModelUtil.cardEditPositionIntent, 0)
                        if (cardOnFileViewModel.isPracticeTabSelected) {
                            val tempPaymentGatewayToken = cardOnFileViewModel.practiceCardList[position]
                            responseObj.allowPracticeCardDeletion = tempPaymentGatewayToken.allowPracticeCardDeletion
                            cardOnFileViewModel.practiceCardList[position] = responseObj
                        } else {
                            val tempPaymentGatewayToken = cardOnFileViewModel.patientCardList[position]
                            responseObj.allowPracticeCardDeletion = tempPaymentGatewayToken.allowPracticeCardDeletion
                            cardOnFileViewModel.patientCardList[position] = responseObj
                        }
                        cardOnFileListAdapter.updateCard(position, responseObj)
                    }
                } else {
                    refreshRecyclerView()
                }
            }
        }
    }

    //region life cycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCardOnFileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cardOnFileViewModel = ViewModelProvider(this, viewModelFactory)[CardOnFileViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(binding.cofToolbar)
        UIUtil.applyViewInsertForBottomView(binding.cofMainLayout)

        val selectedPatient = authManager.selectedPatientLive.value
        if (selectedPatient != null) {
            val practiceList = ModelUtil.getBluefinEnabledPractice(selectedPatient)
            if (!practiceList.isNullOrEmpty()) {
                cardOnFileViewModel.bluefinEnabledPractices = practiceList
                binding.cofFilterBtn.visibility = View.VISIBLE
                setUpFilterPopUp()
            }
        } else {
            //TODO:Show error view
        }

        binding.cofRefreshLayout.setColorSchemeResources(R.color.colorAccent)
        getCardOnFileList()
        setUpTabView()
        setUpRecyclerView()
        setOnClickListener()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }


    //endregion


    fun setOnClickListener() {
        binding.cofAddBtn.setOnClickListener {
            showAddOrEditCof(false, "", 0)
        }
        binding.cofBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.cofFilterBtn.setOnClickListener {
            popUpMenu?.show()
        }
        binding.cofRefreshLayout.setOnRefreshListener {
            binding.cofRefreshLayout.isRefreshing = true
            refreshRecyclerView()
        }
    }


    private fun showAddOrEditCof(isEdit: Boolean, editData: String, position: Int) {
        try {
            val intent = Intent(this, AddOrEditCardOnFileActivity::class.java)
            intent.putExtra(ModelUtil.cardEditIntent, isEdit)
            intent.putExtra(ModelUtil.cardEditDataIntent, editData)
            intent.putExtra(ModelUtil.cardEditPositionIntent, position)
            val selectedPractice = cardOnFileViewModel.bluefinEnabledPractices[cardOnFileViewModel.selectedPracticePosition]
            val selectedCardOnFile = cardOnFileViewModel.cardOnFilePOJOArrList.first { cardOnFilePOJO ->
                cardOnFilePOJO.practiceId == selectedPractice.practiceId
            }
            intent.putExtra(ModelUtil.cardEditAddressIntent, MDJsonBuilder.formAddressJson(selectedCardOnFile.postalAddress).toString())
            intent.putExtra(ModelUtil.cardEditCountryIntent, selectedCardOnFile.countryStr)
            intent.putExtra(ModelUtil.cardEditPracticeIdIntent, selectedCardOnFile.practiceId)
            resultLauncher.launch(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }

    //region UI Logic
    private fun setUpTabView() {
        binding.cofTabLayout.addTab(binding.cofTabLayout.newTab().setText(R.string.practice_saved_card))
        binding.cofTabLayout.addTab(binding.cofTabLayout.newTab().setText(R.string.patient_saved_card))
        binding.cofTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    binding.cofAddBtn.visibility = View.GONE
                    cardOnFileViewModel.isPracticeTabSelected = true
                    renderRecyclerViewDataBasedOnFilter(cardOnFileViewModel.practiceCardList)
                } else if (tab?.position == 1) {
                    binding.cofAddBtn.visibility = View.VISIBLE
                    cardOnFileViewModel.isPracticeTabSelected = false
                    renderRecyclerViewDataBasedOnFilter(cardOnFileViewModel.patientCardList)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
    }

    private fun setUpRecyclerView() {
        binding.cofRecyclerView.isVisible = true
        binding.cofRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        cardOnFileListAdapter.setPracticeCardOnFileListener(object : CardOnFileListAdapter.PracticeCardOnFileListener {
            override fun onDeleteCard(paymentGatewayToken: PaymentGatewayToken, position: Int) {
                deleteCard(paymentGatewayToken, position)
            }

            override fun onEditCard(paymentGatewayToken: PaymentGatewayToken, position: Int) {
                val paymentGatewayTokenJson = MDJsonBuilder.formPaymentGatewayTokenObj(paymentGatewayToken)
                showAddOrEditCof(true, paymentGatewayTokenJson.toString(), position)
            }

        })
        binding.cofRecyclerView.adapter = cardOnFileListAdapter
        binding.cofRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, scrollX: Int, scrollY: Int) {
                super.onScrolled(recyclerView, scrollX, scrollY)
                try {
                    if (!cardOnFileViewModel.isPracticeTabSelected) {
                        if (scrollY > 0 && binding.cofAddBtn.visibility == View.VISIBLE) {
                            binding.cofAddBtn.hide()
                        } else if (scrollY < 0 && binding.cofAddBtn.visibility != View.VISIBLE) {
                            binding.cofAddBtn.show()
                        }
                    }
                } catch (e: Exception) {

                }
            }
        })
    }


    private fun setUpFilterPopUp() {
        popUpMenu = PopupMenu(this, binding.cofFilterBtnAnchor, Gravity.START)
        popUpMenu?.inflate(R.menu.menu_card_on_file_filter)
        cardOnFileViewModel.bluefinEnabledPractices.forEachIndexed { index, practice ->
            popUpMenu?.menu?.add(Menu.NONE, index, Menu.NONE, practice.practiceName)
        }
        popUpMenu?.setOnMenuItemClickListener { menuItem ->
            setUpdatedDataToCardOnFileList()
            cardOnFileViewModel.selectedPracticePosition = menuItem.itemId
            showFilteredData(menuItem.itemId)
            return@setOnMenuItemClickListener true
        }
    }

    fun deleteCard(paymentGatewayToken: PaymentGatewayToken, position: Int) {
        val builder = AlertDialog.Builder(this)

        builder.setMessage(R.string.card_delete_confirmation_msg)
        builder.setPositiveButton(R.string.yes_str) { _, _ ->
            progressDialog.setMessage(R.string.sending_data_str)
            progressDialog.show()
            deleteCardOnFile(paymentGatewayToken, position)
        }
        builder.setNegativeButton(R.string.no_str) { _, _ ->

        }

        builder.show()

    }

    private fun refreshRecyclerView() {
        try {
            val selectedPractice = cardOnFileViewModel.bluefinEnabledPractices[cardOnFileViewModel.selectedPracticePosition]
            getCardOnFileForPractice(selectedPractice)
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }
    //endregion

    //region renderDataLogic
    private fun setUpdatedDataToCardOnFileList() {
        try {
            val selectedPractice = cardOnFileViewModel.bluefinEnabledPractices[cardOnFileViewModel.selectedPracticePosition]
            val selectedCardOnFile = cardOnFileViewModel.cardOnFilePOJOArrList.first { cardOnFilePOJO ->
                cardOnFilePOJO.practiceId == selectedPractice.practiceId
            }
            selectedCardOnFile.patientSavedCardArrayList = cardOnFileViewModel.patientCardList
            selectedCardOnFile.practiceSavedCardArrayList = cardOnFileViewModel.practiceCardList
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }

    private fun showFilteredData(position: Int) {
        val selectedPractice = cardOnFileViewModel.bluefinEnabledPractices[position]
        val selectedPracticeCardList = cardOnFileViewModel.cardOnFilePOJOArrList.first {
            it.practiceId == selectedPractice.practiceId
        }
        cardOnFileViewModel.practiceCardList = selectedPracticeCardList.practiceSavedCardArrayList
        cardOnFileViewModel.patientCardList = selectedPracticeCardList.patientSavedCardArrayList
        if (cardOnFileViewModel.isPracticeTabSelected) {
            renderRecyclerViewDataBasedOnFilter(cardOnFileViewModel.practiceCardList)
        } else {
            renderRecyclerViewDataBasedOnFilter(cardOnFileViewModel.patientCardList)
        }
        binding.cofPracticeNameTv.text = selectedPractice.practiceName
    }

    private fun renderRecyclerViewDataBasedOnFilter(paymentGatewayTokenList: ArrayList<PaymentGatewayToken>) {
        if (paymentGatewayTokenList.isEmpty()) {
            binding.cofNoDataLayout.root.visibility = View.VISIBLE
            if (cardOnFileViewModel.isPracticeTabSelected) {
                binding.cofNoDataLayout.nocardTextViewHint.visibility = View.GONE
            } else {
                binding.cofNoDataLayout.nocardTextViewHint.visibility = View.VISIBLE
            }
            binding.cofPracticeNameTv.visibility = View.GONE
        } else {
            binding.cofNoDataLayout.root.visibility = View.GONE
            binding.cofPracticeNameTv.visibility = View.VISIBLE
            cardOnFileListAdapter.setDataList(paymentGatewayTokenList)
        }
    }
    //endregion


    //region Network calls

    private fun getCardOnFileForPractice(practiceList: PracticeList) {
        progressDialog.setMessage(R.string.loading_data)
        progressDialog.show()
        cardOnFileViewModel.getCardOnFileForPractice(practiceList).observe(this) {
            if (it != null) {
                progressDialog.dismiss()
                if (it is CardOnFilePOJO) {
                    cardOnFileViewModel.patientCardList = it.patientSavedCardArrayList
                    cardOnFileViewModel.practiceCardList = it.practiceSavedCardArrayList
                    if (!cardOnFileViewModel.isPracticeTabSelected) {
                        renderRecyclerViewDataBasedOnFilter(cardOnFileViewModel.patientCardList)
                    } else {
                        renderRecyclerViewDataBasedOnFilter(cardOnFileViewModel.practiceCardList)
                    }
                    if (binding.cofRefreshLayout.isRefreshing) {
                        binding.cofRefreshLayout.isRefreshing = false
                    }
                } else if(it is APIResponseError) {
                    UIUtil.showRetryAndCancelDialog(this,it.errorMsg?:getString( R.string.requestFailedTryAgain), R.string.retry_str, false) { isRetry ->
                        if (isRetry) {
                            getCardOnFileForPractice(practiceList)
                        } else {
                            onBackPressed()
                        }
                    }
                }
            }
        }
    }

    private fun getCardOnFileList() {
        progressDialog.setMessage(R.string.loading_data)
        progressDialog.show()
        cardOnFileViewModel.getCardOnFileList(cardOnFileViewModel.bluefinEnabledPractices).observe(this) {
            if (it != null) {
                progressDialog.dismiss()
                if (it is ArrayList<*>) {
                    cardOnFileViewModel.cardOnFilePOJOArrList = it as? ArrayList<CardOnFilePOJO> ?: ArrayList()
                    showFilteredData(0)
                } else if (it is APIResponseError) {
                    UIUtil.showRetryAndCancelDialog(this, it.errorMsg ?: getString(R.string.requestFailedTryAgain), R.string.retry_str, false) { isRetry ->
                        if (isRetry) {
                            getCardOnFileList()
                        } else {
                            onBackPressed()
                        }
                    }
                }
            }
        }
    }

    private fun deleteCardOnFile(paymentGatewayToken: PaymentGatewayToken, position: Int) {
        progressDialog.setMessage(R.string.update_data_str)
        progressDialog.show()
        cardOnFileViewModel.deleteCardOnFile(paymentGatewayToken).observe(this) {
            if (it != null) {
                progressDialog.dismiss()
                if (it is Boolean && it) {
                    val size = cardOnFileListAdapter.removeCard(position)
                    if (cardOnFileViewModel.isPracticeTabSelected) {
                        cardOnFileViewModel.practiceCardList.remove(paymentGatewayToken)
                    } else {
                        cardOnFileViewModel.patientCardList.remove(paymentGatewayToken)
                    }
                    if (size == 0) {
                        binding.cofNoDataLayout.root.visibility = View.VISIBLE
                        binding.cofPracticeNameTv.visibility = View.GONE
                    }
                    UIUtil.showToast(this, R.string.card_delete_success_msg, Toast.LENGTH_LONG)
                } else if (it is APIResponseError) {
                    UIUtil.showToast(this, it.errorMsg ?: getString(R.string.error_card_delete__msg), Toast.LENGTH_LONG)
                }
            }
        }
    }


    //endregion


}