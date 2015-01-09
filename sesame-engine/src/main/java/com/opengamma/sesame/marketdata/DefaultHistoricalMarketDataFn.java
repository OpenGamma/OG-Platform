/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.PointsCurveNodeWithIdentifier;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.Environment;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;
import com.opengamma.util.time.LocalDateRange;

/**
 * Default implementation of {@link HistoricalMarketDataFn}.
 */
public class DefaultHistoricalMarketDataFn implements HistoricalMarketDataFn {

  @Override
  public Result<LocalDateDoubleTimeSeries> getCurveNodeValues(Environment env,
                                                              CurveNodeWithIdentifier node,
                                                              LocalDateRange dateRange) {
    ExternalIdBundle id = node.getIdentifier().toBundle();
    FieldName fieldName = FieldName.of(node.getDataField());
    return get(env, id, fieldName, dateRange);
  }

  @Override
  public Result<LocalDateDoubleTimeSeries> getCurveNodeUnderlyingValue(Environment env,
                                                                       PointsCurveNodeWithIdentifier node,
                                                                       LocalDateRange dateRange) {
    ExternalIdBundle id = node.getUnderlyingIdentifier().toBundle();
    FieldName fieldName = FieldName.of(node.getUnderlyingDataField());
    return get(env, id, fieldName, dateRange);
  }

  @Override
  public Result<LocalDateDoubleTimeSeries> getMarketValues(Environment env,
                                                           ExternalIdBundle id,
                                                           LocalDateRange dateRange) {
    return get(env, id, MarketDataUtils.MARKET_VALUE, dateRange);
  }

  @Override
  public Result<LocalDateDoubleTimeSeries> getValues(Environment env,
                                                     ExternalIdBundle id,
                                                     FieldName fieldName,
                                                     LocalDateRange dateRange) {
    return get(env, id, fieldName, dateRange);
  }

  @Override
  public Result<LocalDateDoubleTimeSeries> getFxRates(Environment env,
                                                      CurrencyPair currencyPair,
                                                      LocalDateRange dateRange) {
    FxRateId rateId = FxRateId.of(currencyPair.getBase(), currencyPair.getCounter());
    Result<DateTimeSeries<LocalDate, Double>> result = env.getMarketDataBundle().get(rateId, Double.class, dateRange);

    if (!result.isSuccess()) {
      return Result.failure(result);
    } else {
      return Result.success(MarketDataUtils.asLocalDateDoubleTimeSeries(result.getValue()));
    }
  }

  private Result<LocalDateDoubleTimeSeries> get(Environment env,
                                                ExternalIdBundle id,
                                                FieldName fieldName,
                                                LocalDateRange dateRange) {
    RawId<Double> key = RawId.of(id, fieldName);
    Result<DateTimeSeries<LocalDate, Double>> timeSeriesResult =
        env.getMarketDataBundle().get(key, Double.class, dateRange);

    if (!timeSeriesResult.isSuccess()) {
      return Result.failure(timeSeriesResult);
    }
    LocalDateDoubleTimeSeries timeSeries = MarketDataUtils.asLocalDateDoubleTimeSeries(timeSeriesResult.getValue());

    if (timeSeries.isEmpty()) {
      return Result.failure(FailureStatus.MISSING_DATA, "Empty time series found for {}/{}", id, fieldName);
    } else {
      return Result.success(timeSeries);
    }
  }
}
