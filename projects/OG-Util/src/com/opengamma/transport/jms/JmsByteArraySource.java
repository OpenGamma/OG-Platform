/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
 * The {@code receiveTimeout} property on the supplied {@link JmsTemplate}
 * is completely ignored (and is actually changed dynamically by the
 * {@code JmsByteArraySource}).
 * The whole class is synchronized to ensure that the last message batch is maintained
 * properly, and use of the underlying {@link JmsTemplate} is handled properly.
 *
 * @author kirk
 */
public class JmsByteArraySource implements ByteArraySource {
  private final JmsTemplate _jmsTemplate;
  private final List<Message> _lastMessageBatch = Collections.synchronizedList(new ArrayList<Message>());
  
  public JmsByteArraySource(JmsTemplate jmsTemplate) {
    ArgumentChecker.notNull(jmsTemplate, "jmsTemplate");
    _jmsTemplate = jmsTemplate;
  }

  /**
   * @return the jmsTemplate
   */
  public JmsTemplate getJmsTemplate() {
    return _jmsTemplate;
  }
  
  public List<Message> getLastMessageBatch() {
    return new ArrayList<Message>(_lastMessageBatch);
  }

  @Override
  public synchronized List<byte[]> batchReceive(long maxWaitInMilliseconds) {
    getJmsTemplate().setReceiveTimeout(maxWaitInMilliseconds);
    Message message = getJmsTemplate().receive();
    if (message == null) {
      return Collections.emptyList();
    }
    _lastMessageBatch.clear();
    List<byte[]> byteBatch = new LinkedList<byte[]>();
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
  public synchronized byte[] receive(long maxWaitInMilliseconds) {
    getJmsTemplate().setReceiveTimeout(maxWaitInMilliseconds);
    Message message = getJmsTemplate().receive();
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
