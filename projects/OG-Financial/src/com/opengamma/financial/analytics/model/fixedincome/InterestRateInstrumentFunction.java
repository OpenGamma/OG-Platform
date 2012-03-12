/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
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
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.InterpolatedYieldCurveFunction;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public abstract class InterestRateInstrumentFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * 
   */
  protected static final String RESULT_PROPERTY_TYPE = "Type";

  /**
   * 
   */
  protected static final String TYPE_FORWARD = "Forward";

  /**
   * 
   */
  protected static final String TYPE_FUNDING = "Funding";

  private FixedIncomeConverterDataProvider _definitionConverter;
  private final String _valueRequirementName;
  private FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> _visitor;

  public InterestRateInstrumentFunction(final String valueRequirementName) {
    Validate.notNull(valueRequirementName, "value requirement name");
    _valueRequirementName = valueRequirementName;
  }

  protected String getValueRequirementName() {
    return _valueRequirementName;
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
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getSecurity() instanceof FinancialSecurity)) {
      return false;
    }
    //TODO remove this when we've checked that removing IR futures from the fixed income instrument types
    // doesn't break curves
    if (target.getSecurity() instanceof InterestRateFutureSecurity) {
      return false;
    }
    return InterestRateInstrumentType.isFixedIncomeInstrumentType((FinancialSecurity) target.getSecurity());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    final ValueProperties.Builder properties = createValueProperties()
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency);
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get()));
  }

  protected Set<ValueSpecification> getResults(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName, final String curveCalculationMethod,
      final String currency) {
    final ValueProperties.Builder properties = createValueProperties()
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency);
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get()));
  }

  // REVIEW 2011-11-25 andrew -- The property for the curve name should really be NAME when applied to the curve object;
  // it should only become CURVE when applied to something using it (like this).
  protected ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName, final String advisoryForwardCurveName, final String advisoryFundingCurveName,
      final String curveCalculationMethod) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties.Builder properties = ValueProperties.with(ValuePropertyNames.CURVE, curveName);
    if (advisoryForwardCurveName != null) {
      properties.with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, advisoryForwardCurveName);
    }
    if (advisoryFundingCurveName != null) {
      properties.with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, advisoryFundingCurveName);
    }
    properties.with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get());
  }

  protected ValueRequirement getInterpolatedCurveRequirement(final ComputationTarget target, final String curveName, final boolean isForward,
      final String curveCalculationMethod) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties.Builder properties = ValueProperties.with(ValuePropertyNames.CURVE, curveName);
    if (isForward) {
      properties.withOptional(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    } else {
      properties.withOptional(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    }
    properties.with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get());
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> forwardCurves = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final Set<String> fundingCurves = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final Set<String> curveCalculationMethodNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    if ((forwardCurves == null) || (fundingCurves == null) || (forwardCurves.size() != 1) || (fundingCurves.size() != 1)) {
      // Can't support an unbound request; an injection function must be used (or declare all as optional and use [PLAT-1771])
      return null;
    }
    if (curveCalculationMethodNames == null || curveCalculationMethodNames.size() != 1) {
      return null;
    }
    final String forwardCurve = forwardCurves.iterator().next();
    final String fundingCurve = fundingCurves.iterator().next();
    final String curveCalculationMethod = curveCalculationMethodNames.iterator().next();
    if (curveCalculationMethod.equals(InterpolatedYieldCurveFunction.CALCULATION_METHOD_NAME)) {
      return Sets.newHashSet(getInterpolatedCurveRequirement(target, forwardCurve, true, curveCalculationMethod),
          getInterpolatedCurveRequirement(target, fundingCurve, false, curveCalculationMethod));
    }
    if (forwardCurve.equals(fundingCurve)) {
      return Collections.singleton(getCurveRequirement(target, forwardCurve, null, null, curveCalculationMethod));
    }
    return Sets.newHashSet(getCurveRequirement(target, forwardCurve, forwardCurve, fundingCurve, curveCalculationMethod),
        getCurveRequirement(target, fundingCurve, forwardCurve, fundingCurve, curveCalculationMethod));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String forwardCurveName = null;
    String fundingCurveName = null;
    String curveCalculationMethod = null;
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    if (inputs.size() == 1) {
      final ValueSpecification spec = inputs.keySet().iterator().next();
      forwardCurveName = spec.getProperty(ValuePropertyNames.CURVE);
      fundingCurveName = forwardCurveName;
      curveCalculationMethod = spec.getProperty(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    }
    assert inputs.size() == 2;
    // Only need to check one; the advisory forward/funding will be correct on either
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification key = entry.getKey();
      final ValueRequirement value = entry.getValue();
      if (!key.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
        throw new OpenGammaRuntimeException("Expecting only yield curves as inputs");
      }
      final Set<String> forwardCurveProperties = value.getConstraints().getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
      if (forwardCurveProperties != null) {
        if (forwardCurveProperties.size() == 1) {
          forwardCurveName = forwardCurveProperties.iterator().next();
        } else if (forwardCurveProperties.isEmpty()) {
          forwardCurveName = value.getConstraint(ValuePropertyNames.CURVE);
        }
        if (curveCalculationMethod == null) {
          curveCalculationMethod = key.getProperty(ValuePropertyNames.CURVE_CALCULATION_METHOD);
        }
      }
      final Set<String> fundingCurveProperties = value.getConstraints().getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
      if (fundingCurveProperties != null) {
        if (fundingCurveProperties.size() == 1) {
          fundingCurveName = fundingCurveProperties.iterator().next();
        } else if (fundingCurveProperties.isEmpty()) {
          fundingCurveName = value.getConstraint(ValuePropertyNames.CURVE);
        }
        if (curveCalculationMethod == null) {
          curveCalculationMethod = key.getProperty(ValuePropertyNames.CURVE_CALCULATION_METHOD);
        }
      }
    }
    assert forwardCurveName != null;
    assert fundingCurveName != null;
    assert curveCalculationMethod != null;
    return getResults(target, forwardCurveName, fundingCurveName, curveCalculationMethod, currency);
  }

  public abstract Set<ComputedValue> getComputedValues(InstrumentDerivative derivative, YieldCurveBundle bundle, FinancialSecurity security, ComputationTarget target, String forwardCurveName,
      String fundingCurveName, String curveCalculationMethod, String currency);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String forwardCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final String curveCalculationMethod = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_METHOD);
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
    final YieldCurveBundle bundle = new YieldCurveBundle(new String[] {fundingCurveName, forwardCurveName }, new YieldAndDiscountCurve[] {fundingCurve, forwardCurve });
    final InstrumentDefinition<?> definition = security.accept(_visitor);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
    }
    final InstrumentDerivative derivative = _definitionConverter.convert(security, definition, now,
        FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security, fundingCurveName, forwardCurveName), dataSource);
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    return getComputedValues(derivative, bundle, security, target, forwardCurveName, fundingCurveName, curveCalculationMethod, currency);
  }

  protected ValueSpecification getResultSpec(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName, final String curveCalculationMethod,
      final String currency) {
    return new ValueSpecification(getValueRequirementName(), target.toSpecification(), createValueProperties()
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency).get());
  }

}
