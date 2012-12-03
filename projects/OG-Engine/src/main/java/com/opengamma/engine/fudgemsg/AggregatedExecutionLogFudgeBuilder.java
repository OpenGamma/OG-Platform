/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.engine.view.ExecutionLogWithContext;
import com.opengamma.engine.view.calc.SimpleAggregatedExecutionLog;
import com.opengamma.util.log.LogLevel;

/**
 * Fudge message builder for {@link SimpleAggregatedExecutionLog}.
 */
@FudgeBuilderFor(SimpleAggregatedExecutionLog.class)
public class AggregatedExecutionLogFudgeBuilder implements FudgeBuilder<SimpleAggregatedExecutionLog> {

  private static final String LOG_LEVEL_FIELD_NAME = "logLevel";
  private static final String EXECUTION_LOGS_COLLECTED_FIELD_NAME = "executionLogsCollected";
  private static final String EXECUTION_LOG_FIELD_NAME = "executionLog";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, SimpleAggregatedExecutionLog object) {
    MutableFudgeMsg msg = serializer.newMessage();
    if (!object.getLogLevels().isEmpty()) {
      for (LogLevel logLevel : object.getLogLevels()) {
        serializer.addToMessage(msg, LOG_LEVEL_FIELD_NAME, null, logLevel.name());
      }
    }
    if (object.getLogs() != null) {
      msg.add(EXECUTION_LOGS_COLLECTED_FIELD_NAME, null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
      for (ExecutionLogWithContext log : object.getLogs()) {
        serializer.addToMessage(msg, EXECUTION_LOG_FIELD_NAME, null, log);
      }
    }
    return msg;
  }

  @Override
  public SimpleAggregatedExecutionLog buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    final EnumSet<LogLevel> logLevels = EnumSet.noneOf(LogLevel.class);
    for (FudgeField levelField : message.getAllByName(LOG_LEVEL_FIELD_NAME)) {
      logLevels.add(LogLevel.valueOf((String) levelField.getValue()));
    }
    final boolean executionLogsCollected = message.hasField(EXECUTION_LOGS_COLLECTED_FIELD_NAME);
    final List<ExecutionLogWithContext> logs;
    if (executionLogsCollected) {
      logs = new ArrayList<ExecutionLogWithContext>();
      for (FudgeField logField : message.getAllByName(EXECUTION_LOG_FIELD_NAME)) {
        logs.add(deserializer.fieldValueToObject(ExecutionLogWithContext.class, logField));
      }
    } else {
      logs = null;
    }
    return new SimpleAggregatedExecutionLog(logLevels, logs);
  }

}

