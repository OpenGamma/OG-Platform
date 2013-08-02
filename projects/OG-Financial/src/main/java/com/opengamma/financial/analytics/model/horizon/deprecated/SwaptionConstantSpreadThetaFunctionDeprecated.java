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
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.SwaptionSecurityConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.horizon.SwaptionConstantSpreadThetaFunction;
import com.opengamma.financial.analytics.model.swaption.SwaptionUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see SwaptionConstantSpreadThetaFunction
 */
@Deprecated
public class SwaptionConstantSpreadThetaFunctionDeprecated extends AbstractFunction.NonCompiledInvoker {

  private static final int DAYS_TO_MOVE_FORWARD = 1; // TODO Add to Value Properties
  private FinancialSecurityVisitor<InstrumentDefinition<?>> _visitor;

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final SwapSecurityConverterDeprecated swapConverter = new SwapSecurityConverterDeprecated(holidaySource, conventionSource, regionSource, false);
    final SwaptionSecurityConverter swaptionConverter = new SwaptionSecurityConverter(securitySource, swapConverter);
    _visitor = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder().swaptionVisitor(swaptionConverter).create();
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String forwardCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final String curveCalculationMethod = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final SwaptionSecurity security = (SwaptionSecurity) target.getSecurity();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final Object volatilitySurfaceObject = inputs.getValue(getVolatilityRequirement(surfaceName, currency));
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface");
    }
    final VolatilitySurface volatilitySurface = (VolatilitySurface) volatilitySurfaceObject;
    if (!(volatilitySurface.getSurface() instanceof InterpolatedDoublesSurface)) {
      throw new OpenGammaRuntimeException("Expecting an InterpolatedDoublesSurface; got " + volatilitySurface.getSurface().getClass());
    }
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final YieldCurveBundle bundle = getYieldCurves(target, inputs, forwardCurveName, fundingCurveName, curveCalculationMethod);
    final InstrumentDefinition<?> definition = security.accept(_visitor);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
    }
    final BlackFlatSwaptionParameters parameters = new BlackFlatSwaptionParameters(volatilitySurface.getSurface(),
        SwaptionUtils.getSwapGenerator(security, definition, securitySource));
    final YieldCurveWithBlackSwaptionBundle blackData = new YieldCurveWithBlackSwaptionBundle(parameters, bundle);

    final ConstantSpreadHorizonThetaCalculator calculator = ConstantSpreadHorizonThetaCalculator.getInstance();

    if (security.isCashSettled()) {
      final SwaptionCashFixedIborDefinition cashSettled = (SwaptionCashFixedIborDefinition) definition;
      final String[] curveNamesForSecurity = new String[] {fundingCurveName, forwardCurveName };
      final MultipleCurrencyAmount theta = calculator.getTheta(cashSettled, now, curveNamesForSecurity, blackData, DAYS_TO_MOVE_FORWARD);
      return Collections.singleton(new ComputedValue(getResultSpec(target, forwardCurveName, fundingCurveName, curveCalculationMethod, surfaceName, currency.getCode()), theta));
    }
    final SwaptionPhysicalFixedIborDefinition physicallySettled = (SwaptionPhysicalFixedIborDefinition) definition;
    final String[] curveNamesForSecurity = new String[] {fundingCurveName, forwardCurveName };
    final MultipleCurrencyAmount theta = calculator.getTheta(physicallySettled, now, curveNamesForSecurity, blackData, DAYS_TO_MOVE_FORWARD);
    return Collections.singleton(new ComputedValue(getResultSpec(target, forwardCurveName, fundingCurveName, curveCalculationMethod, surfaceName, currency.getCode()), theta));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.SWAPTION_SECURITY;
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
    final Set<String> surfaceNames = desiredValue.getConstraints().getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final String forwardCurveName = forwardCurves.iterator().next();
    final String fundingCurveName = fundingCurves.iterator().next();
    final String curveCalculationMethod = curveCalculationMethodNames.iterator().next();
    final String surfaceName = surfaceNames.iterator().next();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(getVolatilityRequirement(surfaceName, FinancialSecurityUtils.getCurrency(target.getSecurity())));
    if (forwardCurveName.equals(fundingCurveName)) {
      requirements.add(getCurveRequirement(target, forwardCurveName, null, null, curveCalculationMethod));
      return requirements;
    }
    requirements.add(getCurveRequirement(target, forwardCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod));
    requirements.add(getCurveRequirement(target, fundingCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod));
    return requirements;
  }

  private ValueProperties.Builder getResultProperties(final String currency) {
    final ValueProperties.Builder properties = createValueProperties()
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
        .withAny(ValuePropertyNames.SURFACE)
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(InterestRateFutureConstantSpreadThetaFunctionDeprecated.PROPERTY_THETA_CALCULATION_METHOD, InterestRateFutureConstantSpreadThetaFunctionDeprecated.THETA_CONSTANT_SPREAD);
    return properties;
  }

  private ValueProperties.Builder getResultProperties(final String currency, final String forwardCurveName, final String fundingCurveName, final String curveCalculationMethod,
      final String surfaceName) {
    final ValueProperties.Builder properties = createValueProperties()
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
        .with(ValuePropertyNames.SURFACE, surfaceName)
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

  private ValueRequirement getVolatilityRequirement(final String surface, final Currency currency) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surface)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.SWAPTION_ATM).get();
    return new ValueRequirement(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE, ComputationTargetSpecification.of(currency), properties);
  }

  private ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName, final String advisoryForward, final String advisoryFunding,
      final String curveCalculationMethod) {
    return YieldCurveFunction.getCurveRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), curveName, advisoryForward, advisoryFunding,
        curveCalculationMethod);
  }

  private ValueSpecification getResultSpec(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName, final String curveCalculationMethod,
      final String surfaceName, final String currency) {
    return new ValueSpecification(ValueRequirementNames.VALUE_THETA, target.toSpecification(), getResultProperties(currency, forwardCurveName, fundingCurveName, curveCalculationMethod,
        surfaceName).get());
  }
}
