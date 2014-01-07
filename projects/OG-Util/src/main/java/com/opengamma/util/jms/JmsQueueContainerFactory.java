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

  /**
   * Creates a container to receive JMS messages allowing concurrency to be controlled.
   *
   * @param queueName  the topic name, not null
   * @param listener the listener, not null
   * @param concurrentConsumers number of initial consumers for messages, greater than 0
   * @param maxConcurrentConsumers maximum number of consumers for messages, greater than 0
   * @return the container, not null
   */
  JmsQueueContainer create(String queueName, Object listener, int concurrentConsumers, int maxConcurrentConsumers);
}

