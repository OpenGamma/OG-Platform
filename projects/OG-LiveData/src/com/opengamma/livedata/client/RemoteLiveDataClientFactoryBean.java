/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import javax.jms.ConnectionFactory;

import org.springframework.jms.core.JmsTemplate;

import com.opengamma.livedata.LiveDataClient;
import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.jms.JmsByteArrayMessageSender;
import com.opengamma.transport.jms.JmsByteArrayRequestSender;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Creates a {@link JmsLiveDataClient}.
 */
public class RemoteLiveDataClientFactoryBean extends SingletonFactoryBean<LiveDataClient> {

  private ConnectionFactory _connectionFactory;
  private String _subscriptionTopic;
  private String _entitlementTopic;
  private String _heartbeatTopic;
  
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
  
  public void setHeartbeatTopic(String heartbeatTopic) {
    _heartbeatTopic = heartbeatTopic;
  }
  
  public String getHeartbeatTopic() {
    return _heartbeatTopic;
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
    if (getHeartbeatTopic() != null) {
      JmsByteArrayMessageSender jmsHeartbeatSender = new JmsByteArrayMessageSender(getHeartbeatTopic(), jmsTemplate);
      liveDataClient.setHeartbeatMessageSender(jmsHeartbeatSender);
    }
    liveDataClient.start();
    return liveDataClient;
  }
  
}
