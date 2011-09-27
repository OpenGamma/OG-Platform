/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

/**
 * Hosts a temporary queue through which messages will be received.
 */
public class JmsTemporaryQueueHost {

  /**
   * The message consumer.
   */
  private final MessageConsumer _consumer;
  /**
   * The connection.
   */
  private final Connection _connection;
  /**
   * The temporary queue
   */
  private final TemporaryQueue _queue;
  
  public JmsTemporaryQueueHost(ConnectionFactory connectionFactory, MessageListener listener) throws JMSException {
    _connection = connectionFactory.createConnection();
    Session session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    _queue = session.createTemporaryQueue();
    _consumer = session.createConsumer(_queue);
    _consumer.setMessageListener(listener);
    _connection.start();
  }
  
  public String getQueueName() throws JMSException {
    return _queue.getQueueName();
  }
  
  public void close() throws JMSException {
    _connection.close();
  }
  
}
