package com.charm.phr.mphr.ui.billing

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.BillingRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
class BillingActivityModule {

    @Provides
    fun provideBillingViewModel(billingRepository: BillingRepository): BillingViewModel = BillingViewModel(billingRepository)

    @Provides
    fun provideBillingViewModelProviderFactory(billingViewModel: BillingViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(billingViewModel)

    @Provides
    fun provideBillingViewPagerAdapter(billingActivity: BillingActivity , appContext: Context):
            BillingViewPagerAdapter = BillingViewPagerAdapter(billingActivity , appContext )

}

//region FragmentProvider

@Module
internal abstract class InvoiceFragmentProvider {

    @ContributesAndroidInjector(modules = [InvoiceFragmentModule::class])
    internal abstract fun provideInvoiceFragment(): InvoiceFragment
}

@Module
internal abstract class ReceiptFragmentProvider {

    @ContributesAndroidInjector(modules = [ReceiptFragmentModule::class])
    internal abstract fun provideReceiptFragment(): ReceiptFragment
}

//endregion

//region FragmentModule

@Module
class InvoiceFragmentModule {
    //

    @Provides
    fun provideInvoiceListAdapter():
            InvoiceListAdapter = InvoiceListAdapter()

}

@Module
class ReceiptFragmentModule {
    //

    @Provides
    fun provideReceiptListAdapter():
            ReceiptListAdapter = ReceiptListAdapter()
}

//endregion