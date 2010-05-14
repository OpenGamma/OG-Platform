/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.datasender;

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
import com.opengamma.livedata.server.MarketDataDistributor;

/**
 * 
 *
 * @author kirk
 */
public class MarketDataFudgeJmsSender implements MarketDataSender {
  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataFudgeJmsSender.class);
  
  private JmsTemplate _jmsTemplate;
  private final FudgeContext _fudgeContext;
  
  private volatile boolean _interrupted = false;
  
  public MarketDataFudgeJmsSender() {
    _fudgeContext = new FudgeContext();
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

  /**
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public void sendMarketData(MarketDataDistributor distributor, FudgeFieldContainer normalizedMsg) {
    if (_interrupted) {
      s_logger.debug("Interrupted - not sending message {}", normalizedMsg);
      return;      
    }
    
    DistributionSpecification distributionSpec = distributor.getDistributionSpec();
    
    LiveDataValueUpdateBean liveDataValueUpdateBean = distributor.getLastKnownValueUpdate();
    s_logger.debug("Sending Live Data update {}", liveDataValueUpdateBean);
    
    FudgeFieldContainer fudgeMsg = liveDataValueUpdateBean.toFudgeMsg(getFudgeContext());
    String destinationName = distributionSpec.getJmsTopic();
    final byte[] bytes = getFudgeContext().toByteArray(fudgeMsg);
    getJmsTemplate().send(destinationName, new MessageCreator() {
      @Override
      public Message createMessage(Session session) throws JMSException {
        // TODO kirk 2009-10-30 -- We want to put stuff in the properties as well I think.
        BytesMessage bytesMessage = session.createBytesMessage();
        bytesMessage.writeBytes(bytes);
        return bytesMessage;
      }
    });
  }
  
  public synchronized void transportInterrupted() {
    s_logger.error("Transport interrupted");
    _interrupted = true;
  }

  public synchronized void transportResumed() {
    s_logger.info("Transport resumed");
    _interrupted = false;
  }

}
