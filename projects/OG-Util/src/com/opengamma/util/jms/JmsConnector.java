/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.jms;

import java.io.Closeable;
import java.net.URI;

import javax.jms.ConnectionFactory;

import org.apache.commons.lang.StringUtils;
import org.springframework.jms.core.JmsTemplate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ReflectionUtils;

/**
 * Connector used to access JMS.
 * <p>
 * This class is usually configured using the associated factory bean.
 */
public class JmsConnector implements Closeable {

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
   * @param clientBrokerUri  the client configuration to connect to the broker, such as a URI, null if no config provided for clients
   * @param topicName  the topic name, null if left up to the application
   */
  public JmsConnector(String name, JmsTemplate jmsTemplateTopic, JmsTemplate jmsTemplateQueue, URI clientBrokerUri, String topicName) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(jmsTemplateTopic, "jmsTemplateTopic");
    ArgumentChecker.notNull(jmsTemplateQueue, "jmsTemplateQueue");
    _name = name;
    _jmsTemplateTopic = jmsTemplateTopic;
    _jmsTemplateQueue = jmsTemplateQueue;
    _clientBrokerUri = clientBrokerUri;
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
    return new JmsConnector(_name, _jmsTemplateTopic, _jmsTemplateQueue, _clientBrokerUri, topicName);
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
