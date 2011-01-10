/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.ArgumentChecker;

/**
 * Multiplies the value of a {@code Double} field by a constant.
 *
 * @author pietari
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
  public MutableFudgeFieldContainer apply(
      MutableFudgeFieldContainer msg,
      FieldHistoryStore fieldHistory) {
    
    Double value = msg.getDouble(_field);
    if (value != null) {
      double newValue = value * _multiplier;
      msg.remove(_field);
      msg.add(_field, newValue);
    }
    return msg;
    
  }

}
