/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

import com.google.common.base.Objects;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TODO what's the policy on arg checking? public API only?
 * TODO CONCURRENCY - completely ignored at the moment
 */
/* package */ class RestUpdateManagerImpl implements RestUpdateManager {

  //private static final Logger s_logger = LoggerFactory.getLogger(RestUpdateManagerImpl.class);

  private static final long DEFAULT_TIMEOUT_CHECK_PERIOD = 60000;

  /** Clients are disconnected if they haven't been heard of after fine minutes */
  private static final long DEFAULT_TIMEOUT = 300000;

  // TODO a better way to generate client IDs
  // TODO might not need an atomic var if all accesses end up being guarded by sync blocks
  private final AtomicLong _clientConnectionId = new AtomicLong();
  private final ChangeManager _changeManager;
  private final ViewportFactory _viewportFactory;
  private final long _timeout;
  private final long _timeoutCheckPeriod;
  private final Object _lock = new Object();

  /** Connections keyed on client ID */
  private final Map<String, ClientConnection> _connectionsByClientId = new HashMap<String, ClientConnection>();
  // TODO how can this be cleaned? this class has no idea when the viewport changes. hmm.
  private final Map<String, ClientConnection> _connectionsByViewportUrl = new HashMap<String, ClientConnection>();
  // TODO what map impl? concurrent? or handle concurrency somewhere else?
  private final Map<String, String> _clientIdsToViewportUrls = new HashMap<String, String>();
  // TODO concurrent?
  private final Map<String, ConnectionTimeoutTask> _timeoutTasks = new HashMap<String, ConnectionTimeoutTask>();
  private final Timer _timer = new Timer();

  // TODO this isn't right, there's a ChangeManager for each master / source / repo
  // TODO aggregate change manager?
  // TODO or a similar interface that also includes MasterType in the event
  // TODO map of ChangeManagers keyed on MasterType? or a class that encapsulates that logic?
  public RestUpdateManagerImpl(ChangeManager changeManager, ViewportFactory viewportFactory) {
    this(changeManager, viewportFactory, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_CHECK_PERIOD);
  }

  public RestUpdateManagerImpl(ChangeManager changeManager, ViewportFactory viewportFactory, long timeout, long timeoutCheckPeriod) {
    _changeManager = changeManager;
    _viewportFactory = viewportFactory;
    _timeout = timeout;
    _timeoutCheckPeriod = timeoutCheckPeriod;
  }

  // handshake method that returns the client ID, must be called before any of the long-polling subscribe methods
  @Override
  public String newConnection(String userId, RestUpdateListener updateListener, TimeoutListener timeoutListener) {
    // TODO check args
    synchronized (_lock) {
      String clientId = Long.toString(_clientConnectionId.getAndIncrement());
      ClientConnection connection = new ClientConnection(userId, clientId, updateListener, _viewportFactory);
      _changeManager.addChangeListener(connection);
      _connectionsByClientId.put(clientId, connection);
      ConnectionTimeoutTask timeoutTask = new ConnectionTimeoutTask(userId, clientId, timeoutListener);
      _timeoutTasks.put(clientId, timeoutTask);
      _timer.scheduleAtFixedRate(timeoutTask, _timeoutCheckPeriod, _timeoutCheckPeriod);
      return clientId;
    }
  }

  @Override
  public void closeConnection(String userId, String clientId) {
    ClientConnection connection;
    synchronized (_lock) {
      connection = getConnectionByClientId(userId, clientId);
      _connectionsByClientId.remove(clientId);
      String viewportUrl = _clientIdsToViewportUrls.remove(clientId);
      if (viewportUrl != null) {
        _connectionsByViewportUrl.remove(viewportUrl);
      }
      _timeoutTasks.remove(clientId);
      _changeManager.removeChangeListener(connection);
      connection.disconnect();
    }
  }

  @Override
  public void subscribe(String userId, String clientId, UniqueId uid, String url) {
    synchronized (_lock) {
      getConnectionByClientId(userId, clientId).subscribe(uid, url);
    }
  }

  @Override
  public Viewport getViewport(String userId, String clientId, String viewportUrl) {
    synchronized (_lock) {
      return getConnectionByViewportUrl(userId, viewportUrl).getViewport(viewportUrl);
    }
  }

  @Override
  public void createViewport(String userId,
                             String clientId,
                             ViewportDefinition viewportDefinition,
                             String viewportUrl,
                             String dataUrl,
                             String gridUrl) {
    synchronized (_lock) {
      ClientConnection connection = getConnectionByClientId(userId, clientId);
      // TODO this isn't great - need to do this in a sync block to avoid a race condition where the connection is keyed
      // by viewport URL before it's created the viewport.  but creating a viewport can involve creating and attaching
      // a view client which might be slow and is locking this class for all clients.
      connection.createViewport(viewportDefinition, viewportUrl, dataUrl, gridUrl);
      _connectionsByViewportUrl.put(viewportUrl, connection);
      _clientIdsToViewportUrls.put(clientId, viewportUrl);
    }
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
    resetTimeout(connection.getClientId());
    return connection;
  }

  private ClientConnection getConnectionByViewportUrl(String userId, String viewportUrl) {
    ClientConnection connection = _connectionsByViewportUrl.get(viewportUrl);
    if (connection == null) {
      throw new DataNotFoundException("Unknown viewport ID: " + viewportUrl);
    }
    if (!Objects.equal(userId, connection.getUserId())) {
      throw new DataNotFoundException("User ID " + userId + " is not associated with viewport " + viewportUrl);
    }
    resetTimeout(connection.getClientId());
    return connection;
  }

  private void resetTimeout(String clientId) {
    _timeoutTasks.get(clientId).reset();
  }

  /* TODO there is a potential problem with the timeout mechanism
  a client's timeout timer is reset whenever the server hears from the client.  this includes requests for data.
  so if the view is producing regular udpates the client will never time out.
  however if a user is looking at a view that doesn't update very often it's possible that the connection.
  would time out even though the client was still interested in the view.
  this would also be true for clients that don't listen for updates and just make occassional requests to get data.
  a fix for this would be to add a heartbeat() method to allow the timer to be reset.
  in the case of the long-polling connection this could be done whenever the HTTP connection is re-established even
  if no data is requested.
  for any persistent connections that might be implemented in future (e.g. web sockets) the connector would have to
  run a timer task to invoke heartbeat().
  there is no simple answer for clients that make requests but don't listen for updates as the server has no way
  of knowing the client is still there.
  */
  class ConnectionTimeoutTask extends TimerTask {

    private final AtomicLong _lastAccessTime = new AtomicLong();
    private final String _userId;
    private final String _clientId;
    private final TimeoutListener _disconnectionListener;

    public ConnectionTimeoutTask(String userId, String clientId, TimeoutListener disconnectionListener) {
      _userId = userId;
      _clientId = clientId;
      _disconnectionListener = disconnectionListener;
      reset();
    }

    void reset() {
      _lastAccessTime.set(System.currentTimeMillis());
    }

    @Override
    public void run() {
      if (System.currentTimeMillis() - _lastAccessTime.get() > _timeout) {
        cancel();
        closeConnection(_userId, _clientId);
        _disconnectionListener.timeout(_clientId);
      }
    }
  }
}
