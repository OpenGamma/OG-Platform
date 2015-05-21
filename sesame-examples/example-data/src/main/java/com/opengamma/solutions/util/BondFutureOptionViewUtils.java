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
import com.opengamma.financial.convention.daycount.DayCountFactory;
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
import com.opengamma.id.ExternalIdBundle;
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
import com.opengamma.sesame.bondfuture.BondFutureCalculatorFactory;
import com.opengamma.sesame.bondfuture.BondFutureFn;
import com.opengamma.sesame.bondfuture.DefaultBondFutureFn;
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
import com.opengamma.sesame.trade.BondFutureTrade;
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
  private static String CURVE_BUNDLE = "CurveBundle";

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
            column(OutputNames.SECURITY_MODEL_PRICE, output(OutputNames.SECURITY_MODEL_PRICE, BondFutureOptionTrade.class)),
            column(OutputNames.DELTA, output(OutputNames.DELTA, BondFutureOptionTrade.class)),
            column(OutputNames.GAMMA, output(OutputNames.GAMMA, BondFutureOptionTrade.class)),
            column(OutputNames.VEGA, output(OutputNames.VEGA, BondFutureOptionTrade.class)),
            column(OutputNames.THETA, output(OutputNames.THETA, BondFutureOptionTrade.class)),
            column(OutputNames.PV01, output(OutputNames.PV01, BondFutureOptionTrade.class))
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
    MulticurveProviderDiscount multicurve = new MulticurveProviderDiscount();
    Map<String, CurveUtils.CurveRawData> discountingCurves = CurveUtils.parseCurves(discountingFile);
    for(Map.Entry<String, CurveUtils.CurveRawData> curve : discountingCurves.entrySet()) {
      YieldAndDiscountCurve yieldCurve = CurveUtils.createYieldCurve(curve.getKey() + " Discounting", curve.getValue());
      multicurve.setCurve(Currency.of(curve.getKey()), yieldCurve);
    }

    Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> issuer = new LinkedHashMap<>();
    Map<String, CurveUtils.CurveRawData> issuerCurves = CurveUtils.parseCurves(issuerFile);
    for(Map.Entry<String, CurveUtils.CurveRawData> curve : issuerCurves.entrySet()) {
      YieldAndDiscountCurve yieldCurve = CurveUtils.createIssuerCurve(curve.getKey(), curve.getValue());
      Pair<Object, LegalEntityFilter<LegalEntity>> key = Pairs.<Object, LegalEntityFilter<LegalEntity>>of(curve.getKey(), new LegalEntityShortName());
      issuer.put(key, yieldCurve);
    }

    IssuerProviderBundle issuerProviderBundle = new IssuerProviderBundle(new IssuerProviderDiscount(multicurve, issuer),
                                                                         new CurveBuildingBlockBundle());
    builder.add(IssuerMulticurveId.of(CURVE_BUNDLE), issuerProviderBundle);
  }

  /**
   * Create volatility surfaces and add to the {@link MarketDataEnvironmentBuilder}
   * @param builder the MarketDataEnvironmentBuilder
   * @param file the name of the volatility surface file
   * @throws IOException
   */
  public static void parseVolatilitySurfaces(MarketDataEnvironmentBuilder builder, String file) throws IOException {
    Map<String, VolUtils.VolRawData> vols = VolUtils.parseVols(file);
    for(Map.Entry<String, VolUtils.VolRawData> surface : vols.entrySet()) {
      builder.add(VolatilitySurfaceId.of(surface.getKey()), VolUtils.createVolatilitySurface(surface.getValue()));
    }
  }

  /**
   * Parse the input portfolio
   * @param file the name of the portfolio file
   * @param securityMaster  to persists the security
   * @return map of trade to currency
   * @throws IOException
   */
  public static HashMap<Object, String> parseBondFutureOptions(String file, SecurityMaster securityMaster) throws IOException {

    HashMap<Object, String> trades = new HashMap<>();
    Reader reader = new BufferedReader(
        new InputStreamReader(
            new ClassPathResource(file).getInputStream()
        )
    );

    try {
      CSVReader csvReader = new CSVReader(reader);
      String[] line;
      csvReader.readNext(); // skip headers
      while ((line = csvReader.readNext()) != null) {

        //Security
        ExternalId underlyingId = ExternalSchemes.isinSecurityId(line[0]);
        OptionType optionType = OptionType.parse(line[1]);
        ExerciseType exerciseType = ExerciseType.of(line[2]);
        Currency currency = Currency.parse(line[3]);
        double strike = Double.parseDouble(line[4]);
        Expiry expiry = new Expiry(ZonedDateTime.of(LocalDate.parse(line[5]),
                                                    LocalTime.of(0, 0),
                                                    ZoneId.of("UTC")));
        double pointValue = 1;
        String exchange = currency.getCode();
        boolean margined = false;
        BondFutureOptionSecurity security = new BondFutureOptionSecurity(exchange, exchange, expiry,
                                                                         exerciseType, underlyingId, pointValue, margined,
                                                                         currency, strike, optionType);
        security.setName(underlyingId.getValue() + " " +
                             optionType.toString() + " Option " +
                             expiry.getExpiry().toLocalDate().toString() + " " + strike);
        securityMaster.add(new SecurityDocument(security));

        //Trade
        Counterparty counterparty = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "COUNTERPARTY"));
        BigDecimal tradeQuantity = BigDecimal.valueOf(Long.parseLong(line[6]));
        LocalDate tradeDate = LocalDate.now();
        OffsetTime tradeTime = OffsetTime.of(LocalTime.of(0, 0), ZoneOffset.UTC);
        SimpleTrade trade = new SimpleTrade(security,
                                            tradeQuantity,
                                            counterparty,
                                            tradeDate,
                                            tradeTime);
        trade.setPremium(0.0);
        trade.setPremiumCurrency(currency);

        trades.put(new BondFutureOptionTrade(trade), currency.getCode());

      }
    } catch (IOException e) {
      s_logger.error("Failed to parse trade data ", e);

    }
    return trades;
  }

  /**
   * Parse the underlying bond futures
   * @param file the name of the underlying bond futures file
   * @param securityMaster to persists the security
   * @throws IOException
   */
  public static HashMap<Object, String> parseBondFutures(String file, SecurityMaster securityMaster) throws IOException {

    HashMap<Object, String> trades = new HashMap<>();
    Reader reader = new BufferedReader(
        new InputStreamReader(
            new ClassPathResource(file).getInputStream()
        )
    );

    try {
      CSVReader csvReader = new CSVReader(reader);
      String[] line;
      csvReader.readNext(); // skip headers
      while ((line = csvReader.readNext()) != null) {

        Currency currency = Currency.of(line[3]);
        Expiry expiry = new Expiry(ZonedDateTime.of(LocalDate.parse(line[4]), LocalTime.of(0, 0), ZoneId.of("UTC")));
        String tradingExchange = currency.getCode();
        String settlementExchange = currency.getCode();
        double unitAmount = Double.parseDouble(line[7]);
        Collection<BondFutureDeliverable> basket = new ArrayList<>();
        ExternalId id = ExternalSchemes.isinSecurityId(line[1]);
        ExternalIdBundle underlying = id.toBundle();
        double conversion = Double.parseDouble(line[2]);
        BondFutureDeliverable bondFutureDeliverable = new BondFutureDeliverable(underlying, conversion);
        basket.add(bondFutureDeliverable);
        ZonedDateTime firstDeliveryDate = ZonedDateTime.of(LocalDate.parse(line[5]), LocalTime.of(0, 0), ZoneId.of("UTC"));
        ZonedDateTime lastDeliveryDate = ZonedDateTime.of(LocalDate.parse(line[6]), LocalTime.of(0, 0), ZoneId.of("UTC"));
        String category = currency.getCode();
        BondFutureSecurity security =  new BondFutureSecurity(expiry, tradingExchange, settlementExchange, currency, unitAmount, basket,
                                                              firstDeliveryDate, lastDeliveryDate, category);
        security.setExternalIdBundle(ExternalSchemes.isinSecurityId(line[0]).toBundle());
        security.setName(line[0] + " Bond Future on " + id.getValue() + " " +
                             expiry.getExpiry().toLocalDate().toString() + " " + unitAmount);
        securityMaster.add(new SecurityDocument(security));

        //Trade
        Counterparty counterparty = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "COUNTERPARTY"));
        BigDecimal tradeQuantity = BigDecimal.valueOf(1);
        LocalDate tradeDate = LocalDate.of(1900,1,1);
        OffsetTime tradeTime = OffsetTime.of(LocalTime.of(0, 0), ZoneOffset.UTC);
        SimpleTrade trade = new SimpleTrade(security,
                                            tradeQuantity,
                                            counterparty,
                                            tradeDate,
                                            tradeTime);
        trade.setPremium(0.0);
        trade.setPremiumCurrency(currency);

        trades.put(new BondFutureTrade(trade), currency.getCode());

      }
    } catch (IOException e) {
      s_logger.error("Failed to parse trade data ", e);
    }
    return trades;
  }

  /**
   * Parse the underlying bonds
   * @param file the name of the underlying bonds file
   * @param securityMaster to persists the security
   * @throws IOException
   */
  public static void parseBonds(String file, SecurityMaster securityMaster) throws IOException {

    Reader reader = new BufferedReader(
        new InputStreamReader(
            new ClassPathResource(file).getInputStream()
        )
    );

    try {
      CSVReader csvReader = new CSVReader(reader);
      String[] line;
      csvReader.readNext(); // skip headers
      while ((line = csvReader.readNext()) != null) {

        String issuerName = line[1];
        String issuerDomicile = line[2];
        String issuerType = "";
        Currency currency = Currency.of(line[3]);
        YieldConvention yieldConvention = YieldConventionFactory.INSTANCE.getYieldConvention(line[4]);
        DayCount dayCountConvention = DayCountFactory.of(line[5]);

        Period couponPeriod = Period.parse("P" + line[6]);
        String couponType = "";
        double couponRate = Double.parseDouble(line[7]);
        Frequency couponFrequency = PeriodFrequency.of(couponPeriod);

        ZonedDateTime maturityDate = ZonedDateTime.of(LocalDate.parse(line[8]), LocalTime.of(0, 0), ZoneId.of("UTC"));
        ZonedDateTime firstCouponDate = ZonedDateTime.of(LocalDate.parse(line[9]), LocalTime.of(0, 0), ZoneId.of("UTC"));
        ZonedDateTime accrualDate = ZonedDateTime.of(LocalDate.parse(line[10]), LocalTime.of(0, 0), ZoneId.of("UTC"));
        ZonedDateTime settlementDate = ZonedDateTime.of(LocalDate.parse(line[11]), LocalTime.of(0, 0), ZoneId.of("UTC"));
        Expiry lastTradeDate = new Expiry(maturityDate);

        double issuancePrice = 1.0;
        double totalAmountIssued = 1.0;
        double minimumAmount = 1.0;
        double minimumIncrement = 1.0;
        double parAmount = 1.0;
        double redemptionValue = 1.0;

        GovernmentBondSecurity security =
            new GovernmentBondSecurity(issuerName, issuerType, issuerDomicile, issuerType, currency, yieldConvention,
                                       lastTradeDate, couponType, couponRate, couponFrequency, dayCountConvention,
                                       accrualDate, settlementDate, firstCouponDate, issuancePrice,
                                       totalAmountIssued, minimumAmount, minimumIncrement, parAmount, redemptionValue);

        security.setExternalIdBundle(ExternalSchemes.isinSecurityId(line[0]).toBundle());
        securityMaster.add(new SecurityDocument(security));

      }
    } catch (IOException e) {
      s_logger.error("Failed to parse trade data ", e);

    }
  }

  private static ExposureFunctions createExposureFunction(Collection<String> currencies) {
    List<String> exposureFunctions = ImmutableList.of(CurrencyExposureFunction.NAME);
    ImmutableList<String> currencyList = ImmutableSet.copyOf(currencies).asList();
    Map<ExternalId, String> idsToNames = new HashMap<>();
    for (String currency : currencyList) {
      idsToNames.put(ExternalId.of("CurrencyISO", currency), CURVE_BUNDLE);
    }
    return new ExposureFunctions("Exposure", exposureFunctions, idsToNames);
  }


}
