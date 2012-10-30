/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
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
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.BondFutureSecurityConverter;
import com.opengamma.financial.analytics.conversion.BondSecurityConverter;
import com.opengamma.financial.analytics.conversion.CashSecurityConverter;
import com.opengamma.financial.analytics.conversion.FRASecurityConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateFutureSecurityConverter;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverter;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
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
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public abstract class InterestRateInstrumentCurveSpecificFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateInstrumentCurveSpecificFunction.class);
  private static final String PROPERTY_REQUESTED_CURVE = "RequestedCurve";
  private final String _valueRequirement;
  private FinancialSecurityVisitor<InstrumentDefinition<?>> _visitor;
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
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
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

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final InstrumentDefinition<?> definition = security.accept(_visitor);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
    }
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final String curveCalculationMethod = curveCalculationConfig.getCalculationMethod();
    final InstrumentDerivative derivative = InterestRateInstrumentFunction.getDerivative(security, now, timeSeries, curveNames, definition, _definitionConverter);
    final YieldCurveBundle bundle = InterestRateInstrumentFunction.getYieldCurves(inputs, curveCalculationConfig, curveCalculationConfigSource);
    final ValueProperties properties = createValueProperties(target, curveName, curveCalculationConfigName);
    final ValueSpecification resultSpec = new ValueSpecification(_valueRequirement, target.toSpecification(), properties);
    return getResults(derivative, curveName, bundle, curveCalculationConfigName, curveCalculationMethod, inputs, target, resultSpec);
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
    final ValueProperties properties = createValueProperties(target);
    return Collections.singleton(new ValueSpecification(_valueRequirement, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curves = constraints.getValues(ValuePropertyNames.CURVE);
    if (curves == null || curves.size() != 1) {
      s_logger.error("Must specify a curve name");
      return null;
    }
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
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    if (!ComputationTargetSpecification.of(currency).equals(curveCalculationConfig.getTarget())) {
      s_logger.error("Security currency and curve calculation config id were not equal; have {} and {}", currency, curveCalculationConfig.getTarget());
      return null;
    }
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final String curve = curves.iterator().next();
    if (Arrays.binarySearch(curveNames, curve) < 0) {
      s_logger.error("Curve named {} is not available in curve calculation configuration called {}", curve, curveCalculationConfigName);
      return null;
    }
    final String[] applicableCurveNames = FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security, curveNames);
    if (Arrays.binarySearch(applicableCurveNames, curve) < 0) {
      s_logger.error("{} {} security is not sensitive to the curve {}", new Object[] {currency, security.getClass(), curve});
      return null;
    }
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    final Set<ValueRequirement> curveRequirements = YieldCurveFunctionUtils.getCurveRequirements(curveCalculationConfig, curveCalculationConfigSource);
    final Set<ValueRequirement> curveWithRequestedCurveRequirements = new HashSet<ValueRequirement>();
    for (final ValueRequirement requirement : curveRequirements) {
      final ValueProperties.Builder properties = requirement.getConstraints().copy();
      properties.with(PROPERTY_REQUESTED_CURVE, curve).withOptional(PROPERTY_REQUESTED_CURVE);
      curveWithRequestedCurveRequirements.add(new ValueRequirement(requirement.getValueName(), requirement.getTargetReference(), properties.get()));
    }
    requirements.addAll(curveWithRequestedCurveRequirements);
    final Set<ValueRequirement> timeSeriesRequirements = InterestRateInstrumentFunction.getDerivativeTimeSeriesRequirements(security, security.accept(_visitor), _definitionConverter);
    if (timeSeriesRequirements == null) {
      return null;
    }
    requirements.addAll(timeSeriesRequirements);
    return requirements;
  }

  protected abstract Set<ComputedValue> getResults(final InstrumentDerivative derivative, final String curveName, final YieldCurveBundle curves,
      final String curveCalculationConfigName, final String curveCalculationMethod, final FunctionInputs inputs, final ComputationTarget target,
      final ValueSpecification resultSpec);

  protected ValueProperties createValueProperties(final ComputationTarget target) {
    final Security security = target.getSecurity();
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    final ValueProperties.Builder properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .withAny(ValuePropertyNames.CURVE)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    return properties.get();
  }

  protected ValueProperties createValueProperties(final ComputationTarget target, final String curveName, final String curveCalculationConfigName) {
    final Security security = target.getSecurity();
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    final ValueProperties.Builder properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName);
    return properties.get();
  }

  protected FixedIncomeConverterDataProvider getConverter() {
    return _definitionConverter;
  }

  protected String getValueRequirement() {
    return _valueRequirement;
  }

  protected FinancialSecurityVisitor<InstrumentDefinition<?>> getVisitor() {
    return _visitor;
  }
}
