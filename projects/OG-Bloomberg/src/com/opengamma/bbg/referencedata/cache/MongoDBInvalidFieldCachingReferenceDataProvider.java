/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.MutableFudgeMsg;

import com.google.common.collect.Maps;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.mongo.MongoConnector;

/**
 * Decorates a reference data provider, adding caching of invalid field errors.
 * <p>
 * The cache is implemented using the a Mongo database.
 */
public class MongoDBInvalidFieldCachingReferenceDataProvider extends AbstractInvalidFieldCachingReferenceDataProvider {

  /**
   * The Mongo collection name.
   */
  private static final String PERMANENT_ERRORS = "invalidfields";
  /**
   * The Fudge field name.
   */
  private static final String FIELD_PERMANENT_ERROR_NAME = "PERMANENT_ERROR";

  /**
   * The cache.
   */
  private final MongoDBReferenceDataCache _cache;
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying provider, not null
   * @param mongoConnector  the Mongo connector, not null
   */
  public MongoDBInvalidFieldCachingReferenceDataProvider(ReferenceDataProvider underlying, MongoConnector mongoConnector) {
    this(underlying, mongoConnector, OpenGammaFudgeContext.getInstance());
  }

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying provider, not null
   * @param mongoConnector  the Mongo connector, not null
   * @param fudgeContext  the Fudge context, not null
   */
  public MongoDBInvalidFieldCachingReferenceDataProvider(
      ReferenceDataProvider underlying,
      MongoConnector mongoConnector,
      FudgeContext fudgeContext) {
    super(underlying);
    ArgumentChecker.notNull(mongoConnector, "mongoConnector");
    _cache = new MongoDBReferenceDataCache(mongoConnector, PERMANENT_ERRORS);
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  @Override
  protected void saveInvalidFields(String identifier, Set<String> invalidFields) {
    _cache.save(wrap(identifier, invalidFields));
  }

  // wrap/unwrap of data here is rather weird...
  private ReferenceData wrap(String security, Set<String> permanentFailures) {
    ReferenceData wrapped = new ReferenceData(security);
    MutableFudgeMsg fieldData = _fudgeContext.newMessage();
    for (String fieldNames : permanentFailures) {
      fieldData.add(FIELD_PERMANENT_ERROR_NAME, fieldNames);
    }
    wrapped.setFieldValues(fieldData);
    return wrapped;
  }

  //-------------------------------------------------------------------------
  @Override
  protected Map<String, Set<String>> loadInvalidFields(Set<String> identifiers) {
    Map<String, ReferenceData> results = _cache.load(identifiers);
    Map<String, Set<String>> result = Maps.newHashMap();
    for (Entry<String, ReferenceData> entry : results.entrySet()) {
      result.put(entry.getKey(), unwrap(entry.getValue()));
    }
    return result;
  }

  private Set<String> unwrap(ReferenceData value) {
    Set<String> unwrapped = new HashSet<String>();
    for (FudgeField fieldData : value.getFieldValues().getAllByName(FIELD_PERMANENT_ERROR_NAME)) {
      unwrapped.add((String) fieldData.getValue());
    }
    return unwrapped;
  }

}
