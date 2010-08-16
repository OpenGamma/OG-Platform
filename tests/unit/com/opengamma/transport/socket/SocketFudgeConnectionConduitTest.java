/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.junit.Test;

import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeConnectionReceiver;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageSender;

/**
 * Tests the SocketFudgeConnection and ServerSocketFudgeConnectionReceiver classes
 */
public class SocketFudgeConnectionConduitTest {
  
  private static class TestFudgeMessageReceiver implements FudgeMessageReceiver {

    private FudgeFieldContainer _message;

    @Override
    public synchronized void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
      assertNotNull(fudgeContext);
      assertNotNull(msgEnvelope);
      _message = msgEnvelope.getMessage();
      notify ();
    }
    
    public synchronized FudgeFieldContainer waitForMessage () throws InterruptedException {
      if (_message == null) {
        wait (1000L);
      }
      return _message;
    }

  }

  private final AtomicInteger _uid = new AtomicInteger();

  private FudgeFieldContainer createMessage() {
    final MutableFudgeFieldContainer message = FudgeContext.GLOBAL_DEFAULT.newMessage();
    message.add("uid", _uid.incrementAndGet());
    return message;
  }

  @Test
  public void simpleTest() throws Exception {
    final FudgeFieldContainer testMessage1 = createMessage();
    final FudgeFieldContainer testMessage2 = createMessage();
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
    final TestFudgeMessageReceiver clientReceiver = new TestFudgeMessageReceiver();
    client.setFudgeMessageReceiver(clientReceiver);
    client.getFudgeMessageSender().send(testMessage1);
    assertEquals(testMessage2, clientReceiver.waitForMessage ());
    client.stop();
    server.stop();
  }
  
  @Test
  public void messageReceiverTest() throws Exception {
    final FudgeFieldContainer testMessage1 = createMessage();
    final FudgeFieldContainer testMessage2 = createMessage();
    final FudgeFieldContainer testMessage3 = createMessage();
    final TestFudgeMessageReceiver message3Receiver = new TestFudgeMessageReceiver();
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
    assertNull(message3Receiver._message);
    client.getFudgeMessageSender().send(testMessage2);
    assertNull(message3Receiver._message);
    client.getFudgeMessageSender().send(testMessage3);
    assertEquals(testMessage3, message3Receiver.waitForMessage());
    server.stop();
    client.stop();
  }
  
  private class MessageReadWrite extends Thread implements FudgeMessageReceiver {

    private static final long TIMEOUT = 5000L;
    private static final int NUM_MESSAGES = 1000;

    private FudgeMessageSender _sender;
    private int _received;
    
    @Override
    public void run() {
      for (int i = 0; i < NUM_MESSAGES; i++) {
        final FudgeFieldContainer message = createMessage();
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
      final long timeout = System.currentTimeMillis() + TIMEOUT;
      while ((_received < NUM_MESSAGES) && (System.currentTimeMillis() < timeout)) {
        wait(TIMEOUT);
      }
      return _received == NUM_MESSAGES;
    }

  }

  @Test
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
  
}
