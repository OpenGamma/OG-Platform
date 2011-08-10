/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TODO what's the policy on arg checking? public API only?
 * TODO refactor so stack isn't so deep? create objects in here and pass into connections? will that work?
 */
public class SubscriptionManagerImpl implements SubscriptionManager {

  private static final Logger s_logger = LoggerFactory.getLogger(SubscriptionManagerImpl.class);

  private final AtomicLong _clientConnectionId = new AtomicLong();

  private final ViewportFactory _viewportFactory;

  // TODO what has this been replaced with?
  //private final MasterChangeManager _masterChangeManager;
  // TODO what map impl? concurrent? or handle concurrency somewhere else?
  /** Connections keyed on client ID */
  private final Map<String, ClientConnection> _connections = new HashMap<String, ClientConnection>();

  public SubscriptionManagerImpl(/*MasterChangeManager masterChangeManager, */ViewportFactory viewportFactory) {
    //_masterChangeManager = masterChangeManager;
    _viewportFactory = viewportFactory;
  }

  // handshake method that returns the client ID, must be called before any of the long-polling subscribe methods
  @Override
  public String newConnection(String userId, SubscriptionListener listener) {
    // TODO check args
    // TODO  TimerTask to close connection if it times out? or should that live downstream? or both?
    String clientId = Long.toString(_clientConnectionId.getAndIncrement());
    // TODO does the connection need to know its client ID?
    ClientConnection connection = new ClientConnection(clientId, userId, listener, _viewportFactory);
    _connections.put(clientId, connection);
    return clientId;
  }

  @Override
  public void closeConnection(String userId, String clientId) {
    
  }

  //
  // TODO need to generate a URL to GET the viewport data. lastest view data available from /viewport/{viewportId}?
  public String createViewportSubscription(String userId, String clientId, ViewportSubscriptionRequest request) {
    getConnection(userId, clientId).createViewportSubscription(request);
    return viewportId(clientId);
  }

  private String viewportId(String clientId) {
    // TODO need a new unique viewport ID
    //return ;
    throw new UnsupportedOperationException("TODO");
  }

  public void activateViewportSubscription(String userId, String clientId, String viewportId) {
    getConnection(userId, clientId).activateViewportSubscription(viewportId);
  }

  public void cancelViewportSubscription(String userId, String clientId, String viewportId) {

  }

  public void createEntitySubscription(String userId, String clientId, UniqueId entityId) {

  }

  // TODO probably won't want to cancel these, just let them fire and die?
  // the whole point of the transient subscription is that it wouldn't need to be cancelled
  // or should a new entity subscription for the same client ID cancel the existing one?
  /*void cancelEntitySubscription(String userId, String clientId, UniqueId entityId) {
    
  }*/

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

  // TODO where will the logic live to know which of these to call?
  // SubscriptionRequest? with single method subscribe(SubscriptionManager)? impls know which method applies to them
  // JSONSubscriptionRequestFactory creates them? or fromJSON method on SubscriptionRequest?
  /* TODO subscribe methods - all need a user ID and client ID arg
  new view subs - handle, details
  reactivate existing view sub - handle
  entity sub - URL (or unique ID?)
  cancel view sub - handle - is this necessary?
  cancel entity sub - URL (unique ID?) - is this necessary
  */

  // TODO method for looking up latests results for a ViewSubscription (i.e. the subset of the results which the client is interested in)
  // TODO return type
  public ViewportResults getLatestViewportResults(String userId, String clientId, String viewportId) {
    // TODO what if the viewportId is stale, i.e. the subscription has changed?  should the viewport persist? 404?
    throw new UnsupportedOperationException("TODO");
  }
}
