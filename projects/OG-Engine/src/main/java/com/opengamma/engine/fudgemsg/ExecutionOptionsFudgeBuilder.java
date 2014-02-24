/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

import org.apache.commons.lang.BooleanUtils;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Fudge message builder for {@link ExecutionOptions}
 */
@FudgeBuilderFor(ExecutionOptions.class)
public class ExecutionOptionsFudgeBuilder implements FudgeBuilder<ExecutionOptions> {

  private static final String EXECUTION_SEQUENCE_FIELD = "executionSequence";

  private static final String AWAIT_MARKET_DATA_FIELD = "awaitMarketData";
  private static final String TRIGGER_CYCLE_ON_LIVE_DATA_CHANGED_FIELD = "liveDataTriggerEnabled";
  private static final String TRIGGER_CYCLE_ON_TIME_ELAPSED_FIELD = "timeElapsedTriggerEnabled";
  private static final String RUN_AS_FAST_AS_POSSIBLE_FIELD = "runAsFastAsPossible";
  private static final String COMPILE_ONLY_FIELD = "compileOnly";
  private static final String FETCH_MARKET_DATA_ONLY_FIELD = "fetchMarketDataOnly";
  private static final String SKIP_CYCLE_ON_NO_MARKET_DATA_FIELD = "skipCycleOnNoMarketData";
  private static final String WAIT_FOR_INITIAL_TRIGGER_FIELD = "waitForInitialTrigger";
  private static final String MAX_SUCCESSIVE_DELTA_CYCLES_FIELD = "maxSuccessiveDeltaCycles";
  private static final String MARKET_DATA_TIMEOUT_MILLIS_FIELD = "marketDataTimeoutMillis";
  private static final String DEFAULT_EXECUTION_OPTIONS_FIELD = "defaultExecutionOptions";
  private static final String BATCH_FIELD = "batch";

  private static final Collection<Pair<String, ViewExecutionFlags>> s_flags = Arrays.<Pair<String, ViewExecutionFlags>>asList(
      Pairs.of(AWAIT_MARKET_DATA_FIELD, ViewExecutionFlags.AWAIT_MARKET_DATA),
      Pairs.of(TRIGGER_CYCLE_ON_LIVE_DATA_CHANGED_FIELD, ViewExecutionFlags.TRIGGER_CYCLE_ON_MARKET_DATA_CHANGED),
      Pairs.of(TRIGGER_CYCLE_ON_TIME_ELAPSED_FIELD, ViewExecutionFlags.TRIGGER_CYCLE_ON_TIME_ELAPSED),
      Pairs.of(RUN_AS_FAST_AS_POSSIBLE_FIELD, ViewExecutionFlags.RUN_AS_FAST_AS_POSSIBLE),
      Pairs.of(COMPILE_ONLY_FIELD, ViewExecutionFlags.COMPILE_ONLY),
      Pairs.of(FETCH_MARKET_DATA_ONLY_FIELD, ViewExecutionFlags.FETCH_MARKET_DATA_ONLY),
      Pairs.of(SKIP_CYCLE_ON_NO_MARKET_DATA_FIELD, ViewExecutionFlags.SKIP_CYCLE_ON_NO_MARKET_DATA),
      Pairs.of(WAIT_FOR_INITIAL_TRIGGER_FIELD, ViewExecutionFlags.WAIT_FOR_INITIAL_TRIGGER),
      Pairs.of(BATCH_FIELD, ViewExecutionFlags.BATCH));

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ExecutionOptions object) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessageWithClassHeaders(msg, EXECUTION_SEQUENCE_FIELD, null, object.getExecutionSequence());
    for (Pair<String, ViewExecutionFlags> flags : s_flags) {
      if (object.getFlags().contains(flags.getSecond())) {
        msg.add(flags.getFirst(), Boolean.TRUE);
      }
    }
    if (object.getMaxSuccessiveDeltaCycles() != null) {
      msg.add(MAX_SUCCESSIVE_DELTA_CYCLES_FIELD, object.getMaxSuccessiveDeltaCycles());
    }
    if (object.getMarketDataTimeoutMillis() != null) {
      msg.add(MARKET_DATA_TIMEOUT_MILLIS_FIELD, object.getMarketDataTimeoutMillis());
    }
    serializer.addToMessage(msg, DEFAULT_EXECUTION_OPTIONS_FIELD, null, object.getDefaultExecutionOptions());
    return msg;
  }

  @Override
  public ExecutionOptions buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    ViewCycleExecutionSequence executionSequence = deserializer.fudgeMsgToObject(ViewCycleExecutionSequence.class, message.getMessage(EXECUTION_SEQUENCE_FIELD));
    EnumSet<ViewExecutionFlags> flags = EnumSet.noneOf(ViewExecutionFlags.class);
    for (Pair<String, ViewExecutionFlags> flagField : s_flags) {
      if (BooleanUtils.isTrue(message.getBoolean(flagField.getFirst()))) {
        flags.add(flagField.getSecond());
      }
    }
    Integer maxSuccessiveDeltaCycles = null;
    if (message.hasField(MAX_SUCCESSIVE_DELTA_CYCLES_FIELD)) {
      maxSuccessiveDeltaCycles = message.getInt(MAX_SUCCESSIVE_DELTA_CYCLES_FIELD);
    }
    Long marketDataTimeoutMillis = null;
    if (message.hasField(MARKET_DATA_TIMEOUT_MILLIS_FIELD)) {
      marketDataTimeoutMillis = message.getLong(MARKET_DATA_TIMEOUT_MILLIS_FIELD);
    }
    FudgeField defaultExecutionOptionsField = message.getByName(DEFAULT_EXECUTION_OPTIONS_FIELD);
    ViewCycleExecutionOptions defaultExecutionOptions = defaultExecutionOptionsField != null ?
        deserializer.fieldValueToObject(ViewCycleExecutionOptions.class, defaultExecutionOptionsField) : null;

    return new ExecutionOptions(executionSequence, flags, maxSuccessiveDeltaCycles, marketDataTimeoutMillis, defaultExecutionOptions);
  }

}
