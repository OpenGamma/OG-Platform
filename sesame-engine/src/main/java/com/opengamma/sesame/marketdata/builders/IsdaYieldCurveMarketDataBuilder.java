/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.builders;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.opengamma.financial.analytics.isda.credit.YieldCurveData;
import com.opengamma.financial.analytics.isda.credit.YieldCurveDataSnapshot;
import com.opengamma.sesame.marketdata.CreditCurveDataId;
import com.opengamma.sesame.marketdata.IsdaYieldCurveDataId;
import com.opengamma.sesame.marketdata.IsdaYieldCurveDataSnapshotId;
import com.opengamma.sesame.marketdata.MarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataId;
import com.opengamma.sesame.marketdata.MarketDataRequirement;
import com.opengamma.sesame.marketdata.MarketDataSource;
import com.opengamma.sesame.marketdata.SingleValueRequirement;
import com.opengamma.sesame.marketdata.TimeSeriesRequirement;
import com.opengamma.sesame.marketdata.scenarios.CyclePerturbations;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;

/**
 * Market data builder for isda yield curves.
 */
public class IsdaYieldCurveMarketDataBuilder implements MarketDataBuilder {

  @Override
  public Set<MarketDataRequirement> getSingleValueRequirements(SingleValueRequirement requirement,
                                                               ZonedDateTime valuationTime,
                                                               Set<? extends MarketDataRequirement> suppliedData) {
    return Collections.emptySet();
  }

  @Override
  public Map<SingleValueRequirement, Result<?>> buildSingleValues(MarketDataBundle marketDataBundle,
                                                                  ZonedDateTime valuationTime,
                                                                  Set<SingleValueRequirement> requirements,
                                                                  MarketDataSource marketDataSource,
                                                                  CyclePerturbations cyclePerturbations) {

    ImmutableMap.Builder<SingleValueRequirement, Result<?>> results = ImmutableMap.builder();

    for (SingleValueRequirement requirement : requirements) {
      IsdaYieldCurveDataId curveDataId = (IsdaYieldCurveDataId) requirement.getMarketDataId();
      Result<YieldCurveDataSnapshot> snapshotResult =
          marketDataBundle.get(IsdaYieldCurveDataSnapshotId.of(curveDataId.getSnapshot()), YieldCurveDataSnapshot.class);
      if (snapshotResult.isSuccess()) {
        Map<Currency, YieldCurveData> yieldCurves = snapshotResult.getValue().getYieldCurves();
        Currency currency = curveDataId.getCurrency();

        if (yieldCurves.containsKey(currency)) {
          results.put(requirement, Result.success(yieldCurves.get(currency)));
        } else {
          //failure - no key in snapshot
          results.put(requirement,
                      Result.failure(FailureStatus.MISSING_DATA,
                                     "Failed to load curve data for yield curve key {} in snapshot {} for valuation {}",
                                     currency,
                                     curveDataId.getSnapshot(),
                                     valuationTime));
        }
      } else {
        //failure - no snapshot
        results.put(requirement, snapshotResult);
      }

    }

    return results.build();
  }

  @Override
  public Class<? extends MarketDataId> getKeyType() {
    return IsdaYieldCurveDataId.class;
  }

  @Override
  public Set<MarketDataRequirement> getTimeSeriesRequirements(TimeSeriesRequirement requirement,
                                                              Map<MarketDataId<?>, DateTimeSeries<LocalDate, ?>> suppliedData) {
    return Collections.emptySet();
  }

  @Override
  public Map<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> buildTimeSeries(
      MarketDataBundle marketDataBundle,
      Set<TimeSeriesRequirement> requirements,
      MarketDataSource marketDataSource,
      CyclePerturbations cyclePerturbations) {
    return Collections.emptyMap();
  }

}
