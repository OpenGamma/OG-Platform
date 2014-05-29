/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.tuple;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.util.tuple.Triple;

/**
 * Fudge builder for {@code Triple}.
 */
@GenericFudgeBuilderFor(Triple.class)
public final class TripleFudgeBuilder implements FudgeBuilder<Triple<?, ?, ?>> {

  /** Field name. */
  public static final String FIRST_FIELD_NAME = "first";
  /** Field name. */
  public static final String SECOND_FIELD_NAME = "second";
  /** Field name. */
  public static final String THIRD_FIELD_NAME = "third";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Triple<?, ?, ?> object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    if (object.getFirst() != null) {
      serializer.addToMessageObject(msg, FIRST_FIELD_NAME, null, object.getFirst(), Object.class);
    }
    if (object.getSecond() != null) {
      serializer.addToMessageObject(msg, SECOND_FIELD_NAME, null, object.getSecond(), Object.class);
    }
    if (object.getThird() != null) {
      serializer.addToMessageObject(msg, THIRD_FIELD_NAME, null, object.getThird(), Object.class);
    }
    return msg;
  }

  @Override
  public Triple<?, ?, ?> buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    Object first = null;
    if (msg.hasField(FIRST_FIELD_NAME)) {
      first = deserializer.fieldValueToObject(msg.getByName(FIRST_FIELD_NAME));
    }
    Object second = null;
    if (msg.hasField(SECOND_FIELD_NAME)) {
      second = deserializer.fieldValueToObject(msg.getByName(SECOND_FIELD_NAME));
    }
    Object third = null;
    if (msg.hasField(THIRD_FIELD_NAME)) {
      third = deserializer.fieldValueToObject(msg.getByName(THIRD_FIELD_NAME));
    }
    return Triple.of(first, second, third);
  }

}
