/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import java.util.Set;

import org.fudgemsg.MutableFudgeMsg;

import com.google.common.collect.ImmutableSet;
import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.ArgumentChecker;

/**
 * Multiplies the value of a {@code Double} field by a constant.
 */
public class UnitChange implements NormalizationRule {
  
  private final Set<String> _fields;
  private final double _multiplier;
  
  public UnitChange(String field, double multiplier) {
    ArgumentChecker.notNull(field, "Field name");
    _fields = ImmutableSet.of(field);
    _multiplier = multiplier;        
  }
  
  public UnitChange(Set<String> fields, double multiplier) {
    ArgumentChecker.notNull(fields, "Field names");
    _fields = fields;
    _multiplier = multiplier;
  }
  
  public UnitChange(double multiplier, String... fields) {
    ArgumentChecker.notNull(fields, "fields");
    _fields = ImmutableSet.copyOf(fields);
    _multiplier = multiplier;
  }
  
  @Override
  public MutableFudgeMsg apply(MutableFudgeMsg msg, String securityUniqueId, FieldHistoryStore fieldHistory) {
    return multiplyFields(msg, _fields, _multiplier);
  }

  private static MutableFudgeMsg multiplyFields(MutableFudgeMsg msg, Set<String> fields, double multiplier) {
    for (String field : fields) {
      Double value = msg.getDouble(field);
      if (value != null) {
        double newValue = value * multiplier;
        msg.remove(field);
        msg.add(field, newValue);
      }
    }
    return msg;
  }
  
}
