/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import com.opengamma.web.server.push.subscription.RestUpdateListener;
import org.eclipse.jetty.continuation.Continuation;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link RestUpdateListener} that pushes updates over a long-polling HTTP connection using Jetty's continuations.
 * If any updates arrive while there is no connection they are queued and sent as soon as the connection
 * is re-established.  If multiple updates for the same object are queued only one is sent.  All updates
 * only contain the REST URL of the updated object so they are identical.
 * TODO listener on the continuation? timeout?
 */
/* package */ class LongPollingUpdateListener implements RestUpdateListener {

  private static final Logger s_logger = LoggerFactory.getLogger(LongPollingUpdateListener.class);

  /** Key for the array of updated URLs in the JSON */
  static final String UPDATES = "updates";

  private final Object _lock = new Object();
  private final Set<String> _updates = new HashSet<String>();

  private Continuation _continuation;

  @Override
  public void itemUpdated(String url) {
    synchronized (_lock) {
      if (_continuation != null) {
        try {
          sendUpdate(formatUpdate(url));
        } catch (JSONException e) {
          // this shouldn't ever happen, the updates are all URLs
          s_logger.warn("Unable to format updates as JSON. updates: " + _updates, e);
        }
      } else {
        _updates.add(url);
      }
    }
  }

  /* package */ void connect(Continuation continuation) {
    synchronized (_lock) {
      // what if _continuation isn't null? shouldn't happen but is possible. is it an error?
      if (_continuation != null) {
        continuation.complete();
      }
      _continuation = continuation;
      // if there are updates queued sent them immediately otherwise save the continuation until an update
      if (!_updates.isEmpty()) {
        try {
          sendUpdate(formatUpdate(_updates));
        } catch (JSONException e) {
          // this shouldn't ever happen, the updates are all URLs
          s_logger.warn("Unable to format updates as JSON. updates: " + _updates, e);
        }
        _updates.clear();
      }
    }
  }

  private void sendUpdate(String urls) {
    _continuation.setAttribute(LongPollingServlet.RESULTS, urls);
    _continuation.resume();
    _continuation = null;
  }

  // for testing
  /* package */ boolean isConnected() {
    synchronized (_lock) {
      return _continuation != null;
    }
  }

  private String formatUpdate(String url) throws JSONException {
    return new JSONObject().put(UPDATES, new Object[]{url}).toString();
  }

  private String formatUpdate(Collection<String> urls) throws JSONException {
    return new JSONObject().put(UPDATES, urls).toString();
  }

  /* package */ void disconnect() {
    // TODO any possibility of deadlocks?
    synchronized (_lock) {
      if (_continuation != null) {
        _continuation.complete();
      }
    }
  }
}
