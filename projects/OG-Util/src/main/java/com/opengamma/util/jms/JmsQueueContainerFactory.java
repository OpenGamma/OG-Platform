/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.jms;

/**
 * Factory used to create containers that receive JMS messages.
 */
public interface JmsQueueContainerFactory {

  /**
   * Creates a container to receive JMS messages.
   * 
   * @param queueName  the topic name, not null
   * @param listener  the listener, not null
   * @return the container, not null
   */
  JmsQueueContainer create(String queueName, Object listener);
}
