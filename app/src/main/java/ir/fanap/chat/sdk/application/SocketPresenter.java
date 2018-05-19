package ir.fanap.chat.sdk.application;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import com.fanap.podasync.networking.Async;

public class SocketPresenter implements SocketContract.presenter {

    private Async async;
    private SocketContract.view view;

    public SocketPresenter(SocketContract.view view, Context context) {
        this.view = view;
        async = Async.getInstance(context);
    }

    @Override
    public String getMessage() {
        String message = async.getMessage();
        view.showMessage(message);
        view.messageCalled();
        return message;
    }

    @Override
    public void connect(String socketServerAddress, String appId) {
        async.connect(socketServerAddress, appId);
    }

    @Override
    public void sendMessage(String textMessage, int messageType, long[] receiversId) {
        async.sendMessage(textMessage, messageType,receiversId);
    }

    @Override
    public void getLiveState() {
        async.getStateLiveData();
    }

    @Override
    public String getState() {
        return async.getState();
    }

    @Override
    public boolean isSocketOpen() {
        boolean isSocketOpen = false;
        if ((async.getState()) != null) {
            if (async.getState().equals("OPEN")) {
                isSocketOpen = true;
            }
        }
        return isSocketOpen;
    }

    @Override
    public String getPeerId() {
        return async.getPeerId();
    }

    @Override
    public void logOut() {
        async.logOut();
    }

    @Override
    public LiveData<String> getLiveData() {
        return async.getStateLiveData();
    }

    @Override
    public void getErrorMessage() {
        String error = async.getErrorMessage();
        view.showErrorMessage(error);
    }

    @Override
    public void closeSocket() {
        async.closeSocket();
    }
}
