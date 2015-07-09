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
import com.google.common.collect.ImmutableSet;
import com.opengamma.financial.analytics.isda.credit.CreditCurveData;
import com.opengamma.financial.analytics.isda.credit.CreditCurveDataKey;
import com.opengamma.financial.analytics.isda.credit.CreditCurveDataSnapshot;
import com.opengamma.sesame.marketdata.CreditCurveDataId;
import com.opengamma.sesame.marketdata.CreditCurveDataSnapshotId;
import com.opengamma.sesame.marketdata.MarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataId;
import com.opengamma.sesame.marketdata.MarketDataRequirement;
import com.opengamma.sesame.marketdata.MarketDataSource;
import com.opengamma.sesame.marketdata.SingleValueRequirement;
import com.opengamma.sesame.marketdata.TimeSeriesRequirement;
import com.opengamma.sesame.marketdata.scenarios.CyclePerturbations;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;

/**
 * Market data builder for credit curves.
 */
public class CreditCurveMarketDataBuilder implements MarketDataBuilder {

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
      CreditCurveDataId curveDataId = (CreditCurveDataId) requirement.getMarketDataId();
      Result<CreditCurveDataSnapshot> snapshotResult =
          marketDataBundle.get(CreditCurveDataSnapshotId.of(curveDataId.getSnapshot()), CreditCurveDataSnapshot.class);
      if (snapshotResult.isSuccess()) {
        ImmutableMap<CreditCurveDataKey, CreditCurveData> creditCurves = snapshotResult.getValue().getCreditCurves();
        CreditCurveDataKey key = curveDataId.getKey();

        if (creditCurves.containsKey(key)) {
          results.put(requirement, Result.success(creditCurves.get(key)));
        } else {
          //failure - no key in snapshot
          results.put(requirement,
                      Result.failure(FailureStatus.MISSING_DATA,
                                     "Failed to load curve data for credit curve key {} in snapshot {} for valuation {}",
                                     key,
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
    return CreditCurveDataId.class;
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
