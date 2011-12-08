/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivitySABRCalculator;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class InterestRateFutureOptionSABRSensitivitiesFunction extends InterestRateFutureOptionFunction {
  private static final PresentValueSABRSensitivitySABRCalculator CALCULATOR = PresentValueSABRSensitivitySABRCalculator.getInstance();

  public InterestRateFutureOptionSABRSensitivitiesFunction(String forwardCurveName, String fundingCurveName, final String surfaceName) {
    super(forwardCurveName, fundingCurveName, surfaceName);
  }

  @Override
  protected Set<ComputedValue> getResults(final InstrumentDerivative irFutureOption, final SABRInterestRateDataBundle data, final Set<ValueRequirement> desiredValues, final FunctionInputs inputs,
      final ComputationTarget target) {
    final PresentValueSABRSensitivityDataBundle sensitivities = CALCULATOR.visit(irFutureOption, data);
    final Map<DoublesPair, Double> alphaSensitivities = sensitivities.getAlpha().getMap();
    final Map<DoublesPair, Double> nuSensitivities = sensitivities.getNu().getMap();
    final Map<DoublesPair, Double> rhoSensitivities = sensitivities.getRho().getMap();
    if (alphaSensitivities.size() != 1) {
      throw new OpenGammaRuntimeException("Can only handle sensitivities at one (t, T) point for now");
    }
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    results.add(new ComputedValue(getResultSpec(target, ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY), getMatrix(alphaSensitivities)));
    results.add(new ComputedValue(getResultSpec(target, ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY), getMatrix(nuSensitivities)));
    results.add(new ComputedValue(getResultSpec(target, ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY), getMatrix(rhoSensitivities)));
    return results;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(getResultSpec(target, ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY), getResultSpec(target, ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY),
        getResultSpec(target, ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY));
  }

  private DoubleLabelledMatrix2D getMatrix(final Map<DoublesPair, Double> map) {
    final Map.Entry<DoublesPair, Double> entry = map.entrySet().iterator().next();
    return new DoubleLabelledMatrix2D(new Double[] {entry.getKey().first}, new Double[] {entry.getKey().second}, new double[][] {new double[] {entry.getValue()}});
  }

  private ValueSpecification getResultSpec(ComputationTarget target, String valueRequirementName) {
    ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode())
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, getForwardCurveName()).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, getFundingCurveName())
        .with(ValuePropertyNames.SURFACE, getSurfaceName()).get();
    return new ValueSpecification(valueRequirementName, target.toSpecification(), properties);
  }
}
