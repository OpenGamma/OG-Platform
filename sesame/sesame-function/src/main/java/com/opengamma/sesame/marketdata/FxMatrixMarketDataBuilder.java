/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Lists;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilder;
import com.opengamma.sesame.marketdata.scenarios.CyclePerturbations;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;

/**
 * Market data builder that creates matrices of FX rates.
 */
public class FxMatrixMarketDataBuilder implements MarketDataBuilder {

  @SuppressWarnings("unchecked")
  @Override
  public Set<MarketDataRequirement> getSingleValueRequirements(SingleValueRequirement requirement,
                                                               ZonedDateTime valuationTime,
                                                               Set<? extends MarketDataRequirement> suppliedData) {
    FxMatrixId id = (FxMatrixId) requirement.getMarketDataId();
    Set<Currency> currencies = id.getCurrencies();
    ArrayList<Currency> currencyList = Lists.newArrayList(currencies);
    // sort the currencies so the order is predictable. otherwise the currency pairs can be reversed between
    // runs and the market data requested in the first won't necessarily match what's requested in the second
    Collections.sort(currencyList);
    // arbitrarily choose the first currency as the base
    Iterator<Currency> itr = currencyList.iterator();
    Currency baseCurrency = itr.next();
    Set<MarketDataRequirement> requirements = new HashSet<>();

    while (itr.hasNext()) {
      CurrencyPair currencyPair = CurrencyPair.of(baseCurrency, itr.next());
      FxRateId rateId = FxRateId.of(currencyPair);
      requirements.add(SingleValueRequirement.of(rateId, requirement.getMarketDataTime()));
    }
    return requirements;
  }

  @Override
  public Set<MarketDataRequirement> getTimeSeriesRequirements(
      TimeSeriesRequirement requirement,
      Map<MarketDataId<?>, DateTimeSeries<LocalDate, ?>> suppliedData) {

    // TODO implement getTimeSeriesRequirements()
    throw new UnsupportedOperationException("getTimeSeriesRequirements not implemented");
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<SingleValueRequirement, Result<?>> buildSingleValues(MarketDataBundle marketDataBundle,
                                                                  ZonedDateTime valuationTime,
                                                                  Set<SingleValueRequirement> requirements,
                                                                  MarketDataSource marketDataSource,
                                                                  CyclePerturbations cyclePerturbations) {
    Map<SingleValueRequirement, Result<?>> results = new HashMap<>();

    for (SingleValueRequirement requirement : requirements) {
      FxMatrixId id = (FxMatrixId) requirement.getMarketDataId();
      Set<Currency> currencies = id.getCurrencies();
      results.put(requirement, buildFxMatrix(marketDataBundle, currencies));
    }
    return results;
  }

  @Override
  public Map<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> buildTimeSeries(
      MarketDataBundle marketDataBundle,
      Set<TimeSeriesRequirement> requirements,
      MarketDataSource marketDataSource,
      CyclePerturbations cyclePerturbations) {

    // TODO implement
    return Collections.emptyMap();
  }

  private Result<FXMatrix> buildFxMatrix(MarketDataBundle marketDataBundle, Set<Currency> currencies) {
    FXMatrix fxMatrix = new FXMatrix();
    ArrayList<Currency> currencyList = Lists.newArrayList(currencies);
    // sort the currencies so the order is predictable. otherwise the currency pairs can be reversed between
    // runs and the market data requested in the first won't necessarily match what's requested in the second
    Collections.sort(currencyList);
    // arbitrarily choose the first currency as the base
    Iterator<Currency> itr = currencyList.iterator();
    Currency baseCurrency = itr.next();

    while (itr.hasNext()) {
      Currency counterCurrency = itr.next();
      FxRateId rateId = FxRateId.of(CurrencyPair.of(baseCurrency, counterCurrency));
      Result<Double> rate = marketDataBundle.get(rateId, Double.class);

      if (!rate.isSuccess()) {
        return Result.failure(rate);
      }
      fxMatrix.addCurrency(counterCurrency, baseCurrency, rate.getValue());
    }
    return Result.success(fxMatrix);
  }

  @Override
  public Class<? extends MarketDataId> getKeyType() {
    return FxMatrixId.class;
  }
}
