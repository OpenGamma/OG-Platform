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
import java.util.TreeSet;

import org.fudgemsg.FudgeContext;

import com.bloomberglp.blpapi.SessionOptions;
import com.google.common.collect.Sets;
import com.opengamma.bbg.BloombergReferenceDataProvider;
import com.opengamma.bbg.ErrorInfo;
import com.opengamma.bbg.PerSecurityReferenceDataResult;
import com.opengamma.bbg.ReferenceDataResult;
import com.opengamma.bbg.referencedata.statistics.BloombergReferenceDataStatistics;

/**
 * A {@link BloombergReferenceDataProvider} which avoids requerying fields for which permanent errors have been observed.
 * BBG-72 
 */
public abstract class AbstractPermanentErrorCachingReferenceDataProvider extends BloombergReferenceDataProvider {
  //TODO: this is quite similar to the AbstractCachingReferenceDataProvider

  
  protected AbstractPermanentErrorCachingReferenceDataProvider(SessionOptions sessionOptions,
      BloombergReferenceDataStatistics statistics) {
    super(sessionOptions, statistics);
  }

  @Override
  public ReferenceDataResult getFields(Set<String> securities, Set<String> fields) {
    Map<String, Set<String>> failedFieldsMap = getFailedFields(securities);

    ReferenceDataResult result = new ReferenceDataResult();
    Map<Set<String>, Set<String>> requiredFields = determineSecuritiesForFieldSets(securities, fields, failedFieldsMap);
    for (Entry<Set<String>, Set<String>> group : requiredFields.entrySet()) {
      if (group.getKey().isEmpty()) {
        for (String security : group.getValue()) {
          PerSecurityReferenceDataResult emptyResult = new PerSecurityReferenceDataResult(security);
          emptyResult.setFieldData(FudgeContext.EMPTY_MESSAGE);
          result.addResult(emptyResult);
        }
      } else {
        ReferenceDataResult underlying = super.getFields(group.getValue(), group.getKey());
        for (String security : underlying.getSecurities()) {
          PerSecurityReferenceDataResult secResult = underlying.getResult(security);
          saveExceptions(security, secResult.getFieldExceptions(), failedFieldsMap.get(security));
          result.addResult(secResult);
        }
      }
    }

    return result;
  }

  private Map<Set<String>, Set<String>> determineSecuritiesForFieldSets(Set<String> securities, Set<String> fields, Map<String, Set<String>> failedFieldsMap) {
    Map<Set<String>, Set<String>> result = new HashMap<Set<String>, Set<String>>();
    for (String securityDes : securities) {
      Set<String> failedFields = failedFieldsMap.get(securityDes);
      Set<String> missingFields = null;
      if (failedFields == null) {
        missingFields = fields;
      } else {
        missingFields = Sets.difference(fields, failedFields);
      }
      Set<String> securitiesMatchingFields = result.get(missingFields);
      if (securitiesMatchingFields == null) {
        securitiesMatchingFields = new TreeSet<String>();
        result.put(missingFields, securitiesMatchingFields);
      }
      securitiesMatchingFields.add(securityDes);
    }
    return result;
  }

  private void saveExceptions(String security, Map<String, ErrorInfo> fieldExceptions, Set<String> knownFailures) {
    Set<String> permanentFailures = new HashSet<String>();
    for (Entry<String, ErrorInfo> entry : fieldExceptions.entrySet()) {
      if (isPermanent(entry.getValue())) {
        permanentFailures.add(entry.getKey());
      }
    }
    if (permanentFailures.isEmpty()) {
      return;
    }
    if (knownFailures != null) {
      permanentFailures.addAll(knownFailures);
    }
    savePermanentErrors(security, permanentFailures);
  }

  
  protected abstract void savePermanentErrors(String security, Set<String> permanentFailures);
  protected abstract Map<String, Set<String>> getFailedFields(Set<String> securities);
  
  private boolean isPermanent(ErrorInfo value) {
    //BBG-72
    return "BAD_FLD".equals(value.getCategory()) && "NOT_APPLICABLE_TO_REF_DATA".equals(value.getSubcategory());
  }
}
