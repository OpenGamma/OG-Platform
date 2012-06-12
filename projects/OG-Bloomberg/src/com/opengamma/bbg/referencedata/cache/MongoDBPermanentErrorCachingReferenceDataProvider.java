/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.MutableFudgeMsg;

import com.bloomberglp.blpapi.SessionOptions;
import com.opengamma.bbg.PerSecurityReferenceDataResult;
import com.opengamma.bbg.referencedata.statistics.BloombergReferenceDataStatistics;
import com.opengamma.bbg.referencedata.statistics.NullBloombergReferenceDataStatistics;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.mongo.MongoConnector;

/**
 * A reference data provider that caches permanent errors in MongoDB.
 */
public class MongoDBPermanentErrorCachingReferenceDataProvider extends AbstractPermanentErrorCachingReferenceDataProvider {

  /**
   * The Mongo collection name.
   */
  private static final String PERMANENT_ERRORS = "PermanentErrors";
  /**
   * The Fudge field name.
   */
  private static final String FIELD_PERMANENT_ERROR_NAME = "PERMANENT_ERROR";

  //TODO: when cache implementation is composed properly this can be split more sanely
  /**
   * The Mongo cache.
   */
  private final MongoDBReferenceDataCache _cache;
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;

  /**
   * Creates an instance.
   * 
   * @param sessionOptions  the session options, not null
   * @param mongoConnector  the Mongo connector, not null
   */
  public MongoDBPermanentErrorCachingReferenceDataProvider(SessionOptions sessionOptions, MongoConnector mongoConnector) {
    this(sessionOptions, mongoConnector, NullBloombergReferenceDataStatistics.INSTANCE);
  }

  /**
   * Creates an instance with statistics gathering.
   * 
   * @param sessionOptions  the session options, not null
   * @param mongoConnector  the Mongo connector, not null
   * @param statistics  the statistics to collect, not null
   */
  public MongoDBPermanentErrorCachingReferenceDataProvider(
      SessionOptions sessionOptions,
      MongoConnector mongoConnector,
      BloombergReferenceDataStatistics statistics) {
    this(sessionOptions, mongoConnector, statistics, OpenGammaFudgeContext.getInstance());
  }

  /**
   * Creates an instance.
   * 
   * @param sessionOptions  the session options, not null
   * @param mongoConnector  the Mongo connector, not null
   * @param statistics  the statistics to collect, not null
   * @param fudgeContext  the Fudge context, not null
   */
  public MongoDBPermanentErrorCachingReferenceDataProvider(
      SessionOptions sessionOptions,
      MongoConnector mongoConnector,
      BloombergReferenceDataStatistics statistics,
      FudgeContext fudgeContext) {
    super(bloombergConnector, statistics);
    _cache = new MongoDBReferenceDataCache(mongoConnector, PERMANENT_ERRORS);
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  @Override
  protected void savePermanentErrors(String security, Set<String> permanentFailures) {
    _cache.saveCachedResult(wrap(security, permanentFailures));
  }

  @Override
  protected Map<String, Set<String>> getFailedFields(Set<String> securities) {
    Map<String, PerSecurityReferenceDataResult> results = _cache.loadCachedResults(securities);
    Map<String, Set<String>> ret = new HashMap<String, Set<String>>();
    for (Entry<String, PerSecurityReferenceDataResult> entry : results.entrySet()) {
      ret.put(entry.getKey(), unWrap(entry.getValue()));
    }
    return ret;
  }

  private PerSecurityReferenceDataResult wrap(String security, Set<String> permanentFailures) {
    PerSecurityReferenceDataResult wrapped = new PerSecurityReferenceDataResult(security);
    MutableFudgeMsg fieldData = _fudgeContext.newMessage();
    for (String fieldNames : permanentFailures) {
      fieldData.add(FIELD_PERMANENT_ERROR_NAME, fieldNames);
    }
    wrapped.setFieldData(fieldData);
    return wrapped;
  }

  private Set<String> unWrap(PerSecurityReferenceDataResult value) {
    Set<String> unwrapped = new HashSet<String>();
    for (FudgeField fieldData : value.getFieldData().getAllByName(FIELD_PERMANENT_ERROR_NAME)) {
      unwrapped.add((String) fieldData.getValue());
    }
    return unwrapped;
  }

}
