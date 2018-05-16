package ir.fanap.chat.sdk.application;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SocketPresenterInstrumentedTest {

    @Rule
    public ActivityTestRule<ExampleActivity> mActivityRule = new ActivityTestRule<>(ExampleActivity.class);

    private static SocketPresenter socketPresenter;

    @Mock
    private static SocketContract.view view;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @BeforeClass
    public static void setUp() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();

        socketPresenter = new SocketPresenter(view, appContext);
    }

    @Test
    public void useAppContext() {
        // Context of the app under test
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("ir.fanap.chat.sdk", appContext.getPackageName());
    }

    @MediumTest
    public void ShouldConnectToSocket(){
        socketPresenter.connect("ws://172.16.110.235:8003/ws", "UIAPP");

        assertEquals("OPEN",socketPresenter.getState());
    }

    @MediumTest
    public void ShouldReceiveMessages() {
        Mockito.when(socketPresenter.getMessage()).thenReturn("");
    }

    @MediumTest
    public void ShouldSendEmptyMessage() {

    }

    @Test
    public void ShouldCloseSocketConnection() {

    }
}
