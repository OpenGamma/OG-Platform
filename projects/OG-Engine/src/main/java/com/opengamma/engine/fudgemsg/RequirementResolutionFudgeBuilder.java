/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.depgraph.DependencyNodeFunction;
import com.opengamma.engine.depgraph.ambiguity.FullRequirementResolution;
import com.opengamma.engine.depgraph.ambiguity.RequirementResolution;
import com.opengamma.engine.depgraph.impl.DependencyNodeFunctionImpl;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Fudge builder for {@link RequirementResolution}.
 * 
 * <pre>
 * message RequirementResolution {
 *   required ValueSpecification specification;   // the resolved specification
 *   required string function;                    // the function identifier
 *   optional FunctionParameters parameters;      // the function parameters, omitted if empty
 *   optional FullRequirementResolution[] inputs; // the resolved inputs, if any
 * }
 * </pre>
 */
@FudgeBuilderFor(RequirementResolution.class)
public class RequirementResolutionFudgeBuilder implements FudgeBuilder<RequirementResolution> {

  // TODO: Improve efficiency - the targets and value names will be duplicated quite a lot in the specifications

  private static final String SPECIFICATION_FIELD_NAME = "specification";
  private static final String FUNCTION_FIELD_NAME = "function";
  private static final String PARAMETERS_FIELD_NAME = "parameters";
  private static final String INPUTS_FIELD_NAME = "inputs";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final RequirementResolution object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, SPECIFICATION_FIELD_NAME, null, object.getSpecification());
    serializer.addToMessage(msg, FUNCTION_FIELD_NAME, null, object.getFunction().getFunctionId());
    if (!(object.getFunction().getParameters() instanceof EmptyFunctionParameters)) {
      serializer.addToMessageWithClassHeaders(msg, PARAMETERS_FIELD_NAME, null, object.getFunction().getParameters(), FunctionParameters.class);
    }
    final MutableFudgeMsg inputsMsg = msg.addSubMessage(INPUTS_FIELD_NAME, null);
    for (FullRequirementResolution input : object.getInputs()) {
      serializer.addToMessage(inputsMsg, null, null, input);
    }
    return msg;
  }

  private DependencyNodeFunction decodeFunction(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final String functionId = msg.getString(FUNCTION_FIELD_NAME);
    final FudgeField parameters = msg.getByName(PARAMETERS_FIELD_NAME);
    if (parameters == null) {
      return DependencyNodeFunctionImpl.of(functionId, EmptyFunctionParameters.INSTANCE);
    } else {
      return DependencyNodeFunctionImpl.of(functionId, deserializer.fieldValueToObject(FunctionParameters.class, parameters));
    }
  }

  private Collection<FullRequirementResolution> decodeInputs(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final FudgeMsg inputsMsg = msg.getMessage(INPUTS_FIELD_NAME);
    if (inputsMsg == null) {
      return ImmutableSet.of();
    }
    final Collection<FullRequirementResolution> inputs = new ArrayList<FullRequirementResolution>(inputsMsg.getNumFields());
    for (FudgeField inputField : inputsMsg) {
      inputs.add(deserializer.fieldValueToObject(FullRequirementResolution.class, inputField));
    }
    return inputs;
  }

  @Override
  public RequirementResolution buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final ValueSpecification specification = deserializer.fieldValueToObject(ValueSpecification.class, msg.getByName(SPECIFICATION_FIELD_NAME));
    final DependencyNodeFunction function = decodeFunction(deserializer, msg);
    final Collection<FullRequirementResolution> inputs = decodeInputs(deserializer, msg);
    return new RequirementResolution(specification, function, inputs);
  }

}
