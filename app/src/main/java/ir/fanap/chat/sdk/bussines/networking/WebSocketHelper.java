package ir.fanap.chat.sdk.bussines.networking;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
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
    private static final String PREFERENCE = "PREFERENCE";
    private static final String PEER_ID = "PEER_ID";
    private static final String DEVICE_ID = "DEVICE_ID";
    private boolean isDeviceRegister = false;
    private boolean isServerRegister = false;
    private static SharedPreferences sharedPrefs;
    private MessageWrapperVo messageWrapperVo;
    private static Moshi moshi;
    private String errorMessage;
    private String message;
    private String state;
    private String appId;
    private String peerId;
    private String deviceID;
    private MutableLiveData<String> stateLiveData = new MutableLiveData<>();

    private WebSocketHelper() {
    }

    public static WebSocketHelper getInstance(Context context) {
        if (instance == null) {
            sharedPrefs = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
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
        Log.i("onTextMessage", textMessage);

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
                if (!peerIdExistence()) {
                    savePeerId(peerId);
                }

                if (isServerRegister && peerId.equals(getPeerId())) {
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
                    PeerInfo peerInfo = new PeerInfo();
                    peerInfo.setRenew(true);
                    peerInfo.setAppId(getAppId());
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
                Log.i("Ready for ping", textMessage);
                isServerRegister = true;
                websocket.setPingInterval(2 * 1000);
                break;
        }
    }

    @Override
    public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
        super.onStateChanged(websocket, newState);
        //Error on line below

        stateLiveData.postValue(newState.toString());
        setState(state);
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
        setAppId(appId);
        try {
            webSocket = webSocketFactory
                    .setConnectionTimeout(TIMEOUT)
                    .createSocket(socketServerAddress).setPingInterval(3 * 1000)
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

            uniqueID = sharedPrefs.getString(PREFERENCE, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREFERENCE, uniqueID);
                editor.apply();
            }
        }
        return uniqueID;
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
    }

    @Override
    public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        super.onCloseFrame(websocket, frame);
        Log.e("onCloseFrame", frame.getCloseReason());
        reConnect();
    }

    public LiveData<String> getStateLiveData() {
        return stateLiveData;
    }

    /**
     * If peerIdExistence we send refresh to the
     * Async else we send renew to the Async to
     * get the new PeerId
     */
    private void reConnect() {
        //TODO need to discussed
        //TODO add device id
        String message;
        if (peerIdExistence()) {
            PeerInfo peerInfo = new PeerInfo();
            peerInfo.setAppId(getAppId());
//            peerInfo.setDeviceId();
            peerInfo.setRefresh(true);
            JsonAdapter<PeerInfo> jsonPeerMessageAdapter = moshi.adapter(PeerInfo.class);
            String jason = jsonPeerMessageAdapter.toJson(peerInfo);
            message = getMessageWrapper(moshi, jason, AsyncMessageType.MessageType.PING);
            webSocket.sendText(message);
        } else {
            //TODO add device id
            PeerInfo peerInfo = new PeerInfo();
            peerInfo.setAppId(getAppId());
//            peerInfo.setDeviceId();
            peerInfo.setRenew(true);
            JsonAdapter<PeerInfo> jsonPeerMessageAdapter = moshi.adapter(PeerInfo.class);
            String jason = jsonPeerMessageAdapter.toJson(peerInfo);
            message = getMessageWrapper(moshi, jason, AsyncMessageType.MessageType.PING);
            webSocket.sendText(message);
        }
    }

    /*
     * Remove the peerId
     * */
    public void logOut() {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(PEER_ID, null);
        editor.apply();
        isServerRegister = false;
        isDeviceRegister = false;
        PeerInfo peerInfo = new PeerInfo();
        peerInfo.setAppId(getAppId());
        peerInfo.setRenew(true);

    }

    private void sendPing() {
    }

    private boolean peerIdExistence() {
        boolean isPeerIdExistence;
        String peerId = sharedPrefs.getString(PEER_ID, null);
        setPeerId(peerId);
        if (peerId == null) {
            isPeerIdExistence = false;
        } else {
            isPeerIdExistence = true;
        }
        return isPeerIdExistence;
    }

    private void savePeerId(String peerId) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(PEER_ID, peerId);
        editor.apply();
    }

    private void saveDeviceId(String deviceId){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(DEVICE_ID, deviceId);
        editor.apply();
    }

    private String getDeviceId(){
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    private String getPeerId(){
        return peerId;
    }

    public void setPeerId(String peerId) {
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
}

