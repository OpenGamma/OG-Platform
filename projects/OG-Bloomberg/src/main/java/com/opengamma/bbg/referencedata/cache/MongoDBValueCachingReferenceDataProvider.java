/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeContext;

import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.mongo.MongoConnector;

/**
 * Decorates a reference data provider, adding caching.
 * <p>
 * The cache is implemented using the a Mongo database.
 */
public class MongoDBValueCachingReferenceDataProvider extends AbstractValueCachingReferenceDataProvider {

  /**
   * The Mongo collection name.
   */
  public static final String REFERENCE_DATA = "values";

  /**
   * The cache.
   */
  private MongoDBReferenceDataCache _cache;

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying provider, not null
   * @param mongoConnector  the Mongo connector, not null
   */
  public MongoDBValueCachingReferenceDataProvider(ReferenceDataProvider underlying, MongoConnector mongoConnector) {
    this(underlying, mongoConnector, OpenGammaFudgeContext.getInstance());
  }

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying provider, not null
   * @param mongoConnector  the Mongo connector, not null
   * @param fudgeContext  the Fudge context, not null
   */
  public MongoDBValueCachingReferenceDataProvider(ReferenceDataProvider underlying, MongoConnector mongoConnector, FudgeContext fudgeContext) {
    super(underlying, fudgeContext);
    ArgumentChecker.notNull(mongoConnector, "mongoConnector");
    _cache = new MongoDBReferenceDataCache(mongoConnector, REFERENCE_DATA);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the cache field.
   * 
   * @return the cache
   */
  public MongoDBReferenceDataCache getCache() {
    return _cache;
  }

  //-------------------------------------------------------------------------
  @Override
  protected Map<String, ReferenceData> loadFieldValues(Set<String> identifiers) {
    return _cache.load(identifiers);
  }

  @Override
  protected void saveFieldValues(ReferenceData result) {
    _cache.save(result);
  }

}
