/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import static com.opengamma.util.result.ResultTestUtils.assertSuccess;
import static org.mockito.Mockito.mock;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.SimpleCurrencyMatrix;
import com.opengamma.id.ExternalId;
import com.opengamma.sesame.marketdata.builders.FxRateMarketDataBuilder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class FxRateMarketDataBuilderTest {

  private static final double DELTA = 0.00001;
  private static final FieldName MARKET_VALUE = FieldName.of(MarketDataRequirementNames.MARKET_VALUE);
  private static final double GBPUSD_RATE = 1.61;
  private static final double USDCHF_RATE = 0.91;
  private static final double EURUSD_RATE = 1.35;

  private static SingleValueRequirement requirement(String currencyPair) {
    return SingleValueRequirement.of(FxRateId.of(CurrencyPair.parse(currencyPair)));
  }

  @Test
  public void fixedRate() {
    SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    matrix.setFixedConversion(Currency.GBP, Currency.USD, GBPUSD_RATE);
    FxRateMarketDataBuilder builder = new FxRateMarketDataBuilder(matrix);

    MarketDataBundle emptyBundle = MarketDataEnvironmentBuilder.empty().toBundle();
    SingleValueRequirement gbpUsd = requirement("GBP/USD");
    SingleValueRequirement usdGbp = requirement("USD/GBP");
    Set<SingleValueRequirement> requirements = ImmutableSet.of(gbpUsd, usdGbp);
    MarketDataSource marketDataSource = mock(MarketDataSource.class);
    Map<SingleValueRequirement, Result<?>> values =
        builder.buildSingleValues(emptyBundle, ZonedDateTime.now(), requirements, marketDataSource);

    Result<?> gbpUsdResult = values.get(gbpUsd);
    assertSuccess(gbpUsdResult);
    assertEquals(GBPUSD_RATE, (Double) gbpUsdResult.getValue(), DELTA);

    Result<?> usdGbpResult = values.get(usdGbp);
    assertSuccess(usdGbpResult);
    assertEquals(1 / GBPUSD_RATE, (Double) usdGbpResult.getValue(), DELTA);
  }

  @Test
  public void lookUpRate() {
    SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    ExternalId rateId = ExternalId.of("x", "GBPUSD");
    ValueRequirement valueReq = new ValueRequirement(MARKET_VALUE.getName(), ComputationTargetType.PRIMITIVE, rateId);
    matrix.setLiveData(Currency.GBP, Currency.USD, valueReq);
    FxRateMarketDataBuilder builder = new FxRateMarketDataBuilder(matrix);
    MarketDataSource marketDataSource = mock(MarketDataSource.class);
    MarketDataBundle bundle =
        new MarketDataEnvironmentBuilder()
            .add(RawId.of(rateId.toBundle()), GBPUSD_RATE)
            .valuationTime(ZonedDateTime.now())
            .build()
            .toBundle();

    SingleValueRequirement gbpUsd = requirement("GBP/USD");
    SingleValueRequirement usdGbp = requirement("USD/GBP");
    Set<SingleValueRequirement> requirements = ImmutableSet.of(gbpUsd, usdGbp);
    Map<SingleValueRequirement, Result<?>> values =
        builder.buildSingleValues(bundle, ZonedDateTime.now(), requirements, marketDataSource);

    Result<?> gbpUsdResult = values.get(gbpUsd);
    assertSuccess(gbpUsdResult);
    assertEquals(GBPUSD_RATE, (Double) gbpUsdResult.getValue(), DELTA);

    Result<?> usdGbpResult = values.get(usdGbp);
    assertSuccess(usdGbpResult);
    assertEquals(1 / GBPUSD_RATE, (Double) usdGbpResult.getValue(), DELTA);
  }

  @Test
  public void crossFixed() {
    SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    matrix.setFixedConversion(Currency.USD, Currency.CHF, USDCHF_RATE);
    matrix.setFixedConversion(Currency.EUR, Currency.USD, EURUSD_RATE);
    matrix.setCrossConversion(Currency.EUR, Currency.CHF, Currency.USD);
    FxRateMarketDataBuilder builder = new FxRateMarketDataBuilder(matrix);

    SingleValueRequirement eurChf = requirement("EUR/CHF");
    SingleValueRequirement chfEur = requirement("CHF/EUR");
    Set<SingleValueRequirement> requirements = ImmutableSet.of(eurChf, chfEur);
    MarketDataBundle bundle = MarketDataEnvironmentBuilder.empty().toBundle();
    MarketDataSource marketDataSource = mock(MarketDataSource.class);
    Map<SingleValueRequirement, Result<?>> values =
        builder.buildSingleValues(bundle, ZonedDateTime.now(), requirements, marketDataSource);

    Result<?> eurChfResult = values.get(eurChf);
    assertSuccess(eurChfResult);
    assertEquals(USDCHF_RATE * EURUSD_RATE, (Double) eurChfResult.getValue(), DELTA);

    Result<?> chfEurResult = values.get(chfEur);
    assertSuccess(chfEurResult);
    assertEquals(1 / (USDCHF_RATE * EURUSD_RATE), (Double) chfEurResult.getValue(), DELTA);
  }

  @Test
  public void crossLookup() {
    SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();

    ExternalId usdchfRateId = ExternalId.of("x", "USDCHF");
    ValueRequirement usdchfReq = new ValueRequirement(MARKET_VALUE.getName(), ComputationTargetType.PRIMITIVE, usdchfRateId);
    matrix.setLiveData(Currency.USD, Currency.CHF, usdchfReq);
    double usdchfValue = USDCHF_RATE;

    ExternalId eurusdRateId = ExternalId.of("x", "EURUSD");
    ValueRequirement eurusdReq = new ValueRequirement(MARKET_VALUE.getName(), ComputationTargetType.PRIMITIVE, eurusdRateId);
    matrix.setLiveData(Currency.EUR, Currency.USD, eurusdReq);
    double eurusdValue = EURUSD_RATE;

    matrix.setCrossConversion(Currency.EUR, Currency.CHF, Currency.USD);
    FxRateMarketDataBuilder builder = new FxRateMarketDataBuilder(matrix);

    MarketDataBundle bundle =
        new MarketDataEnvironmentBuilder()
            .add(RawId.of(usdchfRateId.toBundle()), usdchfValue)
            .add(RawId.of(eurusdRateId.toBundle()), eurusdValue)
            .valuationTime(ZonedDateTime.now())
            .build()
            .toBundle();

    SingleValueRequirement eurChf = requirement("EUR/CHF");
    SingleValueRequirement chfEur = requirement("CHF/EUR");
    Set<SingleValueRequirement> requirements = ImmutableSet.of(eurChf, chfEur);

    MarketDataSource marketDataSource = mock(MarketDataSource.class);
    Map<SingleValueRequirement, Result<?>> values =
        builder.buildSingleValues(bundle, ZonedDateTime.now(), requirements, marketDataSource);

    Result<?> eurChfResult = values.get(eurChf);
    assertSuccess(eurChfResult);
    assertEquals(USDCHF_RATE * EURUSD_RATE, (Double) eurChfResult.getValue(), DELTA);

    Result<?> chfEurResult = values.get(chfEur);
    assertSuccess(chfEurResult);
    assertEquals(1 / (USDCHF_RATE * EURUSD_RATE), (Double) chfEurResult.getValue(), DELTA);
  }
}
