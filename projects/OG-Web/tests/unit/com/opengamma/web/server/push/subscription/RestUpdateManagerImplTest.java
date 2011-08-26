package com.opengamma.web.server.push.subscription;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.UniqueId;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;

/**
 *
 */
public class RestUpdateManagerImplTest {

  @Test(expectedExceptions = DataNotFoundException.class)
  public void timeout() throws InterruptedException {
    // update manager with non-default short timeouts
    RestUpdateManagerImpl updateManager = new RestUpdateManagerImpl(mock(ChangeManager.class), mock(ViewportFactory.class), 1000, 500);
    // connection that will be allowed to time out
    String clientId = updateManager.newConnection("userId", mock(RestUpdateListener.class), mock(TimeoutListener.class));
    // should complete normally
    updateManager.subscribe("userId", clientId, UniqueId.of("Tst", "123"), "url");
    // wait until timeout
    Thread.sleep(2000);
    // connection should have timed out, exception will be thrown because clientId is unknown
    updateManager.subscribe("userId", clientId, UniqueId.of("Tst", "1234"), "url");
  }
}
