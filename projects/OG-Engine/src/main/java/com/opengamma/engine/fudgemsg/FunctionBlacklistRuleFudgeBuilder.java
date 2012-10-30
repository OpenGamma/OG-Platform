/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.ArrayList;
import java.util.Collection;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.blacklist.FunctionBlacklistRule;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Fudge builder for {@link FunctionBlacklistRule}.
 * 
 * <pre>
 * message FunctionBlacklistRule {
 *   optional string functionId;                       // function identifier to match on
 *   optional FunctionParameters functionParameters;   // function parameters to match on
 *   optional ComputationTargetSpecification target;   // target to match on
 *   optional ValueSpecification[] inputs;             // inputs to match on
 *   optional ValueSpecification[] outputs;            // outputs to match on
 * }
 * </pre>
 */
@FudgeBuilderFor(FunctionBlacklistRule.class)
public class FunctionBlacklistRuleFudgeBuilder implements FudgeBuilder<FunctionBlacklistRule> {

  private static final String FUNCTION_ID_FIELD = "functionId";
  private static final String FUNCTION_PARAMETERS_FIELD = "functionParameters";
  private static final String TARGET_FIELD = "target";
  private static final String INPUTS_FIELD = "inputs";
  private static final String OUTPUTS_FIELD = "outputs";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FunctionBlacklistRule object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, FUNCTION_ID_FIELD, null, object.getFunctionIdentifier());
    serializer.addToMessageWithClassHeaders(msg, FUNCTION_PARAMETERS_FIELD, null, object.getFunctionParameters(), FunctionParameters.class);
    serializer.addToMessage(msg, TARGET_FIELD, null, object.getTarget());
    if (object.getInputs() != null) {
      final MutableFudgeMsg inputs = msg.addSubMessage(INPUTS_FIELD, null);
      for (ValueSpecification input : object.getInputs()) {
        serializer.addToMessage(inputs, null, null, input);
      }
    }
    if (object.getOutputs() != null) {
      final MutableFudgeMsg outputs = msg.addSubMessage(OUTPUTS_FIELD, null);
      for (ValueSpecification output : object.getOutputs()) {
        serializer.addToMessage(outputs, null, null, output);
      }
    }
    return msg;
  }

  @Override
  public FunctionBlacklistRule buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final FunctionBlacklistRule rule = new FunctionBlacklistRule();
    FudgeField field = message.getByName(FUNCTION_ID_FIELD);
    if (field != null) {
      rule.setFunctionIdentifier(message.getFieldValue(String.class, field));
    }
    field = message.getByName(FUNCTION_PARAMETERS_FIELD);
    if (field != null) {
      rule.setFunctionParameters(deserializer.fieldValueToObject(FunctionParameters.class, field));
    }
    field = message.getByName(TARGET_FIELD);
    if (field != null) {
      rule.setTarget(deserializer.fieldValueToObject(ComputationTargetReference.class, field).getSpecification());
    }
    field = message.getByName(INPUTS_FIELD);
    if (field != null) {
      final FudgeMsg fieldValue = message.getFieldValue(FudgeMsg.class, field);
      final Collection<ValueSpecification> inputs = new ArrayList<ValueSpecification>(fieldValue.getNumFields());
      for (FudgeField input : fieldValue) {
        inputs.add(deserializer.fieldValueToObject(ValueSpecification.class, input));
      }
      rule.setInputs(inputs);
    }
    field = message.getByName(OUTPUTS_FIELD);
    if (field != null) {
      final FudgeMsg fieldValue = message.getFieldValue(FudgeMsg.class, field);
      final Collection<ValueSpecification> outputs = new ArrayList<ValueSpecification>(fieldValue.getNumFields());
      for (FudgeField output : fieldValue) {
        outputs.add(deserializer.fieldValueToObject(ValueSpecification.class, output));
      }
      rule.setOutputs(outputs);
    }
    return rule;
  }

}
