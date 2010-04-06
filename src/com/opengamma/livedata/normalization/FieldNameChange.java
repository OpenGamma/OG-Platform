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
public class FieldNameChange implements NormalizationRule {
  
  private final String _from;
  private final String _to;
  
  public FieldNameChange(String from, String to) {
    ArgumentChecker.checkNotNull(from, "From");
    ArgumentChecker.checkNotNull(to, "To");
    _from = from;
    _to = to;
  }
  
  @Override
  public void apply(MutableFudgeFieldContainer msg) {
    // TODO
  }

}
