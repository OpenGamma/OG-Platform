/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;

/**
 * Fudge message builder for {@link ComputationTarget}.
 */
@FudgeBuilderFor(ComputationTarget.class)
public class ComputationTargetBuilder implements FudgeBuilder<ComputationTarget> {

  private static final String TYPE_FIELD = "type";
  private static final String VALUE_FIELD = "value";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, ComputationTarget object) {
    MutableFudgeMsg msg = context.newMessage();
    context.addToMessage(msg, TYPE_FIELD, null, object.getType());
    context.addToMessageWithClassHeaders(msg, VALUE_FIELD, null, object.getValue());
    return msg;
  }

  @Override
  public ComputationTarget buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    ComputationTargetType type = context.fieldValueToObject(ComputationTargetType.class, message.getByName(TYPE_FIELD));
    Object value = context.fieldValueToObject(message.getByName(VALUE_FIELD));
    return new ComputationTarget(type, value);
  }

}
