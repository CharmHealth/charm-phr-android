package com.charm.phr.mphr.ui.allergy

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.WellnessRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import com.charm.phr.mphr.util.AuthManager
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
class AllergyActivityModule {

    @Provides
    fun provideAllergyViewModel(wellnessRepository: WellnessRepository):
            AllergyViewModel = AllergyViewModel(wellnessRepository)

    @Provides
    fun provideAllergyActivityModule(allergyViewModel: AllergyViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory<AllergyViewModel>(allergyViewModel)

    @Provides
    fun provideActiveAllergyListRecyclerAdapter(authManager: AuthManager):
            ActiveAllergyListRecyclerAdapter = ActiveAllergyListRecyclerAdapter(authManager)

}