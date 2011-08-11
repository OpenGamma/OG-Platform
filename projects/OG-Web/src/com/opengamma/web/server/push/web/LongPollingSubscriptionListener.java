/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import com.google.common.base.Joiner;
import com.opengamma.web.server.push.subscription.SubscriptionEvent;
import com.opengamma.web.server.push.subscription.SubscriptionListener;
import org.eclipse.jetty.continuation.Continuation;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link SubscriptionListener} that pushes updates over a long-polling HTTP connection using Jetty's continuations.
 * If any updates arrive while there is no connection they are queued and sent as soon as the connection
 * is re-established.  If multiple updates for the same object are queued only one is sent.  All updates
 * only contain the ID of the updated object so they are identical.
 * TODO listener on the continuation? timeout?
 */
class LongPollingSubscriptionListener implements SubscriptionListener {

  private final Object _lock = new Object();
  private final Set<String> _updates = new HashSet<String>();

  private Continuation _continuation;

  @Override
  public void itemUpdated(SubscriptionEvent event) {
    synchronized (_lock) {
      if (_continuation != null) {
        sendUpdate(event.getUrl());
      } else {
        _updates.add(event.getUrl());
      }
    }
  }

  public void connect(Continuation continuation) {
    synchronized (_lock) {
      // what if _continuation isn't null? shouldn't happen but is possible. is it an error?
      if (_continuation != null) {
        continuation.complete();
      }
      _continuation = continuation;
      // if there are updates queued sent them immediately otherwise save the continuation until an update
      if (!_updates.isEmpty()) {
        sendUpdate(Joiner.on("\n").join(_updates));
      }
    }
  }

  private void sendUpdate(String urls) {
    _continuation.setAttribute(LongPollingServlet.RESULTS, urls);
    _continuation.resume();
    _continuation = null;
  }

  // TODO better name? isBlocking? isWaiting?
  // for testing
  boolean isConnected() {
    synchronized (_lock) {
      return _continuation != null;
    }
  }
}
