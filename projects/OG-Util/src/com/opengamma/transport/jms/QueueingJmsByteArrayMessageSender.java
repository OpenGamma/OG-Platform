/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

/**
 * A message sender that uses JMS. Messages are sent in the background, preserving their order.
 */
public class QueueingJmsByteArrayMessageSender extends JmsByteArrayMessageSender {

  private static final Logger s_logger = LoggerFactory.getLogger(JmsByteArrayMessageSender.class);
  
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
            sendSync(nextMessage);
          } catch (InterruptedException e) {
            s_logger.warn("Interrupted while waiting for next message to send", e);
            return;
          } catch (Exception e) {
            s_logger.error("Failed to send message asynchronously", e);
          }
        }
      }
    });
    _senderThread.setDaemon(true);
    _senderThread.start();
  }

  //-------------------------------------------------------------------------
  @Override
  public void send(byte[] message) {
    _messageQueue.add(message);
  }
  
  public void shutdown() {
    _senderThread.interrupt();
  }
  
  //-------------------------------------------------------------------------
  private void sendSync(byte[] message) {
    super.send(message);
  }
  
}
