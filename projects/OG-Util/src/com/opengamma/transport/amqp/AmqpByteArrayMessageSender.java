/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.amqp;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageCreator;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.SimpleMessageProperties;

import com.opengamma.transport.ByteArrayMessageSender;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class AmqpByteArrayMessageSender extends AbstractAmqpByteArraySender implements ByteArrayMessageSender {
  
  private final MessageProperties _messageProperties;
  
  public AmqpByteArrayMessageSender(AmqpTemplate amqpTemplate,
      String exchange,
      String routingKey) {
    this(amqpTemplate, exchange, routingKey, new SimpleMessageProperties());
  }
  
  public AmqpByteArrayMessageSender(AmqpTemplate amqpTemplate,
      String exchange,
      String routingKey,
      MessageProperties messageProperties) {
    super(amqpTemplate, exchange, routingKey);
    
    ArgumentChecker.notNull(messageProperties, "messageProperties");
    _messageProperties = messageProperties;
  }
  
  public MessageProperties getMessageProperties() {
    return _messageProperties;
  }
  
  @Override
  public void send(final byte[] message) {
    getAmqpTemplate().send(getExchange(), getRoutingKey(), new MessageCreator() {
      public Message createMessage() {
        return new Message(message, getMessageProperties());
      }
    });
  }
  
}
