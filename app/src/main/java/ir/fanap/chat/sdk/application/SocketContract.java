package ir.fanap.chat.sdk.application;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.util.List;

interface SocketContract {

    interface view {
        void showMessage(String message);

        void showErrorMessage(String error);

        void showOnMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames);

        void showOnConnectError(WebSocket websocket, WebSocketException exception);
    }

    interface presenter {
        void getMessage();

        void connect(String socketServerAddress, String appId);

        void sendMessage(String textMessage);

        void getErrorMessage();
    }
}
