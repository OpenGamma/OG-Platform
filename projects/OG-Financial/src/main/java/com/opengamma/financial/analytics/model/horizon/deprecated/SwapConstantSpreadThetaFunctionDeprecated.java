/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.horizon.deprecated;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.horizon.ConstantSpreadHorizonThetaCalculator;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.FixingTimeSeriesVisitor;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverterDeprecated;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.horizon.SwapConstantSpreadThetaFunction;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * @deprecated Use the version of the function that does not refer to funding or forward curves
 * @see SwapConstantSpreadThetaFunction
 */
@Deprecated
public class SwapConstantSpreadThetaFunctionDeprecated extends AbstractFunction.NonCompiledInvoker {
  private static final int DAYS_TO_MOVE_FORWARD = 1; // TODO Add to Value Properties
  private FinancialSecurityVisitor<InstrumentDefinition<?>> _visitor;

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final SwapSecurityConverterDeprecated swapConverter = new SwapSecurityConverterDeprecated(holidaySource, conventionSource, regionSource, false);
    _visitor = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder().swapSecurityVisitor(swapConverter).create();
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final SwapSecurity security = (SwapSecurity) target.getSecurity();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String forwardCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final String curveCalculationMethod = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    final YieldCurveBundle bundle = getYieldCurves(target, inputs, forwardCurveName, fundingCurveName, curveCalculationMethod);
    final InstrumentDefinition<?> definition = security.accept(_visitor);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
    }
    final ZonedDateTimeDoubleTimeSeries[] fixingSeries = new ZonedDateTimeDoubleTimeSeries[] {FixingTimeSeriesVisitor.convertTimeSeries(
        (HistoricalTimeSeries) inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES), now) };
    final String[] curveNamesForSecurity = FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security, fundingCurveName, forwardCurveName);
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    final ConstantSpreadHorizonThetaCalculator calculator = ConstantSpreadHorizonThetaCalculator.getInstance();
    if (definition instanceof SwapFixedIborDefinition) {
      final MultipleCurrencyAmount theta = calculator.getTheta((SwapFixedIborDefinition) definition, now, curveNamesForSecurity, bundle, fixingSeries, DAYS_TO_MOVE_FORWARD);
      return Collections.singleton(new ComputedValue(getResultSpec(target, forwardCurveName, fundingCurveName, curveCalculationMethod, currency), theta));
    } else if (definition instanceof SwapFixedONDefinition) {
      final MultipleCurrencyAmount theta = calculator.getTheta((SwapFixedONDefinition) definition, now, curveNamesForSecurity, bundle, fixingSeries, DAYS_TO_MOVE_FORWARD);
      return Collections.singleton(new ComputedValue(getResultSpec(target, forwardCurveName, fundingCurveName, curveCalculationMethod, currency), theta));
    } else if (definition instanceof SwapFixedIborSpreadDefinition) {
      final MultipleCurrencyAmount theta = calculator.getTheta((SwapFixedIborSpreadDefinition) definition, now, curveNamesForSecurity, bundle, fixingSeries, DAYS_TO_MOVE_FORWARD);
      return Collections.singleton(new ComputedValue(getResultSpec(target, forwardCurveName, fundingCurveName, curveCalculationMethod, currency), theta));
    }
    throw new OpenGammaRuntimeException("Can only handle fixed / float ibor and ois swaps; have " + definition.getClass());
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.SWAP_SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final InterestRateInstrumentType type = InterestRateInstrumentType.getInstrumentTypeFromSecurity((FinancialSecurity) target.getSecurity());
    return type == InterestRateInstrumentType.SWAP_FIXED_IBOR || type == InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD || type == InterestRateInstrumentType.SWAP_FIXED_OIS;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    final ValueProperties.Builder properties = getResultProperties(currency);
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.VALUE_THETA, target.toSpecification(), properties.get()));
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
    final Set<String> curveCalculationMethodNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    if (curveCalculationMethodNames == null || curveCalculationMethodNames.size() != 1) {
      return null;
    }
    final String forwardCurveName = forwardCurves.iterator().next();
    final String fundingCurveName = fundingCurves.iterator().next();
    final String curveCalculationMethod = curveCalculationMethodNames.iterator().next();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    if (forwardCurveName.equals(fundingCurveName)) {
      requirements.add(getCurveRequirement(target, forwardCurveName, null, null, curveCalculationMethod));
      return requirements;
    }
    requirements.add(getCurveRequirement(target, forwardCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod));
    requirements.add(getCurveRequirement(target, fundingCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod));
    final HistoricalTimeSeriesResolver resolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final ConventionBundleSource conventions = OpenGammaCompilationContext.getConventionBundleSource(context);
    requirements.add(((FinancialSecurity) target.getSecurity()).accept(new FixingTimeSeriesVisitor(conventions, resolver, DateConstraint.VALUATION_TIME)));
    return requirements;
  }

  private ValueProperties.Builder getResultProperties(final String currency) {
    final ValueProperties.Builder properties = createValueProperties()
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(InterestRateFutureConstantSpreadThetaFunctionDeprecated.PROPERTY_THETA_CALCULATION_METHOD, InterestRateFutureConstantSpreadThetaFunctionDeprecated.THETA_CONSTANT_SPREAD);
    return properties;
  }

  private ValueProperties.Builder getResultProperties(final String currency, final String forwardCurveName, final String fundingCurveName, final String curveCalculationMethod) {
    final ValueProperties.Builder properties = createValueProperties()
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(InterestRateFutureConstantSpreadThetaFunctionDeprecated.PROPERTY_THETA_CALCULATION_METHOD, InterestRateFutureConstantSpreadThetaFunctionDeprecated.THETA_CONSTANT_SPREAD);
    return properties;
  }

  private YieldCurveBundle getYieldCurves(final ComputationTarget target, final FunctionInputs inputs, final String forwardCurveName, final String fundingCurveName,
      final String curveCalculationMethod) {
    final ValueRequirement forwardCurveRequirement = getCurveRequirement(target, forwardCurveName, null, null, curveCalculationMethod);
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + forwardCurveRequirement);
    }
    Object fundingCurveObject = null;
    if (!forwardCurveName.equals(fundingCurveName)) {
      final ValueRequirement fundingCurveRequirement = getCurveRequirement(target, fundingCurveName, null, null, curveCalculationMethod);
      fundingCurveObject = inputs.getValue(fundingCurveRequirement);
      if (fundingCurveObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + fundingCurveRequirement);
      }
    }
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = fundingCurveObject == null ? forwardCurve : (YieldAndDiscountCurve) fundingCurveObject;
    return new YieldCurveBundle(new String[] {fundingCurveName, forwardCurveName }, new YieldAndDiscountCurve[] {fundingCurve, forwardCurve });
  }

  private ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName, final String advisoryForward, final String advisoryFunding,
      final String curveCalculationMethod) {
    return YieldCurveFunction.getCurveRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), curveName, advisoryForward, advisoryFunding,
        curveCalculationMethod);
  }

  private ValueSpecification getResultSpec(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName, final String curveCalculationMethod,
      final String currency) {
    return new ValueSpecification(ValueRequirementNames.VALUE_THETA, target.toSpecification(), getResultProperties(currency, forwardCurveName, fundingCurveName, curveCalculationMethod).get());
  }
}
