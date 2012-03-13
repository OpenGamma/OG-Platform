/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.PV01Calculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;

/**
 * 
 */
public class InterestRateInstrumentPV01Function extends InterestRateInstrumentCurveSpecificFunction {
  private static final PV01Calculator CALCULATOR = PV01Calculator.getInstance();

  public InterestRateInstrumentPV01Function() {
    super(ValueRequirementNames.PV01);
  }

  @Override
  public Set<ComputedValue> getResults(final InstrumentDerivative derivative, final String curveName, final InterpolatedYieldCurveSpecificationWithSecurities curveSpec,
      final YieldCurveBundle curves, final ValueSpecification resultSpec) {
    final Map<String, Double> pv01 = CALCULATOR.visit(derivative, curves);
    if (!pv01.containsKey(curveName)) {
      throw new OpenGammaRuntimeException("Could not get PV01 for curve named " + curveName + "; should never happen");
    }
    return Collections.singleton(new ComputedValue(resultSpec, pv01.get(curveName)));
  }
  //  @Override
  //  public Set<ComputedValue> getComputedValues(final InstrumentDerivative derivative, final YieldCurveBundle bundle,
  //      final FinancialSecurity security, final ComputationTarget target, final String forwardCurveName, final String fundingCurveName,
  //      final String curveCalculationMethod, final String currency) {
  //    final Map<String, Double> pv01 = CALCULATOR.visit(derivative, bundle);
  //    final Set<ComputedValue> result = new HashSet<ComputedValue>();
  //    result.add(new ComputedValue(getResultSpec(target, fundingCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod),
  //        pv01.containsKey(fundingCurveName) ? pv01.get(fundingCurveName) : 0));
  //    result.add(new ComputedValue(getResultSpec(target, forwardCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod),
  //        pv01.containsKey(forwardCurveName) ? pv01.get(forwardCurveName) : 0));
  //    return result;
  //  }

  //  @Override
  //  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
  //    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
  //    final ValueProperties.Builder properties = createValueProperties()
  //        .withAny(ValuePropertyNames.CURVE)
  //        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
  //        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
  //        .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
  //        .with(ValuePropertyNames.CURRENCY, currency)
  //        .with(ValuePropertyNames.CURVE_CURRENCY, currency);
  //    final Set<ValueSpecification> results = Sets.newHashSetWithExpectedSize(2);
  //    properties.with(RESULT_PROPERTY_TYPE, TYPE_FORWARD);
  //    final ComputationTargetSpecification targetSpec = target.toSpecification();
  //    results.add(new ValueSpecification(getValueRequirementName(), targetSpec, properties.get()));
  //    properties.withoutAny(RESULT_PROPERTY_TYPE).with(RESULT_PROPERTY_TYPE, TYPE_FUNDING);
  //    results.add(new ValueSpecification(getValueRequirementName(), targetSpec, properties.get()));
  //    return results;
  //  }
  //
  //  @Override
  //  public Set<ValueSpecification> getResults(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName, final String curveCalculationMethod,
  //      final String currency) {
  //    return Sets.newHashSet(getResultSpec(target, forwardCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod, currency),
  //        getResultSpec(target, fundingCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod, currency));
  //  }
  //
  //  private ValueSpecification getResultSpec(final ComputationTarget target, final String curveName, final String forwardCurveName, final String fundingCurveName,
  //      final String curveCalculationMethod, final String currency) {
  //    final ValueProperties.Builder properties = createValueProperties()
  //        .with(ValuePropertyNames.CURVE, curveName)
  //        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
  //        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
  //        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
  //        .with(ValuePropertyNames.CURRENCY, currency)
  //        .with(ValuePropertyNames.CURVE_CURRENCY, currency);
  //    return new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get());
  //  }

}
