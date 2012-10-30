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
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;

/**
 * Fudge message builder for {@link ArbitraryViewCycleExecutionSequence}
 */
@FudgeBuilderFor(ArbitraryViewCycleExecutionSequence.class)
public class ArbitraryViewCycleExecutionSequenceFudgeBuilder implements FudgeBuilder<ArbitraryViewCycleExecutionSequence> {

  private static final String SEQUENCE_FIELD = "sequence";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ArbitraryViewCycleExecutionSequence object) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, SEQUENCE_FIELD, null, object.getRemainingSequence());
    return msg;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ArbitraryViewCycleExecutionSequence buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    List<ViewCycleExecutionOptions> sequence = deserializer.fieldValueToObject(List.class, msg.getByName(SEQUENCE_FIELD));
    return new ArbitraryViewCycleExecutionSequence(sequence);
  }

}
