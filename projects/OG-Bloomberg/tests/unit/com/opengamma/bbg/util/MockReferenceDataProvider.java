/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.util;

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
import com.opengamma.bbg.PerSecurityReferenceDataResult;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.ReferenceDataResult;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Mock class.
 */
public class MockReferenceDataProvider implements ReferenceDataProvider {

  private Set<String> _expectedFields = Sets.newHashSet();
  private Map<String, Multimap<String, String>> _mockDataMap = Maps.newHashMap();

  @Override
  public ReferenceDataResult getFields(Set<String> securityKeys, Set<String> fields) {
    if (_expectedFields.size() > 0) {
      for (String field : _expectedFields) {
        assertTrue(fields.contains(field));
      }
    }
    ReferenceDataResult result = new ReferenceDataResult();
    for (String secKey : securityKeys) {
      if (_mockDataMap.containsKey(secKey)) {
        // known security
        PerSecurityReferenceDataResult secRes = new PerSecurityReferenceDataResult(secKey);
        MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
        
        Multimap<String, String> fieldMap = _mockDataMap.get(secKey);
        if (fieldMap != null) {
          // security actually has data
          for (String field : fields) {
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
        secRes.setFieldData(msg);
        result.addResult(secRes);
        
      } else {
        // security wasn't marked as known
        fail("Security not found: " + secKey + " in " + _mockDataMap.keySet());
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
