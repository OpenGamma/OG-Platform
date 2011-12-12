/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

import org.springframework.jms.support.JmsUtils;

import com.opengamma.util.jms.JmsConnector;

/**
 * Hosts a temporary queue through which messages will be received.
 */
public class JmsTemporaryQueueHost {

  /**
   * The message consumer.
   */
  private final MessageConsumer _consumer;
  /**
   * The session.
   */
  private final Session _session;
  /**
   * The connection.
   */
  private final Connection _connection;
  /**
   * The temporary queue
   */
  private final TemporaryQueue _queue;

  public JmsTemporaryQueueHost(JmsConnector jmsConnector, MessageListener listener) throws JMSException {
    _connection = jmsConnector.getConnectionFactory().createConnection();
    _session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    _queue = _session.createTemporaryQueue();
    _consumer = _session.createConsumer(_queue);
    _consumer.setMessageListener(listener);
    _connection.start();
  }

  public String getQueueName() throws JMSException {
    return _queue.getQueueName();
  }

  public void close() throws JMSException {
    JmsUtils.closeMessageConsumer(_consumer);
    JmsUtils.closeSession(_session);
    _connection.close();
  }
}
