/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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

/**
 * 
 *
 * @author kirk
 */
public class JmsByteArrayRequestSender extends AbstractJmsByteArraySender implements ByteArrayRequestSender {
  private static final Logger s_logger = LoggerFactory.getLogger(JmsByteArrayRequestSender.class);
  
  private final ExecutorService _executor;

  public JmsByteArrayRequestSender(String destinationName,
      JmsTemplate jmsTemplate) {
    this(destinationName, jmsTemplate, Executors.newCachedThreadPool());
  }
  
  public JmsByteArrayRequestSender(String destinationName,
      JmsTemplate jmsTemplate,
      ExecutorService executor) {
    super(destinationName, jmsTemplate);
    
    ArgumentChecker.notNull(executor, "executor");
    _executor = executor;
  }
  

  @Override
  public void sendRequest(final byte[] request,
      final ByteArrayMessageReceiver responseReceiver) {
    s_logger.debug("Dispatching request of size {} to destination {}", request.length, getDestinationName());
    _executor.execute(new Runnable() {
      @Override
      public void run() {
        getJmsTemplate().execute(new SessionCallback<Object>() {
          @Override
          public Object doInJms(Session session) throws JMSException {
            try {
              TemporaryTopic tempTopic = session.createTemporaryTopic();
              s_logger.debug("Requesting response to temp topic {}", tempTopic);
              MessageConsumer consumer = session.createConsumer(tempTopic);
              BytesMessage bytesMessage = session.createBytesMessage();
              bytesMessage.writeBytes(request);
              bytesMessage.setJMSReplyTo(tempTopic);
              Destination requestDestination = getJmsTemplate().getDestinationResolver().resolveDestinationName(session, getDestinationName(), getJmsTemplate().isPubSubDomain());
              MessageProducer producer = session.createProducer(requestDestination);
              producer.send(bytesMessage);
              Message response = consumer.receive(getJmsTemplate().getReceiveTimeout());
              if (response == null) {
                // TODO UTL-37.
                s_logger.error("Timeout reached while waiting for a response to send to {}", responseReceiver);
                return null;
              }
              byte[] bytes = JmsByteArrayHelper.extractBytes(response);
              consumer.close();
              s_logger.debug("Dispatching response of length {}", bytes.length);
              responseReceiver.messageReceived(bytes);
            
            } catch (Exception e) {
              // TODO UTL-37.
              s_logger.error("Unexpected exception while waiting for a response to send to {}", responseReceiver);
            }
            return null;
          }
        }, true);
      }
    });
  }

}
