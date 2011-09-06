/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.view.DeltaComparer;
import com.opengamma.engine.view.DeltaDefinition;

/**
 * Fudge message builder for {@code DeltaDefinition}.
 */
@FudgeBuilderFor(DeltaDefinition.class)
public class DeltaDefinitionFudgeBuilder implements FudgeBuilder<DeltaDefinition> {
  /**
   * Fudge field name.
   */
  private static final String NUMBER_COMPARER_KEY = "numberComparer";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, DeltaDefinition object) {
    MutableFudgeMsg msg = serializer.newMessage();
    if (object.getNumberComparer() != null) {
      serializer.addToMessageWithClassHeaders(msg, NUMBER_COMPARER_KEY, null, object.getNumberComparer(), DeltaComparer.class);
    }
    return msg;
  }

  @SuppressWarnings("unchecked")
  @Override
  public DeltaDefinition buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    FudgeField fudgeField = message.getByName(NUMBER_COMPARER_KEY);
    DeltaDefinition deltaDefinition = new DeltaDefinition();
    if (fudgeField != null) {
      deltaDefinition.setNumberComparer(deserializer.fieldValueToObject(DeltaComparer.class, fudgeField));
    }
    return deltaDefinition;
  }

}
