/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.MutableFudgeMsg;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.opengamma.bbg.referencedata.impl.AbstractReferenceDataProvider;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Mock class.
 */
public class MockReferenceDataProvider extends AbstractReferenceDataProvider {

  private Set<String> _expectedFields = Sets.newHashSet();
  private Map<String, Multimap<String, String>> _mockDataMap = Maps.newHashMap();

  @Override
  protected ReferenceDataProviderGetResult doBulkGet(ReferenceDataProviderGetRequest request) {
    if (_expectedFields.size() > 0) {
      for (String field : _expectedFields) {
        assertTrue(request.getFields().contains(field));
      }
    }
    ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
    for (String identifier : request.getIdentifiers()) {
      if (_mockDataMap.containsKey(identifier)) {
        // known security
        ReferenceData refData = new ReferenceData(identifier);
        MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
        
        Multimap<String, String> fieldMap = _mockDataMap.get(identifier);
        if (fieldMap != null) {
          // security actually has data
          for (String field : request.getFields()) {
            Collection<String> values = fieldMap.get(field);
            assertTrue("Field not found: " + field + " in " + fieldMap.keySet(), values.size() > 0);
            assertNotNull(values);
            for (String value : values) {
              if (value != null) {
                if (value.contains("=")) {
                  MutableFudgeMsg submsg = OpenGammaFudgeContext.getInstance().newMessage();
                  submsg.add(StringUtils.substringBefore(value, "="), StringUtils.substringAfter(value, "="));
                  msg.add(field, submsg);
                } else {
                  msg.add(field, value);
                }
              }
            }
          }
        }
        refData.setFieldValues(msg);
        result.addReferenceData(refData);
        
      } else {
        // security wasn't marked as known
        fail("Security not found: " + identifier + " in " + _mockDataMap.keySet());
      }
    }
    return result;
  }

  public void addExpectedField(String field) {
    _expectedFields.add(field);
  }

  public void addResult(String securityKey, String field, String value) {
    if (field == null) {
      // security is known and normal (empty) result returned
      _mockDataMap.put(securityKey, null);
    } else {
      // security is known and normal data is stored
      Multimap<String, String> baseMap = _mockDataMap.get(securityKey);
      if (baseMap == null) {
        baseMap = ArrayListMultimap.create();
        _mockDataMap.put(securityKey, baseMap);
      }
      baseMap.put(field, value);
    }
  }

}
