/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.bloomberg.loader;

import static com.google.common.collect.Sets.newHashSet;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.loader.hts.BloombergHistoricalTimeSeriesLoader;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.ShutdownUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.Tenor;

/**
 * Example code to load a very simple vanilla FX option portfolio.
 * <p>
 * This code is kept deliberately as simple as possible.
 * There are no checks for the securities or portfolios already existing, so if you run it
 * more than once you will get multiple copies portfolios and securities with the same names.
 * It is designed to run against the HSQLDB example database.
 */
public class ExampleVanillaFxOptionPortfolioLoader extends AbstractTool<IntegrationToolContext> {

  /** Logger */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleFxForwardPortfolioLoader.class);
  /** The number of securities in this portfolio */
  private static final int SIZE = 200;
  /** The name of the portfolio */
  public static final String PORTFOLIO_NAME = "FX Vanilla Option Portfolio";
  /** The currency pairs */
  public static final UnorderedCurrencyPair[] CCYS = new UnorderedCurrencyPair[] {UnorderedCurrencyPair.of(Currency.USD, Currency.EUR)};
  /** The scheme used for an identifier */
  private static final String ID_SCHEME = "FX_VANILLA_OPTION_GENERATOR";
  /** The tenors */
  private static final Tenor[] TENORS = new Tenor[] {Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.FIVE_YEARS, Tenor.TEN_YEARS};
  /** Formats the rates in the security name */
  private static final DecimalFormat FORMATTER = new DecimalFormat("##.###");

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    try {
      boolean success =
        new ExampleTimeSeriesRatingLoader().initAndRun(args, IntegrationToolContext.class) &&
        new ExampleVanillaFxOptionPortfolioLoader().initAndRun(args, IntegrationToolContext.class);
      ShutdownUtils.exit(success ? 0 : -1);
    } catch (Throwable ex) {
      ex.printStackTrace();
      ShutdownUtils.exit(-2);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    final Collection<FXOptionSecurity> fxOptions = createRandomFXOptions();
    if (fxOptions.size() == 0) {
      throw new OpenGammaRuntimeException("No valid vanilla FX options were generated");
    }
    persistToPortfolio(fxOptions, PORTFOLIO_NAME);
  }

  private Collection<FXOptionSecurity> createRandomFXOptions() {
    fetch(CCYS);
    final Collection<FXOptionSecurity> fxOptions = new ArrayList<>();
    final Random random = new Random(1235);
    for (int i = 0; i < SIZE; i++) {
      FXOptionSecurity fxOption = null;
      try {
        final UnorderedCurrencyPair ccys = CCYS[random.nextInt(CCYS.length)];
        final LocalDate tradeDate = getTradeDate(random, ccys.getFirstCurrency());
        final Tenor tenor = TENORS[random.nextInt(TENORS.length)];
        fxOption = makeFXOption(random, ccys, tradeDate, tenor);
      } catch (final Exception e) {
        e.printStackTrace();
      }
      if (fxOption != null) {
        fxOptions.add(fxOption);
      }
    }
    final StringBuilder sb = new StringBuilder();
    sb.append("Parsed ").append(fxOptions.size()).append(" FX options:\n");
    for (final FXOptionSecurity fxOption : fxOptions) {
      sb.append("\t").append(fxOption.getName()).append("\n");
    }
    s_logger.info(sb.toString());
    return fxOptions;
  }

  private void fetch(final UnorderedCurrencyPair[] ccys) {
    final Set<ExternalId> externalIds = newHashSet();
    for (final UnorderedCurrencyPair pair : ccys) {
      final List<ExternalId> tickers = getFXRateFor(pair);
      externalIds.addAll(tickers);
    }
    final BloombergHistoricalTimeSeriesLoader loader = new BloombergHistoricalTimeSeriesLoader(
      getToolContext().getHistoricalTimeSeriesMaster(),
      getToolContext().getHistoricalTimeSeriesProvider(),
      new BloombergIdentifierProvider(getToolContext().getBloombergReferenceDataProvider()));
    loader.loadTimeSeries(externalIds, "UNKNOWN", "PX_LAST", LocalDate.now().minusYears(1), LocalDate.now());
  }

  private static List<ExternalId> getFXRateFor(final UnorderedCurrencyPair ccys) {
    final Currency firstCurrency = ccys.getFirstCurrency();
    final Currency secondCurrency = ccys.getSecondCurrency();
    final List<ExternalId> tickers = new ArrayList<>();
    tickers.add(ExternalSchemes.bloombergTickerSecurityId(firstCurrency.getCode() + " Curncy"));
    tickers.add(ExternalSchemes.bloombergTickerSecurityId(secondCurrency.getCode() + " Curncy"));
    tickers.add(ExternalSchemes.bloombergTickerSecurityId(firstCurrency.getCode() + secondCurrency.getCode() + " Curncy"));
    tickers.add(ExternalSchemes.bloombergTickerSecurityId(secondCurrency.getCode() + firstCurrency.getCode() + " Curncy"));
    return tickers;
  }

  private FXOptionSecurity makeFXOption(final Random random, final UnorderedCurrencyPair ccys, final LocalDate tradeDate, final Tenor maturity) {
    final ZonedDateTime expiryDate = tradeDate.plus(maturity.getPeriod()).atStartOfDay().atZone(ZoneOffset.UTC);
    final double putAmount, callAmount;
    final Currency putCurrency, callCurrency;
    final double strike;
    if (random.nextBoolean()) {
      putCurrency = ccys.getFirstCurrency();
      callCurrency = ccys.getSecondCurrency();
      final double spot = getSpotRate(CurrencyPair.of(putCurrency, callCurrency), tradeDate);
      putAmount = 10000000 * (1 + random.nextInt(10)) / 100;
      callAmount = putAmount * spot * (1 + random.nextDouble() / 20);
      strike = putAmount / callAmount;
    } else {
      callCurrency = ccys.getFirstCurrency();
      putCurrency = ccys.getSecondCurrency();
      final double spot = getSpotRate(CurrencyPair.of(callCurrency, putCurrency), tradeDate);
      callAmount = 10000000 * (1 + random.nextInt(10)) / 100;
      putAmount = callAmount * spot * (1 + random.nextDouble() / 20);
      strike = putAmount / callAmount;
    }
    final StringBuilder sb = new StringBuilder();
    sb.append(expiryDate.toLocalDate());
    sb.append(" ");
    sb.append(putCurrency);
    sb.append("/");
    sb.append(callCurrency);
    sb.append(" @ ");
    sb.append(FORMATTER.format(strike));
    final FXOptionSecurity fxOption = new FXOptionSecurity(putCurrency, callCurrency, putAmount, callAmount, new Expiry(expiryDate), expiryDate.plusDays(2),
        random.nextBoolean(), new EuropeanExerciseType());
    fxOption.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    fxOption.setName(sb.toString());
    return fxOption;
  }

  private LocalDate getTradeDate(final Random random, final Currency ccy) {
    final HolidaySource holidaySource = getToolContext().getHolidaySource();
    LocalDate tradeDate;
    do {
      tradeDate = DateUtils.previousWeekDay().minusDays(random.nextInt(30));
    } while (holidaySource.isHoliday(tradeDate, ccy));
    return tradeDate;
  }

  private Double getSpotRate(final CurrencyPair ccys, final LocalDate tradeDate) {
    final HistoricalTimeSeriesSource historicalSource = getToolContext().getHistoricalTimeSeriesSource();
    final String ticker = ccys.getBase().getCode() + ccys.getCounter().getCode() + " Curncy";
    final ExternalIdBundle idBundle = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId(ticker));
    final HistoricalTimeSeries spotSeries = historicalSource.getHistoricalTimeSeries("PX_LAST",
        idBundle, HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME, tradeDate.minusDays(30), true, tradeDate, true);
    if (spotSeries == null) {
      throw new OpenGammaRuntimeException("Could not get spot rate series for " + ticker);
    }
    if (spotSeries.getTimeSeries().isEmpty()) {
      throw new OpenGammaRuntimeException("Series for " + ticker + " was empty");
    }
    return spotSeries.getTimeSeries().getLatestValue();
  }

  private void persistToPortfolio(final Collection<FXOptionSecurity> fxOptions, final String portfolioName) {
    final PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    final PositionMaster positionMaster = getToolContext().getPositionMaster();
    final SecurityMaster securityMaster = getToolContext().getSecurityMaster();

    final ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);
    final ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName, rootNode);
    final PortfolioDocument portfolioDoc = new PortfolioDocument();
    portfolioDoc.setPortfolio(portfolio);

    for (final FXOptionSecurity fxOption : fxOptions) {
      final SecurityDocument fxOptionToAddDoc = new SecurityDocument();
      fxOptionToAddDoc.setSecurity(fxOption);
      securityMaster.add(fxOptionToAddDoc);
      final ManageablePosition fxOptionPosition = new ManageablePosition(BigDecimal.ONE, fxOption.getExternalIdBundle());
      final PositionDocument addedDoc = positionMaster.add(new PositionDocument(fxOptionPosition));
      rootNode.addPosition(addedDoc.getUniqueId());
    }
    portfolioMaster.add(portfolioDoc);
  }
}
