package com.charm.phr.mphr.ui.flowsheet

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.QuestionnaireRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class FlowsheetListActivityModule {

    @Provides
    fun provideFlowsheetListViewModel(questionnaireRepository: QuestionnaireRepository):
            FlowsheetListViewModel = FlowsheetListViewModel(questionnaireRepository)

    @Provides
    fun provideFlowsheetListViewModelFactory(flowsheetListViewModel: FlowsheetListViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(flowsheetListViewModel)


    @Provides
    fun provideFlowsheetListAdapter():
            FlowsheetListAdapter = FlowsheetListAdapter()
}