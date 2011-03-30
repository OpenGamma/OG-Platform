/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.math.function.ParameterizedFunction;

/**
 * Fudge builders for com.opengamma.math.function.* classes
 */
final class MathFunction {

  private MathFunction() {
  }

  /**
   * Fudge builder for {@code ParameterizedFunction.SerializedForm}
   */
  @FudgeBuilderFor(ParameterizedFunction.SerializedForm.class)
  public static final class ParameterizedFunctionSerializedForm extends AbstractSerializedFormBuilder<ParameterizedFunction.SerializedForm> {

    private static final String FUNCTION_FIELD_NAME = "function";
    private static final String PARAMETERS_FIELD_NAME = "parameters";
    private static final String ARGUMENTS_FIELD_NAME = "arguments";

    @SuppressWarnings("unchecked")
    @Override
    public Object buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
      final ParameterizedFunction<Object, Object, ?> function = (ParameterizedFunction<Object, Object, ?>) context.fieldValueToObject(message.getByName(FUNCTION_FIELD_NAME));
      FudgeField x = message.getByName(PARAMETERS_FIELD_NAME);
      if (x != null) {
        return function.asFunctionOfParameters(context.fieldValueToObject(x));
      } else {
        x = message.getByName(ARGUMENTS_FIELD_NAME);
        if (x != null) {
          return function.asFunctionOfArguments(context.fieldValueToObject(x));
        } else {
          return function;
        }
      }
    }

    @Override
    public void buildMessage(final FudgeSerializationContext context, final MutableFudgeFieldContainer message, final ParameterizedFunction.SerializedForm object) {
      context.objectToFudgeMsgWithClassHeaders(message, FUNCTION_FIELD_NAME, null, substituteObject(object.getFunction()));
      if (object.getParameters() != null) {
        context.objectToFudgeMsgWithClassHeaders(message, PARAMETERS_FIELD_NAME, null, object.getParameters());
      } else {
        context.objectToFudgeMsgWithClassHeaders(message, ARGUMENTS_FIELD_NAME, null, object.getArguments());
      }
    }
  }

}
