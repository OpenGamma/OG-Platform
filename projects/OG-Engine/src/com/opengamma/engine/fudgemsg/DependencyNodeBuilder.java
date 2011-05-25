/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Fudge message builder for {@link DependencyNode}.
 */
@FudgeBuilderFor(DependencyNode.class)
public class DependencyNodeBuilder implements FudgeBuilder<DependencyNode> {

  private static final String COMPUTATION_TARGET_FIELD = "target";
  private static final String FUNCTION_UNIQUE_ID_FIELD = "functionUniqueId";
  private static final String FUNCTION_PARAMETERS_FIELD = "functionParameters";
  private static final String INPUT_VALUES_FIELD = "inputValues";
  private static final String OUTPUT_VALUES_FIELD = "outputValues";
  private static final String TERMINAL_OUTPUT_VALUES_FIELD = "terminalOutputValues";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, DependencyNode node) {
    MutableFudgeMsg msg = context.newMessage();
    context.addToMessage(msg, COMPUTATION_TARGET_FIELD, null, node.getComputationTarget());
    if (node.getFunction() != null) {
      msg.add(FUNCTION_UNIQUE_ID_FIELD, null, node.getFunction().getUniqueId());
      context.addToMessageWithClassHeaders(msg, FUNCTION_PARAMETERS_FIELD, null, node.getFunction().getParameters());
    }
    context.addToMessage(msg, INPUT_VALUES_FIELD, null, node.getInputValues());
    context.addToMessage(msg, OUTPUT_VALUES_FIELD, null, node.getOutputValues());
    context.addToMessage(msg, TERMINAL_OUTPUT_VALUES_FIELD, null, node.getTerminalOutputValues());
    return msg;
  }

  @SuppressWarnings("unchecked")
  @Override
  public DependencyNode buildObject(FudgeDeserializationContext context, FudgeMsg msg) {
    ComputationTarget target = context.fieldValueToObject(ComputationTarget.class, msg.getByName(COMPUTATION_TARGET_FIELD));
    
    String functionUniqueId = msg.getString(FUNCTION_UNIQUE_ID_FIELD);
    FudgeField functionParametersField = msg.getByName(FUNCTION_PARAMETERS_FIELD);
    FunctionParameters functionParameters = functionParametersField != null ? context.fieldValueToObject(FunctionParameters.class, functionParametersField) : null;
    
    Set<ValueSpecification> inputValues = context.fieldValueToObject(Set.class, msg.getByName(INPUT_VALUES_FIELD));
    Set<ValueSpecification> outputValues = context.fieldValueToObject(Set.class, msg.getByName(OUTPUT_VALUES_FIELD));
    Set<ValueSpecification> terminalOutputValues = context.fieldValueToObject(Set.class, msg.getByName(TERMINAL_OUTPUT_VALUES_FIELD));
    
    DependencyNode node = new DependencyNode(target);
    
    CompiledFunctionDefinition function = new CompiledFunctionDefinitionStub(target.getType());
    ParameterizedFunction parameterizedFunction = new ParameterizedFunction(function, functionParameters);
    parameterizedFunction.setUniqueId(functionUniqueId);
    node.setFunction(parameterizedFunction);
    
    for (ValueSpecification inputValue : inputValues) {
      node.addInputValue(inputValue);
    }
    for (ValueSpecification outputValue : outputValues) {
      node.addOutputValue(outputValue);
    }
    for (ValueSpecification terminalOutputValue : terminalOutputValues) {
      node.addTerminalOutputValue(terminalOutputValue);
    }
    return node;
  }
  
  /**
   * It is both impratical and undesirable to serialise the full function definition, so deserialised objects instead
   * contain a stub with selected details.
   */
  private static class CompiledFunctionDefinitionStub implements CompiledFunctionDefinition {

    private final ComputationTargetType _targetType;
    
    public CompiledFunctionDefinitionStub(ComputationTargetType targetType) {
      _targetType = targetType;
    }
    
    @Override
    public FunctionDefinition getFunctionDefinition() {
      throw new UnsupportedOperationException();
    }

    @Override
    public ComputationTargetType getTargetType() {
      return _targetType;
    }

    @Override
    public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Set<ValueRequirement> getAdditionalRequirements(FunctionCompilationContext context, ComputationTarget target, Set<ValueSpecification> inputs, Set<ValueSpecification> outputs) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Set<ValueSpecification> getRequiredLiveData() {
      return Collections.emptySet();
    }

    @Override
    public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
      throw new UnsupportedOperationException();
    }

    @Override
    public FunctionInvoker getFunctionInvoker() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Instant getEarliestInvocationTime() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Instant getLatestInvocationTime() {
      throw new UnsupportedOperationException();
    }
    
  }

}
