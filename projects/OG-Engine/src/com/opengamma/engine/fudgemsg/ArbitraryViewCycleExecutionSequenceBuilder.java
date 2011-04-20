/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.List;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;

/**
 * Fudge message builder for {@link ArbitraryViewCycleExecutionSequence}
 */
@FudgeBuilderFor(ArbitraryViewCycleExecutionSequence.class)
public class ArbitraryViewCycleExecutionSequenceBuilder implements FudgeBuilder<ArbitraryViewCycleExecutionSequence> {

  private static final String SEQUENCE_FIELD = "sequence";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, ArbitraryViewCycleExecutionSequence object) {
    MutableFudgeMsg msg = context.newMessage();
    context.addToMessage(msg, SEQUENCE_FIELD, null, object.getRemainingSequence());
    return msg;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ArbitraryViewCycleExecutionSequence buildObject(FudgeDeserializationContext context, FudgeMsg msg) {
    List<ViewCycleExecutionOptions> sequence = context.fieldValueToObject(List.class, msg.getByName(SEQUENCE_FIELD));
    return new ArbitraryViewCycleExecutionSequence(sequence);
  }

}
