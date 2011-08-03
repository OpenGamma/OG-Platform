/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivitySABRCalculator;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class InterestRateFutureOptionSABRSensitivitiesFunction extends InterestRateFutureOptionFunction {
  private static final PresentValueSABRSensitivitySABRCalculator CALCULATOR = PresentValueSABRSensitivitySABRCalculator.getInstance();

  public InterestRateFutureOptionSABRSensitivitiesFunction(final String surfaceName) {
    super(surfaceName, ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY, ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY, ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY);
  }

  @Override
  protected Set<ComputedValue> getResults(final InterestRateDerivative irFutureOption, final SABRInterestRateDataBundle data, final ValueSpecification[] specifications,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ComputationTarget target) {
    if (specifications.length != 3) {
      throw new OpenGammaRuntimeException("Specifications array length did not match the number requested");
    }
    final PresentValueSABRSensitivityDataBundle sensitivities = CALCULATOR.visit(irFutureOption, data);
    final Map<DoublesPair, Double> alphaSensitivities = sensitivities.getAlpha();
    final Map<DoublesPair, Double> nuSensitivities = sensitivities.getNu();
    final Map<DoublesPair, Double> rhoSensitivities = sensitivities.getRho();
    if (alphaSensitivities.size() != 1) {
      throw new OpenGammaRuntimeException("Can only handle sensitivities at one (t, T) point for now");
    }
    final Set<ComputedValue> results = new HashSet<ComputedValue>();

    results.add(new ComputedValue(specifications[0], getMatrix(alphaSensitivities)));
    results.add(new ComputedValue(specifications[1], getMatrix(nuSensitivities)));
    results.add(new ComputedValue(specifications[2], getMatrix(rhoSensitivities)));
    return results;
  }

  private DoubleLabelledMatrix2D getMatrix(final Map<DoublesPair, Double> map) {
    final Map.Entry<DoublesPair, Double> entry = map.entrySet().iterator().next();
    return new DoubleLabelledMatrix2D(new Double[] {entry.getKey().first},
                                      new Double[] {entry.getKey().second},
                                      new double[][] {new double[] {entry.getValue()}});
  }
}
