/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Random;

import javax.jms.ConnectionFactory;

import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.opengamma.transport.CollectingByteArrayMessageReceiver;

/**
 * 
 *
 * @author kirk
 */
public class JmsByteArrayTransportTest {

  @Test
  public void topicConduit() throws Exception {
    String topicName = "JmsByteArrayTransportTest-topicConduit-" + System.getProperty("user.name") + "-" + System.currentTimeMillis();
    ConnectionFactory cf = ActiveMQTestUtil.createTestConnectionFactory();
    JmsTemplate jmsTemplate = new JmsTemplate();
    jmsTemplate.setConnectionFactory(cf);
    jmsTemplate.setPubSubDomain(true);
    
    JmsByteArrayMessageSender messageSender = new JmsByteArrayMessageSender(topicName, jmsTemplate);
    CollectingByteArrayMessageReceiver collectingReceiver = new CollectingByteArrayMessageReceiver();
    JmsByteArrayMessageDispatcher messageDispatcher = new JmsByteArrayMessageDispatcher(collectingReceiver);
    
    DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
    container.setConnectionFactory(cf);
    container.setMessageListener(messageDispatcher);
    container.setDestinationName(topicName);
    container.setPubSubDomain(true);
    container.afterPropertiesSet();
    container.start();

    Random random = new Random();
    byte[] randomBytes = new byte[1024];
    random.nextBytes(randomBytes);
    
    while(!container.isRunning()) {
      Thread.sleep(10l);
    }
    
    messageSender.send(randomBytes);
    long startTime = System.currentTimeMillis();
    while(collectingReceiver.getMessages().isEmpty()) {
      Thread.sleep(10l);
      if((System.currentTimeMillis() - startTime) > 5000l) {
        fail("Did not receive a message in 5 seconds.");
      }
    }
    assertEquals(1, collectingReceiver.getMessages().size());
    byte[] receivedBytes = collectingReceiver.getMessages().get(0);
    assertEquals(randomBytes.length, receivedBytes.length);
    for(int i = 0; i < randomBytes.length; i++) {
      assertEquals(randomBytes[i], receivedBytes[i]);
    }
    
    container.stop();
    container.destroy();
  }
}
