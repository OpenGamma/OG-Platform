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

import java.util.LinkedList;
import java.util.Queue;

/**
 * TODO needs to queue updates if there is no continuation and dispatch when a connection is established
 * TODO listener on the continuation? timeout?
 */
class LongPollingSubscriptionListener implements SubscriptionListener {

  private final Object _lock = new Object();
  private final Queue<String> _updates = new LinkedList<String>();

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
      _continuation = continuation;
      // if there are updates queued sent them immediately otherwise save the continuation until an update
      if (!_updates.isEmpty()) {
        sendUpdate(Joiner.on("\n").join(_updates));
      }
    }
  }

  private void sendUpdate(String url) {
    _continuation.setAttribute(SubscriptionServlet.RESULTS, url);
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
