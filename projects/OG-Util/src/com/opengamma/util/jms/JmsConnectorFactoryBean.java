/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.jms;

import javax.jms.ConnectionFactory;

import org.springframework.jms.core.JmsTemplate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Factory bean to provide JMS connectors.
 * <p>
 * This class provides a simple-to-setup and simple-to-use way to access JMS.
 * The main benefit is simpler configuration, especially if that configuration is in XML.
 */
public class JmsConnectorFactoryBean extends SingletonFactoryBean<JmsConnector> {

  /**
   * The configuration name.
   */
  private String _name;
  /**
   * The connection factory.
   */
  private ConnectionFactory _connectionFactory;
  /**
   * The template.
   */
  private JmsTemplate _jmsTemplate;
  /**
   * The pub-sub domain flag, as per JmsTemplate.
   */
  private boolean _pubSubDomain;
  /**
   * The topic name.
   */
  private String _topicName;

  /**
   * Creates an instance.
   */
  public JmsConnectorFactoryBean() {
  }

  /**
   * Creates an instance based on an existing connector.
   * <p>
   * This copies the name, factory and template.
   * 
   * @param base  the base connector to copy, not null
   */
  public JmsConnectorFactoryBean(JmsConnector base) {
    setName(base.getName());
    setConnectionFactory(base.getConnectionFactory());
    setJmsTemplate(base.getJmsTemplate());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the descriptive name.
   * 
   * @return the descriptive name
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name.
   * The descriptive name must be set.
   * 
   * @param name  the descriptive name
   */
  public void setName(String name) {
    _name = name;
  }

  /**
   * Gets the connection factory.
   * 
   * @return the connection factory
   */
  public ConnectionFactory getConnectionFactory() {
    return _connectionFactory;
  }

  /**
   * Sets the connection factory.
   * The factory must be set.
   * 
   * @param connectionFactory  the connection factory
   */
  public void setConnectionFactory(ConnectionFactory connectionFactory) {
    _connectionFactory = connectionFactory;
  }

  /**
   * Gets the JMS template.
   * 
   * @return the JMS template
   */
  public JmsTemplate getJmsTemplate() {
    return _jmsTemplate;
  }

  /**
   * Sets the template.
   * If this is not set, then a template will be created.
   * 
   * @param jmsTemplate  the template
   */
  public void setJmsTemplate(JmsTemplate jmsTemplate) {
    _jmsTemplate = jmsTemplate;
  }

  /**
   * Gets the pub-sub domain flag.
   * 
   * @return the flag
   */
  public boolean isPubSubDomain() {
    return _pubSubDomain;
  }

  /**
   * Sets the pub-sub domain.
   * This is only used if the template is not specified by {@link #setJmsTemplate(JmsTemplate)}.
   * This defaults to false.
   * 
   * @param pubSubDomain  the pub-sub domain
   */
  public void setPubSubDomain(boolean pubSubDomain) {
    _pubSubDomain = pubSubDomain;
  }

  /**
   * Gets the topic name.
   * 
   * @return the topic name
   */
  public String getTopicName() {
    return _topicName;
  }

  /**
   * Sets the topic name.
   * This is optional.
   * 
   * @param topicName  the topic name
   */
  public void setTopicName(String topicName) {
    _topicName = topicName;
  }

  //-------------------------------------------------------------------------
  @Override
  public JmsConnector createObject() {
    ArgumentChecker.notNull(getName(), "name");
    final JmsTemplate jmsTemplate = createTemplate();
    final String topicName = getTopicName();  // store in variable to protect against change by subclass
    return new JmsConnector(getName(), jmsTemplate, topicName);
  }

  /**
   * Creates the template.
   * 
   * @return the template, may be null
   */
  protected JmsTemplate createTemplate() {
    final ConnectionFactory providedFactory = getConnectionFactory();  // store in variable to protect against change by subclass
    final JmsTemplate providedTemplate = getJmsTemplate();  // store in variable to protect against change by subclass
    if (providedTemplate != null) {
      return providedTemplate;
    }
    if (providedFactory != null) {
      final JmsTemplate template = new JmsTemplate(providedFactory);
      template.setPubSubDomain(isPubSubDomain());
      return template;
    }
    throw new IllegalArgumentException("Neither ConnectionFactory nor JmsTemplate provided");
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
