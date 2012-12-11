/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.view.ViewResultEntry;

/**
 * Fudge message builder for {@link ViewResultEntry}.
 */
@FudgeBuilderFor(ViewResultEntry.class)
public class ViewResultEntryFudgeBuilder implements FudgeBuilder<ViewResultEntry> {

  private static final String CALC_CONFIG_FIELD = "calcConfig";
  private static final String VALUE_FIELD = "value";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ViewResultEntry object) {
    MutableFudgeMsg msg = serializer.newMessage();
    msg.add(CALC_CONFIG_FIELD, object.getCalculationConfiguration());
    serializer.addToMessage(msg, VALUE_FIELD, null, object.getComputedValue());
    return msg;
  }

  @Override
  public ViewResultEntry buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    String calcConfig = msg.getString(CALC_CONFIG_FIELD);
    ComputedValueResult value = deserializer.fieldValueToObject(ComputedValueResult.class, msg.getByName(VALUE_FIELD));
    return new ViewResultEntry(calcConfig, value);
  }

}
