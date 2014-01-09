/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.SAMPLING_FUNCTION;
import static com.opengamma.engine.value.ValuePropertyNames.SAMPLING_PERIOD;
import static com.opengamma.engine.value.ValuePropertyNames.SCHEDULE_CALCULATOR;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.schedule.HolidayDateRemovalFunction;
import com.opengamma.analytics.financial.schedule.Schedule;
import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunction;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesDifferenceOperator;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.core.value.MarketDataRequirementNames;
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
import com.opengamma.financial.analytics.LocalDateLabelledMatrix1D;
import com.opengamma.financial.analytics.curve.ConfigDBCurveSpecificationBuilder;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.CurveUtils;
import com.opengamma.financial.analytics.curve.credit.ConfigDBCurveDefinitionSource;
import com.opengamma.financial.analytics.curve.credit.CurveDefinitionSource;
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.model.credit.CreditSecurityToIdentifierVisitor;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.cds.LegacyCDSSecurity;
import com.opengamma.financial.security.cds.StandardCDSSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * 
 */
public class CreditInstrumentCS01PnLFunction extends AbstractFunction.NonCompiledInvoker {
  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  /** A calendar containing only weekends */
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  /** Calculates the first difference of a time series */
  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();

  private CurveDefinitionSource _curveDefinitionSource;
  private CurveSpecificationBuilder _curveSpecificationBuilder;

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveDefinitionSource = ConfigDBCurveDefinitionSource.init(context, this);
    _curveSpecificationBuilder = ConfigDBCurveSpecificationBuilder.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues)
      throws AsynchronousExecution {
    final Position position = target.getPosition();
    final Clock snapshotClock = executionContext.getValuationClock();
    final LocalDate now = ZonedDateTime.now(snapshotClock).toLocalDate();
    final String currency = FinancialSecurityUtils.getCurrency(position.getSecurity()).getCode();
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final ValueProperties constraints = desiredValue.getConstraints();
    final String desiredCurrency;
    final Set<String> desiredCurrencies = constraints.getValues(ValuePropertyNames.CURRENCY);
    if (desiredCurrencies != null && !desiredCurrencies.isEmpty()) {
      desiredCurrency = Iterables.getOnlyElement(desiredCurrencies);
    } else {
      desiredCurrency = currency;
    }
    final Period samplingPeriod = getSamplingPeriod(desiredValue.getConstraint(ValuePropertyNames.SAMPLING_PERIOD));
    final LocalDate startDate = now.minus(samplingPeriod);
    final Schedule scheduleCalculator = getScheduleCalculator(desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR));
    final TimeSeriesSamplingFunction samplingFunction = getSamplingFunction(desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION));
    final LocalDate[] schedule = HOLIDAY_REMOVER.getStrippedSchedule(scheduleCalculator.getSchedule(startDate, now, true, false), WEEKEND_CALENDAR);
    final CreditSecurityToIdentifierVisitor identifierVisitor = new CreditSecurityToIdentifierVisitor(OpenGammaExecutionContext.getSecuritySource(executionContext));
    final FinancialSecurity security = (FinancialSecurity) target.getPosition().getSecurity();
    final String spreadCurveName = security.accept(identifierVisitor).getUniqueId().getValue();
    //TODO
    final String curveName = getCurvePrefix() + "_" + spreadCurveName;
    final CurveSpecification curveSpecification = CurveUtils.getCurveSpecification(snapshotClock.instant(), _curveDefinitionSource, _curveSpecificationBuilder, now, curveName);
    DoubleTimeSeries<?> fxSeries = null;
    boolean isInverse = true;
    if (!desiredCurrency.equals(currency)) {
      final Object fxSeriesObject = inputs.getValue(ValueRequirementNames.HISTORICAL_FX_TIME_SERIES);
      if (fxSeriesObject == null) {
        throw new OpenGammaRuntimeException("Could not get historical FX series");
      }
      @SuppressWarnings("unchecked")
      final Map.Entry<UnorderedCurrencyPair, DoubleTimeSeries<?>> entry = Iterables.getOnlyElement(((Map<UnorderedCurrencyPair, DoubleTimeSeries<?>>) fxSeriesObject).entrySet());
      final Object currencyPairObject = inputs.getValue(ValueRequirementNames.CURRENCY_PAIRS);
      if (currencyPairObject == null) {
        throw new OpenGammaRuntimeException("Could not get currency pairs");
      }
      final CurrencyPairs currencyPairs = (CurrencyPairs) currencyPairObject;
      if (desiredCurrency.equals(currencyPairs.getCurrencyPair(Currency.of(desiredCurrency), Currency.of(currency)).getCounter().getCode())) {
        isInverse = false;
      }
      fxSeries = entry.getValue();
    }
    final Object bucketedCS01Object = inputs.getValue(ValueRequirementNames.BUCKETED_CS01);
    if (bucketedCS01Object == null) {
      throw new OpenGammaRuntimeException("Could not get bucketed CS01");
    }
    final LocalDateLabelledMatrix1D bucketedCS01 = (LocalDateLabelledMatrix1D) bucketedCS01Object;
    final Object htsObject = inputs.getValue(ValueRequirementNames.CREDIT_SPREAD_CURVE_HISTORICAL_TIME_SERIES);
    if (htsObject == null) {
      throw new OpenGammaRuntimeException("Could not get credit spread curve historical time series");
    }
    final HistoricalTimeSeriesBundle hts = (HistoricalTimeSeriesBundle) htsObject;
    final NavigableSet<CurveNodeWithIdentifier> nodes = getNodes(now, security, curveSpecification.getNodes());
    DoubleTimeSeries<?> pnlSeries = getPnLSeries(nodes, bucketedCS01, hts, schedule, samplingFunction, fxSeries, isInverse);
    if (pnlSeries == null) {
      throw new OpenGammaRuntimeException("Could not get any values for security " + position.getSecurity());
    }
    pnlSeries = pnlSeries.multiply(position.getQuantity().doubleValue());
    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), desiredValue.getConstraints());
    return Sets.newHashSet(new ComputedValue(resultSpec, pnlSeries));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getPosition().getSecurity();
    return security instanceof StandardCDSSecurity || security instanceof LegacyCDSSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().withAny(CURRENCY).withAny(SAMPLING_PERIOD).withAny(SAMPLING_FUNCTION).withAny(SCHEDULE_CALCULATOR)
        .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.BUCKETED_CS01).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Position position = target.getPosition();
    final FinancialSecurity security = (FinancialSecurity) position.getSecurity();
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> periodNames = constraints.getValues(SAMPLING_PERIOD);
    if (periodNames == null || periodNames.size() != 1) {
      return null;
    }
    final String samplingPeriod = periodNames.iterator().next();
    final Set<String> scheduleNames = constraints.getValues(SCHEDULE_CALCULATOR);
    if (scheduleNames == null || scheduleNames.size() != 1) {
      return null;
    }
    final Set<String> samplingFunctionNames = constraints.getValues(SAMPLING_FUNCTION);
    if (samplingFunctionNames == null || samplingFunctionNames.size() != 1) {
      return null;
    }
    final CreditSecurityToIdentifierVisitor identifierVisitor = new CreditSecurityToIdentifierVisitor(OpenGammaCompilationContext.getSecuritySource(context));
    final String spreadCurveName = security.accept(identifierVisitor).getUniqueId().getValue();
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.add(getBucketedCS01Requirement(security));
    requirements.add(getCreditSpreadCurveHTSRequirement(security, getCurvePrefix() + "_" + spreadCurveName, samplingPeriod));
    final Set<String> resultCurrencies = constraints.getValues(CURRENCY);
    if (resultCurrencies != null && resultCurrencies.size() == 1) {
      final ValueRequirement ccyConversionTSRequirement = getCurrencyConversionTSRequirement(position, currency, resultCurrencies);
      if (ccyConversionTSRequirement != null) {
        requirements.add(ccyConversionTSRequirement);
        requirements.add(new ValueRequirement(ValueRequirementNames.CURRENCY_PAIRS, ComputationTargetSpecification.NULL));
      }
    }
    return requirements;
  }

  protected ValueRequirement getCurrencyConversionTSRequirement(final Position position, final String currencyString, final Set<String> resultCurrencies) {
    final String resultCurrency = Iterables.getOnlyElement(resultCurrencies);
    if (!resultCurrency.equals(currencyString)) {
      final ValueProperties.Builder properties = ValueProperties.builder();
      properties.with(ValuePropertyNames.CURRENCY, resultCurrencies);
      final ComputationTargetSpecification targetSpec = ComputationTargetSpecification.of(position.getSecurity());
      return new ValueRequirement(ValueRequirementNames.HISTORICAL_FX_TIME_SERIES, targetSpec, properties.get());
    }
    return null;
  }

  protected ValueRequirement getBucketedCS01Requirement(final Security security) {
    final ValueProperties properties = ValueProperties.builder().get();
    return new ValueRequirement(ValueRequirementNames.BUCKETED_CS01, ComputationTargetSpecification.of(security), properties);
  }

  protected ValueRequirement getCreditSpreadCurveHTSRequirement(final Security security, final String curveName, final String samplingPeriod) {
    return HistoricalTimeSeriesFunctionUtils.createCreditSpreadCurveHTSRequirement(security, curveName, MarketDataRequirementNames.MARKET_VALUE, null,
        DateConstraint.VALUATION_TIME.minus(samplingPeriod), true, DateConstraint.VALUATION_TIME, true);
  }

  protected String getCurvePrefix() {
    return "SAMEDAY";
  }

  protected NavigableSet<CurveNodeWithIdentifier> getNodes(final LocalDate now, final FinancialSecurity security, final Set<CurveNodeWithIdentifier> allNodes) {
    return new TreeSet<>(allNodes);
  }

  private Period getSamplingPeriod(final String samplingPeriodName) {
    return Period.parse(samplingPeriodName);
  }

  private Schedule getScheduleCalculator(final String scheduleCalculatorName) {
    return ScheduleCalculatorFactory.getScheduleCalculator(scheduleCalculatorName);
  }

  private TimeSeriesSamplingFunction getSamplingFunction(final String samplingFunctionName) {
    return TimeSeriesSamplingFunctionFactory.getFunction(samplingFunctionName);
  }

  private DoubleTimeSeries<?> getPnLSeries(final Set<CurveNodeWithIdentifier> nodes, final LocalDateLabelledMatrix1D bucketedCS01, final HistoricalTimeSeriesBundle htsBundle,
      final LocalDate[] schedule, final TimeSeriesSamplingFunction samplingFunction, final DoubleTimeSeries<?> fxSeries, final boolean isInverse) {
    DoubleTimeSeries<?> pnlSeries = null;
    final int nNodes = nodes.size();
    if (bucketedCS01.size() != nNodes) {
      throw new OpenGammaRuntimeException("Number of nodes in credit spread curve (" + nNodes + ") does not match number of bucketed CS01 values (" + bucketedCS01.size() + ")");
    }
    final double[] cs01 = bucketedCS01.getValues();
    int i = 0;
    for (final CurveNodeWithIdentifier node : nodes) {
      final ExternalIdBundle id = ExternalIdBundle.of(node.getIdentifier());
      final HistoricalTimeSeries hts = htsBundle.get(MarketDataRequirementNames.MARKET_VALUE, id);
      if (hts == null) {
        throw new OpenGammaRuntimeException("Could not get historical time series for " + id);
      }
      if (hts.getTimeSeries().isEmpty()) {
        throw new OpenGammaRuntimeException("Time series for " + id + " is empty");
      }
      DateDoubleTimeSeries<?> nodeTimeSeries = samplingFunction.getSampledTimeSeries(hts.getTimeSeries(), schedule);
      if (fxSeries != null) {
        if (isInverse) {
          nodeTimeSeries = nodeTimeSeries.divide(fxSeries);
        } else {
          nodeTimeSeries = nodeTimeSeries.multiply(fxSeries);
        }
      }
      nodeTimeSeries = DIFFERENCE.evaluate(nodeTimeSeries.multiply(10000));
      final double sensitivity = cs01[i++];
      if (pnlSeries == null) {
        pnlSeries = nodeTimeSeries.multiply(sensitivity);
      } else {
        pnlSeries = pnlSeries.add(nodeTimeSeries.multiply(sensitivity));
      }
    }
    return pnlSeries;
  }
}
