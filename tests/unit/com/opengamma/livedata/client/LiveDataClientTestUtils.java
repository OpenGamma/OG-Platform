/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import javax.jms.ConnectionFactory;

import org.springframework.jms.core.JmsTemplate;

import com.opengamma.livedata.server.AbstractLiveDataServer;
import com.opengamma.livedata.server.SubscriptionRequestReceiver;
import com.opengamma.livedata.server.datasender.MarketDataFudgeJmsSender;
import com.opengamma.livedata.server.datasender.MarketDataFudgeSender;
import com.opengamma.livedata.server.datasender.MarketDataSender;
import com.opengamma.transport.ByteArrayFudgeMessageReceiver;
import com.opengamma.transport.ByteArrayFudgeMessageSender;
import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.DirectInvocationByteArrayMessageSender;
import com.opengamma.transport.FudgeRequestDispatcher;
import com.opengamma.transport.InMemoryByteArrayRequestConduit;
import com.opengamma.transport.jms.ActiveMQTestUtil;

/**
 * 
 *
 * @author pietari
 */
public class LiveDataClientTestUtils {
  
  public static DistributedLiveDataClient getInMemoryConduitClient(AbstractLiveDataServer server) {
    ByteArrayFudgeRequestSender requestSender = new ByteArrayFudgeRequestSender(
        new InMemoryByteArrayRequestConduit(
            new FudgeRequestDispatcher(
                new SubscriptionRequestReceiver(server))));
    DistributedLiveDataClient liveDataClient = new DistributedLiveDataClient(requestSender);
    
    MarketDataSender marketDataSender = new MarketDataFudgeSender(
        new ByteArrayFudgeMessageSender(
            new DirectInvocationByteArrayMessageSender(
                new ByteArrayFudgeMessageReceiver(liveDataClient))));
    server.addMarketDataSender(marketDataSender);
    
    liveDataClient.setFudgeContext(liveDataClient.getFudgeContext());
    
    return liveDataClient;
  }
  
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
    
    MarketDataFudgeJmsSender mdfjs = new MarketDataFudgeJmsSender();
    mdfjs.setJmsTemplate(marketDataTemplate);
    server.addMarketDataSender(mdfjs);
    
    liveDataClient.setFudgeContext(liveDataClient.getFudgeContext());
    
    return liveDataClient;
  }

}
