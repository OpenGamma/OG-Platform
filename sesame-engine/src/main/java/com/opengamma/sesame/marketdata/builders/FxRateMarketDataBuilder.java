/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.builders;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.core.link.ConfigLink;
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
import com.opengamma.sesame.marketdata.MarketDataTime;
import com.opengamma.sesame.marketdata.RawId;
import com.opengamma.sesame.marketdata.SingleValueRequirement;
import com.opengamma.sesame.marketdata.TimeSeriesRequirement;
import com.opengamma.sesame.marketdata.scenarios.CyclePerturbations;
import com.opengamma.sesame.marketdata.scenarios.FilteredPerturbation;
import com.opengamma.timeseries.date.DateEntryIterator;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeriesBuilder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Function2;
import com.opengamma.util.result.Result;
import com.opengamma.util.time.LocalDateRange;

/**
 * Creates market data for the FX rates for currency pairs.
 * <p>
 * FX rates are requested using {@link FxRateId} instances which specify the currency pair. This builder
 * hides the tickers used for looking up the rates from a market data provider and derives cross rates
 * from directly quoted rates.
 * <p>
 * The underlying market data tickers and cross rate configuration comes from a {@link CurrencyMatrix}.
 */
public class FxRateMarketDataBuilder implements MarketDataBuilder {

  private static final Logger s_logger = LoggerFactory.getLogger(FxRateMarketDataBuilder.class);

  private final ConfigLink<CurrencyMatrix> _currencyMatrixLink;

  /**
   * @param currencyMatrixLink defines how FX rates should be looked up from a market data provider or derived
   *   from other rates
   */
  public FxRateMarketDataBuilder(ConfigLink<CurrencyMatrix> currencyMatrixLink) {
    _currencyMatrixLink = ArgumentChecker.notNull(currencyMatrixLink, "currencyMatrix");
  }

  @Override
  public Set<MarketDataRequirement> getSingleValueRequirements(SingleValueRequirement requirement,
                                                               ZonedDateTime valuationTime,
                                                               Set<? extends MarketDataRequirement> suppliedData) {
    FxRateId rateId = (FxRateId) requirement.getMarketDataId();
    CurrencyPair currencyPair = rateId.getCurrencyPair();
    // If the supplied data already contains the rate then it won't be in the requirements at all.
    // If the supplied data contains the inverse rate we can use that instead of looking up the market rate
    FxRateId inverseRateId = FxRateId.of(currencyPair.inverse());
    MarketDataRequirement inverseRateRequirement =
        SingleValueRequirement.of(inverseRateId, requirement.getMarketDataTime());

    if (suppliedData.contains(inverseRateRequirement)) {
      // don't need to return any requirements because the inverse rate is in the supplied data and we can use
      // that to calculate the rate we need
      return ImmutableSet.of();
    }
    // TODO Java 8 - replace with a lambda
    Function2<MarketDataId<?>, MarketDataTime, MarketDataRequirement> requirementBuilder =
        new Function2<MarketDataId<?>, MarketDataTime, MarketDataRequirement>() {

          @Override
          public MarketDataRequirement apply(MarketDataId<?> marketDataId, MarketDataTime time) {
            return SingleValueRequirement.of(marketDataId, time);
          }
    };
    Set<MarketDataRequirement> requirements = getRequirements(currencyPair.getBase(),
                                                              currencyPair.getCounter(),
                                                              requirement.getMarketDataTime(),
                                                              requirementBuilder);
    s_logger.debug("Returning requirements {} for single value requirement {}", requirements, requirement);
    return requirements;
  }

  @Override
  public Set<MarketDataRequirement> getTimeSeriesRequirements(
      TimeSeriesRequirement requirement,
      Map<MarketDataId<?>, DateTimeSeries<LocalDate, ?>> suppliedData) {

    FxRateId rateId = (FxRateId) requirement.getMarketDataId();
    CurrencyPair currencyPair = rateId.getCurrencyPair();
    // If the supplied data already contains the rate then it won't be in the requirements at all.
    // If the supplied data contains the inverse rate we can use that instead of looking up the market rate
    FxRateId inverseRateId = FxRateId.of(currencyPair.inverse());
    MarketDataTime marketDataTime = requirement.getMarketDataTime();
    LocalDateRange dateRange = marketDataTime.getDateRange();
    DateTimeSeries<LocalDate, ?> timeSeries = suppliedData.get(inverseRateId);

    if (timeSeries != null &&
        !dateRange.getStartDateInclusive().isBefore(timeSeries.getEarliestTime()) &&
        !dateRange.getEndDateInclusive().isAfter(timeSeries.getLatestTime())) {
      // don't need to return any requirements because the inverse rate is in the supplied data and we can use
      // that to calculate the rate we need
      return ImmutableSet.of();
    }
    // TODO Java 8 - replace with a lambda
    Function2<MarketDataId<?>, MarketDataTime, MarketDataRequirement> requirementBuilder =
        new Function2<MarketDataId<?>, MarketDataTime, MarketDataRequirement>() {

          @Override
          public MarketDataRequirement apply(MarketDataId<?> marketDataId, MarketDataTime time) {
            return TimeSeriesRequirement.of(marketDataId, time.getDateRange());
          }
        };
    Set<MarketDataRequirement> requirements = getRequirements(currencyPair.getBase(),
                                                              currencyPair.getCounter(),
                                                              requirement.getMarketDataTime(),
                                                              requirementBuilder);
    s_logger.debug("Returning requirements {} for time series requirement {}", requirements, requirement);
    return requirements;
  }

  @Override
  public Map<SingleValueRequirement, Result<?>> buildSingleValues(MarketDataBundle marketDataBundle,
                                                                  ZonedDateTime valuationTime,
                                                                  Set<SingleValueRequirement> requirements,
                                                                  MarketDataSource marketDataSource,
                                                                  CyclePerturbations cyclePerturbations) {
    ImmutableMap.Builder<SingleValueRequirement, Result<?>> resultsBuilder = ImmutableMap.builder();

    for (SingleValueRequirement requirement : requirements) {
      // there will always be zero or one perturbations for an FX rate requirement
      Collection<FilteredPerturbation> perturbations = cyclePerturbations.getInputPerturbations().get(requirement);
      Optional<FilteredPerturbation> perturbation;

      if (perturbations.isEmpty()) {
        perturbation = Optional.absent();
      } else {
        perturbation = Optional.of(perturbations.iterator().next());
      }
      FxRateId rateId = (FxRateId) requirement.getMarketDataId();
      CurrencyPair currencyPair = rateId.getCurrencyPair();
      // if the supplied data contains the inverse rate we can use that
      FxRateId inverseRateId = FxRateId.of(currencyPair.inverse());
      Result<Double> inverseResult = marketDataBundle.get(inverseRateId, Double.class);

      if (inverseResult.isSuccess()) {
        Double inverseRate = inverseResult.getValue();
        resultsBuilder.put(requirement, Result.success(1 / inverseRate));
      } else {
        Result<Double> rate = getRate(marketDataBundle, currencyPair.getBase(), currencyPair.getCounter(), perturbation);
        resultsBuilder.put(requirement, rate);
      }
    }
    Map<SingleValueRequirement, Result<?>> results = resultsBuilder.build();
    s_logger.debug("Returning results {} from buildSingleValues", results);
    return results;
  }

  @Override
  public Map<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> buildTimeSeries(
      MarketDataBundle marketDataBundle,
      Set<TimeSeriesRequirement> requirements,
      MarketDataSource marketDataSource,
      CyclePerturbations cyclePerturbations) {

    ImmutableMap.Builder<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> resultsBuilder =
        ImmutableMap.builder();

    for (TimeSeriesRequirement requirement : requirements) {
      FxRateId rateId = (FxRateId) requirement.getMarketDataId();
      CurrencyPair currencyPair = rateId.getCurrencyPair();
      // if the supplied data contains the inverse rate we can use that
      FxRateId inverseRateId = FxRateId.of(currencyPair.inverse());
      LocalDateRange dateRange = requirement.getMarketDataTime().getDateRange();
      Result<DateTimeSeries<LocalDate, Double>> inverseResult =
          marketDataBundle.get(inverseRateId, Double.class, dateRange);

      if (inverseResult.isSuccess()) {
        DateTimeSeries<LocalDate, Double> inverseSeries = inverseResult.getValue();
        DateTimeSeries<LocalDate, Double> series = reciprocal(inverseSeries);
        Result<DateTimeSeries<LocalDate, ?>> result = Result.<DateTimeSeries<LocalDate, ?>>success(series);
        resultsBuilder.put(requirement, result);
      } else {
        Result<? extends DateTimeSeries<LocalDate, ?>> seriesResult =
            getRate(marketDataBundle, currencyPair.getBase(), currencyPair.getCounter(), dateRange);
        resultsBuilder.put(requirement, seriesResult);
      }
    }
    Map<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> results = resultsBuilder.build();
    s_logger.debug("Returning results {} from buildTimeSeries", results);
    return results;
  }

  private DateTimeSeries<LocalDate, Double> reciprocal(DateTimeSeries<LocalDate, Double> series) {
    if (series instanceof LocalDateDoubleTimeSeries) {
      return ((LocalDateDoubleTimeSeries) series).reciprocal();
    } else {
      // this shouldn't ever happen
      LocalDateDoubleTimeSeriesBuilder builder = ImmutableLocalDateDoubleTimeSeries.builder();
      DateEntryIterator<LocalDate, Double> itr = series.iterator();

      while (itr.hasNext()) {
        builder.put(itr.nextTimeFast(), 1 / itr.currentValue());
      }
      return builder.build();
    }
  }

  @Override
  public Class<? extends MarketDataId> getKeyType() {
    return FxRateId.class;
  }

  // TODO Java 8 - use JDK optional and lambdas for applying the perturbation

  /**
   * Returns the FX rate for a pair of currencies, using the currency matrix to determine how to find the data.
   * An optional perturbation is applied to the rate before returning it.
   * <p>
   * If the rate is derived from two other rates (a cross rate) and a perturbation is provided, the result will be
   * a failure. Applying perturbations to cross rates isn't supported as it introduces inconsistencies in the
   * set of market data.
   *
   * @param marketDataBundle market data for the calculation cycle
   * @param base base currency of the pair
   * @param counter counter currency of the pair
   * @param perturbation perturbation to apply to the rate
   * @return a result containing the rate
   */
  private Result<Double> getRate(
      final MarketDataBundle marketDataBundle,
      final Currency base,
      final Currency counter,
      final Optional<FilteredPerturbation> perturbation) {

    CurrencyMatrixValueVisitor<Result<Double>> visitor = new CurrencyMatrixValueVisitor<Result<Double>>() {
      @Override
      public Result<Double> visitFixed(CurrencyMatrixValue.CurrencyMatrixFixed fixedValue) {
        double fixedRate = fixedValue.getFixedValue();

        if (perturbation.isPresent()) {
          return Result.success(((Double) perturbation.get().apply(fixedRate)));
        } else {
          return Result.success(fixedRate);
        }
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
          double rate = req.isReciprocal() ? 1 / spotRate : spotRate;

          if (perturbation.isPresent()) {
            return Result.success(((Double) perturbation.get().apply(rate)));
          } else {
            return Result.success(rate);
          }
        } else {
          return Result.failure(result);
        }
      }

      @Override
      public Result<Double> visitCross(CurrencyMatrixValue.CurrencyMatrixCross cross) {
        // perturbations aren't allowed for cross rates because they introduce inconsistencies
        if (perturbation.isPresent()) {
          return Result.failure(
              FailureStatus.INVALID_INPUT,
              "Perturbation defined for cross rate {}/{}. Perturbations are not supported for cross rates",
              base.getCode(),
              counter.getCode());
        }
        Result<Double> baseCrossRate = getRate(marketDataBundle, base, cross.getCrossCurrency(), perturbation);
        Result<Double> crossCounterRate = getRate(marketDataBundle, cross.getCrossCurrency(), counter, perturbation);

        // TODO Java 8 - replace with lambda
        return baseCrossRate.combineWith(crossCounterRate, new Function2<Double, Double, Result<Double>>() {
          @Override
          public Result<Double> apply(Double rate1, Double rate2) {
            return Result.success(rate1 * rate2);
          }
        });
      }
    };
    CurrencyMatrixValue value = _currencyMatrixLink.resolve().getConversion(base, counter);

    if (value == null) {
      return Result.failure(FailureStatus.MISSING_DATA,
                            "No conversion available for {}",
                            CurrencyPair.of(base, counter));
    }
    return value.accept(visitor);
  }

  private Result<DateTimeSeries<LocalDate, Double>> getRate(final MarketDataBundle marketDataBundle,
                                                            final Currency base,
                                                            final Currency counter,
                                                            final LocalDateRange dateRange) {

    CurrencyMatrixValueVisitor<Result<DateTimeSeries<LocalDate, Double>>> visitor =
        new CurrencyMatrixValueVisitor<Result<DateTimeSeries<LocalDate, Double>>>() {

      @Override
      public Result<DateTimeSeries<LocalDate, Double>> visitFixed(CurrencyMatrixValue.CurrencyMatrixFixed fixedValue) {
        LocalDateDoubleTimeSeriesBuilder builder = ImmutableLocalDateDoubleTimeSeries.builder();

        for (LocalDate localDate : dateRange) {
          builder.put(localDate, fixedValue.getFixedValue());
        }
        return Result.<DateTimeSeries<LocalDate, Double>>success(builder.build());
      }

      @SuppressWarnings("unchecked")
      @Override
      public Result<DateTimeSeries<LocalDate, Double>> visitValueRequirement(
          CurrencyMatrixValue.CurrencyMatrixValueRequirement req) {

        ValueRequirement valueRequirement = req.getValueRequirement();
        ExternalIdBundle id = valueRequirement.getTargetReference().getRequirement().getIdentifiers();
        String dataField = valueRequirement.getValueName();
        RawId<Double> marketDataId = RawId.of(id, FieldName.of(dataField));
        Result<DateTimeSeries<LocalDate, Double>> result = marketDataBundle.get(marketDataId, Double.class, dateRange);

        if (result.isSuccess()) {
          DateTimeSeries<LocalDate, Double> series = result.getValue();
          return Result.success(req.isReciprocal() ? reciprocal(series) : series);
        } else {
          return Result.failure(result);
        }
      }

      @Override
      public Result<DateTimeSeries<LocalDate, Double>> visitCross(CurrencyMatrixValue.CurrencyMatrixCross cross) {
        Result<DateTimeSeries<LocalDate, Double>> baseCrossSeries =
            getRate(marketDataBundle, base, cross.getCrossCurrency(), dateRange);
        Result<DateTimeSeries<LocalDate, Double>> crossCounterSeries =
            getRate(marketDataBundle, cross.getCrossCurrency(), counter, dateRange);

        // TODO Java 8 - use Result.combineWith and a lambda
        if (baseCrossSeries.isSuccess() && crossCounterSeries.isSuccess()) {
          DateTimeSeries<LocalDate, Double> series = multiply(baseCrossSeries.getValue(), crossCounterSeries.getValue());
          return Result.success(series);
        } else {
          return Result.failure(baseCrossSeries, crossCounterSeries);
        }
      }
    };
    CurrencyMatrixValue value = _currencyMatrixLink.resolve().getConversion(base, counter);

    if (value == null) {
      return Result.failure(FailureStatus.MISSING_DATA,
                            "No conversion available for {}",
                            CurrencyPair.of(base, counter));
    }
    return value.accept(visitor);
  }

  private DateTimeSeries<LocalDate, Double> multiply(DateTimeSeries<LocalDate, Double> series1,
                                                     DateTimeSeries<LocalDate, Double> series2) {
    if ((series1 instanceof LocalDateDoubleTimeSeries) && (series2 instanceof LocalDateDoubleTimeSeries)) {
      return ((LocalDateDoubleTimeSeries) series1).multiply(((LocalDateDoubleTimeSeries) series2));
    } else {
      // this shouldn't happen, the series should be LocalDateDoubleTimeSeries
      LocalDateDoubleTimeSeriesBuilder builder = ImmutableLocalDateDoubleTimeSeries.builder();
      LocalDate start =
          series1.getEarliestTime().isBefore(series2.getEarliestTime()) ?
              series1.getEarliestTime() :
              series2.getEarliestTime();
      LocalDate end =
          series1.getLatestTime().isAfter(series2.getLatestTime()) ?
              series1.getLatestTime() :
              series2.getLatestTime();
      LocalDateRange dateRange = LocalDateRange.of(start, end, true);

      for (LocalDate date : dateRange) {
        Double value1 = series1.getValue(date);
        Double value2 = series2.getValue(date);

        if (value1 != null && value2 != null) {
          builder.put(date, value1 * value2);
        }
      }
      return builder.build();
    }
  }

  private Set<MarketDataRequirement> getRequirements(
      final Currency base,
      final Currency counter,
      final MarketDataTime marketDataTime,
      final Function2<MarketDataId<?>, MarketDataTime, MarketDataRequirement> requirementBuilder) {

    CurrencyMatrixValueVisitor<Set<MarketDataRequirement>> visitor =
        new CurrencyMatrixValueVisitor<Set<MarketDataRequirement>>() {

      @Override
      public Set<MarketDataRequirement> visitFixed(CurrencyMatrixValue.CurrencyMatrixFixed fixedValue) {
        // if the rate is fixed there's no market data required
        return ImmutableSet.of();
      }

      @SuppressWarnings("unchecked")
      @Override
      public Set<MarketDataRequirement> visitValueRequirement(CurrencyMatrixValue.CurrencyMatrixValueRequirement req) {
        ValueRequirement valueRequirement = req.getValueRequirement();
        ExternalIdBundle id = valueRequirement.getTargetReference().getRequirement().getIdentifiers();
        String dataField = valueRequirement.getValueName();
        MarketDataId<?> marketDataId = RawId.of(id, FieldName.of(dataField));
        return ImmutableSet.of(requirementBuilder.apply(marketDataId, marketDataTime));
      }

      @Override
      public Set<MarketDataRequirement> visitCross(CurrencyMatrixValue.CurrencyMatrixCross cross) {
        return ImmutableSet.<MarketDataRequirement>builder()
            .addAll(getRequirements(base, cross.getCrossCurrency(), marketDataTime, requirementBuilder))
            .addAll(getRequirements(cross.getCrossCurrency(), counter, marketDataTime, requirementBuilder))
            .build();
      }
    };
    CurrencyMatrixValue value = _currencyMatrixLink.resolve().getConversion(base, counter);
    return value.accept(visitor);
  }

}
