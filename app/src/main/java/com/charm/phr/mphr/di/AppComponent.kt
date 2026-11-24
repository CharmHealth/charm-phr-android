package com.charm.phr.mphr.di

import com.charm.phr.mphr.MPHRApplication
import com.charm.phr.mphr.di.module.AppModule
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidSupportInjectionModule::class, AppModule::class, ActivityBuilder::class , ServiceBuilder::class])
interface AppComponent : AndroidInjector<MPHRApplication> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<MPHRApplication>()
}