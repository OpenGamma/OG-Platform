/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.opengamma.id.ExternalId;

/**
 * An implemention of {@link LastKnownValueStoreProvider} which backs onto Redis.
 * <p/>
 * It has the following properties that should be set:
 * <dl>
 *   <dt>server</dt>
 *   <dd>The hostname of the server that should be used. Defaults to localhost.</dd>
 *   <dt>port</dt>
 *   <dd>The redis port to connect to. Defaults to 6379.</dd>
 *   <dt>globalPrefix</dt>
 *   <dd>A string that will be prepended onto all keys to separate different uses on the same
 *       Redis cluster. Defaults to empty string, for unit testing should probably be set
 *       to user name.</dd>
 *   <dt>writeThrough</dt>
 *   <dd>Whether all writes should flow through to the underlying Redis store.
 *       Defaults to true.
 *       If there are multiple live data servers, only one of which is in charge
 *       of updating Redis, set this to false on all but the master updating
 *       version.</dd>
 * </dl>
 * 
 */
public class RedisLastKnownValueStoreProvider implements LastKnownValueStoreProvider {
  private static final Logger s_logger = LoggerFactory.getLogger(RedisLastKnownValueStoreProvider.class);
  private String _server = "localhost";
  private int _port = 6379;
  private String _globalPrefix = "";
  private boolean _writeThrough = true;
  private volatile boolean _isInitialized;
  private JedisPool _jedisPool;

  /**
   * Gets the server.
   * @return the server
   */
  public String getServer() {
    return _server;
  }

  /**
   * Sets the server.
   * @param server  the server
   */
  public void setServer(String server) {
    _server = server;
  }

  /**
   * Gets the port.
   * @return the port
   */
  public int getPort() {
    return _port;
  }

  /**
   * Sets the port.
   * @param port  the port
   */
  public void setPort(int port) {
    _port = port;
  }

  /**
   * Gets the globalPrefix.
   * @return the globalPrefix
   */
  public String getGlobalPrefix() {
    return _globalPrefix;
  }

  /**
   * Sets the globalPrefix.
   * @param globalPrefix  the globalPrefix
   */
  public void setGlobalPrefix(String globalPrefix) {
    _globalPrefix = globalPrefix;
  }

  /**
   * Gets the writeThrough.
   * @return the writeThrough
   */
  public boolean isWriteThrough() {
    return _writeThrough;
  }

  /**
   * Sets the writeThrough.
   * @param writeThrough  the writeThrough
   */
  public void setWriteThrough(boolean writeThrough) {
    _writeThrough = writeThrough;
  }

  @Override
  public LastKnownValueStore newInstance(ExternalId security, String normalizationRuleSetId) {
    initIfNecessary();
    String redisKey = generateRedisKey(security, normalizationRuleSetId);
    s_logger.debug("Creating Redis LKV store on {}/{} with key name {}", new Object[] {security, normalizationRuleSetId, redisKey});
    updateIdentifiers(security);
    RedisLastKnownValueStore store = new RedisLastKnownValueStore(_jedisPool, redisKey, isWriteThrough());
    return store;
  }
  
  /**
   * @param security
   * @param normalizationRuleSetId
   * @return
   */
  private String generateRedisKey(ExternalId security, String normalizationRuleSetId) {
    StringBuilder sb = new StringBuilder();
    if (getGlobalPrefix() != null) {
      sb.append(getGlobalPrefix());
    }
    sb.append(security.getScheme().getName());
    sb.append("-");
    sb.append(security.getValue());
    sb.append("[");
    sb.append(normalizationRuleSetId);
    sb.append("]");
    return sb.toString();
  }
  
  private String generateAllSchemesKey() {
    StringBuilder sb = new StringBuilder();
    if (getGlobalPrefix() != null) {
      sb.append(getGlobalPrefix());
    }
    sb.append("-<ALL_SCHEMES>");
    return sb.toString();
  }
  
  private String generatePerSchemeKey(String scheme) {
    StringBuilder sb = new StringBuilder();
    if (getGlobalPrefix() != null) {
      sb.append(getGlobalPrefix());
    }
    sb.append(scheme);
    sb.append("-");
    sb.append("<ALL_IDENTIFIERS>");
    return sb.toString();
  }

  protected void initIfNecessary() {
    if (_isInitialized) {
      return;
    }
    synchronized (this) {
      assert _jedisPool == null;
      s_logger.info("Connecting to {}:{}. Write-through set to: {}", new Object[] {getServer(), getPort(), _writeThrough});
      JedisPoolConfig poolConfig = new JedisPoolConfig();
      //poolConfig.set...
      JedisPool pool = new JedisPool(poolConfig, getServer(), getPort());
      _jedisPool = pool;
      
      _isInitialized = true;
    }
  }
  
  protected void updateIdentifiers(ExternalId security) {
    Jedis jedis = _jedisPool.getResource();
    jedis.sadd(generateAllSchemesKey(), security.getScheme().getName());
    jedis.sadd(generatePerSchemeKey(security.getScheme().getName()), security.getValue());
    _jedisPool.returnResource(jedis);
  }

  @Override
  public Set<String> getAllIdentifiers(String identifierScheme) {
    initIfNecessary();
    Jedis jedis = _jedisPool.getResource();
    Set<String> allMembers = jedis.smembers(generatePerSchemeKey(identifierScheme));
    _jedisPool.returnResource(jedis);
    s_logger.info("Loaded {} identifiers from Jedis (full contents in Debug level log)", allMembers.size());
    if (s_logger.isDebugEnabled()) {
      s_logger.debug("Loaded identifiers from Jedis: {}", allMembers);
    }
    return allMembers;
  }

  @Override
  public boolean isAvailable(ExternalId security, String normalizationRuleSetId) {
    initIfNecessary();
    String redisKey = generateRedisKey(security, normalizationRuleSetId);
    Jedis jedis = _jedisPool.getResource();
    boolean isAvailable = jedis.exists(redisKey);
    _jedisPool.returnResource(jedis);
    return isAvailable;
  }
  
}
