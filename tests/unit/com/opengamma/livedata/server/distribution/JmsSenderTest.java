/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import static org.junit.Assert.assertEquals;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.transport.CollectingByteArrayMessageReceiver;
import com.opengamma.transport.jms.ActiveMQTestUtil;
import com.opengamma.transport.jms.JmsByteArrayMessageDispatcher;

/**
 * 
 *
 * @author pietari
 */
public class JmsSenderTest {
  
  private CollectingByteArrayMessageReceiver _collectingReceiver;
  private DefaultMessageListenerContainer _container;
  private MarketDataDistributor _mdd;
  private JmsSenderFactory _factory;
  
  @Before
  public void setUp() {
    ActiveMQConnectionFactory cf = ActiveMQTestUtil.createTestConnectionFactory();
    cf.setUseRetroactiveConsumer(true);

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
  
  @After
  public void tearDown() {
    _container.stop();
    _container.destroy();
  }
  
  @Test
  public void simpleScenario() throws Exception {
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("name", "ruby");
    
    _mdd.distributeLiveData(msg);
    _mdd.distributeLiveData(FudgeContext.EMPTY_MESSAGE); // empty message not sent
    
    Thread.sleep(100); // allow data to flow through
    
    assertEquals(1, _collectingReceiver.getMessages().size());
    
    for (byte[] byteArray : _collectingReceiver.getMessages()) {
      FudgeMsgEnvelope msgEnvelope = FudgeContext.GLOBAL_DEFAULT.deserialize(byteArray);
      LiveDataValueUpdateBean update = LiveDataValueUpdateBean.fromFudgeMsg(msgEnvelope.getMessage());
      assertEquals(msg, update.getFields());
    }
  }
  
  @Test
  public void reconnectionScenario() throws Exception {
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
    
    Thread.sleep(100); // allow data to flow through

    assertEquals(3, _collectingReceiver.getMessages().size());
    LiveDataValueUpdateBean[] updates = new LiveDataValueUpdateBean[3]; 
    
    for (int i = 0; i < _collectingReceiver.getMessages().size(); i++) {
      FudgeMsgEnvelope msgEnvelope = FudgeContext.GLOBAL_DEFAULT.deserialize(_collectingReceiver.getMessages().get(i));
      LiveDataValueUpdateBean update = LiveDataValueUpdateBean.fromFudgeMsg(msgEnvelope.getMessage());
      updates[i] = update;
    }
    
    assertEquals(msg1, updates[0].getFields());
    assertEquals(0, updates[0].getSequenceNumber());
    
    assertEquals(msg3, updates[1].getFields());
    assertEquals(2, updates[1].getSequenceNumber());
    
    assertEquals(msg4, updates[2].getFields());
    assertEquals(3, updates[2].getSequenceNumber());
  }

}
