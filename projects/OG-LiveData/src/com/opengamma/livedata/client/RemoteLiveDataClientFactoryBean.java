/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import javax.jms.ConnectionFactory;

import org.springframework.jms.core.JmsTemplate;

import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.jms.JmsByteArrayRequestSender;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * Creates a LiveDataClient that connects to JMS published data.
 */
public class RemoteLiveDataClientFactoryBean extends SingletonFactoryBean<LiveDataClient> {

  private ConnectionFactory _connectionFactory;
  private String _subscriptionTopic;
  private String _entitlementTopic;
  
  public void setConnectionFactory(final ConnectionFactory connectionFactory) {
    _connectionFactory = connectionFactory;
  }
  
  public ConnectionFactory getConnectionFactory() {
    return _connectionFactory;
  }
  
  public void setSubscriptionTopic(final String subscriptionTopic) {
    _subscriptionTopic = subscriptionTopic;
  }
  
  public String getSubscriptionTopic() {
    return _subscriptionTopic;
  }
  
  public void setEntitlementTopic(String entitlementTopic) {
    _entitlementTopic = entitlementTopic;
  }

  public String getEntitlementTopic() {
    return _entitlementTopic;
  }
  
  @Override
  protected LiveDataClient createObject() {
    final JmsTemplate jmsTemplate = new JmsTemplate();
    jmsTemplate.setPubSubDomain(true);
    jmsTemplate.setConnectionFactory(getConnectionFactory());
    
    JmsByteArrayRequestSender jmsSubscriptionRequestSender = new JmsByteArrayRequestSender(getSubscriptionTopic(), jmsTemplate);
    ByteArrayFudgeRequestSender fudgeSubscriptionRequestSender = new ByteArrayFudgeRequestSender(jmsSubscriptionRequestSender);
    
    JmsByteArrayRequestSender jmsEntitlementRequestSender = new JmsByteArrayRequestSender(getEntitlementTopic(), jmsTemplate);
    ByteArrayFudgeRequestSender fudgeEntitlementRequestSender = new ByteArrayFudgeRequestSender(jmsEntitlementRequestSender);
    
    final JmsLiveDataClient liveDataClient = new JmsLiveDataClient(
        fudgeSubscriptionRequestSender, 
        fudgeEntitlementRequestSender, 
        getConnectionFactory(), 
        OpenGammaFudgeContext.getInstance(),
        JmsLiveDataClient.DEFAULT_NUM_SESSIONS);
    liveDataClient.setFudgeContext(OpenGammaFudgeContext.getInstance());
    liveDataClient.start();
    return liveDataClient;
  }
  
}
