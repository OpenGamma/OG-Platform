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
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.IndicatorType;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;
import org.fudgemsg.types.FudgeMsgFieldType;
import org.fudgemsg.types.IndicatorFieldType;
import org.fudgemsg.types.StringFieldType;

import com.opengamma.engine.value.ValueProperties;

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
 * See {@link NearlyInfiniteValuePropertiesBuilder} for an exception to this builder
 */
@GenericFudgeBuilderFor(ValueProperties.class)
public class ValuePropertiesBuilder implements FudgeBuilder<ValueProperties> {

  private static final String WITH_FIELD = "with";
  private static final String WITHOUT_FIELD = "without";

  @Override
  public MutableFudgeFieldContainer buildMessage(final FudgeSerializationContext context, final ValueProperties object) {
    final MutableFudgeFieldContainer message = context.newMessage();

    if (!object.isEmpty()) {
      if (object.getProperties().isEmpty()) {
        //Infinite or NearlyInfinite

        MutableFudgeFieldContainer withoutMessage = context.newMessage();
        message.add(WITHOUT_FIELD, withoutMessage);

        if (ValueProperties.NearlyInfinitePropertiesImpl.class.isInstance(object)) {
          ValueProperties.NearlyInfinitePropertiesImpl nearlyInifite = (ValueProperties.NearlyInfinitePropertiesImpl) (object);
          for (String without : nearlyInifite.getWithout()) {
            withoutMessage.add((String) null, without);
          }
        }

      } else {
        MutableFudgeFieldContainer withMessage = context.newMessage();
        message.add(WITH_FIELD, withMessage);

        for (String propertyName : object.getProperties()) {
          final Set<String> propertyValues = object.getValues(propertyName);
          final boolean optional = object.isOptional(propertyName);
          if ((propertyValues.size() > 1) || optional) {
            final MutableFudgeFieldContainer subMessage = context.newMessage();
            if (optional) {
              subMessage.add(null, null, IndicatorFieldType.INSTANCE, IndicatorType.INSTANCE);
            }
            for (String propertyValue : propertyValues) {
              subMessage.add(null, null, StringFieldType.INSTANCE, propertyValue);
            }
            withMessage.add(propertyName, null, FudgeMsgFieldType.INSTANCE, subMessage);
          } else if (propertyValues.isEmpty()) {
            withMessage.add(propertyName, null, IndicatorFieldType.INSTANCE, IndicatorType.INSTANCE);
          } else {
            withMessage.add(propertyName, null, StringFieldType.INSTANCE, propertyValues.iterator().next());
          }
        }
      }
    }
    return message;
  }

  @Override
  public ValueProperties buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    if (message.isEmpty()) {
      return ValueProperties.none();
    }
    final ValueProperties.Builder builder = ValueProperties.builder();

    FudgeFieldContainer withoutMessage = message.getMessage(WITHOUT_FIELD);
    if (withoutMessage != null) {
      //Infinite or NearlyInfinite
      ValueProperties ret = ValueProperties.all();
      for (FudgeField fudgeField : withoutMessage) {
        ret = ret.withoutAny((String) fudgeField.getValue());
      }
      return ret;
    }

    FudgeFieldContainer withMessage = message.getMessage(WITH_FIELD);
    if (withMessage == null)
    {
      return ValueProperties.none();
    }
    for (FudgeField field : withMessage) {
      final String propertyName = field.getName();
      
      switch (field.getType().getTypeId()) {
        case FudgeTypeDictionary.INDICATOR_TYPE_ID:
          builder.withAny(propertyName);
          break;
        case FudgeTypeDictionary.STRING_TYPE_ID:
          builder.with(propertyName, (String) field.getValue());
          break;
        case FudgeTypeDictionary.FUDGE_MSG_TYPE_ID: {
          final FudgeFieldContainer subMessage = (FudgeFieldContainer) field.getValue();
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
