/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.Lifecycle;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bloomberglp.blpapi.Session;
import com.bloomberglp.blpapi.SessionOptions;
import com.bloomberglp.blpapi.Subscription;
import com.bloomberglp.blpapi.SubscriptionList;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.CachingReferenceDataProvider;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * A Bloomberg Live Data Server. 
 */
public class BloombergLiveDataServer extends AbstractBloombergLiveDataServer {
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergLiveDataServer.class);
  
  // Injected Inputs:
  private final SessionOptions _sessionOptions;
  private final CachingReferenceDataProvider _cachingRefDataProvider;
  private final ReferenceDataProvider _underlyingReferenceDataProvider;

  // Runtime State:
  private Session _session;
  private BloombergEventDispatcher _eventDispatcher;
  private Thread _eventDispatcherThread;

  private long _subscriptionLimit = Long.MAX_VALUE;
  private volatile RejectedDueToSubscriptionLimitEvent _lastLimitRejection; // = null
  
  public BloombergLiveDataServer(SessionOptions sessionOptions, CachingReferenceDataProvider cachingReferenceDataProvider) {
    this(sessionOptions, cachingReferenceDataProvider, cachingReferenceDataProvider.getUnderlying());
  }

  private BloombergLiveDataServer(SessionOptions sessionOptions,
      CachingReferenceDataProvider cachingRefDataProvider,
      ReferenceDataProvider underlyingReferenceDataProvider) {
    ArgumentChecker.notNull(sessionOptions, "Bloomberg Session Options");
    ArgumentChecker.notNull(cachingRefDataProvider, "Caching Reference Data Provider");
    ArgumentChecker.notNull(underlyingReferenceDataProvider, "Bloomberg Reference Data Provider");
    _sessionOptions = sessionOptions;
    _cachingRefDataProvider = cachingRefDataProvider;
    _underlyingReferenceDataProvider = underlyingReferenceDataProvider;
  }

  /**
   * @return the sessionOptions
   */
  public SessionOptions getSessionOptions() {
    return _sessionOptions;
  }

  /**
   * @return the session
   */
  public Session getSession() {
    return _session;
  }

  /**
   * @param session the session to set
   */
  public void setSession(Session session) {
    _session = session;
  }

  /**
   * @return the eventDispatcher
   */
  public BloombergEventDispatcher getEventDispatcher() {
    return _eventDispatcher;
  }

  /**
   * @return the eventDispatcherThread
   */
  public Thread getEventDispatcherThread() {
    return _eventDispatcherThread;
  }

  /**
   * @param eventDispatcherThread the eventDispatcherThread to set
   */
  public void setEventDispatcherThread(Thread eventDispatcherThread) {
    _eventDispatcherThread = eventDispatcherThread;
  }

  /**
   * @param eventDispatcher the eventDispatcher to set
   */
  public void setEventDispatcher(BloombergEventDispatcher eventDispatcher) {
    _eventDispatcher = eventDispatcher;
  }

  @Override
  protected void doDisconnect() {
    getEventDispatcher().terminate();
    try {
      getEventDispatcherThread().join(10000L);
    } catch (InterruptedException e) {
      Thread.interrupted();
      s_logger.warn("Interrupted while waiting for event dispatcher thread to terminate", e);
    }    
    setEventDispatcher(null);
    setEventDispatcherThread(null);
    
    s_logger.info("Disconnecting from existing session...");
    try {
      getSession().stop();
    } catch (InterruptedException e) {
      Thread.interrupted();
      s_logger.warn("Interrupted while waiting for session to stop", e);        
    } catch (Exception e) {
      s_logger.warn("Could not stop session", e);
    }
    setSession(null);
  }
  
  @Override
  protected void doConnect() {
    s_logger.info("Making Bloomberg service connection...");
    Session session = new Session(getSessionOptions());
    try {
      if (!session.start()) {
        throw new OpenGammaRuntimeException("Unable to start session with options " + getSessionOptions());
      }
    } catch (InterruptedException e) {
      Thread.interrupted();
      throw new OpenGammaRuntimeException("Unable to start session with options " + getSessionOptions(), e);
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Unable to start session with options " + getSessionOptions(), e);
    }
    // TODO kirk 2009-10-12 -- Should stop session if we fail here. Not doing so
    // due to the interrupted exception crap.
    s_logger.info("Connected. Opening service.");
    try {
      if (!session.openService("//blp/mktdata")) {
        throw new OpenGammaRuntimeException("Unable to open MarketData service");
      }
    } catch (InterruptedException e) {
      Thread.interrupted();
      throw new OpenGammaRuntimeException("Unable to open MarketData service", e);
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Unable to open MarketData service", e);
    }
    BloombergEventDispatcher eventDispatcher = new BloombergEventDispatcher(this, session);
    Thread eventDispatcherThread = new Thread(eventDispatcher, "Bloomberg LiveData Dispatcher");
    eventDispatcherThread.setDaemon(true);
    eventDispatcherThread.start();
    
    // If we got this far, we're ready, and we can call all the setters.
    setSession(session);
    setEventDispatcher(eventDispatcher);
    setEventDispatcherThread(eventDispatcherThread);
    
    // make sure the reference data provider also reconnects
    if (_underlyingReferenceDataProvider instanceof Lifecycle) {
      ((Lifecycle) _underlyingReferenceDataProvider).start();
    }
  }

  @Override
  protected void checkSubscribe(Set<String> uniqueIds) {
    //NOTE: need to do this here, rather than in doSubscribe, because otherwise we'd do the initial snapshot anyway.
    checkLimitRemaining(uniqueIds.size());
    super.checkSubscribe(uniqueIds);
  }

  @Override
  protected Map<String, Object> doSubscribe(Collection<String> bbgUniqueIds) {
    ArgumentChecker.notNull(bbgUniqueIds, "Unique IDs");
    if (bbgUniqueIds.isEmpty()) {
      return Collections.emptyMap();
    }
    
    Map<String, Object> returnValue = new HashMap<String, Object>();
    
    SubscriptionList sl = new SubscriptionList();
    for (String bbgUniqueId : bbgUniqueIds) {
      String securityDes = "/buid/" + bbgUniqueId;
      Subscription subscription = new Subscription(securityDes, BloombergDataUtils.STANDARD_FIELDS_LIST);
      sl.add(subscription);
      returnValue.put(bbgUniqueId, subscription);
    }
    
    try {
      getSession().subscribe(sl);
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Could not subscribe to " + bbgUniqueIds, e);
    }
    
    return returnValue;
  }

  public long getSubscriptionLimit() {
    return _subscriptionLimit;
  }

  public void setSubscriptionLimit(long subscriptionLimit) {
    _subscriptionLimit = subscriptionLimit;
  }
  
  private void checkLimitRemaining(int requested) {
    int afterSubscriptionCount = requested + getActiveSubscriptionIds().size();
    if (afterSubscriptionCount > getSubscriptionLimit()) {
      String message = "Rejecting subscription request, would result in limit of " + getSubscriptionLimit() + " being exceeded " + afterSubscriptionCount;
      s_logger.warn(message);
      _lastLimitRejection = new RejectedDueToSubscriptionLimitEvent(getSubscriptionLimit(), requested, afterSubscriptionCount);
      throw new OpenGammaRuntimeException(message);
    }
  }
  
  @Override
  protected void doUnsubscribe(Collection<Object> subscriptionHandles) {
    ArgumentChecker.notNull(subscriptionHandles, "Subscription handles");
    if (subscriptionHandles.isEmpty()) {
      return;
    }
    
    SubscriptionList sl = new SubscriptionList();
    
    for (Object subscriptionHandle : subscriptionHandles) {
      Subscription subscription = (Subscription) subscriptionHandle;
      sl.add(subscription);
    }
    
    try {
      getSession().unsubscribe(sl);
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Could not unsubscribe from " + subscriptionHandles, e);
    }
  }
  
  @Override
  public CachingReferenceDataProvider getCachingReferenceDataProvider() {
    return _cachingRefDataProvider;
  }

  @Override
  public ReferenceDataProvider getUnderlyingReferenceDataProvider() {
    return _underlyingReferenceDataProvider;
  }
  
  
  /**
   * Gets the last limit rejection event.
   * @return the lastLimitRejection
   */
  public RejectedDueToSubscriptionLimitEvent getLastLimitRejection() {
    return _lastLimitRejection;
  }
  /**
   * Starts the Bloomberg Server.
   * 
   * @param args Not needed
   */
  public static void main(String[] args) { // CSIGNORE
    ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("/com/opengamma/bbg/livedata/bbg-livedata-context.xml");
    context.start();
  }

}
