/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.amqp;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import com.opengamma.transport.ByteArrayMessageSender;
import com.opengamma.util.ArgumentChecker;

/**
 * Sender for AMQP.
 */
public class AmqpByteArrayMessageSender extends AbstractAmqpByteArraySender implements ByteArrayMessageSender {

  private final MessageProperties _messageProperties;

  /**
   * Creates an instance.
   * 
   * @param amqpTemplate  the template, not null
   * @param exchange  the exchange, not null
   * @param routingKey  the routing key, not null
   */
  public AmqpByteArrayMessageSender(AmqpTemplate amqpTemplate, String exchange, String routingKey) {
    this(amqpTemplate, exchange, routingKey, new MessageProperties());
  }

  /**
   * Creates an instance.
   * 
   * @param amqpTemplate  the template, not null
   * @param exchange  the exchange, not null
   * @param routingKey  the routing key, not null
   * @param messageProperties  the properties, not null
   */
  public AmqpByteArrayMessageSender(AmqpTemplate amqpTemplate, String exchange, String routingKey, MessageProperties messageProperties) {
    super(amqpTemplate, exchange, routingKey);
    ArgumentChecker.notNull(messageProperties, "messageProperties");
    _messageProperties = messageProperties;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the message properties.
   * 
   * @return the properties, not null
   */
  public MessageProperties getMessageProperties() {
    return _messageProperties;
  }

  //-------------------------------------------------------------------------
  @Override
  public void send(final byte[] message) {
    Message amqpMsg = new Message(message, getMessageProperties());
    getAmqpTemplate().send(getExchange(), getRoutingKey(), amqpMsg);
  }

}
