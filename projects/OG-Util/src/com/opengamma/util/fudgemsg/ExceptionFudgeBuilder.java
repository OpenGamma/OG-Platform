/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import java.lang.reflect.Constructor;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

/**
 * Fudge builder for {@code Exception}.
 */
@GenericFudgeBuilderFor(Exception.class)
public final class ExceptionFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<Exception> {

  /** Field name. */
  public static final String TYPE_FIELD_NAME = "type";
  /** Field name. */
  public static final String MESSAGE_FIELD_NAME = "message";

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Exception object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final Exception object) {
    if (object == null) {
      return null;
    }
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(final FudgeSerializer serializer, final Exception object, final MutableFudgeMsg msg) {
    msg.add(TYPE_FIELD_NAME, object.getClass().getName());
    msg.add(MESSAGE_FIELD_NAME, object.getMessage());
  }

  //-------------------------------------------------------------------------
  @Override
  public Exception buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return fromFudgeMsg(deserializer, msg);
  }

  @SuppressWarnings("unchecked")
  public static Exception fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    String type = msg.getString(TYPE_FIELD_NAME);
    String message = msg.getString(MESSAGE_FIELD_NAME);
    try {
      Class<? extends Exception> exceptionType = (Class<? extends Exception>) Class.forName(type);
      Constructor<? extends Exception> messageConstructor = exceptionType.getConstructor(String.class);
      return messageConstructor.newInstance(message);
    } catch (Exception e) {
      return new Exception(message);
    }
  }

}
