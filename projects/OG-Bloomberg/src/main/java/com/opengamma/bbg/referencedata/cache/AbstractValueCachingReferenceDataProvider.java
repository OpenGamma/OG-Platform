/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataError;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.bbg.referencedata.impl.AbstractReferenceDataProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Abstract reference data provider decorator that caches field values.
 * <p>
 * It is recommended to use a cache over the underlying provider to avoid excess queries on Bloomberg.
 */
public abstract class AbstractValueCachingReferenceDataProvider extends AbstractReferenceDataProvider {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractValueCachingReferenceDataProvider.class);
  /**
   * Constant used when field not available.
   */
  private static final String FIELD_NOT_AVAILABLE_NAME = "NOT_AVAILABLE_FIELD";

  /**
   * The underlying provider.
   */
  private final ReferenceDataProvider _underlying;
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying provider, not null
   */
  protected AbstractValueCachingReferenceDataProvider(ReferenceDataProvider underlying) {
    this(underlying, OpenGammaFudgeContext.getInstance());
  }

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying provider, not null
   * @param fudgeContext  the Fudge context, not null
   */
  protected AbstractValueCachingReferenceDataProvider(final ReferenceDataProvider underlying, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying provider.
   * 
   * @return the underlying provider, not null
   */
  public ReferenceDataProvider getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the Fudge context.
   * 
   * @return the context, not null
   */
  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  //-------------------------------------------------------------------------
  @Override
  protected ReferenceDataProviderGetResult doBulkGet(ReferenceDataProviderGetRequest request) {
    // if use-cache is false, then do not cache
    if (request.isUseCache() == false) {
      return getUnderlying().getReferenceData(request);
    }
    
    // load from cache
    Map<String, ReferenceData> cachedResults = loadFieldValues(request.getIdentifiers());
    
    // filter the request removing known invalid fields
    final Map<Set<String>, Set<String>> identifiersByFields = buildUnderlyingRequestGroups(request, cachedResults);
    
    // process everything that remains
    ReferenceDataProviderGetResult resolvedResults = loadAndPersistUnknownFields(cachedResults, identifiersByFields);
    resolvedResults = stripUnwantedFields(resolvedResults, request.getFields());
    return resolvedResults;
  }

  protected ReferenceDataProviderGetResult stripUnwantedFields(final ReferenceDataProviderGetResult resolvedResults, final Set<String> fields) {
    ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
    for (ReferenceData unstippedDataResult : resolvedResults.getReferenceData()) {
      String identifier = unstippedDataResult.getIdentifier();
      ReferenceData strippedDataResult = new ReferenceData(identifier);
      strippedDataResult.getErrors().addAll(unstippedDataResult.getErrors());
      MutableFudgeMsg strippedFields = getFudgeContext().newMessage();
      FudgeMsg unstrippedFieldData = unstippedDataResult.getFieldValues();
      // check requested fields
      for (String requestField : fields) {
        List<FudgeField> fudgeFields = unstrippedFieldData.getAllByName(requestField);
        for (FudgeField fudgeField : fudgeFields) {
          strippedFields.add(requestField, fudgeField.getValue());
        }
      }
      strippedDataResult.setFieldValues(strippedFields);
      result.addReferenceData(strippedDataResult);
    }
    return result;
  }

  protected ReferenceDataProviderGetResult loadAndPersistUnknownFields(
      Map<String, ReferenceData> cachedResults,
      Map<Set<String>, Set<String>> identifiersByFields) {
    
    // TODO kirk 2009-10-23 -- Also need to maintain securities we don't need to put back in the database.
    ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
    // REVIEW kirk 2009-10-23 -- Candidate for scatter/gather.
    for (Map.Entry<Set<String>, Set<String>> entry : identifiersByFields.entrySet()) {
      Set<String> requestedIdentifiers = entry.getValue();
      Set<String> requestedFields = entry.getKey();
      assert !requestedIdentifiers.isEmpty();
      if (entry.getKey().isEmpty()) {
        s_logger.debug("Satisfied entire request for securities {} from cache", requestedIdentifiers);
        for (String securityKey : requestedIdentifiers) {
          result.addReferenceData(cachedResults.get(securityKey));
        }
        continue;
      }
      s_logger.info("Loading {} fields for {} securities from underlying", entry.getKey().size(), requestedIdentifiers.size());
      final ReferenceDataProviderGetRequest underlyingRequest = ReferenceDataProviderGetRequest.createGet(requestedIdentifiers, requestedFields, false);
      ReferenceDataProviderGetResult loadedResult = getUnderlying().getReferenceData(underlyingRequest);
      for (String identifier : requestedIdentifiers) {
        ReferenceData cachedResult = cachedResults.get(identifier);
        ReferenceData freshResult = loadedResult.getReferenceDataOrNull(identifier);
        freshResult = (freshResult != null ? freshResult : new ReferenceData(identifier));
        
        ReferenceData resolvedResult = getCombinedResult(requestedFields, cachedResult, freshResult);
        saveFieldValues(resolvedResult);
        result.addReferenceData(resolvedResult);
      }
    }
    return result;
  }

  private ReferenceData getCombinedResult(Set<String> requestedFields, ReferenceData cachedResult, ReferenceData freshResult) {
    MutableFudgeMsg unionFieldData = null;
    if (cachedResult == null) {
      unionFieldData = getFudgeContext().newMessage();
    } else {
      unionFieldData = getFudgeContext().newMessage(cachedResult.getFieldValues());
    }
    Set<String> returnedFields = new HashSet<String>();
    for (FudgeField freshField : freshResult.getFieldValues().getAllFields()) {
      unionFieldData.add(freshField);
      returnedFields.add(freshField.getName());
    }
    
    // cache not available fields as well
    Set<String> notAvaliableFields = Sets.newTreeSet(requestedFields);
    notAvaliableFields.removeAll(returnedFields);
    
    // add list of not available fields
    for (String notAvailableField : notAvaliableFields) {
      unionFieldData.add(FIELD_NOT_AVAILABLE_NAME, notAvailableField);
    }
    
    // create combined result
    ReferenceData resolvedResult = new ReferenceData(freshResult.getIdentifier(), unionFieldData);
    for (ReferenceDataError error : freshResult.getErrors()) {
      if (resolvedResult.getErrors().contains(error) == false) {
        resolvedResult.getErrors().add(error);          
      }
    }
    return resolvedResult;
  }

  /**
   * Examines and groups the request using the known invalid fields.
   * 
   * @param request  the request, not null
   * @param cachedResults  the cached results, keyed by identifier, not null
   * @return the map of field-set to identifier-set, not null
   */
  protected Map<Set<String>, Set<String>> buildUnderlyingRequestGroups(ReferenceDataProviderGetRequest request, Map<String, ReferenceData> cachedResults) {
    Map<Set<String>, Set<String>> result = Maps.newHashMap();
    for (String identifier : request.getIdentifiers()) {
      // select known invalid fields for the identifier
      ReferenceData cachedResult = cachedResults.get(identifier);
      
      // calculate the missing fields that must be queried from the underlying
      Set<String> missingFields = null;
      if (cachedResult == null) {
        missingFields = Sets.newHashSet(request.getFields());
      } else {
        missingFields = Sets.newHashSet(Sets.difference(request.getFields(), cachedResult.getFieldValues().getAllFieldNames()));
        // remove known not available fields from missingFields
        List<String> notAvailableFieldNames = getNotAvailableFields(cachedResult);
        for (String field : notAvailableFieldNames) {
          missingFields.remove(field);
        }
      }
      
      // build the grouped result map, keyed from field-set to identifier-set
      Set<String> resultIdentifiers = result.get(missingFields);
      if (resultIdentifiers == null) {
        resultIdentifiers = Sets.newTreeSet();
        result.put(missingFields, resultIdentifiers);
      }
      resultIdentifiers.add(identifier);
    }
    return result;
  }

  private List<String> getNotAvailableFields(ReferenceData cachedResult) {
    List<FudgeField> notAvailableFields = cachedResult.getFieldValues().getAllByName(FIELD_NOT_AVAILABLE_NAME);
    List<String> notAvailableFieldNames = new ArrayList<String>(notAvailableFields.size());
    for (FudgeField field : notAvailableFields) {
      notAvailableFieldNames.add((String) field.getValue());
    }
    return notAvailableFieldNames;
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the field values from the cache.
   * 
   * @param identifiers  the identifiers to find errors for, not null
   * @return the map of reference data keyed by identifier, not null
   */
  protected abstract Map<String, ReferenceData> loadFieldValues(Set<String> identifiers);

  /**
   * Saves the field value into the cache.
   * 
   * @param result  the result to save, not null
   */
  protected abstract void saveFieldValues(ReferenceData result);

  //-------------------------------------------------------------------------
  /**
   * Refreshes the cache.
   * 
   * @param identifiers  the identifiers, not null
   */
  public void refresh(Set<String> identifiers) {
    // TODO bulk queries
    Map<String, ReferenceData> cachedResults = loadFieldValues(identifiers);
    
    Map<Set<String>, Set<String>> identifiersByFields = Maps.newHashMap();
    
    for (String identifier : identifiers) {
      ReferenceData cachedResult = cachedResults.get(identifier);
      if (cachedResult == null) {
        continue; // nothing to refresh
      }
      Set<String> fields = new HashSet<String>();
      fields.addAll(cachedResult.getFieldValues().getAllFieldNames());
      fields.addAll(getNotAvailableFields(cachedResult));
      fields.remove(FIELD_NOT_AVAILABLE_NAME);
      Set<String> secsForTheseFields = identifiersByFields.get(fields);
      if (secsForTheseFields == null) {
        secsForTheseFields = new HashSet<String>();
        identifiersByFields.put(fields, secsForTheseFields);
      }
      secsForTheseFields.add(identifier);
    }
    
    for (Entry<Set<String>, Set<String>> entry : identifiersByFields.entrySet()) {
      Set<String> identifiersForTheseFields = entry.getValue();
      Set<String> fields = entry.getKey();
      
      ReferenceDataProviderGetRequest underlyingRequest = ReferenceDataProviderGetRequest.createGet(identifiersForTheseFields, fields, false);
      ReferenceDataProviderGetResult underlyingResult = _underlying.getReferenceData(underlyingRequest);
      for (ReferenceData refData : underlyingResult.getReferenceData()) {
        ReferenceData previousResult = cachedResults.get(refData.getIdentifier());
        ReferenceData resolvedResult = getCombinedResult(fields, new ReferenceData(refData.getIdentifier()), refData);
        if (differentCachedResult(previousResult, resolvedResult)) {
          saveFieldValues(resolvedResult);
        }
      }
    }
  }

  private boolean differentCachedResult(ReferenceData previousResult, ReferenceData resolvedResult) {
    if (previousResult.getIdentifier().equals(resolvedResult.getIdentifier()) == false) {
      throw new OpenGammaRuntimeException("Attempting to compare two different securities " + previousResult + " " + resolvedResult);
    }
    // TODO better, non ordered comparison
    if (previousResult.getFieldValues().toString().equals(resolvedResult.getFieldValues().toString())) {
      return false;
    }
    return true;
  }

}
