/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

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
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.sensitivities.FactorExposureData;
import com.opengamma.financial.sensitivities.RawSecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ExternallyProvidedSensitivityPnLFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(ExternallyProvidedSensitivityPnLFunction.class);

  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();

  private static final LocalDate MAGIC_DATE = LocalDate.of(2009, 06, 05);

  private final String _resolutionKey;

  public ExternallyProvidedSensitivityPnLFunction(final String resolutionKey) {
    ArgumentChecker.notNull(resolutionKey, "resolution key");
    _resolutionKey = resolutionKey;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final ComputationTargetSpecification positionSpec = target.toSpecification();
    final RawSecurity security = (RawSecurity) position.getSecurity();
    final SecuritySource secSource = executionContext.getSecuritySource();
    //final Clock snapshotClock = executionContext.getValuationClock();
    final LocalDate now = MAGIC_DATE; //ZonedDateTime.now(snapshotClock).getDate();
    final Currency currency = FinancialSecurityUtils.getCurrency(position.getSecurity());
    final String currencyString = currency.getCode();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueProperties constraints = desiredValue.getConstraints();
    final Period samplingPeriod = getSamplingPeriod(constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD));
    final LocalDate startDate = now.minus(samplingPeriod);
    final Schedule scheduleCalculator = getScheduleCalculator(constraints.getValues(ValuePropertyNames.SCHEDULE_CALCULATOR));
    final TimeSeriesSamplingFunction samplingFunction = getSamplingFunction(constraints.getValues(ValuePropertyNames.SAMPLING_FUNCTION));
    final LocalDate[] schedule = HOLIDAY_REMOVER.getStrippedSchedule(scheduleCalculator.getSchedule(startDate, now, true, false), WEEKEND_CALENDAR); //REVIEW emcleod should "fromEnd" be hard-coded?
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    DoubleTimeSeries<?> result = getPnLSeries(security, secSource, timeSeries, inputs, startDate, now, schedule, samplingFunction);
    result = result.multiply(position.getQuantity().doubleValue());
    final ValueProperties resultProperties = getResultProperties(desiredValue, currencyString);
    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.PNL_SERIES, positionSpec, resultProperties);
    return Sets.newHashSet(new ComputedValue(resultSpec, result));
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getPosition().getSecurity();
    return RawSecurityUtils.isExternallyProvidedSensitivitiesSecurity(security);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final RawSecurity security = (RawSecurity) target.getPosition().getSecurity();
    final String samplingPeriod = desiredValue.getConstraint(ValuePropertyNames.SAMPLING_PERIOD);
    final Set<ValueRequirement> sensitivityRequirements = getSensitivityRequirements(OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context), context.getSecuritySource(), security,
        samplingPeriod);
    return sensitivityRequirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Position position = target.getPosition();
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(position.getSecurity()).getCode())
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  private ValueProperties getResultProperties(final ValueRequirement desiredValue, final String currency) {
    return createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.SAMPLING_PERIOD, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_PERIOD))
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR))
        .with(ValuePropertyNames.SAMPLING_FUNCTION, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION))
        .get();
  }

  private String getPropertyName(final Set<String> propertyName) {
    if (propertyName == null || propertyName.isEmpty() || propertyName.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique property name: " + propertyName);
    }
    return propertyName.iterator().next();
  }

  private Period getSamplingPeriod(final Set<String> samplingPeriodNames) {
    final String samplingPeriodName = getPropertyName(samplingPeriodNames);
    return Period.parse(samplingPeriodName);
  }

  private Schedule getScheduleCalculator(final Set<String> scheduleCalculatorNames) {
    final String scheduleCalculatorName = getPropertyName(scheduleCalculatorNames);
    return ScheduleCalculatorFactory.getScheduleCalculator(scheduleCalculatorName);
  }

  private TimeSeriesSamplingFunction getSamplingFunction(final Set<String> samplingFunctionNames) {
    final String samplingFunctionName = getPropertyName(samplingFunctionNames);
    return TimeSeriesSamplingFunctionFactory.getFunction(samplingFunctionName);
  }

  private DoubleTimeSeries<?> getPnLSeries(final RawSecurity security, final SecuritySource secSource, final HistoricalTimeSeriesBundle timeSeries,
      final FunctionInputs inputs, final LocalDate startDate, final LocalDate now, final LocalDate[] schedule, final TimeSeriesSamplingFunction samplingFunction) {
    DoubleTimeSeries<?> pnlSeries = null;
    final List<FactorExposureData> factors = RawSecurityUtils.decodeFactorExposureData(secSource, security);
    for (final FactorExposureData factor : factors) {
      final HistoricalTimeSeries dbNodeTimeSeries = timeSeries.get("PX_LAST", factor.getFactorExternalId());
      if (dbNodeTimeSeries == null || dbNodeTimeSeries.getTimeSeries().size() == 0) {
        //s_logger.warn("Could not identifier / price series pair for " + id + " for " + _resolutionKey + "/PX_LAST");
        //throw new OpenGammaRuntimeException("Could not identifier / price series pair for " + id + " for " + _resolutionKey + "/PX_LAST");
        continue;
      }
      DateDoubleTimeSeries<?> nodeTimeSeries = samplingFunction.getSampledTimeSeries(dbNodeTimeSeries.getTimeSeries(), schedule);
      nodeTimeSeries = DIFFERENCE.evaluate(nodeTimeSeries);
      final Double sensitivity = (Double) inputs.getValue(getSensitivityRequirement(factor.getExposureExternalId()));
      if (sensitivity != null) {
        if (pnlSeries == null) {
          pnlSeries = nodeTimeSeries.multiply(sensitivity / 100d);
        } else {
          pnlSeries = pnlSeries.add(nodeTimeSeries.multiply(sensitivity / 100d));
        }
      } else {
        s_logger.warn("Could not get sensitivity " + factor.getExposureExternalId());
      }
    }
    if (pnlSeries == null) {
      final List<LocalDate> dates = new ArrayList<LocalDate>();
      final List<Double> values = new ArrayList<Double>();
      dates.add(MAGIC_DATE.minusDays(7));
      dates.add(MAGIC_DATE);
      values.add(0d);
      values.add(0d);
      pnlSeries = ImmutableLocalDateDoubleTimeSeries.of(dates, values);
    }
    return pnlSeries;
  }

  //  private double getNormalizationFactor(final StripInstrumentType type) {
  //    switch(type) {
  //      case TENOR_SWAP:
  //        return 10000.;
  //      case BASIS_SWAP:
  //        return 10000.;
  //      case OIS_SWAP:
  //        return 10000.;
  //      default:
  //        return 100.;
  //    }
  //  }

  protected Set<ValueRequirement> getSensitivityRequirements(final HistoricalTimeSeriesResolver resolver, final SecuritySource secSource, final RawSecurity rawSecurity, final String samplingPeriod) {
    final Set<ValueRequirement> requirements = Sets.newHashSet();
    final Collection<FactorExposureData> decodedSensitivities = RawSecurityUtils.decodeFactorExposureData(secSource, rawSecurity);
    for (final FactorExposureData exposureEntry : decodedSensitivities) {
      requirements.add(getSensitivityRequirement(exposureEntry.getExposureExternalId()));
      requirements.add(getTimeSeriesRequirement(resolver, exposureEntry.getFactorExternalId().toBundle(), samplingPeriod));
    }
    return requirements;
  }

  protected ValueRequirement getSensitivityRequirement(final ExternalId externalId) {
    return new ValueRequirement(/*ExternalDataRequirementNames.SENSITIVITY*/"EXPOSURE", ComputationTargetType.PRIMITIVE, externalId);
  }

  protected ValueRequirement getTimeSeriesRequirement(final HistoricalTimeSeriesResolver resolver, final ExternalIdBundle bundle, final String samplingPeriod) {
    return HistoricalTimeSeriesFunctionUtils.createHTSRequirement(resolver.resolve(bundle, null, null, null, "PX_LAST", _resolutionKey), "PX_LAST",
        DateConstraint.of(MAGIC_DATE.minus(Period.parse(samplingPeriod))), true, DateConstraint.of(MAGIC_DATE), true);
  }

}
