/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Calculates the present value of an interest rate future option using the SABR model
 */
public class IRFutureOptionSABRPresentValueFunction extends IRFutureOptionSABRFunction {
  /** The present value calculator */
  private static final PresentValueSABRCalculator CALCULATOR = PresentValueSABRCalculator.getInstance();

  /**
   * Default constructor
   */
  public IRFutureOptionSABRPresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getResult(final FunctionExecutionContext context, final Set<ValueRequirement> desiredValues, final FunctionInputs inputs,
      final ComputationTarget target, final InstrumentDerivative irFutureOption, final SABRInterestRateDataBundle data) {
    final double pv = irFutureOption.accept(CALCULATOR, data);
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final ValueProperties properties = desiredValue.getConstraints().copy()
        .with(ValuePropertyNames.FUNCTION, getUniqueId())
        .get();
    final ValueSpecification spec = new ValueSpecification(getValueRequirementNames()[0], target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, pv));
  }

}
