/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
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
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateInstrumentTradeOrSecurityConverter;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.CurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.money.Currency;

/**
 * Function to source time series data from a {@link HistoricalTimeSeriesSource} attached to the execution context needed to convert each of the instruments in a curve to their OG-Analytics derivative
 * form.
 */
public class YieldCurveInstrumentConversionHistoricalTimeSeriesFunction extends AbstractFunction.NonCompiledInvoker {

  private InterestRateInstrumentTradeOrSecurityConverter _securityConverter;
  private FixedIncomeConverterDataProvider _definitionConverter;
  private CurveCalculationConfigSource _curveCalculationConfig;

  protected InterestRateInstrumentTradeOrSecurityConverter getSecurityConverter() {
    return _securityConverter;
  }

  protected FixedIncomeConverterDataProvider getDefinitionConverter() {
    return _definitionConverter;
  }

  protected CurveCalculationConfigSource getCurveCalculationConfig() {
    return _curveCalculationConfig;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    _securityConverter = new InterestRateInstrumentTradeOrSecurityConverter(holidaySource, conventionSource, regionSource, securitySource, true);
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource, timeSeriesResolver);
    _curveCalculationConfig = new ConfigDBCurveCalculationConfigSource(configSource);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final UniqueId uid = target.getUniqueId();
    return (uid != null) && uid.getScheme().equals(Currency.OBJECT_SCHEME);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, target.toSpecification(), createValueProperties()
        .withAny(ValuePropertyNames.CURVE).withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG).get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> curveNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
    if ((curveNames == null) || (curveNames.size() != 1)) {
      return null;
    }
    final Set<String> curveCalculations = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if ((curveCalculations == null) || (curveCalculations.size() != 1)) {
      return null;
    }
    return Collections.singleton(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, target.toSpecification(), desiredValue.getConstraints()));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final MultiCurveCalculationConfig curveCalculationConfig = getCurveCalculationConfig().getConfig(desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG));
    final InterpolatedYieldCurveSpecificationWithSecurities curve = (InterpolatedYieldCurveSpecificationWithSecurities) inputs.getValue(ValueRequirementNames.YIELD_CURVE_SPEC);
    final Set<ValueRequirement> timeSeriesRequirements = new HashSet<ValueRequirement>();
    for (final FixedIncomeStripWithSecurity strip : curve.getStrips()) {
      final String[] curveNamesForSecurity = curveCalculationConfig.getCurveExposureForInstrument(curveName, strip.getInstrumentType());
      final InstrumentDefinition<?> definition = getSecurityConverter().visit(strip.getSecurity());
      final Set<ValueRequirement> requirements = getDefinitionConverter().getConversionTimeSeriesRequirements(strip.getSecurity(), definition, curveNamesForSecurity);
      if (requirements == null) {
        throw new OpenGammaRuntimeException("Can't get time series requirements for " + strip + " on " + curveName);
      }
      timeSeriesRequirements.addAll(requirements);
    }
    final HistoricalTimeSeriesBundle timeSeries = new HistoricalTimeSeriesBundle();
    final HistoricalTimeSeriesSource timeSeriesSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    for (ValueRequirement timeSeriesRequirement : timeSeriesRequirements) {
      final HistoricalTimeSeries hts = HistoricalTimeSeriesFunction.executeImpl(executionContext, timeSeriesSource, timeSeriesRequirement);
      if (hts == null) {
        throw new OpenGammaRuntimeException("Can't get time series for " + timeSeriesRequirement);
      }
      timeSeries.add(timeSeriesRequirement.getConstraint(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY), timeSeriesSource.getExternalIdBundle(hts.getUniqueId()), hts);
    }
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, target.toSpecification(), desiredValue
        .getConstraints()), timeSeries));
  }

}
