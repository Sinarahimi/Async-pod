package ir.fanap.chat.sdk.bussines.networking;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.neovisionaries.ws.client.PayloadGenerator;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ir.fanap.chat.sdk.bussines.model.AsyncMessageType;
import ir.fanap.chat.sdk.bussines.model.ClientMessage;
import ir.fanap.chat.sdk.bussines.model.Message;
import ir.fanap.chat.sdk.bussines.model.MessageVo;
import ir.fanap.chat.sdk.bussines.model.MessageWrapperVo;
import ir.fanap.chat.sdk.bussines.model.PeerInfo;
import ir.fanap.chat.sdk.bussines.model.PeerMessage;
import ir.fanap.chat.sdk.bussines.model.RegistrationRequest;

public class WebsocketHelper extends WebSocketAdapter {

    private static final int TIMEOUT = 5000;
    /**
     * By default WebSocketFactory uses for non-secure WebSocket connections (ws:)
     * and for secure WebSocket connections (wss:).
     */
    private WebSocket webSocket;
    private Context context;
    private String url;
    private static final String TAG = "WebSocketHelper" + " ";
    private static String uniqueID = null;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    private boolean isDeviceRegister = false;
    private boolean isServerRegister = false;
    private SharedPreferences sharedPrefs;
    private MessageWrapperVo messageWrapperVo;
    private Moshi moshi;


    public void init(String socketServerAddress, String appId) {
        moshi = new Moshi.Builder().build();
        webSocketConnect(socketServerAddress, appId);
    }

    @Override
    public void onTextMessage(WebSocket websocket, String textMessage) throws Exception {
        super.onTextMessage(websocket, textMessage);
        JsonAdapter<ClientMessage> jsonAdapter = moshi.adapter(ClientMessage.class);
        ClientMessage clientMessage = jsonAdapter.fromJson(textMessage);

        int type = 0;
        if (clientMessage != null) {
            type = clientMessage.getType();
        }
        @AsyncMessageType.MessageType int currentMessageType = type;
        switch (currentMessageType) {
            case AsyncMessageType.MessageType.ACK:
                MessageVo messageVo = new MessageVo();
                //TODO how should i fill MessageID with the content that in the client message

                long messageid = 11;
                messageVo.setMessageId(messageid);
                messageVo.setContent("");
                JsonAdapter<MessageVo> jsonMessageVoAdapter = moshi.adapter(MessageVo.class);
                String jsonMessageVo = jsonMessageVoAdapter.toJson(messageVo);
                messageWrapper(jsonMessageVo, AsyncMessageType.MessageType.ACK);
                break;
            case AsyncMessageType.MessageType.DEVICE_REGISTER:
                //TODO the content of the client message has a PeerID and we have to persist that

                isServerRegister = true;
                RegistrationRequest registrationRequest = new RegistrationRequest();
                registrationRequest.setName("oauth-wire");
                JsonAdapter<RegistrationRequest> jsonRegistrationRequestVoAdapter = moshi.adapter(RegistrationRequest.class);
                String jsonRegistrationRequestVo = jsonRegistrationRequestVoAdapter.toJson(registrationRequest);
                String jsonMessageWrapperVo = getMessageWrapper(moshi, jsonRegistrationRequestVo,AsyncMessageType.MessageType.DEVICE_REGISTER);
                websocket.sendText(jsonMessageWrapperVo);
                break;
            case AsyncMessageType.MessageType.ERROR_MESSAGE:
                //TODO log at last we have to give it to the chat package

                break;
            case AsyncMessageType.MessageType.MESSAGE_ACK_NEEDED:
                // TODO plus you give it to chat package you sent ACK to the server also

                break;
            case AsyncMessageType.MessageType.MESSAGE_SENDER_ACK_NEEDED:
                // TODO plus you give it to chat package you sent ACK to the server also

                break;
            case AsyncMessageType.MessageType.MESSAGE:
                //TODO just give it to the chat package

                break;
            case AsyncMessageType.MessageType.PEER_REMOVED:
                //TODO Ask

                break;
            case AsyncMessageType.MessageType.PING:
                if (!isDeviceRegister) {
//                    uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
                    PeerInfo peerInfo = new PeerInfo();
                    peerInfo.setDeviceId(clientMessage.getContent());
                    peerInfo.setRenew(true);

                    JsonAdapter<PeerInfo> jsonPeerMessageAdapter = moshi.adapter(PeerInfo.class);
                    String peerMessageJson = jsonPeerMessageAdapter.toJson(peerInfo);
                    messageWrapper(peerMessageJson, AsyncMessageType.MessageType.DEVICE_REGISTER);
                    JsonAdapter<MessageWrapperVo> jsonMessageWrapperVOAdapter = moshi.adapter(MessageWrapperVo.class);
                    String jsonMessageWrapper = jsonMessageWrapperVOAdapter.toJson(messageWrapperVo);
                    websocket.sendText(jsonMessageWrapper);

                } else websocket.sendPing();
                break;
            case AsyncMessageType.MessageType.SERVER_REGISTER:
                // The server is register
                if (isDeviceRegister) {
                    if (!isServerRegister) {
                        isDeviceRegister = true;
                        sendPing();
                    }
                }
                break;
        }
    }

    @Override
    public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
        super.onStateChanged(websocket, newState);
        Log.i("onStateChanged", newState.toString());
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        super.onError(websocket, cause);
        Log.e("onError", cause.toString());
        cause.getCause().printStackTrace();
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
        super.onConnectError(websocket, exception);
        Log.e("onConnected", exception.toString());
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        super.onConnected(websocket, headers);
        Log.i("onConnected", headers.toString());
    }

    @Override
    public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {
        super.onMessageError(websocket, cause, frames);
        Log.e("onMessageError", cause.toString());

        PeerInfo peerInfo = new PeerInfo();
        peerInfo.setRefresh(true);

        //TODO connect with refresh PeerInfo
    }

    private void webSocketConnect(String socketServerAddress, final String appId) {
        WebSocketFactory webSocketFactory = new WebSocketFactory();
        webSocketFactory.setVerifyHostname(false);

        try {
            webSocket = webSocketFactory
                    .setConnectionTimeout(TIMEOUT)
                    .createSocket(socketServerAddress).setPingInterval(4 * 1000)
                    .addListener(this);

            webSocket.setPingPayloadGenerator(new PayloadGenerator() {
                @Override
                public byte[] generate() {
                    String deviceId = getUniqueID();
                    PeerMessage PeerMessage = new PeerMessage();
                    PeerMessage.setAppId(appId);
                    PeerMessage.setDeviceId(deviceId);
                    return new byte[0];
                }
            });
            webSocket.connectAsynchronously();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private String getMessageWrapper(Moshi moshi, String json, int messageType) {
        messageWrapper(json, messageType);
        JsonAdapter<MessageWrapperVo> jsonMessageWrapperVoAdapter = moshi.adapter(MessageWrapperVo.class);
        return jsonMessageWrapperVoAdapter.toJson(messageWrapperVo);
    }

    private void messageWrapper(String json, int messageType) {
        messageWrapperVo = new MessageWrapperVo();
        messageWrapperVo.setContent(json);
        messageWrapperVo.setType(messageType);
    }

    // Send a ping per 10 seconds.
    private void sendPing() {
        webSocket.setPingInterval(10 * 1000);
    }

    public void sendMessage(String textContent) {
        Message message = new Message();
        message.setContent(textContent);
        JsonAdapter<Message> jsonAdapter = moshi.adapter(Message.class);
        String jsonMessage = jsonAdapter.toJson(message);
        messageWrapper(jsonMessage,AsyncMessageType.MessageType.MESSAGE_ACK_NEEDED);

        //TODO next
//        webSocket.sendText(getMessageWrapper(moshi,));
    }

    //  it's (relatively) easily resettable because it only persists as long as the app is installed.
    private synchronized String getUniqueID() {
        if (uniqueID == null) {
            sharedPrefs = context.getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.apply();
            }
        }
        return uniqueID;
    }

    @Override
    public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        super.onCloseFrame(websocket, frame);
        //TODO connect with refresh with PeerInfo
    }
}
