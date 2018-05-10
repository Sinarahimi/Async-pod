package ir.fanap.chat.sdk.application;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class MainViewModule {

    @Binds
    abstract SocketContract.view provideMainView(MainActivity mainActivity);

}

