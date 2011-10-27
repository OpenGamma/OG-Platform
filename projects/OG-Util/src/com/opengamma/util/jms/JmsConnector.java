/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.jms;

import javax.jms.ConnectionFactory;

import org.springframework.jms.core.JmsTemplate;

import com.opengamma.util.ArgumentChecker;

/**
 * Connector used to access JMS.
 * <p>
 * This class is usually configured using the associated factory bean.
 */
public class JmsConnector {

  /**
   * The configuration name.
   */
  private final String _name;
  /**
   * The JMS template.
   */
  private final JmsTemplate _jmsTemplate;
  /**
   * The topic name.
   */
  private final String _topicName;

  /**
   * Creates an instance.
   * 
   * @param name  the configuration name, not null
   * @param jmsTemplate  the JMS Spring template, not null
   * @param topicName  the topic name, null if left up to the application
   */
  public JmsConnector(String name, JmsTemplate jmsTemplate, String topicName) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(jmsTemplate, "jmsTemplate");
    _name = name;
    _jmsTemplate = jmsTemplate;
    _topicName = topicName;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the display name of the connector.
   * 
   * @return a name usable for display, not null
   */
  public String getName() {
    return _name;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the JMS connection factory.
   * <p>
   * This is shared between all users of this object and must not be further configured.
   * 
   * @return the JMS connection factory, may be null
   */
  public ConnectionFactory getConnectionFactory() {
    return _jmsTemplate.getConnectionFactory();
  }

  /**
   * Gets the shared JMS template.
   * <p>
   * This is shared between all users of this object and must not be further configured.
   * 
   * @return the JMS template, null if the session factory is null
   */
  public JmsTemplate getJmsTemplate() {
    return _jmsTemplate;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the topic name.
   * 
   * @return the topic name, null if topic left up to the application
   */
  public String getTopicName() {
    return _topicName;
  }

  /**
   * Returns a copy of this connector with a new topic name.
   * 
   * @param topicName  the new topic name, not null
   * @return a copy of this connector with the new topic name, not null
   */
  public JmsConnector withTopicName(String topicName) {
    return new JmsConnector(_name, _jmsTemplate, topicName);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a description of this object suitable for debugging.
   * 
   * @return the description, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + _name + "]";
  }

}
