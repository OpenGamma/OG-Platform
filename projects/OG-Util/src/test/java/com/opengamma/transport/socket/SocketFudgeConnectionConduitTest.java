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
import org.testng.annotations.Test;

import com.opengamma.transport.CollectingFudgeMessageReceiver;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeConnectionReceiver;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.Timeout;

/**
 * Tests the SocketFudgeConnection and ServerSocketFudgeConnectionReceiver classes
 */
@Test(groups = TestGroup.INTEGRATION, singleThreaded = true)
public class SocketFudgeConnectionConduitTest {
  
  private final AtomicInteger _counter = new AtomicInteger();

  private FudgeMsg createMessage() {
    final MutableFudgeMsg message = FudgeContext.GLOBAL_DEFAULT.newMessage();
    message.add("counter", _counter.incrementAndGet());
    return message;
  }

  public void simpleTest() throws Exception {
    final FudgeMsg testMessage1 = createMessage();
    final FudgeMsg testMessage2 = createMessage();
    // receiver will respond to testMessage1 with testMessage2
    final FudgeConnectionReceiver serverReceiver = new FudgeConnectionReceiver() {
      @Override
      public void connectionReceived(FudgeContext fudgeContext, FudgeMsgEnvelope message, FudgeConnection connection) {
        assertNotNull(fudgeContext);
        assertNotNull(message);
        assertNotNull(connection);
        assertEquals(testMessage1, message.getMessage());
        connection.getFudgeMessageSender().send(testMessage2);
      }
    };
    final ServerSocketFudgeConnectionReceiver server = new ServerSocketFudgeConnectionReceiver(FudgeContext.GLOBAL_DEFAULT, serverReceiver);
    server.start();
    final SocketFudgeConnection client = new SocketFudgeConnection(FudgeContext.GLOBAL_DEFAULT);
    client.setInetAddress(InetAddress.getLocalHost());
    client.setPortNumber(server.getPortNumber());
    // connect and send testMessage1, then verify that testMessage2 was sent back
    final CollectingFudgeMessageReceiver clientReceiver = new CollectingFudgeMessageReceiver();
    client.setFudgeMessageReceiver(clientReceiver);
    client.getFudgeMessageSender().send(testMessage1);
    final FudgeMsgEnvelope envelope = clientReceiver.waitForMessage(Timeout.standardTimeoutMillis());
    assertNotNull(envelope);
    assertEquals(testMessage2, envelope.getMessage());
    client.stop();
    server.stop();
  }
  
  public void messageReceiverTest() throws Exception {
    final FudgeMsg testMessage1 = createMessage();
    final FudgeMsg testMessage2 = createMessage();
    final FudgeMsg testMessage3 = createMessage();
    final CollectingFudgeMessageReceiver message3Receiver = new CollectingFudgeMessageReceiver();
    // receiver will ignore testMessage1
    // after receiving testMessage2, will set the message receiver on the connection
    // it shouldn't be called again - messages should be dispatched to the connection's receiver
    final FudgeConnectionReceiver serverReceiver = new FudgeConnectionReceiver() {
      private int _count;
      @Override
      public void connectionReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope message, final FudgeConnection connection) {
        assertNotNull(fudgeContext);
        assertNotNull(message);
        assertNotNull(connection);
        switch (_count++) {
          case 0:
            assertEquals(testMessage1, message.getMessage());
            break;
          case 1:
            assertEquals(testMessage2, message.getMessage());
            connection.setFudgeMessageReceiver(message3Receiver);
            break;
          default:
            fail("Shouldn't have been called a third time");
            break;
        }
      }
    };
    final ServerSocketFudgeConnectionReceiver server = new ServerSocketFudgeConnectionReceiver(FudgeContext.GLOBAL_DEFAULT, serverReceiver);
    server.start();
    final SocketFudgeConnection client = new SocketFudgeConnection(FudgeContext.GLOBAL_DEFAULT);
    client.setInetAddress (InetAddress.getLocalHost ());
    client.setPortNumber (server.getPortNumber ());
    // send messages 1, 2, 3 and verify 3 went to the test receiver
    client.getFudgeMessageSender().send(testMessage1);
    assertTrue(message3Receiver.getMessages().isEmpty());
    client.getFudgeMessageSender().send(testMessage2);
    assertTrue(message3Receiver.getMessages().isEmpty());
    client.getFudgeMessageSender().send(testMessage3);
    final FudgeMsgEnvelope envelope = message3Receiver.waitForMessage(Timeout.standardTimeoutMillis());
    assertNotNull(envelope);
    assertEquals(testMessage3, envelope.getMessage());
    server.stop();
    client.stop();
  }
  
  private class MessageReadWrite extends Thread implements FudgeMessageReceiver {

    private static final int NUM_MESSAGES = 1000;

    private FudgeMessageSender _sender;
    private int _received;
    
    @Override
    public void run() {
      for (int i = 0; i < NUM_MESSAGES; i++) {
        final FudgeMsg message = createMessage();
        _sender.send(message);
      }
    }

    @Override
    public synchronized void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
      _received++;
      if (_received == NUM_MESSAGES) {
        notify();
      } else if (_received > NUM_MESSAGES) {
        fail("Too many messages received");
      }
    }

    public synchronized boolean waitForMessages() throws InterruptedException {
      final long period = Timeout.standardTimeoutMillis();
      final long timeout = System.currentTimeMillis() + period;
      while ((_received < NUM_MESSAGES) && (System.currentTimeMillis() < timeout)) {
        wait(period);
      }
      return _received == NUM_MESSAGES;
    }

  }

  @Test(invocationCount = 5, successPercentage = 19)
  public void parallelIOTest() throws Exception {
    final MessageReadWrite serverThread = new MessageReadWrite();
    // receiver will attach the serverThread to the connection and start the thread
    final FudgeConnectionReceiver serverReceiver = new FudgeConnectionReceiver() {
      @Override
      public void connectionReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope envelope, final FudgeConnection connection) {
        // pass on the first message
        serverThread.messageReceived(fudgeContext, envelope);
        // and let it receive all others as they arrive
        serverThread._sender = connection.getFudgeMessageSender();
        connection.setFudgeMessageReceiver(serverThread);
        serverThread.start();
      }
    };
    final ServerSocketFudgeConnectionReceiver server = new ServerSocketFudgeConnectionReceiver(FudgeContext.GLOBAL_DEFAULT, serverReceiver);
    server.start();
    final SocketFudgeConnection client = new SocketFudgeConnection(FudgeContext.GLOBAL_DEFAULT);
    client.setInetAddress(InetAddress.getLocalHost());
    client.setPortNumber(server.getPortNumber());
    // client thread will send a stream of messages, and receive those back from the server
    final MessageReadWrite clientThread = new MessageReadWrite();
    clientThread._sender = client.getFudgeMessageSender();
    client.setFudgeMessageReceiver(clientThread);
    clientThread.start();
    // wait to see if both have behaved
    assertTrue(serverThread.waitForMessages());
    assertTrue(clientThread.waitForMessages());
    server.stop();
    client.stop();
  }
  
  private int[] parallelSendTest(final ExecutorService executorClient, final ExecutorService executorServer, final AtomicInteger concurrencyMax) throws Exception {
    final FudgeConnectionReceiver serverReceiver = new FudgeConnectionReceiver() {
      @Override
      public void connectionReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope envelope, final FudgeConnection connection) {
        connection.setFudgeMessageReceiver(new FudgeMessageReceiver() {
          @Override
          public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
            MutableFudgeMsg message = fudgeContext.newMessage();
            message.add("foo", 1);
            connection.getFudgeMessageSender().send(message);
            try {
              Thread.sleep(Timeout.standardTimeoutMillis());
            } catch (InterruptedException e) {
            }
            message = fudgeContext.newMessage();
            message.add("foo", 2);
            connection.getFudgeMessageSender().send(message);
          }
        });
      }
    };
    final ServerSocketFudgeConnectionReceiver server = (executorServer != null) ? new ServerSocketFudgeConnectionReceiver(FudgeContext.GLOBAL_DEFAULT, serverReceiver, executorServer)
        : new ServerSocketFudgeConnectionReceiver(FudgeContext.GLOBAL_DEFAULT, serverReceiver);
    server.start();
    final SocketFudgeConnection client = (executorClient != null) ? new SocketFudgeConnection(FudgeContext.GLOBAL_DEFAULT, executorClient) : new SocketFudgeConnection(FudgeContext.GLOBAL_DEFAULT);
    client.setInetAddress(InetAddress.getLocalHost());
    client.setPortNumber(server.getPortNumber());
    final CollectingFudgeMessageReceiver responses = new CollectingFudgeMessageReceiver () {
      private final AtomicInteger _concurrency = new AtomicInteger (0);
      @Override
      public void messageReceived (final FudgeContext fudgeContext, final FudgeMsgEnvelope envelope) {
        final int concurrency = _concurrency.incrementAndGet ();
        if (concurrency > concurrencyMax.get ()) {
          concurrencyMax.set (concurrency);
        }
        try {
          Thread.sleep(Timeout.standardTimeoutMillis() / 2L);
        } catch (InterruptedException e) {
        }
        _concurrency.decrementAndGet ();
        super.messageReceived (fudgeContext, envelope);
      }
    };
    client.setFudgeMessageReceiver(responses);
    client.getFudgeMessageSender().send(FudgeContext.EMPTY_MESSAGE);
    client.getFudgeMessageSender().send(FudgeContext.EMPTY_MESSAGE);
    client.getFudgeMessageSender().send(FudgeContext.EMPTY_MESSAGE);
    final int[] result = new int[4];
    for (int i = 0; i < 4; i++) {
      final FudgeMsgEnvelope envelope = responses.waitForMessage(Timeout.standardTimeoutMillis() * 2L);
      assertNotNull (envelope);
      result[i] = envelope.getMessage().getInt("foo");
    }
    return result;
  }

  public void parallelSendTest_single_single() throws Exception {
    final AtomicInteger concurrencyMax = new AtomicInteger(0);
    final int[] result = parallelSendTest(null, null, concurrencyMax);
    assertEquals(1, concurrencyMax.get());
    assertEquals(1, result[0]);
    assertEquals(2, result[1]);
    assertEquals(1, result[2]);
    assertEquals(2, result[3]);
  }

  public void parallelSendTest_multi_single() throws Exception {
    final AtomicInteger concurrencyMax = new AtomicInteger(0);
    final int[] result = parallelSendTest(Executors.newCachedThreadPool(), null, concurrencyMax);
    assertEquals(2, concurrencyMax.get());
    assertEquals(1, result[0]);
    // The server might send the messages in order, but the client can receive them out of order
    if (result[1] == 2) {
      assertEquals(1, result[2]);
    } else if (result[1] == 1) {
      assertEquals(2, result[2]);
    } else {
      fail();
    }
    assertEquals(2, result[3]);
  }

  public void parallelSendTest_single_multi() throws Exception {
    final AtomicInteger concurrencyMax = new AtomicInteger(0);
    final int[] result = parallelSendTest(null, Executors.newCachedThreadPool(), concurrencyMax);
    assertEquals(1, concurrencyMax.get());
    assertEquals(1, result[0]);
    assertEquals(1, result[1]);
    assertEquals(2, result[2]);
    assertEquals(2, result[3]);
  }

  public void parallelSendTest_multi_multi() throws Exception {
    final AtomicInteger concurrencyMax = new AtomicInteger(0);
    final int[] result = parallelSendTest(Executors.newCachedThreadPool(), Executors.newCachedThreadPool(), concurrencyMax);
    assertEquals(2, concurrencyMax.get());
    assertEquals(1, result[0]);
    assertEquals(1, result[1]);
    assertEquals(2, result[2]);
    assertEquals(2, result[3]);
  }

}
