package ir.fanap.chat.sdk.application;

import android.arch.lifecycle.LiveData;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.util.List;

public interface SocketContract {

    interface view {
        void showMessage(String message);

        void messageCalled();

        void showErrorMessage(String error);

        void showOnMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames);

        void showOnConnectError(WebSocket websocket, WebSocketException exception);

        void showSocketState(String state);

        void showLiveDataState(LiveData state);
    }

    interface presenter {
        String getMessage();

        void connect(String socketServerAddress, String appId);

        void sendMessage(String textMessage, int messageType, long[] receiversId);

        void getLiveState();

        String getState();

        void logOut();

        LiveData<String> getLiveData();

        void getErrorMessage();

        void closeSocket();

        boolean isSocketOpen();

        String getPeerId();
    }
}
