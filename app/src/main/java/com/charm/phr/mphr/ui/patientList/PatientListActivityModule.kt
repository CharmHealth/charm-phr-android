package com.charm.phr.mphr.ui.patientList

import android.content.Context
import com.charm.phr.mphr.util.AuthManager
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides

@Module
class PatientListActivityModule {

    @Provides
    fun providePatientListAdapter( picasso: Picasso ,  appContext: Context ,  authManager: AuthManager):
        PatientListAdapter = PatientListAdapter(picasso , appContext, authManager)


}