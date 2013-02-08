/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.apache.commons.lang.Validate;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Fudge message builder for {@code ValueSpecification}.
 */
@FudgeBuilderFor(ValueSpecification.class)
public class ValueSpecificationFudgeBuilder implements FudgeBuilder<ValueSpecification> {

  /**
   * Fudge field name for valueName.
   */
  private static final String VALUE_NAME_KEY = "valueName";

  /**
   * Fudge field name for properties.
   */
  private static final String PROPERTIES_KEY = "properties";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ValueSpecification object) {
    MutableFudgeMsg msg = serializer.newMessage();
    msg.add(VALUE_NAME_KEY, null, FudgeWireType.STRING, object.getValueName());
    ComputationTargetReferenceFudgeBuilder.buildMessageImpl(serializer, msg, object.getTargetSpecification());
    serializer.addToMessage(msg, PROPERTIES_KEY, null, object.getProperties());
    return msg;
  }

  @Override
  public ValueSpecification buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    FudgeField fudgeField = message.getByName(VALUE_NAME_KEY);
    Validate.notNull(fudgeField, "Fudge message is not a ValueSpecification - field '" + VALUE_NAME_KEY + "' is not present");
    final String valueName = message.getFieldValue(String.class, fudgeField);
    final ComputationTargetSpecification targetSpecification = ComputationTargetReferenceFudgeBuilder.buildObjectImpl(deserializer, message).getSpecification();
    fudgeField = message.getByName(PROPERTIES_KEY);
    Validate.notNull(fudgeField, "Fudge message is not a ValueSpecification - field '" + PROPERTIES_KEY + "' is not present");
    final ValueProperties properties = deserializer.fieldValueToObject(ValueProperties.class, fudgeField);
    return new ValueSpecification(valueName, targetSpecification, properties);
  }

}
