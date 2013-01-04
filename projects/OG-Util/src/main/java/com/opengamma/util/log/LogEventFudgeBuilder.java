/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.log;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

/**
 * Fudge message builder for {@link LogEvent}.
 */
@GenericFudgeBuilderFor(LogEvent.class)
public class LogEventFudgeBuilder implements FudgeBuilder<LogEvent> {

  private static final String LEVEL_FIELD = "level";
  private static final String MESSAGE_FIELD = "message";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, LogEvent object) {
    MutableFudgeMsg msg = serializer.newMessage();
    msg.add(LEVEL_FIELD, object.getLevel().name());
    msg.add(MESSAGE_FIELD, object.getMessage());
    return msg;
  }

  @Override
  public LogEvent buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    LogLevel level = LogLevel.valueOf(msg.getString(LEVEL_FIELD));
    String message = msg.getString(MESSAGE_FIELD);
    return new SimpleLogEvent(level, message);
  }

}
