/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.wire.types.FudgeWireType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * A {@link LastKnownValueStore} backed by a Redis server.
 * In the case where this is not write-through, it primarily just acts
 * as a local cache which can be asynchronously updated from Redis
 * to retrieve the current values.
 * 
 */
public class RedisLastKnownValueStore implements LastKnownValueStore {
  private static final Logger s_logger = LoggerFactory.getLogger(RedisLastKnownValueStore.class);
  private final FieldHistoryStore _inMemoryStore = new FieldHistoryStore();
  private final JedisPool _jedisPool;
  //private final byte[] _jedisKey;
  private String _jedisKey;
  private final boolean _writeThrough;
  
  public RedisLastKnownValueStore(JedisPool jedisPool, String jedisKey, boolean writeThrough) {
    ArgumentChecker.notNull(jedisPool, "Jedis Pool");
    ArgumentChecker.notNull(jedisKey, "Jedis key");
    _jedisPool = jedisPool;
    /*
    try {
      _jedisKey = jedisKey.getBytes("UTF-8");
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("UTF-8 not a supported charset.");
    }
    */
    _jedisKey = jedisKey;
    _writeThrough = writeThrough;
    updateFromRedis();
  }

  /**
   * Gets the jedisKey.
   * @return the jedisKey
   */
  protected String getJedisKey() {
    return _jedisKey;
  }

  /**
   * Gets the jedisPool.
   * @return the jedisPool
   */
  public JedisPool getJedisPool() {
    return _jedisPool;
  }

  /**
   * Gets the writeThrough.
   * @return the writeThrough
   */
  public boolean isWriteThrough() {
    return _writeThrough;
  }
  
  // TODO kirk 2012-07-16 -- Actually implement asynchronous reading from Redis
  
  // TODO kirk 2012-07-16 -- Synchronization here is crazy restrictive.
  // Clearly could be done with a ReadWriteLock.

  @Override
  public synchronized void updateFields(FudgeMsg fieldValues) {
    // TODO kirk 2012-07-16 -- This is really only good enough as a proof of concept.
    // Ideally you'd want to handle more than just double-as-string ('cos really? That totally lame),
    // but I just want to get this working.
    if (isWriteThrough()) {
      Jedis jedis = getJedisPool().getResource();
      try {
        for (FudgeField field : fieldValues.getAllFields()) {
          if (field.getType().getTypeId() != FudgeWireType.DOUBLE_TYPE_ID) {
            s_logger.info("Redis encoding for {} can only handle doubles, can't handle {}", getJedisKey(), field);
            continue;
          }
          // Yep, this is ugly as hell.
          jedis.hset(getJedisKey(), field.getName(), ((Double) field.getValue()).toString());
        }
      } finally {
        getJedisPool().returnResource(jedis);
      }
    }
    _inMemoryStore.liveDataReceived(fieldValues);
  }

  @Override
  public synchronized FudgeMsg getFields() {
    return _inMemoryStore.getLastKnownValues();
  }

  @Override
  public synchronized boolean isEmpty() {
    return _inMemoryStore.isEmpty();
  }

  /**
   * 
   */
  public synchronized void updateFromRedis() {
    _inMemoryStore.clear();
    Jedis jedis = getJedisPool().getResource();
    try {
      // TODO kirk 2012-07-16 -- Give this a FudgeContext.
      MutableFudgeMsg fudgeMsg = OpenGammaFudgeContext.getInstance().newMessage();
      Map<String, String> allFields = jedis.hgetAll(getJedisKey());
      for (Map.Entry<String, String> fieldEntry : allFields.entrySet()) {
        fudgeMsg.add(fieldEntry.getKey(), Double.parseDouble(fieldEntry.getValue()));
      }
      _inMemoryStore.liveDataReceived(fudgeMsg);
    } finally {
      getJedisPool().returnResource(jedis);
    }
  }

}
