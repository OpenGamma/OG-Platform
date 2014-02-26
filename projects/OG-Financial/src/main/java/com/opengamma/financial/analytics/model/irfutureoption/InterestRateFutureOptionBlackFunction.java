/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
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
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionSecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionTradeConverterDeprecated;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.model.black.BlackDiscountingIRFutureOptionFunction;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Base class for a range of functions computing values on an IRFuturesOption using the Black Model
 * 
 * @deprecated Use classes that descend from {@link BlackDiscountingIRFutureOptionFunction}
 */
@Deprecated
public abstract class InterestRateFutureOptionBlackFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateFutureOptionBlackFunction.class);
  /** The name of the value that will be calculated */
  private final String _valueRequirementName;
  /** True if the result properties include {@link ValuePropertyNames#CURRENCY} */
  private final boolean _setCurrencyProperty;
  /** Converts an {@link InstrumentDefinition} to an {@link InstrumentDerivative} */
  private FixedIncomeConverterDataProvider _dataConverter;
  private ConfigDBCurveCalculationConfigSource _curveCalculationConfigSource;

  /**
   * @param valueRequirementName The value requirement name, not null
   * @param setCurrencyProperty True if the result properties include {@link ValuePropertyNames#CURRENCY}
   */
  public InterestRateFutureOptionBlackFunction(final String valueRequirementName, final boolean setCurrencyProperty) {
    ArgumentChecker.notNull(valueRequirementName, "value requirement name");
    _valueRequirementName = valueRequirementName;
    _setCurrencyProperty = setCurrencyProperty;
  }

  /**
   * Gets the interest rate future option trade converter.
   * 
   * @param context The compilation context
   * @return The converter
   */
  private static InterestRateFutureOptionTradeConverterDeprecated getConverter(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context); // TODO [PLAT-5966] Remove
    return new InterestRateFutureOptionTradeConverterDeprecated(new InterestRateFutureOptionSecurityConverterDeprecated(holidaySource, conventionSource, regionSource, securitySource,
        context.getComputationTargetResolver().getVersionCorrection()));
  }

  /**
   * Gets the interest rate future option trade converter.
   * 
   * @param context The execution context
   * @return The converter
   */
  private static InterestRateFutureOptionTradeConverterDeprecated getConverter(final FunctionExecutionContext context) {
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(context);
    return new InterestRateFutureOptionTradeConverterDeprecated(new InterestRateFutureOptionSecurityConverterDeprecated(holidaySource, conventionSource, regionSource, securitySource,
        context.getComputationTargetResolver().getVersionCorrection()));
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context); // TODO [PLAT-5966] Remove
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    _dataConverter = new FixedIncomeConverterDataProvider(conventionSource, securitySource, timeSeriesResolver);
    _curveCalculationConfigSource = ConfigDBCurveCalculationConfigSource.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final Trade trade = target.getTrade();
    final IRFutureOptionSecurity security = (IRFutureOptionSecurity) trade.getSecurity();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String surfaceNameWithPrefix = surfaceName + "_" + IRFutureOptionFunctionHelper.getFutureOptionPrefix(target); // Done to enable standard and midcurve options to share the same default name
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final String[] fullCurveNames = new String[Math.max(2, curveNames.length)];
    for (int i = 0; i < curveNames.length; i++) {
      fullCurveNames[i] = curveNames[i] + "_" + currency.getCode();
    }
    if (curveNames.length == 1) { // MultiCurveCalculationConfig contains just a single curve for discounting and forwarding
      fullCurveNames[1] = fullCurveNames[0];
    }
    final YieldCurveBundle curves = YieldCurveFunctionUtils.getAllYieldCurves(inputs, curveCalculationConfig, _curveCalculationConfigSource);
    final Object volatilitySurfaceObject = inputs.getValue(getVolatilityRequirement(surfaceNameWithPrefix, currency));
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface");
    }
    final VolatilitySurface volatilitySurface = (VolatilitySurface) volatilitySurfaceObject;
    if (!(volatilitySurface.getSurface() instanceof InterpolatedDoublesSurface)) {
      throw new OpenGammaRuntimeException("Expecting an InterpolatedDoublesSurface; got " + volatilitySurface.getSurface().getClass());
    }
    final InstrumentDefinition<?> irFutureOptionDefinition = getConverter(executionContext).convert(trade);
    final InstrumentDerivative irFutureOption = _dataConverter.convert(security, irFutureOptionDefinition, now, fullCurveNames, timeSeries);
    final ValueSpecification spec = new ValueSpecification(_valueRequirementName, target.toSpecification(), desiredValue.getConstraints());
    final YieldCurveWithBlackCubeBundle data = new YieldCurveWithBlackCubeBundle(volatilitySurface.getSurface(), curves);
    return getResult(irFutureOption, data, spec, desiredValues);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getTrade().getSecurity() instanceof IRFutureOptionSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode();
    return Collections.singleton(new ValueSpecification(_valueRequirementName, target.toSpecification(), getResultProperties(currency).get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    String surfaceName = constraints.getStrictValue(ValuePropertyNames.SURFACE);
    if (surfaceName == null) {
      return null;
    }
    final String curveCalculationConfigName = constraints.getStrictValue(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigName == null) {
      return null;
    }
    surfaceName = surfaceName + "_" + IRFutureOptionFunctionHelper.getFutureOptionPrefix(target);
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + curveCalculationConfigName);
      return null;
    }
    final Trade trade = target.getTrade();
    final Currency currency = FinancialSecurityUtils.getCurrency(trade.getSecurity());
    if (!ComputationTargetSpecification.of(currency).equals(curveCalculationConfig.getTarget())) {
      s_logger.error("Security currency and curve calculation config id were not equal; have {} and {}", currency, curveCalculationConfig.getTarget());
      return null;
    }
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.addAll(YieldCurveFunctionUtils.getCurveRequirements(curveCalculationConfig, _curveCalculationConfigSource));
    requirements.add(getVolatilityRequirement(surfaceName, currency));
    try {
      final Set<ValueRequirement> tsRequirements = _dataConverter.getConversionTimeSeriesRequirements(trade.getSecurity(), getConverter(context).convert(trade));
      if (tsRequirements != null) {
        requirements.addAll(tsRequirements);
      }
    } catch (final Exception e) {
      s_logger.error(e.getMessage());
      return null;
    }
    return requirements;
  }

  /**
   * Calculates the result
   * 
   * @param irFutureOption The IR future option
   * @param data The data used in pricing
   * @param spec The value specification of the result
   * @param desiredValues The constraints on the function
   * @return The result
   */
  protected abstract Set<ComputedValue> getResult(final InstrumentDerivative irFutureOption, final YieldCurveWithBlackCubeBundle data, final ValueSpecification spec,
      Set<ValueRequirement> desiredValues);

  /**
   * Gets the result properties.
   * 
   * @param currency The currency
   * @return The result properties
   */
  protected ValueProperties.Builder getResultProperties(final String currency) {
    final ValueProperties.Builder builder = createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG).withAny(ValuePropertyNames.SURFACE);
    if (_setCurrencyProperty) {
      return builder.with(ValuePropertyNames.CURRENCY, currency);
    }
    return builder;
  }

  /**
   * Gets the volatility surface requirement.
   * 
   * @param surface The surface name
   * @param currency The currency
   * @return The volatility surface requirement
   */
  private static ValueRequirement getVolatilityRequirement(final String surface, final Currency currency) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.SURFACE, surface)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION).get();
    return new ValueRequirement(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE, ComputationTargetSpecification.of(currency), properties);
  }

}
