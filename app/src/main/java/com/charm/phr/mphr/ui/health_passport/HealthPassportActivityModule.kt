package com.charm.phr.mphr.ui.health_passport

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.QuestionnaireRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory

import dagger.Module
import dagger.Provides

@Module
class HealthPassportActivityModule {

    @Provides
    fun provideHealthPassportViewModel(questionnaireRepository: QuestionnaireRepository):
            HealthPassportViewModel = HealthPassportViewModel(questionnaireRepository)

    @Provides
    fun provideHealthPassportViewModelFactory(healthPassportViewModel: HealthPassportViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(healthPassportViewModel)


    @Provides
    fun provideHealthPassportListAdapter():
            HealthPassportListAdapter = HealthPassportListAdapter()

    @Provides
    fun provideHealthPassportGridListAdapter():
            HealthPassportGridModuleAdapter = HealthPassportGridModuleAdapter()

}