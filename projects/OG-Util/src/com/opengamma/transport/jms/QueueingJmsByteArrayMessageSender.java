/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import com.opengamma.util.ArgumentChecker;

/**
 * A message sender that uses JMS. Messages are sent in the background, preserving their order.
 */
public class QueueingJmsByteArrayMessageSender extends JmsByteArrayMessageSender {

  private static final Logger s_logger = LoggerFactory.getLogger(JmsByteArrayMessageSender.class);
  
  private static final byte[] s_shutdownSentinel = new byte[0];

  private final AtomicBoolean _isShutdown = new AtomicBoolean(false);
  private final BlockingQueue<byte[]> _messageQueue = new LinkedBlockingQueue<byte[]>();
  private final Thread _senderThread;

  /**
   * Creates an instance associated with a destination and template.
   * 
   * @param destinationName  the destination name, not null
   * @param jmsTemplate  the template, not null
   */
  public QueueingJmsByteArrayMessageSender(final String destinationName, final JmsTemplate jmsTemplate) {
    super(destinationName, jmsTemplate);
    
    _senderThread = new Thread(new Runnable() {
      @Override
      public void run() {
        while (true) {
          byte[] nextMessage;
          try {
            nextMessage = _messageQueue.take();
            if (_isShutdown.get()) {
              break;
            }
            sendSync(nextMessage);
          } catch (Exception e) {
            s_logger.error("Failed to send message asynchronously", e);
          }
        }
      }
    }, String.format("QueueingJmsByteArrayMessageSender %s", destinationName));
    _senderThread.setDaemon(true);
    _senderThread.start();
  }

  //-------------------------------------------------------------------------
  @Override
  public void send(byte[] message) {
    _messageQueue.add(message);
  }
  
  public void shutdown() {
    //NOTE: PLAT-1236 ActiveMQ doesn't like us interrupting the thread.
    _isShutdown.set(true);
    _messageQueue.add(s_shutdownSentinel); // Make sure that the take doesn't block 
  }
  
  //-------------------------------------------------------------------------
  private void sendSync(byte[] message) {
    ArgumentChecker.notNull(message, "message");
    super.send(message);
  }
  
}
