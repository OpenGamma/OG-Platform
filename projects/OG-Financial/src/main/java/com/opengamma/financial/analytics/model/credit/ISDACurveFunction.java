/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.InstantProvider;
import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.LastTimeCalculator;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
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
import com.opengamma.financial.analytics.conversion.InterestRateInstrumentTradeOrSecurityConverter;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunctionHelper;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.cds.ISDAFunctionConstants;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class ISDACurveFunction extends AbstractFunction {
  private static final LastTimeCalculator LAST_DATE_CALCULATOR = LastTimeCalculator.getInstance();

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext compilationContext, final InstantProvider atInstantProvider) {
    final ZonedDateTime atInstant = ZonedDateTime.ofInstant(atInstantProvider, TimeZone.UTC);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(compilationContext);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(compilationContext);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(compilationContext);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(compilationContext);
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(compilationContext);
    final InterestRateInstrumentTradeOrSecurityConverter securityConverter = new InterestRateInstrumentTradeOrSecurityConverter(holidaySource, conventionSource, regionSource, securitySource, true);
    final FixedIncomeConverterDataProvider definitionConverter = new FixedIncomeConverterDataProvider(conventionSource, timeSeriesResolver);
    return new AbstractInvokingCompiledFunction(atInstant.withTime(0, 0), atInstant.plusDays(1).withTime(0, 0).minusNanos(1000000)) {

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
        final Clock snapshotClock = executionContext.getValuationClock();
        final ZonedDateTime now = snapshotClock.zonedDateTime();
        final HistoricalTimeSeriesBundle timeSeries = (HistoricalTimeSeriesBundle) inputs.getValue(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES);
        final ValueRequirement desiredValue = desiredValues.iterator().next();
        final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
        final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
        final String offsetString = desiredValue.getConstraint(ISDAFunctionConstants.ISDA_CURVE_OFFSET);
        final int offset = Integer.parseInt(offsetString);
        final Object specificationObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_SPEC);
        if (specificationObject == null) {
          throw new OpenGammaRuntimeException("Could not get interpolated yield curve specification");
        }
        final Object dataObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_MARKET_DATA);
        if (dataObject == null) {
          throw new OpenGammaRuntimeException("Could not get yield curve data");
        }
        final InterpolatedYieldCurveSpecificationWithSecurities specification = (InterpolatedYieldCurveSpecificationWithSecurities) specificationObject;
        final SnapshotDataBundle data = (SnapshotDataBundle) dataObject;
        final Map<ExternalId, Double> marketData = YieldCurveFunctionHelper.buildMarketDataMap(data);
        final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
        final MultiCurveCalculationConfig curveCalculationConfig = new ConfigDBCurveCalculationConfigSource(configSource).getConfig(curveCalculationConfigName);
        final int n = marketData.size();
        final double[] times = new double[n];
        final double[] yields = new double[n];
        int i = 0;
        for (final FixedIncomeStripWithSecurity strip : specification.getStrips()) {
          final String securityType = strip.getSecurity().getSecurityType();
          if (!(securityType.equals(CashSecurity.SECURITY_TYPE) || securityType.equals(SwapSecurity.SECURITY_TYPE))) {
            throw new OpenGammaRuntimeException("ISDA curves should only use Libor and swap rates");
          }
          final Double marketValue = marketData.get(strip.getSecurityIdentifier());
          if (marketValue == null) {
            throw new OpenGammaRuntimeException("Could not get market data for " + strip);
          }
          final FinancialSecurity financialSecurity = (FinancialSecurity) strip.getSecurity();
          final String[] curveNamesForSecurity = curveCalculationConfig.getCurveExposureForInstrument(curveName, strip.getInstrumentType());
          final InstrumentDefinition<?> definition = securityConverter.visit(financialSecurity);
          final InstrumentDerivative derivative = definitionConverter.convert(financialSecurity, definition, now, curveNamesForSecurity, timeSeries);
          if (derivative == null) {
            throw new OpenGammaRuntimeException("Had a null InterestRateDefinition for " + strip);
          }
          times[i] = LAST_DATE_CALCULATOR.visit(derivative);
          yields[i++] = marketValue;
        }
        final ISDACurve curve = new ISDACurve(curveCalculationConfigName, times, yields, offset);
        final ValueProperties properties = createValueProperties()
            .with(ValuePropertyNames.CURVE, curveName)
            .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
            .with(ISDAFunctionConstants.ISDA_CURVE_OFFSET, offsetString)
            .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME).get();
        final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(spec, curve));
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
        if (target.getType() != ComputationTargetType.PRIMITIVE) {
          return false;
        }
        return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        @SuppressWarnings("synthetic-access")
        final ValueProperties properties = createValueProperties()
            .withAny(ValuePropertyNames.CURVE)
            .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
            .withAny(ISDAFunctionConstants.ISDA_CURVE_OFFSET)
            .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME).get();
        return Collections.singleton(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), properties));
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
        if (curveNames == null || curveNames.size() != 1) {
          return null;
        }
        final Set<String> curveCalculationConfigNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
        if (curveCalculationConfigNames == null || curveCalculationConfigNames.size() != 1) {
          return null;
        }
        final String curveName = Iterables.getOnlyElement(curveNames);
        final String curveCalculationConfigName = Iterables.getOnlyElement(curveCalculationConfigNames);
        final ValueProperties tsProperties = ValueProperties.builder()
            .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName).get();
        final ValueProperties curveProperties = ValueProperties.builder()
            .with(ValuePropertyNames.CURVE, curveName)
            .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName).get();
        final Set<ValueRequirement> requirements = Sets.newHashSetWithExpectedSize(3);
        final ComputationTargetSpecification targetSpec = target.toSpecification();
        requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, targetSpec, curveProperties));
        requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, targetSpec, curveProperties));
        requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, targetSpec, tsProperties));
        return requirements;
      }
    };
  }

}
