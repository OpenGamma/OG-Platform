/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.push;

import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.Timeout;
import com.opengamma.web.analytics.rest.MasterType;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION)
public class ConnectionManagerImplTest {

  @Test(expectedExceptions = DataNotFoundException.class)
  public void timeout() throws InterruptedException {
    // update manager with non-default short timeouts
    final MasterChangeManager masterChangeManager = new MasterChangeManager(Collections.<MasterType, ChangeProvider>emptyMap());
    final ConnectionManagerImpl connectionManager = new ConnectionManagerImpl(mock(ChangeManager.class),
                                                                        masterChangeManager,
                                                                        new LongPollingConnectionManager(),
                                                                        Timeout.standardTimeoutMillis() / 2,
                                                                        Timeout.standardTimeoutMillis() / 4);
    // connection that will be allowed to time out
    final String clientId = connectionManager.clientConnected("userId");
    // should complete normally
    connectionManager.subscribe("userId", clientId, UniqueId.of("Tst", "123"), "url");
    // wait until timeout
    Thread.sleep(Timeout.standardTimeoutMillis());
    // connection should have timed out, exception will be thrown because clientId is unknown
    connectionManager.subscribe("userId", clientId, UniqueId.of("Tst", "1234"), "url");
  }
}
