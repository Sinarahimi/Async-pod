package ir.fanap.chat.sdk.application;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class MainModule {

    private Context context;
    @Provides
    SocketPresenter socketPresenter(SocketContract.view view, Context context) {
        this.context = context;
        return new SocketPresenter(view, context);
    }

    @Provides
    Context provideContext() {
        return context;
    }
}
