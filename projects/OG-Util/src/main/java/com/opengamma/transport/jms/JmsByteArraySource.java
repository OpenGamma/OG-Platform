/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.jms.Message;

import org.springframework.jms.core.JmsTemplate;

import com.opengamma.transport.ByteArraySource;
import com.opengamma.util.ArgumentChecker;

/**
 * A byte array source for JMS.
 * <p>
 * The {@code receiveTimeout} property on the supplied {@link JmsTemplate}
 * is completely ignored, being changed dynamically by this class.
 * The whole class is synchronized to ensure that the last message batch is maintained
 * properly, and use of the underlying {@link JmsTemplate} is handled properly.
 */
public class JmsByteArraySource implements ByteArraySource {

  /**
   * The underlying JMS template.
   */
  private final JmsTemplate _jmsTemplate;
  /**
   * The last message batch.
   */
  private final List<Message> _lastMessageBatch = Collections.synchronizedList(new ArrayList<Message>());

  /**
   * Creates an instance wrapping a JMS template.
   * 
   * @param jmsTemplate  the JMS template, not null
   */
  public JmsByteArraySource(final JmsTemplate jmsTemplate) {
    ArgumentChecker.notNull(jmsTemplate, "jmsTemplate");
    _jmsTemplate = jmsTemplate;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying JMS template.
   * 
   * @return the underlying JMS template, not null
   */
  public JmsTemplate getJmsTemplate() {
    return _jmsTemplate;
  }

  /**
   * Gets a copy of the last message batch.
   * 
   * @return the last batch, not null
   */
  public List<Message> getLastMessageBatch() {
    synchronized (_lastMessageBatch) {  // need to sync as following line uses iterator
      return new ArrayList<Message>(_lastMessageBatch);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public synchronized List<byte[]> batchReceive(final long maxWaitInMilliseconds) {
    getJmsTemplate().setReceiveTimeout(maxWaitInMilliseconds);
    Message message = getJmsTemplate().receive();
    if (message == null) {
      return Collections.emptyList();
    }
    _lastMessageBatch.clear();
    final List<byte[]> byteBatch = new LinkedList<byte[]>();
    getJmsTemplate().setReceiveTimeout(JmsTemplate.RECEIVE_TIMEOUT_NO_WAIT);
    
    while (message != null) {
      _lastMessageBatch.add(message);
      byte[] bytes = JmsByteArrayHelper.extractBytes(message);
      byteBatch.add(bytes);

      message = getJmsTemplate().receive();
    }
    return byteBatch;
  }

  @Override
  public synchronized List<byte[]> batchReceiveNoWait() {
    return batchReceive(JmsTemplate.RECEIVE_TIMEOUT_NO_WAIT);
  }

  @Override
  public synchronized byte[] receive(final long maxWaitInMilliseconds) {
    getJmsTemplate().setReceiveTimeout(maxWaitInMilliseconds);
    final Message message = getJmsTemplate().receive();
    byte[] bytes = JmsByteArrayHelper.extractBytes(message);
    _lastMessageBatch.clear();
    _lastMessageBatch.add(message);
    return bytes;
  }

  @Override
  public synchronized byte[] receiveNoWait() {
    return receive(JmsTemplate.RECEIVE_TIMEOUT_NO_WAIT);
  }

}
