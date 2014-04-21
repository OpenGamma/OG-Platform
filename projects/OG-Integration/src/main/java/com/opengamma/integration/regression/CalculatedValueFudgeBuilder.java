/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Builder for CalculatedValues. Doesn't use secondary types.
 */
@FudgeBuilderFor(CalculatedValue.class)
public class CalculatedValueFudgeBuilder implements FudgeBuilder<CalculatedValue> {

  private static final String SPECIFICATION_PROPERTIES = "specification_properties";
  private static final String TARGET_NAME = "target_name";
  private static final String TARGET_TYPE = "target_type";
  private static final String VALUE = "value";
  private static final Integer TYPES_HEADER_ORDINAL = 0;
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CalculatedValue object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    
    serializer.addToMessage(msg, SPECIFICATION_PROPERTIES, null, object.getSpecificationProperties());
    msg.add(TARGET_NAME, object.getTargetName());
    msg.add(TARGET_TYPE, object.getTargetType());
    
    addToMessageWithHeader(serializer, msg, VALUE, object.getValue());
    
    return msg;
  }

  @Override
  public CalculatedValue buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    ValueProperties specificationProperties = deserializer.fieldValueToObject(ValueProperties.class, msg.getByName(SPECIFICATION_PROPERTIES));
    String targetName = msg.getString(TARGET_NAME);
    String targetType = msg.getString(TARGET_TYPE);
    
    Object value = deserializer.fieldValueToObject(msg.getByName(VALUE));
    
    return CalculatedValue.of(value, specificationProperties, targetType, targetName);
  }

  
  
  /**
   * Bypasses secondary types.
   * @param serializer serializer 
   * @param message message
   * @param name name
   * @param ordinal ordinal
   * @param object object
   */
  static void addToMessageWithHeader(final FudgeSerializer serializer, final MutableFudgeMsg message, final String name, final Object object) {
    final Class<?> clazz = object.getClass();
    FudgeContext fudgeContext = OpenGammaFudgeContext.getInstance();
    final FudgeFieldType fieldType = fudgeContext.getTypeDictionary().getByJavaType(clazz);
    if (isNative(fieldType, object) && !(fieldType instanceof SecondaryFieldType)) {
      message.add(name, null, fieldType, object);
    } else {
      // look up a custom or default builder and embed as sub-message
      final MutableFudgeMsg submsg = serializer.objectToFudgeMsg(object);
      if (!fudgeContext.getObjectDictionary().isDefaultObject(clazz)) {
        if (submsg.getByOrdinal(TYPES_HEADER_ORDINAL) == null) {
          FudgeSerializer.addClassHeader(submsg, clazz, Object.class);
        }
      }
      message.add(name, null, FudgeWireType.SUB_MESSAGE, submsg);
    }

  }
  
  private static boolean isNative(final FudgeFieldType fieldType, final Object object) {
    if (fieldType == null) {
      return false;
    }
    return FudgeWireType.SUB_MESSAGE.equals(fieldType) == false ||
            (FudgeWireType.SUB_MESSAGE.equals(fieldType) && object instanceof FudgeMsg);
  }

}
