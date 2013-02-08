/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.LastTimeCalculator;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
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
import com.opengamma.financial.analytics.conversion.CashSecurityConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverter;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.model.cds.ISDAFunctionConstants;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class ISDAYieldCurveFunction extends AbstractFunction {
  private static final LastTimeCalculator LAST_DATE_CALCULATOR = LastTimeCalculator.getInstance();

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext compilationContext, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(compilationContext);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(compilationContext);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(compilationContext);
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(compilationContext);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource, regionSource, true);
    final CashSecurityConverter cashConverter = new CashSecurityConverter(holidaySource, regionSource);
    final FinancialSecurityVisitor<InstrumentDefinition<?>> securityConverter = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder()
        .swapSecurityVisitor(swapConverter)
        .cashSecurityVisitor(cashConverter).create();
    final FixedIncomeConverterDataProvider definitionConverter = new FixedIncomeConverterDataProvider(conventionSource, timeSeriesResolver);
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000)) {

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
        final Clock snapshotClock = executionContext.getValuationClock();
        final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
        final HistoricalTimeSeriesBundle timeSeries = (HistoricalTimeSeriesBundle) inputs.getValue(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES);
        final ValueRequirement desiredValue = desiredValues.iterator().next();
        final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
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
        final Map<ExternalId, Double> marketData = data.getDataPoints();
        final int n = marketData.size();
        final double[] times = new double[n];
        final double[] yields = new double[n];
        int i = 0;
        final String[] curveNamesForSecurity = new String[] {curveName, curveName };
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
          final InstrumentDefinition<?> definition = financialSecurity.accept(securityConverter);
          final InstrumentDerivative derivative = definitionConverter.convert(financialSecurity, definition, now, curveNamesForSecurity, timeSeries);
          if (derivative == null) {
            throw new OpenGammaRuntimeException("Had a null InterestRateDefinition for " + strip);
          }
          times[i] = derivative.accept(LAST_DATE_CALCULATOR);
          yields[i++] = marketValue;
        }
        final ISDACurve curve = new ISDACurve(curveName, times, yields, offset);
        final ValueProperties properties = createValueProperties()
            .with(ValuePropertyNames.CURVE, curveName)
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
        return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        @SuppressWarnings("synthetic-access")
        final ValueProperties properties = createValueProperties()
            .withAny(ValuePropertyNames.CURVE)
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
        final String curveName = Iterables.getOnlyElement(curveNames);
        final ValueProperties properties = ValueProperties.builder()
            .with(ValuePropertyNames.CURVE, curveName).get();
        final Set<ValueRequirement> requirements = Sets.newHashSetWithExpectedSize(3);
        final ComputationTargetSpecification targetSpec = target.toSpecification();
        requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, targetSpec, properties));
        requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, targetSpec, properties));
        requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, targetSpec, properties));
        return requirements;
      }
    };
  }

}
