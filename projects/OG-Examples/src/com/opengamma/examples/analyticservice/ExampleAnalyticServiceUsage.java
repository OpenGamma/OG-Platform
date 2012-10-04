/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.analyticservice;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.time.calendar.LocalDate;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.MessageCreator;

import com.google.common.collect.Lists;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.generate.scripts.Scriptable;
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
  
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();
  
  private static final ExecutorService s_tradeUpdaterExecutor = Executors.newSingleThreadExecutor();
  
  private static final ExecutorService s_resultListenerExecutor = Executors.newSingleThreadExecutor();
  
  @Override
  protected void doRun() throws Exception {
    ToolContext toolContext = getToolContext();
    List<ExternalId> securities = getSecurityIds(toolContext.getSecurityMaster());
    
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
    
    BlockingQueue<String> topics = new LinkedBlockingQueue<String>();
        
    s_tradeUpdaterExecutor.submit(new TradeGenerator(securities, jmsConnectorFactoryBean.getObjectCreating(), destinationName, topics));
    
    Thread.sleep(WAIT_BTW_TRADES * 10);
    jmsConnectionFactory.stop();
   
  }
  
  private List<ExternalId> getSecurityIds(final SecurityMaster securityMaster) {
    List<ExternalId> result = Lists.newArrayList();
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setSecurityType(EquitySecurity.SECURITY_TYPE);
    SecuritySearchResult searchResult = securityMaster.search(request);
    for (ManageableSecurity security : searchResult.getSecurities()) {
      result.add(security.getExternalIdBundle().getExternalId(ExternalSchemes.OG_SYNTHETIC_TICKER));
    }
    return result;
  }

  /**
   * Main method to run the tool.
   */
  public static void main(String[] args) {  // CSIGNORE
    new ExampleAnalyticServiceUsage().initAndRun(args, ToolContext.class);
  }
  
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
  
  private class ResultListener extends TerminatableJob {
    
    private final BlockingQueue<String> _topics;
    
    public ResultListener(BlockingQueue<String> topics) {
      _topics = topics;
    }

    @Override
    protected void runOneCycle() {
      try {
        String topic = _topics.take();
        
        
        
      } catch (InterruptedException ex) {
        s_logger.warn("interuupted waiting", ex);
      }
    }
    
  }
  
  private class TradeGenerator extends TerminatableJob {
    
    private final List<ExternalId> _securities;
    private final JmsConnector _jmsConnector;
    private final String _destinationName;
    private final Random _random = new Random();
    private final BlockingQueue<String> _topics;
    
    public TradeGenerator(List<ExternalId> securities, JmsConnector jmsConnector, String destinationName, BlockingQueue<String> topics) {
      _securities = securities;
      _jmsConnector = jmsConnector;
      _destinationName = destinationName;
      _topics = topics;
    }

    @Override
    protected void runOneCycle() {
      Trade trade = generateTrade();
      
      FudgeMsg msg = s_fudgeContext.toFudgeMsg(trade).getMessage();
      
      s_logger.debug("sending {} to {}", msg, _destinationName);
      
      final byte[] bytes = s_fudgeContext.toByteArray(msg);
      
      _jmsConnector.getJmsTemplateTopic().send(_destinationName, new MessageCreator() {
        @Override
        public Message createMessage(Session session) throws JMSException {
          BytesMessage bytesMessage = session.createBytesMessage();
          bytesMessage.writeBytes(bytes);
          return bytesMessage;
        }
      });
      
      try {
        Thread.sleep(WAIT_BTW_TRADES);
      } catch (InterruptedException ex) {
        s_logger.warn("interuppted while sleeping ", ex);
        
      }
    }
    
    private Trade generateTrade() {
      SimpleTrade trade = new SimpleTrade();
      trade.setCounterparty(COUNTERPARTY);
      trade.setPremiumCurrency(Currency.USD);
      trade.setQuantity(BigDecimal.valueOf(_random.nextInt(10) + 10));
      trade.setTradeDate(LocalDate.now());
      String providerId = GUIDGenerator.generate().toString();
      trade.addAttribute(PROVIDER_ID_NAME, RANDOM_ID_SCHEME + "~" + providerId);
      String topic = PREFIX + SEPARATOR + providerId + SEPARATOR + "Default" + SEPARATOR + ValueRequirementNames.FAIR_VALUE;
      _topics.offer(topic);
      ExternalId externalId = _securities.get(_random.nextInt(_securities.size()));
      trade.setSecurityLink(new SimpleSecurityLink(externalId));
      s_logger.debug("Generated {}", trade);
      return trade;
    }
    
  }

}
