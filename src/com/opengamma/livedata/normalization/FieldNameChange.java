/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import org.fudgemsg.FudgeField;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.livedata.server.FieldHistoryStore;
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
    ArgumentChecker.notNull(from, "From");
    ArgumentChecker.notNull(to, "To");
    _from = from;
    _to = to;
  }
  
  @Override
  public MutableFudgeFieldContainer apply(
      MutableFudgeFieldContainer msg,
      FieldHistoryStore fieldHistory) {
    
    FudgeField field = msg.getByName(_from);
    if (field != null) {
      Object value = field.getValue();
      msg.remove(_from);
      msg.add(_to, value);
    }
    return msg;
    
  }

}
