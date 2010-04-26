/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

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
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class MarketDataFudgeJmsSender implements MarketDataSender {
  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataFudgeJmsSender.class);
  
  private final JmsTemplate _jmsTemplate;
  private final FudgeContext _fudgeContext;
  
  public MarketDataFudgeJmsSender(JmsTemplate jmsTemplate) {
    this(jmsTemplate, new FudgeContext());
  }
  
  public MarketDataFudgeJmsSender(JmsTemplate jmsTemplate, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(jmsTemplate, "JmsTemplate");
    ArgumentChecker.notNull(fudgeContext, "Fudge Context");
    _jmsTemplate = jmsTemplate;
    _fudgeContext = fudgeContext;
  }
  
  /**
   * @return the jmsTemplate
   */
  public JmsTemplate getJmsTemplate() {
    return _jmsTemplate;
  }

  /**
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public void sendMarketData(DistributionSpecification distributionSpec, FudgeFieldContainer normalizedMsg) {
    
    LiveDataValueUpdateBean liveDataValueUpdateBean = new LiveDataValueUpdateBean(
        System.currentTimeMillis(), 
        distributionSpec.getFullyQualifiedLiveDataSpecification(), 
        normalizedMsg);
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

}
