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

import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;

/**
 * 
 */
@FudgeBuilderFor(ValueRequirement.class)
public class ValueRequirementFudgeBuilder implements FudgeBuilder<ValueRequirement> {
  /**
   * Fudge field name.
   */
  public static final String VALUE_NAME_FIELD_NAME = "valueName";
  /**
   * Fudge field name.
   */
  public static final String CONSTRAINTS_FIELD_NAME = "constraints";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ValueRequirement object) {
    MutableFudgeMsg msg = serializer.newMessage();
    String valueName = object.getValueName();
    msg.add(VALUE_NAME_FIELD_NAME, valueName);
    ComputationTargetReferenceFudgeBuilder.buildMessageImpl(serializer, msg, object.getTargetReference());
    if (!object.getConstraints().isEmpty()) {
      serializer.addToMessage(msg, CONSTRAINTS_FIELD_NAME, null, object.getConstraints());
    }
    return msg;
  }

  @Override
  public ValueRequirement buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    String valueName = message.getString(VALUE_NAME_FIELD_NAME);
    Validate.notNull(valueName, "Fudge message is not a ValueRequirement - field 'valueName' is not present");
    ComputationTargetReference targetReference = ComputationTargetReferenceFudgeBuilder.buildObjectImpl(deserializer, message);
    FudgeField constraints = message.getByName(CONSTRAINTS_FIELD_NAME);
    if (constraints != null) {
      return new ValueRequirement(valueName, targetReference, deserializer.fieldValueToObject(ValueProperties.class, constraints));
    } else {
      return new ValueRequirement(valueName, targetReference);
    }
  }

}
