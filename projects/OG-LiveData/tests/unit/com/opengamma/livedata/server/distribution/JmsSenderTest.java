/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.livedata.LiveDataValueUpdateBeanFudgeBuilder;
import com.opengamma.transport.CollectingByteArrayMessageReceiver;
import com.opengamma.transport.jms.JmsByteArrayMessageDispatcher;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;
import com.opengamma.util.test.ActiveMQTestUtils;

/**
 * 
 */
@Test // PL 24.6.2010: Putting on ignore as it's failing intermittently on Bamboo and I can't figure out why
public class JmsSenderTest {
  
  private CollectingByteArrayMessageReceiver _collectingReceiver;
  private DefaultMessageListenerContainer _container;
  private MarketDataDistributor _mdd;
  private JmsSenderFactory _factory;

  @BeforeClass
  public void setUpClass() {
    ActiveMQConnectionFactory cf = ActiveMQTestUtils.createTestConnectionFactory();
    JmsConnectorFactoryBean jmsFactory= new JmsConnectorFactoryBean();
    jmsFactory.setName(getClass().getSimpleName());
    jmsFactory.setConnectionFactory(cf);
    JmsConnector jmsConnector = jmsFactory.createObject();
    
    _factory = new JmsSenderFactory(jmsConnector);
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
  public void tearDown() {
    _container.stop();
    _container.destroy();
  }

  @BeforeMethod
  public void setUp() {
    _collectingReceiver.clearMessages();
  }

  //-------------------------------------------------------------------------
  private void ensureStarted() {
    assertTrue(_container.isActive());
    assertTrue(_container.isRunning());
    assertEquals(1, _container.getActiveConsumerCount());
  }
  
  @Test(timeOut=30000, enabled = false)
  public void simpleScenario() throws Exception {
    ensureStarted();
    
    FudgeContext fudgeContext = OpenGammaFudgeContext.getInstance();
    MutableFudgeMsg msg = fudgeContext.newMessage();
    msg.add("name", "ruby");
    
    _mdd.distributeLiveData(msg);
    _mdd.distributeLiveData(FudgeContext.EMPTY_MESSAGE); // empty message not sent
    
    // allow data to flow through
    while (_collectingReceiver.getMessages().isEmpty()) {
      Thread.sleep(100);
    }
    Thread.sleep(100);
    assertEquals(1, _collectingReceiver.getMessages().size());
    
    FudgeDeserializer deserializer = new FudgeDeserializer(fudgeContext);
    for (byte[] byteArray : _collectingReceiver.getMessages()) {
      FudgeMsgEnvelope msgEnvelope = fudgeContext.deserialize(byteArray);
      LiveDataValueUpdateBean update = LiveDataValueUpdateBeanFudgeBuilder.fromFudgeMsg(deserializer, msgEnvelope.getMessage());
      assertEquals(msg, update.getFields());
    }
  }
  
  @Test(timeOut=30000, enabled = false)
  public void reconnectionScenario() throws Exception {
    ensureStarted();
    
    FudgeContext fudgeContext = OpenGammaFudgeContext.getInstance();
    MutableFudgeMsg msg1 = fudgeContext.newMessage();
    msg1.add("name", "olivia");
    
    MutableFudgeMsg msg2 = fudgeContext.newMessage();
    msg2.add("name", "ruby");
    
    MutableFudgeMsg msg3 = fudgeContext.newMessage();
    msg3.add("name", "sophie"); // will overwrite ruby
    msg3.add("address", "london");
    
    MutableFudgeMsg msg4 = fudgeContext.newMessage();
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
    
    FudgeDeserializer deserializer = new FudgeDeserializer(fudgeContext);
    for (int i = 0; i < _collectingReceiver.getMessages().size(); i++) {
      FudgeMsgEnvelope msgEnvelope = fudgeContext.deserialize(_collectingReceiver.getMessages().get(i));
      LiveDataValueUpdateBean update = LiveDataValueUpdateBeanFudgeBuilder.fromFudgeMsg(deserializer, msgEnvelope.getMessage());
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
