/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

/* TODO there is a potential problem with the timeout mechanism
a client's timeout timer is reset whenever the server hears from the client.  this includes requests for data.
so if the view is producing regular udpates and the client is fetching the new data then the client will never time out.
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

/**
 * Task that tells a {@link ConnectionManager} when a client connection has been idle for a certain time.
 */
/* package */ class ConnectionTimeoutTask extends TimerTask {

  private final AtomicLong _lastAccessTime = new AtomicLong();
  private final String _userId;
  private final String _clientId;
  private final long _timeout;
  private ConnectionManager _connectionManager;

  /**
   * @param connectionManager The manager of the connection being timed
   * @param userId The ID of the user who owns the connection
   * @param clientId The ID of the connection
   * @param timeout The maximum time in milliseconds the connection is allowed to be idle
   */
  ConnectionTimeoutTask(ConnectionManager connectionManager, String userId, String clientId, long timeout) {
    _connectionManager = connectionManager;
    _userId = userId;
    _clientId = clientId;
    _timeout = timeout;
    reset();
  }

  /**
   * Resets the idle time to zero.
   */
  /* package */ void reset() {
    _lastAccessTime.set(System.currentTimeMillis());
  }

  /**
   * Invokes {@link ConnectionManager#clientConnected(String)} if the idle time exceeds the timeout.
   */
  @Override
  public void run() {
    if (System.currentTimeMillis() - _lastAccessTime.get() > _timeout) {
      cancel();
      _connectionManager.clientDisconnected(_userId, _clientId);
    }
  }
}
