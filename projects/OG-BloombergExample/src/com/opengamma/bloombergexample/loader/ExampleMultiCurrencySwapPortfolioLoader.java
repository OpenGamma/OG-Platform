/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.loader;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.loader.BloombergHistoricalLoader;
import com.opengamma.bbg.tool.BloombergToolContext;
import com.opengamma.bloombergexample.tool.AbstractExampleTool;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.security.swap.*;
import com.opengamma.id.ExternalId;
import com.opengamma.master.config.impl.MasterConfigSource;
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
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Triple;

/**
 * Example code to load a very simple multicurrency swap portfolio.
 * <p>
 * This code is kept deliberately as simple as possible.
 * There are no checks for the securities or portfolios already existing, so if you run it
 * more than once you will get multiple copies portfolios and securities with the same names.
 * It is designed to run against the HSQLDB example database.
 */
public class ExampleMultiCurrencySwapPortfolioLoader extends AbstractExampleTool {

  /**
   * Logger.
   */
  private static Logger s_logger = LoggerFactory.getLogger(ExampleMultiCurrencySwapPortfolioLoader.class);

  /**
   * Size of securities in portfolio
   */
  private static final int SECURITIES_SIZE = 200;

  /**
   * The name of the portfolio.
   */
  public static final String PORTFOLIO_NAME = "Example MultiCurrency Swap Portfolio";

  /**
   * The scheme used for an identifier which is added to each swap created from the CSV file
   */
  private static final String ID_SCHEME = "SWAP_GENERATOR";

  /**
   * Portfolio currencies
   */
  public static final Currency[] s_currencies;

  private static final Tenor[] s_tenors;

  private static final int DAYS_TRADING = 60;

  static {
    s_currencies = new Currency[]{Currency.USD, Currency.GBP, Currency.EUR, Currency.JPY, Currency.CHF};
    //Currency.AUD, Currency.SEK, Currency.NOK };
    s_tenors = new Tenor[]{Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FIVE_YEARS,
      Tenor.ofYears(7), Tenor.ofYears(10), Tenor.ofYears(12), Tenor.ofYears(15), Tenor.ofYears(20)};
  }

  //-------------------------------------------------------------------------

  /**
   * Main method to run the tool.
   * No arguments are needed.
   *
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    new ExampleTimeSeriesRatingLoader().initAndRun(args);
    new ExampleMultiCurrencySwapPortfolioLoader().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    Collection<SwapSecurity> swaps = createRandomSwaps();
    if (swaps.size() == 0) {
      throw new OpenGammaRuntimeException("No (valid) swaps were generated.");
    }
    persistToPortfolio(swaps, PORTFOLIO_NAME);
  }

  private Collection<SwapSecurity> createRandomSwaps() {
    Collection<SwapSecurity> swaps = new ArrayList<SwapSecurity>();

    SecureRandom random = new SecureRandom();

    // Currency, TradeDate, Maturity
    List<Triple<Currency, LocalDate, Tenor>> swapsArgs = newArrayList();

    for (int i = 0; i < SECURITIES_SIZE; i++) {
      Currency ccy = getCurrency(random);
      LocalDate tradeDate = getTradeDate(random, ccy);
      Tenor maturity = s_tenors[random.nextInt(s_tenors.length)];
      swapsArgs.add(Triple.of(ccy, tradeDate, maturity));
    }

    fetch(swapsArgs);

    for (Triple<Currency, LocalDate, Tenor> swapArgs : swapsArgs) {
      SwapSecurity swap = null;
      try {
        swap = makeSwap(random, swapArgs.getFirst(), swapArgs.getSecond(), swapArgs.getThird());
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (swap != null) {
        swaps.add(swap);
      }
    }

    StringBuilder sb = new StringBuilder();
    sb.append("Parsed ").append(swaps.size()).append(" swaps:\n");
    for (SwapSecurity swap : swaps) {
      sb.append("\t").append(swap.getName()).append("\n");
    }
    s_logger.info(sb.toString());
    return swaps;
  }

  private void fetch(List<Triple<Currency, LocalDate, Tenor>> swapsArgs) {
    MasterConfigSource configSource = new MasterConfigSource(getToolContext().getConfigMaster());
    Set<ExternalId> externalIds = newHashSet();
    for (Triple<Currency, LocalDate, Tenor> swapArgs : swapsArgs) {
      ExternalId swapRateForMaturityIdentifier = getSwapRateFor(configSource, swapArgs.getFirst(), swapArgs.getSecond(), swapArgs.getThird());
      externalIds.add(swapRateForMaturityIdentifier);
      s_logger.info("LOADING TS: {}  --->  {}", swapRateForMaturityIdentifier);
    }

    BloombergHistoricalLoader loader = new BloombergHistoricalLoader(
      getToolContext().getHistoricalTimeSeriesMaster(),
      ((BloombergToolContext) getToolContext()).getBloombergHistoricalTimeSeriesSource(),
      new BloombergIdentifierProvider(((BloombergToolContext) getToolContext()).getBloombergReferenceDataProvider()));
    loader.setReload(true);
    loader.addTimeSeries(externalIds, "CMPL", "PX_LAST", null, null);
  }

  private SwapSecurity makeSwap(final SecureRandom random, final Currency ccy, final LocalDate tradeDate, final Tenor maturity) {

    // get the identifier for the swap rate for the maturity we're interested in (assuming the fixed rate will be =~ swap rate)
    Double fixedRate = getFixedRate(random, ccy, tradeDate, maturity);
    Double notional = (double) random.nextInt(100000) * 1000;
    boolean isPayFixed = random.nextBoolean();

    ConventionBundle swapConvention = getSwapConventionBundle(ccy);
    ConventionBundle liborConvention = getLiborConventionBundle(swapConvention);

    // look up the BLOOMBERG ticker out of the bundle
    ExternalId liborIdentifier = liborConvention.getIdentifiers().getExternalId(SecurityUtils.BLOOMBERG_TICKER);
    if (liborIdentifier == null) {
      throw new OpenGammaRuntimeException("No bloomberg ticker set up for " + swapConvention.getName());
    }

    ZonedDateTime tradeDateTime = ZonedDateTime.of(tradeDate, LocalTime.MIDNIGHT, TimeZone.UTC);
    ZonedDateTime maturityDateTime = ZonedDateTime.of(tradeDate.plus(maturity.getPeriod()), LocalTime.MIDNIGHT, TimeZone.UTC);
    String counterparty = "CParty";

    SwapLeg fixedLeg = new FixedInterestRateLeg(swapConvention.getSwapFixedLegDayCount(),
      swapConvention.getSwapFixedLegFrequency(),
      swapConvention.getSwapFixedLegRegion(),
      swapConvention.getSwapFixedLegBusinessDayConvention(),
      new InterestRateNotional(ccy, notional),
      false, fixedRate);

    FloatingInterestRateLeg floatingLeg = new FloatingInterestRateLeg(swapConvention.getSwapFloatingLegDayCount(),
      swapConvention.getSwapFloatingLegFrequency(),
      swapConvention.getSwapFloatingLegRegion(),
      swapConvention.getSwapFloatingLegBusinessDayConvention(),
      new InterestRateNotional(ccy, notional),
      false, ExternalId.of(liborIdentifier.getScheme().toString(), liborIdentifier.getValue()),
      FloatingRateType.IBOR);
    // look up the value on our chosen trade date
    Double initialRate = getInitialRate(tradeDate, liborIdentifier);
    floatingLeg.setInitialFloatingRate(initialRate);

    String fixedLegDescription = PortfolioLoaderHelper.RATE_FORMATTER.format(fixedRate);
    String floatingLegDescription = swapConvention.getSwapFloatingLegInitialRate().getValue();
    SwapLeg payLeg;
    String payLegDescription;
    SwapLeg receiveLeg;
    String receiveLegDescription;
    if (isPayFixed) {
      payLeg = fixedLeg;
      payLegDescription = fixedLegDescription;
      receiveLeg = floatingLeg;
      receiveLegDescription = floatingLegDescription;
    } else {
      payLeg = floatingLeg;
      payLegDescription = floatingLegDescription;
      receiveLeg = fixedLeg;
      receiveLegDescription = fixedLegDescription;
    }
    SwapSecurity swap = new SwapSecurity(tradeDateTime, tradeDateTime, maturityDateTime, counterparty, payLeg, receiveLeg);
    swap.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap.setName("IR Swap " + ccy + " " + PortfolioLoaderHelper.NOTIONAL_FORMATTER.format(notional) + " " +
      maturityDateTime.toString(PortfolioLoaderHelper.OUTPUT_DATE_FORMATTER) + " - " + payLegDescription + " / " + receiveLegDescription);
    return swap;
  }

  private Double getFixedRate(SecureRandom random, Currency ccy, LocalDate tradeDate, Tenor maturity) {
    HistoricalTimeSeriesSource historicalSource = getToolContext().getHistoricalTimeSeriesSource();
    MasterConfigSource configSource = new MasterConfigSource(getToolContext().getConfigMaster());
    ExternalId swapRateForMaturityIdentifier = getSwapRateFor(configSource, ccy, tradeDate, maturity);
    if (swapRateForMaturityIdentifier == null) {
      throw new OpenGammaRuntimeException("Couldn't get swap rate identifier for " + ccy + " [" + maturity + "]" + " from " + tradeDate);
    }

    HistoricalTimeSeries fixedRateSeries = historicalSource.getHistoricalTimeSeries("PX_LAST", swapRateForMaturityIdentifier.toBundle(), HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME, tradeDate, true, tradeDate, true);
    if (fixedRateSeries == null) {
      throw new OpenGammaRuntimeException("can't find time series for " + swapRateForMaturityIdentifier + " on " + tradeDate);
    }
    Double fixedRate = (fixedRateSeries.getTimeSeries().getEarliestValue() + random.nextDouble()) / 100d;
    return fixedRate;
  }

  private Double getInitialRate(LocalDate tradeDate, ExternalId liborIdentifier) {
    HistoricalTimeSeriesSource historicalSource = getToolContext().getHistoricalTimeSeriesSource();
    HistoricalTimeSeries initialRateSeries = historicalSource.getHistoricalTimeSeries(
      "PX_LAST", liborIdentifier.toBundle(),
      HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME, tradeDate, true, tradeDate, true);
    if (initialRateSeries == null || initialRateSeries.getTimeSeries().isEmpty()) {
      throw new OpenGammaRuntimeException("couldn't get series for " + liborIdentifier + " on " + tradeDate);
    }
    Double initialRate = initialRateSeries.getTimeSeries().getEarliestValue();
    return initialRate;
  }

  private ConventionBundle getLiborConventionBundle(ConventionBundle swapConvention) {
    // get the convention of the identifier of the initial rate
    ConventionBundleSource conventionSource = getToolContext().getConventionBundleSource();
    ConventionBundle liborConvention = conventionSource.getConventionBundle(swapConvention.getSwapFloatingLegInitialRate());
    if (liborConvention == null) {
      throw new OpenGammaRuntimeException("Couldn't get libor convention for " + swapConvention.getSwapFloatingLegInitialRate());
    }
    return liborConvention;
  }

  private ConventionBundle getSwapConventionBundle(Currency ccy) {
    ConventionBundleSource conventionSource = getToolContext().getConventionBundleSource();
    ConventionBundle swapConvention = conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, ccy.getCode() + "_SWAP"));
    if (swapConvention == null) {
      throw new OpenGammaRuntimeException("Couldn't get swap convention for " + ccy.getCode());
    }
    return swapConvention;
  }

  private LocalDate getTradeDate(SecureRandom random, Currency ccy) {
    HolidaySource holidaySource = getToolContext().getHolidaySource();
    LocalDate tradeDate;
    do {
      tradeDate = DateUtils.previousWeekDay().minusDays(random.nextInt(DAYS_TRADING));
    } while (holidaySource.isHoliday(tradeDate, ccy));
    return tradeDate;
  }

  private Currency getCurrency(SecureRandom random) {
    int offset = random.nextInt(s_currencies.length);
    Currency ccy = s_currencies[offset];
    return ccy;
  }

  private static ExternalId getSwapRateFor(ConfigSource configSource, Currency ccy, LocalDate tradeDate, Tenor tenor) {
    CurveSpecificationBuilderConfiguration curveSpecConfig = configSource.getByName(CurveSpecificationBuilderConfiguration.class, "DEFAULT_" + ccy.getCode(), null);
    if (curveSpecConfig == null) {
      throw new OpenGammaRuntimeException("No curve spec builder configuration for DEFAULT_" + ccy.getCode());
    }
    ExternalId swapSecurity;
    if (ccy.equals(Currency.USD)) {
      // Standard (i.e. matches convention) floating leg tenor for USD is 3M
      swapSecurity = curveSpecConfig.getSwap3MSecurity(tradeDate, tenor);
    } else {
      // Standard (i.e. matches convention) floating leg tenor for CHF, JPY, GBP, EUR is 6M
      swapSecurity = curveSpecConfig.getSwap6MSecurity(tradeDate, tenor);
    }
    return swapSecurity;
  }

  private void persistToPortfolio(Collection<SwapSecurity> swaps, String portfolioName) {
    PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    PositionMaster positionMaster = getToolContext().getPositionMaster();
    SecurityMaster securityMaster = getToolContext().getSecurityMaster();

    ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);
    ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName, rootNode);
    PortfolioDocument portfolioDoc = new PortfolioDocument();
    portfolioDoc.setPortfolio(portfolio);

    for (SwapSecurity swap : swaps) {
      SecurityDocument swapToAddDoc = new SecurityDocument();
      swapToAddDoc.setSecurity(swap);
      securityMaster.add(swapToAddDoc);
      ManageablePosition swapPosition = new ManageablePosition(BigDecimal.ONE, swap.getExternalIdBundle());
      PositionDocument addedDoc = positionMaster.add(new PositionDocument(swapPosition));
      rootNode.addPosition(addedDoc.getUniqueId());
    }
    portfolioMaster.add(portfolioDoc);
  }

}
