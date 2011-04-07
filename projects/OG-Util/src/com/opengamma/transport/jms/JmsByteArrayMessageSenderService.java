/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.opengamma.util.ArgumentChecker;

/**
 * A service to receive messages (byte arrays) and publish them to a
 * JMS destination asynchronously as {@code ExecutorService} jobs.
 */
public class JmsByteArrayMessageSenderService {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(JmsByteArrayMessageSenderService.class);

  /**
   * The executor in use.
   */
  // Is one thread best? Does the JMS layer allow concurrent publishing? Is an I/O bound task going to benefit from concurrency? 
  private static final ExecutorService s_executor = Executors.newSingleThreadExecutor();

  /**
   * The JMS template.
   */
  private final JmsTemplate _jmsTemplate;
  /**
   * The queued data.
   */
  private final ConcurrentMap<String, byte[]> _queuedData;

  /**
   * Creates an insatnce based on a JMS template.
   * 
   * @param jmsTemplate  the JMS template, not null
   */
  public JmsByteArrayMessageSenderService(final JmsTemplate jmsTemplate) {
    _jmsTemplate = jmsTemplate;
    _queuedData = new ConcurrentHashMap<String, byte[]>();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the JMS template.
   * 
   * @return the template, not null
   */
  protected JmsTemplate getJmsTemplate() {
    return _jmsTemplate;
  }

  /**
   * Gets the executor.
   * 
   * @return the executor, not null
   */
  protected ExecutorService getExecutor() {
    return s_executor;
  }

  /**
   * Gets the queued data.
   * 
   * @return the queued data, not null
   */
  protected ConcurrentMap<String, byte[]> getQueuedData() {
    return _queuedData;
  }

  //-------------------------------------------------------------------------
  /**
   * Posts the message.
   * 
   * @param destinationName  the JMS destination name, not null
   * @param message  the message as a byte array, not null
   */
  protected void postMessage(final String destinationName, final byte[] message) {
    ArgumentChecker.notNull(message, "message");
    s_logger.debug("posting message size {} to {}", message.length, destinationName);
    if (getQueuedData().put(destinationName, message) == null) {
      s_logger.debug("scheduling send on {}", destinationName);
      getExecutor().execute(new Runnable() {
        @Override
        public void run() {
          final byte[] message = getQueuedData().remove(destinationName);
          s_logger.debug("Sending message size {} to {}", message.length, destinationName);
          getJmsTemplate().send(destinationName, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
              final BytesMessage bytesMessage = session.createBytesMessage();
              bytesMessage.writeBytes(message);
              return bytesMessage;
            }
          });
        }
      });
    } else {
      // if there was data already in the map, then there's an unscheduled job too
      s_logger.debug("discarded previous message to {}", destinationName);
    }
  }

  /**
   * Gets the message sender instance.
   * <p>
   * The sender allows a byte array to be sent to the destination.
   * 
   * @param destinationName  the JMS destination name, not null
   * @return the sender instance, not null
   */
  public JmsByteArrayMessageSender getMessageSender(final String destinationName) {
    ArgumentChecker.notNull(destinationName, "destinationName");
    return new JmsByteArrayMessageSender(destinationName, getJmsTemplate()) {
      @Override
      public void send(final byte[] message) {
        postMessage(destinationName, message);
      }
    };
  }

}
