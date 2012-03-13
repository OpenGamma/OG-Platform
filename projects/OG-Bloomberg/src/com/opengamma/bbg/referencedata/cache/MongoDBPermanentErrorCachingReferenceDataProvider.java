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
import com.opengamma.bbg.AbstractCachingReferenceDataProvider;
import com.opengamma.bbg.PerSecurityReferenceDataResult;
import com.opengamma.bbg.referencedata.statistics.BloombergReferenceDataStatistics;
import com.opengamma.bbg.referencedata.statistics.NullBloombergReferenceDataStatistics;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.mongo.MongoConnector;

/**
 * A {@link AbstractCachingReferenceDataProvider} which caches the data using a {@link MongoDBReferenceDataCache} 
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
  private final MongoDBReferenceDataCache _cache;
  private final FudgeContext _fudgeContext;

  /**
   * Creates an instance.
   * 
   * @param sessionOptions  the session options, not null
   * @param connector  the Mongo connector, not null
   */
  public MongoDBPermanentErrorCachingReferenceDataProvider(SessionOptions sessionOptions, MongoConnector connector) {
    this(sessionOptions, connector, NullBloombergReferenceDataStatistics.INSTANCE);
  }
  /**
   * Creates an instance.
   * 
   * @param sessionOptions  the session options, not null
   * @param connector  the Mongo connector, not null
   * @param statistics the statistics to record
   */
  public MongoDBPermanentErrorCachingReferenceDataProvider(SessionOptions sessionOptions, MongoConnector connector, BloombergReferenceDataStatistics statistics) {
    this(sessionOptions, connector, statistics, OpenGammaFudgeContext.getInstance());
  }

  /**
   * Creates an instance.
   * 
   * @param sessionOptions  the session options, not null
   * @param connector  the Mongo connector, not null
   * @param statistics the statistics to record
   * @param fudgeContext  the Fudge context, not null
   */
  public MongoDBPermanentErrorCachingReferenceDataProvider(SessionOptions sessionOptions, MongoConnector connector, BloombergReferenceDataStatistics statistics, FudgeContext fudgeContext) {
    super(sessionOptions, statistics);
    _cache = new MongoDBReferenceDataCache(connector, PERMANENT_ERRORS);
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
