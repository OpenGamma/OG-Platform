/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryTopic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;

import com.opengamma.transport.ByteArrayMessageReceiver;
import com.opengamma.transport.ByteArrayRequestSender;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.NamedThreadPoolFactory;

/**
 * An RPC message sender/receiver that uses JMS.
 * <p>
 * This is a simple implementation based on JMS.
 */
public class JmsByteArrayRequestSender extends AbstractJmsByteArraySender implements ByteArrayRequestSender {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(JmsByteArrayRequestSender.class);

  /**
   * The executor service.
   */
  private final ExecutorService _executor;

  /**
   * Creates an instance associated with a destination and template.
   * 
   * @param destinationName the destination name, not null
   * @param jmsTemplate the template, not null
   */
  public JmsByteArrayRequestSender(final String destinationName, final JmsTemplate jmsTemplate) {
    this(destinationName, jmsTemplate, Executors.newCachedThreadPool(new NamedThreadPoolFactory("JMS-request")));
  }

  /**
   * Creates an instance associated with a destination and template, specifying the executor to use.
   * 
   * @param destinationName the destination name, not null
   * @param jmsTemplate the template, not null
   * @param executor the executor, not null
   */
  public JmsByteArrayRequestSender(final String destinationName, final JmsTemplate jmsTemplate, final ExecutorService executor) {
    super(destinationName, jmsTemplate);
    ArgumentChecker.notNull(executor, "executor");
    _executor = executor;
  }

  //-------------------------------------------------------------------------
  @Override
  public void sendRequest(final byte[] request, final ByteArrayMessageReceiver responseReceiver) {
    s_logger.debug("Dispatching request of size {} to destination {}", request.length, getDestinationName());
    _executor.execute(new Runnable() {
      @Override
      public void run() {
        getJmsTemplate().execute(new SessionCallback<Object>() {
          @Override
          public Object doInJms(Session session) throws JMSException {
            try {
              final TemporaryTopic tempTopic = session.createTemporaryTopic();
              s_logger.debug("Requesting response to temp topic {}", tempTopic);
              final byte[] bytes;

              final MessageConsumer consumer = session.createConsumer(tempTopic);
              try {
                final BytesMessage bytesMessage = session.createBytesMessage();
                bytesMessage.writeBytes(request);
                bytesMessage.setJMSReplyTo(tempTopic);
                final Destination requestDestination = getJmsTemplate().getDestinationResolver().resolveDestinationName(session, getDestinationName(), getJmsTemplate().isPubSubDomain());
                final MessageProducer producer = session.createProducer(requestDestination);
                try {
                  producer.send(bytesMessage);
                } finally {
                  producer.close();
                }
                final Message response = consumer.receive(getJmsTemplate().getReceiveTimeout());
                if (response == null) {
                  // TODO UTL-37.
                  s_logger.error("Timeout reached while waiting for a response to send to {}", responseReceiver);
                  return null;
                }
                bytes = JmsByteArrayHelper.extractBytes(response);
              } finally {
                consumer.close();
              }
              s_logger.debug("Dispatching response of length {}", bytes.length);
              responseReceiver.messageReceived(bytes);

            } catch (Exception ex) {
              // TODO UTL-37.
              s_logger.error("Unexpected exception while waiting for a response to send to " + responseReceiver, ex);
            }
            return null;
          }
        }, true);
      }
    });
  }

}
