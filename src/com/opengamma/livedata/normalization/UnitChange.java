/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author pietari
 */
public class UnitChange implements NormalizationRule {
  
  private final String _field;
  private final double _multiplier;
  
  public UnitChange(String field, double multiplier) {
    ArgumentChecker.checkNotNull(field, "Field name");
    _field = field;
    _multiplier = multiplier;        
  }
  
  @Override
  public void apply(MutableFudgeFieldContainer msg) {
    // TODO
  }

}
