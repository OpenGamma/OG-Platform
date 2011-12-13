package com.opengamma.web.server.push;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.UniqueId;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;

/**
 *
 */
public class ConnectionManagerImplTest {

  @Test(expectedExceptions = DataNotFoundException.class)
  public void timeout() throws InterruptedException {
    // update manager with non-default short timeouts
    ConnectionManagerImpl connectionManager = new ConnectionManagerImpl(mock(ChangeManager.class),
                                                                        mock(MasterChangeManager.class),
                                                                        mock(ViewportManager.class),
                                                                        mock(LongPollingConnectionManager.class),
                                                                        1000,
                                                                        500);
    // connection that will be allowed to time out
    String clientId = connectionManager.clientConnected("userId");
    // should complete normally
    connectionManager.subscribe("userId", clientId, UniqueId.of("Tst", "123"), "url");
    // wait until timeout
    Thread.sleep(2000);
    // connection should have timed out, exception will be thrown because clientId is unknown
    connectionManager.subscribe("userId", clientId, UniqueId.of("Tst", "1234"), "url");
  }
}
