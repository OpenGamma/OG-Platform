/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.bloomberg.loader;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.tool.IntegrationToolContext;
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
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.ShutdownUtils;
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
@Scriptable
public class ExampleMultiCurrencySwapPortfolioLoader extends AbstractTool<IntegrationToolContext> {

  /** Logger. */
  private static Logger s_logger = LoggerFactory.getLogger(ExampleMultiCurrencySwapPortfolioLoader.class);

  /**
   * Size of securities in portfolio
   */
  private static final int SECURITIES_SIZE = 200;

  /**
   * The name of the portfolio.
   */
  public static final String PORTFOLIO_NAME = "Multi-currency Swap Portfolio";

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
    s_tenors = new Tenor[]{Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FIVE_YEARS,
      Tenor.ofYears(7), Tenor.ofYears(10), Tenor.ofYears(12), Tenor.ofYears(15), Tenor.ofYears(20)};
  }

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) {  // CSIGNORE
    try {
      boolean success =
          new ExampleTimeSeriesRatingLoader().initAndRun(args, IntegrationToolContext.class) &&
          new ExampleMultiCurrencySwapPortfolioLoader().initAndRun(args, IntegrationToolContext.class);
      ShutdownUtils.exit(success ? 0 : -1);
    } catch (Throwable ex) {
      ex.printStackTrace();
      ShutdownUtils.exit(-2);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    final Collection<SwapSecurity> swaps = createRandomSwaps();
    if (swaps.size() == 0) {
      throw new OpenGammaRuntimeException("No (valid) swaps were generated.");
    }
    persistToPortfolio(swaps, PORTFOLIO_NAME);
  }

  private Collection<SwapSecurity> createRandomSwaps() {
    final Collection<SwapSecurity> swaps = new ArrayList<>();

    final SecureRandom random = new SecureRandom();

    // Currency, TradeDate, Maturity
    final List<Triple<Currency, LocalDate, Tenor>> swapsArgs = newArrayList();

    for (int i = 0; i < SECURITIES_SIZE; i++) {
      final Currency ccy = getCurrency(random);
      final LocalDate tradeDate = getTradeDate(random, ccy);
      final Tenor maturity = s_tenors[random.nextInt(s_tenors.length)];
      swapsArgs.add(Triple.of(ccy, tradeDate, maturity));
    }

    fetch(swapsArgs);

    for (final Triple<Currency, LocalDate, Tenor> swapArgs : swapsArgs) {
      SwapSecurity swap = null;
      try {
        swap = makeSwap(random, swapArgs.getFirst(), swapArgs.getSecond(), swapArgs.getThird());
      } catch (final Exception e) {
        e.printStackTrace();
      }
      if (swap != null) {
        swaps.add(swap);
      }
    }

    final StringBuilder sb = new StringBuilder();
    sb.append("Parsed ").append(swaps.size()).append(" swaps:\n");
    for (final SwapSecurity swap : swaps) {
      sb.append("\t").append(swap.getName()).append("\n");
    }
    s_logger.info(sb.toString());
    return swaps;
  }

  private void fetch(final List<Triple<Currency, LocalDate, Tenor>> swapsArgs) {
    final MasterConfigSource configSource = new MasterConfigSource(getToolContext().getConfigMaster());
    final Set<ExternalId> externalIds = newHashSet();
    for (final Triple<Currency, LocalDate, Tenor> swapArgs : swapsArgs) {
      final ExternalId swapRateForMaturityIdentifier = getSwapRateFor(configSource, swapArgs.getFirst(), swapArgs.getSecond(), swapArgs.getThird());
      externalIds.add(swapRateForMaturityIdentifier);
    }

    final BloombergHistoricalTimeSeriesLoader loader = new BloombergHistoricalTimeSeriesLoader(
      getToolContext().getHistoricalTimeSeriesMaster(),
      getToolContext().getHistoricalTimeSeriesProvider(),
      new BloombergIdentifierProvider(getToolContext().getBloombergReferenceDataProvider()));
    loader.loadTimeSeries(externalIds, "UNKNOWN", "PX_LAST", LocalDate.now().minusYears(1), LocalDate.now());
  }

  private SwapSecurity makeSwap(final SecureRandom random, final Currency ccy, final LocalDate tradeDate, final Tenor maturity) {

    // get the identifier for the swap rate for the maturity we're interested in (assuming the fixed rate will be =~ swap rate)
    final Double fixedRate = getFixedRate(random, ccy, tradeDate, maturity);
    final Double notional = (double) (random.nextInt(99999) + 1) * 1000;
    final boolean isPayFixed = random.nextBoolean();

    final ConventionBundle swapConvention = getSwapConventionBundle(ccy);
    final ConventionBundle liborConvention = getLiborConventionBundle(swapConvention);

    // look up the BLOOMBERG ticker out of the bundle
    final ExternalId liborIdentifier = liborConvention.getIdentifiers().getExternalId(ExternalSchemes.BLOOMBERG_TICKER);
    if (liborIdentifier == null) {
      throw new OpenGammaRuntimeException("No bloomberg ticker set up for " + swapConvention.getName());
    }

    final ZonedDateTime tradeDateTime = tradeDate.atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime maturityDateTime = tradeDate.plus(maturity.getPeriod()).atStartOfDay(ZoneOffset.UTC);
    final String counterparty = "CParty";

    final SwapLeg fixedLeg = new FixedInterestRateLeg(swapConvention.getSwapFixedLegDayCount(),
      swapConvention.getSwapFixedLegFrequency(),
      swapConvention.getSwapFixedLegRegion(),
      swapConvention.getSwapFixedLegBusinessDayConvention(),
      new InterestRateNotional(ccy, notional),
      false, fixedRate);

    final FloatingInterestRateLeg floatingLeg = new FloatingInterestRateLeg(swapConvention.getSwapFloatingLegDayCount(),
      swapConvention.getSwapFloatingLegFrequency(),
      swapConvention.getSwapFloatingLegRegion(),
      swapConvention.getSwapFloatingLegBusinessDayConvention(),
      new InterestRateNotional(ccy, notional),
      false, ExternalId.of(liborIdentifier.getScheme().toString(), liborIdentifier.getValue()),
      FloatingRateType.IBOR);
    // look up the value on our chosen trade date
    final Double initialRate = getInitialRate(tradeDate, liborIdentifier);
    floatingLeg.setInitialFloatingRate(initialRate);

    final String fixedLegDescription = PortfolioLoaderHelper.RATE_FORMATTER.format(fixedRate);
    final String floatingLegDescription = swapConvention.getSwapFloatingLegInitialRate().getValue();
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
    final SwapSecurity swap = new SwapSecurity(tradeDateTime, tradeDateTime, maturityDateTime, counterparty, payLeg, receiveLeg);
    swap.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap.setName("IR Swap " + ccy + " " + PortfolioLoaderHelper.NOTIONAL_FORMATTER.format(notional) + " " +
      maturityDateTime.toString(PortfolioLoaderHelper.OUTPUT_DATE_FORMATTER) + " - " + payLegDescription + " / " + receiveLegDescription);
    return swap;
  }

  private Double getFixedRate(final SecureRandom random, final Currency ccy, final LocalDate tradeDate, final Tenor maturity) {
    final HistoricalTimeSeriesSource historicalSource = getToolContext().getHistoricalTimeSeriesSource();
    final MasterConfigSource configSource = new MasterConfigSource(getToolContext().getConfigMaster());
    final ExternalId swapRateForMaturityIdentifier = getSwapRateFor(configSource, ccy, tradeDate, maturity);
    if (swapRateForMaturityIdentifier == null) {
      throw new OpenGammaRuntimeException("Couldn't get swap rate identifier for " + ccy + " [" + maturity + "]" + " from " + tradeDate);
    }

    final HistoricalTimeSeries fixedRateSeries = historicalSource.getHistoricalTimeSeries("PX_LAST",
        swapRateForMaturityIdentifier.toBundle(), HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME, tradeDate.minusDays(30), true, tradeDate, true);
    if (fixedRateSeries == null) {
      throw new OpenGammaRuntimeException("can't find time series for " + swapRateForMaturityIdentifier + " on " + tradeDate);
    }
    final Double fixedRate = (fixedRateSeries.getTimeSeries().getLatestValue() + random.nextDouble()) / 100d;
    return fixedRate;
  }

  private Double getInitialRate(final LocalDate tradeDate, final ExternalId liborIdentifier) {
    final HistoricalTimeSeriesSource historicalSource = getToolContext().getHistoricalTimeSeriesSource();
    final HistoricalTimeSeries initialRateSeries = historicalSource.getHistoricalTimeSeries(
      "PX_LAST", liborIdentifier.toBundle(),
      HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME, tradeDate.minusDays(30), true, tradeDate, true);
    if (initialRateSeries == null || initialRateSeries.getTimeSeries().isEmpty()) {
      throw new OpenGammaRuntimeException("couldn't get series for " + liborIdentifier);
    }
    return initialRateSeries.getTimeSeries().getLatestValue();
  }

  private ConventionBundle getLiborConventionBundle(final ConventionBundle swapConvention) {
    // get the convention of the identifier of the initial rate
    final ConventionBundleSource conventionSource = getToolContext().getConventionBundleSource();
    final ConventionBundle liborConvention = conventionSource.getConventionBundle(swapConvention.getSwapFloatingLegInitialRate());
    if (liborConvention == null) {
      throw new OpenGammaRuntimeException("Couldn't get libor convention for " + swapConvention.getSwapFloatingLegInitialRate());
    }
    return liborConvention;
  }

  private ConventionBundle getSwapConventionBundle(final Currency ccy) {
    final ConventionBundleSource conventionSource = getToolContext().getConventionBundleSource();
    final ConventionBundle swapConvention = conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, ccy.getCode() + "_SWAP"));
    if (swapConvention == null) {
      throw new OpenGammaRuntimeException("Couldn't get swap convention for " + ccy.getCode());
    }
    return swapConvention;
  }

  private LocalDate getTradeDate(final SecureRandom random, final Currency ccy) {
    final HolidaySource holidaySource = getToolContext().getHolidaySource();
    LocalDate tradeDate;
    do {
      tradeDate = DateUtils.previousWeekDay().minusDays(random.nextInt(DAYS_TRADING));
    } while (holidaySource.isHoliday(tradeDate, ccy));
    return tradeDate;
  }

  private Currency getCurrency(final SecureRandom random) {
    final int offset = random.nextInt(s_currencies.length);
    final Currency ccy = s_currencies[offset];
    return ccy;
  }

  private static ExternalId getSwapRateFor(final ConfigSource configSource, final Currency ccy, final LocalDate tradeDate, final Tenor tenor) {
    final CurveSpecificationBuilderConfiguration curveSpecConfig = configSource.getSingle(CurveSpecificationBuilderConfiguration.class, "DEFAULT_" + ccy.getCode(), VersionCorrection.LATEST);
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

  private void persistToPortfolio(final Collection<SwapSecurity> swaps, final String portfolioName) {
    final PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    final PositionMaster positionMaster = getToolContext().getPositionMaster();
    final SecurityMaster securityMaster = getToolContext().getSecurityMaster();

    final ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);
    final ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName, rootNode);
    final PortfolioDocument portfolioDoc = new PortfolioDocument();
    portfolioDoc.setPortfolio(portfolio);

    for (final SwapSecurity swap : swaps) {
      final SecurityDocument swapToAddDoc = new SecurityDocument();
      swapToAddDoc.setSecurity(swap);
      securityMaster.add(swapToAddDoc);
      final ManageablePosition swapPosition = new ManageablePosition(BigDecimal.ONE, swap.getExternalIdBundle());
      final PositionDocument addedDoc = positionMaster.add(new PositionDocument(swapPosition));
      rootNode.addPosition(addedDoc.getUniqueId());
    }
    portfolioMaster.add(portfolioDoc);
  }

}
