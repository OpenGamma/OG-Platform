/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jms.ConnectionFactory;

import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.testng.annotations.Test;

import com.opengamma.transport.BatchByteArrayMessageReceiver;
import com.opengamma.util.test.ActiveMQTestUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION)
public class JmsBatchMessageDispatcherTest {

  @Test(invocationCount = 5, successPercentage = 19)
  public void queueOperation() throws InterruptedException {
    String queueName = "JmsBatchMessageDispatcherTest-queueOperation-" + System.getProperty("user.name") + "-" + System.currentTimeMillis();
    ConnectionFactory cf = new PooledConnectionFactory(ActiveMQTestUtils.createTestConnectionFactory());
    
    JmsTemplate sendingTemplate = new JmsTemplate();
    sendingTemplate.setConnectionFactory(cf);
    sendingTemplate.setPubSubDomain(false);
    sendingTemplate.setDefaultDestinationName(queueName);
    sendingTemplate.afterPropertiesSet();
    
    JmsTemplate receivingTemplate = new JmsTemplate();
    receivingTemplate.setConnectionFactory(cf);
    receivingTemplate.setPubSubDomain(false);
    receivingTemplate.setDefaultDestinationName(queueName);
    receivingTemplate.afterPropertiesSet();
    
    JmsByteArrayMessageSender messageSender = new JmsByteArrayMessageSender(queueName, sendingTemplate);
    
    JmsBatchMessageDispatcher dispatcher = new JmsBatchMessageDispatcher(receivingTemplate);
    final List<Integer> batchSizes = Collections.synchronizedList(new ArrayList<Integer>());
    BatchByteArrayMessageReceiver receiver = new BatchByteArrayMessageReceiver() {
      @Override
      public void messagesReceived(List<byte[]> messages) {
        batchSizes.add(messages.size());
      }
    };
    dispatcher.addReceiver(receiver);
    
    // First we put messages into the queue
    messageSender.send(new byte[10]);
    messageSender.send(new byte[10]);
    messageSender.send(new byte[10]);
    messageSender.send(new byte[10]);
    messageSender.send(new byte[10]);
    
    // Add in a delay. 
    Thread.sleep(1000l);

    // Now start the dispatcher.
    dispatcher.start();
    
    assertBatchSize(batchSizes, 5);
    
    batchSizes.clear();
    messageSender.send(new byte[20]);
    assertBatchSize(batchSizes, 1);
    
    dispatcher.stop();
  }
  
  private static void assertBatchSize(List<Integer> batchSizes, int totalSize) throws InterruptedException {
    int actualTotal = 0;
    long startTime = System.currentTimeMillis();
    while(actualTotal < totalSize) {
      while(batchSizes.isEmpty()) {
        Thread.sleep(100);
        if ((System.currentTimeMillis() - startTime) > 5000l) {
          fail("Did not receive a batch in 5 seconds.");
        }
      }
      synchronized(batchSizes) {
        for(Integer batchSize : batchSizes) {
          actualTotal += batchSize;
        }
        batchSizes.clear();
      }
      
      if ((System.currentTimeMillis() - startTime) > 5000l) {
        fail("Did not receive expected total batches in 5 seconds.");
      }
    }
    assertEquals(actualTotal, totalSize);
  }

}
