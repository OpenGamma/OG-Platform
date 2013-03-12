/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

/**
 * A manager of users that provide access to underlying services which are managed.
 * <p>
 * This class is a placeholder until full user management is added.
 */
public class FinancialUserManager {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(FinancialUserManager.class);
  /**
   * The map of users.
   */
  private final ConcurrentHashMap<String, FinancialUser> _userMap = new ConcurrentHashMap<String, FinancialUser>();
  /**
   * The user services.
   */
  private final FinancialUserServices _services;
  /**
   * The client tracker.
   */
  private final FinancialClientTracker _clientTracker;
  /**
   * The user data tracker.
   */
  private final FinancialUserDataTracker _userDataTracker;

  /**
   * Creates an instance.
   * 
   * @param services  the services, not null
   * @param clientTracker  the tracker, not null
   * @param userDataTracker  the tracker, not null
   */
  public FinancialUserManager(FinancialUserServices services, FinancialClientTracker clientTracker, FinancialUserDataTracker userDataTracker) {
    _services = services;
    _clientTracker = clientTracker;
    _userDataTracker = userDataTracker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the services.
   * 
   * @return the services, not null
   */
  public FinancialUserServices getServices() {
    return _services;
  }

  /**
   * Gets the tracker.
   * 
   * @return the tracker, not null
   */
  public FinancialClientTracker getClientTracker() {
    return _clientTracker;
  }

  /**
   * Gets the tracker.
   * 
   * @return the tracker, not null
   */
  public FinancialUserDataTracker getUserDataTracker() {
    return _userDataTracker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a user.
   * 
   * @param userName  the user name, not null
   * @return the user, null if not found
   */
  public FinancialUser getUser(String userName) {
    return _userMap.get(userName);
  }

  /**
   * Gets a user, creating if it does not exist.
   * 
   * @param userName  the user name, not null
   * @return the user, not null
   */
  public FinancialUser getOrCreateUser(String userName) {
    FinancialUser user = _userMap.get(userName);
    if (user == null) {
      _clientTracker.userCreated(userName);
      FinancialUser freshUser = new FinancialUser(this, userName);
      user = _userMap.putIfAbsent(userName, freshUser);
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
  public void deleteClients(final Instant timestamp) {
    final Iterator<Map.Entry<String, FinancialUser>> userIterator = _userMap.entrySet().iterator();
    while (userIterator.hasNext()) {
      final Map.Entry<String, FinancialUser> userEntry = userIterator.next();
      s_logger.debug("deleting clients for user {}", userEntry.getKey());
      int activeClients = userEntry.getValue().getClientManager().deleteClients(timestamp);
      if (activeClients == 0) {
        s_logger.debug("deleting user {}", userEntry.getKey());
        userIterator.remove();
        getClientTracker().userDiscarded(userEntry.getKey());
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
  public void createDeleteTask(ScheduledExecutorService scheduler, Duration clientTimeOut) {
    long timeOutMillis = clientTimeOut.toMillis();
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
      deleteClients(Instant.now().minusMillis(_timeoutMillis));
    }
  }

}
