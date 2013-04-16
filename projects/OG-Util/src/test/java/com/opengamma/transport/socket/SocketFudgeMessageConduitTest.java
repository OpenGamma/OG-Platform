/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.fail;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.transport.CollectingFudgeMessageReceiver;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION)
public class SocketFudgeMessageConduitTest {
  public void simpleTest() throws Exception {
    CollectingFudgeMessageReceiver collectingReceiver = new CollectingFudgeMessageReceiver();
    ServerSocketFudgeMessageReceiver socketReceiver = new ServerSocketFudgeMessageReceiver(collectingReceiver, FudgeContext.GLOBAL_DEFAULT);
    socketReceiver.start();
    
    SocketFudgeMessageSender sender = new SocketFudgeMessageSender();
    sender.setInetAddress(InetAddress.getLocalHost());
    sender.setPortNumber(socketReceiver.getPortNumber());
    
    MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("RATM", "Bombtrack");
    msg.add("You Know", "It's All Of That");
    sender.send(msg);
    
    msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("Anger", "is a gift");
    sender.send(msg);
    
    int nChecks = 0;
    while(collectingReceiver.getMessages().size() < 2) {
      Thread.sleep(100);
      nChecks++;
      if(nChecks > 20) {
        fail("Didn't receive messages in 2 seconds");
      }
    }
    
    FudgeMsgEnvelope envelope = null;
    envelope = collectingReceiver.getMessages().get(0);
    assertNotNull(envelope);
    assertNotNull(envelope.getMessage());
    assertEquals("Bombtrack", envelope.getMessage().getString("RATM"));
    assertEquals("It's All Of That", envelope.getMessage().getString("You Know"));
    assertEquals(2, envelope.getMessage().getNumFields());

    envelope = collectingReceiver.getMessages().get(1);
    assertNotNull(envelope);
    assertNotNull(envelope.getMessage());
    assertEquals("is a gift", envelope.getMessage().getString("Anger"));
    assertEquals(1, envelope.getMessage().getNumFields());
    
    sender.stop();
    socketReceiver.stop();
  }

  private void parallelSendTest(final ExecutorService executor, final AtomicInteger maxConcurrency) throws Exception {
    final CollectingFudgeMessageReceiver receiver = new CollectingFudgeMessageReceiver() {
      private final AtomicInteger _concurrency = new AtomicInteger(0);
      @Override
      public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
        final int concurrency = _concurrency.incrementAndGet();
        if (concurrency > maxConcurrency.get()) {
          maxConcurrency.set(concurrency);
        }
        try {
          Thread.sleep (1000);
        } catch (InterruptedException e) {
        }
        _concurrency.decrementAndGet();
        super.messageReceived(fudgeContext, msgEnvelope);
      }
    };
    final ServerSocketFudgeMessageReceiver server = (executor != null) ? new ServerSocketFudgeMessageReceiver(receiver, FudgeContext.GLOBAL_DEFAULT, executor)
        : new ServerSocketFudgeMessageReceiver(receiver, FudgeContext.GLOBAL_DEFAULT);
    server.start();
    final SocketFudgeMessageSender sender = new SocketFudgeMessageSender();
    sender.setInetAddress(InetAddress.getLocalHost());
    sender.setPortNumber(server.getPortNumber());
    sender.send(FudgeContext.EMPTY_MESSAGE);
    sender.send(FudgeContext.EMPTY_MESSAGE);
    assertNotNull (receiver.waitForMessage(2000));
    assertNotNull (receiver.waitForMessage(2000));
  }

  public void parallelSendTest_single() throws Exception {
    for (int retry = 0; retry < 3; retry++) {
      try {
        final AtomicInteger concurrencyMax = new AtomicInteger(0);
        parallelSendTest(null, concurrencyMax);
        assertEquals(1, concurrencyMax.get());
        break; // success
      } catch (AssertionError ex) {
        continue;
      }
    }
  }

  public void parallelSendTest_multi() throws Exception {
    for (int retry = 0; retry < 3; retry++) {
      try {
        final AtomicInteger concurrencyMax = new AtomicInteger(0);
        parallelSendTest(Executors.newCachedThreadPool(), concurrencyMax);
        assertEquals(2, concurrencyMax.get());
        break; // success
      } catch (AssertionError ex) {
        continue;
      }
    }
  }

}
