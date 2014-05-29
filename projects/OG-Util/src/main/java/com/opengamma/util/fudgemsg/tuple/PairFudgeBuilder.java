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
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.IntDoublePair;
import com.opengamma.util.tuple.IntObjectPair;
import com.opengamma.util.tuple.LongDoublePair;
import com.opengamma.util.tuple.LongObjectPair;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Fudge builder for {@code DoublesPair}.
 */
@GenericFudgeBuilderFor(Pair.class)
//Without this the JavaBeanBuilder is used, which is slow
@FudgeBuilderFor(DoublesPair.class)
public final class PairFudgeBuilder implements FudgeBuilder<Pair<?, ?>> {

  /** Field name. */
  public static final String FIRST_FIELD_NAME = "first";
  /** Field name. */
  public static final String SECOND_FIELD_NAME = "second";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Pair<?, ?> object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    if (object instanceof LongObjectPair || object instanceof LongDoublePair) {
      msg.add("firstLong", object.getFirst());
    } else if (object instanceof IntObjectPair || object instanceof IntDoublePair) {
      msg.add("firstInt", object.getFirst());
    } else if (object instanceof DoublesPair) {
      msg.add("firstDouble", object.getFirst());
    } else {
      if (object.getFirst() != null) {
        if (object.getFirst() instanceof String) {
          msg.add(FIRST_FIELD_NAME, null, FudgeWireType.STRING, object.getFirst());
        } else {
          serializer.addToMessageObject(msg, FIRST_FIELD_NAME, null, object.getFirst(), Object.class);
        }
      }
    }
    if (object instanceof LongDoublePair || object instanceof IntDoublePair || object instanceof DoublesPair) {
      msg.add("secondDouble", object.getSecond());
    } else {
      if (object.getSecond() != null) {
        if (object.getSecond() instanceof String) {
          msg.add(SECOND_FIELD_NAME, null, FudgeWireType.STRING, object.getSecond());
        } else {
          serializer.addToMessageObject(msg, SECOND_FIELD_NAME, null, object.getSecond(), Object.class);
        }
      }
    }
    return msg;
  }

  @Override
  public Pair<?, ?> buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    final Long firstLong = msg.getLong("firstLong");
    if (firstLong != null) {
      final Double secondDouble = msg.getDouble("secondDouble");
      if (secondDouble != null) {
        return LongDoublePair.of(firstLong.longValue(), secondDouble.doubleValue());
      } else {
        final FudgeField secondField = msg.getByName(SECOND_FIELD_NAME);
        final Object second = secondField != null ? deserializer.fieldValueToObject(secondField) : null;
        return LongObjectPair.of(firstLong.longValue(), second);
      }
    }
    final Long firstInt = msg.getLong("firstInt");
    if (firstInt != null) {
      final Double secondDouble = msg.getDouble("secondDouble");
      if (secondDouble != null) {
        return IntDoublePair.of(firstInt.intValue(), secondDouble.doubleValue());
      } else {
        final FudgeField secondField = msg.getByName(SECOND_FIELD_NAME);
        final Object second = secondField != null ? deserializer.fieldValueToObject(secondField) : null;
        return IntObjectPair.of(firstInt.intValue(), second);
      }
    }
    final Double firstDouble = msg.getDouble("firstDouble");
    final Double secondDouble = msg.getDouble("secondDouble");
    if (firstDouble != null && secondDouble != null) {
      return DoublesPair.of(firstDouble.doubleValue(), secondDouble.doubleValue());
    }
    final FudgeField firstField = msg.getByName(FIRST_FIELD_NAME);
    final Object first = firstField != null ? deserializer.fieldValueToObject(firstField) : null;
    final FudgeField secondField = msg.getByName(SECOND_FIELD_NAME);
    final Object second = secondField != null ? deserializer.fieldValueToObject(secondField) : null;
    return ObjectsPair.of(first, second);
  }

}
