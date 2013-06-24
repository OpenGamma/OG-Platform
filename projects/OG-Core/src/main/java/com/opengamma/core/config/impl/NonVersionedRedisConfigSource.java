/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.mapping.FudgeObjectReader;
import org.fudgemsg.mapping.FudgeObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.google.common.base.Charsets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/*
 * REDIS DATA STRUCTURES:
 * Key["ALL_CLASSES"] -> Set[ClassName]
 * Key[ClassName] -> Set[ConfigName]
 * Key[ClassName - ConfigName] -> Hash
 * Hash["DATA"] -> Fudge encoded configuration data
 * 
 * While this data structure is more than necessary (in that you could cut out the hash for
 * the configuration item), it allows future expansion if more data is required to be stored
 * later without reformatting the Redis instance.
 * 
 */


/**
 * A lightweight {@link ConfigSource} that cannot handle any versioning, and
 * which stores all configuration documents as a Fudge-encoded BLOB in Redis as a
 * backing store.
 */
public class NonVersionedRedisConfigSource implements ConfigSource {
  private static final Logger s_logger = LoggerFactory.getLogger(NonVersionedRedisConfigSource.class);
  private final JedisPool _jedisPool;
  private final FudgeContext _fudgeContext;
  private final String _redisPrefix;
  
  private static final byte[] DATA_NAME_AS_BYTES = "DATA".getBytes(Charsets.UTF_8);
  
  public NonVersionedRedisConfigSource(JedisPool jedisPool) {
    this(jedisPool, "");
  }
  
  public NonVersionedRedisConfigSource(JedisPool jedisPool, String redisPrefix) {
    this(jedisPool, redisPrefix, OpenGammaFudgeContext.getInstance());
  }
  
  public NonVersionedRedisConfigSource(JedisPool jedisPool, String redisPrefix, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(jedisPool, "jedisPool");
    ArgumentChecker.notNull(redisPrefix, "redisPrefix");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    
    _jedisPool = jedisPool;
    _redisPrefix = redisPrefix;
    _fudgeContext = fudgeContext;
  }

  /**
   * Gets the jedisPool.
   * @return the jedisPool
   */
  protected JedisPool getJedisPool() {
    return _jedisPool;
  }

  /**
   * Gets the fudgeContext.
   * @return the fudgeContext
   */
  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }
  
  /**
   * Gets the redisPrefix.
   * @return the redisPrefix
   */
  protected String getRedisPrefix() {
    return _redisPrefix;
  }

  // ---------------------------------------------------------------------
  // DATA SETTING/UPDATING OPERATIONS
  // UNIQUE TO THIS CLASS
  // ---------------------------------------------------------------------
  
  public <R> void put(Class<R> clazz, String configName, R object) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(configName, "configName");
    ArgumentChecker.notNull(object, "object");
    ArgumentChecker.isTrue(clazz.isAssignableFrom(object.getClass()), "Unable to assign " + object.getClass() + " to " + clazz);
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    FudgeObjectWriter objectWriter = getFudgeContext().createObjectWriter(baos);
    objectWriter.write(object);
    byte[] objectAsBytes = baos.toByteArray();
    
    String classKeyName = getClassKeyName(clazz);
    byte[] itemHashKeyName = getItemHashKeyName(clazz, configName);

    Jedis jedis = getJedisPool().getResource();
    try {
      jedis.sadd("ALL_CLASSES", clazz.getName());
      jedis.sadd(classKeyName, configName);
      jedis.hset(itemHashKeyName, DATA_NAME_AS_BYTES, objectAsBytes);
      
      getJedisPool().returnResource(jedis);
    } catch (Exception e) {
      s_logger.warn("Unable to persist to Redis - " + clazz + " - " + configName, e);
      getJedisPool().returnBrokenResource(jedis);
      throw new OpenGammaRuntimeException("Unable to persist to Redis - " + clazz + " - " + configName, e);
    }
  }
  
  public <R> void delete(Class<R> clazz, String configName) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(configName, "configName");
    
    String classKeyName = getClassKeyName(clazz);
    byte[] itemHashKeyName = getItemHashKeyName(clazz, configName);

    Jedis jedis = getJedisPool().getResource();
    try {
      
      jedis.srem(classKeyName, configName);
      jedis.del(itemHashKeyName);
      
      getJedisPool().returnResource(jedis);
    } catch (Exception e) {
      s_logger.warn("Unable to delete from Redis - " + clazz + " - " + configName, e);
      getJedisPool().returnBrokenResource(jedis);
      throw new OpenGammaRuntimeException("Unable to persist from Redis - " + clazz + " - " + configName, e);
    }
  }
  
  // ---------------------------------------------------------------------
  // CORE IMPLEMENTED METHODS ON CONFIGSOURCE
  // ---------------------------------------------------------------------
  
  @Override
  public <R> Collection<ConfigItem<R>> get(Class<R> clazz, String configName, VersionCorrection versionCorrection) {
    R latest = getLatestByName(clazz, configName);
    ConfigItem<R> configItem = ConfigItem.of(latest);
    configItem.setName(configName);
    // REVIEW kirk 2013-06-03 -- Do we need to do any more to the config item?
    return Collections.singleton(configItem);
  }

  @Override
  public <R> Collection<ConfigItem<R>> getAll(Class<R> clazz, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    
    String classKeyName = getClassKeyName(clazz);
    
    List<ConfigItem<R>> items = new LinkedList<ConfigItem<R>>();
    
    Jedis jedis = getJedisPool().getResource();
    try {
      
      Set<String> itemNames = jedis.smembers(classKeyName);
      
      for (String itemName : itemNames) {
        byte[] itemHashKeyName = getItemHashKeyName(clazz, itemName);
        byte[] dataAsBytes = jedis.hget(itemHashKeyName, DATA_NAME_AS_BYTES);
        R config = convertBytesToConfigurationObject(clazz, dataAsBytes);
        ConfigItem<R> configItem = ConfigItem.of(config);
        configItem.setName(itemName);
        // REVIEW kirk 2013-06-03 -- Do we need to do any more to the config item?
        items.add(configItem);
      }
      
      getJedisPool().returnResource(jedis);
    } catch (Exception e) {
      s_logger.warn("Unable to lookup from Redis - " + clazz, e);
      getJedisPool().returnBrokenResource(jedis);
      throw new OpenGammaRuntimeException("Unable to lookup from Redis - " + clazz, e);
    }
    
    return items;
  }

  @Override
  public <R> R getSingle(Class<R> clazz, String configName, VersionCorrection versionCorrection) {
    return getLatestByName(clazz, configName);
  }

  @Override
  public <R> R getLatestByName(Class<R> clazz, String configName) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(configName, "configName");
    
    String classKeyName = getClassKeyName(clazz);
    byte[] itemHashKeyName = getItemHashKeyName(clazz, configName);
    
    byte[] dataAsBytes = null;
    
    Jedis jedis = getJedisPool().getResource();
    try {
      if (jedis.sismember(classKeyName, configName)) {
        dataAsBytes = jedis.hget(itemHashKeyName, DATA_NAME_AS_BYTES);
      } else {
        s_logger.debug("No config named {} for class {}", configName, clazz);
      }
      
      getJedisPool().returnResource(jedis);
    } catch (Exception e) {
      s_logger.warn("Unable to lookup latest by name from Redis - " + clazz + " - " + configName, e);
      getJedisPool().returnBrokenResource(jedis);
      throw new OpenGammaRuntimeException("Unable to lookup latest by name from Redis - " + clazz + " - " + configName, e);
    }
    
    if (dataAsBytes == null) {
      s_logger.debug("No data for config named {} for class {}", configName, clazz);
      return null;
    }
    
    R config = convertBytesToConfigurationObject(clazz, dataAsBytes);
    
    return config;
  }
  
  protected <R> String getClassKeyName(Class<R> clazz) {
    StringBuilder sb = new StringBuilder();
    if (!getRedisPrefix().isEmpty()) {
      sb.append(getRedisPrefix());
      sb.append("-");
    }
    sb.append(clazz.getName());
    return sb.toString();
  }
  
  protected <R> byte[] getItemHashKeyName(Class<R> clazz, String configName) {
    StringBuilder sb = new StringBuilder();
    sb.append(getClassKeyName(clazz));
    sb.append('-');
    sb.append(configName);
    String hashKeyName = sb.toString();
    byte[] bytes = hashKeyName.getBytes(Charsets.UTF_8);
    return bytes;
  }
  
  protected <R> R convertBytesToConfigurationObject(Class<R> clazz, byte[] dataAsBytes) {
    FudgeObjectReader objectReader = getFudgeContext().createObjectReader(new ByteArrayInputStream(dataAsBytes));
    R object = objectReader.read(clazz);
    return object;
  }

  // ---------------------------------------------------------------------
  // UNSUPPORTED OPERATIONS
  // ---------------------------------------------------------------------
  
  @Override
  public Map<UniqueId, ConfigItem<?>> get(Collection<UniqueId> uniqueIds) {
    throw new UnsupportedOperationException("Not implemented in this ConfigSource.");
  }

  @Override
  public Map<ObjectId, ConfigItem<?>> get(Collection<ObjectId> objectIds, VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException("Not implemented in this ConfigSource.");
  }

  @Override
  public ChangeManager changeManager() {
    throw new UnsupportedOperationException("Not implemented in this ConfigSource.");
  }

  @Override
  public ConfigItem<?> get(UniqueId uniqueId) {
    throw new UnsupportedOperationException("Not implemented in this ConfigSource.");
  }

  @Override
  public ConfigItem<?> get(ObjectId objectId, VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException("Not implemented in this ConfigSource.");
  }

  @Override
  public <R> R getConfig(Class<R> clazz, UniqueId uniqueId) {
    throw new UnsupportedOperationException("Not implemented in this ConfigSource.");
  }

  @Override
  public <R> R getConfig(Class<R> clazz, ObjectId objectId, VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException("Not implemented in this ConfigSource.");
  }

}
