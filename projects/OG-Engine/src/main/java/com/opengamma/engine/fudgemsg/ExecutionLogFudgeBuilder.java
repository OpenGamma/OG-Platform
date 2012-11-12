/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.ArrayList;
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

/**
 * Fudge message builder for {@link ExecutionLog}.
 */
@GenericFudgeBuilderFor(ExecutionLog.class)
public class ExecutionLogFudgeBuilder implements FudgeBuilder<ExecutionLog> {

  private static final String ERROR_INDICATOR_FIELD_NAME = "error";
  private static final String WARN_INDICATOR_FIELD_NAME = "warn";
  private static final String INFO_INDICATOR_FIELD_NAME = "info";
  private static final String LOG_EVENT_FIELD_NAME = "logEvent";
  private static final String EXCEPTION_CLASS_FIELD_NAME = "exceptionClass";
  private static final String EXCEPTION_MESSAGE_FIELD_NAME = "exceptionMessage";
  private static final String EXCEPTION_STACK_TRACE_FIELD_NAME = "exceptionStackTrace";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ExecutionLog object) {
    MutableFudgeMsg msg = serializer.newMessage();
    if (object.hasError()) {
      msg.add(ERROR_INDICATOR_FIELD_NAME, null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
    }
    if (object.hasWarn()) {
      msg.add(WARN_INDICATOR_FIELD_NAME, null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
    }
    if (object.hasInfo()) {
      msg.add(INFO_INDICATOR_FIELD_NAME, null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
    }
    if (object.getEvents() != null) {
      for (LogEvent event : object.getEvents()) {
        serializer.addToMessage(msg, LOG_EVENT_FIELD_NAME, null, event);
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
    final boolean hasError = message.hasField(ERROR_INDICATOR_FIELD_NAME);
    final boolean hasWarn = message.hasField(WARN_INDICATOR_FIELD_NAME);
    final boolean hasInfo = message.hasField(INFO_INDICATOR_FIELD_NAME);
    final List<LogEvent> events = new ArrayList<LogEvent>();
    for (FudgeField eventField : message.getAllByName(LOG_EVENT_FIELD_NAME)) {
      events.add(deserializer.fieldValueToObject(LogEvent.class, eventField));
    }
    final String exceptionClass = message.getString(EXCEPTION_CLASS_FIELD_NAME);
    final String exceptionMessage = message.getString(EXCEPTION_MESSAGE_FIELD_NAME);
    final String exceptionStackTrace = message.getString(EXCEPTION_STACK_TRACE_FIELD_NAME);
    
    return new ExecutionLog() {

      @Override
      public boolean hasError() {
        return hasError;
      }

      @Override
      public boolean hasWarn() {
        return hasWarn;
      }

      @Override
      public boolean hasInfo() {
        return hasInfo;
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
      
    };
  }

}
