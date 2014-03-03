/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetRequirement;
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
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.volatility.surface.SABRFittingPropertyUtils;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
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
  /** Converts an {@link InstrumentDefinition} to {@link InstrumentDerivative} */
  private FixedIncomeConverterDataProvider _dataConverter;
  /** The values that the function can calculate */
  private final String[] _valueRequirementNames;
  private ConfigDBCurveCalculationConfigSource _curveCalculationConfigSource;

  /**
   * @param valueRequirementNames The value requirement names, not null or empty
   */
  public IRFutureOptionSABRFunction(final String... valueRequirementNames) {
    ArgumentChecker.notEmpty(valueRequirementNames, "value requirement names");
    _valueRequirementNames = valueRequirementNames;
  }

  private InterestRateFutureOptionTradeConverterDeprecated getConverter(final FunctionExecutionContext context) {
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(context);
    return new InterestRateFutureOptionTradeConverterDeprecated(new InterestRateFutureOptionSecurityConverterDeprecated(holidaySource, conventionSource, regionSource, securitySource,
        context.getComputationTargetResolver().getVersionCorrection()));
  }

  private InterestRateFutureOptionTradeConverterDeprecated getConverter(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    return new InterestRateFutureOptionTradeConverterDeprecated(new InterestRateFutureOptionSecurityConverterDeprecated(holidaySource, conventionSource, regionSource, securitySource,
        context.getComputationTargetResolver().getVersionCorrection()));
  }

  protected ConfigDBCurveCalculationConfigSource getCurveCalculationConfigSource() {
    return _curveCalculationConfigSource;
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
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, 
      final Set<ValueRequirement> desiredValues)
    throws AsynchronousExecution {
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final Trade trade = target.getTrade();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final YieldCurveBundle curves = YieldCurveFunctionUtils.getAllYieldCurves(inputs, curveCalculationConfig, _curveCalculationConfigSource);
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
    final InstrumentDefinition<InstrumentDerivative> irFutureOptionDefinition = (InstrumentDefinition<InstrumentDerivative>) getConverter(executionContext).convert(trade);
    final InstrumentDerivative irFutureOption = _dataConverter.convert(trade.getSecurity(), irFutureOptionDefinition, now, curveNames, timeSeries);
    return getResult(executionContext, desiredValues, inputs, target, irFutureOption, data);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getTrade().getSecurity();
    if (!(security instanceof IRFutureOptionSecurity)) {
      return false;
    }
    // REVIEW Andrew 2012-01-17 -- This shouldn't be necessary; the securities in the master should be logically correct and not refer to incorrect or missing underlyings
    final ExternalId identifier = ((IRFutureOptionSecurity) security).getUnderlyingId();
    final ComputationTargetRequirement underlyingTarget = new ComputationTargetRequirement(ComputationTargetType.SECURITY, identifier);
    final ComputationTargetSpecification underlyingSpecification = context.getComputationTargetResolver().getSpecificationResolver().getTargetSpecification(underlyingTarget);
    if (underlyingSpecification == null) {
      s_logger.error("Loader error: " + security.getName() + " - cannot resolve underlying identifier " + identifier);
      return false;
    }
    final ComputationTarget underlying = context.getComputationTargetResolver().resolve(underlyingSpecification);
    if (underlying == null) {
      s_logger.error("Loader error: " + security.getName() + " - cannot resolve underlying " + underlyingSpecification);
      return false;
    }
    final Security underlyingSecurity = underlying.getSecurity();
    if (!(underlying.getValue() instanceof InterestRateFutureSecurity)) {
      s_logger.error("Loader error: " + security.getName() + " - IRateFutureOption has an underlying that is not an IRFuture: " + underlyingSecurity.getName());
      return false;
    }
    return true;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = ValueProperties.all();
    final Set<ValueSpecification> results = new HashSet<>();
    for (final String valueRequirement : _valueRequirementNames) {
      results.add(new ValueSpecification(valueRequirement, target.toSpecification(), properties));
    }
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> calculationMethod = constraints.getValues(ValuePropertyNames.CALCULATION_METHOD);
    if (calculationMethod != null && calculationMethod.size() == 1) {
      if (!Iterables.getOnlyElement(calculationMethod).equals(SmileFittingPropertyNamesAndValues.SABR)) {
        return null;
      }
    }
    final Set<String> curveCalculationConfigs = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigs == null || curveCalculationConfigs.size() != 1) {
      return null;
    }
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final Set<String> fittingMethods = constraints.getValues(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD);
    if (fittingMethods == null || fittingMethods.size() != 1) {
      return null;
    }
    final String curveCalculationConfig = Iterables.getOnlyElement(curveCalculationConfigs);
    final String surfaceName = Iterables.getOnlyElement(surfaceNames) + "_" + IRFutureOptionFunctionHelper.getFutureOptionPrefix(target);
    final Trade trade = target.getTrade();
    final Currency currency = FinancialSecurityUtils.getCurrency(trade.getSecurity());
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.addAll(getCurveRequirement(trade, curveCalculationConfig, context));
    final ValueRequirement surfaceRequirement = SABRFittingPropertyUtils.getSurfaceRequirement(desiredValue, surfaceName, currency, InstrumentTypeProperties.IR_FUTURE_OPTION);
    if (surfaceRequirement == null) {
      return null;
    }
    requirements.add(surfaceRequirement);
    // REVIEW Andrew 2012-01-17 -- This check shouldn't be necessary; we know the security is a IRFutureOptionSecurity because of #canApplyTo
    /*
    final SecuritySource secSource = context.getSecuritySource();
    final Security secFromIdBundle = secSource.getSingle(security.getExternalIdBundle());
    if (!(secFromIdBundle instanceof IRFutureOptionSecurity)) {
      //  s_logger.error("Loader error: " + secFromIdBundle.toString() + " has been loaded as an InterestRateFutureOption.");
      return null;
    }
     */
    final Set<ValueRequirement> timeSeriesRequirement = getTimeSeriesRequirement(context, trade);
    if (timeSeriesRequirement == null) {
      return null;
    }
    requirements.addAll(timeSeriesRequirement);
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String surfaceName = null;
    boolean curvePropertiesSet = false;
    boolean surfacePropertiesSet = false;
    ValueProperties.Builder properties = createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, SmileFittingPropertyNamesAndValues.SABR).with(ValuePropertyNames.CURRENCY,
        FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode());
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification value = entry.getKey();
      final String inputName = value.getValueName();
      if (inputName.equals(ValueRequirementNames.YIELD_CURVE) && !curvePropertiesSet) {
        final ValueProperties curveProperties = value.getProperties().copy().withoutAny(ValuePropertyNames.FUNCTION).withoutAny(ValuePropertyNames.CURVE)
            .withoutAny(ValuePropertyNames.CURRENCY).get();
        for (final String property : curveProperties.getProperties()) {
          properties.with(property, curveProperties.getValues(property));
        }
        curvePropertiesSet = true;
      } else if (inputName.equals(ValueRequirementNames.SABR_SURFACES) && !surfacePropertiesSet) {
        final String fullSurfaceName = value.getProperty(ValuePropertyNames.SURFACE);
        surfaceName = fullSurfaceName.substring(0, fullSurfaceName.length() - 3);
        final ValueProperties surfaceFittingProperties = value.getProperties().copy().withoutAny(ValuePropertyNames.FUNCTION)
            .withoutAny(SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL).withoutAny(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE)
            .withoutAny(ValuePropertyNames.CURRENCY).withoutAny(ValuePropertyNames.SURFACE).get();
        for (final String property : surfaceFittingProperties.getProperties()) {
          properties.with(property, surfaceFittingProperties.getValues(property));
        }
        surfacePropertiesSet = true;
      }
    }
    assert surfaceName != null;
    assert curvePropertiesSet;
    assert surfacePropertiesSet;
    properties = properties.with(ValuePropertyNames.SURFACE, surfaceName);
    final Set<ValueSpecification> results = new HashSet<>();
    for (final String valueRequirement : _valueRequirementNames) {
      results.add(new ValueSpecification(valueRequirement, target.toSpecification(), properties.get()));
    }
    return results;
  }

  /**
   * @param context The function execution context
   * @param desiredValues The desired values
   * @param inputs The function inputs
   * @param target The computation target
   * @param irFutureOption The derivative form of the interest rate future option
   * @param data The SABR parameter surfaces and yield curve data
   * @return The results
   */
  protected abstract Set<ComputedValue> getResult(FunctionExecutionContext context, Set<ValueRequirement> desiredValues, FunctionInputs inputs, ComputationTarget target,
      InstrumentDerivative irFutureOption, SABRInterestRateDataBundle data);

  /**
   * @return The value requirement names
   */
  protected String[] getValueRequirementNames() {
    return _valueRequirementNames;
  }

  private Set<ValueRequirement> getCurveRequirement(final Trade trade, final String curveCalculationConfigName, final FunctionCompilationContext context) {
    final Set<ValueRequirement> requirements = new HashSet<>();
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + curveCalculationConfigName);
      return null;
    }
    final Currency currency = FinancialSecurityUtils.getCurrency(trade.getSecurity());
    if (!ComputationTargetSpecification.of(currency).equals(curveCalculationConfig.getTarget())) {
      s_logger.error("Security currency and curve calculation config id were not equal; have {} and {}", currency, curveCalculationConfig.getTarget());
      return null;
    }
    requirements.addAll(YieldCurveFunctionUtils.getCurveRequirements(curveCalculationConfig, _curveCalculationConfigSource));
    return requirements;
  }

  private Set<ValueRequirement> getTimeSeriesRequirement(final FunctionCompilationContext context, final Trade trade) {
    return _dataConverter.getConversionTimeSeriesRequirements(trade.getSecurity(), getConverter(context).convert(trade));
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
