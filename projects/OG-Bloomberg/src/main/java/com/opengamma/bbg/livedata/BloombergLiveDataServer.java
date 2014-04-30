/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.ehcache.CacheManager;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.Lifecycle;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.Identity;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.MessageIterator;
import com.bloomberglp.blpapi.Subscription;
import com.bloomberglp.blpapi.SubscriptionList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.SessionProvider;
import com.opengamma.bbg.permission.BloombergBpipeApplicationUserIdentityProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.engine.marketdata.live.MarketDataAvailabilityNotification;
import com.opengamma.livedata.server.AbstractEventDispatcher;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * A Bloomberg Live Data Server.
 */
public class BloombergLiveDataServer extends AbstractBloombergLiveDataServer {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergLiveDataServer.class);
  /** Interval between attempts to reconnect to Bloomberg. */
  private static final long RECONNECT_PERIOD = 30000;

  // Injected Inputs:
  private final BloombergConnector _bloombergConnector;
  private final ReferenceDataProvider _referenceDataProvider;

  /** Creates and manages the Bloomberg session and service. */
  private final SessionProvider _sessionProvider;
  /** Timer for managing reconnection tasks. */
  private final Timer _timer = new Timer();

  // Runtime State:
  private BloombergEventDispatcher _eventDispatcher;
  private Thread _eventDispatcherThread;

  private long _subscriptionLimit = Long.MAX_VALUE;
  private volatile RejectedDueToSubscriptionLimitEvent _lastLimitRejection; // = null
  /** Task for (re)connecting to Bloomberg. */
  private BloombergLiveDataServer.ConnectTask _connectTask;
  /** For sending a notification message that Bloomberg data is available. */
  private final FudgeMessageSender _availabilityNotificationSender;
  private volatile Identity _applicationUserIdentity;
  private final boolean _requiresAuthorization;

  /**
   * Creates an instance.
   * 
   * @param bloombergConnector the connector, not null
   * @param referenceDataProvider the reference data provider, not null
   * @param cacheManager the cache manager, not null
   * @param availabilityNotificationSender For sending notifications when Bloomberg data becomes available
   */
  public BloombergLiveDataServer(BloombergConnector bloombergConnector, ReferenceDataProvider referenceDataProvider, CacheManager cacheManager,
      FudgeMessageSender availabilityNotificationSender) {
    super(cacheManager);
    ArgumentChecker.notNull(bloombergConnector, "bloombergConnector");
    ArgumentChecker.notNull(bloombergConnector.getSessionOptions(), "bloombergConnector.sessionOptions");
    ArgumentChecker.notNull(referenceDataProvider, "referenceDataProvider");
    ArgumentChecker.notNull(availabilityNotificationSender, "availabilityNotificationSender");

    _requiresAuthorization = bloombergConnector.requiresAuthorization();
    _availabilityNotificationSender = availabilityNotificationSender;

    _bloombergConnector = bloombergConnector;
    _referenceDataProvider = referenceDataProvider;
    _sessionProvider = new SessionProvider(_bloombergConnector, getServiceNames());
  }

  private List<String> getServiceNames() {
    List<String> serviceNames = Lists.newArrayList(BloombergConstants.MKT_DATA_SVC_NAME);
    if (_requiresAuthorization) {
      serviceNames.add(BloombergConstants.AUTH_SVC_NAME);
    }
    return serviceNames;
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
    _sessionProvider.invalidateSession();
  }

  @Override
  protected void doConnect() {
    // Always start the provider - just in case the lifecycle isn't being followed
    _sessionProvider.start();
    // getting the session throws an exception if BBG isn't available which is the behaviour we want
    _sessionProvider.getSession();

    if (_requiresAuthorization) {
      // we need authorization done
      BloombergBpipeApplicationUserIdentityProvider identityProvider = new BloombergBpipeApplicationUserIdentityProvider(_sessionProvider);
      _applicationUserIdentity = identityProvider.getIdentity();
    }

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
      String securityDes = getBloombergSubscriptionPathPrefix() + bbgUniqueId;
      final List<String> standardFields = getLiveDataFields();
      Subscription subscription = new Subscription(securityDes, standardFields);
      sl.add(subscription);
      returnValue.put(bbgUniqueId, subscription);
    }

    try {
      if (_requiresAuthorization) {
        _sessionProvider.getSession().subscribe(sl, _applicationUserIdentity);
      } else {
        _sessionProvider.getSession().subscribe(sl);
      }
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Could not subscribe to " + bbgUniqueIds, e);
    }

    return returnValue;
  }

  private List<String> getLiveDataFields() {
    if (!_requiresAuthorization) {
      return BloombergDataUtils.STANDARD_FIELDS_LIST;
    }
    final List<String> result = Lists.newArrayList(BloombergDataUtils.STANDARD_FIELDS_LIST);
    result.add(BloombergConstants.EID_LIVE_DATA_FIELD);
    return result;
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
   * 
   * @return the lastLimitRejection
   */
  public RejectedDueToSubscriptionLimitEvent getLastLimitRejection() {
    return _lastLimitRejection;
  }

  @Override
  public synchronized void start() {
    _sessionProvider.start();
    if (getConnectionStatus() == ConnectionStatus.NOT_CONNECTED) {
      _connectTask = new ConnectTask();
      _timer.schedule(_connectTask, 0, RECONNECT_PERIOD);
    }
  }

  @Override
  public synchronized void stop() {
    if (getConnectionStatus() == ConnectionStatus.CONNECTED) {
      stopExpirationManager();
      if (_connectTask != null) {
        _connectTask.cancel();
      }
      disconnect();
    }
    _sessionProvider.stop();
  }

  /**
   * Task that connects to Bloomberg and periodically tries to reconnect if the connection is down.
   */
  private class ConnectTask extends TimerTask {

    @Override
    public void run() {
      synchronized (BloombergLiveDataServer.this) {
        if (getConnectionStatus() == ConnectionStatus.NOT_CONNECTED) {
          try {
            s_logger.info("Connecting to Bloomberg");
            connect();
            startExpirationManager();
            reestablishSubscriptions();
            MarketDataAvailabilityNotification notification = new MarketDataAvailabilityNotification(ImmutableSet.of(ExternalSchemes.BLOOMBERG_BUID, ExternalSchemes.BLOOMBERG_BUID_WEAK,
                ExternalSchemes.BLOOMBERG_TCM, ExternalSchemes.BLOOMBERG_TICKER, ExternalSchemes.BLOOMBERG_TICKER_WEAK));
            FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
            s_logger.info("Sending notification that Bloomberg is available: {}", notification);
            _availabilityNotificationSender.send(notification.toFudgeMsg(serializer));
          } catch (Exception e) {
            s_logger.warn("Failed to connect to Bloomberg", e);
          }
        }
      }
    }
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
   * This is the job which actually will dispatch messages from Bloomberg to the various internal consumers.
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
          liveDataReceived(bbgUniqueId, eventAsFudgeMsg);
          continue;
        }
        s_logger.info("Got event {} {} {}", event.eventType(), bbgUniqueId, msg.asElement());

        if (event.eventType() == Event.EventType.SESSION_STATUS) {
          s_logger.info("SESSION_STATUS event received: {}", msg.messageType());
          if (msg.messageType().toString().equals("SessionTerminated")) {
            disconnect();
            terminate();
          }
        }
      }
    }
  }
}
