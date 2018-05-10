package ir.fanap.chat.sdk.application;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@Component(modules = {AndroidSupportInjectionModule.class, BuilderModule.class, MainModule.class})
public interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(App application);
        AppComponent build();
    }

    void inject(App app);
}

