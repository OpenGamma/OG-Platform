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
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.execution.RealTimeViewCycleExecutionSequence;

/**
 * Fudge message builder for {@link RealTimeViewCycleExecutionSequence}
 */
@FudgeBuilderFor(RealTimeViewCycleExecutionSequence.class)
public class RealTimeViewCycleExecutionSequenceBuilder implements FudgeBuilder<RealTimeViewCycleExecutionSequence> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, RealTimeViewCycleExecutionSequence object) {
    return context.newMessage();
  }

  @Override
  public RealTimeViewCycleExecutionSequence buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    return new RealTimeViewCycleExecutionSequence();
  }

}
