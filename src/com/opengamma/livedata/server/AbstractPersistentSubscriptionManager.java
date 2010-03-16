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
import org.springframework.beans.factory.annotation.Autowired;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.livedata.LiveDataSpecificationImpl;
import com.opengamma.util.ArgumentChecker;

/**
 * Stores persistent subscriptions in persistent storage so they're not lost if
 * the server crashes.
 * <p>
 * If you modify the list of persistent subscriptions in persistent storage by
 * editing the persistent storage (DB/file/whatever) using external tools
 * while the server is down, these changes will be reflected on the server
 * the next time it starts. 
 * 
 * @author pietari
 */
abstract public class AbstractPersistentSubscriptionManager {
  
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractPersistentSubscriptionManager.class);
  
  public static final long DEFAULT_SAVE_PERIOD = 60000;
  
  @Autowired
  private final AbstractLiveDataServer _server;
  
  private Set<PersistentSubscription> _previousSavedState = null;
  private Set<PersistentSubscription> _persistentSubscriptions = new HashSet<PersistentSubscription>();
  private volatile boolean _initialised = false;
  
  
  public AbstractPersistentSubscriptionManager(AbstractLiveDataServer server) {
    this(server, new Timer("PersistentSubscriptionManager Timer"), DEFAULT_SAVE_PERIOD);
  }
  
  public AbstractPersistentSubscriptionManager(AbstractLiveDataServer server, Timer timer, long savePeriod) {
    ArgumentChecker.checkNotNull(server, "Live Data Server");
    _server = server;
    timer.schedule(new SaveTask(), savePeriod, savePeriod);
  }

  
  private class SaveTask extends TimerTask {
    @Override
    public void run() {
      try {
        if (!_initialised) {
          refresh();
          _initialised = true;
        }
        save();
      } catch (RuntimeException e) {
        s_logger.error("Saving persistent subscriptions to storage failed", e);
      }
    }
  }
  
  /**
   * Read persistent subscriptions from persistent storage.
   * Subscribes to any entries to which we are not yet subscribed.
   */
  public synchronized void refresh() {
    s_logger.debug("Refreshing persistent subscriptions from storage");
    
    clear();    
    readFromStorage();
    readFromServer();
    
    for (PersistentSubscription sub : _persistentSubscriptions) {
      LiveDataSpecificationImpl spec = new LiveDataSpecificationImpl(new DomainSpecificIdentifier(_server.getUniqueIdDomain(), sub.getId()));
      if (!_server.isSubscribedTo(spec)) {
        s_logger.info("A persistent subscription added in storage, subscribing to " + spec);
        _server.subscribe(spec, true);
      }
    }
    
    s_logger.debug("Refreshed persistent subscriptions from storage");
  }
  
  /**
   * Saves all persistent subscriptions to persistent storage.
   */
  public synchronized void save() {
    s_logger.debug("Dumping persistent subscriptions to storage");
    
    clear();
    readFromServer();
    
    // Only save if changed
    if (_previousSavedState == null || !_previousSavedState.equals(_persistentSubscriptions)) {
      s_logger.info("Persistent subscriptions changed on the server, saving " + _persistentSubscriptions.size() + " subscriptions to storage.");
      saveToStorage();
      _previousSavedState = _persistentSubscriptions;
      _persistentSubscriptions = new HashSet<PersistentSubscription>();
      readFromServer();
    }
    
    s_logger.debug("Dumped persistent subscriptions to storage");
  }
  
  private void clear() {
    _persistentSubscriptions.clear();    
  }
  
  public synchronized Set<PersistentSubscription> getPersistentSubscriptions() {
    return new HashSet<PersistentSubscription>(_persistentSubscriptions);
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
        addPersistentSubscription(new PersistentSubscription(sub.getSecurityUniqueId()));
      }
    }
  }
  
  /**
   * Reads entries from persistent storage (DB, flat file, ...) 
   */
  protected abstract void readFromStorage();
  
  /**
   * Saves entries to persistent storage (DB, flat file, ...)
   */
  public abstract void saveToStorage();
    
}
