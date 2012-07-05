/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.horizon;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.interestrate.ConstantSpreadHorizonThetaCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
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
import com.opengamma.financial.analytics.conversion.FixingTimeSeriesVisitor;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverter;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class SwapConstantSpreadThetaFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(SwapConstantSpreadThetaFunction.class);
  private static final int DAYS_TO_MOVE_FORWARD = 1; // TODO Add to Value Properties
  private FinancialSecurityVisitor<InstrumentDefinition<?>> _visitor;

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource, regionSource, false);
    _visitor = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder().swapSecurityVisitor(swapConverter).create();
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final SwapSecurity security = (SwapSecurity) target.getSecurity();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final YieldCurveBundle bundle = YieldCurveFunctionUtils.getAllYieldCurves(inputs, curveCalculationConfig, curveCalculationConfigSource);
    final InstrumentDefinition<?> definition = security.accept(_visitor);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
    }
    @SuppressWarnings("unchecked")
    final DoubleTimeSeries<ZonedDateTime>[] fixingSeries = new DoubleTimeSeries[] {FixingTimeSeriesVisitor.convertTimeSeries(
        (HistoricalTimeSeries) inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES), now) };
    final String[] curveNamesForSecurity = FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security, curveNames[0], curveNames[1]);
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    final ConstantSpreadHorizonThetaCalculator calculator = ConstantSpreadHorizonThetaCalculator.getInstance();
    if (definition instanceof SwapFixedIborDefinition) {
      final MultipleCurrencyAmount theta = calculator.getTheta((SwapFixedIborDefinition) definition, now, curveNamesForSecurity, bundle, fixingSeries, DAYS_TO_MOVE_FORWARD);
      return Collections.singleton(new ComputedValue(getResultSpec(target, curveCalculationConfigName, currency), theta));
    } else if (definition instanceof SwapFixedONDefinition) {
      final MultipleCurrencyAmount theta = calculator.getTheta((SwapFixedONDefinition) definition, now, curveNamesForSecurity, bundle, fixingSeries, DAYS_TO_MOVE_FORWARD);
      return Collections.singleton(new ComputedValue(getResultSpec(target, curveCalculationConfigName, currency), theta));
    } else if (definition instanceof SwapFixedIborSpreadDefinition) {
      final MultipleCurrencyAmount theta = calculator.getTheta((SwapFixedIborSpreadDefinition) definition, now, curveNamesForSecurity, bundle, fixingSeries, DAYS_TO_MOVE_FORWARD);
      return Collections.singleton(new ComputedValue(getResultSpec(target, curveCalculationConfigName, currency), theta));
    }
    throw new OpenGammaRuntimeException("Can only handle fixed / float ibor and ois swaps; have " + definition.getClass());
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getSecurity() instanceof SwapSecurity)) {
      return false;
    }
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
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveCalculationConfigNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigNames == null || curveCalculationConfigNames.size() != 1) {
      return null;
    }
    final String curveCalculationConfigName = curveCalculationConfigNames.iterator().next();
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + curveCalculationConfigName);
      return null;
    }
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    if (!currency.equals(curveCalculationConfig.getUniqueId())) {
      s_logger.error("Security currency and curve calculation config id were not equal; have {} and {}", currency, curveCalculationConfig.getUniqueId());
    }
    final Set<ValueRequirement> requirements = YieldCurveFunctionUtils.getCurveRequirements(curveCalculationConfig, curveCalculationConfigSource);
    final HistoricalTimeSeriesResolver resolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final ConventionBundleSource conventions = OpenGammaCompilationContext.getConventionBundleSource(context);
    requirements.add(((FinancialSecurity) target.getSecurity()).accept(new FixingTimeSeriesVisitor(conventions, resolver, DateConstraint.VALUATION_TIME)));
    return requirements;
  }

  private ValueProperties.Builder getResultProperties(final String currency) {
    final ValueProperties.Builder properties = createValueProperties()
        .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(InterestRateFutureConstantSpreadThetaFunction.PROPERTY_THETA_CALCULATION_METHOD, InterestRateFutureConstantSpreadThetaFunction.THETA_CONSTANT_SPREAD);
    return properties;
  }

  private ValueProperties.Builder getResultProperties(final String currency, final String curveCalculationConfig) {
    final ValueProperties.Builder properties = createValueProperties()
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(InterestRateFutureConstantSpreadThetaFunction.PROPERTY_THETA_CALCULATION_METHOD, InterestRateFutureConstantSpreadThetaFunction.THETA_CONSTANT_SPREAD);
    return properties;
  }

  private ValueSpecification getResultSpec(final ComputationTarget target, final String curveCalculationConfig, final String currency) {
    return new ValueSpecification(ValueRequirementNames.VALUE_THETA, target.toSpecification(), getResultProperties(currency, curveCalculationConfig).get());
  }
}
