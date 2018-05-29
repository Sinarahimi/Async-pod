package ir.fanap.chat.sdk.application;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.util.List;

import ir.fanap.chat.sdk.R;

public class ExampleActivity extends AppCompatActivity implements SocketContract.view {

    SocketPresenter socketPresenter;
    private static final String SOCKET_SERVER = "ws://172.16.110.235:8003/ws";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.button);
        Button getStateButton = findViewById(R.id.getState);
        Button closeButton = findViewById(R.id.buttonclosesocket);
        socketPresenter = new SocketPresenter(this, this);

        closeButton.setOnClickListener(v -> {
            Handler handler = new Handler();
            handler.postDelayed(() -> socketPresenter.closeSocket(),3000);
        });

        getStateButton.setOnClickListener(v -> socketPresenter.getLiveState());
        button.setOnClickListener(v -> socketPresenter.connect("ws://172.16.110.235:8003/ws", "UIAPP","async-server","afa51d8291d444072a0831d3a18cb5030"));

        socketPresenter.getLiveData().observe(this, s -> {
            TextView textView = findViewById(R.id.textViewstate);
            textView.setText(s);
        });
    }

    @Override
    public void showMessage(String message) {
        TextView textViewShowMessage = findViewById(R.id.textViewShowMessage);
        textViewShowMessage.setText(message);
        Log.d("message", message);
    }

    @Override
    public void showErrorMessage(String error) {

    }

    @Override
    public void showOnMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) {

    }


    @Override
    public void showOnConnectError(WebSocket websocket, WebSocketException exception) {

    }

    @Override
    public void showSocketState(String state) {

    }

    @Override
    public void showLiveDataState(LiveData state) {

    }

    private void sendMessage(String textMessage, int messageType) {
        socketPresenter.sendMessage(textMessage, messageType);
    }
}
