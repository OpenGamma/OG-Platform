/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome.deprecated;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
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
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.BondFutureSecurityConverter;
import com.opengamma.financial.analytics.conversion.BondSecurityConverter;
import com.opengamma.financial.analytics.conversion.CashSecurityConverter;
import com.opengamma.financial.analytics.conversion.FRASecurityConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateFutureSecurityConverter;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverter;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentFunction;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see InterestRateInstrumentFunction
 */
@Deprecated
public abstract class InterestRateInstrumentFunctionDeprecated extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateInstrumentFunctionDeprecated.class);
  private FixedIncomeConverterDataProvider _definitionConverter;
  private final String _valueRequirementName;
  private FinancialSecurityVisitor<InstrumentDefinition<?>> _visitor;

  public InterestRateInstrumentFunctionDeprecated(final String valueRequirementName) {
    Validate.notNull(valueRequirementName, "value requirement name");
    _valueRequirementName = valueRequirementName;
  }

  protected String getValueRequirementName() {
    return _valueRequirementName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    if (holidaySource == null) {
      throw new UnsupportedOperationException("A holiday source is required");
    }
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    if (regionSource == null) {
      throw new UnsupportedOperationException("A region source is required");
    }
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    if (conventionSource == null) {
      throw new UnsupportedOperationException("A convention bundle source is required");
    }
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    if (securitySource == null) {
      throw new UnsupportedOperationException("A security source is required");
    }
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    if (timeSeriesResolver == null) {
      throw new UnsupportedOperationException("A time series resolver is required");
    }
    final CashSecurityConverter cashConverter = new CashSecurityConverter(holidaySource, regionSource);
    final FRASecurityConverter fraConverter = new FRASecurityConverter(holidaySource, regionSource, conventionSource);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource, regionSource, false);
    final BondSecurityConverter bondConverter = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    final InterestRateFutureSecurityConverter irFutureConverter = new InterestRateFutureSecurityConverter(holidaySource, conventionSource, regionSource);
    final BondFutureSecurityConverter bondFutureConverter = new BondFutureSecurityConverter(securitySource, bondConverter);
    _visitor = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder().cashSecurityVisitor(cashConverter).fraSecurityVisitor(fraConverter)
        .swapSecurityVisitor(swapConverter).interestRateFutureSecurityVisitor(irFutureConverter).bondSecurityVisitor(bondConverter)
        .bondFutureSecurityVisitor(bondFutureConverter).create();
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource, timeSeriesResolver);
  }

  protected FinancialSecurityVisitor<InstrumentDefinition<?>> getVisitor() {
    return _visitor;
  }

  protected FixedIncomeConverterDataProvider getConverter() {
    return _definitionConverter;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return InterestRateInstrumentType.FIXED_INCOME_INSTRUMENT_TARGET_TYPE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
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
      } catch (final OpenGammaRuntimeException ogre) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    final ValueProperties.Builder properties = getResultProperties(currency);
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get()));
  }

  protected static ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName, final String advisoryForward,
      final String advisoryFunding, final String curveCalculationMethod) {
    return YieldCurveFunction.getCurveRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), curveName, advisoryForward, advisoryFunding,
        curveCalculationMethod);
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
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Set<ValueRequirement> timeSeriesRequirements = _definitionConverter.getConversionTimeSeriesRequirements(security, security.accept(_visitor));
    if (timeSeriesRequirements == null) {
      return null;
    }
    requirements.addAll(timeSeriesRequirements);
    if (forwardCurveName.equals(fundingCurveName)) {
      requirements.add(getCurveRequirement(target, forwardCurveName, null, null, curveCalculationMethod));
      return requirements;
    }
    requirements.add(getCurveRequirement(target, forwardCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod));
    requirements.add(getCurveRequirement(target, fundingCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod));
    return requirements;
  }

  public abstract Set<ComputedValue> getComputedValues(InstrumentDerivative derivative, YieldCurveBundle bundle, FinancialSecurity security, ComputationTarget target,
      String forwardCurveName, String fundingCurveName, String curveCalculationMethod, String currency);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String forwardCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final String curveCalculationMethod = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    final YieldCurveBundle bundle = getYieldCurves(target, inputs, forwardCurveName, fundingCurveName, curveCalculationMethod);
    final InstrumentDefinition<?> definition = security.accept(_visitor);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
    }
    try {
      final InstrumentDerivative derivative = _definitionConverter.convert(security, definition, now,
          FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security, fundingCurveName, forwardCurveName), timeSeries);
      final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
      return getComputedValues(derivative, bundle, security, target, forwardCurveName, fundingCurveName, curveCalculationMethod, currency);
    } catch (final Exception ogre) {
      s_logger.error("Error thrown by analytics on security {} with funding curve name {}, forward curve name {}.  Rethrowing.",
          new Object[] {security.toString(), fundingCurveName, forwardCurveName});
      throw new OpenGammaRuntimeException("Error thrown by analytics", ogre);
    }
  }

  protected ValueProperties.Builder getResultProperties(final String currency) {
    final ValueProperties.Builder properties = createValueProperties().withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE).withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD).with(ValuePropertyNames.CURRENCY, currency);
    return properties;
  }

  protected ValueProperties.Builder getResultProperties(final String currency, final String forwardCurveName, final String fundingCurveName,
      final String curveCalculationMethod) {
    final ValueProperties.Builder properties = createValueProperties().with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName).with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
        .with(ValuePropertyNames.CURRENCY, currency);
    return properties;
  }

  protected ValueSpecification getResultSpec(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName,
      final String curveCalculationMethod, final String currency) {
    return new ValueSpecification(getValueRequirementName(), target.toSpecification(), getResultProperties(currency, forwardCurveName, fundingCurveName,
        curveCalculationMethod).get());
  }

  protected static YieldCurveBundle getYieldCurves(final ComputationTarget target, final FunctionInputs inputs, final String forwardCurveName,
      final String fundingCurveName, final String curveCalculationMethod) {
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
    return new YieldCurveBundle(new String[] {fundingCurveName, forwardCurveName}, new YieldAndDiscountCurve[] {fundingCurve, forwardCurve});
  }

}
