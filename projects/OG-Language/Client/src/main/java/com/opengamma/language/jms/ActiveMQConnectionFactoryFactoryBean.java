/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.jms;

import javax.jms.ConnectionFactory;

import org.apache.activemq.pool.PooledConnectionFactory;

import com.opengamma.language.config.Configuration;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Configures an ActiveMQ JMS provider from the configuration document. The document must have a string entry giving
 * the URL for the ActiveMQ server. 
 */
public class ActiveMQConnectionFactoryFactoryBean extends SingletonFactoryBean<ConnectionFactory> {

  private Configuration _configuration;
  private String _configurationEntry = "activeMQ";

  public void setConfiguration(final Configuration configuration) {
    ArgumentChecker.notNull(configuration, "configuration");
    _configuration = configuration;
  }

  public Configuration getConfiguration() {
    return _configuration;
  }

  public void setConfigurationEntry(final String configurationEntry) {
    ArgumentChecker.notNull(configurationEntry, "configurationEntry");
    _configurationEntry = configurationEntry;
  }

  public String getConfigurationEntry() {
    return _configurationEntry;
  }

  @Override
  protected ConnectionFactory createObject() {
    ArgumentChecker.notNull(getConfiguration(), "configuration");
    final String brokerURL = getConfiguration().getStringConfiguration(getConfigurationEntry());
    if (brokerURL == null) {
      return new PooledConnectionFactory();
    } else {
      return new PooledConnectionFactory(brokerURL);
    }
  }

}
