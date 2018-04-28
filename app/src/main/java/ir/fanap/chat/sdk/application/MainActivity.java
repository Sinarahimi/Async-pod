package ir.fanap.chat.sdk.application;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

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
        socketPresenter = new SocketPresenter(this, this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socketPresenter.connect("ws://172.16.110.235:8003/ws", "UIAPP");
            }
        });
    }

    @Override
    public void showMessage(String message) {

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
}
