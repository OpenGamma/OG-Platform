/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.jms.core.JmsTemplate;

import com.opengamma.livedata.LiveDataClient;
import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.jms.JmsByteArrayMessageSender;
import com.opengamma.transport.jms.JmsByteArrayRequestSender;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;

/**
 * Creates a {@link JmsLiveDataClient}.
 */
public class RemoteLiveDataClientFactoryBean extends SingletonFactoryBean<LiveDataClient> implements DisposableBean {

  private JmsConnector _jmsConnector;
  private String _subscriptionTopic;
  private String _entitlementTopic;
  private String _heartbeatTopic;
  
  public void setJmsConnector(final JmsConnector jmsConnector) {
    _jmsConnector = jmsConnector;
  }
  
  public JmsConnector getJmsConnector() {
    return _jmsConnector;
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
    final JmsTemplate jmsTemplate = getJmsConnector().getJmsTemplateTopic();
    
    JmsByteArrayRequestSender jmsSubscriptionRequestSender = new JmsByteArrayRequestSender(getSubscriptionTopic(), jmsTemplate);
    ByteArrayFudgeRequestSender fudgeSubscriptionRequestSender = new ByteArrayFudgeRequestSender(jmsSubscriptionRequestSender);
    
    JmsByteArrayRequestSender jmsEntitlementRequestSender = new JmsByteArrayRequestSender(getEntitlementTopic(), jmsTemplate);
    ByteArrayFudgeRequestSender fudgeEntitlementRequestSender = new ByteArrayFudgeRequestSender(jmsEntitlementRequestSender);
    
    final JmsLiveDataClient liveDataClient = new JmsLiveDataClient(
        fudgeSubscriptionRequestSender, 
        fudgeEntitlementRequestSender, 
        getJmsConnector(), 
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

  @Override
  public void destroy() {
    LiveDataClient ldc = getObject();
    if (ldc != null) {
      ldc.close();
    }
  }

}
