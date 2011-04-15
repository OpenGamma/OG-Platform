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

import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;

/**
 * Fudge message builder for {@link ExecutionOptions}
 */
@FudgeBuilderFor(ExecutionOptions.class)
public class ExecutionOptionsBuilder implements FudgeBuilder<ExecutionOptions> {

  private static final String EXECUTION_SEQUENCE_FIELD = "executionSequence";
  private static final String RUN_AS_FAST_AS_POSSIBLE_FIELD = "runAsFastAsPossible";
  private static final String LIVE_DATA_TRIGGER_ENABLED_FIELD = "liveDataTriggerEnabled";
  private static final String MAX_SUCCESSIVE_DELTA_CYCLES_FIELD = "liveDataTriggerEnabled";
  private static final String COMPILE_ONLY_FIELD = "compileOnly";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, ExecutionOptions object) {
    MutableFudgeMsg msg = context.newMessage();
    context.addToMessageWithClassHeaders(msg, EXECUTION_SEQUENCE_FIELD, null, object.getExecutionSequence());
    msg.add(RUN_AS_FAST_AS_POSSIBLE_FIELD, object.isRunAsFastAsPossible());
    msg.add(LIVE_DATA_TRIGGER_ENABLED_FIELD, object.isLiveDataTriggerEnabled());
    if (object.getMaxSuccessiveDeltaCycles() != null) {
      msg.add(MAX_SUCCESSIVE_DELTA_CYCLES_FIELD, object.getMaxSuccessiveDeltaCycles());
    }
    msg.add(COMPILE_ONLY_FIELD, object.isCompileOnly());
    return msg;
  }

  @Override
  public ExecutionOptions buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    ViewCycleExecutionSequence executionSequence = context.fudgeMsgToObject(ViewCycleExecutionSequence.class, message.getMessage(EXECUTION_SEQUENCE_FIELD));
    boolean runAsFastAsPossible = message.getBoolean(RUN_AS_FAST_AS_POSSIBLE_FIELD);
    boolean liveDataTriggerEnabled = message.getBoolean(LIVE_DATA_TRIGGER_ENABLED_FIELD);
    Integer maxSuccessiveDeltaCycles = null;
    if (message.hasField(MAX_SUCCESSIVE_DELTA_CYCLES_FIELD)) {
      maxSuccessiveDeltaCycles = message.getInt(MAX_SUCCESSIVE_DELTA_CYCLES_FIELD);
    }
    boolean compileOnly = message.getBoolean(COMPILE_ONLY_FIELD);
    return new ExecutionOptions(executionSequence, runAsFastAsPossible, liveDataTriggerEnabled, maxSuccessiveDeltaCycles, compileOnly);
  }
  
  

}
