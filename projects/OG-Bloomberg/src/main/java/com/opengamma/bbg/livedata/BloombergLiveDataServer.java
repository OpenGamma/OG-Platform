/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.Lifecycle;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.MessageIterator;
import com.bloomberglp.blpapi.Subscription;
import com.bloomberglp.blpapi.SubscriptionList;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.SessionProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.livedata.server.AbstractEventDispatcher;
import com.opengamma.util.ArgumentChecker;

import net.sf.ehcache.CacheManager;

/**
 * A Bloomberg Live Data Server. 
 */
public class BloombergLiveDataServer extends AbstractBloombergLiveDataServer {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergLiveDataServer.class);

  // Injected Inputs:
  private final BloombergConnector _bloombergConnector;
  private final ReferenceDataProvider _referenceDataProvider;

  /** Creates and manages the Bloomberg session and service. */
  private final SessionProvider _sessionProvider;

  // Runtime State:
  private BloombergEventDispatcher _eventDispatcher;
  private Thread _eventDispatcherThread;

  private long _subscriptionLimit = Long.MAX_VALUE;
  private volatile RejectedDueToSubscriptionLimitEvent _lastLimitRejection; // = null

  /**
   * Creates an instance.
   * 
   * @param bloombergConnector  the connector, not null
   * @param referenceDataProvider  the reference data provider, not null
   * @param cacheManager  the cache manager, not null
   */
  public BloombergLiveDataServer(BloombergConnector bloombergConnector, ReferenceDataProvider referenceDataProvider, CacheManager cacheManager) {
    super(cacheManager);
    ArgumentChecker.notNull(bloombergConnector, "bloombergConnector");
    ArgumentChecker.notNull(referenceDataProvider, "referenceDataProvider");
    _bloombergConnector = bloombergConnector;
    _referenceDataProvider = referenceDataProvider;
    _sessionProvider = new SessionProvider(_bloombergConnector, BloombergConstants.MKT_DATA_SVC_NAME);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the Bloomberg connector.
   * 
   * @return the connector, not null
   */
  public BloombergConnector getBloombergConnector() {
    return _bloombergConnector;
  }

  @Override
  protected void doDisconnect() {
    _eventDispatcher.terminate();
    try {
      _eventDispatcherThread.join(10000L);
    } catch (InterruptedException e) {
      Thread.interrupted();
      s_logger.warn("Interrupted while waiting for event dispatcher thread to terminate", e);
    }
    _eventDispatcher = null;
    _eventDispatcherThread = null;
    _sessionProvider.close();
  }

  @Override
  protected void doConnect() {
    BloombergEventDispatcher eventDispatcher = new BloombergEventDispatcher(this);
    Thread eventDispatcherThread = new Thread(eventDispatcher, "Bloomberg LiveData Dispatcher");
    eventDispatcherThread.setDaemon(true);
    eventDispatcherThread.start();
    
    // If we got this far, we're ready, and we can call all the setters.
    _eventDispatcher = eventDispatcher;
    _eventDispatcherThread = eventDispatcherThread;

    // make sure the reference data provider also reconnects
    if (_referenceDataProvider instanceof Lifecycle) {
      ((Lifecycle) _referenceDataProvider).start();
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

    Map<String, Object> returnValue = Maps.newHashMap();
    
    SubscriptionList sl = new SubscriptionList();
    for (String bbgUniqueId : bbgUniqueIds) {
      String securityDes = "/buid/" + bbgUniqueId;
      Subscription subscription = new Subscription(securityDes, BloombergDataUtils.STANDARD_FIELDS_LIST);
      sl.add(subscription);
      returnValue.put(bbgUniqueId, subscription);
    }
    
    try {
      _sessionProvider.getSession().subscribe(sl);
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Could not subscribe to " + bbgUniqueIds, e);
    }
    
    return returnValue;
  }

  @Override
  public ConnectionStatus getConnectionStatus() {
    return _sessionProvider.isConnected() ? ConnectionStatus.CONNECTED : ConnectionStatus.NOT_CONNECTED;
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
      _sessionProvider.getSession().unsubscribe(sl);
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Could not unsubscribe from " + subscriptionHandles, e);
    }
  }

  @Override
  public ReferenceDataProvider getReferenceDataProvider() {
    return _referenceDataProvider;
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
  /**
   * This is the job which actually will dispatch messages from Bloomberg to the various
   * internal consumers.
   *
   * @author kirk
   */
  private final class BloombergEventDispatcher extends AbstractEventDispatcher {

    private BloombergEventDispatcher(BloombergLiveDataServer server) {
      super(server);
    }

    @Override
    protected void preStart() {
      super.preStart();
    }

    @Override
    protected void dispatch(long maxWaitMilliseconds) {
      Event event = null;
      try {
        event = _sessionProvider.getSession().nextEvent(1000L);
      } catch (InterruptedException e) {
        Thread.interrupted();
      }
      if (event == null) {
        return;
      }
      MessageIterator msgIter = event.messageIterator();
      while (msgIter.hasNext()) {
        Message msg = msgIter.next();
        String bbgUniqueId = msg.topicName();

        if (event.eventType() == Event.EventType.SUBSCRIPTION_DATA) {
          FudgeMsg eventAsFudgeMsg = BloombergDataUtils.parseElement(msg.asElement());
          getServer().liveDataReceived(bbgUniqueId, eventAsFudgeMsg);
          // REVIEW 2012-09-19 Andrew -- Why return? Might the event contain multiple messages?
          return;
        }
        s_logger.info("Got event {} {} {}", event.eventType(), bbgUniqueId, msg.asElement());

        if (event.eventType() == Event.EventType.SESSION_STATUS) {
          if (msg.messageType().toString().equals("SessionTerminated")) {
            disconnected();
          }
        }
      }
    }

  }
}
