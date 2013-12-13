/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.redis;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.Connector;

/**
 * Connector used to access Redis databases.
 * <p>
 * This class is usually configured using the associated factory bean.
 */
public class RedisConnector implements Connector {

  /**
   * The configuration name.
   */
  private final String _name;
  
  private final String _host;
  
  private final int _port;
  
  private final String _password;
  
  private final JedisPool _jedisPool;
  
  private final JedisPoolConfig _jedisPoolConfig;
  
  private final int _timeOut;
  
  /**
   * Creates an instance.
   *  
   * @param name the configuration name, not null
   * @param host the redis server host, not null
   * @param port the redis server port, not null
   */
  public RedisConnector(final String name, final String host, final int port) {
    this(name, host, port, (String) null);
  }

  /**
   * Creates an instance.
   *  
   * @param name the configuration name, not null
   * @param host the redis server host, not null
   * @param port the redis server port, not null
   * @param password the redis server password, may be null
   */
  public RedisConnector(final String name, final String host, final int port, String password) {
    this(name, host, port, password, new JedisPoolConfig(), Protocol.DEFAULT_TIMEOUT);
  }

  /**
   * Creates an instance.
   * 
   * @param name  the configuration name, not null
   * @param host  the redis server host, not null
   * @param port  the redis server port, not null
   * @param config the redis pool config, not null
   */
  public RedisConnector(final String name, final String host, final int port, final JedisPoolConfig config) {
    this(name, host, port, null, config, Protocol.DEFAULT_TIMEOUT);
  }

  /**
   * Creates an instance.
   * 
   * @param name  the configuration name, not null
   * @param host  the redis server host, not null
   * @param port  the redis server port, not null
   * @param password the redis server password. may be null
   * @param config the redis pool config, not null
   * @param timeOut the time out in millisecond, not null
   */
  public RedisConnector(final String name, final String host, final int port, final String password, final JedisPoolConfig config, final int timeOut) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(host, "redisServer");
    ArgumentChecker.notNull(port, "port");
    ArgumentChecker.notNull(config, "jedis pool config");
    ArgumentChecker.notNull(timeOut, "timeOut");
    
    _name = name;
    _host = host;
    _port = port;
    _password = password;
    _jedisPoolConfig = config;
    _timeOut = timeOut;
    
    JedisPool pool = null;
    if (password == null) {
      pool = new JedisPool(config, _host, _port, _timeOut);
    } else {
      pool = new JedisPool(config, _host, _port, _timeOut, _password);
    }
    _jedisPool = pool;
  }

  //-------------------------------------------------------------------------
  @Override
  public final String getName() {
    return _name;
  }
  
  /**
   * Gets the host.
   * @return the host
   */
  public String getHost() {
    return _host;
  }

  /**
   * Gets the port.
   * @return the port
   */
  public int getPort() {
    return _port;
  }

  /**
   * Gets the password.
   * @return the password
   */
  public String getPassword() {
    return _password;
  }

  /**
   * Gets the jedisPool.
   * @return the jedisPool
   */
  public JedisPool getJedisPool() {
    return _jedisPool;
  }
  
  /**
   * Gets the timeOut.
   * @return the timeOut
   */
  public int getTimeOut() {
    return _timeOut;
  }

  /**
   * Gets the jedisPoolConfig.
   * @return the jedisPoolConfig
   */
  public JedisPoolConfig getJedisPoolConfig() {
    return _jedisPoolConfig;
  }

  @Override
  public final Class<? extends Connector> getType() {
    return RedisConnector.class;
  }

  //-------------------------------------------------------------------------
  @Override
  public void close() {
    _jedisPool.destroy();
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
