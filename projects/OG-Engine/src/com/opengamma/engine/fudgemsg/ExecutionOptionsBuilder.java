/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.EnumSet;

import org.apache.commons.lang.BooleanUtils;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.id.UniqueIdentifier;

/**
 * Fudge message builder for {@link ExecutionOptions}
 */
@FudgeBuilderFor(ExecutionOptions.class)
public class ExecutionOptionsBuilder implements FudgeBuilder<ExecutionOptions> {

  private static final String EXECUTION_SEQUENCE_FIELD = "executionSequence";
  
  private static final String TRIGGER_CYCLE_ON_LIVE_DATA_CHANGED_FIELD = "liveDataTriggerEnabled";
  private static final String TRIGGER_CYCLE_ON_TIME_ELAPSED_FIELD = "timeElapsedTriggerEnabled";
  private static final String RUN_AS_FAST_AS_POSSIBLE_FIELD = "runAsFastAsPossible";
  private static final String COMPILE_ONLY_FIELD = "compileOnly";
  
  private static final String MAX_SUCCESSIVE_DELTA_CYCLES_FIELD = "maxSuccessiveDeltaCycles";
  private static final String SNAPSHOT_FIELD = "snapshotId";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, ExecutionOptions object) {
    MutableFudgeMsg msg = context.newMessage();
    context.addToMessageWithClassHeaders(msg, EXECUTION_SEQUENCE_FIELD, null, object.getExecutionSequence());
    msg.add(TRIGGER_CYCLE_ON_LIVE_DATA_CHANGED_FIELD, object.getFlags().contains(ViewExecutionFlags.TRIGGER_CYCLE_ON_LIVE_DATA_CHANGED));
    msg.add(TRIGGER_CYCLE_ON_TIME_ELAPSED_FIELD, object.getFlags().contains(ViewExecutionFlags.TRIGGER_CYCLE_ON_TIME_ELAPSED));
    msg.add(RUN_AS_FAST_AS_POSSIBLE_FIELD, object.getFlags().contains(ViewExecutionFlags.RUN_AS_FAST_AS_POSSIBLE));
    msg.add(COMPILE_ONLY_FIELD, object.getFlags().contains(ViewExecutionFlags.COMPILE_ONLY));
    if (object.getMaxSuccessiveDeltaCycles() != null) {
      msg.add(MAX_SUCCESSIVE_DELTA_CYCLES_FIELD, object.getMaxSuccessiveDeltaCycles());
    }
    msg.add(SNAPSHOT_FIELD, object.getMarketDataSnapshotIdentifier());
    return msg;
  }

  @Override
  public ExecutionOptions buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    ViewCycleExecutionSequence executionSequence = context.fudgeMsgToObject(ViewCycleExecutionSequence.class, message.getMessage(EXECUTION_SEQUENCE_FIELD));
    EnumSet<ViewExecutionFlags> flags = EnumSet.noneOf(ViewExecutionFlags.class);
    if (BooleanUtils.isTrue(message.getBoolean(TRIGGER_CYCLE_ON_LIVE_DATA_CHANGED_FIELD))) {
      flags.add(ViewExecutionFlags.TRIGGER_CYCLE_ON_LIVE_DATA_CHANGED);
    }
    if (BooleanUtils.isTrue(message.getBoolean(TRIGGER_CYCLE_ON_TIME_ELAPSED_FIELD))) {
      flags.add(ViewExecutionFlags.TRIGGER_CYCLE_ON_TIME_ELAPSED);
    }
    if (BooleanUtils.isTrue(message.getBoolean(RUN_AS_FAST_AS_POSSIBLE_FIELD))) {
      flags.add(ViewExecutionFlags.RUN_AS_FAST_AS_POSSIBLE);
    }
    if (BooleanUtils.isTrue(message.getBoolean(COMPILE_ONLY_FIELD))) {
      flags.add(ViewExecutionFlags.COMPILE_ONLY);
    }
    Integer maxSuccessiveDeltaCycles = null;
    if (message.hasField(MAX_SUCCESSIVE_DELTA_CYCLES_FIELD)) {
      maxSuccessiveDeltaCycles = message.getInt(MAX_SUCCESSIVE_DELTA_CYCLES_FIELD);
    }
    UniqueIdentifier snapshotId = message.getValue(UniqueIdentifier.class, SNAPSHOT_FIELD);
    return new ExecutionOptions(executionSequence, flags, maxSuccessiveDeltaCycles, snapshotId);
  }
  
  

}
