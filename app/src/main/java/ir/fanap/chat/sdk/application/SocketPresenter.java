package ir.fanap.chat.sdk.application;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.Nullable;

import ir.fanap.chat.sdk.bussines.networking.WebSocketHelper;

public class SocketPresenter implements SocketContract.presenter {


    private WebSocketHelper webSocketHelper;
    private SocketContract.view view;

    public SocketPresenter(SocketContract.view view, Context context) {
        this.view = view;
        webSocketHelper = WebSocketHelper.getInstance(context);
    }

    @Override
    public void getMessage() {
        String message = webSocketHelper.getMessage();
        view.showMessage(message);
    }

    @Override
    public void connect(String socketServerAddress, String appId) {
        webSocketHelper.webSocketConnect(socketServerAddress, appId);
    }

    @Override
    public void sendMessage(String textMessage) {
        webSocketHelper.sendMessage(textMessage);
    }

    @Override
    public void getState() {
//        String state = webSocketHelper.getState();
//        view.showSocketState(state);
        webSocketHelper.getStateLiveData();
    }


    @Override
    public LiveData<String> getLiveData() {
        return webSocketHelper.getStateLiveData();
    }

    @Override
    public void getErrorMessage() {
        String error = webSocketHelper.getErrorMessage();
        view.showErrorMessage(error);
    }
}
