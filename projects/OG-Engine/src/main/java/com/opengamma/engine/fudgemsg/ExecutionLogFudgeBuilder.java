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
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.engine.view.ExecutionLog;
import com.opengamma.util.log.LogEvent;
import com.opengamma.util.log.LogLevel;

/**
 * Fudge message builder for {@link ExecutionLog}.
 */
@GenericFudgeBuilderFor(ExecutionLog.class)
public class ExecutionLogFudgeBuilder implements FudgeBuilder<ExecutionLog> {

  private static final String LOG_EVENTS_COLLECTED_FIELD_NAME = "logEventsCollected";
  private static final String LOG_EVENT_FIELD_NAME = "logEvent";
  private static final String LOG_LEVEL_FIELD_NAME = "logLevel";
  private static final String EXCEPTION_CLASS_FIELD_NAME = "exceptionClass";
  private static final String EXCEPTION_MESSAGE_FIELD_NAME = "exceptionMessage";
  private static final String EXCEPTION_STACK_TRACE_FIELD_NAME = "exceptionStackTrace";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ExecutionLog object) {
    MutableFudgeMsg msg = serializer.newMessage();
    if (object.getEvents() != null) {
      msg.add(LOG_EVENTS_COLLECTED_FIELD_NAME, null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
      for (LogEvent event : object.getEvents()) {
        serializer.addToMessage(msg, LOG_EVENT_FIELD_NAME, null, event);
      }
    }
    if (!object.getLogLevels().isEmpty()) {
      for (LogLevel logLevel : object.getLogLevels()) {
        serializer.addToMessage(msg, LOG_LEVEL_FIELD_NAME, null, logLevel.name());
      }
    }
    if (object.getExceptionClass() != null) {
      serializer.addToMessage(msg, EXCEPTION_CLASS_FIELD_NAME, null, object.getExceptionClass());
    }
    if (object.getExceptionMessage() != null) {
      serializer.addToMessage(msg, EXCEPTION_MESSAGE_FIELD_NAME, null, object.getExceptionMessage());
    }
    if (object.getExceptionStackTrace() != null) {
      serializer.addToMessage(msg, EXCEPTION_STACK_TRACE_FIELD_NAME, null, object.getExceptionStackTrace());
    }
    return msg;
  }

  @Override
  public ExecutionLog buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    final boolean logEventsCollected = message.hasField(LOG_EVENTS_COLLECTED_FIELD_NAME);
    final List<LogEvent> events;
    if (logEventsCollected) {
      events = new ArrayList<LogEvent>();
      for (FudgeField eventField : message.getAllByName(LOG_EVENT_FIELD_NAME)) {
        events.add(deserializer.fieldValueToObject(LogEvent.class, eventField));
      }
    } else {
      events = null;
    }
    final EnumSet<LogLevel> logLevels = EnumSet.noneOf(LogLevel.class);
    for (FudgeField levelField : message.getAllByName(LOG_LEVEL_FIELD_NAME)) {
      logLevels.add(LogLevel.valueOf((String) levelField.getValue()));
    }
    final String exceptionClass = message.getString(EXCEPTION_CLASS_FIELD_NAME);
    final String exceptionMessage = message.getString(EXCEPTION_MESSAGE_FIELD_NAME);
    final String exceptionStackTrace = message.getString(EXCEPTION_STACK_TRACE_FIELD_NAME);
    
    return new ExecutionLog() {

      @Override
      public EnumSet<LogLevel> getLogLevels() {
        return logLevels;
      }

      @Override
      public List<LogEvent> getEvents() {
        return events;
      }

      @Override
      public boolean hasException() {
        return getExceptionClass() != null;
      }

      @Override
      public String getExceptionClass() {
        return exceptionClass;
      }

      @Override
      public String getExceptionMessage() {
        return exceptionMessage;
      }

      @Override
      public String getExceptionStackTrace() {
        return exceptionStackTrace;
      }

      @Override
      public boolean isEmpty() {
        return getLogLevels().isEmpty() && !hasException();
      }
      
    };
  }

}
