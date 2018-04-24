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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
    public void showOnConncetError(WebSocket websocket, WebSocketException exception) {

    }
}
