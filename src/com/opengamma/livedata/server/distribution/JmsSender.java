/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.ArgumentChecker;

/**
 * This {@link MarketDataSender} sends market data to JMS.
 * <p>
 * When the sender loses connection to JMS, it starts building a 
 * cumulative delta of changes. This cumulative delta is published when 
 * the sender reconnects.
 *
 * @author kirk
 */
public class JmsSender implements MarketDataSender {
  private static final Logger s_logger = LoggerFactory.getLogger(JmsSender.class);
  
  private final JmsTemplate _jmsTemplate;
  private final FudgeContext _fudgeContext;
  private final MarketDataDistributor _distributor;
  
  private final FieldHistoryStore _cumulativeDelta = new FieldHistoryStore();
  private long _lastSequenceNumber;
  
  private boolean _interrupted = false;
  
  public JmsSender(JmsTemplate jmsTemplate, MarketDataDistributor distributor) {
    ArgumentChecker.notNull(jmsTemplate, "JMS template");
    ArgumentChecker.notNull(distributor, "Market data distributor");
    
    _jmsTemplate = jmsTemplate;
    _fudgeContext = new FudgeContext();
    _distributor = distributor;
  }
  
  @Override
  public MarketDataDistributor getDistributor() {
    return _distributor;
  }

  @Override
  public synchronized void sendMarketData(LiveDataValueUpdateBean data) {
    _cumulativeDelta.liveDataReceived(data.getFields());
    _lastSequenceNumber = data.getSequenceNumber(); 
    
    if (_interrupted) {
      s_logger.debug("Interrupted - not sending message {}", data);
      return;      
    }
    
    send();
  }
  
  private void send() {
    DistributionSpecification distributionSpec = getDistributor().getDistributionSpec();
    
    LiveDataValueUpdateBean liveDataValueUpdateBean = new LiveDataValueUpdateBean(
        _lastSequenceNumber, 
        _distributor.getDistributionSpec().getFullyQualifiedLiveDataSpecification(), 
        _cumulativeDelta.getLastKnownValues());
    s_logger.debug("Sending Live Data update {}", liveDataValueUpdateBean);
    
    FudgeFieldContainer fudgeMsg = liveDataValueUpdateBean.toFudgeMsg(_fudgeContext);
    String destinationName = distributionSpec.getJmsTopic();
    final byte[] bytes = _fudgeContext.toByteArray(fudgeMsg);
    
    _jmsTemplate.send(destinationName, new MessageCreator() {
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
  
  public synchronized boolean isInterrupted() {
    return _interrupted;
  }
  
  public synchronized void transportInterrupted() {
    s_logger.error("Transport interrupted");
    _interrupted = true;
  }

  public synchronized void transportResumed() {
    s_logger.info("Transport resumed");
    _interrupted = false;
    
    try {
      send();
    } catch (RuntimeException e) {
      s_logger.error("transportResumed() failed", e);
    }
  }

}
