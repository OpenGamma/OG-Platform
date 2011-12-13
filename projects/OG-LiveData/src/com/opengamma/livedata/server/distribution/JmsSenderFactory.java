/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;

/**
 * Factory to create JMS senders.
 */
public class JmsSenderFactory implements MarketDataSenderFactory {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(JmsSenderFactory.class);

  /**
   * A {@code WeakHashMap} is used here so the senders can be garbage collected
   * automatically when they're no longer used.
   */
  private final Set<JmsSender> _allActiveSenders = Collections.newSetFromMap(new WeakHashMap<JmsSender, Boolean>());
  /**
   * The JMS connector.
   */
  private JmsConnector _jmsConnector;
  /**
   * The Fudge context.
   */
  private FudgeContext _fudgeContext;
  /**
   * The executor.
   */
  private final ExecutorService _executor;

  /**
   * Creates an instance.
   */
  public JmsSenderFactory() {
    final int threads = Math.max(Runtime.getRuntime().availableProcessors(), 1) * 2;
    final ThreadPoolExecutor executor = new ThreadPoolExecutor(threads, threads, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    executor.allowCoreThreadTimeOut(true);
    _executor = executor;
    setFudgeContext(OpenGammaFudgeContext.getInstance());
  }

  /**
   * Creates an instance.
   * 
   * @param jmsConnector  the JMS connector
   */
  public JmsSenderFactory(JmsConnector jmsConnector) {
    this();
    setJmsConnector(jmsConnector);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the JMS connector.
   * 
   * @return the JMS connector
   */
  public JmsConnector getJmsConnector() {
    return _jmsConnector;
  }

  /**
   * Sets the JMS connector.
   * 
   * @param jmsConnector  the connector
   */
  public void setJmsConnector(JmsConnector jmsConnector) {
    _jmsConnector = jmsConnector;
  }

  /**
   * Gets the Fudge context.
   * 
   * @return the Fudge context
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Sets the Fudge context.
   * 
   * @param fudgeContext  the Fudge context
   */
  public void setFudgeContext(FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  public synchronized void transportInterrupted() {
    s_logger.warn("JMS transport interrupted; notifying {} senders", _allActiveSenders.size());
    for (final JmsSender sender : _allActiveSenders) {
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
    for (final JmsSender sender : _allActiveSenders) {
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
    JmsSender sender = new JmsSender(_jmsConnector, distributor, getFudgeContext());
    _allActiveSenders.add(sender);
    return Collections.<MarketDataSender>singleton(sender);
  }

}
