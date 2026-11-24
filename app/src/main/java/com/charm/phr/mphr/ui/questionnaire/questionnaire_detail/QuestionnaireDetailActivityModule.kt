package com.charm.phr.mphr.ui.questionnaire.questionnaire_detail

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.PatientRepository
import com.charm.phr.mphr.model.repository.QuestionnaireRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides
import java.util.concurrent.Executor

@Module
class QuestionnaireDetailActivityModule {

    @Provides
    fun provideQuestionnaireDetailViewModel(questionnaireRepository: QuestionnaireRepository ,patientRepository: PatientRepository , executor: Executor):
                QuestionnaireDetailViewModel = QuestionnaireDetailViewModel(questionnaireRepository , patientRepository, executor)

    @Provides
    fun provideQuestionnaireDetailViewModelFactory(questionnaireDetailViewModel: QuestionnaireDetailViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(questionnaireDetailViewModel)
}