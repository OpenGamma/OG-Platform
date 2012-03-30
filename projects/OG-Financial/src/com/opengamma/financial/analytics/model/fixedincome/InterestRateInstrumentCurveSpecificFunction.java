/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.BondFutureSecurityConverter;
import com.opengamma.financial.analytics.conversion.BondSecurityConverter;
import com.opengamma.financial.analytics.conversion.CashSecurityConverter;
import com.opengamma.financial.analytics.conversion.FRASecurityConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.FutureSecurityConverter;
import com.opengamma.financial.analytics.conversion.InterestRateFutureSecurityConverter;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverter;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public abstract class InterestRateInstrumentCurveSpecificFunction extends AbstractFunction.NonCompiledInvoker {
  private final String _valueRequirement;
  private FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> _visitor;
  private FixedIncomeConverterDataProvider _definitionConverter;

  public InterestRateInstrumentCurveSpecificFunction(final String valueRequirement) {
    ArgumentChecker.notNull(valueRequirement, "value requirement");
    _valueRequirement = valueRequirement;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final CashSecurityConverter cashConverter = new CashSecurityConverter();
    final FRASecurityConverter fraConverter = new FRASecurityConverter(holidaySource, regionSource, conventionSource);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource, regionSource, false);
    final BondSecurityConverter bondConverter = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    final InterestRateFutureSecurityConverter irFutureConverter = new InterestRateFutureSecurityConverter(holidaySource, conventionSource, regionSource);
    final BondFutureSecurityConverter bondFutureConverter = new BondFutureSecurityConverter(securitySource, bondConverter);
    final FutureSecurityConverter futureConverter = new FutureSecurityConverter(bondFutureConverter, irFutureConverter);
    _visitor = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder().cashSecurityVisitor(cashConverter).fraSecurityVisitor(fraConverter).swapSecurityVisitor(swapConverter)
        .futureSecurityVisitor(futureConverter).bondSecurityVisitor(bondConverter).create();
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final ValueProperties constraints = desiredValues.iterator().next().getConstraints();
    final String forwardCurveName = constraints.getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE).iterator().next();
    final String fundingCurveName = constraints.getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE).iterator().next();
    final String curveName = constraints.getValues(ValuePropertyNames.CURVE).iterator().next();
    final String calculationMethod = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD).iterator().next();
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement curveSpecRequirement = getCurveSpecRequirement(target, curveName);
    final Object curveSpecObject = inputs.getValue(curveSpecRequirement);
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveSpecRequirement);
    }
    final InstrumentDefinition<?> definition = security.accept(_visitor);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
    }
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    final InstrumentDerivative derivative = _definitionConverter.convert(security, definition, now, new String[] {fundingCurveName, forwardCurveName}, dataSource);
    final YieldCurveBundle bundle = getYieldCurves(target, inputs, forwardCurveName, fundingCurveName, calculationMethod);
    final ValueProperties properties = createValueProperties(target, curveName, forwardCurveName, fundingCurveName, calculationMethod);
    final ValueSpecification resultSpec = new ValueSpecification(_valueRequirement, target.toSpecification(), properties);
    return getResults(derivative, curveName, curveSpec, bundle, resultSpec);
  }

  public abstract Set<ComputedValue> getResults(final InstrumentDerivative derivative, final String curveName, final InterpolatedYieldCurveSpecificationWithSecurities curveSpec,
      final YieldCurveBundle curves, final ValueSpecification resultSpec);

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getSecurity() instanceof FinancialSecurity)) {
      return false;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    //TODO remove this when we've checked that removing IR futures from the fixed income instrument types
    // doesn't break curves
    if (target.getSecurity() instanceof InterestRateFutureSecurity) {
      return false;
    }
    if (security instanceof SwapSecurity) {
      try {
        final InterestRateInstrumentType type = InterestRateInstrumentType.getInstrumentTypeFromSecurity(security);
        return type == InterestRateInstrumentType.SWAP_FIXED_IBOR || type == InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD
            || type == InterestRateInstrumentType.SWAP_IBOR_IBOR || type == InterestRateInstrumentType.SWAP_FIXED_OIS;
      } catch (OpenGammaRuntimeException ogre) {
        return false;
      }
    }
    return InterestRateInstrumentType.isFixedIncomeInstrumentType(security);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties(target);
    return Collections.singleton(new ValueSpecification(_valueRequirement, target.toSpecification(), properties));
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
    final Set<String> calculationMethodNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    if (calculationMethodNames == null || calculationMethodNames.size() != 1) {
      return null;
    }
    final Set<String> curves = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
    final String forwardCurveName = forwardCurves.iterator().next();
    final String fundingCurveName = fundingCurves.iterator().next();
    final String curveName;
    if ((curves == null) || curves.isEmpty()) {
      // Curve not constrained, so make arbitrary choice.
      curveName = forwardCurveName;
    } else {
      if (curves.contains(forwardCurveName)) {
        curveName = forwardCurveName;
      } else if (curves.contains(fundingCurveName)) {
        curveName = fundingCurveName;
      } else {
        // Instrument isn't sensitive to the requested curve.
        return null;
      }
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

  private static YieldCurveBundle getYieldCurves(final ComputationTarget target, final FunctionInputs inputs, final String forwardCurveName, final String fundingCurveName,
      final String calculationMethod) {
    final ValueRequirement forwardCurveRequirement = getCurveRequirement(target, forwardCurveName, null, null, calculationMethod);
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + forwardCurveRequirement);
    }
    Object fundingCurveObject = null;
    if (!forwardCurveName.equals(fundingCurveName)) {
      final ValueRequirement fundingCurveRequirement = getCurveRequirement(target, fundingCurveName, null, null, calculationMethod);
      fundingCurveObject = inputs.getValue(fundingCurveRequirement);
      if (fundingCurveObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + fundingCurveRequirement);
      }
    }
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = fundingCurveObject == null ? forwardCurve : (YieldAndDiscountCurve) fundingCurveObject;
    return new YieldCurveBundle(new String[] {fundingCurveName, forwardCurveName}, new YieldAndDiscountCurve[] {fundingCurve, forwardCurve});
  }

  private static ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName, final String advisoryForward, final String advisoryFunding,
      final String calculationMethod) {
    return YieldCurveFunction.getCurveRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), curveName, advisoryForward, advisoryFunding,
        calculationMethod);
  }

  private static ValueRequirement getCurveSpecRequirement(final ComputationTarget target, final String curveName) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties.Builder properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get());
  }

  private ValueProperties createValueProperties(final ComputationTarget target) {
    final Security security = target.getSecurity();
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    final ValueProperties.Builder properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
        .withAny(ValuePropertyNames.CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    return properties.get();
  }

  private ValueProperties createValueProperties(final ComputationTarget target, final String curveName, final String forwardCurveName,
      final String fundingCurveName, final String curveCalculationMethod) {
    final Security security = target.getSecurity();
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    final ValueProperties.Builder properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
        .with(ValuePropertyNames.CURVE, curveName)
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName);
    return properties.get();
  }
}
