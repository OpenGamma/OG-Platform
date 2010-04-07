/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import java.util.HashSet;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author pietari
 */
public class FieldFilter implements NormalizationRule {
  
  private final Set<String> _fieldsToAccept;
  private final FudgeContext CONTEXT = FudgeContext.GLOBAL_DEFAULT; 
  
  public FieldFilter(Set<String> fieldsToAccept) {
    ArgumentChecker.checkNotNull(fieldsToAccept, "List of accepted fields");    
    _fieldsToAccept = new HashSet<String>(fieldsToAccept);
  }

  @Override
  public MutableFudgeFieldContainer apply(MutableFudgeFieldContainer msg) {
    MutableFudgeFieldContainer normalizedMsg = CONTEXT.newMessage();
    for (String fieldName : _fieldsToAccept) {
      FudgeField value = msg.getByName(fieldName);
      if (value != null) {
        normalizedMsg.add(value);
      }
    }
    return normalizedMsg;    
  }
  
}
