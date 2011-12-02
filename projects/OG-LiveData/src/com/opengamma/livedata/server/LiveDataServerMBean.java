/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.livedata.server.distribution.MarketDataDistributor;
import com.opengamma.util.ArgumentChecker;

/**
 * JMX management of a LiveData server.
 */
@ManagedResource(
    description = "LiveData server attributes and operations that can be managed via JMX"
    )
public class LiveDataServerMBean {
  
  private static final Logger s_logger = LoggerFactory.getLogger(LiveDataServerMBean.class);
  private final AbstractLiveDataServer _server;
  
  public LiveDataServerMBean(AbstractLiveDataServer server) {
    ArgumentChecker.notNull(server, "Live Data Server");
    _server = server;
  }
  
  @ManagedAttribute(description = "The unique id domain of the underlying server.")
  public String getUniqueIdDomain() {
    try {
      ExternalScheme uniqueIdDomain = _server.getUniqueIdDomain();
      return uniqueIdDomain == null ? "<null>" : uniqueIdDomain.toString();
    } catch (RuntimeException e) {
      s_logger.error("getUniqueIdDomain() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }
  
  @ManagedAttribute(description = "The connection status of the underlying server.")
  public String getConnectionStatus() {
    try {
      return _server.getConnectionStatus().toString();
    } catch (RuntimeException e) {
      s_logger.error("getConnectionStatus() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }
  
  @ManagedAttribute(description = "The type of the underlying server.")
  public String getServerType() {
    try {
      return _server.getClass().getName();
    } catch (RuntimeException e) {
      s_logger.error("getServerType() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }
  
  @ManagedAttribute(description = "How many different tickers the server subscribes to.")
  public int getNumActiveSubscriptions() {
    try {
      return _server.getNumActiveSubscriptions();
    } catch (RuntimeException e) {
      s_logger.error("getNumActiveSubscriptions() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }
  
  @ManagedAttribute(description = "Security IDs the server subscribes to."
      + " The form of the IDs is dependent on the source system"
      + " - Reuters RICs, Bloomberg unique IDs, etc.")
  public Set<String> getActiveSubscriptionIds() {
    try {
      return _server.getActiveSubscriptionIds();
    } catch (RuntimeException e) {
      s_logger.error("getActiveSubscriptionIds() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }
  
  @ManagedAttribute(description = "JMS topics the server publishes to.")
  public Set<String> getActiveDistributionSpecs() {
    try {
      return _server.getActiveDistributionSpecs();
    } catch (RuntimeException e) {
      s_logger.error("getActiveDistributionSpecs() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }
  
  @ManagedAttribute(description = "The number of market data updates the server has processed in its lifetime.")
  public long getNumMarketDataUpdatesReceived() {
    try {
      return _server.getNumMarketDataUpdatesReceived();
    } catch (RuntimeException e) {
      s_logger.error("getNumLiveDataUpdatesSent() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }
  
  @ManagedAttribute(description = "# of market data updates/sec, calculated over the last 60 seconds")
  public double getNumLiveDataUpdatesSentPerSecondOverLastMinute() {
    try {
      return _server.getNumLiveDataUpdatesSentPerSecondOverLastMinute();            
    } catch (RuntimeException e) {
      s_logger.error("getNumLiveDataUpdatesSentPerSecondOverLastMinute() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }
  
  @ManagedOperation(description = "Subscribes to market data. The subscription will be non-persistent."
      + " If the server already subscribes to the given market data, this method is a "
      + " no-op. Returns the name of the JMS topic market data will be published on.")
  @ManagedOperationParameters({
      @ManagedOperationParameter(name = "securityUniqueId", description = "Security unique ID. Server type dependent.)") })
  public String subscribe(String securityUniqueId) {
    try {
      LiveDataSubscriptionResponse response = _server.subscribe(securityUniqueId);
      if (response.getSubscriptionResult() != LiveDataSubscriptionResult.SUCCESS) {
        throw new RuntimeException("Unsuccessful subscription: " + response.getUserMessage());
      }
      return response.getTickDistributionSpecification();
    } catch (RuntimeException e) {
      s_logger.error("subscribe(" + securityUniqueId + ") failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }
  
  @ManagedOperation(description = "Subscribes to market data. The subscription will be persistent."
      + " If the server already subscribes to the given market data, this method will make the "
      + " subscription persistent. Returns the name of the JMS topic market data will be published on.")
  @ManagedOperationParameters({ @ManagedOperationParameter(name = "securityUniqueId", description = "Security unique ID. Server type dependent.)") })
  public String subscribePersistently(String securityUniqueId) {
    try {
      LiveDataSpecification spec = _server.getLiveDataSpecification(securityUniqueId);
      LiveDataSubscriptionResponse response = _server.subscribe(spec, true);
      if (response.getSubscriptionResult() != LiveDataSubscriptionResult.SUCCESS) {
        throw new RuntimeException("Unsuccessful subscription: " + response.getUserMessage());
      }
      return response.getTickDistributionSpecification();
    } catch (RuntimeException e) {
      s_logger.error("subscribe(" + securityUniqueId + ") failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }
  
  @ManagedOperation(description = "Converts all subscriptions to persistent.")
  public void subscribeAllPersistently() {
    setPersitenceForAll(true);
  }
  
  @ManagedOperation(description = "Converts all subscriptions to non-persistent.")
  public void unpersistAllSubscribtions() {
    setPersitenceForAll(false);
  }

  private void setPersitenceForAll(boolean persistent) {
    try {
      Set<String> activeSubscriptionIds = _server.getActiveSubscriptionIds();
      for (String string : activeSubscriptionIds) {
        MarketDataDistributor marketDataDistributor = _server.getMarketDataDistributor(string);
        marketDataDistributor.setPersistent(persistent);
      }
    } catch (RuntimeException e) {
      s_logger.error("Setting all susbcriptions to persistent=" + persistent + " failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }
  
  @ManagedOperation(description = "Unsubscribes from market data. "
      + "Works even if the subscription is persistent. "
      + "Returns true if a market data subscription was actually removed,"
      + " false otherwise.")
  @ManagedOperationParameters({
       @ManagedOperationParameter(name = "securityUniqueId", description = "Security unique ID. Server type dependent.)") })
  public boolean unsubscribe(String securityUniqueId) {
    try {
      return _server.unsubscribe(securityUniqueId); 
    } catch (RuntimeException e) {
      s_logger.error("unsubscribe(" + securityUniqueId + ") failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  
  @ManagedOperation(description = "Gets the current snapshot of a security. Will not cause an underlying snapshot.")
  @ManagedOperationParameters({
       @ManagedOperationParameter(name = "securityUniqueId", description = "Security unique ID. Server type dependent.)") })
  public String getSnapshot(String securityUniqueId) {
    try {
      MarketDataDistributor marketDataDistributor = _server.getMarketDataDistributor(securityUniqueId);
      return marketDataDistributor.getSnapshot().toString();
    } catch (RuntimeException e) {
      s_logger.error("getSnapshot() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }
  
  @ManagedOperation(description = "Gets the current snapshot of all active securities. Will not cause any underlying snapshots.")
  public String[] getAllSnapshots() {
    try {
      Set<String> activeSubscriptionIds = _server.getActiveSubscriptionIds();
      Iterable<String> results = Iterables.transform(activeSubscriptionIds, new Function<String, String>() {

        @Override
        public String apply(String from) {
          try {
            return getSnapshot(from);
          } catch (RuntimeException e) {
            return e.getMessage();
          }
        }
      });
      return Iterators.toArray(results.iterator(), String.class);
    } catch (RuntimeException e) {
      s_logger.error("getAllSnapshots() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }
  
  @ManagedOperation(description = "Gets the current field history of a security. Will not cause an underlying snapshot.")
  @ManagedOperationParameters({ @ManagedOperationParameter(name = "securityUniqueId", description = "Security unique ID. Server type dependent.)") })
  public String getFieldHistory(String securityUniqueId) {
    try {
      Subscription subscription = _server.getSubscription(securityUniqueId);
      if (subscription == null) {
        return null;
      }
      return subscription.getLiveDataHistory().getLastKnownValues().toString();
    } catch (RuntimeException e) {
      s_logger.error("getFieldHistory() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedOperation(description = "Gets the current field history of all active securities. Will not cause any underlying snapshots.")
  public String[] getAllFieldHistories() {
    try {
      Set<String> activeSubscriptionIds = _server.getActiveSubscriptionIds();
      Iterable<String> results = Iterables.transform(activeSubscriptionIds, new Function<String, String>() {

        @Override
        public String apply(String from) {
          try {
            return getFieldHistory(from);
          } catch (RuntimeException e) {
            return e.getMessage();
          }
        }
      });
      return Iterators.toArray(results.iterator(), String.class);
    } catch (RuntimeException e) {
      s_logger.error("getAllFieldHistories() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }
  
}
