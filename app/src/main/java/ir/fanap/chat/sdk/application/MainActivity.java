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

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.util.List;

import ir.fanap.chat.sdk.R;

public class MainActivity extends AppCompatActivity implements SocketContract.view {

    //("ws://172.16.110.235:8003/ws", "UIAPP")
    SocketPresenter socketPresenter;
    private static final String SOCKET_SERVER = "ws://172.16.110.235:8003/ws";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.button);
        Button getStateButton = findViewById(R.id.getState);
        Button closeButton = findViewById(R.id.buttonclosesocket);
        Button buttonLogOut = findViewById(R.id.buttonLogOut);

        buttonLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        socketPresenter.closeSocket();
                    }
                },3000);
            }
        });
        getStateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socketPresenter.getState();
            }
        });
        socketPresenter = new SocketPresenter(this, this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socketPresenter.connect("ws://172.16.110.235:8003/ws", "UIAPP");
            }
        });

        socketPresenter.getLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                TextView textView = findViewById(R.id.textViewstate);
                textView.setText(s);
            }
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
