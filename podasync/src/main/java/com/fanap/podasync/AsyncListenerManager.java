package com.fanap.podasync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class AsyncListenerManager {
    private final List<AsyncListener> mListeners = new ArrayList<>();
    private boolean mSyncNeeded = true;
    private List<AsyncListener> mCopiedListeners;


    public AsyncListenerManager(){
    }

    public void addListener(AsyncListener listener)
    {
        if (listener == null)
        {
            return;
        }

        synchronized (mListeners)
        {
            mListeners.add(listener);
            mSyncNeeded = true;
        }
    }

    public void callOnTextMessage(String message) throws IOException {
        for (AsyncListener listener : getSynchronizedListeners())
        {
            listener.OnTextMessage(message);
//            try
//            {
//            }
//            catch (Throwable t)
//            {
//                callHandleCallbackError(listener, t);
//            }
        }
    }

    private List<AsyncListener> getSynchronizedListeners()
    {
        synchronized (mListeners)
        {
            if (!mSyncNeeded)
            {
                return mCopiedListeners;
            }

            // Copy mListeners to copiedListeners.
            List<AsyncListener> copiedListeners = new ArrayList<>(mListeners.size());

            for (AsyncListener listener : mListeners)
            {
                copiedListeners.add(listener);
            }

            // Synchronize.
            mCopiedListeners = copiedListeners;
            mSyncNeeded      = false;

            return copiedListeners;
        }
    }
}
