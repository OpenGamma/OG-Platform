/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.push;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jetty.continuation.Continuation;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.util.ArgumentChecker;

/**
 * {@link UpdateListener} that pushes updates over a long-polling HTTP connection using Jetty's continuations.
 * If any updates arrive while there is no connection they are queued and sent as soon as the connection
 * is re-established.  If multiple updates for the same object are queued only one is sent.  All updates
 * only contain the REST URL of the updated object so they are identical.
 */
/* package */ class LongPollingUpdateListener implements UpdateListener {

  private static final Logger s_logger = LoggerFactory.getLogger(LongPollingUpdateListener.class);

  /** Key for the array of updated URLs in the JSON */
  static final String UPDATES = "updates";

  private final Object _lock = new Object();
  private final Set<Object> _updates = Sets.newHashSet();
  private final String _userId;
  private final ConnectionTimeoutTask _timeoutTask;
  private final String _clientId;

  private Continuation _continuation;

  /**
   * Creates a new listener for a user.
   * @param clientId Client ID of the connection
   * @param userId Login ID of the user
   * @param timeoutTask Connection timeout task that the listener must reset every time the connection is set up
   */
  /* package */ LongPollingUpdateListener(String clientId, String userId, ConnectionTimeoutTask timeoutTask) {
    _clientId = clientId;
    ArgumentChecker.notEmpty(clientId, "clientId");
    //ArgumentChecker.notEmpty(userId, "userId");
    ArgumentChecker.notNull(timeoutTask, "timeoutTask");
    _userId = userId;
    _timeoutTask = timeoutTask;
  }

  /**
   * Publishes {@code url} to the client as JSON.  If the client is connected (i.e. this listener has a
   * continuation) the URL is sent immediately.  If the client isn't connected it is queued until the
   * connection is re-established.
   * @param callbackId REST URL of the item that has been updated
   */
  @Override
  public void itemUpdated(Object callbackId) {
    ArgumentChecker.notNull(callbackId, "url");
    synchronized (_lock) {
      if (_continuation != null) {
        try {
          sendUpdate(formatUpdate(callbackId));
        } catch (JSONException e) {
          // this shouldn't ever happen
          s_logger.warn("Unable to format callback ID as JSON: " + callbackId, e);
        }
      } else {
        _updates.add(callbackId);
      }
    }
  }

  /**
   * Publishes {@code urls} to the client as JSON.  If the client is connected (i.e. this listener has a
   * continuation) the URLs are sent immediately.  If the client isn't connected they are queued until the
   * connection is re-established.
   * @param callbackIds REST URLs of the items that have been updated
   */
  @Override
  public void itemsUpdated(Collection<?> callbackIds) {
    ArgumentChecker.notNull(callbackIds, "callbackIds");
    if (callbackIds.isEmpty()) {
      return;
    }
    synchronized (_lock) {
      if (_continuation != null) {
        try {
          sendUpdate(formatUpdate(callbackIds));
        } catch (JSONException e) {
          // this shouldn't ever happen, the updates are all URLs
          s_logger.warn("Unable to format URLs as JSON. URLs: " + callbackIds, e);
        }
      } else {
        _updates.addAll(callbackIds);
      }
    }
  }

  /**
   * Invoked when a client establishes a long-polling HTTP connection.
   * @param continuation The connection's continuation
   */
  /* package */ void connect(Continuation continuation) {
    synchronized (_lock) {
      s_logger.debug("Long polling connection established, resetting timeout task {}", _timeoutTask);
      _timeoutTask.reset();
      _continuation = continuation;
      _continuation.setTimeout(10000);
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

  /**
   * Adds {@code urls} to the connection's continuation and resumes it so the response is sent to the client.
   * @param update URLs of the changed items
   */
  private void sendUpdate(String update) {
    _continuation.setAttribute(LongPollingServlet.RESULTS, update);
    _continuation.resume();
    _continuation = null;
    s_logger.debug("Sent update to client {}: {}", _clientId, update);
  }

  // for testing
  /* package */ boolean isConnected() {
    synchronized (_lock) {
      return _continuation != null;
    }
  }

  /**
   * Formats a URL as JSON.
   * @param url A URL
   * @return {@code {updates: [url]}}
   * @throws JSONException Never
   */
  private String formatUpdate(Object url) throws JSONException {
    return new JSONObject().put(UPDATES, new Object[]{url}).toString();
  }

  /**
   * Formats URLs as JSON.
   * @param urls URLs
   * @return {@code {updates: [url1, url2, ...]}}
   * @throws JSONException Never
   */
  private String formatUpdate(Collection<?> urls) throws JSONException {
    return new JSONObject().put(UPDATES, urls).toString();
  }

  /**
   * Closes this listener's HTTP connection.
   */
  /* package */ void disconnect() {
    synchronized (_lock) {
      if (_continuation != null && _continuation.isSuspended()) {
        _continuation.complete();
      }
      _continuation = null;
    }
  }

  /**
   * @return Login ID of the user who owns this listener's connection
   */
  /* package */ String getUserId() {
    return _userId;
  }

  /**
   * Invoked when this listener's continuation times out before any data is sent.
   * @param continuation The continuation that timed out - should be this listener's continuation.
   */
  /* package */ void timeout(Continuation continuation) {
    synchronized (_lock) {
      if (continuation == _continuation) {
        _continuation = null;
      }
    }
  }
}
