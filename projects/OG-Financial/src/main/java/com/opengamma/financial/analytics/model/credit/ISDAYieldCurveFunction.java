/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
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
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class ISDAYieldCurveFunction extends AbstractFunction.NonCompiledInvoker {
  private static final LastTimeCalculator LAST_DATE_CALCULATOR = LastTimeCalculator.getInstance();
  private FinancialSecurityVisitor<InstrumentDefinition<?>> _securityConverter;
  private FixedIncomeConverterDataProvider _definitionConverter;

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource, regionSource, true);
    final CashSecurityConverter cashConverter = new CashSecurityConverter(holidaySource, regionSource);
    _securityConverter = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder()
        .swapSecurityVisitor(swapConverter)
        .cashSecurityVisitor(cashConverter).create();
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource, timeSeriesResolver);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final HistoricalTimeSeriesBundle timeSeries = (HistoricalTimeSeriesBundle) inputs.getValue(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String curveCalculationConfig = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final String offsetString = desiredValue.getConstraint(ISDAFunctionConstants.ISDA_CURVE_OFFSET);
    final int offset = Integer.parseInt(offsetString);
    final Object dataObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_MARKET_DATA);
    if (dataObject == null) {
      throw new OpenGammaRuntimeException("Couldn't get yield curve data for " + curveName);
    }
    final Object specObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_SPEC);
    if (specObject == null) {
      throw new OpenGammaRuntimeException("Could not get yield curve specification for " + curveName + " and target " + target.getName());
    }
    final SnapshotDataBundle marketData = (SnapshotDataBundle) dataObject;
    final InterpolatedYieldCurveSpecificationWithSecurities yieldCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) specObject;
    final int n = yieldCurveSpec.getStrips().size();
    final double[] times = new double[n];
    final double[] yields = new double[n];
    final ZonedDateTime[] curveDates = new ZonedDateTime[n];
    int i = 0;
    final String[] curveNamesForSecurity = new String[] {curveName, curveName };
    for (final FixedIncomeStripWithSecurity strip : yieldCurveSpec.getStrips()) {
      final String securityType = strip.getSecurity().getSecurityType();
      if (!(securityType.equals(CashSecurity.SECURITY_TYPE) || securityType.equals(SwapSecurity.SECURITY_TYPE) || securityType.equals(specObject))) {
        throw new OpenGammaRuntimeException("ISDA curves should only use Libor and swap rates");
      }
      final Double marketValue = marketData.getDataPoint(strip.getSecurityIdentifier());
      if (marketValue == null) {
        throw new OpenGammaRuntimeException("Could not get market data for " + strip);
      }
      final FinancialSecurity financialSecurity = (FinancialSecurity) strip.getSecurity();
      final InstrumentDefinition<?> definition = financialSecurity.accept(_securityConverter);
      final InstrumentDerivative derivative = _definitionConverter.convert(financialSecurity, definition, now, curveNamesForSecurity, timeSeries);
      if (derivative == null) {
        throw new OpenGammaRuntimeException("Had a null InterestRateDefinition for " + strip);
      }
      times[i] = derivative.accept(LAST_DATE_CALCULATOR);
      curveDates[i] = strip.getMaturity().withHour(0).withMinute(0).withSecond(0).withNano(0);
      yields[i++] = marketValue;
    }
    final ISDADateCurve curve = new ISDADateCurve(curveName, curveDates, times, yields, offset);
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ISDAFunctionConstants.ISDA_CURVE_OFFSET, offsetString)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_APPROX).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, curve));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.CURRENCY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.CURVE)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(ISDAFunctionConstants.ISDA_CURVE_OFFSET)
        .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_APPROX)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveCalculationMethod = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    if (curveCalculationMethod == null) {
      return null;
    }
    if (curveCalculationMethod.size() == 1) {
      if (!ISDAFunctionConstants.ISDA_METHOD_NAME.equals(Iterables.getOnlyElement(curveCalculationMethod))) {
        return null;
      }
      final Set<String> implementationMethod = constraints.getValues(ISDAFunctionConstants.ISDA_IMPLEMENTATION);
      if (implementationMethod != null && implementationMethod.size() == 1) {
        if (!ISDAFunctionConstants.ISDA_IMPLEMENTATION_APPROX.equals(Iterables.getOnlyElement(implementationMethod))) {
          return null;
        }
      }
    }
    final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
    if (curveNames == null || curveNames.size() != 1) {
      return null;
    }
    final Set<String> curveCalculationConfigs = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigs == null || curveCalculationConfigs.size() != 1) {
      return null;
    }
    final Set<String> isdaCurveOffsets = constraints.getValues(ISDAFunctionConstants.ISDA_CURVE_OFFSET);
    if (isdaCurveOffsets == null || isdaCurveOffsets.size() != 1) {
      return null;
    }
    final String curveName = Iterables.getOnlyElement(curveNames);
    final String curveCalculationConfig = Iterables.getOnlyElement(curveCalculationConfigs);
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .withOptional(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .get();
    final ValueProperties curveTSProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .get();
    final Set<ValueRequirement> requirements = new HashSet<>();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, targetSpec, properties));
    requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, targetSpec, properties));
    requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, targetSpec, curveTSProperties));
    return requirements;
  }

  @Override
  public boolean canHandleMissingRequirements() {
    // historical time series likely to be absent
    return true;
  }

  @Override
  public boolean canHandleMissingInputs() {
    return true;
  }


}
