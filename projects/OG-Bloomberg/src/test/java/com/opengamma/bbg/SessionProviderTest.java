/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.fail;

import java.util.concurrent.atomic.AtomicReference;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.bloomberglp.blpapi.EventHandler;
import com.bloomberglp.blpapi.Session;
import com.bloomberglp.blpapi.SessionOptions;
import com.opengamma.livedata.ConnectionUnavailableException;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link SessionProvider}
 */
@Test(groups = TestGroup.UNIT)
public class SessionProviderTest {

  private SessionOptions sessionOptions() {
    final SessionOptions options = new SessionOptions();
    options.setServerHost("invalid-host-name");
    return options;
  }

  private void assertSessionFailure(final SessionProvider provider, final String message) {
    try {
      provider.getSession();
      fail();
    } catch (ConnectionUnavailableException e) {
      assertEquals(e.getMessage(), message);
    }
  }

  public void testLifecycle() {
    final BloombergConnector connector = new BloombergConnector("Test", sessionOptions());
    try {
      final SessionProvider a = new SessionProvider(connector, "Test");
      assertSessionFailure(a, "Session provider has not been started");
      a.start();
      assertSessionFailure(a, "Failed to open session");
      assertSessionFailure(a, "No Bloomberg connection is available");
      a.stop();
      assertSessionFailure(a, "Session provider has not been started");
    } finally {
      connector.close();
    }
  }

  public void testAvailabilityNotifications() throws Exception {
    final AtomicReference<Session> session = new AtomicReference<Session>();
    final BloombergConnector connector = new BloombergConnector("Test", sessionOptions()) {
      @Override
      public Session createOpenSession() {
        return session.get();
      }

      @Override
      public Session createOpenSession(EventHandler eventHandler) {
        return createOpenSession();
      }

    };
    try {
      final SessionProvider a = new SessionProvider(connector, "A");
      final SessionProvider b = new SessionProvider(connector, "B");
      a.start();
      b.start();
      // Initial failure - can't connect
      assertSessionFailure(a, "Bloomberg service failed to start: A");
      assertSessionFailure(b, "Bloomberg service failed to start: B");
      // Cached failure - still can't connect
      assertSessionFailure(a, "No Bloomberg connection is available");
      assertSessionFailure(b, "No Bloomberg connection is available");
      // Force A to reconnect (successfully)
      final Session sessionImpl = Mockito.mock(Session.class);
      Mockito.when(sessionImpl.openService(Mockito.anyString())).thenReturn(true);
      session.set(sessionImpl);
      a.stop();
      a.start();
      assertSame(a.getSession(), session.get());
      // Cached failures is now cleared for B
      assertSame(b.getSession(), session.get());
    } finally {
      connector.close();
    }
  }

}
