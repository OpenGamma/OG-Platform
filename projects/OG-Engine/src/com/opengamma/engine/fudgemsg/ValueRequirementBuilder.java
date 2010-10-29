/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.FudgeFieldChecker;

/**
 * 
 */
@FudgeBuilderFor(ValueRequirement.class)
public class ValueRequirementBuilder implements FudgeBuilder<ValueRequirement> {
  /**
   * Fudge field name.
   */
  public static final String VALUE_NAME_FIELD_NAME = "valueName";
  /**
   * Fudge field name.
   */
  public static final String CONSTRAINTS_FIELD_NAME = "constraints";

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, ValueRequirement object) {
    MutableFudgeFieldContainer msg = context.newMessage();
    String valueName = object.getValueName();
    msg.add(VALUE_NAME_FIELD_NAME, valueName);
    ComputationTargetSpecificationBuilder.addMessageFields(context, msg, object.getTargetSpecification());
    if (!object.getConstraints().isEmpty()) {
      context.objectToFudgeMsg(msg, CONSTRAINTS_FIELD_NAME, null, object.getConstraints());
    }
    return msg;
  }

  @Override
  public ValueRequirement buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    String valueName = message.getString(VALUE_NAME_FIELD_NAME);
    FudgeFieldChecker.notNull(valueName, "Fudge message is not a ValueRequirement - field 'valueName' is not present");
    ComputationTargetSpecification targetSpecification = ComputationTargetSpecificationBuilder.buildObjectImpl(context, message);
    FudgeFieldChecker.notNull(targetSpecification, "Fudge message is not a ValueRequirement - field 'computationTargetSpecification' is not present");
    FudgeField constraints = message.getByName(CONSTRAINTS_FIELD_NAME);
    if (constraints != null) {
      return new ValueRequirement(valueName, targetSpecification, context.fieldValueToObject(ValueProperties.class, constraints));
    } else {
      return new ValueRequirement(valueName, targetSpecification);
    }
  }

}
