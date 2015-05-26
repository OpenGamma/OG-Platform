/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.util;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.column;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.configureView;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;
import static com.opengamma.sesame.config.ConfigBuilder.output;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.financial.analytics.curve.exposure.CurrencyExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.sesame.CurveSelector;
import com.opengamma.sesame.CurveSelectorMulticurveBundleFn;
import com.opengamma.sesame.DefaultForwardCurveFn;
import com.opengamma.sesame.DefaultGridInterpolator2DFn;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.ForwardCurveFn;
import com.opengamma.sesame.GridInterpolator2DFn;
import com.opengamma.sesame.MarketExposureSelector;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.config.ConfigBuilder;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.equity.StaticReplicationDataBundleFn;
import com.opengamma.sesame.equity.StrikeDataBundleFn;
import com.opengamma.sesame.equity.StrikeDataFromPriceBundleFn;
import com.opengamma.sesame.equityindexoptions.DefaultEquityIndexOptionFn;
import com.opengamma.sesame.equityindexoptions.EquityIndexOptionFn;
import com.opengamma.sesame.marketdata.ForwardCurveId;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.MulticurveId;
import com.opengamma.sesame.marketdata.SurfaceId;
import com.opengamma.sesame.marketdata.VolatilitySurfaceId;
import com.opengamma.sesame.trade.EquityIndexOptionTrade;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Utility class for Equity Index Options views
 */
public final class EquityIndexOptionViewUtils {

  private EquityIndexOptionViewUtils() { /* private constructor */ }
  private static final Logger s_logger = LoggerFactory.getLogger(EquityIndexOptionViewUtils.class);

  /**
   * Utility for creating a Equity Index Options specific view column
   * @param currencies to be used in the creation of the exposure function
   * @param priceSurface whether the surface is price based (true) or vol based (false)
   */
  public static ViewConfig createViewConfig(Collection<String> currencies, boolean priceSurface) {


    ConfigBuilder.Implementations implementations;
    if (priceSurface) {
      implementations = implementations(
          GridInterpolator2DFn.class, DefaultGridInterpolator2DFn.class,
          EquityIndexOptionFn.class, DefaultEquityIndexOptionFn.class,
          StaticReplicationDataBundleFn.class, StrikeDataFromPriceBundleFn.class,
          CurveSelector.class, MarketExposureSelector.class,
          ForwardCurveFn.class, DefaultForwardCurveFn.class,
          DiscountingMulticurveCombinerFn.class, CurveSelectorMulticurveBundleFn.class);
    } else {
      implementations = implementations(
          EquityIndexOptionFn.class, DefaultEquityIndexOptionFn.class,
          StaticReplicationDataBundleFn.class, StrikeDataBundleFn.class,
          CurveSelector.class, MarketExposureSelector.class,
          ForwardCurveFn.class, DefaultForwardCurveFn.class,
          DiscountingMulticurveCombinerFn.class, CurveSelectorMulticurveBundleFn.class);
    }

    ViewConfig viewConfig = configureView(
        "Equity Index Options View",
        config(
            arguments(
                function(
                    MarketExposureSelector.class,
                    argument("exposureFunctions", ConfigLink.resolved(createExposureFunction(currencies)))),
                function(
                    DefaultGridInterpolator2DFn.class,
                    argument("xInterpolatorName", "Linear"),
                    argument("xLeftExtrapolatorName", "FlatExtrapolator"),
                    argument("xRightExtrapolatorName", "FlatExtrapolator"),
                    argument("yInterpolatorName", "Linear"),
                    argument("yLeftExtrapolatorName", "FlatExtrapolator"),
                    argument("yRightExtrapolatorName", "FlatExtrapolator"))),
            implementations),
        column(OutputNames.PRESENT_VALUE, output(OutputNames.PRESENT_VALUE, EquityIndexOptionTrade.class)),
        column(OutputNames.DELTA, output(OutputNames.DELTA, EquityIndexOptionTrade.class)),
        column(OutputNames.GAMMA, output(OutputNames.GAMMA, EquityIndexOptionTrade.class)),
        column(OutputNames.VEGA, output(OutputNames.VEGA, EquityIndexOptionTrade.class)),
        column(OutputNames.PV01, output(OutputNames.PV01, EquityIndexOptionTrade.class)),
        column(OutputNames.BUCKETED_PV01, output(OutputNames.BUCKETED_PV01, EquityIndexOptionTrade.class))
    );

    return viewConfig;
  }

  /**
   * Create discounting curves and add to the {@link MarketDataEnvironmentBuilder}
   * @param builder the MarketDataEnvironmentBuilder
   * @param file name of the discounting curves file
   * @throws IOException
   */
  public static void parseDiscountingCurves(MarketDataEnvironmentBuilder builder, String file) throws IOException {
    String bundleName = "MultiCurve";
    MulticurveProviderDiscount multicurve = new MulticurveProviderDiscount();
    Map<String, CurveUtils.CurveRawData> curves = CurveUtils.parseCurves(file);
    for(Map.Entry<String, CurveUtils.CurveRawData> curve : curves.entrySet()) {
      YieldAndDiscountCurve yieldCurve = CurveUtils.createYieldCurve(curve.getKey() + " Discounting", curve.getValue());
      multicurve.setCurve(Currency.of(curve.getKey()), yieldCurve);

    }
    MulticurveBundle bundle = new MulticurveBundle(multicurve, new CurveBuildingBlockBundle());
    builder.add(MulticurveId.of(bundleName), bundle);
  }

  /**
   * Create volatility surfaces and add to the {@link MarketDataEnvironmentBuilder}
   * @param builder the MarketDataEnvironmentBuilder
   * @param file the name of the surface file
   * @param priceSurface whether the surface is price based (true) or vol based (false)
   * @throws IOException
   */
  public static void parseSurfaces(MarketDataEnvironmentBuilder builder, String file, boolean priceSurface) throws IOException {
    if (priceSurface) {
      parsePriceSurfaces(builder, file);
    } else {
      parseVolatilitySurfaces(builder, file);
    }
  }

  /**
   * Create surfaces and add to the {@link MarketDataEnvironmentBuilder}
   * @param builder the MarketDataEnvironmentBuilder
   * @param file the name of the price surface file
   * @throws IOException
   */
  private static void parsePriceSurfaces(MarketDataEnvironmentBuilder builder, String file) throws IOException {
    Map<String, VolUtils.SurfaceRawData> vols = VolUtils.parseSurface(file);
    for(Map.Entry<String, VolUtils.SurfaceRawData> surface : vols.entrySet()) {
      builder.add(SurfaceId.of(surface.getKey()), VolUtils.createPriceSurface(surface.getValue()));
    }
  }

  /**
   * Create surfaces and add to the {@link MarketDataEnvironmentBuilder}
   * @param builder the MarketDataEnvironmentBuilder
   * @param file the name of the vol surface file
   * @throws IOException
   */
  private static void parseVolatilitySurfaces(MarketDataEnvironmentBuilder builder, String file) throws IOException {
    Map<String, VolUtils.SurfaceRawData> vols = VolUtils.parseSurface(file);
    for(Map.Entry<String, VolUtils.SurfaceRawData> surface : vols.entrySet()) {
      builder.add(VolatilitySurfaceId.of(surface.getKey()), VolUtils.createVolatilitySurface(surface.getValue()));
    }
  }



  /**
   * Create forward curves and add to the {@link MarketDataEnvironmentBuilder}
   * @param builder  the MarketDataEnvironmentBuilder
   * @param file the name of the forward curves file
   * @throws IOException
   */
  public static void parseForwardCurves(MarketDataEnvironmentBuilder builder, String file) throws IOException {
    Map<String, CurveUtils.CurveRawData> curves = CurveUtils.parseCurves(file);
    for(Map.Entry<String, CurveUtils.CurveRawData> curve : curves.entrySet()) {
      builder.add(ForwardCurveId.of(curve.getKey()), CurveUtils.createForwardCurve(curve.getValue()));
    }
  }

  /**
   * Parse the input portfolio
   * @param file the name of the portfolio file
   * @return map of trade to currency
   * @throws IOException
   */
  public static HashMap<Object, String> parsePortfolio(String file) throws IOException {

    HashMap<Object, String> trades = new HashMap<>();
    Reader curveReader = new BufferedReader(
        new InputStreamReader(
            new ClassPathResource(file).getInputStream()
        )
    );

    try {
      CSVReader csvReader = new CSVReader(curveReader);
      String[] line;
      csvReader.readNext(); // skip headers
      while ((line = csvReader.readNext()) != null) {

        //Security
        ExternalId underlyingId = ExternalId.of("TICKER", line[0]);
        OptionType optionType = OptionType.parse(line[1]);
        ExerciseType exerciseType = ExerciseType.of(line[2]);
        Currency currency = Currency.parse(line[3]);
        double strike = Double.parseDouble(line[4]);
        Expiry expiry = new Expiry(ZonedDateTime.of(LocalDate.parse(line[5]),
                                                    LocalTime.of(0, 0),
                                                    ZoneId.of("UTC")));
        double pointValue = 1;
        String exchange = currency.getCode();

        EquityIndexOptionSecurity security = new EquityIndexOptionSecurity(optionType,
                                                                           strike,
                                                                           currency,
                                                                           underlyingId,
                                                                           exerciseType,
                                                                           expiry,
                                                                           pointValue,
                                                                           exchange);
        security.setName(underlyingId.getValue() + " " +
                             optionType.toString() + " Option " +
                             expiry.getExpiry().toLocalDate().toString() + " " + strike);

        //Trade
        Counterparty counterparty = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "COUNTERPARTY"));
        BigDecimal tradeQuantity = BigDecimal.valueOf(1);
        LocalDate tradeDate = LocalDate.now();
        OffsetTime tradeTime = OffsetTime.of(LocalTime.of(0, 0), ZoneOffset.UTC);
        SimpleTrade trade = new SimpleTrade(security,
                                            tradeQuantity,
                                            counterparty,
                                            tradeDate,
                                            tradeTime);

        trades.put(new EquityIndexOptionTrade(trade), currency.getCode());

      }
    } catch (IOException e) {
      s_logger.error("Failed to parse trade data ", e);

    }
    return trades;
  }

  private static ExposureFunctions createExposureFunction(Collection<String> currencies) {
    List<String> exposureFunctions = ImmutableList.of(CurrencyExposureFunction.NAME);
    ImmutableList<String> currencyList = ImmutableSet.copyOf(currencies).asList();
    Map<ExternalId, String> idsToNames = new HashMap<>();
    for (String currency : currencyList) {
      idsToNames.put(ExternalId.of("CurrencyISO", currency), "MultiCurve");
    }
    return new ExposureFunctions("Exposure", exposureFunctions, idsToNames);
  }


}
