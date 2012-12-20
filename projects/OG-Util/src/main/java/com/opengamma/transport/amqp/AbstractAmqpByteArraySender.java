/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.amqp;

import org.springframework.amqp.core.AmqpTemplate;

import com.opengamma.util.ArgumentChecker;

/**
 * Sender for AMQP.
 */
public class AbstractAmqpByteArraySender {

  private final AmqpTemplate _amqpTemplate;
  private final String _exchange;
  private final String _routingKey;

  /**
   * Creates an instance.
   * 
   * @param amqpTemplate  the template, not null
   * @param exchange  the exchange, not null
   * @param routingKey  the routing key, not null
   */
  public AbstractAmqpByteArraySender(AmqpTemplate amqpTemplate, String exchange, String routingKey) {
    ArgumentChecker.notNull(amqpTemplate, "amqpTemplate");
    ArgumentChecker.notNull(exchange, "exchange");
    ArgumentChecker.notNull(routingKey, "routingKey");
    _amqpTemplate = amqpTemplate;
    _exchange = exchange;
    _routingKey = routingKey;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the template.
   * 
   * @return the template, not null
   */
  public AmqpTemplate getAmqpTemplate() {
    return _amqpTemplate;
  }

  /**
   * Gets the exchange.
   * 
   * @return the exchange, not null
   */
  public String getExchange() {
    return _exchange;
  }

  /**
   * Gets the routing key.
   * 
   * @return the routing key, not null
   */
  public String getRoutingKey() {
    return _routingKey;
  }

}
