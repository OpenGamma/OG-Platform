/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import javax.jms.ConnectionFactory;

import org.springframework.jms.core.JmsTemplate;

import com.opengamma.livedata.server.AbstractLiveDataServer;
import com.opengamma.livedata.server.MarketDataFudgeJmsSender;
import com.opengamma.livedata.server.SubscriptionRequestReceiver;
import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.FudgeRequestDispatcher;
import com.opengamma.transport.InMemoryByteArrayRequestConduit;
import com.opengamma.transport.jms.ActiveMQTestUtil;

/**
 * 
 *
 * @author pietari
 */
public class LiveDataClientTestUtils {
  
  public static JmsLiveDataClient getJmsClient(AbstractLiveDataServer server) {
    SubscriptionRequestReceiver subReceiver = new SubscriptionRequestReceiver(server);
    FudgeRequestDispatcher subDispatcher = new FudgeRequestDispatcher(subReceiver);
    InMemoryByteArrayRequestConduit subscriptionConduit = new InMemoryByteArrayRequestConduit(subDispatcher);
    ByteArrayFudgeRequestSender requestSender = new ByteArrayFudgeRequestSender(subscriptionConduit);
    
    ConnectionFactory cf = ActiveMQTestUtil.createTestConnectionFactory();
    
    JmsLiveDataClient liveDataClient = new JmsLiveDataClient(requestSender, cf);
    
    JmsTemplate marketDataTemplate = new JmsTemplate();
    marketDataTemplate.setPubSubDomain(true);
    marketDataTemplate.setConnectionFactory(cf);
    
    MarketDataFudgeJmsSender mdfjs = new MarketDataFudgeJmsSender(marketDataTemplate);
    server.addMarketDataFieldReceiver(mdfjs);
    
    liveDataClient.setFudgeContext(liveDataClient.getFudgeContext());
    
    return liveDataClient;
  }

}
