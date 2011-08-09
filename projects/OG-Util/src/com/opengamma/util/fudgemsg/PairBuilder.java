/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
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
public final class PairBuilder implements FudgeBuilder<Pair<?, ?>> {

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
        addToMessageWithClassHeaders(serializer, msg, FIRST_FIELD_NAME, object.getFirst());
      }
    }
    if (object instanceof LongDoublePair || object instanceof IntDoublePair || object instanceof DoublesPair) {
      msg.add("secondDouble", object.getSecond());
    } else {
      if (object.getSecond() != null) {
        addToMessageWithClassHeaders(serializer, msg, SECOND_FIELD_NAME, object.getSecond());
      }
    }
    return msg;
  }

  /**
   * This does almost the same thing as {@link FudgeSerializer.addToMessageWithClassHeaders} except:
   * - If a secondary type or a builder could be used then the builder will be used
   * -- So that the class headers can be added
   * @param serializer
   * @param msg
   * @param fieldName
   * @param obj
   */
  private void addToMessageWithClassHeaders(FudgeSerializer serializer, MutableFudgeMsg msg, String fieldName,
      Object obj) {
    if (obj == null) {
      return;
    }
    final Class<?> clazz = obj.getClass();
    FudgeContext fudgeContext = serializer.getFudgeContext();
    FudgeTypeDictionary typeDictionary = fudgeContext.getTypeDictionary();
    final FudgeFieldType fieldType = typeDictionary.getByJavaType(clazz);
    if (isNative(fieldType, obj, typeDictionary)) {
      msg.add(fieldName, obj);
      return;
    }
    MutableFudgeMsg valueMsg = serializer.objectToFudgeMsg(obj);
    FudgeSerializer.addClassHeader(valueMsg, obj.getClass());
    msg.add(fieldName, valueMsg);
  }

  /**
   * Checks if the object is in the correct native format to send AND it isn't a secondary type
   * 
   * @param fieldType  the Fudge type, may be null
   * @param object  the value to add, not null
   * @return true if the object can be sent natively
   */
  private boolean isNative(final FudgeFieldType fieldType, final Object object, FudgeTypeDictionary dict) {
    if (fieldType == null) {
      return false;
    }
    boolean isNative = FudgeWireType.SUB_MESSAGE.equals(fieldType) == false ||
            (FudgeWireType.SUB_MESSAGE.equals(fieldType) && object instanceof FudgeMsg);
    return isNative && dict.getByTypeId(fieldType.getTypeId()) == fieldType;
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
