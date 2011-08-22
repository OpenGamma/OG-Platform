/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

import com.google.common.base.Objects;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TODO what's the policy on arg checking? public API only?
 * TODO CONCURRENCY - completely ignored at the moment
 */
/* package */ class RestUpdateManagerImpl implements RestUpdateManager {

  //private static final Logger s_logger = LoggerFactory.getLogger(RestUpdateManagerImpl.class);

  // TODO a better way to generate client IDs
  private final AtomicLong _clientConnectionId = new AtomicLong();
  private final ChangeManager _changeManager;
  private final ViewportFactory _viewportFactory;

  // TODO what map impl? concurrent? or handle concurrency somewhere else?
  /** Connections keyed on client ID */
  private final Map<String, ClientConnection> _connectionsByClientId = new HashMap<String, ClientConnection>();
  // TODO how can this be cleaned? this class has no idea when the viewport changes. hmm.
  private final Map<String, ClientConnection> _connectionsByViewportId = new HashMap<String, ClientConnection>();

  // TODO this isn't right, there's a ChangeManager for each master / source / repo
  // TODO aggregate change manager?
  // TODO or a similar interface that also includes MasterType in the event
  // TODO map of ChangeManagers keyed on MasterType? or a class that encapsulates that logic?
  public RestUpdateManagerImpl(ChangeManager changeManager, ViewportFactory viewportFactory) {
    _changeManager = changeManager;
    _viewportFactory = viewportFactory;
  }

  // handshake method that returns the client ID, must be called before any of the long-polling subscribe methods
  @Override
  public String newConnection(String userId, RestUpdateListener listener) {
    // TODO check args
    // TODO  TimerTask to close connection if it times out? or should that live downstream? or both?
    String clientId = Long.toString(_clientConnectionId.getAndIncrement());
    ClientConnection connection = new ClientConnection(userId, clientId, listener, _viewportFactory);
    _changeManager.addChangeListener(connection);
    _connectionsByClientId.put(clientId, connection);
    return clientId;
  }

  @Override
  public void closeConnection(String userId, String clientId) {
    ClientConnection connection = getConnectionByClientId(userId, clientId);
    _changeManager.removeChangeListener(connection);
    connection.disconnect();
  }

  @Override
  public void subscribe(String userId, String clientId, UniqueId uid, String url) {
    getConnectionByClientId(userId, clientId).subscribe(uid, url);
  }

  @Override
  public Viewport getViewport(String userId, String clientId, String viewportUrl) {
    ClientConnection connection = getConnectionByViewportId(userId, viewportUrl);
    if (connection != null) {
      return connection.getViewport(viewportUrl);
    } else {
      return Viewport.DUMMY;
    }
  }

  public void createViewport(String userId, String clientId, ViewportDefinition viewportDefinition, String viewportUrl) {
    ClientConnection connection = getConnectionByClientId(userId, clientId);
    connection.createViewport(clientId, viewportDefinition, viewportUrl);
    _connectionsByViewportId.put(viewportUrl, connection);
  }

  private ClientConnection getConnectionByClientId(String userId, String clientId) {
    // TODO user logins
    //ArgumentChecker.notEmpty(userId, "userId");
    ArgumentChecker.notEmpty(clientId, "clientId");
    ClientConnection connection = _connectionsByClientId.get(clientId);
    if (connection == null) {
      throw new OpenGammaRuntimeException("Unknown client ID " + clientId);
    }
    if (!Objects.equal(userId, connection.getUserId())) {
      throw new OpenGammaRuntimeException("User ID " + userId + " is not associated with client ID " + clientId);
    }
    return connection;
  }

  private ClientConnection getConnectionByViewportId(String userId, String viewportId) {
    ClientConnection connection = _connectionsByViewportId.get(viewportId);
    if (connection == null) {
      throw new OpenGammaRuntimeException("Unknown viewport ID " + viewportId);
    }
    if (!Objects.equal(userId, connection.getUserId())) {
      throw new OpenGammaRuntimeException("User ID " + userId + " is not associated with viewport " + viewportId);
    }
    return connection;
  }
}
