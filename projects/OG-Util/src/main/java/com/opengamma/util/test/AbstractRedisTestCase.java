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
  private JedisPool _jedisPool;
  private String _redisPrefix;
  
  @BeforeClass
  public void launchJedisPool() {
    _jedisPool = new JedisPool("localhost");
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
