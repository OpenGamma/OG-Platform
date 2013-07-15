/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Instant;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Fudge message builder for {@link DependencyNode}.
 */
@FudgeBuilderFor(DependencyNode.class)
public class DependencyNodeFudgeBuilder implements FudgeBuilder<DependencyNode> {

  private static final String COMPUTATION_TARGET_FIELD = "target";
  private static final String PARAMETERIZED_FUNCTION_UNIQUE_ID_FIELD = "parameterizedFunctionUniqueId";
  private static final String FUNCTION_PARAMETERS_FIELD = "functionParameters";
  private static final String FUNCTION_UNIQUE_ID_FIELD = "functionUniqueId";
  private static final String FUNCTION_SHORT_NAME_FIELD = "functionShortName";
  private static final String INPUT_VALUES_FIELD = "inputValues";
  private static final String OUTPUT_VALUES_FIELD = "outputValues";
  private static final String TERMINAL_OUTPUT_VALUES_FIELD = "terminalOutputValues";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, DependencyNode node) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, COMPUTATION_TARGET_FIELD, null, node.getComputationTarget());
    if (node.getFunction() != null) {
      msg.add(PARAMETERIZED_FUNCTION_UNIQUE_ID_FIELD, null, node.getFunction().getUniqueId());
      serializer.addToMessageWithClassHeaders(msg, FUNCTION_PARAMETERS_FIELD, null, node.getFunction().getParameters());
      FunctionDefinition functionDefinition = node.getFunction().getFunction().getFunctionDefinition();
      msg.add(FUNCTION_UNIQUE_ID_FIELD, functionDefinition.getUniqueId());
      msg.add(FUNCTION_SHORT_NAME_FIELD, functionDefinition.getShortName());
    }
    serializer.addToMessage(msg, INPUT_VALUES_FIELD, null, node.getInputValues());
    serializer.addToMessage(msg, OUTPUT_VALUES_FIELD, null, node.getOutputValues());
    serializer.addToMessage(msg, TERMINAL_OUTPUT_VALUES_FIELD, null, node.getTerminalOutputValues());
    return msg;
  }

  @SuppressWarnings("unchecked")
  @Override
  public DependencyNode buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    ComputationTargetSpecification target = deserializer.fieldValueToObject(ComputationTargetReference.class, msg.getByName(COMPUTATION_TARGET_FIELD)).getSpecification();
    
    String parameterizedFunctionUniqueId = msg.getString(PARAMETERIZED_FUNCTION_UNIQUE_ID_FIELD);
    FudgeField functionParametersField = msg.getByName(FUNCTION_PARAMETERS_FIELD);
    FunctionParameters functionParameters = functionParametersField != null ? deserializer.fieldValueToObject(FunctionParameters.class, functionParametersField) : null;
    
    String functionShortName = msg.getString(FUNCTION_SHORT_NAME_FIELD);
    String functionUniqueId = msg.getString(FUNCTION_UNIQUE_ID_FIELD);
    
    Set<ValueSpecification> inputValues = deserializer.fieldValueToObject(Set.class, msg.getByName(INPUT_VALUES_FIELD));
    Set<ValueSpecification> outputValues = deserializer.fieldValueToObject(Set.class, msg.getByName(OUTPUT_VALUES_FIELD));
    Set<ValueSpecification> terminalOutputValues = deserializer.fieldValueToObject(Set.class, msg.getByName(TERMINAL_OUTPUT_VALUES_FIELD));
    
    DependencyNode node = new DependencyNode(target);
    
    CompiledFunctionDefinition function = new CompiledFunctionDefinitionStub(target.getType(), functionUniqueId, functionShortName);
    ParameterizedFunction parameterizedFunction = new ParameterizedFunction(function, functionParameters);
    parameterizedFunction.setUniqueId(parameterizedFunctionUniqueId);
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
   * It is both impractical and undesirable to serialise the full function definition, so deserialised objects instead contain a stub with selected details.
   */
  private static class CompiledFunctionDefinitionStub implements CompiledFunctionDefinition {

    private final ComputationTargetType _targetType;
    private final FunctionDefinition _functionDefinition;
    
    public CompiledFunctionDefinitionStub(ComputationTargetType targetType, String uniqueId, String shortName) {
      _targetType = targetType;
      _functionDefinition = new FunctionDefinitionStub(uniqueId, shortName);
    }
    
    @Override
    public FunctionDefinition getFunctionDefinition() {
      return _functionDefinition;
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
    
    @Override
    public boolean canHandleMissingRequirements() {
      throw new UnsupportedOperationException();
    }

  }
  
  private static class FunctionDefinitionStub implements FunctionDefinition {
    
    private final String _uniqueId;
    private final String _shortName;
    
    public FunctionDefinitionStub(String uniqueId, String shortName) {
      _uniqueId = uniqueId;
      _shortName = shortName;
    }

    @Override
    public void init(FunctionCompilationContext context) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CompiledFunctionDefinition compile(FunctionCompilationContext context, Instant atInstant) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getUniqueId() {
      return _uniqueId;
    }

    @Override
    public String getShortName() {
      return _shortName;
    }

    @Override
    public FunctionParameters getDefaultParameters() {
      throw new UnsupportedOperationException();
    }
    
  }

}
