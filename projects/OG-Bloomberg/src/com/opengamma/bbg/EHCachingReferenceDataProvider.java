/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * 
 */
public class EHCachingReferenceDataProvider extends AbstractCachingReferenceDataProvider {
  
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
  
  public EHCachingReferenceDataProvider(final ReferenceDataProvider underlying, final CacheManager cacheManager) {
    this(underlying, cacheManager, OpenGammaFudgeContext.getInstance());    
  }
  
  public EHCachingReferenceDataProvider(final ReferenceDataProvider underlying, final CacheManager cacheManager, final FudgeContext fudgeContext) {
    super(underlying, fudgeContext);
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _cacheManager = cacheManager;
    EHCacheUtils.addCache(cacheManager, REFERENCE_DATA_CACHE);
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, REFERENCE_DATA_CACHE);
  }
    
  public CacheManager getCacheManager() {
    return _cacheManager;
  }
  
  @Override
  protected void persistSecurityFields(PerSecurityReferenceDataResult securityResult) {
    String securityDes = securityResult.getSecurity();
    FudgeMsg fieldData = securityResult.getFieldData();
    
    if (securityDes != null && fieldData != null) {
      s_logger.info("Persisting fields for \"{}\": {}", securityDes, securityResult.getFieldData());
      CachedPerSecurityReferenceDataResult cachedObject = createCachedObject(securityResult);
      s_logger.debug("cachedObject={}", cachedObject);
      Element element = new Element(securityDes, cachedObject);
      _cache.put(element);
    }
    
  }
  
  @Override
  protected Map<String, PerSecurityReferenceDataResult> loadCachedResults(Set<String> securities) {
    Map<String, PerSecurityReferenceDataResult> result = new TreeMap<String, PerSecurityReferenceDataResult>();
    FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    // REVIEW kirk 2009-10-23 -- Candidate for scatter/gather for performance.
    for (String security : securities) {
      PerSecurityReferenceDataResult cachedResult = loadStateFromCache(serializer, security);
      if (cachedResult != null) {
        result.put(security, cachedResult);
      }
    }
    return result;
  }
  
  protected PerSecurityReferenceDataResult loadStateFromCache(FudgeSerializer serializer, String securityDes) {
    Element element = _cache.get(securityDes);
    if (element != null) {
      s_logger.debug("Have security data for des {} in cache", securityDes);
      CachedPerSecurityReferenceDataResult fromCache = (CachedPerSecurityReferenceDataResult) element.getValue();
      s_logger.debug("cachedObject={}", fromCache);
      PerSecurityReferenceDataResult result = parseCachedObject(fromCache);
      return result;
    }
    return null;
  }
  
  
  private static class CachedPerSecurityReferenceDataResult implements Serializable {
    private String _security;
    private FudgeMsg _fieldData;
  }
  
  /**
   * @param securityDes
   * @param fromCache
   * @return
   */
  private PerSecurityReferenceDataResult parseCachedObject(CachedPerSecurityReferenceDataResult fromCache) {
    PerSecurityReferenceDataResult result = new PerSecurityReferenceDataResult(fromCache._security);
    result.setFieldData(fromCache._fieldData);
    return result;
  }

  protected CachedPerSecurityReferenceDataResult createCachedObject(PerSecurityReferenceDataResult refDataResult) {
    CachedPerSecurityReferenceDataResult result = new CachedPerSecurityReferenceDataResult();
    result._security = refDataResult.getSecurity();
    result._fieldData = getFudgeContext().newMessage(refDataResult.getFieldData());
    return result;
  }
 
}
