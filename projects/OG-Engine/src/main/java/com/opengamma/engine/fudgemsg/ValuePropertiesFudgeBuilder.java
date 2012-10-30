/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.NearlyInfinitePropertiesImpl;

/**
 * Fudge message builder for {@link ValueProperties}.
 * 
 *  For finite properties there is a field named with:
 *    Format is one field per property, named after the property.
 *    If the property is a wild-card, an indicator is used. If the property has a single value, a string is used. If
 *      the property has multiple values, a sub-message containing unnamed fields with the string values is used.
 *  For infinite properties there is a field named without:
 *    Format is one field per property, named after the property.
 * 
 * See {@link NearlyInfinitePropertiesImpl} for an exception to this builder
 */
@GenericFudgeBuilderFor(ValueProperties.class)
public class ValuePropertiesFudgeBuilder implements FudgeBuilder<ValueProperties> {

  private static final String WITH_FIELD = "with";
  private static final String WITHOUT_FIELD = "without";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ValueProperties object) {
    final MutableFudgeMsg message = serializer.newMessage();

    if (!object.isEmpty()) {
      if (object.getProperties().isEmpty()) {
        //Infinite or NearlyInfinite

        MutableFudgeMsg withoutMessage = serializer.newMessage();
        message.add(WITHOUT_FIELD, withoutMessage);

        if (ValueProperties.NearlyInfinitePropertiesImpl.class.isInstance(object)) {
          ValueProperties.NearlyInfinitePropertiesImpl nearlyInifite = (ValueProperties.NearlyInfinitePropertiesImpl) (object);
          int counter = 0;
          for (String without : nearlyInifite.getWithout()) {
            withoutMessage.add(String.valueOf(counter++), without);
          }
        }

      } else {
        MutableFudgeMsg withMessage = serializer.newMessage();
        message.add(WITH_FIELD, withMessage);

        for (String propertyName : object.getProperties()) {
          final Set<String> propertyValues = object.getValues(propertyName);
          final boolean optional = object.isOptional(propertyName);
          if ((propertyValues.size() > 1) || optional) {
            final MutableFudgeMsg subMessage = serializer.newMessage();
            if (optional) {
              subMessage.add("optional", null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
            }
            int counter = 0;
            for (String propertyValue : propertyValues) {
              subMessage.add(String.valueOf(counter++), null, FudgeWireType.STRING, propertyValue);
            }
            withMessage.add(propertyName, null, FudgeWireType.SUB_MESSAGE, subMessage);
          } else if (propertyValues.isEmpty()) {
            withMessage.add(propertyName, null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
          } else {
            withMessage.add(propertyName, null, FudgeWireType.STRING, propertyValues.iterator().next());
          }
        }
      }
    }
    return message;
  }

  @Override
  public ValueProperties buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    if (message.isEmpty()) {
      return ValueProperties.none();
    }
    final ValueProperties.Builder builder = ValueProperties.builder();

    FudgeMsg withoutMessage = message.getMessage(WITHOUT_FIELD);
    if (withoutMessage != null) {
      //Infinite or NearlyInfinite
      ValueProperties ret = ValueProperties.all();
      for (FudgeField fudgeField : withoutMessage) {
        ret = ret.withoutAny((String) fudgeField.getValue());
      }
      return ret;
    }

    FudgeMsg withMessage = message.getMessage(WITH_FIELD);
    if (withMessage == null) {
      return ValueProperties.none();
    }
    for (FudgeField field : withMessage) {
      final String propertyName = field.getName();
      
      switch (field.getType().getTypeId()) {
        case FudgeWireType.INDICATOR_TYPE_ID:
          builder.withAny(propertyName);
          break;
        case FudgeWireType.STRING_TYPE_ID:
          builder.with(propertyName, (String) field.getValue());
          break;
        case FudgeWireType.SUB_MESSAGE_TYPE_ID: {
          final FudgeMsg subMessage = (FudgeMsg) field.getValue();
          final List<String> values = new ArrayList<String>(subMessage.getNumFields());
          for (FudgeField subField : subMessage) {
            final String value = subMessage.getFieldValue(String.class, subField);
            if (value != null) {
              values.add(value);
            } else {
              builder.withOptional(propertyName);
            }
          }
          if (!values.isEmpty()) {
            builder.with(propertyName, values);
          }
          break;
        }
      }
    }
    return builder.get();
  }

}
