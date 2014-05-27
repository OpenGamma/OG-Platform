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
import java.util.HashMap;
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
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/*
 * REDIS DATA STRUCTURES:
 * Key["ALL_CLASSES"] -> Set[ClassName]
 * Key[ClassName] -> Set[ConfigName]
 * Key[ClassName - ConfigName] -> Hash
 *     Hash["UniqueId"] -> Text-encoded unique ID for the object
 * Key[UniqueId] -> Hash
 *     Hash["DATA"] -> Fudge encoded configuration data
 *     Hash["CLASS"] -> Class name for the item
 *     Hash["NAME"] -> Item name for the item
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
  private final String _allClassesKey;
  
  private static final byte[] DATA_NAME_AS_BYTES = "DATA".getBytes(Charsets.UTF_8);
  private static final byte[] CLASS_NAME_AS_BYTES = "CLASS".getBytes(Charsets.UTF_8);
  private static final byte[] ITEM_NAME_AS_BYTES = "ITEM".getBytes(Charsets.UTF_8);
  
  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "RedisCfg";

  
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
    
    String allClassesKey = null;
    if (redisPrefix.isEmpty()) {
      allClassesKey = "ALL_CLASSES";
    } else {
      allClassesKey = redisPrefix + "-" + "ALL_CLASSES";
    }
    _allClassesKey = allClassesKey;
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
  // REDIS UTILITIES AND KEY MANAGEMENT
  // ---------------------------------------------------------------------
  
  protected <R> String getClassKeyName(Class<R> clazz) {
    StringBuilder sb = new StringBuilder();
    if (!getRedisPrefix().isEmpty()) {
      sb.append(getRedisPrefix());
      sb.append("-");
    }
    sb.append(clazz.getName());
    return sb.toString();
  }
  
  protected <R> String getClassNameRedisKey(Class<R> clazz, String configName) {
    StringBuilder sb = new StringBuilder();
    sb.append(getClassKeyName(clazz));
    sb.append('-');
    sb.append(configName);
    String hashKeyName = sb.toString();
    return hashKeyName;
  }
  
  private byte[] getUniqueIdKey(UniqueId uniqueId) {
    StringBuilder sb = new StringBuilder();
    if (!getRedisPrefix().isEmpty()) {
      sb.append(getRedisPrefix());
      sb.append("-");
    }
    sb.append(uniqueId);
    String text = sb.toString();
    byte[] bytes = text.getBytes(Charsets.UTF_8);
    return bytes;
  }

  protected <R> R convertBytesToConfigurationObject(Class<R> clazz, byte[] dataAsBytes) {
    FudgeObjectReader objectReader = getFudgeContext().createObjectReader(new ByteArrayInputStream(dataAsBytes));
    R object = objectReader.read(clazz);
    return object;
  }
  
  // ---------------------------------------------------------------------
  // DATA SETTING/UPDATING OPERATIONS
  // UNIQUE TO THIS CLASS
  // ---------------------------------------------------------------------
  
  public <R> UniqueId put(Class<R> clazz, String configName, R object) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(configName, "configName");
    ArgumentChecker.notNull(object, "object");
    ArgumentChecker.isTrue(clazz.isAssignableFrom(object.getClass()), "Unable to assign " + object.getClass() + " to " + clazz);
    
    UniqueId uniqueId = UniqueId.of(IDENTIFIER_SCHEME_DEFAULT, GUIDGenerator.generate().toString());
    
    if (object instanceof MutableUniqueIdentifiable) {
      MutableUniqueIdentifiable identifiable = (MutableUniqueIdentifiable) object;
      identifiable.setUniqueId(uniqueId);
    }
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    FudgeObjectWriter objectWriter = getFudgeContext().createObjectWriter(baos);
    objectWriter.write(object);
    byte[] objectAsBytes = baos.toByteArray();
    
    String classKeyName = getClassKeyName(clazz);
    String classNameRedisKey = getClassNameRedisKey(clazz, configName);
    byte[] uniqueIdKey = getUniqueIdKey(uniqueId);
    
    Jedis jedis = getJedisPool().getResource();
    try {
      jedis.sadd(_allClassesKey, clazz.getName());
      jedis.sadd(classKeyName, configName);
      jedis.hset(classNameRedisKey, "UniqueId", uniqueId.toString());
      jedis.hset(uniqueIdKey, DATA_NAME_AS_BYTES, objectAsBytes);
      jedis.hset(uniqueIdKey, CLASS_NAME_AS_BYTES, clazz.getName().getBytes(Charsets.UTF_8));
      jedis.hset(uniqueIdKey, ITEM_NAME_AS_BYTES, configName.getBytes(Charsets.UTF_8));
      
      getJedisPool().returnResource(jedis);
    } catch (Exception e) {
      s_logger.warn("Unable to persist to Redis - " + clazz + " - " + configName, e);
      getJedisPool().returnBrokenResource(jedis);
      throw new OpenGammaRuntimeException("Unable to persist to Redis - " + clazz + " - " + configName, e);
    }
    return uniqueId;
  }
  
  public <R> void delete(Class<R> clazz, String configName) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(configName, "configName");
    
    String classKeyName = getClassKeyName(clazz);
    String classNameRedisKey = getClassNameRedisKey(clazz, configName);

    Jedis jedis = getJedisPool().getResource();
    try {
      
      jedis.srem(classKeyName, configName);
      String uniqueIdText = jedis.hget(classNameRedisKey, "UniqueId");
      if (uniqueIdText != null) {
        UniqueId uniqueId = UniqueId.parse(uniqueIdText);
        byte[] uniqueIdKey = getUniqueIdKey(uniqueId);
        jedis.del(uniqueIdKey);
      }
      jedis.del(classNameRedisKey);
      
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
    ConfigItem<R> latest = getLatestItemByName(clazz, configName);
    if (latest == null) {
      return Collections.emptyList();
    }
    return Collections.singleton(latest);
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
        String classNameRedisKey = getClassNameRedisKey(clazz, itemName);
        String uniqueIdText = jedis.hget(classNameRedisKey, "UniqueId");
        if (uniqueIdText != null) {
          UniqueId uniqueId = UniqueId.parse(uniqueIdText);
          byte[] uniqueIdKey = getUniqueIdKey(uniqueId);
          byte[] dataAsBytes = jedis.hget(uniqueIdKey, DATA_NAME_AS_BYTES);
          R config = convertBytesToConfigurationObject(clazz, dataAsBytes);
          ConfigItem<R> configItem = ConfigItem.of(config);
          configItem.setName(itemName);
          configItem.setType(clazz);
          configItem.setUniqueId(uniqueId);
          items.add(configItem);
        }
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
    ConfigItem<R> latestItem = getLatestItemByName(clazz, configName);
    if (latestItem == null) {
      return null;
    }
    return latestItem.getValue();
  }
  
  public <R> ConfigItem<R> getLatestItemByName(Class<R> clazz, String configName) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(configName, "configName");
    
    String classKeyName = getClassKeyName(clazz);
    String classNameRedisKey = getClassNameRedisKey(clazz, configName);
    
    byte[] dataAsBytes = null;
    UniqueId uniqueId = null;
    
    Jedis jedis = getJedisPool().getResource();
    try {
      if (jedis.sismember(classKeyName, configName)) {
        String uniqueIdText = jedis.hget(classNameRedisKey, "UniqueId");
        if (uniqueIdText != null) {
          uniqueId = UniqueId.parse(uniqueIdText);
          byte[] uniqueIdKey = getUniqueIdKey(uniqueId);
          dataAsBytes = jedis.hget(uniqueIdKey, DATA_NAME_AS_BYTES);
        }
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
    
    ConfigItem<R> configItem = new ConfigItem<R>();
    configItem.setType(clazz);
    configItem.setValue(config);
    configItem.setUniqueId(uniqueId);
    configItem.setName(configName);
    
    return configItem;
  }
  
  // ---------------------------------------------------------------------
  // UNIQUE ID OPERATIONS
  // ---------------------------------------------------------------------
  

  @Override
  public Map<UniqueId, ConfigItem<?>> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, ConfigItem<?>> result = new HashMap<UniqueId, ConfigItem<?>>();
    for (UniqueId uniqueId : uniqueIds) {
      ConfigItem<?> item = get(uniqueId);
      result.put(uniqueId, item);
    }
    return result;
  }

  @Override
  public Map<ObjectId, ConfigItem<?>> get(Collection<ObjectId> objectIds, VersionCorrection versionCorrection) {
    Map<ObjectId, ConfigItem<?>> result = new HashMap<ObjectId, ConfigItem<?>>();
    for (ObjectId objectId : objectIds) {
      ConfigItem<?> item = get(objectId, null);
      result.put(objectId, item);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ConfigItem<?> get(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    byte[] uniqueIdKey = getUniqueIdKey(uniqueId);
    byte[] dataAsBytes = null;
    String className = null;
    
    Jedis jedis = getJedisPool().getResource();
    try {
      dataAsBytes = jedis.hget(uniqueIdKey, DATA_NAME_AS_BYTES);
      className = new String(jedis.hget(uniqueIdKey, CLASS_NAME_AS_BYTES), Charsets.UTF_8);
      getJedisPool().returnResource(jedis);
    } catch (Exception e) {
      s_logger.warn("Unable to lookup by unique id - " + uniqueId, e);
      getJedisPool().returnBrokenResource(jedis);
      throw new OpenGammaRuntimeException("Unable to lookup by unique id - " + uniqueId, e);
    }
    
    if (dataAsBytes == null) {
      return null;
    }
    
    Class<?> clazz;
    try {
      clazz = Class.forName(className);
    } catch (ClassNotFoundException ex) {
      s_logger.warn("Found config item of type {} which we can't load.", className);
      return null;
    }
    
    Object config = convertBytesToConfigurationObject(clazz, dataAsBytes);
    
    @SuppressWarnings("rawtypes")
    ConfigItem configItem = new ConfigItem();
    configItem.setType(clazz);
    configItem.setValue(config);
    configItem.setUniqueId(uniqueId);
    
    return configItem;
  }

  @Override
  public ConfigItem<?> get(ObjectId objectId, VersionCorrection versionCorrection) {
    return get(UniqueId.of(objectId, null));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> R getConfig(Class<R> clazz, UniqueId uniqueId) {
    ConfigItem<?> configItem = get(uniqueId);
    if (configItem == null) {
      return null;
    }
    return (R) configItem.getValue();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> R getConfig(Class<R> clazz, ObjectId objectId, VersionCorrection versionCorrection) {
    ConfigItem<?> configItem = get(UniqueId.of(objectId, null));
    if (configItem == null) {
      return null;
    }
    return (R) configItem.getValue();
  }

  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

}
