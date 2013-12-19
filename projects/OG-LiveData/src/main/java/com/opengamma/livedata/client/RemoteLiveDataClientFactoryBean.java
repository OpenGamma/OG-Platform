/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jms.core.JmsTemplate;

import com.opengamma.livedata.LiveDataClient;
import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.jms.JmsByteArrayMessageSender;
import com.opengamma.transport.jms.JmsByteArrayRequestSender;
import com.opengamma.util.NamedThreadPoolFactory;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;

/**
 * Creates a {@link JmsLiveDataClient}.
 */
public class RemoteLiveDataClientFactoryBean extends SingletonFactoryBean<DistributedLiveDataClient> implements DisposableBean {
  private static final Logger s_logger = LoggerFactory.getLogger(RemoteLiveDataClientFactoryBean.class);

  private JmsConnector _jmsConnector;
  private String _subscriptionTopic;
  private String _entitlementTopic;
  private String _heartbeatTopic;
  private int _maxConcurrentRequests = Integer.MAX_VALUE;

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

  /**
   * Sets the maximum number of concurrent JMS requests that will be made. Each concurrent request requires a thread to be allocated.
   * 
   * @param maxConcurrentRequests maximum requests - set to 0 or negative for unlimited
   */
  public void setMaxConcurrentRequests(final int maxConcurrentRequests) {
    if (maxConcurrentRequests <= 0) {
      _maxConcurrentRequests = Integer.MAX_VALUE;
    } else {
      _maxConcurrentRequests = maxConcurrentRequests;
    }
  }

  /**
   * Returns the maximum number of concurrent JMS requests that will be made. Each concurrent request requires a thread to be allocated.
   * 
   * @return maximum requests
   */
  public int getMaxConcurrentRequests() {
    return _maxConcurrentRequests;
  }

  @Override
  protected DistributedLiveDataClient createObject() {
    final JmsTemplate jmsTemplate = getJmsConnector().getJmsTemplateTopic();
    final int maxConcurrent = getMaxConcurrentRequests();
    final ThreadPoolExecutor executor = new ThreadPoolExecutor(maxConcurrent, maxConcurrent, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadPoolFactory(
        "RemoteLiveDataClient"));
    final JmsByteArrayRequestSender jmsSubscriptionRequestSender = new JmsByteArrayRequestSender(getSubscriptionTopic(), jmsTemplate, executor);
    final ByteArrayFudgeRequestSender fudgeSubscriptionRequestSender = new ByteArrayFudgeRequestSender(jmsSubscriptionRequestSender);
    final JmsByteArrayRequestSender jmsEntitlementRequestSender = new JmsByteArrayRequestSender(getEntitlementTopic(), jmsTemplate, executor);
    final ByteArrayFudgeRequestSender fudgeEntitlementRequestSender = new ByteArrayFudgeRequestSender(jmsEntitlementRequestSender);
    final JmsLiveDataClient liveDataClient = new JmsLiveDataClient(fudgeSubscriptionRequestSender, fudgeEntitlementRequestSender, getJmsConnector(), OpenGammaFudgeContext.getInstance(),
        JmsLiveDataClient.DEFAULT_NUM_SESSIONS);
    liveDataClient.setFudgeContext(OpenGammaFudgeContext.getInstance());
    if (getHeartbeatTopic() != null) {
      JmsByteArrayMessageSender jmsHeartbeatSender = new JmsByteArrayMessageSender(getHeartbeatTopic(), jmsTemplate);
      liveDataClient.setHeartbeatMessageSender(jmsHeartbeatSender);
    }
    liveDataClient.start();
    s_logger.debug("Created and started live data client using {} subscription topic {}, entitlement topic {} and heartbeat topic {}", new Object[] {getJmsConnector().getClientBrokerUri(),
        getSubscriptionTopic(), getEntitlementTopic(), getHeartbeatTopic() });
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
