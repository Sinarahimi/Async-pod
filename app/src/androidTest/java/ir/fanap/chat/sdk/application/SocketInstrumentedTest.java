package ir.fanap.chat.sdk.application;

import android.content.Context;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingPolicies;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;

import ir.fanap.chat.sdk.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;

@RunWith(AndroidJUnit4.class)
public class SocketInstrumentedTest {

    @Rule
    public ActivityTestRule<ExampleActivity> mActivityRule = new ActivityTestRule<>(ExampleActivity.class);

    private static SocketPresenter socketPresenter;

    private static SocketContract.view view;

    private IdlingResource mIdlingResource;

    @BeforeClass
    public static void setUp() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        MockitoAnnotations.initMocks(appContext);
        view = Mockito.mock(SocketContract.view.class);
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
        socketPresenter.connect("ws://172.16.110.235:8003/ws", "UIAPP");
        onView(withId(R.id.button)).perform(click());
        onView(withId(R.id.textViewstate)).check(matches(withText("OPEN")));

    }

    @Test
    public void ShouldReceiveMessages() {
        socketPresenter.connect("ws://172.16.110.235:8003/ws", "UIAPP");
        onView(withId(R.id.button)).perform(click());

        Mockito.verify(view, times(1)).messageCalled();
        assertEquals(Mockito.anyString(), socketPresenter.getMessage());
    }

    @Test
    public void ShouldSendEmptyMessage() {

    }

    @Test
    public void ShouldCloseSocketConnection() {

    }
}
