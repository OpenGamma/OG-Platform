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
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
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
import com.opengamma.financial.analytics.conversion.InterestRateInstrumentTradeOrSecurityConverter;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.CurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;

/**
 * Function to source time series data from a {@link HistoricalTimeSeriesSource} attached to the execution context needed to convert each of the instruments in a curve to their OG-Analytics derivative
 * form.
 */
public class YieldCurveInstrumentConversionHistoricalTimeSeriesFunction extends AbstractFunction.NonCompiledInvoker {

  private FixedIncomeConverterDataProvider _definitionConverter;
  private CurveCalculationConfigSource _curveCalculationConfig;

  protected InterestRateInstrumentTradeOrSecurityConverter getSecurityConverter(final FunctionExecutionContext context) {
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(context);
    return new InterestRateInstrumentTradeOrSecurityConverter(holidaySource, conventionSource, regionSource, securitySource, true, context.getComputationTargetResolver()
        .getVersionCorrection());
  }

  protected FixedIncomeConverterDataProvider getDefinitionConverter() {
    return _definitionConverter;
  }

  protected CurveCalculationConfigSource getCurveCalculationConfig() {
    return _curveCalculationConfig;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context); // TODO [PLAT-5966] Remove
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource, securitySource, timeSeriesResolver);
    _curveCalculationConfig = ConfigDBCurveCalculationConfigSource.init(context, this);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.CURRENCY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, target.toSpecification(), createValueProperties()
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG).get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final String curveCalculationConfigName = desiredValue.getConstraints().getStrictValue(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigName == null) {
      return null;
    }
    final Set<ValueRequirement> requirements = new HashSet<>();
    final MultiCurveCalculationConfig curveCalculationConfig = getCurveCalculationConfig().getConfig(curveCalculationConfigName);
    for (final String curveName : curveCalculationConfig.getYieldCurveNames()) {
      final ValueProperties properties = ValueProperties.with(ValuePropertyNames.CURVE, curveName).get();
      requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, target.toSpecification(), properties));
    }
    return requirements;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final Set<ValueRequirement> timeSeriesRequirements = new HashSet<>();
    final HistoricalTimeSeriesBundle timeSeries = new HistoricalTimeSeriesBundle();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final InterestRateInstrumentTradeOrSecurityConverter securityConverter = getSecurityConverter(executionContext);
    for (final ComputedValue input : inputs.getAllValues()) {
      if (!input.getSpecification().getValueName().equals(ValueRequirementNames.YIELD_CURVE_SPEC)) {
        continue;
      }
      final String curveName = input.getSpecification().getProperty(ValuePropertyNames.CURVE);
      final InterpolatedYieldCurveSpecificationWithSecurities curve = (InterpolatedYieldCurveSpecificationWithSecurities) input.getValue();
      for (final FixedIncomeStripWithSecurity strip : curve.getStrips()) {
        final InstrumentDefinition<?> definition = securityConverter.visit(strip.getSecurity());
        final Set<ValueRequirement> requirements = getDefinitionConverter().getConversionTimeSeriesRequirements(strip.getSecurity(), definition);
        if (requirements == null) {
          throw new OpenGammaRuntimeException("Can't get time series requirements for " + strip + " on " + curveName);
        }
        timeSeriesRequirements.addAll(requirements);
      }
      final HistoricalTimeSeriesSource timeSeriesSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
      for (final ValueRequirement timeSeriesRequirement : timeSeriesRequirements) {
        final HistoricalTimeSeries hts = HistoricalTimeSeriesFunction.executeImpl(executionContext, timeSeriesSource, timeSeriesRequirement.getTargetReference().getSpecification(),
            timeSeriesRequirement);
        if (hts == null) {
          throw new OpenGammaRuntimeException("Can't get time series for " + timeSeriesRequirement);
        }
        timeSeries.add(timeSeriesRequirement.getConstraint(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY), timeSeriesSource.getExternalIdBundle(hts.getUniqueId()), hts);
      }
    }
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName).get();
    return Collections
        .singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, targetSpec, properties), timeSeries));
  }

}
