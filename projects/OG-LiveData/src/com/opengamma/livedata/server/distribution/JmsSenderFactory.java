/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.springframework.jms.core.JmsTemplate;

/**
 * Creates {@link JmsSender}'s.
 */
public class JmsSenderFactory implements MarketDataSenderFactory {
  
  /**
   * A {@code WeakHashMap} is used here so the senders can be garbage collected
   * automatically when they're no longer used.
   */
  private Map<JmsSender, Object> _allActiveSenders = Collections.synchronizedMap(new WeakHashMap<JmsSender, Object>()); 
  
  private JmsTemplate _jmsTemplate;
  
  public JmsSenderFactory() {
  }
  
  public JmsSenderFactory(JmsTemplate jmsTemplate) {
    setJmsTemplate(jmsTemplate);
  }
  
  /**
   * @return the jmsTemplate
   */
  public JmsTemplate getJmsTemplate() {
    return _jmsTemplate;
  }
  
  public void setJmsTemplate(JmsTemplate jmsTemplate) {
    _jmsTemplate = jmsTemplate;
  }
  
  public void transportInterrupted() {
    for (JmsSender sender : _allActiveSenders.keySet()) {
      sender.transportInterrupted();      
    }
  }

  public void transportResumed() {
    for (JmsSender sender : _allActiveSenders.keySet()) {
      sender.transportResumed();      
    }
  }

  @Override
  public Collection<MarketDataSender> create(MarketDataDistributor distributor) {
    JmsSender sender = new JmsSender(_jmsTemplate, distributor);
    _allActiveSenders.put(sender, new Object());
    return Collections.<MarketDataSender>singleton(sender);
  }

}
