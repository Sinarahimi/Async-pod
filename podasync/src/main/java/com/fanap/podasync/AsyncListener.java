package com.fanap.podasync;

import java.io.IOException;

public interface AsyncListener {

    void OnTextMessage(String textMessage) throws IOException;
}
