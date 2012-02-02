/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.test;

import com.opengamma.livedata.client.DistributedLiveDataClient;
import com.opengamma.livedata.client.JmsLiveDataClient;
import com.opengamma.livedata.entitlement.EntitlementServer;
import com.opengamma.livedata.server.AbstractLiveDataServer;
import com.opengamma.livedata.server.SubscriptionRequestReceiver;
import com.opengamma.livedata.server.distribution.FudgeSenderFactory;
import com.opengamma.livedata.server.distribution.JmsSenderFactory;
import com.opengamma.transport.ByteArrayFudgeMessageReceiver;
import com.opengamma.transport.ByteArrayFudgeMessageSender;
import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.DirectInvocationByteArrayMessageSender;
import com.opengamma.transport.FudgeRequestDispatcher;
import com.opengamma.transport.InMemoryByteArrayRequestConduit;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.test.ActiveMQTestUtils;

/**
 * Utility methods to get LiveData clients suitable for testing.
 */
public class LiveDataClientTestUtils {
  
  public static DistributedLiveDataClient getInMemoryConduitClient(AbstractLiveDataServer server) {
    ByteArrayFudgeRequestSender subscriptionRequestSender = getSubscriptionRequestSender(server);
    ByteArrayFudgeRequestSender entitlementRequestSender = getEntitlementRequestSender(server);
    DistributedLiveDataClient liveDataClient = new DistributedLiveDataClient(subscriptionRequestSender, entitlementRequestSender);
    
    FudgeSenderFactory factory = new FudgeSenderFactory(
        new ByteArrayFudgeMessageSender(
            new DirectInvocationByteArrayMessageSender(
                new ByteArrayFudgeMessageReceiver(liveDataClient))));
    server.setMarketDataSenderFactory(factory);
    
    liveDataClient.setFudgeContext(liveDataClient.getFudgeContext());
    
    return liveDataClient;
  }

  public static JmsLiveDataClient getJmsClient(AbstractLiveDataServer server) {
    ByteArrayFudgeRequestSender subscriptionRequestSender = getSubscriptionRequestSender(server);
    ByteArrayFudgeRequestSender entitlementRequestSender = getEntitlementRequestSender(server);
    
    JmsConnector jmsConnector = ActiveMQTestUtils.createTestJmsConnector();
    JmsLiveDataClient liveDataClient = new JmsLiveDataClient(
        subscriptionRequestSender, 
        entitlementRequestSender,
        jmsConnector);
    
    JmsSenderFactory factory = new JmsSenderFactory();
    factory.setJmsConnector(jmsConnector);
    server.setMarketDataSenderFactory(factory);
    
    liveDataClient.setFudgeContext(liveDataClient.getFudgeContext());
    liveDataClient.start();
    
    return liveDataClient;
  }
  
  private static ByteArrayFudgeRequestSender getEntitlementRequestSender(AbstractLiveDataServer server) {
    ByteArrayFudgeRequestSender entitlementRequestSender = new ByteArrayFudgeRequestSender(
        new InMemoryByteArrayRequestConduit(
            new FudgeRequestDispatcher(
                new EntitlementServer(server.getEntitlementChecker()))));
    return entitlementRequestSender;
  }

  private static ByteArrayFudgeRequestSender getSubscriptionRequestSender(AbstractLiveDataServer server) {
    ByteArrayFudgeRequestSender subscriptionRequestSender = new ByteArrayFudgeRequestSender(
        new InMemoryByteArrayRequestConduit(
            new FudgeRequestDispatcher(
                new SubscriptionRequestReceiver(server))));
    return subscriptionRequestSender;
  }

}
