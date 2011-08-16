/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TODO what's the policy on arg checking? public API only?
 * TODO CONCURRENCY
 */
public class SubscriptionManagerImpl implements SubscriptionManager {

  //private static final Logger s_logger = LoggerFactory.getLogger(SubscriptionManagerImpl.class);

  private final AtomicLong _clientConnectionId = new AtomicLong();
  private final ChangeManager _changeManager;
  private final ViewportFactory _viewportFactory;

  // TODO what map impl? concurrent? or handle concurrency somewhere else?
  /** Connections keyed on client ID */
  private final Map<String, ClientConnection> _connections = new HashMap<String, ClientConnection>();

  public SubscriptionManagerImpl(ChangeManager changeManager, ViewportFactory viewportFactory) {
    _changeManager = changeManager;
    _viewportFactory = viewportFactory;
  }

  // handshake method that returns the client ID, must be called before any of the long-polling subscribe methods
  @Override
  public String newConnection(String userId, SubscriptionListener listener) {
    // TODO check args
    // TODO  TimerTask to close connection if it times out? or should that live downstream? or both?
    String clientId = Long.toString(_clientConnectionId.getAndIncrement());
    ClientConnection connection = new ClientConnection(clientId, userId, listener, _viewportFactory);
    _changeManager.addChangeListener(connection);
    _connections.put(clientId, connection);
    return clientId;
  }

  @Override
  public void closeConnection(String userId, String clientId) {
    ClientConnection connection = getConnection(userId, clientId);
    _changeManager.removeChangeListener(connection);
    connection.disconnect();
  }

  @Override
  public void subscribe(String userId, String clientId, UniqueId uid, String url) {
    getConnection(userId, clientId).subscribe(uid, url);
  }

  // TODO is this still correct? does Viewport create the viewport and notify this class? the request arg should be viewportId
  // TODO maybe the API should be completely different - ViewportsResource manages the clients and this class receives events when they change
  // TODO but what about closing subscriptions? the client connection needs to know about its viewports
  // TODO or the ViewportManager could key on client ID and implement a disconnect() method
  // TODO making everying go through ClientConnection might make concurrency easier to manage
  public String createViewportSubscription(String userId, String clientId, ViewportSubscriptionRequest request) {
    getConnection(userId, clientId).createViewportSubscription(request);
    return viewportId(clientId);
  }

  private String viewportId(String clientId) {
    // TODO need a new unique viewport ID
    //return ;
    throw new UnsupportedOperationException("TODO");
  }

  private ClientConnection getConnection(String userId, String clientId) {
    ArgumentChecker.notEmpty(userId, "userId");
    ArgumentChecker.notEmpty(clientId, "clientId");
    ClientConnection connection = _connections.get(clientId);
    if (connection == null) {
      throw new OpenGammaRuntimeException("Unknown client ID " + clientId);
    }
    if (!userId.equals(connection.getUserId())) {
      throw new OpenGammaRuntimeException("User ID " + userId + " is not associated with client ID " + clientId);
    }
    return connection;
  }

  public ViewportResults getLatestViewportResults(String userId, String clientId, String viewportId) {
    // TODO what if the viewportId is stale, i.e. the subscription has changed?  should the viewport persist? 404?
    throw new UnsupportedOperationException("TODO");
  }
}
