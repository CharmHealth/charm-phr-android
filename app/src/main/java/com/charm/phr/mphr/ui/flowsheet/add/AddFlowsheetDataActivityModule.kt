package com.charm.phr.mphr.ui.flowsheet.add

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.QuestionnaireRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class AddFlowsheetDataActivityModule {

    @Provides
    fun provideAddFlowsheetDataViewModel(questionnaireRepository: QuestionnaireRepository):
            AddFlowsheetDataViewModel = AddFlowsheetDataViewModel(questionnaireRepository)

    @Provides
    fun provideAddFlowsheetDataViewModelFactory(addFlowsheetDataViewModel: AddFlowsheetDataViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(addFlowsheetDataViewModel)
}