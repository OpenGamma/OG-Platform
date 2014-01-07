/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 
 */
public abstract class AbstractRedisTestCase {
  /**
   * Set this property to control which host the test cases will connect to.
   * Defaults to localhost.
   */
  public static final String REDIS_HOST_PROPERTY_NAME = "test.redis.host";
  /**
   * Set this property to control which port the test cases will connec to.
   * Defaults to 6379.
   */
  public static final String REDIS_PORT_PROPERTY_NAME = "test.redis.port";
  
  private static final String DEFAULT_REDIS_HOST = "localhost";
  private static final int DEFAULT_REDIS_PORT = 6379;
  
  private JedisPool _jedisPool;
  private String _redisPrefix;
  
  @BeforeClass
  public void launchJedisPool() {
    String redisHost = DEFAULT_REDIS_HOST;
    String redisHostProperty = System.getProperty(REDIS_HOST_PROPERTY_NAME);
    if (redisHostProperty != null) {
      redisHost = redisHostProperty;
    }
    int redisPort = DEFAULT_REDIS_PORT;
    String redisPortProperty = System.getProperty(REDIS_PORT_PROPERTY_NAME);
    if (redisPortProperty != null) {
      redisPort = Integer.parseInt(redisPortProperty);
    }
    
    _jedisPool = new JedisPool(redisHost, redisPort);
    _redisPrefix = System.getProperty("user.name") + "_" + System.currentTimeMillis();
  }
  
  @AfterClass
  public void clearJedisPool() {
    if (_jedisPool == null) {
      return;
    }
    _jedisPool.destroy();
  }
  
  @BeforeMethod
  public void clearRedisDb() {
    Jedis jedis = _jedisPool.getResource();
    jedis.flushDB();
    _jedisPool.returnResource(jedis);
  }

  /**
   * Gets the jedisPool.
   * @return the jedisPool
   */
  protected JedisPool getJedisPool() {
    return _jedisPool;
  }

  /**
   * Gets the redisPrefix.
   * @return the redisPrefix
   */
  protected String getRedisPrefix() {
    return _redisPrefix;
  }

}
