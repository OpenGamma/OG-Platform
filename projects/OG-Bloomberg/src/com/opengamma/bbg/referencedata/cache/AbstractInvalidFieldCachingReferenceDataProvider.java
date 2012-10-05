/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataError;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.bbg.referencedata.impl.AbstractReferenceDataProvider;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract reference data provider decorator that caches permanent invalid field errors.
 * <p>
 * It is strongly recommended to always decorate the underlying provider with a permanent
 * invalid field error caching provider. This avoids excess queries to Bloomberg.
 */
public abstract class AbstractInvalidFieldCachingReferenceDataProvider extends AbstractReferenceDataProvider {
  // See BBG-72 

  /**
   * The underlying provider.
   */
  private final ReferenceDataProvider _underlying;

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying provider, not null
   */
  protected AbstractInvalidFieldCachingReferenceDataProvider(ReferenceDataProvider underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
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

  //-------------------------------------------------------------------------
  @Override
  protected ReferenceDataProviderGetResult doBulkGet(ReferenceDataProviderGetRequest request) {
    // this implementation always caches and ignore the use-cache flag in the request
    // this is because the errors here really are permanent and there is no reason to avoid the cache
    
    // load invalid fields from cache
    final Map<String, Set<String>> cachedErrorsMap = loadInvalidFields(request.getIdentifiers());
    
    // filter the request removing known invalid fields
    final Map<Set<String>, Set<String>> requiredFields = buildUnderlyingRequestGroups(request, cachedErrorsMap);
    
    // process everything that remains
    final ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
    for (Entry<Set<String>, Set<String>> group : requiredFields.entrySet()) {
      if (group.getKey().isEmpty()) {
        // all fields for these identifiers are invalid
        for (String identifier : group.getValue()) {
          result.addReferenceData(new ReferenceData(identifier));
        }
      } else {
        // call the underlying with the filtered subset of identifiers and fields
        final ReferenceDataProviderGetRequest underlyingRequest = ReferenceDataProviderGetRequest.createGet(group.getValue(), group.getKey(), false);
        final ReferenceDataProviderGetResult underlyingResult = getUnderlying().getReferenceData(underlyingRequest);
        for (ReferenceData refData : underlyingResult.getReferenceData()) {
          String identifier = refData.getIdentifier();
          checkAndSaveInvalidFields(refData, cachedErrorsMap.get(identifier));
          result.addReferenceData(refData);
        }
      }
    }
    return result;
  }

  /**
   * Examines and groups the request using the known invalid fields.
   * 
   * @param request  the request, not null
   * @param invalidFieldsByIdentifier  the invalid fields, keyed by identifier, not null
   * @return the map of field-set to identifier-set, not null
   */
  protected Map<Set<String>, Set<String>> buildUnderlyingRequestGroups(ReferenceDataProviderGetRequest request, Map<String, Set<String>> invalidFieldsByIdentifier) {
    Map<Set<String>, Set<String>> result = Maps.newHashMap();
    for (String identifier : request.getIdentifiers()) {
      // select known invalid fields for the identifier
      Set<String> invalidFields = invalidFieldsByIdentifier.get(identifier);
      
      // calculate the missing fields that must be queried from the underlying
      Set<String> missingFields = null;
      if (invalidFields == null) {
        missingFields = Sets.newHashSet(request.getFields());
      } else {
        missingFields = Sets.difference(request.getFields(), invalidFields);
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

  /**
   * Checks the reference data and adds any extra permanent errors to the cache.
   * 
   * @param refData  the reference data to check, not null
   * @param invalidFields  the previously cached invalid fields for the identifier, may be null
   */
  protected void checkAndSaveInvalidFields(ReferenceData refData, Set<String> invalidFields) {
    // find all the new invalid fields
    final Set<String> newPermanentErrors = Sets.newHashSet();
    for (ReferenceDataError error : refData.getErrors()) {
      if (isPermanent(error)) {
        newPermanentErrors.add(error.getField());
      }
    }
    
    // save the new invalid fields
    if (newPermanentErrors.size() > 0) {
      if (invalidFields != null) {
        newPermanentErrors.addAll(invalidFields);
      }
      saveInvalidFields(refData.getIdentifier(), newPermanentErrors);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Finds the fields which previously failed for the specified identifiers.
   * <p>
   * This loads from the cache.
   * 
   * @param identifiers  the identifiers to find errors for, not null
   * @return the map of invalid fields keyed by identifier, not null
   */
  protected abstract Map<String, Set<String>> loadInvalidFields(Set<String> identifiers);

  /**
   * Saves the permanent errors into the cache.
   * <p>
   * This stores into the cache.
   * 
   * @param identifier  the identifier to save errors for, not null
   * @param invalidFields  the invalid fields, not null
   */
  protected abstract void saveInvalidFields(String identifier, Set<String> invalidFields);

  //-------------------------------------------------------------------------
  /**
   * Checks whether the specified error is permanent or not.
   * 
   * @param error  the error object, not null
   * @return true if error is permanent
   */
  protected boolean isPermanent(ReferenceDataError error) {
    return error.isFieldBased() && "BAD_FLD".equals(error.getCategory()) &&
          "NOT_APPLICABLE_TO_REF_DATA".equals(error.getSubcategory());
  }

}
