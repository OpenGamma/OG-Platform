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
import com.opengamma.livedata.server.distribution.FudgeSenderFactory;
import com.opengamma.livedata.server.distribution.JmsSender;
import com.opengamma.livedata.server.distribution.FudgeSender;
import com.opengamma.livedata.server.distribution.JmsSenderFactory;
import com.opengamma.livedata.server.distribution.MarketDataSender;
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
    
    FudgeSenderFactory factory = new FudgeSenderFactory(
        new ByteArrayFudgeMessageSender(
            new DirectInvocationByteArrayMessageSender(
                new ByteArrayFudgeMessageReceiver(liveDataClient))));
    server.setMarketDataSenderFactory(factory);
    
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
    
    JmsSenderFactory factory = new JmsSenderFactory();
    factory.setJmsTemplate(marketDataTemplate);
    server.setMarketDataSenderFactory(factory);
    
    liveDataClient.setFudgeContext(liveDataClient.getFudgeContext());
    
    return liveDataClient;
  }

}
