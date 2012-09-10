/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
  /*package*/ static final String REFERENCE_DATA_CACHE = "referenceData";

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
   * @param underlying the underlying reference data provider, not null
   * @param cacheManager the cache manager, not null
   */
  public EHCachingReferenceDataProvider(final ReferenceDataProvider underlying, final CacheManager cacheManager) {
    this(underlying, cacheManager, OpenGammaFudgeContext.getInstance());
  }

  /**
   * Creates an instance.
   * 
   * @param underlying the underlying reference data provider, not null
   * @param cacheManager the cache manager, not null
   * @param fudgeContext the Fudge context, not null
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
   * @param serializer the Fudge serializer, not null
   * @param securityKey the security, not null
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
  private static final class CachedPerSecurityReferenceDataResult implements Serializable {

    private static final long serialVersionUID = 3L;

    private transient String _security;

    private transient FudgeContext _fudgeContext;

    private transient volatile FudgeMsg _fieldDataMsg;

    private transient volatile byte[] _fieldData;

    private byte[] getFieldData() {
      byte[] fieldData = _fieldData;
      if (fieldData == null) {
        synchronized (this) {
          fieldData = _fieldData;
          if (fieldData == null) {
            fieldData = _fudgeContext.toByteArray(_fieldDataMsg);
            _fieldData = fieldData;
            _fieldDataMsg = null;
          }
        }
      }
      return fieldData;
    }

    private void setFieldData(final byte[] fieldData) {
      _fieldData = fieldData;
    }

    public FudgeMsg getFieldDataMsg(final FudgeContext fudgeContext) {
      FudgeMsg fieldDataMsg = _fieldDataMsg;
      if (fieldDataMsg == null) {
        synchronized (this) {
          fieldDataMsg = _fieldDataMsg;
          if (fieldDataMsg == null) {
            _fudgeContext = fudgeContext;
            fieldDataMsg = fudgeContext.deserialize(_fieldData).getMessage();
            _fieldDataMsg = fieldDataMsg;
            _fieldData = null;
          }
        }
      }
      return _fieldDataMsg;
    }

    public void setFieldDataMsg(final FudgeMsg fieldDataMsg, final FudgeContext fudgeContext) {
      _fieldDataMsg = fieldDataMsg;
      _fudgeContext = fudgeContext;
    }

    public void setSecurity(final String security) {
      _security = security;
    }

    public String getSecurity() {
      return _security;
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
      out.writeUTF(getSecurity());
      final byte[] fieldData = getFieldData();
      out.writeInt(fieldData.length);
      out.write(fieldData);
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
      setSecurity(in.readUTF());
      final int dataLength = in.readInt();
      final byte[] data = new byte[dataLength];
      in.readFully(data);
      setFieldData(data);
    }

  }

  //-------------------------------------------------------------------------
  /**
   * Parse the cached object.
   * 
   * @param fromCache the data from the cache, not null
   * @return the result, not null
   */
  private PerSecurityReferenceDataResult parseCachedObject(CachedPerSecurityReferenceDataResult fromCache) {
    PerSecurityReferenceDataResult result = new PerSecurityReferenceDataResult(fromCache.getSecurity());
    result.setFieldData(fromCache.getFieldDataMsg(getFudgeContext()));
    return result;
  }

  /**
   * Creates the cached object.
   * 
   * @param refDataResult the reference data result.
   * @return the cache object, not null
   */
  protected CachedPerSecurityReferenceDataResult createCachedObject(PerSecurityReferenceDataResult refDataResult) {
    CachedPerSecurityReferenceDataResult result = new CachedPerSecurityReferenceDataResult();
    result.setSecurity(refDataResult.getSecurity());
    result.setFieldDataMsg(getFudgeContext().newMessage(refDataResult.getFieldData()), getFudgeContext());
    return result;
  }

}
