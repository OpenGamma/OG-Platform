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

import javax.time.calendar.Period;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.user.ClientTracker;
import com.opengamma.financial.user.UserDataTracker;

/**
 * RESTful resource which isn't backed by any user objects, just to host /users. Any user requested will
 * be created.
 */
@Path("/data/users")
public class UsersResource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(UsersResource.class);
  /**
   * The map of users.
   */
  private final ConcurrentHashMap<String, UserResource> _userMap = new ConcurrentHashMap<String, UserResource>();
  /**
   * The backing bean.
   */
  private final UserResourceData _data;

  /**
   * Creates an instance.
   * 
   * @param clientTracker  the tracker, not null
   * @param userDataTracker  the tracker, not null
   * @param context  the context, not null
   */
  public UsersResource(final ClientTracker clientTracker, final UserDataTracker userDataTracker, final UsersResourceContext context) {
    _data = new UserResourceData();
    _data.setClientTracker(clientTracker);
    _data.setUserDataTracker(userDataTracker);
    _data.setContext(context);
  }

  //-------------------------------------------------------------------------
  @Path("{username}")
  public UserResource getUser(@Context UriInfo uriInfo, @PathParam("username") String username) {
    UserResource user = _userMap.get(username);
    if (user == null) {
      _data.getClientTracker().userCreated(username);
      _data.setUriInfo(uriInfo);
      UserResource freshUser = new UserResource(username, _data);
      user = _userMap.putIfAbsent(username, freshUser);
      if (user == null) {
        user = freshUser;
      }
    }
    return user;
  }

  //-------------------------------------------------------------------------
  /**
   * Discards any users and clients that haven't been accessed since the given timestamp.
   * 
   * @param timestamp any client resources with a last accessed time before this will be removed
   */
  public void deleteClients(final long timestamp) {
    final Iterator<Map.Entry<String, UserResource>> userIterator = _userMap.entrySet().iterator();
    while (userIterator.hasNext()) {
      final Map.Entry<String, UserResource> userEntry = userIterator.next();
      s_logger.debug("deleting clients for user {}", userEntry.getKey());
      userEntry.getValue().deleteClients(timestamp);
      if (userEntry.getValue().getLastAccessed() == 0) {
        s_logger.debug("deleting user {}", userEntry.getKey());
        userIterator.remove();
        _data.getClientTracker().userDiscarded(userEntry.getKey());
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the scheduled deletion task.
   * 
   * @param scheduler  the scheduler, not null
   * @param clientTimeOut  the time out for clients, not null
   */
  public void createDeleteTask(ScheduledExecutorService scheduler, Period clientTimeOut) {
    long timeOutMillis = clientTimeOut.totalNanosWith24HourDays() / 1000000L;
    DeleteClientsRunnable runnable = new DeleteClientsRunnable(timeOutMillis);
    scheduler.scheduleWithFixedDelay(runnable, timeOutMillis, timeOutMillis, TimeUnit.MILLISECONDS);
  }

  /**
   * Runnable to delete clients.
   */
  class DeleteClientsRunnable implements Runnable {
    private final long _timeoutMillis;

    public DeleteClientsRunnable(long timeoutMillis) {
      super();
      _timeoutMillis = timeoutMillis;
    }

    @Override
    public void run() {
      deleteClients(System.currentTimeMillis() - _timeoutMillis);
    }
  }

}
