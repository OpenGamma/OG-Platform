/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivitySABRCalculator;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Calculates the sensitivity of the present value to the SABR parameters
 */
public class IRFutureOptionSABRSensitivitiesFunction extends IRFutureOptionSABRFunction {
  /** The calculator */
  private static final PresentValueSABRSensitivitySABRCalculator CALCULATOR = PresentValueSABRSensitivitySABRCalculator.getInstance();

  public IRFutureOptionSABRSensitivitiesFunction() {
    super(ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY, ValueRequirementNames.PRESENT_VALUE_SABR_BETA_SENSITIVITY,
        ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY, ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY);
  }

  @Override
  protected Set<ComputedValue> getResult(final FunctionExecutionContext context, final Set<ValueRequirement> desiredValues, final FunctionInputs inputs,
      final ComputationTarget target, final InstrumentDerivative irFutureOption, final SABRInterestRateDataBundle data) {
    final PresentValueSABRSensitivityDataBundle sensitivities = irFutureOption.accept(CALCULATOR, data);
    final SurfaceValue alphaSurface = sensitivities.getAlpha();
    final SurfaceValue betaSurface = sensitivities.getBeta();
    final SurfaceValue rhoSurface = sensitivities.getRho();
    final SurfaceValue nuSurface = sensitivities.getNu();
    final ValueProperties properties = desiredValues.iterator().next().getConstraints().copy()
        .withoutAny(ValuePropertyNames.FUNCTION)
        .with(ValuePropertyNames.FUNCTION, getUniqueId())
        .get();
    final Set<ComputedValue> results = Sets.newHashSetWithExpectedSize(4);
    final String[] names = getValueRequirementNames();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    results.add(new ComputedValue(new ValueSpecification(names[0], targetSpec, properties), getMatrix(alphaSurface)));
    results.add(new ComputedValue(new ValueSpecification(names[1], targetSpec, properties), getMatrix(betaSurface)));
    results.add(new ComputedValue(new ValueSpecification(names[2], targetSpec, properties), getMatrix(rhoSurface)));
    results.add(new ComputedValue(new ValueSpecification(names[3], targetSpec, properties), getMatrix(nuSurface)));
    return results;
  }

  private DoubleLabelledMatrix2D getMatrix(final SurfaceValue values) {
    final Map.Entry<DoublesPair, Double> entry = Iterables.getOnlyElement(values.getMap().entrySet());
    return new DoubleLabelledMatrix2D(new Double[] {entry.getKey().first}, new Double[] {entry.getKey().second}, new double[][] {new double[] {entry.getValue()}});
  }
}
