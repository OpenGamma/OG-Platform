/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Stores persistent subscriptions in persistent storage so they're not lost if
 * the server crashes.
 * <p>
 * If you modify the list of persistent subscriptions in persistent storage by
 * editing the persistent storage (DB/file/whatever) using external tools while
 * the server is down, these changes will be reflected on the server the next
 * time it starts.
 * <p>
 * This beans depends-on the Live Data Server, and any Spring configuration must reflect 
 * this. See {@link http://jira.springframework.org/browse/SPR-2325}.
 * 
 * @author pietari
 */
abstract public class AbstractPersistentSubscriptionManager implements Lifecycle {

  private static final Logger s_logger = LoggerFactory
      .getLogger(AbstractPersistentSubscriptionManager.class);

  public static final long DEFAULT_SAVE_PERIOD = 60000;

  private final AbstractLiveDataServer _server;
  private final Timer _timer;
  private final long _savePeriod;
  private final SaveTask _saveTask = new SaveTask();

  private Set<PersistentSubscription> _previousSavedState = null;
  private Set<PersistentSubscription> _persistentSubscriptions = new HashSet<PersistentSubscription>();
  private volatile boolean _isRunning = false;

  public AbstractPersistentSubscriptionManager(AbstractLiveDataServer server) {
    this(server, new Timer("PersistentSubscriptionManager Timer"),
        DEFAULT_SAVE_PERIOD);
  }

  public AbstractPersistentSubscriptionManager(AbstractLiveDataServer server,
      Timer timer, long savePeriod) {
    ArgumentChecker.checkNotNull(server, "Live Data Server");
    ArgumentChecker.checkNotNull(timer, "Timer");
    if (savePeriod <= 0) {
      throw new IllegalArgumentException("Please give positive save period");
    }
    
    _server = server;
    _timer = timer;
    _savePeriod = savePeriod;
  }

  private class SaveTask extends TimerTask {
    @Override
    public void run() {
      try {
        save();
      } catch (RuntimeException e) {
        s_logger.error("Saving persistent subscriptions to storage failed", e);
      }
    }
  }
  
  
  @Override
  public boolean isRunning() {
    return _isRunning;
  }

  @Override
  public void start() {
    refresh();
    _timer.schedule(_saveTask, _savePeriod, _savePeriod);
    _isRunning = true;
  }

  @Override
  public void stop() {
    _saveTask.cancel();
    _isRunning = false;
  }

  public synchronized void refresh() {
    s_logger.debug("Refreshing persistent subscriptions from storage");

    clear();
    readFromStorage();
    readFromServer();

    updateServer(true);

    s_logger.info("Refreshed persistent subscriptions from storage. There are currently "
            + _persistentSubscriptions.size() + " persistent subscriptions.");
  }

  /**
   * Creates a persistent subscription on the server for any persistent
   * subscriptions which are not yet there.
   */
  private void updateServer(boolean catchExceptions) {
    for (PersistentSubscription sub : _persistentSubscriptions) {

      LiveDataSpecification spec = new LiveDataSpecification(
          new DomainSpecificIdentifier(_server.getUniqueIdDomain(), sub.getId()));
      
      Subscription existingSub = _server.getSubscription(spec);
      
      if (existingSub == null || !existingSub.isPersistent()) {
        s_logger.info("Creating a persistent subscription on server for " + spec);
        try {
          _server.subscribe(spec, true);
        } catch (RuntimeException e) {
          if (catchExceptions) {
            s_logger.error("Creating a persistent subscription failed for " + spec, e);
          } else {
            throw e;            
          }
        }
      }
    }
  }

  public synchronized void save() {
    s_logger.debug("Dumping persistent subscriptions to storage");

    clear();
    readFromServer();

    // Only save if changed
    if (_previousSavedState == null || !_previousSavedState.equals(_persistentSubscriptions)) {
   
      s_logger.info("A change to persistent subscriptions detected, saving "
          + _persistentSubscriptions.size() + " subscriptions to storage.");
      saveToStorage(_persistentSubscriptions);
      _previousSavedState = new HashSet<PersistentSubscription>(_persistentSubscriptions);

    } else {
      s_logger.debug("No changes to persistent subscriptions detected.");      
    }

    s_logger.debug("Dumped persistent subscriptions to storage");
  }
  
  public synchronized Set<String> getPersistentSubscriptions() {
    clear();
    readFromServer();

    HashSet<String> returnValue = new HashSet<String>();
    for (PersistentSubscription ps : _persistentSubscriptions) {
      returnValue.add(ps.getId());
    }

    return returnValue;
  }

  public synchronized void addPersistentSubscription(String securityUniqueId) {
    addPersistentSubscription(new PersistentSubscription(securityUniqueId));
    updateServer(false);
  }

  public synchronized boolean removePersistentSubscription(
      String securityUniqueId) {
    PersistentSubscription ps = new PersistentSubscription(securityUniqueId);
    boolean removed = _persistentSubscriptions.remove(ps);

    Subscription sub = _server.getSubscription(securityUniqueId);
    if (sub != null && sub.isPersistent()) {
      _server.changePersistent(sub, false);
    }

    return removed;
  }

  private void clear() {
    _persistentSubscriptions.clear();
  }

  protected void addPersistentSubscription(PersistentSubscription sub) {
    _persistentSubscriptions.add(sub);
  }

  /**
   * Refreshes persistent subscriptions from the latest status on the server.
   */
  private void readFromServer() {
    for (Subscription sub : _server.getSubscriptions()) {
      if (sub.isPersistent()) {
        addPersistentSubscription(new PersistentSubscription(sub
            .getSecurityUniqueId()));
      }
    }
  }

  /**
   * Reads entries from persistent storage (DB, flat file, ...) and calls
   * {@link addPersistentSubscription(PersistentSubscription sub)} for each one.
   */
  protected abstract void readFromStorage();

  /**
   * Saves entries to persistent storage (DB, flat file, ...)
   */
  public abstract void saveToStorage(Set<PersistentSubscription> newState);

}
