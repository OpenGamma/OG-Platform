/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.analyticservice;

import java.util.List;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.MessageCreator;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.jms.JmsConnector;

/**
 * Distributes computed values over JMS
 */
public class JmsAnalyticsDistributor implements AnalyticResultReceiver {
  
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(JmsAnalyticsDistributor.class);
  
  /**
   * The JMS connector.
   */
  private final JmsConnector _jmsConnector;
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;
  
  private final JmsTopicNameResolver _jmsTopicNameResolver;
    
  public JmsAnalyticsDistributor(final JmsTopicNameResolver jmsTopicNameResolver, final FudgeContext fudgeContext, final JmsConnector jmsConnector) {
    ArgumentChecker.notNull(jmsTopicNameResolver, "jmsTopicNameResolver");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(jmsConnector, "jmsConnector");
    _jmsTopicNameResolver = jmsTopicNameResolver;
    _jmsConnector = jmsConnector;
    _fudgeContext = fudgeContext;
  }

  @Override
  public void analyticReceived(List<ViewResultEntry> allResults) {
    ArgumentChecker.notNull(allResults, "viewResultEntries");
    s_logger.debug("analytic receivied {} view results", allResults.size());
    for (ViewResultEntry viewResultEntry : allResults) {
      String calcConfig = viewResultEntry.getCalculationConfiguration();
      ValueSpecification valueSpec = viewResultEntry.getComputedValue().getSpecification();
      ComputedValue computedValue = viewResultEntry.getComputedValue();
      
      ComputationTargetType type = valueSpec.getTargetSpecification().getType();
      
      if (type.isTargetType(ComputationTargetType.POSITION)) {
        
        String destinationName = _jmsTopicNameResolver.resolve(new JmsTopicNameResolveRequest(calcConfig, valueSpec));
        FudgeMsg fudgeMsg = _fudgeContext.toFudgeMsg(computedValue).getMessage();
        final byte[] bytes = _fudgeContext.toByteArray(fudgeMsg);
        
        s_logger.debug("sending {} to {}", fudgeMsg, destinationName);
        
        _jmsConnector.getJmsTemplateTopic().send(destinationName, new MessageCreator() {
          @Override
          public Message createMessage(Session session) throws JMSException {
            BytesMessage bytesMessage = session.createBytesMessage();
            bytesMessage.writeBytes(bytes);
            return bytesMessage;
          }
        });
      }
    } 
  }
  
}
