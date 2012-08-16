/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

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
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Decorates a reference data provider, adding caching.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHValueCachingReferenceDataProvider extends AbstractValueCachingReferenceDataProvider {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EHValueCachingReferenceDataProvider.class);
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
  public EHValueCachingReferenceDataProvider(final ReferenceDataProvider underlying, final CacheManager cacheManager) {
    this(underlying, cacheManager, OpenGammaFudgeContext.getInstance());    
  }

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying reference data provider, not null
   * @param cacheManager  the cache manager, not null
   * @param fudgeContext  the Fudge context, not null
   */
  public EHValueCachingReferenceDataProvider(final ReferenceDataProvider underlying, final CacheManager cacheManager, final FudgeContext fudgeContext) {
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
  protected Map<String, ReferenceData> loadFieldValues(Set<String> identifiers) {
    Map<String, ReferenceData> result = Maps.newTreeMap();
    FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    for (String identifier : identifiers) {
      ReferenceData cachedResult = loadStateFromCache(serializer, identifier);
      if (cachedResult != null) {
        result.put(identifier, cachedResult);
      }
    }
    return result;
  }

  @Override
  protected void saveFieldValues(ReferenceData result) {
    String identifier = result.getIdentifier();
    FudgeMsg fieldData = result.getFieldValues();
    
    if (identifier != null && fieldData != null) {
      s_logger.info("Persisting fields for \"{}\": {}", identifier, result.getFieldValues());
      Serializable cachedObject = createCachedObject(result);
      s_logger.debug("cachedObject={}", cachedObject);
      Element element = new Element(identifier, cachedObject);
      _cache.put(element);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the state from the cache.
   * 
   * @param serializer  the Fudge serializer, not null
   * @param identifier  the identifier, not null
   * @return the result, null if not found
   */
  protected ReferenceData loadStateFromCache(FudgeSerializer serializer, String identifier) {
    Element element = _cache.get(identifier);
    if (element != null) {
      s_logger.debug("Have security data for des {} in cache", identifier);
      Object fromCache = element.getObjectValue();
      s_logger.debug("cachedObject={}", fromCache);
      return parseCachedObject(fromCache);
    }
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * Parse the cached object.
   * 
   * @param fromCache  the data from the cache, not null
   * @return the result, not null
   */
  protected ReferenceData parseCachedObject(Object fromCache) {
    CachedReferenceData rd = (CachedReferenceData) fromCache;
    return new ReferenceData(rd._identifier, rd._fieldData);
  }

  /**
   * Creates the cached object.
   * 
   * @param refDataResult  the reference data result.
   * @return the cache object, not null
   */
  protected Serializable createCachedObject(ReferenceData refDataResult) {
    CachedReferenceData result = new CachedReferenceData();
    result._identifier = refDataResult.getIdentifier();
    result._fieldData = getFudgeContext().newMessage(refDataResult.getFieldValues());
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Data holder for storing the results.
   */
  private static class CachedReferenceData implements Serializable {
    /** Serialization. */
    private static final long serialVersionUID = -822452207625365560L;
    private String _identifier;
    private FudgeMsg _fieldData;
  }

}
