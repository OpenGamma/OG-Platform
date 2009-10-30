/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.ByteArrayMessageReceiver;
import com.opengamma.transport.ByteArrayRequestSender;

/**
 * 
 *
 * @author kirk
 */
public class JmsByteArrayRequestSender
extends AbstractJmsByteArraySender
implements ByteArrayRequestSender {
  private static final Logger s_logger = LoggerFactory.getLogger(JmsByteArrayRequestSender.class);

  /**
   * @param destinationName
   * @param jmsTemplate
   */
  public JmsByteArrayRequestSender(String destinationName,
      JmsTemplate jmsTemplate) {
    super(destinationName, jmsTemplate);
  }

  @Override
  public void sendRequest(final byte[] request,
      ByteArrayMessageReceiver responseReceiver) {
    s_logger.debug("Dispatching request of size {} to destination {}", request.length, getDestinationName());
    byte[] responseBytes = (byte[]) getJmsTemplate().execute(new SessionCallback() {
      @Override
      public Object doInJms(Session session) throws JMSException {
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
        if(response == null) {
          return null;
        }
        byte[] bytes = JmsByteArrayHelper.extractBytes(response);
        consumer.close();
        return bytes;
      }
      
    }, true);
    if(responseBytes == null) {
      throw new OpenGammaRuntimeException("Did not receive response within " + getJmsTemplate().getReceiveTimeout() + "ms");
    }
    s_logger.debug("Dispatching response of length {}", responseBytes.length);
    responseReceiver.messageReceived(responseBytes);
  }

}
