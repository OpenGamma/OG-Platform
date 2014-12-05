/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import org.threeten.bp.LocalDate;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.PointsCurveNodeWithIdentifier;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.CurrencyMatrixValue;
import com.opengamma.financial.currency.CurrencyMatrixValueVisitor;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.Environment;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeriesBuilder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;
import com.opengamma.util.time.LocalDateRange;

/**
 * Default implementation of {@link HistoricalMarketDataFn}.
 */
public class DefaultHistoricalMarketDataFn implements HistoricalMarketDataFn {

  private final CurrencyMatrix _currencyMatrix;

  public DefaultHistoricalMarketDataFn(CurrencyMatrix currencyMatrix) {
    _currencyMatrix = ArgumentChecker.notNull(currencyMatrix, "currencyMatrix");
  }

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
  public Result<LocalDateDoubleTimeSeries> getMarketValues(Environment env, ExternalIdBundle id, LocalDateRange dateRange) {
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
  public Result<LocalDateDoubleTimeSeries> getFxRates(Environment env, CurrencyPair currencyPair, LocalDateRange dateRange) {
    return getFxRates(env, dateRange, currencyPair.getBase(), currencyPair.getCounter());
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

  private Result<LocalDateDoubleTimeSeries> getFxRates(final Environment env,
                                                       final LocalDateRange dateRange,
                                                       final Currency base,
                                                       final Currency counter) {
    CurrencyMatrixValue value = _currencyMatrix.getConversion(base, counter);
    if (value == null) {
      return Result.failure(FailureStatus.MISSING_DATA,
                                     "No conversion found for {}",
                                     CurrencyPair.of(base, counter));
    }
    CurrencyMatrixValueVisitor<Result<LocalDateDoubleTimeSeries>> visitor =
        new CurrencyMatrixValueVisitor<Result<LocalDateDoubleTimeSeries>>() {

      @Override
      public Result<LocalDateDoubleTimeSeries> visitFixed(CurrencyMatrixValue.CurrencyMatrixFixed fixedValue) {
        LocalDateDoubleTimeSeriesBuilder builder = ImmutableLocalDateDoubleTimeSeries.builder();
        LocalDate start = dateRange.getStartDateInclusive();
        LocalDate end = dateRange.getEndDateInclusive();
        double fixedRate = fixedValue.getFixedValue();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
          builder.put(date, fixedRate);
        }
        return Result.success(builder.build());
      }

      @Override
      public Result<LocalDateDoubleTimeSeries> visitValueRequirement(
          CurrencyMatrixValue.CurrencyMatrixValueRequirement req) {

        ValueRequirement valueRequirement = req.getValueRequirement();
        ExternalIdBundle idBundle = valueRequirement.getTargetReference().getRequirement().getIdentifiers();
        String dataField = valueRequirement.getValueName();
        Result<LocalDateDoubleTimeSeries> result = get(env, idBundle, FieldName.of(dataField), dateRange);

        if (!result.isSuccess()) {
          return result;
        }
        LocalDateDoubleTimeSeries spotRate = result.getValue();
        return Result.success(req.isReciprocal() ? spotRate.reciprocal() : spotRate);
      }

      @Override
      public Result<LocalDateDoubleTimeSeries> visitCross(CurrencyMatrixValue.CurrencyMatrixCross cross) {
        Result<LocalDateDoubleTimeSeries> baseCrossRate = getFxRates(env, dateRange, base, cross.getCrossCurrency());
        Result<LocalDateDoubleTimeSeries> crossCounterRate = getFxRates(env, dateRange, cross.getCrossCurrency(), counter);

        if (Result.anyFailures(baseCrossRate, crossCounterRate)) {
          return Result.failure(baseCrossRate, crossCounterRate);
        } else {
          LocalDateDoubleTimeSeries rate1 = baseCrossRate.getValue();
          LocalDateDoubleTimeSeries rate2 = crossCounterRate.getValue();
          return Result.success(rate1.multiply(rate2));
        }
      }
    };
    return value.accept(visitor);
  }
}
