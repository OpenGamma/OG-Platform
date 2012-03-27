/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.PV01Calculator;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class InterestRateFutureOptionPV01Function extends InterestRateFutureOptionFunction {

  private static final String VALUE_REQUIREMENT = ValueRequirementNames.PV01;
  private static final PV01Calculator CALCULATOR = PV01Calculator.getInstance();

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Sets.newHashSet(getSpecification(target));
  }

  private ValueSpecification getSpecification(final ComputationTarget target) {
    return new ValueSpecification(ValueRequirementNames.PV01, target.toSpecification(),
        createValueProperties()
            .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode())
            .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
            .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
            .withAny(ValuePropertyNames.SURFACE)
            .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
            .with(ValuePropertyNames.SMILE_FITTING_METHOD, SURFACE_FITTING_NAME)
            .get());
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    if (curveName == null) {
      throw new OpenGammaRuntimeException("Must specify a curve against which to calculate the desired value " + VALUE_REQUIREMENT);
    }
    final Set<String> forwardCurves = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    if (forwardCurves == null || forwardCurves.size() != 1) {
      return null;
    }
    final Set<String> fundingCurves = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    if (fundingCurves == null || fundingCurves.size() != 1) {
      return null;
    }
    final Set<String> calculationMethodNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    if (calculationMethodNames == null || calculationMethodNames.size() != 1) {
      return null;
    }
    final String forwardCurveName = forwardCurves.iterator().next();
    final String fundingCurveName = fundingCurves.iterator().next();
    if (!curveName.equals(forwardCurveName) && !curveName.equals(fundingCurveName)) {
      throw new OpenGammaRuntimeException("Asked for sensitivities to a curve (" + curveName + ") to which this interest rate future option is not sensitive " +
          "(allowed " + forwardCurveName + " and " + fundingCurveName + ")");
    }
    final String calculationMethod = calculationMethodNames.iterator().next();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    if (forwardCurveName.equals(fundingCurveName)) {
      requirements.add(getCurveRequirement(target, curveName, null, null, calculationMethod));
      requirements.add(getCurveSpecRequirement(target, curveName));
      return requirements;
    }
    requirements.add(getCurveRequirement(target, forwardCurveName, forwardCurveName, fundingCurveName, calculationMethod));
    requirements.add(getCurveRequirement(target, fundingCurveName, forwardCurveName, fundingCurveName, calculationMethod));
    requirements.add(getCurveSpecRequirement(target, curveName));
    return requirements;
  }

  private static ValueRequirement getCurveSpecRequirement(final ComputationTarget target, final String curveName) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final ValueProperties.Builder properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get());
  }

  @Override
  protected Set<ComputedValue> getResults(InstrumentDerivative irFutureOption, SABRInterestRateDataBundle data, ComputationTarget target, FunctionInputs inputs, String forwardCurveName,
      String fundingCurveName, String surfaceName, String curveCalculationMethod) {
    throw new OpenGammaRuntimeException("Could not get PV01. Perhaps a Curve Constraint was not supplied?");
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {

    // Build the market data bundle
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String forwardCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String curveCalculationMethod = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    final SABRInterestRateDataBundle data = new SABRInterestRateDataBundle(getModelParameters(target, inputs, surfaceName),
        getYieldCurves(target, inputs, forwardCurveName, fundingCurveName, curveCalculationMethod));

    // Check requirements
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    if (curveName == null) {
      throw new OpenGammaRuntimeException("PV01 was not computed as no Curve Constraint was provided.");
    }
    YieldAndDiscountCurve curvePV01 = data.getCurve(curveName);
    if (curvePV01 == null) {
      throw new OpenGammaRuntimeException("PV01 for Curve requested, " + curveName + " was not computed as this curve was not in the market data bundle.");
    }

    // Build the derivative
    final SimpleTrade trade = (SimpleTrade) target.getTrade();
    final ZonedDateTime now = executionContext.getValuationClock().zonedDateTime();
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    @SuppressWarnings("unchecked")
    final InstrumentDefinition<InstrumentDerivative> irFutureOptionDefinition = (InstrumentDefinition<InstrumentDerivative>) getConverter().convert(trade);
    final InstrumentDerivative irFutureOption = getDerivativeConverter().convert(trade.getSecurity(), irFutureOptionDefinition, now, new String[] {fundingCurveName, forwardCurveName }, dataSource);

    // Compute exposure
    final Map<String, Double> pv01 = CALCULATOR.visit(irFutureOption, data);
    if (!pv01.containsKey(curveName)) {
      throw new OpenGammaRuntimeException("Could not get PV01 for curve named " + curveName + "; should never happen");
    }

    final Currency currency = FinancialSecurityUtils.getCurrency(trade.getSecurity()); // TODO Consider using curvePV01's currency
    final ValueSpecification resultSpec = getResultSpec(target, currency, fundingCurveName, surfaceName, forwardCurveName, fundingCurveName, curveCalculationMethod);
    return Collections.singleton(new ComputedValue(resultSpec, pv01.get(curveName)));
  }

  private ValueSpecification getResultSpec(final ComputationTarget target, final Currency ccy, final String curveName, final String surfaceName,
      final String forwardCurveName, final String fundingCurveName, final String calculationMethod) {
    final ValueProperties result = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, calculationMethod)
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(ValuePropertyNames.SMILE_FITTING_METHOD, InterestRateFutureOptionFunction.SURFACE_FITTING_NAME).get();
    return new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), result);
  }
}
