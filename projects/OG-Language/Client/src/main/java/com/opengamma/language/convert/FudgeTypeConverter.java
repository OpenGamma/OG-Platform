/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static com.opengamma.language.convert.TypeMap.ZERO_LOSS_NON_PREFERRED;

import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;
import org.joda.beans.impl.direct.DirectBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.Data;
import com.opengamma.language.Value;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.JavaTypeInfo.Builder;
import com.opengamma.language.invoke.AbstractTypeConverter;

/**
 * Conversions using the Fudge type and object dictionaries. Conversions will use secondary types if applicable, falling back to
 * the message/object builders otherwise.  
 */
public final class FudgeTypeConverter extends AbstractTypeConverter {

  private static final Logger s_logger = LoggerFactory.getLogger(FudgeTypeConverter.class);

  private static final JavaTypeInfo<Object> OBJECT = JavaTypeInfo.builder(Object.class).get();
  private static final JavaTypeInfo<Object> OBJECT_ALLOW_NULL = JavaTypeInfo.builder(Object.class).allowNull().get();
  private static final JavaTypeInfo<FudgeMsg> FUDGE_MSG = JavaTypeInfo.builder(FudgeMsg.class).get();
  private static final JavaTypeInfo<FudgeMsg> FUDGE_MSG_ALLOW_NULL = JavaTypeInfo.builder(FudgeMsg.class).allowNull().get();
  private static final Map<JavaTypeInfo<?>, Integer> FROM_OBJECT = TypeMap.of(ZERO_LOSS_NON_PREFERRED, OBJECT);
  private static final Map<JavaTypeInfo<?>, Integer> FROM_OBJECT_ALLOW_NULL = TypeMap.of(ZERO_LOSS_NON_PREFERRED, OBJECT_ALLOW_NULL);
  private static final Map<JavaTypeInfo<?>, Integer> FROM_FUDGE_MSG = TypeMap.of(ZERO_LOSS_NON_PREFERRED, FUDGE_MSG);
  private static final Map<JavaTypeInfo<?>, Integer> FROM_FUDGE_MSG_ALLOW_NULL = TypeMap.of(ZERO_LOSS_NON_PREFERRED, FUDGE_MSG_ALLOW_NULL);

  private final FudgeContext _fudgeContext;

  public FudgeTypeConverter(final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public synchronized boolean canConvertTo(final JavaTypeInfo<?> targetType) {
    final Class<?> rawType = targetType.getRawClass();
    return ((rawType != Data.class) && (rawType != Value.class) && !getFudgeContext().getObjectDictionary().isDefaultObject(rawType));
  }

  @SuppressWarnings("unchecked")
  @Override
  public void convertValue(final ValueConversionContext conversionContext, final Object value, final JavaTypeInfo<?> type) {
    if (value == null) {
      if (type.isAllowNull()) {
        conversionContext.setResult(null);
      } else {
        conversionContext.setFail();
      }
      return;
    }
    if ((value.getClass() == Data.class) || (value.getClass() == Value.class)) {
      conversionContext.setFail();
      return;
    }
    final FudgeFieldType fieldType = getFudgeContext().getTypeDictionary().getByJavaType(type.getRawClass());
    try {
      if (fieldType == null) {
        // Try conversion from Fudge message
        final FudgeDeserializer deserializer = new FudgeDeserializer(getFudgeContext());
        conversionContext.setResult(deserializer.fudgeMsgToObject(type.getRawClass(), (FudgeMsg) value));
      } else if (fieldType instanceof SecondaryFieldType<?, ?>) {
        // Try conversion from primary type
        conversionContext.setResult(((SecondaryFieldType<Object, Object>) fieldType).primaryToSecondary(value));
      } else {
        final FudgeFieldType valueType = getFudgeContext().getTypeDictionary().getByJavaType(value.getClass());
        if ((valueType == null) && (fieldType.getTypeId() == FudgeWireType.SUB_MESSAGE_TYPE_ID)) {
          // Serialization to a message
          final Class<?> valueClass = value.getClass();
          if (getFudgeContext().getObjectDictionary().isDefaultObject(valueClass)) {
            // Don't convert default objects to messages; they should be expressed using Data/Value constructs
            conversionContext.setFail();
          } else {
            final FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
            final MutableFudgeMsg msg = serializer.objectToFudgeMsg(value);
            if (msg.getByOrdinal(FudgeSerializer.TYPES_HEADER_ORDINAL) == null) {
              FudgeSerializer.addClassHeader(msg, valueClass, baseClass(valueClass));
            }
            conversionContext.setResult(msg);
          }
        } else {
          // Target is a primary type; the source value might be a secondary type that can convert to it
          if (valueType instanceof SecondaryFieldType<?, ?>) {
            final SecondaryFieldType<Object, Object> secondaryValueType = (SecondaryFieldType<Object, Object>) valueType;
            if (type.getRawClass().isAssignableFrom(secondaryValueType.getPrimaryType().getJavaType())) {
              conversionContext.setResult(secondaryValueType.secondaryToPrimary(value));
            } else {
              // The primary type doesn't match the target type 
              conversionContext.setFail();
            }
          } else {
            // Unknown type to convert from
            conversionContext.setFail();
          }
        }
      }
    } catch (Throwable t) {
      // Could be anything from an unsupported operation to an invalid argument that prevents the conversion
      conversionContext.setFail();
    }
  }

  private Class<?> baseClass(Class<?> cls) {
    while (cls.getSuperclass() != Object.class && cls.getSuperclass() != DirectBean.class) {
      cls = cls.getSuperclass();
    }
    return cls.getSuperclass();
  }

  @Override
  public Map<JavaTypeInfo<?>, Integer> getConversionsTo(final JavaTypeInfo<?> targetType) {
    final FudgeFieldType fieldType = getFudgeContext().getTypeDictionary().getByJavaType(targetType.getRawClass());
    if (fieldType == null) {
      // Arbitrary object type found; conversion may be possible from a Fudge message
      s_logger.debug("Possible conversion from FudgeMsg to arbitrary object {}", targetType);
      return targetType.isAllowNull() ? FROM_FUDGE_MSG_ALLOW_NULL : FROM_FUDGE_MSG;
    } else if (fieldType instanceof SecondaryFieldType<?, ?>) {
      // Secondary type found; conversion is from the primary type
      final Builder<?> builder = JavaTypeInfo.builder(((SecondaryFieldType<?, ?>) fieldType).getPrimaryType().getJavaType());
      if (targetType.isAllowNull()) {
        builder.allowNull();
      }
      final JavaTypeInfo<?> sourceType = builder.get();
      s_logger.debug("Secondary type conversion from {} to {}", sourceType, targetType);
      return TypeMap.of(TypeMap.MINOR_LOSS, sourceType);
    } else {
      // Arbitrary wire type found; dictionary conversion may be possible
      s_logger.debug("Possible conversion from Object to wire type {}", targetType);
      return targetType.isAllowNull() ? FROM_OBJECT_ALLOW_NULL : FROM_OBJECT;
    }
  }

}
