/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.builders;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.marketdata.FieldName;
import com.opengamma.sesame.marketdata.MarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataId;
import com.opengamma.sesame.marketdata.MarketDataRequest;
import com.opengamma.sesame.marketdata.MarketDataRequirement;
import com.opengamma.sesame.marketdata.MarketDataSource;
import com.opengamma.sesame.marketdata.MarketDataTime;
import com.opengamma.sesame.marketdata.RawId;
import com.opengamma.sesame.marketdata.SingleValueRequirement;
import com.opengamma.sesame.marketdata.TimeSeriesRequirement;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;
import com.opengamma.util.time.LocalDateRange;

/**
 * Market data builder for market data values provided by an external data source.
 * <p>
 * Default values are taken from a {@link MarketDataSource}, time series and values for a specific date
 * are loaded from a historical time series source.
 */
public class RawMarketDataBuilder implements MarketDataBuilder {

  private final HistoricalTimeSeriesSource _timeSeriesSource;
  private final String _dataSource;
  private final String _dataProvider;

  /**
   * @param timeSeriesSource source of historical time series data
   * @param dataSource the data source name used when querying the time series source
   * @param dataProvider the data provider name used when querying the time series source, possibly null
   */
  public RawMarketDataBuilder(HistoricalTimeSeriesSource timeSeriesSource,
                              String dataSource,
                              @Nullable String dataProvider) {
    _timeSeriesSource = ArgumentChecker.notNull(timeSeriesSource, "timeSeriesSource");
    _dataSource = ArgumentChecker.notEmpty(dataSource, "dataSource");
    _dataProvider = dataProvider;
  }

  @Override
  public Set<MarketDataRequirement> getSingleValueRequirements(SingleValueRequirement requirement,
                                                               ZonedDateTime valuationTime,
                                                               Set<? extends MarketDataRequirement> suppliedData) {
    return Collections.emptySet();
  }

  @Override
  public Set<MarketDataRequirement> getTimeSeriesRequirements(
      TimeSeriesRequirement requirement,
      Map<MarketDataId<?>, DateTimeSeries<LocalDate, ?>> suppliedData) {
    return Collections.emptySet();
  }

  @Override
  public Map<SingleValueRequirement, Result<?>> buildSingleValues(MarketDataBundle marketDataBundle,
                                                                  ZonedDateTime valuationTime,
                                                                  Set<SingleValueRequirement> marketDataRequirements,
                                                                  MarketDataSource marketDataSource) {
    // map of request->requirement so we can build the results
    Map<MarketDataRequest, SingleValueRequirement> requirementMap = new HashMap<>();
    ImmutableMap.Builder<SingleValueRequirement, Result<?>> results = ImmutableMap.builder();

    for (SingleValueRequirement requirement : marketDataRequirements) {
      // builders are keyed by type in the engine and requirements are dispatched to builders based on their key type
      // so this cast will always succeed
      RawId<?> marketDataId = (RawId<?>) requirement.getMarketDataId();
      MarketDataTime time = requirement.getMarketDataTime();

      switch (time.getType()) {
        case VALUATION_TIME:
          // use the market data source
          FieldName fieldName = marketDataId.getFieldName();
          ExternalIdBundle id = marketDataId.getId();
          MarketDataRequest request = MarketDataRequest.of(id, fieldName);
          requirementMap.put(request, requirement);
          break;
        case DATE:
          // use the HTS source
          // for now we only support LocalDate
          LocalDate date = time.getDate();
          // TODO get the value from the time series source
          HistoricalTimeSeries timeSeries =
              _timeSeriesSource.getHistoricalTimeSeries(marketDataId.getId(), _dataSource, _dataProvider,
                                                        marketDataId.toString(), date, true, date, true);

          if (timeSeries != null && !timeSeries.getTimeSeries().isEmpty()) {
            Double value = timeSeries.getTimeSeries().getValue(date);
            if (value != null) {
              results.put(requirement, Result.success(value));
            }
          }
          break;
        default:
          // TODO failure - can't handle exact times yet
          break;
      }
    }
    Map<MarketDataRequest, Result<?>> data = marketDataSource.get(requirementMap.keySet());

    for (Map.Entry<MarketDataRequest, Result<?>> entry : data.entrySet()) {
      MarketDataRequest request = entry.getKey();
      SingleValueRequirement requirement = requirementMap.get(request);
      results.put(requirement, entry.getValue());
    }
    return results.build();
  }

  @Override
  public Map<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> buildTimeSeries(
      MarketDataBundle marketDataBundle,
      Set<TimeSeriesRequirement> requirements,
      MarketDataSource marketDataSource) {

    Map<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> results = new HashMap<>();

    for (TimeSeriesRequirement requirement : requirements) {
      RawId<?> marketDataId = (RawId<?>) requirement.getMarketDataId();
      LocalDateRange dateRange = requirement.getMarketDataTime().getDateRange();

      HistoricalTimeSeries timeSeries =
          _timeSeriesSource.getHistoricalTimeSeries(marketDataId.getId(), _dataSource, _dataProvider,
                                                    marketDataId.getFieldName().getName(),
                                                    dateRange.getStartDateInclusive(), true,
                                                    dateRange.getEndDateInclusive(), true);

      if (timeSeries == null || timeSeries.getTimeSeries().isEmpty()) {
        Result<DateTimeSeries<LocalDate, ?>> result = Result.failure(FailureStatus.MISSING_DATA,
                                                                     "No time series data available for {}/{}",
                                                                     marketDataId.getId(), dateRange);
        results.put(requirement, result);
      } else {
        Result<DateTimeSeries<LocalDate, ?>> result =
            Result.<DateTimeSeries<LocalDate, ?>>success(timeSeries.getTimeSeries());
        results.put(requirement, result);
      }
    }
    return results;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class getKeyType() {
    return RawId.class;
  }
}
