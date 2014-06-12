/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Charsets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.metric.OpenGammaMetricRegistry;

/*
 * REDIS DATA STRUCTURES:
 * UniqueIds for each ExternalId:
 *     Key["EXT-"ExternalId] -> Set[UniqueId]
 * Data for a particular security by UniqueId:
 *     Key["UNQ-"UniqueId] -> Hash
 *       Hash["DATA"] -> Fudge encoded security document
 * 
 * While this data structure is more than necessary (in that you could cut out the hash for
 * the security data), it allows future expansion if more data is required to be stored
 * later without reformatting the Redis instance.
 * 
 */

/**
 * A lightweight {@link SecuritySource} that cannot handle any versioning, and
 * which stores all Security documents as a Fudge-encoded BLOB in Redis as a
 * backing store.
 */
public class NonVersionedRedisSecuritySource implements SecuritySource {
  private static final Logger s_logger = LoggerFactory.getLogger(NonVersionedRedisSecuritySource.class);
  private final JedisPool _jedisPool;
  private final FudgeContext _fudgeContext;
  private final String _redisPrefix;
  private Timer _getTimer = new Timer();
  private Timer _putTimer = new Timer();
  
  private static final byte[] DATA_NAME_AS_BYTES = "DATA".getBytes(Charsets.UTF_8);
  private static final byte[] CLASS_NAME_AS_BYTES = "CLASS".getBytes(Charsets.UTF_8);
  
  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "RedisSec";
  
  public NonVersionedRedisSecuritySource(JedisPool jedisPool) {
    this(jedisPool, "");
  }
  
  public NonVersionedRedisSecuritySource(JedisPool jedisPool, String redisPrefix) {
    this(jedisPool, redisPrefix, OpenGammaFudgeContext.getInstance());
  }
  
  public NonVersionedRedisSecuritySource(JedisPool jedisPool, String redisPrefix, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(jedisPool, "jedisPool");
    ArgumentChecker.notNull(redisPrefix, "redisPrefix");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    
    _jedisPool = jedisPool;
    _redisPrefix = redisPrefix;
    _fudgeContext = fudgeContext;
    registerMetrics(OpenGammaMetricRegistry.getSummaryInstance(), OpenGammaMetricRegistry.getDetailedInstance(), "NonVersionedRedisSecuritySource");
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

  public void registerMetrics(MetricRegistry summaryRegistry, MetricRegistry detailRegistry, String namePrefix) {
    _getTimer = summaryRegistry.timer(namePrefix + ".get");
    _putTimer = summaryRegistry.timer(namePrefix + ".put");
  }


  // ---------------------------------------------------------------------
  // REDIS KEY MANAGEMENT
  // ---------------------------------------------------------------------
  
  protected byte[] toRedisKey(UniqueId uniqueId) {
    StringBuilder sb = new StringBuilder();
    if (!getRedisPrefix().isEmpty()) {
      sb.append(getRedisPrefix());
      sb.append("-");
    }
    sb.append("UNQ-");
    sb.append(uniqueId);
    String keyText = sb.toString();
    byte[] bytes = keyText.getBytes(Charsets.UTF_8);
    return bytes;
  }
  
  protected byte[] toRedisKey(ObjectId objectId) {
    return toRedisKey(UniqueId.of(objectId, null));
  }
  
  protected String toRedisKey(ExternalId externalId) {
    StringBuilder sb = new StringBuilder();
    if (!getRedisPrefix().isEmpty()) {
      sb.append(getRedisPrefix());
      sb.append("-");
    }
    sb.append("EXT-");
    sb.append(externalId);
    return sb.toString();
  }
  
  // ---------------------------------------------------------------------
  // DATA SETTING/UPDATING OPERATIONS
  // UNIQUE TO THIS CLASS
  // ---------------------------------------------------------------------
  
  public UniqueId put(Security security) {
    ArgumentChecker.notNull(security, "security");
    //ArgumentChecker.notNull(security.getUniqueId(), "security uniqueId");
    
    UniqueId uniqueId = security.getUniqueId();
    if (uniqueId == null) {
      uniqueId = UniqueId.of(IDENTIFIER_SCHEME_DEFAULT, GUIDGenerator.generate().toString());
    }
    if (uniqueId.getVersion() != null) {
      uniqueId = UniqueId.of(uniqueId.getObjectId(), null);
    }
    if (security instanceof MutableUniqueIdentifiable) {
      MutableUniqueIdentifiable mutableSecurity = (MutableUniqueIdentifiable) security;
      mutableSecurity.setUniqueId(uniqueId);
    }
    
    try (Timer.Context context = _putTimer.time()) {
      byte[] securityData = SecurityFudgeUtil.convertToFudge(getFudgeContext(), security); 

      Jedis jedis = getJedisPool().getResource();
      try {
        
        for (ExternalId externalId : security.getExternalIdBundle()) {
          String redisKey = toRedisKey(externalId);
          
          jedis.sadd(redisKey, uniqueId.toString());
          if (jedis.scard(redisKey) > 1) {
            s_logger.warn("Multiple securities with same ExternalId {}. Probable misuse.", externalId);
          }
        }
        
        byte[] redisKey = toRedisKey(uniqueId);
        jedis.hset(redisKey, DATA_NAME_AS_BYTES, securityData);
        jedis.hset(redisKey, CLASS_NAME_AS_BYTES, security.getClass().getName().getBytes(Charsets.UTF_8));
        
        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to put security " + security, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to put security " + security, e);
      }
      
    }
    return uniqueId;
  }
  
  // ---------------------------------------------------------------------
  // IMPLEMENTATION OF SECURITYSOURCE
  // ---------------------------------------------------------------------
  
  private interface GetWorker<T> {
    T query(Jedis jedis);
  }
  
  protected <T> T executeGet(GetWorker<T> getWorker) {
    try (Timer.Context context = _getTimer.time()) {
      Jedis jedis = getJedisPool().getResource();
      
      T result = null;
      try {
        result = getWorker.query(jedis);
        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to execute get", e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to execute get()", e);
      }
      
      return result;
    }
  }

  @Override
  public Collection<Security> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    return get(bundle);
  }

  @Override
  public Map<ExternalIdBundle, Collection<Security>> getAll(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection) {
    Map<ExternalIdBundle, Collection<Security>> result = new HashMap<ExternalIdBundle, Collection<Security>>();
    
    for (ExternalIdBundle bundle : bundles) {
      result.put(bundle, get(bundle));
    }
    
    return result;
  }

  @Override
  public Collection<Security> get(ExternalIdBundle bundle) {
    Security security = getSingle(bundle);
    if (security == null) {
      return Collections.emptySet();
    } else {
      return Collections.singleton(security);
    }
  }

  @Override
  public Security getSingle(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    
    if (bundle.size() != 1) {
      s_logger.warn("Possible bad use of NonVersionedRedisSecuritySource: bundle size {} not equal to 1.", bundle);
    }
    
    final ExternalId externalId = bundle.iterator().next();
    Security result = executeGet(new GetWorker<Security>() {
      @Override
      public Security query(Jedis jedis) {
        Set<String> uniqueIds = jedis.smembers(toRedisKey(externalId));
        if (uniqueIds.isEmpty()) {
          return null;
        }
        if (uniqueIds.size() > 1) {
          s_logger.info("Following unique IDs for externalId {} : {}. Choosing randomly.", externalId, uniqueIds);
        }
        UniqueId uniqueId = UniqueId.parse(uniqueIds.iterator().next());
        
        return getInJedis(jedis, uniqueId);
      }
      
    });
    return result;
  }

  @Override
  public Security getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    return getSingle(bundle);
  }

  @Override
  public Map<ExternalIdBundle, Security> getSingle(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection) {
    Map<ExternalIdBundle, Security> result = new HashMap<ExternalIdBundle, Security>();
    
    for (ExternalIdBundle bundle : bundles) {
      Security security = getSingle(bundle);
      result.put(bundle, security);
    }
    
    return result;
  }

  @Override
  public Security get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    Security result = executeGet(new GetWorker<Security>() {
      public Security query(Jedis jedis) {
        return getInJedis(jedis, uniqueId);
      }
    });
    return result;
  }

  @Override
  public Security get(final ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");

    Security result = executeGet(new GetWorker<Security>() {
      public Security query(Jedis jedis) {
        return getInJedis(jedis, UniqueId.of(objectId, null));
      }
    });
    return result;
  }

  @Override
  public Map<UniqueId, Security> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, Security> result = new HashMap<UniqueId, Security>();
    
    for (UniqueId uniqueId : uniqueIds) {
      result.put(uniqueId, get(uniqueId));
    }
    
    return result;
  }

  @Override
  public Map<ObjectId, Security> get(Collection<ObjectId> objectIds, VersionCorrection versionCorrection) {
    Map<ObjectId, Security> result = new HashMap<ObjectId, Security>();
    
    for (ObjectId objectId : objectIds) {
      result.put(objectId, get(UniqueId.of(objectId, null)));
    }
    
    return result;
  }

  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }
  
  protected Security getInJedis(Jedis jedis, UniqueId uniqueId) {
    byte[] redisKey = toRedisKey(uniqueId);
    byte[] securityData = jedis.hget(redisKey, DATA_NAME_AS_BYTES);
    byte[] classNameData = jedis.hget(redisKey, CLASS_NAME_AS_BYTES);
    if (securityData == null) {
      s_logger.warn("No data for security unique ID {}", uniqueId);
      return null;
    } else {
      String className = Charsets.UTF_8.decode(ByteBuffer.wrap(classNameData)).toString();
      Security security = null;
      try {
        security = SecurityFudgeUtil.convertFromFudge(getFudgeContext(), className, securityData);
      } catch (Exception ex) {
        s_logger.warn("Unable to convert from fudge for security unique ID " + uniqueId, ex);
      }
      return security;
    }
    
  }

}
