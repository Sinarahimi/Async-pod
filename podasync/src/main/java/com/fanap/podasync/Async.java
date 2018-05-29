package com.fanap.podasync;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.fanap.podasync.model.AsyncConstant;
import com.fanap.podasync.model.AsyncMessageType;
import com.fanap.podasync.model.ClientMessage;
import com.fanap.podasync.model.Message;
import com.fanap.podasync.model.MessageWrapperVo;
import com.fanap.podasync.model.PeerInfo;
import com.fanap.podasync.model.RegistrationRequest;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.neovisionaries.ws.client.WebSocketState.OPEN;

public class Async extends WebSocketAdapter {

    private static final int TIMEOUT = 5000;
    /**
     * By default WebSocketFactory uses for non-secure WebSocket connections (ws:)
     * and for secure WebSocket connections (wss:).
     */
    private WebSocket webSocket;
    private WebSocket webSocketReconnect;
    private static final String TAG = "Async" + " ";
    private static Async instance;
    private static String uniqueID = null;
    private boolean isServerRegister = false;
    private boolean isDeviceRegister = false;
    private static SharedPreferences sharedPrefs;
    private MessageWrapperVo messageWrapperVo;
    private static Moshi moshi;
    private String errorMessage;
    private long lastTimeMessage;
    private String message;
    private String onError;
    private String state;
    private String appId;
    private String peerId;
    private String deviceID;
    private Exception onConnectException;
    private MutableLiveData<String> stateLiveData = new MutableLiveData<>();
    private String serverAddress;
    final Handler pingHandler = new Handler(Looper.getMainLooper());

    public Async() {
    }

    public static Async getInstance(Context context) {
        if (instance == null) {
            sharedPrefs = context.getSharedPreferences(AsyncConstant.Constants.PREFERENCE, Context.MODE_PRIVATE);
            moshi = new Moshi.Builder().build();
            instance = new Async();
        }
        return instance;
    }

    /**
     * @param textMessage
     */

    @Override
    public void onTextMessage(WebSocket websocket, String textMessage) throws Exception {
        super.onTextMessage(websocket, textMessage);
        JsonAdapter<ClientMessage> jsonAdapter = moshi.adapter(ClientMessage.class);
        ClientMessage clientMessage = jsonAdapter.fromJson(textMessage);
        Log.i("onTextMessage", textMessage);

        int type = 0;
        if (clientMessage != null) {
            type = clientMessage.getType();
        }
        scheduleSendPing(10000);

        @AsyncMessageType.MessageType int currentMessageType = type;
        switch (currentMessageType) {
            case AsyncMessageType.MessageType.ACK:
                setMessage(clientMessage.getContent());
                break;
            case AsyncMessageType.MessageType.DEVICE_REGISTER:
                isDeviceRegister = true;
                String peerId = clientMessage.getContent();
                if (!peerIdExistence()) {
                    savePeerId(peerId);
                }
                /*
                  When socket closes by any reason
                  , server is still registered and we sent a lot of message but
                  they are still in the queue

                  */
                //TODO handle queue message
                if (isServerRegister && peerId.equals(getPeerId())) {
                    if (websocket.getState() == OPEN) {
                        if (websocket.getFrameQueueSize() > 0) {

                        }
                    }

                } else {
                    /*
                      Register server when its not registered
                      */
                    RegistrationRequest registrationRequest = new RegistrationRequest();
                    registrationRequest.setName("oauth-wire");
                    JsonAdapter<RegistrationRequest> jsonRegistrationRequestVoAdapter = moshi.adapter(RegistrationRequest.class);
                    String jsonRegistrationRequestVo = jsonRegistrationRequestVoAdapter.toJson(registrationRequest);
                    String jsonMessageWrapperVo = getMessageWrapper(moshi, jsonRegistrationRequestVo, AsyncMessageType.MessageType.SERVER_REGISTER);
                    websocket.sendText(jsonMessageWrapperVo);
                    lastTimeMessage = new Date().getTime();
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
                lastTimeMessage = new Date().getTime();
                break;
            case AsyncMessageType.MessageType.MESSAGE_SENDER_ACK_NEEDED:
                setMessage(clientMessage.getContent());
                Message messageSenderAckNeeded = new Message();
                messageSenderAckNeeded.setMessageId(clientMessage.getSenderMessageId());

                JsonAdapter<Message> jsonSenderAckNeededAdapter = moshi.adapter(Message.class);
                String jsonSenderAckNeeded = jsonSenderAckNeededAdapter.toJson(messageSenderAckNeeded);
                String jsonSenderAckNeededWrapper = getMessageWrapper(moshi, jsonSenderAckNeeded, AsyncMessageType.MessageType.ACK);
                websocket.sendText(jsonSenderAckNeededWrapper);
                lastTimeMessage = new Date().getTime();
                break;
            case AsyncMessageType.MessageType.MESSAGE:
                setMessage(clientMessage.getContent());
                break;
            case AsyncMessageType.MessageType.PEER_REMOVED:
                break;
            case AsyncMessageType.MessageType.PING:
                if (!isDeviceRegister) {
                    PeerInfo peerInfo = new PeerInfo();
                    peerInfo.setRenew(true);
                    peerInfo.setAppId(getAppId());
                    if (clientMessage != null) {
                        peerInfo.setDeviceId(clientMessage.getContent());
                        saveDeviceId(clientMessage.getContent());
                    }
                    JsonAdapter<PeerInfo> jsonPeerMessageAdapter = moshi.adapter(PeerInfo.class);
                    String peerMessageJson = jsonPeerMessageAdapter.toJson(peerInfo);
                    String jsonPeerInfoWrapper = getMessageWrapper(moshi, peerMessageJson, AsyncMessageType.MessageType.DEVICE_REGISTER);
                    websocket.sendText(jsonPeerInfoWrapper);
                    lastTimeMessage = new Date().getTime();
                } else {
                    scheduleSendPing(50000);
                }
                break;
            case AsyncMessageType.MessageType.SERVER_REGISTER:
                Log.i("Ready for chat", textMessage);
                isServerRegister = true;
                scheduleSendPing(10000);
                break;
        }
    }

    @Override
    public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
        super.onStateChanged(websocket, newState);
        stateLiveData.postValue(newState.toString());
        setState(newState.toString());
        Log.i("onStateChanged", newState.toString());
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        super.onError(websocket, cause);
        Log.e("onError", cause.toString());
        cause.getCause().printStackTrace();
        setOnError(cause.toString());
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
        super.onConnectError(websocket, exception);
        Log.e("onConnected", exception.toString());
        setonConnectError(exception);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        super.onConnected(websocket, headers);
        Log.i("onConnected", headers.toString());
    }

    /**
     * After error event its start reconnecting again.
     * Note that you should not trigger reconnection in onError() method because onError()
     * may be called multiple times due to one error.
     * Instead, onDisconnected() is the right place to trigger reconnection.
     */
    @Override
    public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {
        super.onMessageError(websocket, cause, frames);
        Log.e("onMessageError", cause.toString());
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
        reConnect();
    }

    @Override
    public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        super.onCloseFrame(websocket, frame);
        Log.e("onCloseFrame", frame.getCloseReason());
        reConnect();
    }

    public void webSocketConnect(String socketServerAddress, final String appId) {
        WebSocketFactory webSocketFactory = new WebSocketFactory();
        webSocketFactory.setVerifyHostname(false);
        setAppId(appId);
        setServerAddress(socketServerAddress);
        try {
            webSocket = webSocketFactory
                    .setConnectionTimeout(TIMEOUT)
                    .createSocket(socketServerAddress)
                    .addListener(this);
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

    public void sendMessage(String textContent, int messageType) {
        Message message = new Message();
        message.setContent(textContent);
        JsonAdapter<Message> jsonAdapter = moshi.adapter(Message.class);
        String jsonMessage = jsonAdapter.toJson(message);
        String wrapperJsonString = getMessageWrapper(moshi, jsonMessage, messageType);
        webSocket.sendText(wrapperJsonString);
        lastTimeMessage = new Date().getTime();
    }

    //  it's (relatively) easily resettable because it only persists as long as the app is installed.
    private synchronized String getUniqueID() {
        if (uniqueID == null) {

            uniqueID = sharedPrefs.getString(AsyncConstant.Constants.PREFERENCE, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(AsyncConstant.Constants.PREFERENCE, uniqueID);
                editor.apply();
            }
        }
        return uniqueID;
    }

    public void closeSocket() {
        webSocket.sendClose();
    }

    public LiveData<String> getStateLiveData() {
        return stateLiveData;
    }

    /**
     * If peerIdExistence we set {@param refresh = true} to the
     * Async else we set {@param renew = true}  to the Async to
     * get the new PeerId
     */
    private void reConnect() {
        WebSocketFactory webSocketFactory = new WebSocketFactory();
        webSocketFactory.setVerifyHostname(false);
        String message;
        try {
            webSocketReconnect = webSocketFactory
                    .setConnectionTimeout(TIMEOUT)
                    .createSocket(getServerAddress())
                    .addListener(this);
            webSocketReconnect.connectAsynchronously();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (peerIdExistence()) {
            PeerInfo peerInfo = new PeerInfo();
            peerInfo.setAppId(getAppId());
            peerInfo.setDeviceId(getDeviceId());
            peerInfo.setRefresh(true);
            JsonAdapter<PeerInfo> jsonPeerMessageAdapter = moshi.adapter(PeerInfo.class);
            String jason = jsonPeerMessageAdapter.toJson(peerInfo);
            message = getMessageWrapper(moshi, jason, AsyncMessageType.MessageType.PING);
            webSocketReconnect.sendText(message);
            isDeviceRegister = false;
            lastTimeMessage = new Date().getTime();
        } else {
            PeerInfo peerInfo = new PeerInfo();
            peerInfo.setAppId(getAppId());
            peerInfo.setDeviceId(getDeviceId());
            peerInfo.setRenew(true);
            JsonAdapter<PeerInfo> jsonPeerMessageAdapter = moshi.adapter(PeerInfo.class);
            String jason = jsonPeerMessageAdapter.toJson(peerInfo);
            message = getMessageWrapper(moshi, jason, AsyncMessageType.MessageType.PING);
            webSocketReconnect.sendText(message);
            isDeviceRegister = false;
            lastTimeMessage = new Date().getTime();
        }
    }

    /**
     * Remove the peerId and send ping again but this time
     * peerId that was set in the server was removed
     */
    public void logOut() {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(AsyncConstant.Constants.PEER_ID, null);
        editor.apply();
        isServerRegister = false;
        isDeviceRegister = false;
        webSocketConnect(getServerAddress(), getAppId());
    }

    /**
     * When its send message the lastTimeMessage gets updated.
     * if the {@param currentTime} - {@param lastTimeMessage} was bigger than 10 second
     * it means we need to send ping to keep socket alive.
     * we don't need to set ping interval because its send ping automatically by itself
     * with the {@param type}type that not 0.
     * We set {@param type = 0} with empty content.
     * We set {@param type = 0} with empty content.
     */
    private void sendPing() {
        long currentTime = new Date().getTime();
        if (currentTime - lastTimeMessage > 10000) {
            message = getMessageWrapper(moshi, "", AsyncMessageType.MessageType.PING);
            webSocket.sendText(message);
            lastTimeMessage = new Date().getTime();
        }
    }

    private void scheduleSendPing(int delayTime) {
        pingHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendPing();
                pingHandler.postDelayed(this, 10000);
            }
        }, delayTime);
    }

    private void stopPing() {
        webSocket.sendClose();
        pingHandler.removeCallbacksAndMessages(null);
    }

    private boolean peerIdExistence() {
        boolean isPeerIdExistence;
        String peerId = sharedPrefs.getString(AsyncConstant.Constants.PEER_ID, null);
        setPeerId(peerId);
        isPeerIdExistence = peerId != null;
        return isPeerIdExistence;
    }

    //Save peerId in the SharedPreferences
    private void savePeerId(String peerId) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(AsyncConstant.Constants.PEER_ID, peerId);
        editor.apply();
    }

    //Save deviceId in the SharedPreferences
    private void saveDeviceId(String deviceId) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(AsyncConstant.Constants.DEVICE_ID, deviceId);
        editor.apply();
    }

    private String getDeviceId() {
        return sharedPrefs.getString(AsyncConstant.Constants.DEVICE_ID, null);
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    private String getPeerId() {
        return peerId;
    }

    private void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    private String getAppId() {
        return appId;
    }

    private void setAppId(String appId) {
        this.appId = appId;
    }

    public String getState() {
        return state;
    }

    private void setState(String state) {
        this.state = state;
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

    private String getServerAddress() {
        return serverAddress;
    }

    private void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    private void setOnError(String onError) {
        this.onError = onError;
    }

    public String getOnError() {
        return onError;
    }

    private void setonConnectError(Exception exception) {
        this.onConnectException = exception;
    }

    public Exception getOnConnectError() {
        return onConnectException;
    }
}

