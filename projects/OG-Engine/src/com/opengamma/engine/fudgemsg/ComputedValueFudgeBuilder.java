/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calcnode.InvocationResult;
import org.apache.commons.lang.Validate;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import java.util.Set;

/**
 * Fudge message builder for {@code ComputedValue}.
 */
@FudgeBuilderFor(ComputedValue.class)
public class ComputedValueFudgeBuilder implements FudgeBuilder<ComputedValue> {
  /**
   * Fudge field name.
   */
  private static final String SPECIFICATION_KEY = "specification";
  /**
   * Fudge field name.
   */
  private static final String VALUE_KEY = "value";

  private static final String INVOCATION_RESULT_FIELD_NAME = "result";
  private static final String EXCEPTION_CLASS_FIELD_NAME = "exceptionClass";
  private static final String EXCEPTION_MSG_FIELD_NAME = "exceptionMsg";
  private static final String STACK_TRACE_FIELD_NAME = "stackTrace";
  private static final String MISSING_INPUTS_FIELD_NAME = "missingInputs";
  private static final String REQUIREMENTS_FIELD_NAME = "requirements";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ComputedValue object) {
    MutableFudgeMsg msg = serializer.newMessage();
    ValueSpecification specification = object.getSpecification();
    if (specification != null) {
      serializer.addToMessage(msg, SPECIFICATION_KEY, null, specification);
    }
    if (object.getExceptionClass() != null) {
      serializer.addToMessage(msg, EXCEPTION_CLASS_FIELD_NAME, null, object.getExceptionClass());
    }
    if (object.getExceptionMsg() != null) {
      serializer.addToMessage(msg, EXCEPTION_MSG_FIELD_NAME, null, object.getExceptionMsg());
    }
    if (object.getInvocationResult() != null) {
      serializer.addToMessage(msg, INVOCATION_RESULT_FIELD_NAME, null, object.getInvocationResult());
    }
    if (object.getMissingInputs() != null) {
      serializer.addToMessage(msg, MISSING_INPUTS_FIELD_NAME, null, object.getMissingInputs());
    }
    if (object.getRequirements() != null) {
      serializer.addToMessage(msg, REQUIREMENTS_FIELD_NAME, null, object.getRequirements());
    }
    if (object.getStackTrace() != null) {
      serializer.addToMessage(msg, STACK_TRACE_FIELD_NAME, null, object.getStackTrace());
    }
    Object value = object.getValue();
    if (value != null) {
      serializer.addToMessageWithClassHeaders(msg, VALUE_KEY, null, value);
    }
    return msg;
  }

  @Override
  public ComputedValue buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    FudgeField fudgeField = message.getByName(SPECIFICATION_KEY);
    Validate.notNull(fudgeField, "Fudge message is not a ComputedValue - field 'specification' is not present");
    ValueSpecification valueSpec = deserializer.fieldValueToObject(ValueSpecification.class, fudgeField);
    fudgeField = message.getByName(VALUE_KEY);
    Validate.notNull(fudgeField, "Fudge message is not a ComputedValue - field 'value' is not present");
    Object valueObject = deserializer.fieldValueToObject(fudgeField);
    ComputedValue computedValue = new ComputedValue(valueSpec, valueObject);

    String invocationResultName = message.getString(INVOCATION_RESULT_FIELD_NAME);
    if(invocationResultName != null){
      InvocationResult invocationResult = InvocationResult.valueOf(invocationResultName);
      computedValue.setInvocationResult(invocationResult);
    }

    String exceptionClass = message.getString(EXCEPTION_CLASS_FIELD_NAME);
    String exceptionMsg = message.getString(EXCEPTION_MSG_FIELD_NAME);
    String stackTrace = message.getString(STACK_TRACE_FIELD_NAME);
    if (exceptionClass != null) {
      computedValue.setExceptionClass(exceptionClass);
    }
    if (exceptionMsg != null) {
      computedValue.setExceptionMsg(exceptionMsg);
    }
    if (stackTrace != null) {
      computedValue.setStackTrace(stackTrace);
    }
    FudgeField requirementField = message.getByName(REQUIREMENTS_FIELD_NAME);
    if (requirementField != null){
      Set<ValueRequirement> requirements = deserializer.fieldValueToObject(Set.class, requirementField);
      computedValue.setRequirements(requirements);
    }
    FudgeField missingInputsField = message.getByName(MISSING_INPUTS_FIELD_NAME);
    if(missingInputsField != null){
      Set<ValueSpecification> missingInputs = deserializer.fieldValueToObject(Set.class, missingInputsField);
      computedValue.setMissingInputs(missingInputs);
    }
    return computedValue;
  }
}
