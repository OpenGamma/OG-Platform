/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.id.UniqueId;
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

  public static ValueRequirement createHTSRequirement(final UniqueId timeSeries, final DateConstraint startDate, final boolean includeStart, final DateConstraint endDate, final boolean includeEnd) {
    return new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES, timeSeries, createHTSRequirementProperties(startDate, includeStart, endDate, includeEnd).get());
  }

  public static ValueProperties.Builder createHTSRequirementProperties(final DateConstraint startDate, final boolean includeStart, final DateConstraint endDate, final boolean includeEnd) {
    return ValueProperties.builder()
        .with(START_DATE_PROPERTY, startDate.toString())
        .with(INCLUDE_START_PROPERTY, includeStart ? YES_VALUE : NO_VALUE)
        .with(END_DATE_PROPERTY, endDate.toString())
        .with(INCLUDE_END_PROPERTY, includeEnd ? YES_VALUE : NO_VALUE);
  }

  public static ValueRequirement createYCHTSRequirement(final Currency currency, final String curveName, final String dataField, final String resolutionKey, final DateConstraint startDate,
      final boolean includeStart, final DateConstraint endDate, final boolean includeEnd) {
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_HISTORICAL_TIME_SERIES, currency, createYCHTSRequirementProperties(curveName, dataField, resolutionKey, startDate, includeStart,
        endDate, includeEnd).get());
  }

  public static ValueProperties.Builder createYCHTSRequirementProperties(final String curveName, final String dataField, final String resolutionKey, final DateConstraint startDate,
      final boolean includeStart, final DateConstraint endDate, final boolean includeEnd) {
    return ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY, dataField)
        .with(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY, (resolutionKey != null) ? resolutionKey : "")
        .with(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY, startDate.toString())
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY, includeStart ? HistoricalTimeSeriesFunctionUtils.YES_VALUE : HistoricalTimeSeriesFunctionUtils.NO_VALUE)
        .with(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY, endDate.toString())
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, includeEnd ? HistoricalTimeSeriesFunctionUtils.YES_VALUE : HistoricalTimeSeriesFunctionUtils.NO_VALUE);
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
        final HistoricalTimeSeries hts = (HistoricalTimeSeries) input.getValue();
        bundle.add(timeSeriesSource.getExternalIdBundle(hts.getUniqueId()), hts);
      }
    }
    return bundle;
  }

  protected static boolean parseBoolean(final String str) {
    return YES_VALUE.equals(str);
  }

}
