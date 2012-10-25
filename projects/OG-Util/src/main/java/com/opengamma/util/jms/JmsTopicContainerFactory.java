/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.jms;

/**
 * Factory used to create containers that receive JMS messages.
 */
public interface JmsTopicContainerFactory {

  /**
   * Creates a container to receive JMS messages.
   * 
   * @param topicName  the topic name, not null
   * @param listener  the listener, not null
   * @return the container, not null
   */
  JmsTopicContainer create(String topicName, Object listener);

}
