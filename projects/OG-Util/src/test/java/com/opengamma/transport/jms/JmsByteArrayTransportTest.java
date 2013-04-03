/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Random;

import javax.jms.ConnectionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.testng.annotations.Test;

import com.opengamma.transport.ByteArrayRequestReceiver;
import com.opengamma.transport.CollectingByteArrayMessageReceiver;
import com.opengamma.util.test.ActiveMQTestUtils;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.Timeout;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION)
public class JmsByteArrayTransportTest {

  private static final Logger s_logger = LoggerFactory.getLogger(JmsByteArrayTransportTest.class);

  private static final long TIMEOUT = 10L * Timeout.standardTimeoutMillis();

  @Test(invocationCount = 5, successPercentage = 19)
  public void topicConduit() throws Exception {
    String topicName = "JmsByteArrayTransportTest-topicConduit-" + System.getProperty("user.name") + "-" + System.currentTimeMillis();
    ConnectionFactory cf = ActiveMQTestUtils.createTestConnectionFactory();
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
    //TODO: this is a hack.  The context doesn't seem to have always set up the consumer completely yet
    Thread.sleep(500l);
    
    messageSender.send(randomBytes);
    long startTime = System.currentTimeMillis();
    while(collectingReceiver.getMessages().isEmpty()) {
      Thread.sleep(10l);
      if ((System.currentTimeMillis() - startTime) > TIMEOUT) {
        fail("Did not receive a message in " + (TIMEOUT / 1000) + " seconds.");
      }
    }
    s_logger.debug ("topicConduit message received {}ms before timeout limit", TIMEOUT - (System.currentTimeMillis () - startTime));
    assertEquals(1, collectingReceiver.getMessages().size());
    byte[] receivedBytes = collectingReceiver.getMessages().get(0);
    assertEquals(randomBytes.length, receivedBytes.length);
    for(int i = 0; i < randomBytes.length; i++) {
      assertEquals(randomBytes[i], receivedBytes[i]);
    }
    
    container.stop();
    container.destroy();
  }

  @Test(invocationCount = 5, successPercentage = 19)
  public void requestConduit() throws Exception {
    String topicName = "JmsByteArrayTransportTest-requestConduit-" + System.getProperty("user.name") + "-" + System.currentTimeMillis();
    ConnectionFactory cf = ActiveMQTestUtils.createTestConnectionFactory();
    JmsTemplate jmsTemplate = new JmsTemplate();
    jmsTemplate.setConnectionFactory(cf);
    jmsTemplate.setPubSubDomain(true);
    jmsTemplate.setReceiveTimeout(5000l);

    final Random random = new Random();
    final byte[] responseBytes = new byte[512];
    random.nextBytes(responseBytes);
    
    JmsByteArrayRequestSender requestSender = new JmsByteArrayRequestSender(topicName, jmsTemplate);
    JmsByteArrayRequestDispatcher requestDispatcher = new JmsByteArrayRequestDispatcher(new ByteArrayRequestReceiver() {
      @Override
      public byte[] requestReceived(byte[] message) {
        return responseBytes;
      }
    });
    
    DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
    container.setConnectionFactory(cf);
    container.setMessageListener(requestDispatcher);
    container.setDestinationName(topicName);
    container.setPubSubDomain(true);
    container.afterPropertiesSet();
    container.start();

    byte[] randomBytes = new byte[1024];
    random.nextBytes(randomBytes);
    
    while(!container.isRunning()) {
      Thread.sleep(10l);
    }
    
    CollectingByteArrayMessageReceiver collectingReceiver = new CollectingByteArrayMessageReceiver();
    requestSender.sendRequest(randomBytes, collectingReceiver);
    long startTime = System.currentTimeMillis();
    while(collectingReceiver.getMessages().isEmpty()) {
      Thread.sleep(10l);
      if ((System.currentTimeMillis() - startTime) > TIMEOUT) {
        fail("Did not receive a response in " + (TIMEOUT / 1000) + " seconds.");
      }
    }
    s_logger.debug ("requestConduit message received {}ms before timeout limit", TIMEOUT - (System.currentTimeMillis () - startTime));
    assertEquals(1, collectingReceiver.getMessages().size());
    byte[] receivedBytes = collectingReceiver.getMessages().get(0);
    assertEquals(responseBytes.length, receivedBytes.length);
    for(int i = 0; i < responseBytes.length; i++) {
      assertEquals(responseBytes[i], receivedBytes[i]);
    }
    
    container.stop();
    container.destroy();
  }
}
