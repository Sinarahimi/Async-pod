package ir.fanap.chat.sdk.application;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SocketPresenterTest {

    @Mock
    private SocketContract.view view;

    private SocketPresenter socketPresenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        //TODO what should i do with context that socket presenter need
//        socketPresenter = new SocketPresenter(view,)
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getMessage() {

//        SocketPresenter socketPresenter = new SocketPresenter("","");
//        Assert.assertEquals("",socketPresenter.getMessage());
    }

    @Test
    public void sendMessage() {
        socketPresenter.sendMessage("this is test", 3);

    }

    @Test
    public void getErrorMessage() {
    }

    @Test
    public void getState() {

    }


}