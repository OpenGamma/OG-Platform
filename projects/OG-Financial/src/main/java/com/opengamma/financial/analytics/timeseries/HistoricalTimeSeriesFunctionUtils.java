/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * Helper methods for working with the historical time series functions.
 */
public final class HistoricalTimeSeriesFunctionUtils {

  /**
   * Property describing the "data field" used to resolve the time series for each instrument.
   */
  public static final String DATA_FIELD_PROPERTY = "DataField";

  /**
   * Property describing the "adjuster" that will apply normalization and/or any other rules to the underlying time series.
   */
  public static final String ADJUST_PROPERTY = "Adjust";

  /**
   * Property describing the maximum age of a time-series point.
   */
  public static final String AGE_LIMIT_PROPERTY = "AgeLimit";

  /**
   * Value for {@link #AGE_LIMIT_PROPERTY}.
   */
  public static final String UNLIMITED_AGE_LIMIT_VALUE = "Unlimited";

  /**
   * Property describing the "resolution key" used to resolve the time series for each instrument.
   */
  public static final String RESOLUTION_KEY_PROPERTY = "ResolutionKey";

  /**
   * Property describing the "start date" of the time series value.
   */
  public static final String START_DATE_PROPERTY = "Start";

  /**
   * Property describing whether the start date was included in the time series.
   */
  public static final String INCLUDE_START_PROPERTY = "IncludeStart";

  /**
   * Property describing the "end date" of the time series value.
   */
  public static final String END_DATE_PROPERTY = "End";

  /**
   * Property describing whether the end date was included in the time series.
   */
  public static final String INCLUDE_END_PROPERTY = "IncludeEnd";

  /**
   * Value for {@link #INCLUDE_START_PROPERTY} or {@link #INCLUDE_END_PROPERTY}.
   */
  public static final String YES_VALUE = "Yes";

  /**
   * Value for {@link #INCLUDE_START_PROPERTY} or {@link #INCLUDE_END_PROPERTY}.
   */
  public static final String NO_VALUE = "No";

  private HistoricalTimeSeriesFunctionUtils() {
  }

  public static ValueProperties.Builder htsConstraints(final ValueProperties.Builder properties, final DateConstraint startDate, final boolean includeStart, final DateConstraint endDate,
      final boolean includeEnd) {
    if (startDate != null) {
      properties.with(START_DATE_PROPERTY, startDate.toString()).with(INCLUDE_START_PROPERTY, includeStart ? YES_VALUE : NO_VALUE);
    }
    if (endDate != null) {
      properties.with(END_DATE_PROPERTY, endDate.toString()).with(INCLUDE_END_PROPERTY, includeEnd ? YES_VALUE : NO_VALUE);
    }
    return properties;
  }

  public static ValueRequirement createHTSRequirement(final HistoricalTimeSeriesResolutionResult timeSeries, final String dataField, final ValueProperties constraints) {
    final HistoricalTimeSeriesAdjuster adjuster = timeSeries.getAdjuster();
    final String adjustment = (adjuster == null) ? "" : adjuster.getAdjustment(timeSeries.getHistoricalTimeSeriesInfo().getExternalIdBundle().toBundle()).toString();
    final Builder properties = constraints.copy()
        .with(DATA_FIELD_PROPERTY, dataField)
        .with(ADJUST_PROPERTY, adjustment);
    return new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES, ComputationTargetType.PRIMITIVE, timeSeries.getHistoricalTimeSeriesInfo().getUniqueId(),
        properties.get());
  }

  public static ValueRequirement createHTSRequirement(final HistoricalTimeSeriesResolutionResult timeSeries, final String dataField, final DateConstraint startDate, final boolean includeStart,
      final DateConstraint endDate, final boolean includeEnd) {
    final HistoricalTimeSeriesAdjuster adjuster = timeSeries.getAdjuster();
    final String adjustment = (adjuster == null) ? "" : adjuster.getAdjustment(timeSeries.getHistoricalTimeSeriesInfo().getExternalIdBundle().toBundle()).toString();
    final Builder properties = htsConstraints(ValueProperties.builder(), startDate, includeStart, endDate, includeEnd)
        .with(DATA_FIELD_PROPERTY, dataField)
        .with(ADJUST_PROPERTY, adjustment);
    return new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES, ComputationTargetType.PRIMITIVE, timeSeries.getHistoricalTimeSeriesInfo().getUniqueId(),
        properties.get());
  }

  /** Creates a ValueRequirement for {@link ValueRequirementNames#HISTORICAL_TIME_SERIES_LATEST}.
   *  See {@link HistoricalTimeSeriesLatestSecurityValueFunction} which does the heavy lifting
   *  @param security {@link Security} for which value is required
   *  @param dataField Name of time series value. Example "Close"
   *  @param constraints {@link ValueProperties}
   *  @return The {@link ValueRequirement} "Historical Time Series (latest value)"
   */
  public static ValueRequirement createHTSLatestRequirement(final Security security, final String dataField, final ValueProperties constraints) {
    final Builder properties = (constraints == null ? ValueProperties.none() : constraints).copy().with(DATA_FIELD_PROPERTY, dataField);
    return new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, ComputationTargetSpecification.of(security), properties.get());
  }

  public static ValueRequirement createYCHTSRequirement(final Currency currency, final String curveName, final String dataField, final String resolutionKey, final DateConstraint startDate,
      final boolean includeStart, final DateConstraint endDate, final boolean includeEnd) {
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_HISTORICAL_TIME_SERIES, ComputationTargetType.CURRENCY.specification(currency),
        ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(DATA_FIELD_PROPERTY, dataField)
        .with(RESOLUTION_KEY_PROPERTY, (resolutionKey != null) ? resolutionKey : "")
        .with(START_DATE_PROPERTY, startDate.toString())
        .with(INCLUDE_START_PROPERTY, includeStart ? YES_VALUE : NO_VALUE)
        .with(END_DATE_PROPERTY, endDate.toString())
        .with(INCLUDE_END_PROPERTY, includeEnd ? YES_VALUE : NO_VALUE).get());
  }

  public static ValueRequirement createCreditSpreadCurveHTSRequirement(final Security security, final String curveName, final String dataField, final String resolutionKey,
      final DateConstraint startDate, final boolean includeStart, final DateConstraint endDate, final boolean includeEnd) {
    return new ValueRequirement(ValueRequirementNames.CREDIT_SPREAD_CURVE_HISTORICAL_TIME_SERIES, ComputationTargetSpecification.of(security),
        ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(DATA_FIELD_PROPERTY, dataField)
        .with(RESOLUTION_KEY_PROPERTY, (resolutionKey != null) ? resolutionKey : "")
        .with(START_DATE_PROPERTY, startDate.toString())
        .with(INCLUDE_START_PROPERTY, includeStart ? YES_VALUE : NO_VALUE)
        .with(END_DATE_PROPERTY, endDate.toString())
        .with(INCLUDE_END_PROPERTY, includeEnd ? YES_VALUE : NO_VALUE).get());
  }

  public static ValueRequirement createFXForwardCurveHTSRequirement(final UnorderedCurrencyPair currencyPair, final String curveName, final String dataField, final String resolutionKey,
      final DateConstraint startDate, final boolean includeStart, final DateConstraint endDate, final boolean includeEnd) {
    return new ValueRequirement(ValueRequirementNames.FX_FORWARD_CURVE_HISTORICAL_TIME_SERIES, ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(currencyPair),
        ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(DATA_FIELD_PROPERTY, dataField)
        .with(RESOLUTION_KEY_PROPERTY, (resolutionKey != null) ? resolutionKey : "")
        .with(START_DATE_PROPERTY, startDate.toString())
        .with(INCLUDE_START_PROPERTY, includeStart ? YES_VALUE : NO_VALUE)
        .with(END_DATE_PROPERTY, endDate.toString())
        .with(INCLUDE_END_PROPERTY, includeEnd ? YES_VALUE : NO_VALUE).get());
  }

  public static ValueRequirement createVolatilitySurfaceHTSRequirement(final UnorderedCurrencyPair currencies, final String surfaceName, final String instrumentType, final String dataField,
      final String resolutionKey, final DateConstraint startDate, final boolean includeStart, final DateConstraint endDate, final boolean includeEnd) {
    return new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_HISTORICAL_TIME_SERIES, ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(currencies),
        ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, instrumentType)
        .with(DATA_FIELD_PROPERTY, dataField)
        .with(RESOLUTION_KEY_PROPERTY, (resolutionKey != null) ? resolutionKey : "")
        .with(START_DATE_PROPERTY, startDate.toString())
        .with(INCLUDE_START_PROPERTY, includeStart ? YES_VALUE : NO_VALUE)
        .with(END_DATE_PROPERTY, endDate.toString())
        .with(INCLUDE_END_PROPERTY, includeEnd ? YES_VALUE : NO_VALUE).get());
  }

  /**
   * Reduces any parameters of value name {@link ValueRequirementNames#HISTORICAL_TIME_SERIES} to a single {@link HistoricalTimeSeriesBundle}.
   *
   * @param executionContext the execution context, must contain a {@link HistoricalTimeSeriesSource}
   * @param inputs the function inputs
   * @return the time series bundle, not null
   */
  public static HistoricalTimeSeriesBundle getHistoricalTimeSeriesInputs(final FunctionExecutionContext executionContext, final FunctionInputs inputs) {
    final HistoricalTimeSeriesSource timeSeriesSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final HistoricalTimeSeriesBundle bundle = new HistoricalTimeSeriesBundle();
    for (final ComputedValue input : inputs.getAllValues()) {
      if (ValueRequirementNames.HISTORICAL_TIME_SERIES.equals(input.getSpecification().getValueName())) {
        final String fieldName = input.getSpecification().getProperty(DATA_FIELD_PROPERTY);
        final HistoricalTimeSeries hts = (HistoricalTimeSeries) input.getValue();
        final ExternalIdBundle ids = timeSeriesSource.getExternalIdBundle(hts.getUniqueId());
        if (fieldName == null) {
          // Default to MARKET_VALUE in the bundle; this is not probably correct but this shouldn't happen for well written functions
          bundle.add(MarketDataRequirementNames.MARKET_VALUE, ids, hts);
        } else {
          bundle.add(fieldName, ids, hts);
        }
      }
    }
    return bundle;
  }

  protected static boolean parseBoolean(final String str) {
    return YES_VALUE.equals(str);
  }

}
