/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.transport.CollectingByteArrayMessageReceiver;
import com.opengamma.transport.jms.JmsByteArrayMessageDispatcher;
import com.opengamma.util.test.ActiveMQTestUtil;

/**
 * 
 *
 */
@Ignore // PL 24.6.2010: Putting on ignore as it's failing intermittently on Bamboo and I can't figure out why
public class JmsSenderTest {
  
  private static CollectingByteArrayMessageReceiver _collectingReceiver;
  private static DefaultMessageListenerContainer _container;
  private static MarketDataDistributor _mdd;
  private static JmsSenderFactory _factory;
  
  @BeforeClass
  public static void setUpClass() {
    ActiveMQConnectionFactory cf = ActiveMQTestUtil.createTestConnectionFactory();

    JmsTemplate jmsTemplate = new JmsTemplate(cf);
    jmsTemplate.setPubSubDomain(true);
    
    _factory = new JmsSenderFactory(jmsTemplate);
    _mdd = MarketDataDistributorTest.getTestDistributor(_factory);
    
    _collectingReceiver = new CollectingByteArrayMessageReceiver();
    JmsByteArrayMessageDispatcher messageDispatcher = new JmsByteArrayMessageDispatcher(_collectingReceiver);
    
    _container = new DefaultMessageListenerContainer();
    _container.setConnectionFactory(cf);
    _container.setMessageListener(messageDispatcher);
    _container.setDestinationName(_mdd.getDistributionSpec().getJmsTopic());
    _container.setPubSubDomain(true);
    _container.afterPropertiesSet();
    _container.start();
  }
  
  @AfterClass
  public static void tearDown() {
    _container.stop();
    _container.destroy();
  }
  
  @Before
  public void setUp() {
    _collectingReceiver.clearMessages();
  }
  
  private void ensureStarted() {
    assertTrue(_container.isActive());
    assertTrue(_container.isRunning());
    assertEquals(1, _container.getActiveConsumerCount());
  }
  
  @Test(timeout=30000)
  public void simpleScenario() throws Exception {
    ensureStarted();
    
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("name", "ruby");
    
    _mdd.distributeLiveData(msg);
    _mdd.distributeLiveData(FudgeContext.EMPTY_MESSAGE); // empty message not sent
    
    // allow data to flow through
    while (_collectingReceiver.getMessages().isEmpty()) {
      Thread.sleep(100);
    }
    Thread.sleep(100);
    assertEquals(1, _collectingReceiver.getMessages().size());
    
    for (byte[] byteArray : _collectingReceiver.getMessages()) {
      FudgeMsgEnvelope msgEnvelope = FudgeContext.GLOBAL_DEFAULT.deserialize(byteArray);
      LiveDataValueUpdateBean update = LiveDataValueUpdateBean.fromFudgeMsg(msgEnvelope.getMessage());
      assertEquals(msg, update.getFields());
    }
  }
  
  @Test(timeout=30000)
  public void reconnectionScenario() throws Exception {
    ensureStarted();
    
    MutableFudgeFieldContainer msg1 = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg1.add("name", "olivia");
    
    MutableFudgeFieldContainer msg2 = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg2.add("name", "ruby");
    
    MutableFudgeFieldContainer msg3 = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg3.add("name", "sophie"); // will overwrite ruby
    msg3.add("address", "london");
    
    MutableFudgeFieldContainer msg4 = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg4.add("name", "chloe");
    
    _mdd.distributeLiveData(msg1);
    _factory.transportInterrupted();
    _mdd.distributeLiveData(msg2);
    _mdd.distributeLiveData(msg3);
    _factory.transportResumed();
    _mdd.distributeLiveData(msg4);
    
    // allow data to flow through
    while (_collectingReceiver.getMessages().size() < 3) {
      Thread.sleep(100);
    }
    Thread.sleep(100);
    assertEquals(3, _collectingReceiver.getMessages().size());
    LiveDataValueUpdateBean[] updates = new LiveDataValueUpdateBean[3]; 
    
    for (int i = 0; i < _collectingReceiver.getMessages().size(); i++) {
      FudgeMsgEnvelope msgEnvelope = FudgeContext.GLOBAL_DEFAULT.deserialize(_collectingReceiver.getMessages().get(i));
      LiveDataValueUpdateBean update = LiveDataValueUpdateBean.fromFudgeMsg(msgEnvelope.getMessage());
      updates[i] = update;
    }
    
    assertEquals(msg1, updates[0].getFields());
    assertEquals(1, updates[0].getSequenceNumber()); // starts from 1 because simpleScenario() already sent 1
    
    assertEquals(msg3, updates[1].getFields());
    assertEquals(3, updates[1].getSequenceNumber());
    
    assertEquals(msg4, updates[2].getFields());
    assertEquals(4, updates[2].getSequenceNumber());
  }

}
