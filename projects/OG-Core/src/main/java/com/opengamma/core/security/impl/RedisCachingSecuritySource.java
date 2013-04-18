/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.mapping.FudgeObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.google.common.base.Charsets;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.AbstractSecuritySource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

// TODO kirk 2013-04-16 -- Redis allows TTL to be set on values.
// To match a typical cache, we should give the option to set that.
/**
 * A caching {@link SecuritySource} which is only capable of satisfying
 * calls by UniqueId ({@link #get(UniqueId)}).
 */
public class RedisCachingSecuritySource extends AbstractSecuritySource implements SecuritySource {
  private static final Logger s_logger = LoggerFactory.getLogger(RedisCachingSecuritySource.class);
  private final SecuritySource _underlying;
  private final JedisPool _jedisPool;
  private final String _redisPrefix;
  private final FudgeContext _fudgeContext;
  private final Set<UniqueId> _knownInRedis = new HashSet<UniqueId>();
  
  // REVIEW kirk 2013-04-17 -- It's really not clear at all that any of the locking
  // is necessary or desirable at all. Since we're not actually holding any state,
  // and the underlying source would synchronize anything else, it's really not clear
  // that we're getting any advantage out of the locking.
  // That being said, I've left in the locking logic for now until we determine
  // whether it's desirable.

  private final ReadWriteLock _lock = new ReentrantReadWriteLock();
  
  public RedisCachingSecuritySource(SecuritySource underlying, JedisPool jedisPool) {
    this(underlying, jedisPool, "");
  }
  
  public RedisCachingSecuritySource(SecuritySource underlying, JedisPool jedisPool, String redisPrefix) {
    this(underlying, jedisPool, redisPrefix, OpenGammaFudgeContext.getInstance());
  }
  
  public RedisCachingSecuritySource(SecuritySource underlying, JedisPool jedisPool, String redisPrefix, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(jedisPool, "jedisPool");
    ArgumentChecker.notNull(redisPrefix, "redisPrefix");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _underlying = underlying;
    _jedisPool = jedisPool;
    _redisPrefix = redisPrefix;
    _fudgeContext = fudgeContext;
  }

  /**
   * Gets the underlying.
   * @return the underlying
   */
  protected SecuritySource getUnderlying() {
    return _underlying;
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

  /**
   * Gets the fudgeContext.
   * @return the fudgeContext
   */
  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public Collection<Security> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    Collection<Security> results = getUnderlying().get(bundle, versionCorrection);
    processResults(results);
    return results;
  }

  @Override
  public Collection<Security> get(ExternalIdBundle bundle) {
    Collection<Security> results = getUnderlying().get(bundle);
    processResults(results);
    return results;
  }

  @Override
  public Security getSingle(ExternalIdBundle bundle) {
    Security result = getUnderlying().getSingle(bundle);
    processResult(result);
    return result;
  }

  @Override
  public Security getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    Security result = getUnderlying().getSingle(bundle, versionCorrection);
    processResult(result);
    return result;
  }

  @Override
  public Security get(UniqueId uniqueId) {
    Security security = getFromRedis(uniqueId);
    if (security == null) {
      s_logger.warn("Unable to satisfy {} using Redis", uniqueId);
      security = getUnderlying().get(uniqueId);
      processResult(security);
    } else {
      s_logger.warn("Satisfied {} using Redis", uniqueId);
    }
    return security;
  }

  @Override
  public Security get(ObjectId objectId, VersionCorrection versionCorrection) {
    Security result = getUnderlying().get(objectId, versionCorrection);
    processResult(result);
    return result;
  }

  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }
  
  protected Security getFromRedis(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    byte[] redisKey = toRedisKey(uniqueId);
    Jedis jedis = getJedisPool().getResource();

    try {
      try {
        _lock.readLock().lock();
        
        byte[] data = jedis.get(redisKey);
        if (data == null) {
          return null;
        }
        
        Security security = null;
        try {
          security = convertFromFudge(data);
        } catch (Exception e) {
          s_logger.error("Unserializable data in Redis for uniqueId " + uniqueId + ". Clearing redis.", e);
          try {
            _lock.writeLock().lock();
            jedis.del(redisKey);
          } finally {
            _lock.writeLock().unlock();
          }
        }
        
        return security;
      } finally {
        _lock.readLock().unlock();
      }
    } finally {
      getJedisPool().returnResource(jedis);
    }
  }

  /**
   * @param data
   * @return
   */
  private Security convertFromFudge(byte[] data) {
    ByteArrayInputStream bais = new ByteArrayInputStream(data);
    FudgeObjectReader objectReader = getFudgeContext().createObjectReader(bais);
    Security security = objectReader.read(Security.class);
    return security;
  }

  protected void processResults(Collection<Security> securities) {
    for (Security security : securities) {
      processResult(security);
    }
  }
  
  protected void processResult(Security security) {
    if (security == null) {
      // REVIEW kirk 2013-04-16 -- It may be desirable to cache the null result for optimization.
      // If so, this and getFromRedis() should be changed to match.
      return;
    }
    
    byte[] redisKey = toRedisKey(security.getUniqueId());
    
    Jedis jedis = getJedisPool().getResource();
    try {
      try {
        _lock.readLock().lock();
        
        if (_knownInRedis.contains(security.getUniqueId())) {
          // Already in the cache. Nothing to do here.
          // This may happen if it is being processed as a part of a collection getter.
          return;
        }
        
        if (jedis.exists(redisKey)) {
          // Already in the cache. Nothing to do here.
          // This may happen if it is being processed as a part of a collection getter.
          //s_logger.warn("Not storing {} as already in Redis", security.getUniqueId());
          return;
        }
        
        s_logger.warn("Storing security type {} id {} bundle {} to Redis",
            new Object[] {security.getSecurityType(), security.getUniqueId(), security.getExternalIdBundle()});
        byte[] fudgeData = convertToFudge(security);
        jedis.set(redisKey, fudgeData);
      } finally {
        _lock.readLock().unlock();
      }
    } finally {
      getJedisPool().returnResource(jedis);
    }
    
  }
  
  /**
   * @param security
   * @return
   */
  private byte[] convertToFudge(Security security) {
    ArgumentChecker.notNull(security, "security");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    getFudgeContext().writeObject(security, baos);
    byte[] bytes = baos.toByteArray();
    return bytes;
  }

  private static byte[] toRedisKey(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    String key = uniqueId.toString();
    byte[] bytes = Charsets.UTF_8.encode(key).array();
    return bytes;
  }

}
