/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
 * A service to receive messages (byte arrays) and publish them to a JMS destination asynchronously
 * as ExecutorService jobs.
 */
public class JmsByteArrayMessageSenderService {

  private static final Logger s_logger = LoggerFactory.getLogger(JmsByteArrayMessageSenderService.class);

  // Is one thread best? Does the JMS layer allow concurrent publishing? Is an I/O bound task going to benefit from concurrency? 
  private static final ExecutorService s_executor = Executors.newSingleThreadExecutor();

  private final JmsTemplate _jmsTemplate;
  private final ConcurrentMap<String, byte[]> _queuedData;

  public JmsByteArrayMessageSenderService(final JmsTemplate jmsTemplate) {
    _jmsTemplate = jmsTemplate;
    _queuedData = new ConcurrentHashMap<String, byte[]>();
  }

  protected JmsTemplate getJmsTemplate() {
    return _jmsTemplate;
  }

  protected ExecutorService getExecutor() {
    return s_executor;
  }

  protected ConcurrentMap<String, byte[]> getQueuedData() {
    return _queuedData;
  }

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
              BytesMessage bytesMessage = session.createBytesMessage();
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
