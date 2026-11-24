package com.charm.phr.mphr.ui.components.mdCamera

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import java.util.concurrent.Executor

@Module
class MdCameraActivityModule {

    @Provides
    fun provideMdCameraViewModel():
            MdCameraViewModel = MdCameraViewModel()

    @Provides
    fun provideMdCameraViewModelProviderFactory(mdCameraViewModel: MdCameraViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory<MdCameraViewModel>(mdCameraViewModel)

    @Provides
    fun provideMdPhotoPreviewAdapterAdapter(appContext: Context, picasso: Picasso, executor: Executor):
            MdPhotoPreviewAdapter = MdPhotoPreviewAdapter(appContext, picasso, executor)

    @Provides
    fun provideMdCaptureCameraThumbnailAdapter(appContext: Context, executor: Executor):
            MdCaptureCameraThumbnailAdapter = MdCaptureCameraThumbnailAdapter(appContext, executor)


}

@Module
internal abstract class MdCameraFragmentProvider {

    @ContributesAndroidInjector(modules = [MdCameraFragmentModule::class])
    internal abstract fun provideMdCameraFragmentModule(): MdCameraFragment
}

@Module
internal abstract class MdPhotoPreviewFragmentProvider {

    @ContributesAndroidInjector(modules = [MdPhotoPreviewFragmentModule::class])
    internal abstract fun provideMdPhotoPreviewFragmentModule(): MdPhotoPreviewFragment
}

@Module
internal abstract class ImageCropperFragmentProvider {

    @ContributesAndroidInjector(modules = [ImageCropperFragmentModule::class])
    internal abstract fun provideImageCropperFragment(): ImageCropperFragment
}

@Module
class MdCameraFragmentModule {
    //
}


@Module
class MdPhotoPreviewFragmentModule {
    //
}

@Module
class ImageCropperFragmentModule {
    //
}
