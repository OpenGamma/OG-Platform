/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeContext;

import com.opengamma.bbg.referencedata.cache.MongoDBReferenceDataCache;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.mongo.MongoConnector;

/**
 * An implementation of {@link CachingReferenceDataProvider} which puts all data
 * into a Mongo database.
 */
public class MongoDBCachingReferenceDataProvider extends AbstractCachingReferenceDataProvider {
  //TODO: generic version of this to turn inheritance into composition (AbstractCachingReferenceDataProvider)

  /**
   * The Mongo collection name.
   */
  public static final String REFERENCE_DATA = "ReferenceData";

  /**
   * The cache.
   */
  private final MongoDBReferenceDataCache _cache;

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying provider, not null
   * @param mongoConnector  the Mongo connector, not null
   */
  public MongoDBCachingReferenceDataProvider(final ReferenceDataProvider underlying, final MongoConnector mongoConnector) {
    this(underlying, mongoConnector, OpenGammaFudgeContext.getInstance());
  }

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying provider, not null
   * @param mongoConnector  the Mongo connector, not null
   * @param fudgeContext  the Fudge context, not null
   */
  public MongoDBCachingReferenceDataProvider(final ReferenceDataProvider underlying, final MongoConnector mongoConnector, final FudgeContext fudgeContext) {
    super(underlying, fudgeContext);
    ArgumentChecker.notNull(mongoConnector, "mongoConnector");
    _cache = new MongoDBReferenceDataCache(mongoConnector, REFERENCE_DATA);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void persistSecurityFields(PerSecurityReferenceDataResult securityResult) {
    _cache.saveCachedResult(securityResult);    
  }

  @Override
  protected Map<String, PerSecurityReferenceDataResult> loadCachedResults(Set<String> securities) {
    return _cache.loadCachedResults(securities);    
  }

  /**
   * Gets the cache field.
   * @return the cache
   */
  public MongoDBReferenceDataCache getCache() {
    return _cache;
  }
}
