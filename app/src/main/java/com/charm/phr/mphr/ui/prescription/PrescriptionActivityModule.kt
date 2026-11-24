package com.charm.phr.mphr.ui.prescription

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.PrescriptionRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import com.charm.phr.mphr.util.AuthManager
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
class PrescriptionActivityModule {

    @Provides
    fun providePrescriptionViewModel(prescriptionRepository: PrescriptionRepository): PrescriptionViewModel = PrescriptionViewModel(prescriptionRepository)

    @Provides
    fun providePrescriptionViewModelProviderFactory(prescriptionViewModel: PrescriptionViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(prescriptionViewModel)

    @Provides
    fun providePrescriptionViewPagerAdapter(prescriptionActivity: PrescriptionActivity, appContext: Context , authManager: AuthManager):
            PrescriptionViewPagerAdapter = PrescriptionViewPagerAdapter(prescriptionActivity , appContext ,authManager)

}

//region FragmentProvider

@Module
internal abstract class MedicationFragmentProvider {

    @ContributesAndroidInjector(modules = [MedicationFragmentModule::class])
    internal abstract fun provideMedicationFragment(): MedicationFragment
}

@Module
internal abstract class SupplementFragmentProvider {

    @ContributesAndroidInjector(modules = [SupplementFragmentModule::class])
    internal abstract fun provideSupplementFragment(): SupplementFragment
}

//endregion

//region FragmentModule

@Module
class MedicationFragmentModule {
}

@Module
class SupplementFragmentModule {
    //
    @Provides
    fun provideSupplementListAdapter(authManager: AuthManager):
            SupplementListAdapter = SupplementListAdapter(authManager)
}

//endregion