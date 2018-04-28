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
import ir.fanap.chat.sdk.bussines.model.MessageWrapperVo;
import ir.fanap.chat.sdk.bussines.model.PeerInfo;
import ir.fanap.chat.sdk.bussines.model.PeerMessage;
import ir.fanap.chat.sdk.bussines.model.RegistrationRequest;

import static com.neovisionaries.ws.client.WebSocketState.OPEN;

public class WebSocketHelper extends WebSocketAdapter {

    private static final int TIMEOUT = 5000;
    /**
     * By default WebSocketFactory uses for non-secure WebSocket connections (ws:)
     * and for secure WebSocket connections (wss:).
     */
    private WebSocket webSocket;
    private static final String TAG = "WebSocketHelper" + " ";
    private static WebSocketHelper instance;
    private static String uniqueID = null;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    private boolean isDeviceRegister = false;
    private boolean isServerRegister = false;
    private static SharedPreferences sharedPrefs;
    private MessageWrapperVo messageWrapperVo;
    private static Moshi moshi;
    private String errorMessage;
    private String message;

    private WebSocketHelper() {
    }

    public static WebSocketHelper getInstance(Context context) {
        if (instance == null) {
            sharedPrefs = context.getSharedPreferences(PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            moshi = new Moshi.Builder().build();
            instance = new WebSocketHelper();
        }
        return instance;
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
                setMessage(clientMessage.getContent());
                break;
            case AsyncMessageType.MessageType.DEVICE_REGISTER:
                String peerId = clientMessage.getContent();

                if (isServerRegister) {
                    if (websocket.getState() == OPEN) {
                        if (websocket.getFrameQueueSize() > 0) {

                        }
                    }

                } else {
                    RegistrationRequest registrationRequest = new RegistrationRequest();
                    registrationRequest.setName("oauth-wire");
                    JsonAdapter<RegistrationRequest> jsonRegistrationRequestVoAdapter = moshi.adapter(RegistrationRequest.class);
                    String jsonRegistrationRequestVo = jsonRegistrationRequestVoAdapter.toJson(registrationRequest);
                    String jsonMessageWrapperVo = getMessageWrapper(moshi, jsonRegistrationRequestVo, AsyncMessageType.MessageType.SERVER_REGISTER);
                    websocket.sendText(jsonMessageWrapperVo);
                }

                break;
            case AsyncMessageType.MessageType.ERROR_MESSAGE:
                Log.e(TAG, clientMessage.getContent());
                setErrorMessage(clientMessage.getContent());
                break;
            case AsyncMessageType.MessageType.MESSAGE_ACK_NEEDED:
                setMessage(clientMessage.getContent());
                Message message = new Message();
                message.setMessageId(clientMessage.getSenderMessageId());

                JsonAdapter<Message> jsonMessageAdapter = moshi.adapter(Message.class);
                String jsonMessage = jsonMessageAdapter.toJson(message);
                String jsonMessageWrapper = getMessageWrapper(moshi, jsonMessage, AsyncMessageType.MessageType.ACK);
                websocket.sendText(jsonMessageWrapper);
                break;
            case AsyncMessageType.MessageType.MESSAGE_SENDER_ACK_NEEDED:
                setMessage(clientMessage.getContent());
                Message messageSenderAckNeeded = new Message();
                messageSenderAckNeeded.setMessageId(clientMessage.getSenderMessageId());

                JsonAdapter<Message> jsonSenderAckNeededAdapter = moshi.adapter(Message.class);
                String jsonSenderAckNeeded = jsonSenderAckNeededAdapter.toJson(messageSenderAckNeeded);
                String jsonSenderAckNeededWrapper = getMessageWrapper(moshi, jsonSenderAckNeeded, AsyncMessageType.MessageType.ACK);
                websocket.sendText(jsonSenderAckNeededWrapper);
                break;
            case AsyncMessageType.MessageType.MESSAGE:
                setMessage(clientMessage.getContent());
                break;
            case AsyncMessageType.MessageType.PEER_REMOVED:

                break;
            case AsyncMessageType.MessageType.PING:
                if (!isDeviceRegister) {
                    //uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
                    PeerInfo peerInfo = new PeerInfo();
                    peerInfo.setRenew(true);
                    peerInfo.setAppId("UIAPP");
                    if (clientMessage != null) {
                        peerInfo.setDeviceId(clientMessage.getContent());
                    }
                    JsonAdapter<PeerInfo> jsonPeerMessageAdapter = moshi.adapter(PeerInfo.class);
                    String peerMessageJson = jsonPeerMessageAdapter.toJson(peerInfo);
                    String jsonPeerInfoWrapper = getMessageWrapper(moshi, peerMessageJson, AsyncMessageType.MessageType.DEVICE_REGISTER);
                    websocket.sendText(jsonPeerInfoWrapper);

                } else websocket.sendPing();
                break;
            case AsyncMessageType.MessageType.SERVER_REGISTER:
                isServerRegister = true;
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

        JsonAdapter<PeerInfo> jsonPeerMessageAdapter = moshi.adapter(PeerInfo.class);
        String jason = jsonPeerMessageAdapter.toJson(peerInfo);
        getMessageWrapper(moshi, jason, AsyncMessageType.MessageType.PING);

        //TODO complete this section
    }

    public void webSocketConnect(String socketServerAddress, final String appId) {
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
        messageWrapperVo = new MessageWrapperVo();
        messageWrapperVo.setContent(json);
        messageWrapperVo.setType(messageType);
        JsonAdapter<MessageWrapperVo> jsonMessageWrapperVoAdapter = moshi.adapter(MessageWrapperVo.class);
        return jsonMessageWrapperVoAdapter.toJson(messageWrapperVo);
    }

    public void sendMessage(String textContent) {
        Message message = new Message();
        message.setContent(textContent);
        JsonAdapter<Message> jsonAdapter = moshi.adapter(Message.class);
        String jsonMessage = jsonAdapter.toJson(message);
        String wrapperJsonString = getMessageWrapper(moshi, jsonMessage, AsyncMessageType.MessageType.MESSAGE_ACK_NEEDED);
        webSocket.sendText(wrapperJsonString);
    }

    //  it's (relatively) easily resettable because it only persists as long as the app is installed.
    private synchronized String getUniqueID() {
        if (uniqueID == null) {

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
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
        websocket.connectAsynchronously().recreate();
    }

    @Override
    public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        super.onCloseFrame(websocket, frame);
        Log.e("onCloseFrame",frame.getCloseReason());
        //TODO connect with refresh with PeerInfo

    }

    private void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
