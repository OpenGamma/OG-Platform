/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.types.StringFieldType;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.FudgeFieldChecker;

/**
 * Fudge message builder for {@code ValueSpecification}.
 */
@FudgeBuilderFor(ValueSpecification.class)
public class ValueSpecificationBuilder implements FudgeBuilder<ValueSpecification> {

  /**
   * Fudge field name for valueName.
   */
  private static final String VALUE_NAME_KEY = "valueName";

  /**
   * Fudge field name for properties.
   */
  private static final String PROPERTIES_KEY = "properties";

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, ValueSpecification object) {
    MutableFudgeFieldContainer msg = context.newMessage();
    msg.add(VALUE_NAME_KEY, null, StringFieldType.INSTANCE, object.getValueName());
    ComputationTargetSpecificationBuilder.addMessageFields(context, msg, object.getTargetSpecification());
    context.objectToFudgeMsg(msg, PROPERTIES_KEY, null, object.getProperties());
    return msg;
  }

  @Override
  public ValueSpecification buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    FudgeField fudgeField = message.getByName(VALUE_NAME_KEY);
    FudgeFieldChecker.notNull(fudgeField, "Fudge message is not a ValueSpecification - field '" + VALUE_NAME_KEY + "' is not present");
    final String valueName = message.getFieldValue(String.class, fudgeField);
    final ComputationTargetSpecification targetSpecification = ComputationTargetSpecificationBuilder.buildObjectImpl(context, message);
    fudgeField = message.getByName(PROPERTIES_KEY);
    FudgeFieldChecker.notNull(fudgeField, "Fudge message is not a ValueSpecification - field '" + PROPERTIES_KEY + "' is not present");
    final ValueProperties properties = context.fieldValueToObject(ValueProperties.class, fudgeField);
    return new ValueSpecification(valueName, targetSpecification, properties);
  }

}
