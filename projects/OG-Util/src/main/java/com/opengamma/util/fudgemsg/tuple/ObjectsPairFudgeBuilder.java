/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.tuple;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Fudge builder for {@code ObjectsPair}.
 */
@GenericFudgeBuilderFor(ObjectsPair.class)
public final class ObjectsPairFudgeBuilder implements FudgeBuilder<ObjectsPair<?, ?>> {

  /** Field name. */
  public static final String FIRST_FIELD_NAME = "first";
  /** Field name. */
  public static final String SECOND_FIELD_NAME = "second";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ObjectsPair<?, ?> object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    if (object.getFirst() != null) {
      if (object.getFirst() instanceof String) {
        msg.add(FIRST_FIELD_NAME, null, FudgeWireType.STRING, object.getFirst());
      } else {
        serializer.addToMessageObject(msg, FIRST_FIELD_NAME, null, object.getFirst(), Object.class);
      }
    }
    if (object.getSecond() != null) {
      if (object.getSecond() instanceof String) {
        msg.add(SECOND_FIELD_NAME, null, FudgeWireType.STRING, object.getSecond());
      } else {
        serializer.addToMessageObject(msg, SECOND_FIELD_NAME, null, object.getSecond(), Object.class);
      }
    }
    return msg;
  }

  public static <K, V> MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final Pair<? extends K, ? extends V> object, final Class<K> baseK, final Class<V> baseV) {
    final MutableFudgeMsg msg = serializer.newMessage();
    if (object.getFirst() != null) {
      serializer.addToMessageWithClassHeaders(msg, FIRST_FIELD_NAME, null, object.getFirst(), baseK);
    }
    if (object.getSecond() != null) {
      serializer.addToMessageWithClassHeaders(msg, SECOND_FIELD_NAME, null, object.getSecond(), baseV);
    }
    return msg;
  }

  @Override
  public ObjectsPair<?, ?> buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    Object first;
    if (msg.hasField(FIRST_FIELD_NAME)) {
      first = deserializer.fieldValueToObject(msg.getByName(FIRST_FIELD_NAME));
    } else {
      first = null;
    }
    Object second;
    if (msg.hasField(SECOND_FIELD_NAME)) {
      second = deserializer.fieldValueToObject(msg.getByName(SECOND_FIELD_NAME));
    } else {
      second = null;
    }
    return ObjectsPair.of(first, second);
  }

  public static <K, V> ObjectsPair<K, V> buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg, final Class<K> baseK, final Class<V> baseV) {
    FudgeField field = msg.getByName(FIRST_FIELD_NAME);
    final K first = (field != null) ? deserializer.fieldValueToObject(baseK, field) : null;
    field = msg.getByName(SECOND_FIELD_NAME);
    final V second = (field != null) ? deserializer.fieldValueToObject(baseV, field) : null;
    return ObjectsPair.of(first, second);
  }

}
