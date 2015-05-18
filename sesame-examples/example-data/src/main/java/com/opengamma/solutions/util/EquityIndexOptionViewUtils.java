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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.core.id.ExternalSchemes;
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
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.ForwardCurveFn;
import com.opengamma.sesame.MarketExposureSelector;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.equity.StaticReplicationDataBundleFn;
import com.opengamma.sesame.equity.StrikeDataBundleFn;
import com.opengamma.sesame.equityindexoptions.DefaultEquityIndexOptionFn;
import com.opengamma.sesame.equityindexoptions.EquityIndexOptionFn;
import com.opengamma.sesame.marketdata.ForwardCurveId;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.MulticurveId;
import com.opengamma.sesame.marketdata.VolatilitySurfaceId;
import com.opengamma.sesame.trade.EquityIndexOptionTrade;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Utility class for Equity Index Options views
 */
public final class EquityIndexOptionViewUtils {

  private EquityIndexOptionViewUtils() { /* private constructor */ }
  /**
   * Utility for creating a Equity Index Options specific view column
   */
  public static ViewConfig createViewConfig() {

    return
        configureView(
            "Equity Index Options View",
            config(
                arguments(
                    function(
                        MarketExposureSelector.class,
                        argument("exposureFunctions", ConfigLink.resolved(createExposureFunction())))),
                implementations(
                    EquityIndexOptionFn.class, DefaultEquityIndexOptionFn.class,
                    StaticReplicationDataBundleFn.class, StrikeDataBundleFn.class,
                    CurveSelector.class, MarketExposureSelector.class,
                    ForwardCurveFn.class, DefaultForwardCurveFn.class,
                    DiscountingMulticurveCombinerFn.class, CurveSelectorMulticurveBundleFn.class)),
            column(OutputNames.PRESENT_VALUE, output(OutputNames.PRESENT_VALUE, EquityIndexOptionTrade.class)),
            column(OutputNames.DELTA, output(OutputNames.DELTA, EquityIndexOptionTrade.class)),
            column(OutputNames.GAMMA, output(OutputNames.GAMMA, EquityIndexOptionTrade.class)),
            column(OutputNames.VEGA, output(OutputNames.VEGA, EquityIndexOptionTrade.class)),
            column(OutputNames.PV01, output(OutputNames.PV01, EquityIndexOptionTrade.class)),
            column(OutputNames.BUCKETED_PV01, output(OutputNames.BUCKETED_PV01, EquityIndexOptionTrade.class))
        );
  }

  // TODO create from discounting input
  private static ExposureFunctions createExposureFunction() {
    List<String> exposureFunctions =  ImmutableList.of(CurrencyExposureFunction.NAME);
    Map<ExternalId, String> idsToNames = new HashMap<>();
    idsToNames.put(ExternalId.of("CurrencyISO", "JPY"), "MultiCurve");
    return new ExposureFunctions("Exposure", exposureFunctions, idsToNames);
  }

  public static List<Object> parseTrades(String tradeFile) {
    return INPUTS;
  }

  public static void parseDiscountingCurves(MarketDataEnvironmentBuilder builder, String discountingCurves) throws IOException {
    String bundleName = "MultiCurve";
    MulticurveProviderDiscount multicurve = new MulticurveProviderDiscount();
    Map<String, CurveUtils.CurveRawData> curves = CurveUtils.parseCurves(discountingCurves);
    for(Map.Entry<String, CurveUtils.CurveRawData> curve : curves.entrySet()) {
      YieldAndDiscountCurve yieldCurve = CurveUtils.createYieldCurve(curve.getKey() + " Discounting", curve.getValue());
      multicurve.setCurve(Currency.of(curve.getKey()), yieldCurve);

    }
    MulticurveBundle bundle = new MulticurveBundle(multicurve, new CurveBuildingBlockBundle());
    builder.add(MulticurveId.of(bundleName), bundle);
  }

  public static void parseVolatilitySurfaces(MarketDataEnvironmentBuilder builder, String volatilitySurfaces) throws IOException {
    Map<String, VolUtils.VolRawData> vols = VolUtils.parseVols(volatilitySurfaces);
    for(Map.Entry<String, VolUtils.VolRawData> surface : vols.entrySet()) {
      builder.add(VolatilitySurfaceId.of(surface.getKey()), VolUtils.createVolatilitySurface(surface.getValue()));
    }
  }

  public static void parseForwardCurves(MarketDataEnvironmentBuilder builder, String forwardCurves) throws IOException {
    Map<String, CurveUtils.CurveRawData> curves = CurveUtils.parseCurves(forwardCurves);
    for(Map.Entry<String, CurveUtils.CurveRawData> curve : curves.entrySet()) {
      builder.add(ForwardCurveId.of(curve.getKey()), CurveUtils.createForwardCurve(curve.getValue()));
    }
  }


  //TODO REMOVE ALL STATIC DATA BELOW.....
  private static final ZonedDateTime VALUATION_TIME = DateUtils.getUTCDate(2014, 7, 22);
  private static final List<Object> INPUTS = ImmutableList.<Object>of(createOptionTrade());
  private static EquityIndexOptionTrade createOptionTrade() {
    Counterparty counterparty = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "COUNTERPARTY"));
    BigDecimal tradeQuantity = BigDecimal.valueOf(1);
    LocalDate tradeDate = LocalDate.of(2000, 1, 1);
    OffsetTime tradeTime = OffsetTime.of(LocalTime.of(0, 0), ZoneOffset.UTC);
    SimpleTrade trade = new SimpleTrade(createEquityIndexOptionSecurity(), tradeQuantity, counterparty, tradeDate, tradeTime);
    trade.setPremium(0.0);
    trade.setPremiumCurrency(Currency.JPY);
    return new EquityIndexOptionTrade(trade);
  }

  private static EquityIndexOptionSecurity createEquityIndexOptionSecurity() {
    OptionType optionType = OptionType.CALL;
    double strike = 10500;
    Currency currency = Currency.JPY;
    ExternalId underlyingId = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "NK225");
    ExerciseType exerciseType = ExerciseType.of("European");
    Expiry expiry = new Expiry(VALUATION_TIME.plusMonths(2));
    double pointValue = 1;
    String exchange = "XJPY";
    EquityIndexOptionSecurity security = new EquityIndexOptionSecurity(optionType,
                                                                       strike,
                                                                       currency,
                                                                       underlyingId,
                                                                       exerciseType,
                                                                       expiry,
                                                                       pointValue,
                                                                       exchange);
    security.setName(underlyingId.getValue() + " " + optionType.toString() + " Option " + expiry.getExpiry().toString());
    return security;
  }

}
