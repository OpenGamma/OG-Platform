/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.google.common.base.Objects;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.push.rest.MasterType;

import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TODO what's the policy on arg checking? public API only?
 */
/* package */ class ConnectionManagerImpl implements ConnectionManager {

  //private static final Logger s_logger = LoggerFactory.getLogger(RestUpdateManagerImpl.class);

  private static final long DEFAULT_TIMEOUT_CHECK_PERIOD = 60000;

  /** Clients are disconnected if they haven't been heard of after fine minutes */
  private static final long DEFAULT_TIMEOUT = 300000;

  // TODO a better way to generate client IDs
  private final AtomicLong _clientConnectionId = new AtomicLong();
  private final ChangeManager _changeManager;
  private final ViewportFactory _viewportFactory;
  private final LongPollingConnectionManager _longPollingConnectionManager;
  private final long _timeout;
  private final long _timeoutCheckPeriod;
  /** Connections keyed on client ID */
  private final Map<String, ClientConnection> _connectionsByClientId = new ConcurrentHashMap<String, ClientConnection>();
  /** Connections keyed on viewport ID */
  private final Map<String, ClientConnection> _connectionsByViewportId = new ConcurrentHashMap<String, ClientConnection>();
  private final Timer _timer = new Timer();
  private final MasterChangeManager _masterChangeManager;

  // TODO map of ChangeManagers keyed on MasterType? or a class that encapsulates that logic? for query updates
  public ConnectionManagerImpl(ChangeManager changeManager,
                               MasterChangeManager masterChangeManager,
                               ViewportFactory viewportFactory,
                               LongPollingConnectionManager longPollingConnectionManager) {
    this(changeManager, masterChangeManager, viewportFactory, longPollingConnectionManager, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_CHECK_PERIOD);
  }

  public ConnectionManagerImpl(ChangeManager changeManager,
                               MasterChangeManager masterChangeManager,
                               ViewportFactory viewportFactory,
                               LongPollingConnectionManager longPollingConnectionManager,
                               long timeout,
                               long timeoutCheckPeriod) {
    _changeManager = changeManager;
    _viewportFactory = viewportFactory;
    _longPollingConnectionManager = longPollingConnectionManager;
    _timeout = timeout;
    _timeoutCheckPeriod = timeoutCheckPeriod;
    _masterChangeManager = masterChangeManager;
  }

  // handshake method that returns the client ID, must be called before any of the long-polling subscribe methods

  /**
   * Creates a new connection for a client and returns its client ID.  The client ID should be used by the client
   * when subscribing for asynchronous updates.  A connection typically corresponds to a single browser tab or
   * window.  A user can have multiple simultaneous connections.
   * @param userId The ID of the user creating the connection
   * @return The client ID of the new connection, must be supplied by the client when subscribing for updates
   */
  @Override
  public String openConnection(String userId) {
    // TODO check args
    String clientId = Long.toString(_clientConnectionId.getAndIncrement());
    LongPollingUpdateListener updateListener = _longPollingConnectionManager.handshake(userId, clientId);
    ConnectionTimeoutTask timeoutTask = new ConnectionTimeoutTask(this, userId, clientId, _timeout);
    ClientConnection connection = new ClientConnection(userId, clientId, updateListener, _viewportFactory, timeoutTask);
    _changeManager.addChangeListener(connection);
    _masterChangeManager.addChangeListener(connection);
    _connectionsByClientId.put(clientId, connection);
    _timer.scheduleAtFixedRate(timeoutTask, _timeoutCheckPeriod, _timeoutCheckPeriod);
    return clientId;
  }

  // TODO why is this public? does it need to be?
  @Override
  public void closeConnection(String userId, String clientId) {
    ClientConnection connection = getConnectionByClientId(userId, clientId);
    _connectionsByClientId.remove(clientId);
    _changeManager.removeChangeListener(connection);
    _masterChangeManager.removeChangeListener(connection);
    _longPollingConnectionManager.timeout(clientId);
    connection.disconnect();
  }

  @Override
  public void subscribe(String userId, String clientId, UniqueId uid, String url) {
    getConnectionByClientId(userId, clientId).subscribe(uid, url);
  }

  @Override
  public void subscribe(String userId, String clientId, MasterType masterType, String url) {
    getConnectionByClientId(userId, clientId).subscribe(masterType, url);
  }

  @Override
  public Viewport getViewport(String userId, String clientId, String viewportId) {
    return getConnectionByViewportId(userId, viewportId).getViewport(viewportId);
  }

  @Override
  public void createViewport(String userId,
                             String clientId,
                             ViewportDefinition viewportDefinition,
                             String viewportId,
                             String dataUrl,
                             String gridUrl) {
    ClientConnection connection = getConnectionByClientId(userId, clientId);
    connection.createViewport(viewportDefinition, viewportId, dataUrl, gridUrl);
    _connectionsByViewportId.put(viewportId, connection);
  }

  private ClientConnection getConnectionByClientId(String userId, String clientId) {
    // TODO user logins
    //ArgumentChecker.notEmpty(userId, "userId");
    ArgumentChecker.notEmpty(clientId, "clientId");
    ClientConnection connection = _connectionsByClientId.get(clientId);
    if (connection == null) {
      throw new DataNotFoundException("Unknown client ID: " + clientId);
    }
    if (!Objects.equal(userId, connection.getUserId())) {
      throw new DataNotFoundException("User ID " + userId + " is not associated with client ID " + clientId);
    }
    return connection;
  }

  private ClientConnection getConnectionByViewportId(String userId, String viewportUrl) {
    ClientConnection connection = _connectionsByViewportId.get(viewportUrl);
    if (connection == null) {
      throw new DataNotFoundException("Unknown viewport ID: " + viewportUrl);
    }
    if (!Objects.equal(userId, connection.getUserId())) {
      throw new DataNotFoundException("User ID " + userId + " is not associated with viewport " + viewportUrl);
    }
    return connection;
  }

}
