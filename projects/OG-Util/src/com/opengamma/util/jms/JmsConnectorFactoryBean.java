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
 * <p>
 * The caller can set the connection factory, or one/both of the templates.
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
   * The topic template.
   */
  private JmsTemplate _jmsTemplateTopic;
  /**
   * The queue template.
   */
  private JmsTemplate _jmsTemplateQueue;
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
    setJmsTemplateTopic(base.getJmsTemplateTopic());
    setJmsTemplateQueue(base.getJmsTemplateQueue());
    setTopicName(base.getTopicName());
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
   * Gets the JMS template for topic-based messages.
   * 
   * @return the JMS template for topics
   */
  public JmsTemplate getJmsTemplateTopic() {
    return _jmsTemplateTopic;
  }

  /**
   * Sets the template for topic-based messages.
   * If this is not set, then a template with standard defaults will be created.
   * 
   * @param jmsTemplate  the template
   */
  public void setJmsTemplateTopic(JmsTemplate jmsTemplate) {
    _jmsTemplateTopic = jmsTemplate;
  }

  /**
   * Gets the JMS template for queue-based messages.
   * 
   * @return the JMS template for topics
   */
  public JmsTemplate getJmsTemplateQueue() {
    return _jmsTemplateQueue;
  }

  /**
   * Sets the template for queue-based messages.
   * If this is not set, then a template with standard defaults will be created.
   * 
   * @param jmsTemplate  the template
   */
  public void setJmsTemplateQueue(JmsTemplate jmsTemplate) {
    _jmsTemplateQueue = jmsTemplate;
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
    final ConnectionFactory providedFactory = getConnectionFactory();  // store in variable to protect against change by subclass
    final JmsTemplate providedTemplateTopic = getJmsTemplateTopic();  // store in variable to protect against change by subclass
    final JmsTemplate providedTemplateQueue = getJmsTemplateQueue();  // store in variable to protect against change by subclass
    
    final JmsTemplate jmsTemplateTopic = createTemplateTopic(providedFactory, providedTemplateTopic, providedTemplateQueue);
    final JmsTemplate jmsTemplateQueue = createTemplateQueue(providedFactory, providedTemplateQueue, providedTemplateTopic);
    final String topicName = getTopicName();  // store in variable to protect against change by subclass
    return new JmsConnector(getName(), jmsTemplateTopic, jmsTemplateQueue, topicName);
  }

  /**
   * Creates the template for topics.
   * 
   * @param providedFactory  the provided factory, may be null
   * @param providedTemplateTopic  the provided template for topics, may be null
   * @param providedTemplateQueue  the provided template for queues, may be null
   * @return the topic template, may be null
   */
  protected JmsTemplate createTemplateTopic(ConnectionFactory providedFactory, JmsTemplate providedTemplateTopic, JmsTemplate providedTemplateQueue) {
    if (providedTemplateTopic != null) {
      return providedTemplateTopic;
    }
    if (providedFactory == null && providedTemplateQueue != null) {
      providedFactory = providedTemplateQueue.getConnectionFactory();
    }
    if (providedFactory == null) {
      throw new IllegalArgumentException("ConnectionFactory or JmsTemplate must be provided");
    }
    JmsTemplate template = new JmsTemplate(providedFactory);
    template.setPubSubDomain(true);
    return template;
  }

  /**
   * Creates the template for queues.
   * 
   * @param providedFactory  the provided factory, may be null
   * @param providedTemplateTopic  the provided template for topics, may be null
   * @param providedTemplateQueue  the provided template for queues, may be null
   * @return the topic template, may be null
   */
  protected JmsTemplate createTemplateQueue(ConnectionFactory providedFactory, JmsTemplate providedTemplateTopic, JmsTemplate providedTemplateQueue) {
    if (providedTemplateQueue != null) {
      return providedTemplateQueue;
    }
    if (providedFactory == null && providedTemplateTopic != null) {
      providedFactory = providedTemplateTopic.getConnectionFactory();
    }
    if (providedFactory == null) {
      throw new IllegalArgumentException("ConnectionFactory or JmsTemplate must be provided");
    }
    JmsTemplate template = new JmsTemplate(providedFactory);
    template.setPubSubDomain(false);
    return template;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
