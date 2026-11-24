package com.charm.phr.mphr.ui.problem_list.add_problem_list

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class AddProblemListActivityModule {

    @Provides
    fun provideAddProblemListViewModel(miscRepository: MiscRepository):
            AddProblemListViewModel = AddProblemListViewModel(miscRepository)

    @Provides
    fun provideAddProblemListViewModelFactory(addProblemListViewModel: AddProblemListViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory<AddProblemListViewModel>(addProblemListViewModel)

}