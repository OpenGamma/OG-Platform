/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.analyticservice;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Random;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.MessageCreator;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.scripts.Scriptable;
import com.opengamma.transport.ByteArrayFudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.jms.JmsByteArrayMessageDispatcher;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;
import com.opengamma.util.money.Currency;

/**
 * 
 */
@Scriptable
public class ExampleAnalyticServiceUsage extends AbstractTool<ToolContext> {
  
  private static final String QUEUE_OPTION = "queue";
  private static final String ACTIVE_MQ_OPTION = "activeMQ";
  private static final int WAIT_BTW_TRADES = 10000;
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleAnalyticServiceUsage.class);
  private static final Counterparty COUNTERPARTY = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "TEST"));
  private static final String PROVIDER_ID_NAME  = "providerId";
  private static final String RANDOM_ID_SCHEME = "Rnd";
  private static final String PREFIX = "OGAnalytics";
  private static final String SEPARATOR = ".";
  
  private final Random _random = new Random();
  
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) {  // CSIGNORE
    new ExampleAnalyticServiceUsage().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {
    
    CommandLine commandLine = getCommandLine();
    
    String activeMQUrl = commandLine.getOptionValue(ACTIVE_MQ_OPTION);
    String destinationName = commandLine.getOptionValue(QUEUE_OPTION);
    
    ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(activeMQUrl);
    activeMQConnectionFactory.setWatchTopicAdvisories(false);
    
    PooledConnectionFactory jmsConnectionFactory = new PooledConnectionFactory(activeMQConnectionFactory);
    jmsConnectionFactory.start();
    
    JmsConnectorFactoryBean jmsConnectorFactoryBean = new com.opengamma.util.jms.JmsConnectorFactoryBean();
    jmsConnectorFactoryBean.setName("StandardJms");
    jmsConnectorFactoryBean.setConnectionFactory(jmsConnectionFactory);
    jmsConnectorFactoryBean.setClientBrokerUri(URI.create(activeMQUrl));
    
    JmsConnector jmsConnector = jmsConnectorFactoryBean.getObjectCreating(); 
    ByteArrayFudgeMessageReceiver fudgeReceiver = new ByteArrayFudgeMessageReceiver(new FudgeMessageReceiver() {
      
      @Override
      public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
        FudgeMsg message = msgEnvelope.getMessage();
        s_logger.debug("received {}", message);
      }
    }, s_fudgeContext);
    final JmsByteArrayMessageDispatcher jmsDispatcher = new JmsByteArrayMessageDispatcher(fudgeReceiver);
    
    Connection connection = jmsConnector.getConnectionFactory().createConnection();
    connection.start();
    
    pushTrade("ARG", connection, destinationName, jmsConnector, jmsDispatcher);
    Thread.sleep(WAIT_BTW_TRADES);
    pushTrade("MMM", connection, destinationName, jmsConnector, jmsDispatcher);
    Thread.sleep(WAIT_BTW_TRADES * 10);
    connection.stop();
    jmsConnectionFactory.stop();
   
  }
  
  private void pushTrade(String securityId, Connection connection, String destinationName, JmsConnector jmsConnector, JmsByteArrayMessageDispatcher jmsDispatcher) {
    String providerId = generateTrade(securityId, destinationName, jmsConnector);
    String topicStr = PREFIX + SEPARATOR + providerId + SEPARATOR + "Default" + SEPARATOR + ValueRequirementNames.FAIR_VALUE;
    try {
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Topic topic = session.createTopic(topicStr);
      final MessageConsumer messageConsumer = session.createConsumer(topic);
      messageConsumer.setMessageListener(jmsDispatcher);
    } catch (JMSException e) {
      throw new OpenGammaRuntimeException("Failed to create subscription to JMS topics ", e);
    }  
  }

  private String generateTrade(String securityId, String destinationName, JmsConnector jmsConnector) {
    SimpleTrade trade = new SimpleTrade();
    trade.setCounterparty(COUNTERPARTY);
    trade.setPremiumCurrency(Currency.USD);
    trade.setQuantity(BigDecimal.valueOf(_random.nextInt(10) + 10));
    trade.setTradeDate(LocalDate.now());
    String providerId = GUIDGenerator.generate().toString();
    trade.addAttribute(PROVIDER_ID_NAME, RANDOM_ID_SCHEME + "~" + providerId);
    trade.setSecurityLink(new SimpleSecurityLink(ExternalSchemes.syntheticSecurityId(securityId)));
    s_logger.debug("Generated {}", trade);
    
    FudgeMsg msg = s_fudgeContext.toFudgeMsg(trade).getMessage();
    
    s_logger.debug("sending {} to {}", msg, destinationName);
    
    final byte[] bytes = s_fudgeContext.toByteArray(msg);
    
    jmsConnector.getJmsTemplateQueue().send(destinationName, new MessageCreator() {
      @Override
      public Message createMessage(Session session) throws JMSException {
        BytesMessage bytesMessage = session.createBytesMessage();
        bytesMessage.writeBytes(bytes);
        return bytesMessage;
      }
    });
    return providerId;
  }

  //-------------------------------------------------------------------------
  @Override
  protected Options createOptions(boolean mandatoryConfig) {
    Options options = super.createOptions(mandatoryConfig);
    options.addOption(createActiveMQOption());
    options.addOption(createDestinationOption());
    return options;
  }
  
  @SuppressWarnings("static-access")
  private Option createActiveMQOption() {
    return OptionBuilder.isRequired(true)
                        .hasArgs()
                        .withArgName("ActiveMQ URL")
                        .withDescription("the ActiveMQ broker URL")
                        .withLongOpt(ACTIVE_MQ_OPTION)
                        .create("a");
  }
  
  @SuppressWarnings("static-access")
  private Option createDestinationOption() {
    return OptionBuilder.isRequired(true)
                        .hasArgs()
                        .withArgName("queue name")
                        .withDescription("JMS queue name")
                        .withLongOpt(QUEUE_OPTION)
                        .create("q");
  }
  
}
