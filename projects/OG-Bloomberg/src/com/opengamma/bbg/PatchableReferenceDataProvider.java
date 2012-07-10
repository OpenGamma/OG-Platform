/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.tuple.Pair;

/**
 * A decorator for a ReferenceDataProvider that allows you to override the results from the underlying provider e.g. if you have extra information from another source.
 */
public class PatchableReferenceDataProvider implements ReferenceDataProvider {
  private Map<Pair<String, String>, Object> _patches = new HashMap<Pair<String, String>, Object>();
  private Set<String> _securities = new HashSet<String>();
  private ReferenceDataProvider _underlying;

  /**
   * Constructor to create patchable reference data provider
   * @param underlying the underlying source of reference data
   */
  public PatchableReferenceDataProvider(ReferenceDataProvider underlying) {
    _underlying = underlying;
  }
  
  /**
   * set an override or replacement value for 
   * @param security the Bloomberg security identifier
   * @param field the Bloomberg field name
   * @param result the object to return as a result (must be possible to Fudge encode with standard OG dictionary)
   */
  public void setPatch(String security, String field, Object result) {
    _patches.put(Pair.of(security, field), result);
    _securities.add(security);
  }
  
  @Override
  public ReferenceDataResult getFields(Set<String> securities, Set<String> fields) {
    ReferenceDataResult rawResult = _underlying.getFields(securities, fields);
    for (String security : securities) {
      PerSecurityReferenceDataResult result = rawResult.getResult(security);
      if (_securities.contains(security)) {
        FudgeMsg fieldData = result.getFieldData();
        MutableFudgeMsg alteredFieldData = OpenGammaFudgeContext.getInstance().newMessage(fieldData);
        for (String field : fields) {
          if (_patches.containsKey(Pair.of(security, field))) {
            if (result != null) {
              if (alteredFieldData.hasField(field)) {
                alteredFieldData.remove(field);
              }
              alteredFieldData.add(field, _patches.get(Pair.of(security, field)));
              result.getFieldExceptions().remove(field);
            }
          }
        }
        result.setFieldData(alteredFieldData);
      }
    }
    return null;
  }

}
