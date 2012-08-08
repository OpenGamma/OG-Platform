/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Decorates a reference data provider, adding caching.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingReferenceDataProvider extends AbstractCachingReferenceDataProvider {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingReferenceDataProvider.class);
  /**
   * Cache key for reference data.
   */
  private static final String REFERENCE_DATA_CACHE = "referenceData";

  /**
   * The cache manager.
   */
  private final CacheManager _cacheManager;
  /**
   * The reference data cache.
   */
  private final Cache _cache;

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying reference data provider, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingReferenceDataProvider(final ReferenceDataProvider underlying, final CacheManager cacheManager) {
    this(underlying, cacheManager, OpenGammaFudgeContext.getInstance());    
  }

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying reference data provider, not null
   * @param cacheManager  the cache manager, not null
   * @param fudgeContext  the Fudge context, not null
   */
  public EHCachingReferenceDataProvider(final ReferenceDataProvider underlying, final CacheManager cacheManager, final FudgeContext fudgeContext) {
    super(underlying, fudgeContext);
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _cacheManager = cacheManager;
    EHCacheUtils.addCache(cacheManager, REFERENCE_DATA_CACHE);
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, REFERENCE_DATA_CACHE);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the cache manager.
   * 
   * @return the cache manager, not null
   */
  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  //-------------------------------------------------------------------------
  @Override
  protected void persistSecurityFields(PerSecurityReferenceDataResult securityResult) {
    String securityKey = securityResult.getSecurity();
    FudgeMsg fieldData = securityResult.getFieldData();
    
    if (securityKey != null && fieldData != null) {
      s_logger.info("Persisting fields for \"{}\": {}", securityKey, securityResult.getFieldData());
      CachedPerSecurityReferenceDataResult cachedObject = createCachedObject(securityResult);
      s_logger.debug("cachedObject={}", cachedObject);
      Element element = new Element(securityKey, cachedObject);
      _cache.put(element);
    }
  }

  @Override
  protected Map<String, PerSecurityReferenceDataResult> loadCachedResults(Set<String> securityKeys) {
    Map<String, PerSecurityReferenceDataResult> result = Maps.newTreeMap();
    FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    // REVIEW kirk 2009-10-23 -- Candidate for scatter/gather for performance.
    for (String securityKey : securityKeys) {
      PerSecurityReferenceDataResult cachedResult = loadStateFromCache(serializer, securityKey);
      if (cachedResult != null) {
        result.put(securityKey, cachedResult);
      }
    }
    return result;
  }

  /**
   * Loads the state from the cache.
   * 
   * @param serializer  the Fudge serializer, not null
   * @param securityKey  the security, not null
   * @return the result, null if not found
   */
  protected PerSecurityReferenceDataResult loadStateFromCache(FudgeSerializer serializer, String securityKey) {
    Element element = _cache.get(securityKey);
    if (element != null) {
      s_logger.debug("Have security data for des {} in cache", securityKey);
      CachedPerSecurityReferenceDataResult fromCache = (CachedPerSecurityReferenceDataResult) element.getObjectValue();
      s_logger.debug("cachedObject={}", fromCache);
      PerSecurityReferenceDataResult result = parseCachedObject(fromCache);
      return result;
    }
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * Data holder for storing the results.
   */
  private static class CachedPerSecurityReferenceDataResult implements Serializable {
    /** Serialization. */
    private static final long serialVersionUID = -822452207625365560L;
    private String _security;
    private FudgeMsg _fieldData;
  }

  //-------------------------------------------------------------------------
  /**
   * Parse the cached object.
   * 
   * @param fromCache  the data from the cache, not null
   * @return the result, not null
   */
  private PerSecurityReferenceDataResult parseCachedObject(CachedPerSecurityReferenceDataResult fromCache) {
    PerSecurityReferenceDataResult result = new PerSecurityReferenceDataResult(fromCache._security);
    result.setFieldData(fromCache._fieldData);
    return result;
  }

  /**
   * Creates the cached object.
   * 
   * @param refDataResult  the reference data result.
   * @return the cache object, not null
   */
  protected CachedPerSecurityReferenceDataResult createCachedObject(PerSecurityReferenceDataResult refDataResult) {
    CachedPerSecurityReferenceDataResult result = new CachedPerSecurityReferenceDataResult();
    result._security = refDataResult.getSecurity();
    result._fieldData = getFudgeContext().newMessage(refDataResult.getFieldData());
    return result;
  }

}
