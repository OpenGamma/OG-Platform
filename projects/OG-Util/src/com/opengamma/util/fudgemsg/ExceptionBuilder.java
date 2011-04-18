/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import java.lang.reflect.Constructor;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

/**
 * Fudge message builder for {@link Exception}
 */
@GenericFudgeBuilderFor(Exception.class)
public class ExceptionBuilder implements FudgeBuilder<Exception> {

  private static final String TYPE_FIELD = "type";
  private static final String MESSAGE_FIELD = "message";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, Exception object) {
    MutableFudgeMsg msg = context.newMessage();
    msg.add(TYPE_FIELD, object.getClass().getName());
    msg.add(MESSAGE_FIELD, object.getMessage());
    return msg;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Exception buildObject(FudgeDeserializationContext context, FudgeMsg msg) {
    String type = msg.getString(TYPE_FIELD);
    String message = msg.getString(MESSAGE_FIELD);
    try {
      Class<? extends Exception> exceptionType = (Class<? extends Exception>) Class.forName(type);
      Constructor<? extends Exception> messageConstructor = exceptionType.getConstructor(String.class);
      return messageConstructor.newInstance(message);
    } catch (Exception e) {
      return new Exception(message);
    }
  }

}
