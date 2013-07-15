/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.wire.types.FudgeWireType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisDataException;

import com.opengamma.OpenGammaRuntimeException;
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
    updateFromRedis(true);
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
          String redisValue = toRedisTextValue(getJedisKey(), field);
          
          if (redisValue == null) {
            // Signaling that we're discarding. Log message came out in toRedisTextValue.
            continue;
          }
          
          // Yep, this is ugly as hell.
          try {
            jedis.hset(getJedisKey(), field.getName(), redisValue);
          } catch (JedisDataException jde) {
            s_logger.warn("Unable to write stuff yo.");
          }
        }
      } catch (Exception e) {
        s_logger.error("Unable to write fields to Redis : " + _jedisKey, e);
      } finally {
        getJedisPool().returnResource(jedis);
      }
    }
    _inMemoryStore.liveDataReceived(fieldValues);
  }
  
  /**
   * Convert the contents of a {@link FudgeField} to the text that will be stored
   * in a Redis instance for LKV storage.
   * @param jedisKey key into Jedis, for debugging in log files only.
   * @param field the field's value to use; passed as a {@code FudgeField} for type inforamtion
   * @return the string to store in Redis.
   */
  protected static String toRedisTextValue(String jedisKey, FudgeField field) {
    switch (field.getType().getTypeId()) {
      case FudgeWireType.DOUBLE_TYPE_ID:
        return field.getValue().toString();
      case FudgeWireType.DOUBLE_ARRAY_TYPE_ID:
        return Arrays.toString((double[]) field.getValue());
      case FudgeWireType.STRING_TYPE_ID:
        return (String) field.getValue();
      default:
        s_logger.info("Redis encoding Key {} Field {} - can only handle double, double[], and String. Discarding.", jedisKey, field);
        return null;
    }
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
   * @param failOnError Whether to propagate any exception from a failure to load.
   *                    This should be used to control whether this is resilient
   *                    in the case of Redis failures.
   */
  public synchronized void updateFromRedis(boolean failOnError) {
    _inMemoryStore.clear();
    Jedis jedis = getJedisPool().getResource();
    try {
      // TODO kirk 2012-07-16 -- Give this a FudgeContext.
      MutableFudgeMsg fudgeMsg = OpenGammaFudgeContext.getInstance().newMessage();
      Map<String, String> allFields = jedis.hgetAll(getJedisKey());
      s_logger.debug("Updating {} from Jedis: {}", getJedisKey(), allFields);
      for (Map.Entry<String, String> fieldEntry : allFields.entrySet()) {
        Object parsedRedisObject = fromRedisTextValue(fieldEntry.getValue());
        fudgeMsg.add(fieldEntry.getKey(), parsedRedisObject);
      }
      _inMemoryStore.liveDataReceived(fudgeMsg);
    } catch (Exception e) {
      s_logger.error("Unable to update from Redis", e);
      if (failOnError) {
        throw new OpenGammaRuntimeException("Unable to load state from underlying Redis instance on " + _jedisKey, e);
      }
    } finally {
      getJedisPool().returnResource(jedis);
    }
  }

  /**
   * This method is not an exemplar of proper software engineering.
   * For more information on the rationale, please see http://jira.opengamma.com/browse/PLAT-2536 .
   * <p/>
   * A few problems with this:
   * <ol>
   *   <li>It's still not clear that storing all data as text is appropriate. See PLAT-2536 for commentary.</li>
   *   <li>Our Redis storage (see {@link #toRedisTextValue(FudgeField)} does not have a type
   *       prefix. Therefore, we're relying on the cascading parse attempts here.
   *       This is clearly not optimal, and will cause us serious issues to try to support
   *       all the various Fudge types.</li>
   *   <li>For some reason Java provides you with a way to produce a standardized String representation
   *       for {@code double[]}, but no way to parse that I could see. So yes, I implemented one.</li>
   * </ol>
   * @param value The text from Redis
   * @return a parsed object from that text
   */
  protected static Object fromRedisTextValue(String value) {
    
    try {
      Double doubleValue = Double.parseDouble(value);
      return doubleValue.doubleValue();
    } catch (Exception e) {
      // Not a double. Ignore.
    }
    
    if (value.startsWith("[") && value.endsWith("]")) {
      // Double array.
      // Is there a way to wipe my association with this code? I fear git blame in this case...
      try {
        String stripped = value.substring(1, value.length() - 1);
        String[] split = stripped.split(Pattern.quote(","));
        double[] doubleValues = new double[split.length];
        for (int i = 0; i < split.length; i++) {
          doubleValues[i] = Double.parseDouble(split[i]);
        }
        return doubleValues;
      } catch (Exception e) {
        // Not a double array. Ignore.
        s_logger.debug("String " + value + " looks like a double array but couldn't be parsed", e);
      }
    }
    
    return value;
  }

}
