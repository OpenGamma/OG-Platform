/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.jms;

import java.net.URI;

import javax.jms.ConnectionFactory;

import org.apache.commons.lang.StringUtils;
import org.springframework.jms.core.JmsTemplate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.Connector;
import com.opengamma.util.ReflectionUtils;

/**
 * Connector used to access JMS.
 * <p>
 * This class is usually configured using the associated factory bean.
 */
public class JmsConnector implements Connector {

  /**
   * The configuration name.
   */
  private final String _name;
  /**
   * The JMS template for topic-based messaging.
   */
  private final JmsTemplate _jmsTemplateTopic;
  /**
   * The JMS template for queue-based messaging.
   */
  private final JmsTemplate _jmsTemplateQueue;
  /**
   * The factory for creating containers for topics.
   * This is used to listen to JMS messages.
   */
  private final JmsTopicContainerFactory _topicContainerFactory;
  /**
   * The factory for creating containers for queues.
   * This is used to listen to JMS messages.
   */
  private final JmsQueueContainerFactory _queueContainerFactory;
  /**
   * The configuration needed by the client to connect to the broker.
   */
  private final URI _clientBrokerUri;
  /**
   * The topic name.
   */
  private final String _topicName;

  /**
   * Creates an instance.
   * 
   * @param name  the configuration name, not null
   * @param jmsTemplateTopic  the JMS Spring template for topic-based messaging, not null
   * @param jmsTemplateQueue  the JMS Spring template for queue-based messaging, not null
   * @param containerFactoryTopic  the container factory for topics, may be null
   * @param containerFactoryQueue the container factory for queues, may be null
   * @param clientBrokerUri  the client configuration to connect to the broker, such as a URI, null if no config provided for clients
   * @param topicName  the topic name, null if left up to the application
   */
  public JmsConnector(String name, JmsTemplate jmsTemplateTopic, JmsTemplate jmsTemplateQueue, 
      JmsTopicContainerFactory containerFactoryTopic, JmsQueueContainerFactory containerFactoryQueue,
      URI clientBrokerUri, String topicName) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(jmsTemplateTopic, "jmsTemplateTopic");
    ArgumentChecker.notNull(jmsTemplateQueue, "jmsTemplateQueue");
    _name = name;
    _jmsTemplateTopic = jmsTemplateTopic;
    _jmsTemplateQueue = jmsTemplateQueue;
    _topicContainerFactory = containerFactoryTopic;
    _queueContainerFactory = containerFactoryQueue;
    _clientBrokerUri = clientBrokerUri;
    _topicName = topicName;
  }

  //-------------------------------------------------------------------------
  @Override
  public final String getName() {
    return _name;
  }

  @Override
  public final Class<? extends Connector> getType() {
    return JmsConnector.class;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the JMS connection factory.
   * <p>
   * This extracts the factory of the topic template, which should be the same as that of the queue template.
   * This is shared between all users of this object and must not be further configured.
   * 
   * @return the JMS connection factory, may be null
   */
  public ConnectionFactory getConnectionFactory() {
    return _jmsTemplateTopic.getConnectionFactory();
  }

  /**
   * Gets the shared JMS template for topic-based messaging.
   * <p>
   * This is shared between all users of this object and must not be further configured.
   * 
   * @return the JMS template for topic-based messaging, null if not setup by configuration
   */
  public JmsTemplate getJmsTemplateTopic() {
    return _jmsTemplateTopic;
  }

  /**
   * Gets the shared JMS template for queue-based messaging.
   * <p>
   * This is shared between all users of this object and must not be further configured.
   * 
   * @return the JMS template for queue-based messaging, null if not setup by configuration
   */
  public JmsTemplate getJmsTemplateQueue() {
    return _jmsTemplateQueue;
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
   * Gets the queueContainerFactory.
   * @return the queueContainerFactory
   */
  public JmsQueueContainerFactory getQueueContainerFactory() {
    return _queueContainerFactory;
  }

  //-------------------------------------------------------------------------
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
   * @param topicName  the new topic name, not empty
   * @return a connector with the specified topic name, not null
   */
  public JmsConnector withTopicName(String topicName) {
    ArgumentChecker.notEmpty(topicName, "topicName");
    return new JmsConnector(_name, _jmsTemplateTopic, _jmsTemplateQueue, _topicContainerFactory, _queueContainerFactory, _clientBrokerUri, topicName);
  }

  //-------------------------------------------------------------------------
  /**
   * Ensures that the topic name is set, using the current name or the specified default
   * 
   * @return a connector which definitely has a topic name, not null
   */
  public JmsConnector ensureTopicName() {
    if (StringUtils.isNotEmpty(_topicName)) {
      return this;
    }
    throw new IllegalStateException("JMS topic name must be set");
  }

  /**
   * Ensures that the topic name is set, using the current name or the specified default
   * 
   * @param defaultTopicName  the default topic name, not empty
   * @return a connector which definitely has a topic name, not null
   */
  public JmsConnector ensureTopicName(String defaultTopicName) {
    if (StringUtils.isNotEmpty(_topicName)) {
      return this;
    }
    return withTopicName(defaultTopicName);
  }

  //-------------------------------------------------------------------------
  @Override
  public void close() {
    ReflectionUtils.close(getConnectionFactory());
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
