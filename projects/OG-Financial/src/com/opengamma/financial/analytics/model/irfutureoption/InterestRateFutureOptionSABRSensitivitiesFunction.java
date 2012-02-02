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
  private final String _valueRequirementName;

  public InterestRateFutureOptionSABRSensitivitiesFunction(final String valueRequirementName) {
    _valueRequirementName = valueRequirementName;
  }

  @Override
  protected Set<ComputedValue> getResults(final InstrumentDerivative irFutureOption, final SABRInterestRateDataBundle data, final ComputationTarget target,
      final FunctionInputs inputs, final String forwardCurveName, final String fundingCurveName, final String surfaceName) {
    final PresentValueSABRSensitivityDataBundle sensitivities = CALCULATOR.visit(irFutureOption, data);
    final Map<DoublesPair, Double> alphaSensitivities = getSensitivity(sensitivities); //sensitivities.getAlpha().getMap();
    //    final Map<DoublesPair, Double> nuSensitivities = sensitivities.getNu().getMap();
    //    final Map<DoublesPair, Double> rhoSensitivities = sensitivities.getRho().getMap();
    if (alphaSensitivities.size() != 1) {
      throw new OpenGammaRuntimeException("Can only handle sensitivities at one (t, T) point for now");
    }
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    final ValueSpecification alphaSpec = getResultSpec(target, _valueRequirementName, forwardCurveName, fundingCurveName, surfaceName);
    //    final ValueSpecification nuSpec = getResultSpec(target, ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY, forwardCurveName, fundingCurveName, surfaceName);
    //    final ValueSpecification rhoSpec = getResultSpec(target, ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY, forwardCurveName, fundingCurveName, surfaceName);
    results.add(new ComputedValue(alphaSpec, getMatrix(alphaSensitivities)));
    //    results.add(new ComputedValue(nuSpec, getMatrix(nuSensitivities)));
    //    results.add(new ComputedValue(rhoSpec, getMatrix(rhoSensitivities)));
    return results;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(getResultSpec(target, _valueRequirementName));
    //        getResultSpec(target, ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY),
    //        getResultSpec(target, ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> forwardCurves = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    if (forwardCurves == null || forwardCurves.size() != 1) {
      return null;
    }
    final Set<String> fundingCurves = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    if (fundingCurves == null || fundingCurves.size() != 1) {
      return null;
    }
    final Set<String> surfaceNames = desiredValue.getConstraints().getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final String forwardCurveName = forwardCurves.iterator().next();
    final String fundingCurveName = fundingCurves.iterator().next();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(getSurfaceRequirement(target, surfaceNames.iterator().next()));
    if (forwardCurveName.equals(fundingCurveName)) {
      requirements.add(getCurveRequirement(target, forwardCurveName, null, null));
      return requirements;
    }
    requirements.add(getCurveRequirement(target, forwardCurveName, forwardCurveName, fundingCurveName));
    requirements.add(getCurveRequirement(target, fundingCurveName, forwardCurveName, fundingCurveName));
    return requirements;
  }
  private DoubleLabelledMatrix2D getMatrix(final Map<DoublesPair, Double> map) {
    final Map.Entry<DoublesPair, Double> entry = map.entrySet().iterator().next();
    return new DoubleLabelledMatrix2D(new Double[] {entry.getKey().first}, new Double[] {entry.getKey().second}, new double[][] {new double[] {entry.getValue()}});
  }

  private ValueSpecification getResultSpec(final ComputationTarget target, final String valueRequirementName) {
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode())
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .withAny(ValuePropertyNames.SURFACE).get();
    return new ValueSpecification(valueRequirementName, target.toSpecification(), properties);
  }

  private ValueSpecification getResultSpec(final ComputationTarget target, final String valueRequirementName, final String forwardCurveName,
      final String fundingCurveName, final String surfaceName) {
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode())
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(ValuePropertyNames.SURFACE, surfaceName).get();
    return new ValueSpecification(valueRequirementName, target.toSpecification(), properties);
  }

  private Map<DoublesPair, Double> getSensitivity(final PresentValueSABRSensitivityDataBundle sensitivities) {
    if (_valueRequirementName.equals(ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY)) {
      return sensitivities.getAlpha().getMap();
    }
    if (_valueRequirementName.equals(ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY)) {
      return sensitivities.getNu().getMap();
    }
    if (_valueRequirementName.equals(ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY)) {
      return sensitivities.getRho().getMap();
    }
    throw new OpenGammaRuntimeException("Should never happen");
  }
}
