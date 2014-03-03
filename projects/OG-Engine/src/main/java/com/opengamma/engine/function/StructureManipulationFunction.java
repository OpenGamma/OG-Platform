/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * A function that takes in a structure (e.g. yield curve, volatility surface) and produces a modified version of the original as output.
 * <p>
 * The manipulation to be performed is specified by an implementation of the {@link StructureManipulator} interface. The particular instance to be used will be obtained as a FunctionParameter via the
 * executionContext passed in through the execute method.
 */
public final class StructureManipulationFunction extends IntrinsicFunction {

  private static final Logger s_logger = LoggerFactory.getLogger(StructureManipulationFunction.class);

  /**
   * Shared instance.
   */
  public static final StructureManipulationFunction INSTANCE = new StructureManipulationFunction();

  /**
   * Preferred identifier this function will be available in a repository as.
   */
  public static final String UNIQUE_ID = "StructureManipulator";

  /**
   * The expected name of the parameter containing the {@link StructureManipulator} class.
   */
  public static final String EXPECTED_PARAMETER_NAME = "STRUCTURE_MANIPULATOR";

  /**
   * Private constructor to prevent external instantiation. The {@link #INSTANCE} should be used instead.
   */
  private StructureManipulationFunction() {
    super(UNIQUE_ID);
  }

  /**
   * Execute the function, performing a manipulation of the structured data which will come in via the inputs parameter. The manipulation to actually undertake will be defined by a
   * {@link StructureManipulator} instance passed in through the executionContext. If no manipulator is available the inputs are passed through unaffected (apart from a change to the value
   * specification to ensure they are still valid).
   * 
   * @param executionContext execution context for the function, via which the parameters can be obtained
   * @param inputs the inputs to the function
   * @param target the target
   * @param desiredValues the values expected to be produced
   * @return a set of computed values corresponding to the desired values
   */
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final StructureManipulator<Object> structureManipulator;
    final FunctionParameters parameters = executionContext.getFunctionParameters();
    if (parameters instanceof SimpleFunctionParameters) {
      final SimpleFunctionParameters functionParameters = (SimpleFunctionParameters) parameters;
      structureManipulator = functionParameters.getValue(EXPECTED_PARAMETER_NAME);
    } else {
      structureManipulator = null;
    }
    final Collection<ComputedValue> inputValues = inputs.getAllValues();
    // Only one requirement is expected, but cope with multiple ones just in case
    final Set<ComputedValue> result = Sets.newHashSetWithExpectedSize(inputValues.size());
    for (ComputedValue inputValue : inputValues) {
      final Object inputValueObject = inputValue.getValue();
      final Object outputValueObject;
      if ((inputValueObject != null) && (structureManipulator != null) && structureManipulator.getExpectedType().isAssignableFrom(inputValueObject.getClass())) {
        outputValueObject = structureManipulator.execute(inputValueObject, inputValue.getSpecification(), executionContext);
        s_logger.debug("changed value for target {} from {} to {}", target, inputValueObject, outputValueObject);
      } else {
        outputValueObject = inputValueObject;
      }
      final ValueSpecification inputValueSpec = inputValue.getSpecification();
      final ValueProperties inputProperties = inputValueSpec.getProperties();
      final String inputFunction = inputProperties.getStrictValue(ValuePropertyNames.FUNCTION);
      final ValueProperties outputProperties = inputProperties.copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, inputFunction + UNIQUE_ID).get();
      final ValueSpecification outputValueSpec = new ValueSpecification(inputValueSpec.getValueName(), inputValueSpec.getTargetSpecification(), outputProperties);
      result.add(new ComputedValue(outputValueSpec, outputValueObject));
    }
    return result;
  }

  @Override
  public boolean canHandleMissingInputs() {
    return false;
  }
}
