/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.opengamma.financial.user.ClientTracker;
import com.opengamma.financial.user.UserDataTracker;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource which isn't backed by any user objects, just to host /users. Any user requested will
 * be created.
 */
@Path("/data/users")
public class UsersResource {
  
  private final UsersResourceContext _context;
  private final ClientTracker _clientTracker;
  private final UserDataTracker _userDataTracker;
  private final ConcurrentHashMap<String, UserResource> _userMap = new ConcurrentHashMap<String, UserResource>();

  /**
   * Information about the URI injected by JSR-311.
   */
  @Context
  private UriInfo _uriInfo;
  
  public UsersResource(final ClientTracker clientTracker, final UserDataTracker userDataTracker, final UsersResourceContext context) {
    ArgumentChecker.notNull(clientTracker, "clientTracker");
    ArgumentChecker.notNull(userDataTracker, "userDataTracker");
    ArgumentChecker.notNull(context, "context");
    _context = context;
    _clientTracker = clientTracker;
    _userDataTracker = userDataTracker;
  }
  
  /**
   * Gets the URI info.
   * @return the uri info, not null
   */
  public UriInfo getUriInfo() {
    return _uriInfo;
  }

  @Path("{username}")
  public UserResource getUser(@PathParam("username") String username) {
    UserResource user = _userMap.get(username);
    if (user == null) {
      _clientTracker.userCreated(username);
      UserResource freshUser = new UserResource(username, _context, _clientTracker, _userDataTracker);
      user = _userMap.putIfAbsent(username, freshUser);
      if (user == null) {
        user = freshUser;
      }
    }
    return user;
  }

  /**
   * Discards any users and clients that haven't been accessed since the given timestamp.
   * 
   * @param timestamp any client resources with a last accessed time before this will be removed
   */
  public void deleteClients(final long timestamp) {
    final Iterator<Map.Entry<String, UserResource>> userIterator = _userMap.entrySet().iterator();
    while (userIterator.hasNext()) {
      final Map.Entry<String, UserResource> userEntry = userIterator.next();
      userEntry.getValue().deleteClients(timestamp);
      if (userEntry.getValue().getLastAccessed() == 0) {
        userIterator.remove();
        _clientTracker.userDiscarded(userEntry.getKey());
      }
    }
  }

  public DeleteClientsRunnable createDeleteTask() {
    return new DeleteClientsRunnable();
  }

  /**
   * Runnable to delete clients.
   */
  class DeleteClientsRunnable implements Runnable {
    /**
     * The timeout.
     */
    private long _timeoutMillis = 1800000; // 30m default

    @Override
    public void run() {
      deleteClients(System.currentTimeMillis() - _timeoutMillis);
    }

    /**
     * Sets the timeout.
     * @param timeoutSecs  the timeout seconds
     */
    public void setTimeout(final long timeoutSecs) {
      _timeoutMillis = timeoutSecs * 1000;
    }

    /**
     * Sets the scheduler.
     * @param scheduler  the scheduler, not null
     */
    public void setScheduler(final ScheduledExecutorService scheduler) {
      scheduler.scheduleWithFixedDelay(this, _timeoutMillis, _timeoutMillis, TimeUnit.MILLISECONDS);
    }
  }

}
