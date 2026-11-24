package com.charm.phr.mphr.ui.cardonfile

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.PaymentRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class CardOnFileActivityModule {

    @Provides
    fun provideCardOnFileViewModel(paymentRepository: PaymentRepository): CardOnFileViewModel = CardOnFileViewModel(paymentRepository)

    @Provides
    fun provideCardOnFileViewModelProviderFactory(cardOnFileViewModel: CardOnFileViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(cardOnFileViewModel)

    @Provides
    fun provideCardOnFileListAdapter():
            CardOnFileListAdapter = CardOnFileListAdapter()

}