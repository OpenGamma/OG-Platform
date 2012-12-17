/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
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
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionSecurityConverter;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionTradeConverter;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.model.volatility.SmileFittingProperties;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 * Base class functions that calculate analytic values for interest rate future options using the SABR model.
 */
public abstract class IRFutureOptionSABRFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(IRFutureOptionSABRFunction.class);
  /** The SABR function */
  private static final SABRHaganVolatilityFunction SABR_FUNCTION = new SABRHaganVolatilityFunction();
  /** Converts a {@link Trade} to an {@link InstrumentDefinition} */
  private InterestRateFutureOptionTradeConverter _converter;
  /** Converts an {@link InstrumentDefinition} to {@link InstrumentDerivative} */
  private FixedIncomeConverterDataProvider _dataConverter;
  /** The values that the function can calculate */
  private final String[] _valueRequirementNames;

  /**
   * @param valueRequirementNames The value requirement names, not null or empty
   */
  public IRFutureOptionSABRFunction(final String... valueRequirementNames) {
    ArgumentChecker.notEmpty(valueRequirementNames, "value requirement names");
    _valueRequirementNames = valueRequirementNames;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    _converter = new InterestRateFutureOptionTradeConverter(new InterestRateFutureOptionSecurityConverter(holidaySource, conventionSource, regionSource, securitySource));
    _dataConverter = new FixedIncomeConverterDataProvider(conventionSource, timeSeriesResolver);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final Trade trade = target.getTrade();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final YieldCurveBundle curves = YieldCurveFunctionUtils.getAllYieldCurves(inputs, curveCalculationConfig, curveCalculationConfigSource);
    final Currency currency = FinancialSecurityUtils.getCurrency(trade.getSecurity());
    final String conventionName = currency.getCode() + "_IR_FUTURE";
    final ConventionBundle convention = conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, conventionName));
    if (convention == null) {
      throw new OpenGammaRuntimeException("Could not get convention named " + conventionName);
    }
    final DayCount dayCount = convention.getDayCount();
    if (dayCount == null) {
      throw new OpenGammaRuntimeException("Could not get daycount");
    }
    final SABRInterestRateDataBundle data = new SABRInterestRateDataBundle(getModelParameters(target, inputs, dayCount), curves);
    final InstrumentDefinition<InstrumentDerivative> irFutureOptionDefinition = (InstrumentDefinition<InstrumentDerivative>) _converter.convert(trade);
    final InstrumentDerivative irFutureOption = _dataConverter.convert(trade.getSecurity(), irFutureOptionDefinition, now, curveNames, timeSeries);
    return getResult(desiredValues, inputs, target, irFutureOption, data);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.TRADE) {
      return false;
    }
    return target.getTrade().getSecurity() instanceof IRFutureOptionSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.SURFACE)
        .withAny(SmileFittingProperties.PROPERTY_FITTING_METHOD)
        .with(SmileFittingProperties.PROPERTY_VOLATILITY_MODEL, SmileFittingProperties.SABR)
        .get();
    final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
    for (final String valueRequirement : _valueRequirementNames) {
      results.add(new ValueSpecification(valueRequirement, target.toSpecification(), properties));
    }
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveCalculationConfigs = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigs == null || curveCalculationConfigs.size() != 1) {
      return null;
    }
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final Set<String> fittingMethods = constraints.getValues(SmileFittingProperties.PROPERTY_FITTING_METHOD);
    if (fittingMethods == null || fittingMethods.size() != 1) {
      return null;
    }
    final String curveCalculationConfig = Iterables.getOnlyElement(curveCalculationConfigs);
    final String surfaceName = Iterables.getOnlyElement(surfaceNames) + "_" + IRFutureOptionFunctionHelper.getFutureOptionPrefix(target);
    final String fittingMethod = Iterables.getOnlyElement(fittingMethods);
    final Trade trade = target.getTrade();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.addAll(getCurveRequirement(trade, curveCalculationConfig, context));
    requirements.add(getSurfaceRequirement(trade, surfaceName, fittingMethod));
    final Set<ValueRequirement> timeSeriesRequirement = getTimeSeriesRequirement(trade);
    if (timeSeriesRequirement == null) {
      return null;
    }
    requirements.addAll(timeSeriesRequirement);
    return requirements;
  }

  /**
   * @param desiredValues The desired values
   * @param inputs The function inputs
   * @param target The computation target
   * @param irFutureOption The derivative form of the interest rate future option
   * @param data The SABR parameter surfaces and yield curve data
   * @return The results
   */
  protected abstract Set<ComputedValue> getResult(Set<ValueRequirement> desiredValues, FunctionInputs inputs, ComputationTarget target, InstrumentDerivative irFutureOption,
      SABRInterestRateDataBundle data);

  /**
   * @return The value requirement names
   */
  protected String[] getValueRequirementNames() {
    return _valueRequirementNames;
  }

  private Set<ValueRequirement> getCurveRequirement(final Trade trade, final String curveCalculationConfigName, final FunctionCompilationContext context) {
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + curveCalculationConfigName);
      return null;
    }
    final Currency currency = FinancialSecurityUtils.getCurrency(trade.getSecurity());
    if (!currency.equals(curveCalculationConfig.getUniqueId())) {
      s_logger.error("Security currency and curve calculation config id were not equal; have {} and {}", currency, curveCalculationConfig.getUniqueId());
      return null;
    }
    requirements.addAll(YieldCurveFunctionUtils.getCurveRequirements(curveCalculationConfig, curveCalculationConfigSource));
    return requirements;
  }

  private ValueRequirement getSurfaceRequirement(final Trade trade, final String surfaceName, final String fittingMethod) {
    final Currency currency = FinancialSecurityUtils.getCurrency(trade.getSecurity());
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURRENCY, currency.getCode())
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION)
        .with(SmileFittingProperties.PROPERTY_VOLATILITY_MODEL, SmileFittingProperties.SABR)
        .with(SmileFittingProperties.PROPERTY_FITTING_METHOD, fittingMethod)
        .get();
    return new ValueRequirement(ValueRequirementNames.SABR_SURFACES, currency, properties);
  }

  private Set<ValueRequirement> getTimeSeriesRequirement(final Trade trade) {
    return _dataConverter.getConversionTimeSeriesRequirements(trade.getSecurity(), _converter.convert(trade));
  }

  private SABRInterestRateParameters getModelParameters(final ComputationTarget target, final FunctionInputs inputs, final DayCount dayCount) {
    final Object surfacesObject = inputs.getValue(ValueRequirementNames.SABR_SURFACES);
    if (surfacesObject == null) {
      throw new OpenGammaRuntimeException("Could not get SABR fitted surfaces");
    }
    final SABRFittedSurfaces surfaces = (SABRFittedSurfaces) surfacesObject;
    final InterpolatedDoublesSurface alphaSurface = surfaces.getAlphaSurface();
    final InterpolatedDoublesSurface betaSurface = surfaces.getBetaSurface();
    final InterpolatedDoublesSurface nuSurface = surfaces.getNuSurface();
    final InterpolatedDoublesSurface rhoSurface = surfaces.getRhoSurface();
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, dayCount, SABR_FUNCTION);
  }
}
