/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption.black;

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
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.config.ConfigSource;
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
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.SwaptionSecurityConverterDeprecated;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.model.black.BlackDiscountingSwaptionFunction;
import com.opengamma.financial.analytics.model.swaption.SwaptionUtils;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Base class for functions that produce risk for swaptions using the Black method.
 *
 * @deprecated Use descendants of {@link BlackDiscountingSwaptionFunction}
 */
@Deprecated
public abstract class SwaptionBlackFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(SwaptionBlackFunction.class);
  /** The value requirement name handled by the function instance */
  private final String _valueRequirementName;
  /** Converts {@link SwaptionSecurity} to {@link InstrumentDefinition} */
  private SwaptionSecurityConverterDeprecated _visitor;
  /** Converter {@link InstrumentDefinition} to {@link InstrumentDerivative} */
  private FixedIncomeConverterDataProvider _definitionConverter;

  /**
   * @param valueRequirementName The value requirement name, not null
   */
  public SwaptionBlackFunction(final String valueRequirementName) {
    ArgumentChecker.notNull(valueRequirementName, "value requirement name");
    _valueRequirementName = valueRequirementName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final SwapSecurityConverterDeprecated swapConverter = new SwapSecurityConverterDeprecated(holidaySource, conventionSource, regionSource, false);
    _visitor = new SwaptionSecurityConverterDeprecated(securitySource, swapConverter);
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource, timeSeriesResolver);
    ConfigDBCurveCalculationConfigSource.reinitOnChanges(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
    final SwaptionSecurity security = (SwaptionSecurity) target.getSecurity();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    String[] curveNames = curveCalculationConfig.getYieldCurveNames(); //TODO
    if (curveNames.length == 1) {
      curveNames = new String[] {curveNames[0], curveNames[0] };
    }
    final String[] fullCurveNames = new String[curveNames.length];
    for (int i = 0; i < curveNames.length; i++) {
      fullCurveNames[i] = curveNames[i] + "_" + currency.getCode();
    }
    final YieldCurveBundle curves = YieldCurveFunctionUtils.getYieldCurves(inputs, curveCalculationConfig);
    final Object volatilitySurfaceObject = inputs.getValue(getVolatilityRequirement(surfaceName, currency));
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface");
    }
    final VolatilitySurface volatilitySurface = (VolatilitySurface) volatilitySurfaceObject;
    if (!(volatilitySurface.getSurface() instanceof InterpolatedDoublesSurface)) {
      throw new OpenGammaRuntimeException("Expecting an InterpolatedDoublesSurface; got " + volatilitySurface.getSurface().getClass());
    }
    final InstrumentDefinition<?> definition = security.accept(_visitor);
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final InstrumentDerivative swaption = _definitionConverter.convert(security, definition, now, fullCurveNames, timeSeries);
    final ValueProperties properties = getResultProperties(currency.getCode(), curveCalculationConfigName, surfaceName);
    final ValueSpecification spec = new ValueSpecification(_valueRequirementName, target.toSpecification(), properties);
    final BlackFlatSwaptionParameters parameters = new BlackFlatSwaptionParameters(volatilitySurface.getSurface(),
        SwaptionUtils.getSwapGenerator(security, definition, securitySource));
    final YieldCurveWithBlackSwaptionBundle data = new YieldCurveWithBlackSwaptionBundle(parameters, curves);
    return getResult(swaption, data, spec);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.SWAPTION_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    return Collections.singleton(new ValueSpecification(_valueRequirementName, target.toSpecification(), getResultProperties(currency)));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
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
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    if (!ComputationTargetSpecification.of(currency).equals(curveCalculationConfig.getTarget())) {
      s_logger.error("Security currency and curve calculation config id were not equal; have {} and {}", currency, curveCalculationConfig.getTarget());
      return null;
    }
    final String surfaceName = surfaceNames.iterator().next();
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.addAll(YieldCurveFunctionUtils.getCurveRequirements(curveCalculationConfig, curveCalculationConfigSource));
    requirements.add(getVolatilityRequirement(surfaceName, currency));
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    try {
      final Set<ValueRequirement> timeSeriesRequirements = _definitionConverter.getConversionTimeSeriesRequirements(security, security.accept(_visitor));
      if (timeSeriesRequirements == null) {
        return null;
      }
      requirements.addAll(timeSeriesRequirements);
      return requirements;
    } catch (final Exception e) {
      s_logger.error(e.getMessage());
      return null;
    }
  }

  /**
   * Calculates the desired results.
   * @param swaption The swaption
   * @param data The yield curve and surface data
   * @param spec The result specification
   * @return A set of the desired results
   */
  protected abstract Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final ValueSpecification spec);

  /**
   * Gets the result properties.
   * @param currency The currency
   * @return The result properties
   */
  private ValueProperties getResultProperties(final String currency) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD)
        .withAny(ValuePropertyNames.SURFACE)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .with(ValuePropertyNames.CURRENCY, currency).get();
  }

  /**
   * Gets the result properties.
   * @param currency The currency
   * @param curveCalculationConfigName The curve calculation configuration name
   * @param surfaceName The surface name
   * @return The result properties
   */
  private ValueProperties getResultProperties(final String currency, final String curveCalculationConfigName, final String surfaceName) {
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.SURFACE, surfaceName).get();
    return properties;

  }

  /**
   * Gets the volatility surface requirement.
   * @param surface The surface name
   * @param currency The currency of the surface requirement target
   * @return The volatility surface requirement
   */
  private static ValueRequirement getVolatilityRequirement(final String surface, final Currency currency) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surface)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.SWAPTION_ATM).get();
    return new ValueRequirement(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE, ComputationTargetSpecification.of(currency), properties);
  }
}
