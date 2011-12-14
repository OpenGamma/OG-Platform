/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.UniqueId;
import com.opengamma.web.server.push.rest.MasterType;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;

public class ConnectionManagerImplTest {

  @Test(expectedExceptions = DataNotFoundException.class)
  public void timeout() throws InterruptedException {
    // update manager with non-default short timeouts
    MasterChangeManager masterChangeManager = new MasterChangeManager(Collections.<MasterType, ChangeProvider>emptyMap());
    ConnectionManagerImpl connectionManager = new ConnectionManagerImpl(mock(ChangeManager.class),
                                                                        masterChangeManager,
                                                                        mock(ViewportManager.class),
                                                                        new LongPollingConnectionManager(),
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
