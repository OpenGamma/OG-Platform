/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * A function that takes in a structure (e.g. yield curve, volatility surface) and produces a
 * modified version of the original as output.
 * <p>
 * The manipulation to be performed is specified by an implementation of the
 * {@link StructureManipulator} interface. The particular instance to be used will be obtained
 * as a FunctionParameter via the executionContext passed in through the execute method.
 */
public final class StructureManipulationFunction extends IntrinsicFunction {

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
   * Private constructor to prevent external instantiation. The {@link #INSTANCE} should be
   * used instead.
   */
  private StructureManipulationFunction() {
    super(UNIQUE_ID);
  }

  /**
   * Execute the function, performing a manipulation of the structured data which will come in via
   * the inputs parameter. The manipulation to actually undertake will be defined by a
   * {@link StructureManipulator} instance passed in through the executionContext. If no
   * manipulator is available the inputs are passed through unaffected (apart from a change to the
   * value specification to ensure they are still valid).
   *
   * @param executionContext execution context for the function, via which the parameters can be obtained
   * @param inputs the inputs to the function
   * @param target the target
   * @param desiredValues the values expected to be produced
   * @return a set of computed values corresponding to the desired values
   */
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {

    FunctionParameters parameters = executionContext.getFunctionParameters();
    if (parameters instanceof SimpleFunctionParameters) {

      SimpleFunctionParameters functionParameters = (SimpleFunctionParameters) parameters;
      StructureManipulator<Object> structureManipulator = functionParameters.getValue(EXPECTED_PARAMETER_NAME);

      ImmutableSet.Builder<ComputedValue> builder = ImmutableSet.builder();

      // Only one requirement is actually expected
      for (ValueRequirement requirement : desiredValues) {

        ValueProperties constraints = requirement.getConstraints().withoutAny("MANIPULATION_NODE");
        ValueRequirement stripped = new ValueRequirement(requirement.getValueName(), requirement.getTargetReference(), constraints);

        // As the inputs and outputs should have matching requirements and specs, we can get the
        // appropriate input using the required output
        Object structure = inputs.getValue(stripped);

        Object result;
        if (canHandle(structureManipulator, structure)) {
          result = structureManipulator.execute(structure);
        } else {
          result = structure;
        }
        builder.add(createComputedValue(target, requirement, result));
      }

      return builder.build();
    }

    // We didn't get the parameters we require so can't do any manipulation. However, we can just pass through
    // the original Yield Curve, modifying only the value specification in line with the value requirements
    return convertOriginalInputs(inputs, target, desiredValues);
  }

  private boolean canHandle(StructureManipulator<?> structureManipulator, Object structure) {
    return structure != null && structureManipulator.getExpectedType().isAssignableFrom(structure.getClass());
  }

  private Set<ComputedValue> convertOriginalInputs(FunctionInputs inputs,
                                                   ComputationTarget target,
                                                   Set<ValueRequirement> desiredValues) {

    Set<ComputedValue> results = new HashSet<>();
    for (ValueRequirement desiredValue : desiredValues) {

      ComputedValue computedValue = inputs.getComputedValue(desiredValue.getValueName());
      if (computedValue != null) {
        results.add(createComputedValue(target, desiredValue, computedValue.getValue()));
      }
    }

    return results;
  }

  private ComputedValue createComputedValue(ComputationTarget target, ValueRequirement requirement, Object value) {
    return new ComputedValue(createValueSpecification(target, requirement), value);
  }

  private ValueSpecification createValueSpecification(ComputationTarget target, ValueRequirement requirement) {
    return new ValueSpecification(requirement.getValueName(), target.toSpecification(), requirement.getConstraints());
  }

  @Override
  public boolean canHandleMissingInputs() {
    return false;
  }
}
