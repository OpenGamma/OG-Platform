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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.financial.analytics.curve.exposure.CurrencyExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.sesame.CurveSelector;
import com.opengamma.sesame.CurveSelectorMulticurveBundleFn;
import com.opengamma.sesame.DefaultFixingsFn;
import com.opengamma.sesame.DefaultForwardCurveFn;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.FixingsFn;
import com.opengamma.sesame.ForwardCurveFn;
import com.opengamma.sesame.IssuerProviderBundle;
import com.opengamma.sesame.IssuerProviderFn;
import com.opengamma.sesame.LookupIssuerProviderFn;
import com.opengamma.sesame.MarketExposureSelector;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.bondfutureoption.BlackBondFuturesProviderFn;
import com.opengamma.sesame.bondfutureoption.BlackExpStrikeBondFuturesProviderFn;
import com.opengamma.sesame.bondfutureoption.BondFutureOptionBlackCalculatorFactory;
import com.opengamma.sesame.bondfutureoption.BondFutureOptionCalculatorFactory;
import com.opengamma.sesame.bondfutureoption.BondFutureOptionFn;
import com.opengamma.sesame.bondfutureoption.DefaultBondFutureOptionFn;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.equity.StaticReplicationDataBundleFn;
import com.opengamma.sesame.equity.StrikeDataBundleFn;
import com.opengamma.sesame.equityindexoptions.DefaultEquityIndexOptionFn;
import com.opengamma.sesame.equityindexoptions.EquityIndexOptionFn;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.ForwardCurveId;
import com.opengamma.sesame.marketdata.HistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.IssuerMulticurveId;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.MulticurveId;
import com.opengamma.sesame.marketdata.VolatilitySurfaceId;
import com.opengamma.sesame.trade.BondFutureOptionTrade;
import com.opengamma.sesame.trade.EquityIndexOptionTrade;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Utility class for Bond Future Options views
 */
public final class BondFutureOptionViewUtils {

  private BondFutureOptionViewUtils() { /* private constructor */ }
  private static final Logger s_logger = LoggerFactory.getLogger(BondFutureOptionViewUtils.class);
  private static ExternalId BOND_ID = ExternalSchemes.isinSecurityId("10Y JGB");
  private static ExternalId BOND_FUTURE_ID = ExternalSchemes.isinSecurityId("JBU5");
  private static ExternalId BOND_FUTURE_OPTION_ID = ExternalSchemes.isinSecurityId("JBN_146_5");
  private static String JP_NAME = "JP GOVT";

  /**
   * Utility for creating a Bond Future Options specific view column
   * @param currencies
   */
  public static ViewConfig createViewConfig(Collection<String> currencies) {


    return
        configureView(
            "Bond Future Options View",
            config(
                arguments(
                    function(
                        MarketExposureSelector.class,
                        argument("exposureFunctions", ConfigLink.resolved(createExposureFunction(currencies))))),
                implementations(
                    BlackBondFuturesProviderFn.class, BlackExpStrikeBondFuturesProviderFn.class,
                    BondFutureOptionFn.class, DefaultBondFutureOptionFn.class,
                    BondFutureOptionCalculatorFactory.class, BondFutureOptionBlackCalculatorFactory.class,
                    FixingsFn.class, DefaultFixingsFn.class,
                    IssuerProviderFn.class, LookupIssuerProviderFn.class,
                    CurveSelector.class, MarketExposureSelector.class,
                    HistoricalMarketDataFn.class, DefaultHistoricalMarketDataFn.class)),
            column(OutputNames.PRESENT_VALUE, output(OutputNames.PRESENT_VALUE, BondFutureOptionTrade.class)),
            column(OutputNames.DELTA, output(OutputNames.DELTA, BondFutureOptionTrade.class)),
            column(OutputNames.GAMMA, output(OutputNames.GAMMA, BondFutureOptionTrade.class)),
            column(OutputNames.VEGA, output(OutputNames.VEGA, BondFutureOptionTrade.class)),
            column(OutputNames.THETA, output(OutputNames.THETA, BondFutureOptionTrade.class))
        );
  }

  /**
   * Create discounting curves and add to the {@link MarketDataEnvironmentBuilder}
   * @param builder the MarketDataEnvironmentBuilder
   * @param discountingFile name of the discounting curves file
   * @param issuerFile name of the issuer curves file
   * @throws IOException
   */
  public static void parseCurves(MarketDataEnvironmentBuilder builder, String discountingFile, String issuerFile) throws IOException {
    String bundleName = "MultiCurve";
    //MulticurveProviderDiscount multicurve = new MulticurveProviderDiscount();
    //Map<String, CurveUtils.CurveRawData> curves = CurveUtils.parseCurves(discountingFile);
    //for(Map.Entry<String, CurveUtils.CurveRawData> curve : curves.entrySet()) {
    //  YieldAndDiscountCurve yieldCurve = CurveUtils.createYieldCurve(curve.getKey() + " Discounting", curve.getValue());
    //  multicurve.setCurve(Currency.of(curve.getKey()), yieldCurve);
    //
    //}
    //MulticurveBundle bundle = new MulticurveBundle(multicurve, new CurveBuildingBlockBundle());

    //TODO Issuer


    builder.add(IssuerMulticurveId.of(bundleName), createIssuerBundle());
  }

  /**
   * Create volatility surfaces and add to the {@link MarketDataEnvironmentBuilder}
   * @param builder the MarketDataEnvironmentBuilder
   * @param file the name of the volatility surface file
   * @throws IOException
   */
  public static void parseVolatilitySurfaces(MarketDataEnvironmentBuilder builder, String file) throws IOException {
    //Map<String, VolUtils.VolRawData> vols = VolUtils.parseVols(file);
    //for(Map.Entry<String, VolUtils.VolRawData> surface : vols.entrySet()) {
    //  builder.add(VolatilitySurfaceId.of(surface.getKey()), VolUtils.createVolatilitySurface(surface.getValue()));
    //}

    builder.add(VolatilitySurfaceId.of("TOKYO"), createVolatilitySurface());
  }

  /**
   * Parse the input portfolio
   * @param file the name of the portfolio file
   * @param securityMaster
   * @return map of trade to currency
   * @throws IOException
   */
  public static HashMap<Object, String> parsePortfolio(String file, SecurityMaster securityMaster) throws IOException {

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


        //Trade


        //trades.put(new EquityIndexOptionTrade(trade), currency.getCode());

      }
    } catch (IOException e) {
      s_logger.error("Failed to parse trade data ", e);

    }
    securityMaster.add(new SecurityDocument(createGovernmentBondSecurity()));
    securityMaster.add(new SecurityDocument(createBondFutureSecurity()));
    trades.put(createBondFutureOptionTrade(), "JPY");
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


  //TODO remove static data

  private static BondFutureOptionTrade createBondFutureOptionTrade() {

    Counterparty counterparty = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "COUNTERPARTY"));
    BigDecimal tradeQuantity = BigDecimal.valueOf(1);
    LocalDate tradeDate = LocalDate.of(2000, 1, 1);
    OffsetTime tradeTime = OffsetTime.of(LocalTime.of(0, 0), ZoneOffset.UTC);
    SimpleTrade trade = new SimpleTrade(createBondFutureOptionSecurity(), tradeQuantity, counterparty, tradeDate, tradeTime);
    trade.setPremium(-10.0);
    trade.setPremiumCurrency(Currency.JPY);
    return new BondFutureOptionTrade(trade);
  }

  private static BondFutureOptionSecurity createBondFutureOptionSecurity() {

    String tradingExchange = "TOKYO";
    String settlementExchange = "";
    Expiry expiry = new Expiry(DateUtils.getUTCDate(2015, 6, 30));
    ExerciseType exerciseType = new EuropeanExerciseType();
    ExternalId underlyingId = BOND_FUTURE_ID;
    double pointValue = 1;
    Currency currency = Currency.JPY;
    double strike = 1.465;
    OptionType optionType = OptionType.PUT;
    boolean margined = false;
    BondFutureOptionSecurity security = new BondFutureOptionSecurity(tradingExchange, settlementExchange, expiry,
                                                                     exerciseType, underlyingId, pointValue, margined,
                                                                     currency, strike, optionType);
    security.setExternalIdBundle(BOND_FUTURE_OPTION_ID.toBundle());
    return security;
  }

  public static BondFutureSecurity createBondFutureSecurity() {

    Currency currency = Currency.JPY;

    ZonedDateTime deliveryDate = DateUtils.getUTCDate(2015, 9, 20);
    Expiry expiry = new Expiry(deliveryDate);
    String tradingExchange = "TOKYO";
    String settlementExchange = "";
    double unitAmount = 100_000_000;
    Collection<BondFutureDeliverable> basket = new ArrayList<>();
    BondFutureDeliverable bondFutureDeliverable =
        new BondFutureDeliverable(BOND_ID.toBundle(), 0.706302);
    basket.add(bondFutureDeliverable);

    ZonedDateTime firstDeliveryDate = deliveryDate;
    ZonedDateTime lastDeliveryDate = deliveryDate;
    String category = "test";

    BondFutureSecurity security =  new BondFutureSecurity(expiry, tradingExchange, settlementExchange, currency, unitAmount, basket,
                                                          firstDeliveryDate, lastDeliveryDate, category);
    security.setExternalIdBundle(BOND_FUTURE_ID.toBundle());
    return security;
  }

  public static BondSecurity createGovernmentBondSecurity() {

    String issuerName = "JP GOVT";
    String issuerDomicile = "JP";
    String issuerType = "Sovereign";
    Currency currency = Currency.JPY;
    YieldConvention yieldConvention = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
    DayCount dayCountConvention = DayCounts.ACT_ACT_ISDA;

    Period couponPeriod = Period.parse("P6M");
    String couponType = "Fixed";
    double couponRate = 0.80; //TODO % or bp
    Frequency couponFrequency = PeriodFrequency.of(couponPeriod);

    ZonedDateTime maturityDate = DateUtils.getUTCDate(2022, 9, 20);
    ZonedDateTime firstCouponDate = DateUtils.getUTCDate(2013, 3, 20);
    ZonedDateTime interestAccrualDate = DateUtils.getUTCDate(2012, 9, 20);
    ZonedDateTime settlementDate = DateUtils.getUTCDate(2022, 9, 20);
    Expiry lastTradeDate = new Expiry(maturityDate);

    double issuancePrice = 100.0;
    double totalAmountIssued = 23499000000.0;
    double minimumAmount = 0.01;
    double minimumIncrement = 0.01;
    double parAmount = 100;
    double redemptionValue = 100;

    GovernmentBondSecurity bond =
        new GovernmentBondSecurity(issuerName, issuerType, issuerDomicile, issuerType, currency, yieldConvention,
                                   lastTradeDate, couponType, couponRate, couponFrequency, dayCountConvention,
                                   interestAccrualDate, settlementDate, firstCouponDate, issuancePrice,
                                   totalAmountIssued, minimumAmount, minimumIncrement, parAmount, redemptionValue);
    bond.setExternalIdBundle(BOND_ID.toBundle());
    return bond;
  }

  private static IssuerProviderBundle createIssuerBundle() {
    Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> issuer = new LinkedHashMap<>();
    Pair<Object, LegalEntityFilter<LegalEntity>> key =
        Pairs.<Object, LegalEntityFilter<LegalEntity>>of(JP_NAME, new LegalEntityShortName());
    issuer.put(key, createIssuerCurve());
    return new IssuerProviderBundle(new IssuerProviderDiscount(createDiscountingCurve(), issuer),
                                    new CurveBuildingBlockBundle());
  }

  private static YieldAndDiscountCurve createIssuerCurve() {
    String name = "JPY-JP-GOVT";
    Interpolator1D linearFlat =
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR,
                                                                Interpolator1DFactory.FLAT_EXTRAPOLATOR,
                                                                Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    double[] time = {0.5, 1.0, 2.0, 5.0, 7.0, 10.0, 20.0};
    double[] zc = {-0.0001, -0.0002, -0.0002, 0.0012, 0.0030, 0.0045, 0.0115};
    InterpolatedDoublesCurve curve =
        new InterpolatedDoublesCurve(time, zc, linearFlat, true, name);
    return new YieldCurve(name, curve);
  }

  private static MulticurveProviderDiscount createDiscountingCurve() {
    String name = "JPY Discounting";
    Interpolator1D linearFlat =
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR,
                                                                Interpolator1DFactory.FLAT_EXTRAPOLATOR,
                                                                Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    double[] time = {0.003, 0.25, 0.5, 1.0, 2.0, 5.0, 10.0};
    double[] zc = {0.0006, 0.0006, 0.0006, 0.0006, 0.0007, 0.0020, 0.0050};
    InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(time, zc, linearFlat, true, name);

    MulticurveProviderDiscount multicurve = new MulticurveProviderDiscount();
    multicurve.setCurve(Currency.JPY, new YieldCurve(name, curve));
    return multicurve;
  }

  private static VolatilitySurface createVolatilitySurface() {
    Interpolator1D linearFlat =
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR,
                                                                Interpolator1DFactory.FLAT_EXTRAPOLATOR,
                                                                Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    GridInterpolator2D interpolator2D = new GridInterpolator2D(linearFlat, linearFlat);
    InterpolatedDoublesSurface surface = InterpolatedDoublesSurface.from(
        new double[] {19.0/365.0, 19.0/365.0, 19.0/365.0, 19.0/365.0, 49.0/365.0, 49.0/365.0, 49.0/365.0, 49.0/365.0},
        new double[] {1.45, 1.46, 1.47, 1.48, 1.45, 1.46, 1.47, 1.48},
        new double[] {0.035, 0.032, 0.031, 0.028, 0.0325, 0.0315, 0.0305, 0.0295},
        interpolator2D
    );
    return new VolatilitySurface(surface);
  }



}
