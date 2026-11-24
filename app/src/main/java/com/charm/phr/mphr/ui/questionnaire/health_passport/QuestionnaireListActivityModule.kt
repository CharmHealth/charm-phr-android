package com.charm.phr.mphr.ui.questionnaire.health_passport

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.QuestionnaireRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class QuestionnaireListActivityModule {

    @Provides
    fun provideQuestionnaireListViewModel(questionnaireRepository: QuestionnaireRepository):
            QuestionnaireListViewModel = QuestionnaireListViewModel(questionnaireRepository)

    @Provides
    fun provideQuestionnaireListViewModelFactory(questionnaireListViewModel: QuestionnaireListViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(questionnaireListViewModel)

    @Provides
    fun provideQuestionnaireListAdapter():
            QuestionnaireListAdapter = QuestionnaireListAdapter()
}