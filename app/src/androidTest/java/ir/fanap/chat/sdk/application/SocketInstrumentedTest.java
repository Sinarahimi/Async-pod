package ir.fanap.chat.sdk.application;

import android.content.Context;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingPolicies;
import android.support.test.espresso.IdlingResource;
import android.support.test.runner.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SocketInstrumentedTest {

    private static SocketPresenter socketPresenter;

    private static SocketContract.view view;

    @BeforeClass
    public static void setUp()  {
        Context appContext = InstrumentationRegistry.getTargetContext();
        MockitoAnnotations.initMocks(appContext);
        socketPresenter = new SocketPresenter(view, appContext);
    }

    @Test
    public void useAppContext() {
        // Context of the app under test
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("ir.fanap.chat.sdk", appContext.getPackageName());
    }

    @Test
    public void ShouldConnectToSocket() throws Exception {
        socketPresenter.connect("ws://172.16.110.235:8003/ws","UIAPP");
//        Mockito.when(socketPresenter.getState()).thenReturn("OPEN");
//        Mockito.verify(socketPresenter,Mockito.atLeastOnce()).getState();

        // Make sure Espresso does not time out
        IdlingPolicies.setMasterPolicyTimeout(4000 * 2, TimeUnit.MILLISECONDS);
        IdlingPolicies.setIdlingResourceTimeout(4000 * 2, TimeUnit.MILLISECONDS);

        // Now we wait
        IdlingResource idlingResource = new IdlingResource() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public boolean isIdleNow() {
                return false;
            }

            @Override
            public void registerIdleTransitionCallback(ResourceCallback callback) {

            }
        };
        Espresso.registerIdlingResources(idlingResource);
        assertThat(socketPresenter.getState(),notNullValue());

        assertEquals(true,socketPresenter.isSocketOpen());

//        android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//            }
//        },2000);
//        assertTrue(socketPresenter.isSocketOpen());
//        assertEquals(socketPresenter.getState(),"OPEN");
//        System.out.println(socketPresenter.getState());
    }

    @Test
    public void ShouldReceiveMessages() {
        assertEquals("", socketPresenter.getMessage());
    }

    @Test
    public void ShouldSendEmptyMessage() {

    }

    @Test
    public void ShouldCloseSocketConnection() {

    }
}
