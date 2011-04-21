/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

/**
 * Creates {@link JmsSender}'s.
 */
public class JmsSenderFactory implements MarketDataSenderFactory {
  
  private static final Logger s_logger = LoggerFactory.getLogger(JmsSenderFactory.class);

  /**
   * A {@code WeakHashMap} is used here so the senders can be garbage collected
   * automatically when they're no longer used.
   */
  private final Map<JmsSender, Object> _allActiveSenders = new WeakHashMap<JmsSender, Object>();
  
  private JmsTemplate _jmsTemplate;
  
  private final ExecutorService _executor;

  public JmsSenderFactory() {
    final int threads = Math.max(Runtime.getRuntime().availableProcessors(), 1) * 2;
    final ThreadPoolExecutor executor = new ThreadPoolExecutor(threads, threads, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    executor.allowCoreThreadTimeOut(true);
    _executor = executor;
  }
  
  public JmsSenderFactory(JmsTemplate jmsTemplate) {
    this();
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
  
  public synchronized void transportInterrupted() {
    s_logger.warn("JMS transport interrupted; notifying {} senders", _allActiveSenders.size());
    for (final JmsSender sender : _allActiveSenders.keySet()) {
      _executor.execute(new Runnable() {
        @Override
        public void run() {
          sender.transportInterrupted();
        }
      });
    }
  }

  public synchronized void transportResumed() {
    s_logger.info("JMS transport resumed; notifying {} senders", _allActiveSenders.size());
    for (final JmsSender sender : _allActiveSenders.keySet()) {
      _executor.execute(new Runnable() {
        @Override
        public void run() {
          sender.transportResumed();
        }
      });
    }
  }

  @Override
  public synchronized Collection<MarketDataSender> create(MarketDataDistributor distributor) {
    s_logger.debug("Created JmsSender for {}", distributor);
    JmsSender sender = new JmsSender(_jmsTemplate, distributor);
    _allActiveSenders.put(sender, new Object());
    return Collections.<MarketDataSender>singleton(sender);
  }

}
