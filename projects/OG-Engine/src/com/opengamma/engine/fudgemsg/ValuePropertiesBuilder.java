/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.types.FudgeMsgFieldType;
import org.fudgemsg.types.IndicatorFieldType;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.types.StringFieldType;

import com.opengamma.engine.value.ValueProperties;

/**
 * Fudge message builder for {@link ValueProperties}. Format is one field per property, named after the property.
 * If the property is a wild-card, an indicator is used. If the property has a single value, a string is used. If
 * the property has multiple values, a sub-message containing unnamed fields with the string values is used.
 */
@FudgeBuilderFor(ValueProperties.class)
public class ValuePropertiesBuilder implements FudgeBuilder<ValueProperties> {

  @Override
  public MutableFudgeFieldContainer buildMessage(final FudgeSerializationContext context, final ValueProperties object) {
    final MutableFudgeFieldContainer message = context.newMessage();
    for (String propertyName : object.getProperties()) {
      final Set<String> propertyValues = object.getValues(propertyName);
      if (propertyValues.isEmpty()) {
        message.add(propertyName, null, IndicatorFieldType.INSTANCE, IndicatorType.INSTANCE);
      } else if (propertyValues.size() == 1) {
        message.add(propertyName, null, StringFieldType.INSTANCE, propertyValues.iterator().next());
      } else {
        final MutableFudgeFieldContainer subMessage = context.newMessage();
        for (String propertyValue : propertyValues) {
          subMessage.add(null, null, StringFieldType.INSTANCE, propertyValue);
        }
        message.add(propertyName, null, FudgeMsgFieldType.INSTANCE, subMessage);
      }
    }
    return message;
  }

  @Override
  public ValueProperties buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    final ValueProperties.Builder builder = ValueProperties.builder();
    for (FudgeField field : message) {
      final String propertyName = field.getName();
      if (propertyName == null) {
        // Not valid; could be the class info if that was added
        continue;
      }
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
          for (FudgeField value : subMessage) {
            values.add(subMessage.getFieldValue(String.class, value));
          }
          builder.with(propertyName, values);
          break;
        }
      }
    }
    return builder.get();
  }

}
