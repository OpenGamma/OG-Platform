/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.google.common.collect.Lists;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.server.distribution.MarketDataDistributor;
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
 * this. See <a href="http://jira.springframework.org/browse/SPR-2325">http://jira.springframework.org/browse/SPR-2325</a>.
 * 
 */
public abstract class AbstractPersistentSubscriptionManager implements Lifecycle {

  private static final Logger s_logger = LoggerFactory
      .getLogger(AbstractPersistentSubscriptionManager.class);

  /**
   * Default how often to save the persistent subscriptions to the database, milliseconds
   */
  public static final long DEFAULT_SAVE_PERIOD = 60000L;

  private final AbstractLiveDataServer _server;
  private final Timer _timer;
  private final long _savePeriod;
  private volatile SaveTask _saveTask;

  private Set<PersistentSubscription> _previousSavedState;
  private Set<PersistentSubscription> _persistentSubscriptions = new HashSet<PersistentSubscription>();

  public AbstractPersistentSubscriptionManager(AbstractLiveDataServer server) {
    this(server, new Timer("PersistentSubscriptionManager Timer"),
        DEFAULT_SAVE_PERIOD);
  }

  public AbstractPersistentSubscriptionManager(AbstractLiveDataServer server,
      Timer timer, long savePeriod) {
    ArgumentChecker.notNull(server, "Live Data Server");
    ArgumentChecker.notNull(timer, "Timer");
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
    return _saveTask != null;
  }

  @Override
  public void start() {
    refreshAsync(); //PLAT-1632
    //Safe after refresh queued to avoid empty save
    _saveTask = new SaveTask();
    _timer.schedule(_saveTask, _savePeriod, _savePeriod);
  }

  @Override
  public void stop() {
    _saveTask.cancel();
    _saveTask = null;
    waitForIdleTimer();
  }

  private void waitForIdleTimer() {
    final CountDownLatch countDownLatch = new CountDownLatch(1);;
    s_logger.info("Waiting for timer to be idle");
    try {
      _timer.schedule(new TimerTask() {
        @Override
        public void run() {
          countDownLatch.countDown();
        }
      }, 0);
      countDownLatch.await();
      s_logger.info("Timer idle");
    } catch (Exception ex) {
      s_logger.error("Couldn't waiting for timer to be idle", ex);
    }
  }

  /**
   * This should mean that all the subscriptions become persistent eventually, 
   *  and (importantly) none of them expire in the mean time.
   * Because of the implementation of updateServer.
   */
  private synchronized void refreshAsync() {
    refreshState();

    _timer.schedule(new TimerTask() {
      @Override
      public void run() {
        //We release the lock before here, so someone could sneak in and change things
        updateServer(true);
      }
    }, 0);
  }
  
  public synchronized void refresh() {
    refreshState();

    updateServer(true);
  }

  /**
   * Reads from all sources to our private state
   */
  private synchronized void refreshState() {
    s_logger.debug("Refreshing persistent subscriptions from storage");

    clear();
    readFromStorage();
    readFromServer();
    
    s_logger.info("Refreshed persistent subscriptions from storage. There are currently "
        + _persistentSubscriptions.size() + " persistent subscriptions.");
  }

  /**
   * Creates a persistent subscription on the server for any persistent
   * subscriptions which are not yet there.
   */
  private synchronized void updateServer(boolean catchExceptions) {
    Set<PersistentSubscription> persistentSubscriptionsToMake = new HashSet<PersistentSubscription>(_persistentSubscriptions);
    
    int partitionSize = 100; //Aim is to make sure we can convert subscriptions quickly enough that nothing expires
    List<List<PersistentSubscription>> partitions = Lists.partition(Lists.newArrayList(persistentSubscriptionsToMake), partitionSize);
    for (List<PersistentSubscription> partition : partitions) {
      Iterator<PersistentSubscription> iterator = persistentSubscriptionsToMake.iterator();
      while (iterator.hasNext()) {
        PersistentSubscription sub = iterator.next();
        MarketDataDistributor existingDistributor = _server.getMarketDataDistributor(sub.getFullyQualifiedSpec());
        if (existingDistributor == null) {
          //We'll deal with this in its partition
          continue;
        } else {
          //Upgrade or no/op should be fast, lets do it to avoid expiry
          createPersistentSubscription(catchExceptions, sub);
          iterator.remove();
        }
      }
      
      for (PersistentSubscription sub : partition) {
        //TODO: PLAT-1632 - bulk subscribe, but handle exceptions
        if (!persistentSubscriptionsToMake.contains(sub)) {
          continue; // We did this the fast way
        }
        createPersistentSubscription(catchExceptions, sub);
        persistentSubscriptionsToMake.remove(sub);
      }
    }
  }

  private void createPersistentSubscription(boolean catchExceptions, PersistentSubscription sub) {
    s_logger.info("Creating {}", sub);
    try {
      _server.subscribe(sub.getFullyQualifiedSpec(), true);
    } catch (RuntimeException e) {
      if (catchExceptions) {
        s_logger.error("Creating a persistent subscription failed for " + sub, e);
      } else {
        throw e;            
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
      returnValue.add(ps.getFullyQualifiedSpec().toString());
    }

    return returnValue;
  }

  public synchronized void addPersistentSubscription(String securityUniqueId) {
    LiveDataSpecification spec = getFullyQualifiedLiveDataSpec(securityUniqueId);
    addPersistentSubscription(new PersistentSubscription(spec));
    updateServer(false);
  }

  public synchronized boolean removePersistentSubscription(
      String securityUniqueId) {
    Subscription sub = _server.getSubscription(securityUniqueId);
    if (sub == null) {
      return false;
    }
    
    boolean removed = false;
    for (MarketDataDistributor distributor : sub.getDistributors()) {
      removed = true;
      distributor.setPersistent(false);
    }

    save();
    return removed;
  }
  
  public LiveDataSpecification getFullyQualifiedLiveDataSpec(String securityUniqueId) {
    return _server.getLiveDataSpecification(securityUniqueId);
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
      for (MarketDataDistributor distributor : sub.getDistributors()) {
        if (distributor.isPersistent()) {
          PersistentSubscription ps = new PersistentSubscription(
              distributor.getFullyQualifiedLiveDataSpecification());
          addPersistentSubscription(ps);
        }
      }
    }
  }

  /**
   * Reads entries from persistent storage (DB, flat file, ...) and calls
   * {@link #addPersistentSubscription(PersistentSubscription)} for each one.
   */
  protected abstract void readFromStorage();

  /**
   * Saves entries to persistent storage (DB, flat file, ...)
   * 
   * @param newState Entries to be saved
   */
  public abstract void saveToStorage(Set<PersistentSubscription> newState);

}
