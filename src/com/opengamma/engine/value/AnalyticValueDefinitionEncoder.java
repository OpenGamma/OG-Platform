/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;

import com.opengamma.util.Pair;

/**
 * 
 *
 * @author kirk
 */
public final class AnalyticValueDefinitionEncoder {
  private AnalyticValueDefinitionEncoder() {
  }
  
  public static FudgeMsg toFudgeMsg(AnalyticValueDefinition<?> definition, FudgeContext fudgeContext) {
    FudgeMsg msg = fudgeContext.newMessage();
    for(String key : definition.getKeys()) {
      for(Object value : definition.getValues(key)) {
        msg.add(key, value);
      }
    }
    return msg;
  }
  
  public static AnalyticValueDefinition<?> fromFudgeMsg(FudgeMsgEnvelope envelope) {
    FudgeMsg msg = envelope.getMessage();
    List<Pair<String, Object>> predicates = new ArrayList<Pair<String, Object>>();
    for(FudgeField field : msg.getAllFields()) {
      predicates.add(new Pair<String, Object>(field.getName(), field.getValue()));
    }
    return new AnalyticValueDefinitionImpl<Object>(predicates);
  }

}
