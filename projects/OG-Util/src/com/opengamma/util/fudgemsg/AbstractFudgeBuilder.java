/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.types.FudgeWireType;

/**
 * Abstract Fudge builder.
 */
public abstract class AbstractFudgeBuilder {

  /**
   * Adds an object to the specified message if non-null.
   * 
   * @param msg  the msg to populate, not null
   * @param fieldName  the field name, may be null
   * @param value  the value, null ignored
   */
  protected static void addToMessage(final MutableFudgeMsg msg, final String fieldName, final Object value) {
    if (value != null) {
      if (value instanceof String) {
        msg.add(fieldName, null, FudgeWireType.STRING, value);
      } else if (value instanceof Enum<?>) {
        msg.add(fieldName, null, FudgeWireType.STRING, ((Enum<?>) value).name());
      } else {
        msg.add(fieldName, null, value);
      }
    }
  }

  /**
   * Adds an object to the specified message if non-null.
   * This handles object hierarchies.
   * 
   * @param <T> the declared type
   * @param serializer  the serializer, not null
   * @param msg  the msg to populate, not null
   * @param fieldName  the field name, may be null
   * @param value  the value, null ignored
   * @param declaredType  the declared Java type of the field, not null
   */
  protected static <T> void addToMessage(final FudgeSerializer serializer, final MutableFudgeMsg msg, final String fieldName, final T value, final Class<T> declaredType) {
    if (value != null) {
      MutableFudgeMsg subMsg = serializer.newMessage();
      FudgeSerializer.addClassHeader(subMsg, value.getClass(), declaredType);
      FudgeMsg builtMsg = serializer.objectToFudgeMsg(value);
      for (FudgeField field : builtMsg) {
        subMsg.add(field);
      }
      msg.add(fieldName, null, subMsg);
    }
  }

  /**
   * Adds an object to the specified message if non-null.
   * 
   * @param <T> the declared type
   * @param msg  the msg to populate, not null
   * @param fieldName  the field name, may be null
   * @param objectMsg  the object converted to a message, null ignored
   * @param value  the value, null ignored
   * @param declaredType  the declared Java type of the field, not null
   */
  protected static <T> void addToMessage(final MutableFudgeMsg msg, final String fieldName, final FudgeMsg objectMsg, final T value, final Class<T> declaredType) {
    if (objectMsg != null) {
      FudgeSerializer.addClassHeader((MutableFudgeMsg) objectMsg, value.getClass(), declaredType);
      msg.add(fieldName, null, objectMsg);
    }
  }

//  /**
//   * Adds an object to the specified message if non-null
//   * 
//   * @param msg  the msg to populate, not null
//   * @param fieldName  the field name, may be null
//   * @param wireType  the wire type to use, not null
//   * @param value  the value, null ignored
//   */
//  protected static void addToMessage(final MutableFudgeMsg msg, final String fieldName, final FudgeWireType wireType, final Object value) {
//    if (value != null) {
//      msg.add(fieldName, null, wireType, value);
//    }
//  }

}
