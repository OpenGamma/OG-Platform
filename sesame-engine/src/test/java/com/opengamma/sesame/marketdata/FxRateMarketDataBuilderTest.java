/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import static com.opengamma.util.result.ResultTestUtils.assertSuccess;
import static org.mockito.Mockito.mock;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.SimpleCurrencyMatrix;
import com.opengamma.id.ExternalId;
import com.opengamma.sesame.marketdata.builders.FxRateMarketDataBuilder;
import com.opengamma.sesame.marketdata.scenarios.CurrencyPairFilter;
import com.opengamma.sesame.marketdata.scenarios.CyclePerturbations;
import com.opengamma.sesame.marketdata.scenarios.FxRateShift;
import com.opengamma.sesame.marketdata.scenarios.MarketDataFilter;
import com.opengamma.sesame.marketdata.scenarios.Perturbation;
import com.opengamma.sesame.marketdata.scenarios.SinglePerturbationMapping;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeriesBuilder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.LocalDateRange;

@Test(groups = TestGroup.UNIT)
public class FxRateMarketDataBuilderTest {

  private static final double DELTA = 0.00001;
  private static final FieldName MARKET_VALUE = FieldName.of(MarketDataRequirementNames.MARKET_VALUE);
  private static final double GBPUSD_RATE = 1.61;
  private static final double USDCHF_RATE = 0.91;
  private static final double EURUSD_RATE = 1.35;
  private static final CyclePerturbations EMPTY_PERTURBATIONS =
      new CyclePerturbations(ImmutableSet.<MarketDataRequirement>of(), ImmutableList.<SinglePerturbationMapping>of());

  private static SingleValueRequirement singleValueRequirement(String currencyPair) {
    return SingleValueRequirement.of(FxRateId.of(CurrencyPair.parse(currencyPair)));
  }

  private static TimeSeriesRequirement timeSeriesRequirement(String currencyPair, LocalDateRange dateRange) {
    return TimeSeriesRequirement.of(FxRateId.of(CurrencyPair.parse(currencyPair)), dateRange);
  }

  public void fixedRate() {
    SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    matrix.setFixedConversion(Currency.GBP, Currency.USD, GBPUSD_RATE);
    FxRateMarketDataBuilder builder = new FxRateMarketDataBuilder(ConfigLink.<CurrencyMatrix>resolved(matrix));

    MarketDataBundle emptyBundle = MarketDataEnvironmentBuilder.empty().toBundle();
    SingleValueRequirement gbpUsd = singleValueRequirement("GBP/USD");
    SingleValueRequirement usdGbp = singleValueRequirement("USD/GBP");
    Set<SingleValueRequirement> requirements = ImmutableSet.of(gbpUsd, usdGbp);
    MarketDataSource marketDataSource = mock(MarketDataSource.class);
    Map<SingleValueRequirement, Result<?>> values =
        builder.buildSingleValues(emptyBundle, ZonedDateTime.now(), requirements, marketDataSource, EMPTY_PERTURBATIONS);

    Result<?> gbpUsdResult = values.get(gbpUsd);
    assertSuccess(gbpUsdResult);
    assertEquals(GBPUSD_RATE, (Double) gbpUsdResult.getValue(), DELTA);

    Result<?> usdGbpResult = values.get(usdGbp);
    assertSuccess(usdGbpResult);
    assertEquals(1 / GBPUSD_RATE, (Double) usdGbpResult.getValue(), DELTA);
  }

  public void lookUpRate() {
    SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    ExternalId rateId = ExternalId.of("x", "GBPUSD");
    ValueRequirement valueReq = new ValueRequirement(MARKET_VALUE.getName(), ComputationTargetType.PRIMITIVE, rateId);
    matrix.setLiveData(Currency.GBP, Currency.USD, valueReq);
    FxRateMarketDataBuilder builder = new FxRateMarketDataBuilder(ConfigLink.<CurrencyMatrix>resolved(matrix));
    MarketDataSource marketDataSource = mock(MarketDataSource.class);
    MarketDataBundle bundle =
        new MarketDataEnvironmentBuilder()
            .add(RawId.of(rateId.toBundle()), GBPUSD_RATE)
            .valuationTime(ZonedDateTime.now())
            .build()
            .toBundle();

    SingleValueRequirement gbpUsd = singleValueRequirement("GBP/USD");
    SingleValueRequirement usdGbp = singleValueRequirement("USD/GBP");
    Set<SingleValueRequirement> requirements = ImmutableSet.of(gbpUsd, usdGbp);
    Map<SingleValueRequirement, Result<?>> values =
        builder.buildSingleValues(bundle, ZonedDateTime.now(), requirements, marketDataSource, EMPTY_PERTURBATIONS);

    Result<?> gbpUsdResult = values.get(gbpUsd);
    assertSuccess(gbpUsdResult);
    assertEquals(GBPUSD_RATE, (Double) gbpUsdResult.getValue(), DELTA);

    Result<?> usdGbpResult = values.get(usdGbp);
    assertSuccess(usdGbpResult);
    assertEquals(1 / GBPUSD_RATE, (Double) usdGbpResult.getValue(), DELTA);
  }

  public void crossFixed() {
    SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    matrix.setFixedConversion(Currency.USD, Currency.CHF, USDCHF_RATE);
    matrix.setFixedConversion(Currency.EUR, Currency.USD, EURUSD_RATE);
    matrix.setCrossConversion(Currency.EUR, Currency.CHF, Currency.USD);
    FxRateMarketDataBuilder builder = new FxRateMarketDataBuilder(ConfigLink.<CurrencyMatrix>resolved(matrix));

    SingleValueRequirement eurChf = singleValueRequirement("EUR/CHF");
    SingleValueRequirement chfEur = singleValueRequirement("CHF/EUR");
    Set<SingleValueRequirement> requirements = ImmutableSet.of(eurChf, chfEur);
    MarketDataBundle bundle = MarketDataEnvironmentBuilder.empty().toBundle();
    MarketDataSource marketDataSource = mock(MarketDataSource.class);
    Map<SingleValueRequirement, Result<?>> values =
        builder.buildSingleValues(bundle, ZonedDateTime.now(), requirements, marketDataSource, EMPTY_PERTURBATIONS);

    Result<?> eurChfResult = values.get(eurChf);
    assertSuccess(eurChfResult);
    assertEquals(USDCHF_RATE * EURUSD_RATE, (Double) eurChfResult.getValue(), DELTA);

    Result<?> chfEurResult = values.get(chfEur);
    assertSuccess(chfEurResult);
    assertEquals(1 / (USDCHF_RATE * EURUSD_RATE), (Double) chfEurResult.getValue(), DELTA);
  }

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
    FxRateMarketDataBuilder builder = new FxRateMarketDataBuilder(ConfigLink.<CurrencyMatrix>resolved(matrix));

    MarketDataBundle bundle =
        new MarketDataEnvironmentBuilder()
            .add(RawId.of(usdchfRateId.toBundle()), usdchfValue)
            .add(RawId.of(eurusdRateId.toBundle()), eurusdValue)
            .valuationTime(ZonedDateTime.now())
            .build()
            .toBundle();

    SingleValueRequirement eurChf = singleValueRequirement("EUR/CHF");
    SingleValueRequirement chfEur = singleValueRequirement("CHF/EUR");
    Set<SingleValueRequirement> requirements = ImmutableSet.of(eurChf, chfEur);

    MarketDataSource marketDataSource = mock(MarketDataSource.class);
    Map<SingleValueRequirement, Result<?>> values =
        builder.buildSingleValues(bundle, ZonedDateTime.now(), requirements, marketDataSource, EMPTY_PERTURBATIONS);

    Result<?> eurChfResult = values.get(eurChf);
    assertSuccess(eurChfResult);
    assertEquals(USDCHF_RATE * EURUSD_RATE, (Double) eurChfResult.getValue(), DELTA);

    Result<?> chfEurResult = values.get(chfEur);
    assertSuccess(chfEurResult);
    assertEquals(1 / (USDCHF_RATE * EURUSD_RATE), (Double) chfEurResult.getValue(), DELTA);
  }

  // TODO test time series data

  public void getTimeSeriesRequirements() {
    SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    ExternalId rateId = ExternalId.of("x", "GBPUSD");
    ValueRequirement valueReq = new ValueRequirement(MARKET_VALUE.getName(), ComputationTargetType.PRIMITIVE, rateId);
    matrix.setLiveData(Currency.GBP, Currency.USD, valueReq);
    FxRateMarketDataBuilder builder = new FxRateMarketDataBuilder(ConfigLink.<CurrencyMatrix>resolved(matrix));
    LocalDateRange dateRange = LocalDateRange.of(LocalDate.of(2010, 5, 17), LocalDate.of(2012, 6, 4), true);

    FxRateId fxRateId = FxRateId.of(Currency.GBP, Currency.USD);
    TimeSeriesRequirement requirement = TimeSeriesRequirement.of(fxRateId, dateRange);
    Map<MarketDataId<?>, DateTimeSeries<LocalDate, ?>> suppliedData = ImmutableMap.of();
    Set<MarketDataRequirement> requirements = builder.getTimeSeriesRequirements(requirement, suppliedData);

    assertEquals(1, requirements.size());
    MarketDataRequirement rawReq = requirements.iterator().next();
    assertTrue(rawReq instanceof TimeSeriesRequirement);
    MarketDataId marketDataId = rawReq.getMarketDataId();
    assertTrue(marketDataId instanceof RawId);
    assertEquals(rateId.toBundle(), ((RawId) marketDataId).getId());
    assertEquals(dateRange, rawReq.getMarketDataTime().getDateRange());

    FxRateId inverseFxRateId = FxRateId.of(Currency.GBP, Currency.USD);
    TimeSeriesRequirement inverseRequirement = TimeSeriesRequirement.of(inverseFxRateId, dateRange);
    Set<MarketDataRequirement> inverseRequirements = builder.getTimeSeriesRequirements(inverseRequirement, suppliedData);

    assertEquals(1, inverseRequirements.size());
    MarketDataRequirement inverseRawReq = requirements.iterator().next();
    assertTrue(inverseRawReq instanceof TimeSeriesRequirement);
    MarketDataId inverseMarketDataId = inverseRawReq.getMarketDataId();
    assertTrue(inverseMarketDataId instanceof RawId);
    assertEquals(rateId.toBundle(), ((RawId) inverseMarketDataId).getId());
    assertEquals(dateRange, inverseRawReq.getMarketDataTime().getDateRange());
  }

  /**
   * checks that no requirements are returned if the supplied data contains the inverse rate over the required
   * date range
   */
  public void getTimeSeriesRequirementsInverseRateSupplied() {
    SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    ExternalId rateId = ExternalId.of("x", "GBPUSD");
    FxRateId fxRateId = FxRateId.of(Currency.GBP, Currency.USD);
    FxRateId inverseRateId = FxRateId.of(Currency.USD, Currency.GBP);
    ValueRequirement valueReq = new ValueRequirement(MARKET_VALUE.getName(), ComputationTargetType.PRIMITIVE, rateId);
    matrix.setLiveData(Currency.GBP, Currency.USD, valueReq);
    FxRateMarketDataBuilder builder = new FxRateMarketDataBuilder(ConfigLink.<CurrencyMatrix>resolved(matrix));
    LocalDateRange dateRange = LocalDateRange.of(LocalDate.of(2011, 5, 17), LocalDate.of(2011, 6, 4), true);

    LocalDateDoubleTimeSeriesBuilder timeSeriesBuilder = ImmutableLocalDateDoubleTimeSeries.builder();
    timeSeriesBuilder.put(LocalDate.of(2010, 5, 17), 1);
    timeSeriesBuilder.put(LocalDate.of(2012, 6, 4), 2);
    DateTimeSeries<LocalDate, Double> timeSeries = timeSeriesBuilder.build();
    Map<MarketDataId<?>, DateTimeSeries<LocalDate, ?>> suppliedData =
        ImmutableMap.<MarketDataId<?>, DateTimeSeries<LocalDate, ?>>of(inverseRateId, timeSeries);

    TimeSeriesRequirement requirement = TimeSeriesRequirement.of(fxRateId, dateRange);
    Set<MarketDataRequirement> requirements = builder.getTimeSeriesRequirements(requirement, suppliedData);
    assertTrue(requirements.isEmpty());
  }

  /**
   * checks that no requirements are returned if the supplied data contains the inverse rate but not over the required
   * date range
   */
  public void getTimeSeriesRequirementsInverseRateSuppliedWrongRange() {
    SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    ExternalId rateId = ExternalId.of("x", "GBPUSD");
    FxRateId fxRateId = FxRateId.of(Currency.GBP, Currency.USD);
    FxRateId inverseRateId = FxRateId.of(Currency.USD, Currency.GBP);
    ValueRequirement valueReq = new ValueRequirement(MARKET_VALUE.getName(), ComputationTargetType.PRIMITIVE, rateId);
    matrix.setLiveData(Currency.GBP, Currency.USD, valueReq);
    FxRateMarketDataBuilder builder = new FxRateMarketDataBuilder(ConfigLink.<CurrencyMatrix>resolved(matrix));
    LocalDateRange dateRange = LocalDateRange.of(LocalDate.of(2009, 5, 17), LocalDate.of(2011, 6, 4), true);

    LocalDateDoubleTimeSeriesBuilder timeSeriesBuilder = ImmutableLocalDateDoubleTimeSeries.builder();
    timeSeriesBuilder.put(LocalDate.of(2010, 5, 17), 1);
    timeSeriesBuilder.put(LocalDate.of(2012, 6, 4), 2);
    DateTimeSeries<LocalDate, Double> timeSeries = timeSeriesBuilder.build();
    Map<MarketDataId<?>, DateTimeSeries<LocalDate, ?>> suppliedData =
        ImmutableMap.<MarketDataId<?>, DateTimeSeries<LocalDate, ?>>of(inverseRateId, timeSeries);

    TimeSeriesRequirement requirement = TimeSeriesRequirement.of(fxRateId, dateRange);
    Set<MarketDataRequirement> requirements = builder.getTimeSeriesRequirements(requirement, suppliedData);
    assertEquals(1, requirements.size());
    MarketDataRequirement rawReq = requirements.iterator().next();
    assertTrue(rawReq instanceof TimeSeriesRequirement);
    MarketDataId marketDataId = rawReq.getMarketDataId();
    assertTrue(marketDataId instanceof RawId);
    assertEquals(rateId.toBundle(), ((RawId) marketDataId).getId());
    assertEquals(dateRange, rawReq.getMarketDataTime().getDateRange());
  }

  public void lookUpTimeSeries() {
    LocalDateDoubleTimeSeriesBuilder timeSeriesBuilder = ImmutableLocalDateDoubleTimeSeries.builder();
    LocalDate date1 = LocalDate.of(2010, 5, 17);
    LocalDate date2 = LocalDate.of(2011, 5, 17);
    LocalDate date3 = LocalDate.of(2012, 5, 17);
    timeSeriesBuilder.put(date1, 1);
    timeSeriesBuilder.put(date2, 2);
    timeSeriesBuilder.put(date3, 3);
    DateTimeSeries<LocalDate, Double> timeSeries = timeSeriesBuilder.build();
    SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    ExternalId rateId = ExternalId.of("x", "GBPUSD");
    FxRateId fxRateId = FxRateId.of(Currency.GBP, Currency.USD);
    ValueRequirement valueReq = new ValueRequirement(MARKET_VALUE.getName(), ComputationTargetType.PRIMITIVE, rateId);
    matrix.setLiveData(Currency.GBP, Currency.USD, valueReq);
    FxRateMarketDataBuilder builder = new FxRateMarketDataBuilder(ConfigLink.<CurrencyMatrix>resolved(matrix));

    MarketDataEnvironment marketData =
        new MarketDataEnvironmentBuilder()
            .add(RawId.of(rateId.toBundle()), timeSeries)
            .valuationTime(ZonedDateTime.now())
            .build();

    LocalDateRange dateRange = LocalDateRange.of(date1, date3, true);

    TimeSeriesRequirement requirement = TimeSeriesRequirement.of(fxRateId, dateRange);
    Set<TimeSeriesRequirement> requirements = ImmutableSet.of(requirement);
    Map<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> timeSeriesMap =
        builder.buildTimeSeries(
            marketData.toBundle(),
            requirements,
            new EmptyMarketDataFactory.DataSource(),
            EMPTY_PERTURBATIONS);
    Result<? extends DateTimeSeries<LocalDate, ?>> timeSeriesResult = timeSeriesMap.get(requirement);
    assertSuccess(timeSeriesResult);
    DateTimeSeries<LocalDate, ?> rateTimeSeries = timeSeriesResult.getValue();
    assertEquals(1d, rateTimeSeries.getValue(date1));
    assertEquals(2d, rateTimeSeries.getValue(date2));
    assertEquals(3d, rateTimeSeries.getValue(date3));
  }

  public void lookUpTimeSeriesInverse() {
    LocalDate date1 = LocalDate.of(2010, 5, 17);
    LocalDate date2 = LocalDate.of(2011, 5, 17);
    LocalDate date3 = LocalDate.of(2012, 5, 17);

    LocalDateDoubleTimeSeries timeSeries =
        ImmutableLocalDateDoubleTimeSeries.builder()
            .put(date1, 1)
            .put(date2, 2)
            .put(date3, 3)
            .build();

    SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    ExternalId rateId = ExternalId.of("x", "GBPUSD");
    FxRateId fxRateId = FxRateId.of(Currency.USD, Currency.GBP);
    ValueRequirement valueReq = new ValueRequirement(MARKET_VALUE.getName(), ComputationTargetType.PRIMITIVE, rateId);
    matrix.setLiveData(Currency.GBP, Currency.USD, valueReq);
    FxRateMarketDataBuilder builder = new FxRateMarketDataBuilder(ConfigLink.<CurrencyMatrix>resolved(matrix));

    MarketDataEnvironment marketData =
        new MarketDataEnvironmentBuilder()
            .add(RawId.of(rateId.toBundle()), timeSeries)
            .valuationTime(ZonedDateTime.now())
            .build();

    LocalDateRange dateRange = LocalDateRange.of(date1, date3, true);

    TimeSeriesRequirement requirement = TimeSeriesRequirement.of(fxRateId, dateRange);
    Set<TimeSeriesRequirement> requirements = ImmutableSet.of(requirement);
    Map<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> timeSeriesMap =
        builder.buildTimeSeries(
            marketData.toBundle(),
            requirements,
            new EmptyMarketDataFactory.DataSource(),
            EMPTY_PERTURBATIONS);
    Result<? extends DateTimeSeries<LocalDate, ?>> timeSeriesResult = timeSeriesMap.get(requirement);
    assertSuccess(timeSeriesResult);
    DateTimeSeries<LocalDate, ?> rateTimeSeries = timeSeriesResult.getValue();
    assertEquals(1 / 1d, rateTimeSeries.getValue(date1));
    assertEquals(1 / 2d, rateTimeSeries.getValue(date2));
    assertEquals(1 / 3d, rateTimeSeries.getValue(date3));
  }

  public void crossTimeSeries() {
    LocalDate date1 = LocalDate.of(2010, 5, 17);
    LocalDate date2 = LocalDate.of(2011, 5, 17);
    LocalDate date3 = LocalDate.of(2012, 5, 17);
    LocalDateRange dateRange = LocalDateRange.of(date1, date3, true);

    LocalDateDoubleTimeSeries usdChfSeries =
        ImmutableLocalDateDoubleTimeSeries.builder().put(date1, 1).put(date2, 2).put(date3, 3).build();

    LocalDateDoubleTimeSeries eurUsdSeries =
        ImmutableLocalDateDoubleTimeSeries.builder().put(date1, 2).put(date2, 4).put(date3, 6).build();

    SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();

    ExternalId usdChfId = ExternalId.of("x", "USDCHF");
    ValueRequirement usdChfValueReq =
        new ValueRequirement(MARKET_VALUE.getName(), ComputationTargetType.PRIMITIVE, usdChfId);
    matrix.setLiveData(Currency.USD, Currency.CHF, usdChfValueReq);

    ExternalId eurUsdId = ExternalId.of("x", "EURUSD");
    ValueRequirement eurUsdValueReq =
        new ValueRequirement(MARKET_VALUE.getName(), ComputationTargetType.PRIMITIVE, eurUsdId);
    matrix.setLiveData(Currency.EUR, Currency.USD, eurUsdValueReq);

    matrix.setCrossConversion(Currency.EUR, Currency.CHF, Currency.USD);
    FxRateMarketDataBuilder builder = new FxRateMarketDataBuilder(ConfigLink.<CurrencyMatrix>resolved(matrix));

    RawId usdChfFxRateId = RawId.of(usdChfId.toBundle());
    RawId eurUsdFxRateId = RawId.of(eurUsdId.toBundle());
    TimeSeriesRequirement eurChf = timeSeriesRequirement("EUR/CHF", dateRange);
    TimeSeriesRequirement chfEur = timeSeriesRequirement("CHF/EUR", dateRange);
    Set<TimeSeriesRequirement> requirements = ImmutableSet.of(eurChf, chfEur);
    MarketDataBundle bundle =
        new MarketDataEnvironmentBuilder()
            .add(eurUsdFxRateId, eurUsdSeries)
            .add(usdChfFxRateId, usdChfSeries)
            .valuationTime(ZonedDateTime.now())
            .build()
            .toBundle();
    MarketDataSource marketDataSource = mock(MarketDataSource.class);
    Map<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> values =
        builder.buildTimeSeries(bundle, requirements, marketDataSource, EMPTY_PERTURBATIONS);

    Result<? extends DateTimeSeries<LocalDate, ?>> eurChfResult = values.get(eurChf);
    assertSuccess(eurChfResult);
    DateTimeSeries<LocalDate, ?> eurChfSeries = eurChfResult.getValue();
    assertEquals(2d, (Double) eurChfSeries.getValue(date1), DELTA);
    assertEquals(8d, (Double) eurChfSeries.getValue(date2), DELTA);
    assertEquals(18d, (Double) eurChfSeries.getValue(date3), DELTA);

    Result<? extends DateTimeSeries<LocalDate, ?>> chfEurResult = values.get(chfEur);
    assertSuccess(chfEurResult);
    DateTimeSeries<LocalDate, ?> chfEurSeries = chfEurResult.getValue();
    assertEquals(1 / 2d, (Double) chfEurSeries.getValue(date1), DELTA);
    assertEquals(1 / 8d, (Double) chfEurSeries.getValue(date2), DELTA);
    assertEquals(1 / 18d, (Double) chfEurSeries.getValue(date3), DELTA);
  }

  public void shiftFixed() {
    SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    matrix.setFixedConversion(Currency.GBP, Currency.USD, GBPUSD_RATE);
    FxRateMarketDataBuilder builder = new FxRateMarketDataBuilder(ConfigLink.<CurrencyMatrix>resolved(matrix));

    MarketDataBundle emptyBundle = MarketDataEnvironmentBuilder.empty().toBundle();
    SingleValueRequirement gbpUsd = singleValueRequirement("GBP/USD");
    SingleValueRequirement usdGbp = singleValueRequirement("USD/GBP");
    Set<SingleValueRequirement> requirements = ImmutableSet.of(gbpUsd, usdGbp);
    ImmutableSet<MarketDataRequirement> marketDataRequirements = ImmutableSet.<MarketDataRequirement>of(gbpUsd, usdGbp);
    MarketDataFilter filter = new CurrencyPairFilter(Currency.GBP, Currency.USD);
    Perturbation perturbation = FxRateShift.absolute(0.1);
    SinglePerturbationMapping mapping =
        SinglePerturbationMapping.builder()
            .filter(filter)
            .perturbation(perturbation)
            .build();
    List<SinglePerturbationMapping> mappings = ImmutableList.of(mapping);
    CyclePerturbations perturbations = new CyclePerturbations(marketDataRequirements, mappings);
    MarketDataSource marketDataSource = mock(MarketDataSource.class);
    Map<SingleValueRequirement, Result<?>> values =
        builder.buildSingleValues(emptyBundle, ZonedDateTime.now(), requirements, marketDataSource, perturbations);

    Result<?> gbpUsdResult = values.get(gbpUsd);
    assertSuccess(gbpUsdResult);
    assertEquals(GBPUSD_RATE + 0.1, (Double) gbpUsdResult.getValue(), DELTA);

    Result<?> usdGbpResult = values.get(usdGbp);
    assertSuccess(usdGbpResult);
    assertEquals(1 / (GBPUSD_RATE + 0.1), (Double) usdGbpResult.getValue(), DELTA);
  }

  public void shiftLookup() {
    SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    ExternalId rateId = ExternalId.of("x", "GBPUSD");
    ValueRequirement valueReq = new ValueRequirement(MARKET_VALUE.getName(), ComputationTargetType.PRIMITIVE, rateId);
    matrix.setLiveData(Currency.GBP, Currency.USD, valueReq);
    FxRateMarketDataBuilder builder = new FxRateMarketDataBuilder(ConfigLink.<CurrencyMatrix>resolved(matrix));
    MarketDataSource marketDataSource = mock(MarketDataSource.class);
    MarketDataBundle bundle =
        new MarketDataEnvironmentBuilder()
            .add(RawId.of(rateId.toBundle()), GBPUSD_RATE)
            .valuationTime(ZonedDateTime.now())
            .build()
            .toBundle();

    SingleValueRequirement gbpUsd = singleValueRequirement("GBP/USD");
    SingleValueRequirement usdGbp = singleValueRequirement("USD/GBP");
    Set<SingleValueRequirement> requirements = ImmutableSet.of(gbpUsd, usdGbp);
    Set<MarketDataRequirement> marketDataRequirements = ImmutableSet.<MarketDataRequirement>of(gbpUsd, usdGbp);
    MarketDataFilter filter = new CurrencyPairFilter(Currency.GBP, Currency.USD);
    Perturbation perturbation = FxRateShift.absolute(0.1);
    SinglePerturbationMapping mapping =
        SinglePerturbationMapping.builder()
            .filter(filter)
            .perturbation(perturbation)
            .build();
    List<SinglePerturbationMapping> mappings = ImmutableList.of(mapping);
    CyclePerturbations perturbations = new CyclePerturbations(marketDataRequirements, mappings);
    Map<SingleValueRequirement, Result<?>> values =
        builder.buildSingleValues(bundle, ZonedDateTime.now(), requirements, marketDataSource, perturbations);

    Result<?> gbpUsdResult = values.get(gbpUsd);
    assertSuccess(gbpUsdResult);
    assertEquals(GBPUSD_RATE + 0.1, (Double) gbpUsdResult.getValue(), DELTA);

    Result<?> usdGbpResult = values.get(usdGbp);
    assertSuccess(usdGbpResult);
    assertEquals(1 / (GBPUSD_RATE + 0.1), (Double) usdGbpResult.getValue(), DELTA);
  }

  /**
   * Tests that applying a shift to a cross rate returns a failure. Shifting cross rates isn't supported because
   * it would introduce inconsistent rates. The underlying observable rates should be shifted.
   */
  public void shiftCross() {
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
    FxRateMarketDataBuilder builder = new FxRateMarketDataBuilder(ConfigLink.<CurrencyMatrix>resolved(matrix));

    MarketDataBundle bundle =
        new MarketDataEnvironmentBuilder()
            .add(RawId.of(usdchfRateId.toBundle()), usdchfValue)
            .add(RawId.of(eurusdRateId.toBundle()), eurusdValue)
            .valuationTime(ZonedDateTime.now())
            .build()
            .toBundle();

    SingleValueRequirement eurChf = singleValueRequirement("EUR/CHF");
    SingleValueRequirement chfEur = singleValueRequirement("CHF/EUR");
    Set<SingleValueRequirement> requirements = ImmutableSet.of(eurChf, chfEur);
    ImmutableSet<MarketDataRequirement> marketDataRequirements = ImmutableSet.<MarketDataRequirement>of(eurChf, chfEur);
    MarketDataFilter filter = new CurrencyPairFilter(Currency.EUR, Currency.CHF);
    Perturbation perturbation = FxRateShift.absolute(0.1);
    SinglePerturbationMapping mapping =
        SinglePerturbationMapping.builder()
            .filter(filter)
            .perturbation(perturbation)
            .build();
    List<SinglePerturbationMapping> mappings = ImmutableList.of(mapping);
    CyclePerturbations perturbations = new CyclePerturbations(marketDataRequirements, mappings);

    MarketDataSource marketDataSource = mock(MarketDataSource.class);
    Map<SingleValueRequirement, Result<?>> values =
        builder.buildSingleValues(bundle, ZonedDateTime.now(), requirements, marketDataSource, perturbations);

    Result<?> eurChfResult = values.get(eurChf);
    assertFalse(eurChfResult.isSuccess());

    Result<?> chfEurResult = values.get(chfEur);
    assertFalse(chfEurResult.isSuccess());
  }

  /**
   * Tests applying a shift defined using an inverse currency pair compared to the market data.
   * The underlying market data is the rate for GBP/USD and the shift is defined to apply to USD/GBP
   */
  public void shiftInverse() {
    SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    ExternalId rateId = ExternalId.of("x", "GBPUSD");
    ValueRequirement valueReq = new ValueRequirement(MARKET_VALUE.getName(), ComputationTargetType.PRIMITIVE, rateId);
    matrix.setLiveData(Currency.GBP, Currency.USD, valueReq);
    FxRateMarketDataBuilder builder = new FxRateMarketDataBuilder(ConfigLink.<CurrencyMatrix>resolved(matrix));
    MarketDataSource marketDataSource = mock(MarketDataSource.class);
    MarketDataBundle bundle =
        new MarketDataEnvironmentBuilder()
            .add(RawId.of(rateId.toBundle()), GBPUSD_RATE)
            .valuationTime(ZonedDateTime.now())
            .build()
            .toBundle();

    SingleValueRequirement gbpUsd = singleValueRequirement("GBP/USD");
    SingleValueRequirement usdGbp = singleValueRequirement("USD/GBP");
    Set<SingleValueRequirement> requirements = ImmutableSet.of(gbpUsd, usdGbp);
    Set<MarketDataRequirement> marketDataRequirements = ImmutableSet.<MarketDataRequirement>of(gbpUsd, usdGbp);
    // the shift is defined for the inverse pair of the raw market data
    MarketDataFilter filter = new CurrencyPairFilter(Currency.USD, Currency.GBP);
    Perturbation perturbation = FxRateShift.absolute(0.1);
    SinglePerturbationMapping mapping =
        SinglePerturbationMapping.builder()
            .filter(filter)
            .perturbation(perturbation)
            .build();
    List<SinglePerturbationMapping> mappings = ImmutableList.of(mapping);
    CyclePerturbations perturbations = new CyclePerturbations(marketDataRequirements, mappings);
    Map<SingleValueRequirement, Result<?>> values =
        builder.buildSingleValues(bundle, ZonedDateTime.now(), requirements, marketDataSource, perturbations);

    Result<?> gbpUsdResult = values.get(gbpUsd);
    assertSuccess(gbpUsdResult);
    assertEquals(1 / ((1 / GBPUSD_RATE) + 0.1), (Double) gbpUsdResult.getValue(), DELTA);

    Result<?> usdGbpResult = values.get(usdGbp);
    assertSuccess(usdGbpResult);
    assertEquals((1 / GBPUSD_RATE) + 0.1, (Double) usdGbpResult.getValue(), DELTA);
  }
}
