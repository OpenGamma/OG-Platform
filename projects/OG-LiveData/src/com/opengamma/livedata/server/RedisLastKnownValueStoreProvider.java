/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

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
  private String _server = "localhost";
  private int _port = 6379;
  private String _globalPrefix = "";
  private boolean _writeThrough = true;
  private boolean _isInitialized;
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
    return null;
  }
  
  protected synchronized void initIfNecessary() {
    if (_isInitialized) {
      return;
    }
    assert _jedisPool == null;
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    //poolConfig.set...
    JedisPool pool = new JedisPool(poolConfig, getServer(), getPort());
    _jedisPool = pool;
    
    _isInitialized = true;
  }

}
