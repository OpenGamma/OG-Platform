/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.builders;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;

import java.util.List;

import org.threeten.bp.Period;

import com.google.common.collect.ImmutableList;
import com.opengamma.analytics.financial.provider.curve.issuer.IssuerDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.financial.analytics.curve.ConfigDBCurveSpecificationBuilder;
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.id.VersionCorrection;
import com.opengamma.sesame.CurveNodeConverterFn;
import com.opengamma.sesame.DefaultCurveNodeConverterFn;
import com.opengamma.sesame.component.RetrievalPeriod;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.FxMatrixMarketDataBuilder;
import com.opengamma.sesame.marketdata.HistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.IssuerMulticurveMarketDataBuilder;
import com.opengamma.sesame.marketdata.MulticurveMarketDataBuilder;

/**
 * Helper class for building the standard set of {@link MarketDataBuilder} implementations.
 */
public class MarketDataBuilders {

  private MarketDataBuilders() {
  }

  /**
   * Creates a builder for raw data which requests data from a data source or historical time series.
   *
   * @param componentMap singleton components supplied by the system
   * @param timeSeriesDataSource the name of the data source used for looking up time series of historical data
   * @return a builder for raw market data
   */
  public static RawMarketDataBuilder raw(ComponentMap componentMap, String timeSeriesDataSource) {
    HistoricalTimeSeriesSource timeSeriesSource = componentMap.getComponent(HistoricalTimeSeriesSource.class);
    return new RawMarketDataBuilder(timeSeriesSource, timeSeriesDataSource, null);
  }

  /**
   * Creates a builder for multicurve bundles.
   *
   * @param componentMap singleton components supplied by the system
   * @return a builder for multicurve bundles
   */
  public static MulticurveMarketDataBuilder multicurve(ComponentMap componentMap,
                                                       ConfigLink<CurrencyMatrix> currencyMatrixLink) {
    FunctionModelConfig config =
        config(
            arguments(
                function(
                    DefaultCurveNodeConverterFn.class,
                    argument("timeSeriesDuration", RetrievalPeriod.of(Period.ofYears(1)))),
                function(
                    MulticurveDiscountBuildingRepository.class,
                    argument("toleranceAbs", 1e-10),
                    argument("toleranceRel", 1e-10),
                    argument("stepMaximum", 5000)),
                function(
                    ConfigDBCurveSpecificationBuilder.class,
                    argument("versionCorrection", VersionCorrection.LATEST))),
            implementations(
                CurveSpecificationBuilder.class, ConfigDBCurveSpecificationBuilder.class,
                CurveNodeConverterFn.class, DefaultCurveNodeConverterFn.class,
                HistoricalMarketDataFn.class, DefaultHistoricalMarketDataFn.class));

    return FunctionModel.build(MulticurveMarketDataBuilder.class, config, componentMap);
  }

  /**
   * Creates a builder for issuer multicurve bundles.
   *
   * @param componentMap singleton components supplied by the system
   * @return a builder for issuer multicurve bundles
   */
  public static IssuerMulticurveMarketDataBuilder issuerMulticurve(ComponentMap componentMap,
                                                                   ConfigLink<CurrencyMatrix> currencyMatrixLink) {
    FunctionModelConfig config =
        config(
            arguments(
                function(
                    DefaultCurveNodeConverterFn.class,
                    argument("timeSeriesDuration", RetrievalPeriod.of(Period.ofYears(1)))),
                function(
                    IssuerDiscountBuildingRepository.class,
                    argument("toleranceAbs", 1e-9),
                    argument("toleranceRel", 1e-9),
                    argument("stepMaximum", 1000)),
                function(
                    ConfigDBCurveSpecificationBuilder.class,
                    argument("versionCorrection", VersionCorrection.LATEST))),
            implementations(
                CurveSpecificationBuilder.class, ConfigDBCurveSpecificationBuilder.class,
                CurveNodeConverterFn.class, DefaultCurveNodeConverterFn.class,
                HistoricalMarketDataFn.class, DefaultHistoricalMarketDataFn.class));

    return FunctionModel.build(IssuerMulticurveMarketDataBuilder.class, config, componentMap);
  }

  /**
   * Creates a builder for security market data.
   *
   * @return a builder for security market data
   */
  public static SecurityMarketDataBuilder security() {
    return new SecurityMarketDataBuilder();
  }

  /**
   * Creates a builder for credit curve data.
   *
   * @return a builder for credit curve data
   */
  public static CreditCurveMarketDataBuilder creditCurve() {
    return new CreditCurveMarketDataBuilder();
  }

  /**
   * Creates a builder for isda yield curve data.
   *
   * @return a builder for isda yield curve data
   */
  public static IsdaYieldCurveMarketDataBuilder isdaYieldCurve() {
    return new IsdaYieldCurveMarketDataBuilder();
  }

  /**
   * Creates a builder for matrices of FX rates.
   *
   * @return a builder for matrices of FX rates
   */
  public static FxMatrixMarketDataBuilder fxMatrix() {
    return new FxMatrixMarketDataBuilder();
  }

  /**
   * Creates a builder for FX rates.
   *
   * @param currencyMatrixLink a link to a {@link CurrencyMatrix} defining how to look up or derive FX rates
   * @return a builder for FX rates
   */
  public static FxRateMarketDataBuilder fxRate(ConfigLink<CurrencyMatrix> currencyMatrixLink) {
    return new FxRateMarketDataBuilder(currencyMatrixLink);
  }

  /**
   * Creates the default builders for the built-in market data types.
   *
   * @param componentMap singleton components supplied by the system
   * @param timeSeriesDataSource the name of the data source used for looking up time series of historical data
   * @param currencyMatrixLink a link to a {@link CurrencyMatrix} defining how to look up or derive FX rates
   * @return the default builders for the built-in market data types
   */
  public static List<MarketDataBuilder> standard(ComponentMap componentMap,
                                                 String timeSeriesDataSource,
                                                 ConfigLink<CurrencyMatrix> currencyMatrixLink) {
    return ImmutableList.of(raw(componentMap, timeSeriesDataSource),
                            multicurve(componentMap, currencyMatrixLink),
                            issuerMulticurve(componentMap, currencyMatrixLink),
                            fxMatrix(),
                            fxRate(currencyMatrixLink),
                            security(),
                            creditCurve(),
                            isdaYieldCurve());
  }
}
