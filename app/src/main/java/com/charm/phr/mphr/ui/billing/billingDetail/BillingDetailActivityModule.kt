package com.charm.phr.mphr.ui.billing.billingDetail

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.BillingRepository
import com.charm.phr.mphr.model.repository.PaymentRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class BillingDetailActivityModule {

    @Provides
    fun provideBillingDetailViewModel(billingRepository: BillingRepository , paymentRepository: PaymentRepository):
            BillingDetailViewModel = BillingDetailViewModel(billingRepository , paymentRepository)

    @Provides
    fun provideBillingDetailViewModelProviderFactory(billingDetailViewModel: BillingDetailViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(billingDetailViewModel)

}