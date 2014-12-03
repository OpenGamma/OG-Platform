/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.builders;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.CurrencyMatrixValue;
import com.opengamma.financial.currency.CurrencyMatrixValueVisitor;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.marketdata.FieldName;
import com.opengamma.sesame.marketdata.FxRateId;
import com.opengamma.sesame.marketdata.MarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataId;
import com.opengamma.sesame.marketdata.MarketDataRequirement;
import com.opengamma.sesame.marketdata.MarketDataSource;
import com.opengamma.sesame.marketdata.RawId;
import com.opengamma.sesame.marketdata.SingleValueRequirement;
import com.opengamma.sesame.marketdata.TimeSeriesRequirement;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Function2;
import com.opengamma.util.result.Result;

/**
 *
 */
public class FxRateMarketDataBuilder implements MarketDataBuilder {

  private final CurrencyMatrix _currencyMatrix;

  /**
   * @param currencyMatrix defines how FX rates should be looked up from a market data provider or derived
   *   from other rates
   */
  public FxRateMarketDataBuilder(CurrencyMatrix currencyMatrix) {
    _currencyMatrix = ArgumentChecker.notNull(currencyMatrix, "currencyMatrix");
  }

  @Override
  public Set<MarketDataRequirement> getSingleValueRequirements(SingleValueRequirement requirement,
                                                               ZonedDateTime valuationTime,
                                                               Set<? extends MarketDataRequirement> suppliedData) {
    FxRateId rateId = (FxRateId) requirement.getMarketDataId();
    CurrencyPair currencyPair = rateId.getCurrencyPair();
    // if the supplied data contains the inverse rate we can use that 
    FxRateId inverseRateId = FxRateId.of(currencyPair.inverse());
    MarketDataRequirement inverseRateRequirement =
        SingleValueRequirement.of(inverseRateId, requirement.getMarketDataTime());

    if (suppliedData.contains(inverseRateRequirement)) {
      return ImmutableSet.of();
    }
    Set<RawId<Double>> requiredIds = getRequirements(currencyPair.getBase(), currencyPair.getCounter());
    Set<MarketDataRequirement> requirements = new HashSet<>();

    for (RawId<Double> requiredId : requiredIds) {
      requirements.add(SingleValueRequirement.of(requiredId, requirement.getMarketDataTime()));
    }
    return requirements;
  }

  @Override
  public Set<MarketDataRequirement> getTimeSeriesRequirements(TimeSeriesRequirement requirement,
                                                              Set<MarketDataId<?>> suppliedData) {
    // TODO implement getTimeSeriesRequirements()
    throw new UnsupportedOperationException("getTimeSeriesRequirements not implemented");
  }

  @Override
  public Map<SingleValueRequirement, Result<?>> buildSingleValues(MarketDataBundle marketDataBundle,
                                                                  ZonedDateTime valuationTime,
                                                                  Set<SingleValueRequirement> requirements,
                                                                  MarketDataSource marketDataSource) {
    Map<SingleValueRequirement, Result<?>> results = new HashMap<>();

    for (SingleValueRequirement requirement : requirements) {
      FxRateId rateId = (FxRateId) requirement.getMarketDataId();
      CurrencyPair currencyPair = rateId.getCurrencyPair();
      // if the supplied data contains the inverse rate we can use that
      FxRateId inverseRateId = FxRateId.of(currencyPair.inverse());
      Result<Double> inverseResult = marketDataBundle.get(inverseRateId, Double.class);

      if (inverseResult.isSuccess()) {
        Double inverseRate = inverseResult.getValue();
        results.put(requirement, Result.success(1 / inverseRate));
      } else {
        results.put(requirement, getRate(marketDataBundle, currencyPair.getBase(), currencyPair.getCounter()));
      }
    }
    return results;
  }

  @Override
  public Map<TimeSeriesRequirement, Result<DateTimeSeries<LocalDate, ?>>> buildTimeSeries(
      MarketDataBundle marketDataBundle,
      Set<TimeSeriesRequirement> requirements,
      MarketDataSource marketDataSource) {

    // TODO implement buildTimeSeries()
    throw new UnsupportedOperationException("buildTimeSeries not implemented");
  }

  @Override
  public Class<? extends MarketDataId> getKeyType() {
    return FxRateId.class;
  }

  private Result<Double> getRate(final MarketDataBundle marketDataBundle, final Currency base, final Currency counter) {
    CurrencyMatrixValueVisitor<Result<Double>> visitor = new CurrencyMatrixValueVisitor<Result<Double>>() {
      @Override
      public Result<Double> visitFixed(CurrencyMatrixValue.CurrencyMatrixFixed fixedValue) {
        return Result.success(fixedValue.getFixedValue());
      }

      @SuppressWarnings("unchecked")
      @Override
      public Result<Double> visitValueRequirement(CurrencyMatrixValue.CurrencyMatrixValueRequirement req) {
        ValueRequirement valueRequirement = req.getValueRequirement();
        ExternalIdBundle id = valueRequirement.getTargetReference().getRequirement().getIdentifiers();
        String dataField = valueRequirement.getValueName();
        RawId<Double> marketDataId = RawId.of(id, FieldName.of(dataField));
        Result<Double> result = marketDataBundle.get(marketDataId, Double.class);

        if (result.isSuccess()) {
          Double spotRate = result.getValue();
          return Result.success(req.isReciprocal() ? 1 / spotRate : spotRate);
        } else {
          return Result.failure(result);
        }
      }

      @Override
      public Result<Double> visitCross(CurrencyMatrixValue.CurrencyMatrixCross cross) {
        Result<Double> baseCrossRate = getRate(marketDataBundle, base, cross.getCrossCurrency());
        Result<Double> crossCounterRate = getRate(marketDataBundle, cross.getCrossCurrency(), counter);

        return baseCrossRate.combineWith(crossCounterRate, new Function2<Double, Double, Result<Double>>() {
          @Override
          public Result<Double> apply(Double rate1, Double rate2) {
            return Result.success(rate1 * rate2);
          }
        });
      }
    };
    CurrencyMatrixValue value = _currencyMatrix.getConversion(base, counter);

    if (value == null) {
      return Result.failure(FailureStatus.MISSING_DATA,
                            "No conversion available for {}",
                            CurrencyPair.of(base, counter));
    }
    return value.accept(visitor);
  }
  
  private Set<RawId<Double>> getRequirements(final Currency base, final Currency counter) {
    CurrencyMatrixValueVisitor<Set<RawId<Double>>> visitor = new CurrencyMatrixValueVisitor<Set<RawId<Double>>>() {
      @Override
      public Set<RawId<Double>> visitFixed(CurrencyMatrixValue.CurrencyMatrixFixed fixedValue) {
        // if the rate is fixed there's no market data required
        return ImmutableSet.of();
      }

      @SuppressWarnings("unchecked")
      @Override
      public Set<RawId<Double>> visitValueRequirement(CurrencyMatrixValue.CurrencyMatrixValueRequirement req) {
        ValueRequirement valueRequirement = req.getValueRequirement();
        ExternalIdBundle id = valueRequirement.getTargetReference().getRequirement().getIdentifiers();
        String dataField = valueRequirement.getValueName();
        RawId<Double> marketDataId = RawId.of(id, FieldName.of(dataField));
        return ImmutableSet.of(marketDataId);
      }

      @Override
      public Set<RawId<Double>> visitCross(CurrencyMatrixValue.CurrencyMatrixCross cross) {
        return
            ImmutableSet.<RawId<Double>>builder()
                .addAll(getRequirements(base, cross.getCrossCurrency()))
                .addAll(getRequirements(cross.getCrossCurrency(), counter))
                .build();
      }
    };
    CurrencyMatrixValue value = _currencyMatrix.getConversion(base, counter);
    return value.accept(visitor);
  }

}
