/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.redis;

import redis.clients.jedis.JedisPoolConfig;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Factory bean to provide Redis database connectors.
 * <p>
 * This class provides a simple-to-setup and simple-to-use way to access Redis databases.
 * The main benefit is simpler configuration, especially if that configuration is in XML.
 */
public class RedisConnectorFactoryBean extends SingletonFactoryBean<RedisConnector> {

  /**
   * The configuration name.
   */
  private String _name;
  /**
   * The host name.
   */
  private String _host = "localhost";
  /**
   * The host port.
   */
  private int _port = 6379;
  /**
   * The redis config
   */
  private JedisPoolConfig _config;
  /**
   * Creates an instance.
   */
  public RedisConnectorFactoryBean() {
  }

  /**
   * Creates an instance based on an existing connector.
   * <p>
   * This copies the name, host and port.
   * 
   * @param base  the base connector to copy, not null
   */
  public RedisConnectorFactoryBean(RedisConnector base) {
    setName(base.getName());
    setHost(base.getHost());
    setPort(base.getPort());
    setConfig(base.getJedisPoolConfig());
  }

  //-------------------------------------------------------------------------
  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }
  
  public String getHost() {
    return _host;
  }

  public void setHost(String host) {
    _host = host;
  }

  public int getPort() {
    return _port;
  }

  public void setPort(int port) {
    _port = port;
  }
  
  /**
   * Gets the config.
   * @return the config
   */
  public JedisPoolConfig getConfig() {
    return _config;
  }

  /**
   * Sets the config.
   * @param config  the config
   */
  public void setConfig(JedisPoolConfig config) {
    _config = config;
  }

  //-------------------------------------------------------------------------
  @Override
  public RedisConnector createObject() {
    final String name = getName();  // store in variable to protect against change by subclass
    ArgumentChecker.notNull(name, "name");
    return new RedisConnector(name, getHost(), getPort(), getConfig());
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
