/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.ArgumentChecker;

/**
 * Multiplies the value of a {@code Double} field by a constant.
 */
public class UnitChange implements NormalizationRule {
  
  private final String _field;
  private final double _multiplier;
  
  public UnitChange(String field, double multiplier) {
    ArgumentChecker.notNull(field, "Field name");
    _field = field;
    _multiplier = multiplier;        
  }
  
  @Override
  public MutableFudgeMsg apply(MutableFudgeMsg msg, String securityUniqueId, FieldHistoryStore fieldHistory) {
    return multiplyField(msg, _field, _multiplier);
  }

  /*package*/ static MutableFudgeMsg multiplyField(MutableFudgeMsg msg, String field, double multiplier) {
    Double value = msg.getDouble(field);
    if (value != null) {
      double newValue = value * multiplier;
      msg.remove(field);
      msg.add(field, newValue);
    }
    return msg;
  }
  
}
