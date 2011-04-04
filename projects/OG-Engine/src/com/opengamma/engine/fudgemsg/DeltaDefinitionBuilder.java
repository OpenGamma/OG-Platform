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
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.DeltaComparer;
import com.opengamma.engine.view.DeltaDefinition;

/**
 * Fudge message builder for {@code DeltaDefinition}.
 */
@FudgeBuilderFor(DeltaDefinition.class)
public class DeltaDefinitionBuilder implements FudgeBuilder<DeltaDefinition> {
  /**
   * Fudge field name.
   */
  private static final String NUMBER_COMPARER_KEY = "numberComparer";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, DeltaDefinition object) {
    MutableFudgeMsg msg = context.newMessage();
    if (object.getNumberComparer() != null) {
      context.addToMessageWithClassHeaders(msg, NUMBER_COMPARER_KEY, null, object.getNumberComparer(), DeltaComparer.class);
    }
    return msg;
  }

  @SuppressWarnings("unchecked")
  @Override
  public DeltaDefinition buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    FudgeField fudgeField = message.getByName(NUMBER_COMPARER_KEY);
    DeltaDefinition deltaDefinition = new DeltaDefinition();
    if (fudgeField != null) {
      deltaDefinition.setNumberComparer(context.fieldValueToObject(DeltaComparer.class, fudgeField));
    }
    return deltaDefinition;
  }

}
