package com.charm.phr.mphr.ui.problem_list

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import com.charm.phr.mphr.util.ModelUtil
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
class ProblemListActivityModule {

    @Provides
    fun provideProblemListViewModel(miscRepository: MiscRepository):
            ProblemListViewModel = ProblemListViewModel(miscRepository)

    @Provides
    fun provideProblemListViewModelFactory(problemListViewModel: ProblemListViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(problemListViewModel)


    @Provides
    fun providePrescriptionViewPagerAdapter(problemListActivity: ProblemListActivity, appContext: Context):
            ProblemListViewPagerAdapter = ProblemListViewPagerAdapter(problemListActivity , appContext , problemListActivity.intent.getBooleanExtra(ModelUtil.isHealthPassportIntent , false))

}


//region FragmentProvider

@Module
internal abstract class ActiveProblemListFragmentProvider {

    @ContributesAndroidInjector(modules = [ActiveProblemListFragmentModule::class])
    internal abstract fun provideActiveProblemListFragment(): ActiveProblemListFragment
}

@Module
internal abstract class HistoryProblemListFragmentProvider {

    @ContributesAndroidInjector(modules = [HistoryProblemListFragmentModule::class])
    internal abstract fun provideHistoryProblemListFragment(): HistoryProblemListFragment
}

//endregion

//region FragmentModule

@Module
class ActiveProblemListFragmentModule {
    //

    @Provides
    fun provideProblemListListAdapter():
            ProblemListAdapter = ProblemListAdapter()

}

@Module
class HistoryProblemListFragmentModule {
    //
/*    @Provides
    fun provideHistoryProblemListAdapter():
            SupplementListAdapter = SupplementListAdapter()*/
}

//endregion