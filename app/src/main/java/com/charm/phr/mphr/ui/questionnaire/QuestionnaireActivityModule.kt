package com.charm.phr.mphr.ui.questionnaire

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.QuestionnaireRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
class QuestionnaireActivityModule {

    @Provides
    fun provideQuestionnaireViewModel(questionnaireRepository: QuestionnaireRepository):
            QuestionnaireViewModel = QuestionnaireViewModel(questionnaireRepository)

    @Provides
    fun provideQuestionnaireViewModelFactory(questionnaireViewModel: QuestionnaireViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(questionnaireViewModel)

    @Provides
    fun provideQuestionnaireViewPagerAdapter(questionnaireActivity: QuestionnaireActivity  , appContext: Context):
            QuestionnaireViewPagerAdapter = QuestionnaireViewPagerAdapter(questionnaireActivity , appContext)
}

//region FragmentProvider

@Module
internal abstract class QuestionnaireCurrentFragmentProvider {

    @ContributesAndroidInjector(modules = [QuestionnaireCurrentFragmentModule::class])
    internal abstract fun provideMedicationFragment(): QuestionnaireCurrentFragment
}

@Module
internal abstract class QuestionnairePastFragmentProvider {

    @ContributesAndroidInjector(modules = [QuestionnairePastFragmentModule::class])
    internal abstract fun provideSupplementFragment(): QuestionnairePastFragment
}

//endregion

//region FragmentModule

@Module
class QuestionnaireCurrentFragmentModule {
    //

    @Provides
    fun provideQuestionnaireCurrentListAdapter():
            QuestionnaireCurrentListAdapter = QuestionnaireCurrentListAdapter()

}

@Module
class QuestionnairePastFragmentModule {
    //
    @Provides
    fun provideQuestionnairePastListAdapter():
            QuestionnairePastListAdapter = QuestionnairePastListAdapter()
}

//endregion