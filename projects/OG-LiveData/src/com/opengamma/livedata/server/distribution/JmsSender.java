/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import java.util.concurrent.Semaphore;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.MessageCreator;

import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.livedata.LiveDataValueUpdateBeanFudgeBuilder;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.jms.JmsConnector;

/**
 * This {@link MarketDataSender} sends market data to JMS.
 * <p>
 * When the sender loses connection to JMS, it starts building a 
 * cumulative delta of changes. This cumulative delta is published when 
 * the sender reconnects.
 */
public class JmsSender implements MarketDataSender {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(JmsSender.class);

  /**
   * The JMS connector.
   */
  private final JmsConnector _jmsConnector;
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;
  /**
   * The distributor.
   */
  private final MarketDataDistributor _distributor;
  /**
   * The field value history.
   */
  private final FieldHistoryStore _cumulativeDelta = new FieldHistoryStore();
  /**
   * The last sequence number.
   */
  private long _lastSequenceNumber;
  /**
   * Whether the sender is interrupted.
   */
  private volatile boolean _interrupted;
  /**
   * The internal lock.
   */
  private final Semaphore _lock = new Semaphore(1);

  /**
   * Creates an instance.
   * 
   * @param jmsConnector  the JMS connector, not null
   * @param distributor  the distributor, not null
   * @param fudgeContext  the Fudge context, not null
   */
  public JmsSender(JmsConnector jmsConnector, MarketDataDistributor distributor, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(jmsConnector, "jmsConnector");
    ArgumentChecker.notNull(distributor, "Market data distributor");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _jmsConnector = jmsConnector;
    _fudgeContext = fudgeContext;
    _distributor = distributor;
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataDistributor getDistributor() {
    return _distributor;
  }

  //-------------------------------------------------------------------------
  @Override
  public void sendMarketData(LiveDataValueUpdateBean data) {
    _lock.acquireUninterruptibly();
    try {
      _cumulativeDelta.liveDataReceived(data.getFields());
      _lastSequenceNumber = data.getSequenceNumber(); 
      
      if (_interrupted) {
        s_logger.debug("{}: Interrupted - not sending message", this);
        return;
      }
      
      send();
    } finally {
      _lock.release();
    }
  }

  private void send() {
    DistributionSpecification distributionSpec = getDistributor().getDistributionSpec();
    
    LiveDataValueUpdateBean liveDataValueUpdateBean = new LiveDataValueUpdateBean(
        _lastSequenceNumber, 
        distributionSpec.getFullyQualifiedLiveDataSpecification(), 
        _cumulativeDelta.getLastKnownValues());
    s_logger.debug("{}: Sending Live Data update {}", this, liveDataValueUpdateBean);
    
    FudgeMsg fudgeMsg = LiveDataValueUpdateBeanFudgeBuilder.toFudgeMsg(new FudgeSerializer(_fudgeContext), liveDataValueUpdateBean);
    String destinationName = distributionSpec.getJmsTopic();
    final byte[] bytes = _fudgeContext.toByteArray(fudgeMsg);
    
    _jmsConnector.getJmsTemplateTopic().send(destinationName, new MessageCreator() {
      @Override
      public Message createMessage(Session session) throws JMSException {
        // TODO kirk 2009-10-30 -- We want to put stuff in the properties as well I think.
        BytesMessage bytesMessage = session.createBytesMessage();
        bytesMessage.writeBytes(bytes);
        return bytesMessage;
      }
    });
    
    _cumulativeDelta.clear();
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if the sender is interrupted.
   * 
   * @return true if interrupted
   */
  public boolean isInterrupted() {
    return _interrupted;
  }

  /**
   * Indicates that the transport was interrupted, setting the flag.
   */
  public void transportInterrupted() {
    s_logger.error("Transport interrupted {}", this);
    _interrupted = true;
  }

  /**
   * Marks the sender as active again to stop batching up pending messages, and triggers a send
   * if a send is not already active. Note that it is not safe to call this method from the JMS
   * invoked transportResume message. For example ActiveMQ holds a lock to do an initial 'send'
   * and then gains a lock for the failover while another thread holds the lock for the failover
   * as it calls the notifications so sending will cause deadlock. 
   */
  public void transportResumed() {
    s_logger.info("Transport resumed {}", this);
    _interrupted = false;
    // tryAcquire() is used to avoid re-entry to the send method if a sendMarketData is already
    // active as that will hold the semaphore.
    if (_lock.tryAcquire()) {
      try {
        if (!_cumulativeDelta.isEmpty()) {
          send();
        }
      } catch (RuntimeException e) {
        s_logger.error("transportResumed() failed", e);
      } finally {
        _lock.release();
      }
    }
  }

  @Override
  public String toString() {
    return "JmsSender[" + _distributor.getDistributionSpec().toString() +  "]";    
  }

}
