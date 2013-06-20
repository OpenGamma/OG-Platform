/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.jms;

import java.net.URI;

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
   * The factory for creating containers for topics.
   */
  private JmsTopicContainerFactory _topicContainerFactory;
  /**
   * The factory for creating containers for queues.
   */
  private JmsQueueContainerFactory _queueContainerFactory;
  /**
   * The configuration needed by the client to connect to the broker.
   */
  private URI _clientBrokerUri;
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
    setTopicContainerFactory(base.getTopicContainerFactory());
    setQueueContainerFactory(base.getQueueContainerFactory());
    setClientBrokerUri(base.getClientBrokerUri());
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
   * Gets the topic container factory.
   * 
   * @return the factory for topic containers, null if not available
   */
  public JmsTopicContainerFactory getTopicContainerFactory() {
    return _topicContainerFactory;
  }

  /**
   * Sets the topic container factory.
   * 
   * @param factory  the factory
   */
  public void setTopicContainerFactory(JmsTopicContainerFactory factory) {
    _topicContainerFactory = factory;
  }

  /**
   * Gets the queueContainerFactory.
   * @return the queueContainerFactory
   */
  public JmsQueueContainerFactory getQueueContainerFactory() {
    return _queueContainerFactory;
  }

  /**
   * Sets the queueContainerFactory.
   * @param queueContainerFactory  the queueContainerFactory
   */
  public void setQueueContainerFactory(JmsQueueContainerFactory queueContainerFactory) {
    _queueContainerFactory = queueContainerFactory;
  }

  /**
   * Gets the broker configuration needed by the client to connect to the server.
   * <p>
   * The client needs some form of configuration, frequently a URI, to connect to the JMS broker.
   * This field provides that configuration.
   * 
   * @return the client broker configuration, null if no config provided for clients
   */
  public URI getClientBrokerUri() {
    return _clientBrokerUri;
  }

  /**
   * Sets the broker configuration needed by the client to connect to the server.
   * <p>
   * The client needs some form of configuration, frequently a URI, to connect to the JMS broker.
   * This field provides that configuration.
   * 
   * @param clientBrokerConfig  the client broker configuration, null if no config provided for clients
   */
  public void setClientBrokerUri(URI clientBrokerConfig) {
    _clientBrokerUri = clientBrokerConfig;
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
  protected JmsConnector createObject() {
    ArgumentChecker.notNull(getName(), "name");
    final ConnectionFactory providedFactory = getConnectionFactory();  // store in variable to protect against change by subclass
    final JmsTemplate providedTemplateTopic = getJmsTemplateTopic();  // store in variable to protect against change by subclass
    final JmsTemplate providedTemplateQueue = getJmsTemplateQueue();  // store in variable to protect against change by subclass
    final JmsTopicContainerFactory providedTopicContainerFactory = getTopicContainerFactory();  // store in variable to protect against change by subclass
    final JmsQueueContainerFactory providedQueueContainerFactory = getQueueContainerFactory();  // store in variable to protect against change by subclass
    
    final JmsTemplate jmsTemplateTopic = createTemplateTopic(providedFactory, providedTemplateTopic, providedTemplateQueue);
    final JmsTemplate jmsTemplateQueue = createTemplateQueue(providedFactory, providedTemplateQueue, providedTemplateTopic);
    final JmsTopicContainerFactory topicContainerFactory = createTopicContainerFactory(providedTopicContainerFactory, jmsTemplateTopic.getConnectionFactory());
    final JmsQueueContainerFactory queueContainerFactory = createQueueContainerFactory(providedQueueContainerFactory, jmsTemplateQueue.getConnectionFactory());
    final URI clientBrokerUri = getClientBrokerUri();  // store in variable to protect against change by subclass
    final String topicName = getTopicName();  // store in variable to protect against change by subclass
    return new JmsConnector(getName(), jmsTemplateTopic, jmsTemplateQueue, topicContainerFactory, queueContainerFactory, clientBrokerUri, topicName);
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

  /**
   * Creates a topic container factory.
   * <p>
   * This implementation creates a simple {@link SpringJmsTopicContainerFactory}
   * if one is not provided.
   * 
   * @param providedContainerfactory  the provided factory, may be null
   * @param connectionFactory  the JMS connection factory, not null
   * @return the container factory, may be null
   */
  protected JmsTopicContainerFactory createTopicContainerFactory(JmsTopicContainerFactory providedContainerfactory, ConnectionFactory connectionFactory) {
    if (providedContainerfactory != null) {
      return providedContainerfactory;
    }
    return new SpringJmsTopicContainerFactory(connectionFactory);
  }

  /**
   * Creates a queue container factory.
   * <p>
   * This implementation creates a simple {@link SpringJmsQueueContainerFactory}
   * if one is not provided.
   * 
   * @param providedContainerfactory  the provided factory, may be null
   * @param connectionFactory  the JMS connection factory, not null
   * @return the container factory, may be null
   */
  protected JmsQueueContainerFactory createQueueContainerFactory(JmsQueueContainerFactory providedContainerfactory, ConnectionFactory connectionFactory) {
    if (providedContainerfactory != null) {
      return providedContainerfactory;
    }
    return new SpringJmsQueueContainerFactory(connectionFactory);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
