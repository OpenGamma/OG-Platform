/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.testng.annotations.Test;

import com.opengamma.transport.CollectingFudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestReceiver;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION)
public class SocketFudgeRequestConduitTest {

  @Test(invocationCount = 5, successPercentage = 19)
  public void simpleTest() throws Exception {
    CollectingFudgeMessageReceiver collectingReceiver = new CollectingFudgeMessageReceiver();
    FudgeRequestReceiver requestReceiver = new FudgeRequestReceiver() {
      @Override
      public FudgeMsg requestReceived(FudgeDeserializer deserializer, FudgeMsgEnvelope requestEnvelope) {
        MutableFudgeMsg response = deserializer.getFudgeContext().newMessage();
        response.add("TheTime", System.nanoTime());
        return response;
      }
    };
    ServerSocketFudgeRequestDispatcher requestDispatcher = new ServerSocketFudgeRequestDispatcher(requestReceiver, FudgeContext.GLOBAL_DEFAULT);
    requestDispatcher.start();
    
    SocketFudgeRequestSender sender = new SocketFudgeRequestSender();
    sender.setInetAddress(InetAddress.getLocalHost());
    sender.setPortNumber(requestDispatcher.getPortNumber());
    
    MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("RATM", "Bombtrack");
    msg.add("You Know", "It's All Of That");
    sender.sendRequest(msg, collectingReceiver);
    
    msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("Anger", "is a gift");
    sender.sendRequest(msg, collectingReceiver);
    
    int nChecks = 0;
    while (collectingReceiver.getMessages().size() < 2) {
      Thread.sleep(100);
      nChecks++;
      if (nChecks > 20) {
        fail("Didn't receive messages in 2 seconds");
      }
    }
    
    FudgeMsgEnvelope envelope = null;
    envelope = collectingReceiver.getMessages().get(0);
    assertNotNull(envelope);
    assertNotNull(envelope.getMessage());
    Long firstTimestamp = envelope.getMessage().getLong("TheTime");
    assertNotNull(firstTimestamp);
    assertEquals(1, envelope.getMessage().getNumFields());

    envelope = collectingReceiver.getMessages().get(1);
    assertNotNull(envelope);
    assertNotNull(envelope.getMessage());
    Long secondTimestamp = envelope.getMessage().getLong("TheTime");
    assertNotNull(firstTimestamp);
    assertEquals(1, envelope.getMessage().getNumFields());
    
    assertTrue(secondTimestamp > firstTimestamp);
    
    sender.stop();
    requestDispatcher.stop();
  }

  //-------------------------------------------------------------------------
  private void parallelSendTest(final ExecutorService executor, final AtomicInteger maxConcurrency) throws Exception {
    final CollectingFudgeMessageReceiver collectingReceiver = new CollectingFudgeMessageReceiver();
    FudgeRequestReceiver requestReceiver = new FudgeRequestReceiver() {
      private final AtomicInteger _concurrency = new AtomicInteger(0);

      @Override
      public FudgeMsg requestReceived(FudgeDeserializer deserializer, FudgeMsgEnvelope requestEnvelope) {
        final int concurrency = _concurrency.incrementAndGet();
        if (concurrency > maxConcurrency.get()) {
          maxConcurrency.set(concurrency);
        }
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        _concurrency.decrementAndGet();
        return requestEnvelope.getMessage();
      }
    };
    ServerSocketFudgeRequestDispatcher requestDispatcher = (executor != null) ? new ServerSocketFudgeRequestDispatcher(requestReceiver, FudgeContext.GLOBAL_DEFAULT, executor)
        : new ServerSocketFudgeRequestDispatcher(requestReceiver, FudgeContext.GLOBAL_DEFAULT);
    requestDispatcher.start();

    final SocketFudgeRequestSender sender = new SocketFudgeRequestSender();
    sender.setInetAddress(InetAddress.getLocalHost());
    sender.setPortNumber(requestDispatcher.getPortNumber());
    for (int i = 0; i < 2; i++) {
      new Thread() {
        @Override
        public void run() {
          sender.sendRequest(FudgeContext.EMPTY_MESSAGE, collectingReceiver);
        }
      }.start();
    }
    assertNotNull("Message should be received in 4s timeout", collectingReceiver.waitForMessage(4000L));
    assertNotNull("Message should be received in 4s timeout", collectingReceiver.waitForMessage(4000L));
  }

  @Test(invocationCount = 5, successPercentage = 19)
  public void parallelSendTest_single() throws Exception {
    final AtomicInteger concurrencyMax = new AtomicInteger(0);
    parallelSendTest(null, concurrencyMax);
    assertEquals(1, concurrencyMax.get());
  }

  @Test(invocationCount = 5, successPercentage = 19)
  public void parallelSendTest_multi() throws Exception {
    final AtomicInteger concurrencyMax = new AtomicInteger(0);
    parallelSendTest(Executors.newCachedThreadPool(), concurrencyMax);
    assertEquals(2, concurrencyMax.get());
  }

}
