package com.charm.phr.mphr.ui.flowsheet.entry

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.QuestionnaireRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class FlowsheetDataActivityModule {

    @Provides
    fun provideFlowsheetDataListViewModel(questionnaireRepository: QuestionnaireRepository):
            FlowsheetDataViewModel = FlowsheetDataViewModel(questionnaireRepository)

    @Provides
    fun provideFlowsheetDataListViewModelFactory(flowsheetDataViewModel: FlowsheetDataViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(flowsheetDataViewModel)

    @Provides
    fun provideFlowsheetDataListAdapter():
            FlowsheetDataListAdapter = FlowsheetDataListAdapter()
}