/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.HashSet;
import java.util.Set;

import org.fudgemsg.FudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.model.SubscriptionRequestMessage;
import com.opengamma.livedata.model.SubscriptionResponseMessage;
import com.opengamma.util.ArgumentChecker;

/**
 * The base class from which most OpenGamma Live Data feed servers should
 * extend. Handles most common cases for distributed contract management.
 *
 * @author kirk
 */
public abstract class AbstractLiveDataServer {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractLiveDataServer.class);
  private final Set<MarketDataFieldReceiver> _fieldReceivers = new HashSet<MarketDataFieldReceiver>();


  public void terminatePublication(LiveDataSpecification dataSpec) {
    s_logger.info("Terminating publication of {}", dataSpec);
  }
  
  public void addMarketDataFieldReceiver(MarketDataFieldReceiver fieldReceiver) {
    ArgumentChecker.checkNotNull(fieldReceiver, "Market Data Field Receiver");
    _fieldReceivers.add(fieldReceiver);
  }


  /**
   * @param subscriptionRequest
   * @return
   */
  public abstract SubscriptionResponseMessage subscriptionRequestMade(SubscriptionRequestMessage subscriptionRequest);
  
  public void liveDataReceived(LiveDataSpecification resolvedSpecification, FudgeFieldContainer liveDataFields) {
    s_logger.debug("Live data received: {}", liveDataFields);
    // TODO kirk 2009-10-29 -- This needs to be much better.
    for(MarketDataFieldReceiver receiver : _fieldReceivers) {
      receiver.marketDataReceived(resolvedSpecification, liveDataFields);
    }
  }
}
