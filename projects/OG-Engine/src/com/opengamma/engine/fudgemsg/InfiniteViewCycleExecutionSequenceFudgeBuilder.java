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

import com.opengamma.engine.view.execution.InfiniteViewCycleExecutionSequence;

/**
 * Fudge message builder for {@link InfiniteViewCycleExecutionSequence}
 */
@FudgeBuilderFor(InfiniteViewCycleExecutionSequence.class)
public class InfiniteViewCycleExecutionSequenceFudgeBuilder implements FudgeBuilder<InfiniteViewCycleExecutionSequence> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, InfiniteViewCycleExecutionSequence object) {
    return serializer.newMessage();
  }

  @Override
  public InfiniteViewCycleExecutionSequence buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    return new InfiniteViewCycleExecutionSequence();
  }

}
