/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A decorator for a ReferenceDataProvider that allows you to override the results
 * from the underlying provider e.g. if you have extra information from another source.
 */
public class PatchableReferenceDataProvider extends AbstractReferenceDataProvider {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(PatchableReferenceDataProvider.class);

  private Map<Pair<String, String>, Object> _patches = new HashMap<Pair<String, String>, Object>();
  private Set<String> _securities = new HashSet<String>();
  private ReferenceDataProvider _underlying;

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying source of reference data
   */
  public PatchableReferenceDataProvider(ReferenceDataProvider underlying) {
    _underlying = underlying;
  }

  //-------------------------------------------------------------------------
  /**
   * Sets an override or replacement value.
   * 
   * @param security  the Bloomberg security identifier
   * @param field  the Bloomberg field name
   * @param result  the object to return as a result (must be possible to Fudge encode with standard OG dictionary)
   */
  public void setPatch(String security, String field, Object result) {
    _patches.put(Pairs.of(security, field), result);
    _securities.add(security);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ReferenceDataProviderGetResult doBulkGet(ReferenceDataProviderGetRequest request) {
    ReferenceDataProviderGetResult rawResult = _underlying.getReferenceData(request);
    ReferenceDataProviderGetResult newResult = new ReferenceDataProviderGetResult();
    
    for (ReferenceData refData : rawResult.getReferenceData()) {
      String identifier = refData.getIdentifier();
      if (_securities.contains(identifier)) {
        FudgeMsg fieldData = refData.getFieldValues();
        MutableFudgeMsg alteredFieldData = OpenGammaFudgeContext.getInstance().newMessage(fieldData);
        for (String field : request.getFields()) {
          if (_patches.containsKey(Pairs.of(identifier, field))) {
            if (alteredFieldData.hasField(field)) {
              alteredFieldData.remove(field);
            }
            alteredFieldData.add(field, _patches.get(Pairs.of(identifier, field)));
            refData.removeErrors(field);
          }
        }
        s_logger.debug("Patching {} with {}", new Object[] {fieldData, alteredFieldData });
        refData.setFieldValues(alteredFieldData);
      }
      newResult.addReferenceData(refData);
    }
    return newResult;
  }

}
