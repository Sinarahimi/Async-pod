package ir.fanap.chat.sdk.application;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Binds all sub-components within the app.
 */
@Module
public abstract class BuilderModule {

    @ContributesAndroidInjector(modules = {MainModule.class, MainViewModule.class})
    abstract MainActivity mainActivity();
}

