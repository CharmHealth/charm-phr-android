package com.charm.phr.mphr.ui.visitSummary

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityVisitSummaryBinding
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.UIUtil
import javax.inject.Inject

class VisitSummaryActivity : MDBaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var visitSummaryViewModel: VisitSummaryViewModel

    lateinit var binding: ActivityVisitSummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityVisitSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        visitSummaryViewModel = ViewModelProvider(this, viewModelFactory).get(VisitSummaryViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.visitSummaryToolbar)
        UIUtil.applyViewInsertForBottomView(binding.visitSummaryRootLayout)

        val visitSummaryListFragment = VisitSummaryListFragment()

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.visitSummaryMainLayout, visitSummaryListFragment , "visitSummaryListFragment")
        fragmentTransaction.commit()


        binding.visitSummaryBackBtn.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
