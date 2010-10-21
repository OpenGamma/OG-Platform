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
   * Fudge field name for requirementSpecification
   */
  private static final String REQUIREMENT_SPECIFICATION_KEY = "requirementSpecification";
  /**
   * Fudge field name for functionUniqueId
   */
  private static final String FUNCTION_UNIQUE_ID_KEY = "functionUniqueId";
  /**
   * Fudge field name for properties.
   */
  private static final String PROPERTIES_KEY = "properties";

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, ValueSpecification object) {
    MutableFudgeFieldContainer msg = context.newMessage();
    ValueRequirement requirementSpecification = object.getRequirementSpecification();
    if (requirementSpecification != null) {
      context.objectToFudgeMsg(msg, REQUIREMENT_SPECIFICATION_KEY, null, requirementSpecification);
    }
    String functionUniqueId = object.getFunctionUniqueId();
    if (functionUniqueId != null) {
      msg.add(FUNCTION_UNIQUE_ID_KEY, null, functionUniqueId);
    }
    context.objectToFudgeMsg(msg, PROPERTIES_KEY, null, object.getProperties());
    return msg;
  }

  @Override
  public ValueSpecification buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    FudgeField fudgeField = message.getByName(REQUIREMENT_SPECIFICATION_KEY);
    FudgeFieldChecker.notNull(fudgeField, "Fudge message is not a ValueSpecification - field 'requirementSpecification' is not present");
    ValueRequirement requirementSpecification = context.fieldValueToObject(ValueRequirement.class, fudgeField);
    FudgeFieldChecker.notNull(requirementSpecification, "Fudge message is not a ValueSpecification - field 'requirementSpecification' is not ValueRequirement message");
    String functionUniqueId = message.getString(FUNCTION_UNIQUE_ID_KEY);
    FudgeFieldChecker.notNull(functionUniqueId, "Fudge message is not a ValueSpecification - field 'functionUniqueId' is not present");
    fudgeField = message.getByName(PROPERTIES_KEY);
    if (fudgeField != null) {
      return new ValueSpecification(requirementSpecification, context.fieldValueToObject(ValueProperties.class, fudgeField));
    } else {
      return new ValueSpecification(requirementSpecification, ValueProperties.none());
    }
  }

}
