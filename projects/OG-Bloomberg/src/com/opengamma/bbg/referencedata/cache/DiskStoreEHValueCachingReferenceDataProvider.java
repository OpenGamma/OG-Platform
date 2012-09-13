/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import java.io.Serializable;

import net.sf.ehcache.CacheManager;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;

import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;

/**
 * Decorates a reference data provider, adding caching.
 * <p>
 * The cache is implemented using {@code EHCache} and uses a byte[] for use on disk.
 */
public class DiskStoreEHValueCachingReferenceDataProvider extends EHValueCachingReferenceDataProvider {

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying reference data provider, not null
   * @param cacheManager  the cache manager, not null
   */
  public DiskStoreEHValueCachingReferenceDataProvider(final ReferenceDataProvider underlying, final CacheManager cacheManager) {
    super(underlying, cacheManager);
  }

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying reference data provider, not null
   * @param cacheManager  the cache manager, not null
   * @param fudgeContext  the Fudge context, not null
   */
  public DiskStoreEHValueCachingReferenceDataProvider(final ReferenceDataProvider underlying, final CacheManager cacheManager, final FudgeContext fudgeContext) {
    super(underlying, cacheManager, fudgeContext);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ReferenceData parseCachedObject(Object fromCache) {
    CachedReferenceDataForDisk rd = (CachedReferenceDataForDisk) fromCache;
    FudgeMsg fieldValues = getFudgeContext().deserialize(rd._fieldData).getMessage();
    return new ReferenceData(rd._identifier, fieldValues);
  }

  @Override
  protected Object createCachedObject(ReferenceData refDataResult) {
    CachedReferenceDataForDisk result = new CachedReferenceDataForDisk();
    result._identifier = refDataResult.getIdentifier();
    result._fieldData = getFudgeContext().toByteArray(refDataResult.getFieldValues());
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Data holder for storing the results.
   */
  private static class CachedReferenceDataForDisk implements Serializable {
    /** Serialization. */
    private static final long serialVersionUID = -8758715022254765226L;
    private String _identifier;
    private byte[] _fieldData;
  }

}
