/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.util.money.Currency;

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

  public static ValueRequirement createHTSRequirement(final HistoricalTimeSeriesResolutionResult timeSeries, final String dataField, final DateConstraint startDate, final boolean includeStart,
      final DateConstraint endDate, final boolean includeEnd) {
    final HistoricalTimeSeriesAdjuster adjuster = timeSeries.getAdjuster();
    final String adjustment = (adjuster == null) ? "" : adjuster.getAdjustment(timeSeries.getHistoricalTimeSeriesInfo().getExternalIdBundle().toBundle()).toString();
    return new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES, timeSeries.getHistoricalTimeSeriesInfo().getUniqueId(),
        ValueProperties.builder()
            .with(DATA_FIELD_PROPERTY, dataField)
            .with(ADJUST_PROPERTY, adjustment)
            .with(START_DATE_PROPERTY, startDate.toString())
            .with(INCLUDE_START_PROPERTY, includeStart ? YES_VALUE : NO_VALUE)
            .with(END_DATE_PROPERTY, endDate.toString())
            .with(INCLUDE_END_PROPERTY, includeEnd ? YES_VALUE : NO_VALUE).get());
  }

  public static ValueRequirement createYCHTSRequirement(final Currency currency, final String curveName, final String dataField, final String resolutionKey, final DateConstraint startDate,
      final boolean includeStart, final DateConstraint endDate, final boolean includeEnd) {
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_HISTORICAL_TIME_SERIES, currency,
        ValueProperties.builder()
            .with(ValuePropertyNames.CURVE, curveName)
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
    for (ComputedValue input : inputs.getAllValues()) {
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
