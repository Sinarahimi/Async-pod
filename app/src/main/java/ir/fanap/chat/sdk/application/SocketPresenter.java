package ir.fanap.chat.sdk.application;

import ir.fanap.chat.sdk.bussines.networking.WebSocketHelper;

public class SocketPresenter implements SocketContract.presenter {

    private WebSocketHelper webSocketHelper;
    private SocketContract.view view;

    public SocketPresenter (){

    }

    @Override
    public void getMessage() {
        String message = webSocketHelper.getMessage();
        view.showMessage(message);
    }

    @Override
    public void sendMessage() {

    }

    @Override
    public void getErrorMessage() {
        String error = webSocketHelper.getErrorMessage();
        view.showErrorMessage(error);
    }
}
