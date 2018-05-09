package ir.fanap.chat.sdk.application;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;

public class SocketPresenterTest {


    @Mock
    private SocketContract.view view;

    private SocketPresenter socketPresenter;

    @Before
    public void setUp() throws Exception {
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