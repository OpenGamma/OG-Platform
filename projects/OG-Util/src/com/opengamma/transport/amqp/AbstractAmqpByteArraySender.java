/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.amqp;

import org.springframework.amqp.core.AmqpTemplate;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class AbstractAmqpByteArraySender {
  
  private final AmqpTemplate _amqpTemplate;
  private final String _exchange;
  private final String _routingKey;
  
  public AbstractAmqpByteArraySender(AmqpTemplate amqpTemplate,
      String exchange,
      String routingKey) {
    ArgumentChecker.notNull(amqpTemplate, "amqpTemplate");
    ArgumentChecker.notNull(exchange, "exchange");
    ArgumentChecker.notNull(routingKey, "routingKey");
    _amqpTemplate = amqpTemplate;
    _exchange = exchange;
    _routingKey = routingKey;
  }

  public AmqpTemplate getAmqpTemplate() {
    return _amqpTemplate;
  }

  public String getExchange() {
    return _exchange;
  }

  public String getRoutingKey() {
    return _routingKey;
  }


}
