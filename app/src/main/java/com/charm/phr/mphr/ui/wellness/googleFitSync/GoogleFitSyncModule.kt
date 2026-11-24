package com.charm.phr.mphr.ui.wellness.googleFitSync

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class GoogleFitSyncModule {

    @Provides
    fun provideGoogleFitSyncModel():
            GoogleFitSyncModel = GoogleFitSyncModel()

    @Provides
    fun provideGoogleFitSyncModelFactory(googleFitSyncModel: GoogleFitSyncModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(googleFitSyncModel)

}