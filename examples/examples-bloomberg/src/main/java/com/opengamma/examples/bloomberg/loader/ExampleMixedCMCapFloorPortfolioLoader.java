/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.bloomberg.loader;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
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
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.ShutdownUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 * Example code to load a simple portfolio of cap/floors and constant-maturity swaps.
 * <p>
 * This code is kept deliberately as simple as possible.
 * There are no checks for the securities or portfolios already existing, so if you run it
 * more than once you will get multiple copies portfolios and securities with the same names.
 * It is designed to run against the HSQLDB example database.
 */
public class ExampleMixedCMCapFloorPortfolioLoader extends AbstractTool<IntegrationToolContext> {

  /** Logger */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleMixedCMCapFloorPortfolioLoader.class);
  /** The trade date */
  private static final LocalDate TRADE_DATE = DateUtils.previousWeekDay().minusDays(30);
  /** Following business day convention */
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;
  /** The region */
  private static final ExternalId REGION = ExternalSchemes.financialRegionId("US+GB");
  /** Act/360 day-count */
  private static final DayCount ACT_360 = DayCounts.ACT_360;
  /** The currency */
  private static final Currency CURRENCY = Currency.USD;
  /** The counterparty */
  private static final String COUNTERPARTY = "Cpty";
  /** 3m Libor ticker */
  private static final ExternalId LIBOR_3M = ExternalSchemes.bloombergTickerSecurityId("US0003M Index");
  /** The tenors */
  private static final Tenor[] TENORS = new Tenor[] {Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FIVE_YEARS, Tenor.TEN_YEARS};
  /** The pay tenors */
  private static final Tenor[] PAY_TENORS = new Tenor[] {Tenor.ONE_YEAR, Tenor.FIVE_YEARS};
  /** The receive tenors */
  private static final Tenor[] RECEIVE_TENORS = new Tenor[] {Tenor.TWO_YEARS, Tenor.TEN_YEARS};
  /** The strike formatter */
  private static final DecimalFormat FORMAT = new DecimalFormat("##.##");
  /** The id scheme for these securities */
  private static final String ID_SCHEME = "CAP_FLOOR_SWAP_GENERATOR";
  /** The portfolio name */
  public static final String PORTFOLIO_NAME = "CMS and Cap/Floor Portfolio";
  /** A map of tenors to swap rate tickers */
  private static final Map<Tenor, ExternalId> TICKERS = new HashMap<>();

  static {
    TICKERS.put(Tenor.ONE_YEAR, ExternalSchemes.bloombergTickerSecurityId("USSW1 Curncy"));
    TICKERS.put(Tenor.TWO_YEARS, ExternalSchemes.bloombergTickerSecurityId("USSW2 Curncy"));
    TICKERS.put(Tenor.THREE_YEARS, ExternalSchemes.bloombergTickerSecurityId("USSW3 Curncy"));
    TICKERS.put(Tenor.FIVE_YEARS, ExternalSchemes.bloombergTickerSecurityId("USSW5 Curncy"));
    TICKERS.put(Tenor.TEN_YEARS, ExternalSchemes.bloombergTickerSecurityId("USSW10 Curncy"));
  }

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { //CSIGNORE
    try {
      boolean success =
          new ExampleTimeSeriesRatingLoader().initAndRun(args, IntegrationToolContext.class) &&
          new ExampleMixedCMCapFloorPortfolioLoader().initAndRun(args, IntegrationToolContext.class);
      ShutdownUtils.exit(success ? 0 : -1);
    } catch (Throwable ex) {
      ex.printStackTrace();
      ShutdownUtils.exit(-2);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    final Random random = new Random(45689);
    final Collection<FinancialSecurity> securities = createRandomSwaps(random, 40);
    securities.addAll(createRandomCapFloors(random, 60));
    securities.addAll(createRandomCapFloorCMSSpreads(random, 60));
    if (securities.size() == 0) {
      throw new OpenGammaRuntimeException("No valid securities were generated");
    }
    persistToPortfolio(securities, PORTFOLIO_NAME);
  }

  private Collection<FinancialSecurity> createRandomSwaps(final Random random, final int n) {
    final Collection<FinancialSecurity> securities = new HashSet<>();
    for (int i = 0; i < n; i++) {
      FinancialSecurity security = null;
      try {
        final Tenor tenor = TENORS[random.nextInt(TENORS.length)];
        security = makeSwap(random, tenor);
      } catch (final Exception e) {
        e.printStackTrace();
      }
      if (security != null) {
        securities.add(security);
      }
    }
    final StringBuilder sb = new StringBuilder();
    sb.append("Parsed ").append(securities.size()).append(" swaps:\n");
    for (final FinancialSecurity security : securities) {
      sb.append("\t").append(security.getName()).append("\n");
    }
    s_logger.info(sb.toString());
    return securities;
  }

  private Collection<FinancialSecurity> createRandomCapFloors(final Random random, final int n) {
    final Collection<FinancialSecurity> securities = new HashSet<>();
    for (int i = 0; i < n; i++) {
      FinancialSecurity security = null;
      try {
        final Tenor tenor = TENORS[random.nextInt(TENORS.length)];
        security = makeCapFloor(random, tenor);
      } catch (final Exception e) {
        e.printStackTrace();
      }
      if (security != null) {
        securities.add(security);
      }
    }
    final StringBuilder sb = new StringBuilder();
    sb.append("Parsed ").append(securities.size()).append(" cap/floors:\n");
    for (final FinancialSecurity security : securities) {
      sb.append("\t").append(security.getName()).append("\n");
    }
    s_logger.info(sb.toString());
    return securities;
  }

  private Collection<FinancialSecurity> createRandomCapFloorCMSSpreads(final Random random, final int n) {
    final Collection<FinancialSecurity> securities = new HashSet<>();
    for (int i = 0; i < n; i++) {
      FinancialSecurity security = null;
      try {
        final Tenor tenor = TENORS[random.nextInt(TENORS.length)];
        final Tenor payTenor = PAY_TENORS[random.nextInt(PAY_TENORS.length)];
        final Tenor receiveTenor = RECEIVE_TENORS[random.nextInt(RECEIVE_TENORS.length)];
        security = makeCMSCapFloorSpread(random, payTenor, receiveTenor, tenor, i);
      } catch (final Exception e) {
        e.printStackTrace();
      }
      if (security != null) {
        securities.add(security);
      }
    }
    final StringBuilder sb = new StringBuilder();
    sb.append("Parsed ").append(securities.size()).append(" CMS cap/floor spreads:\n");
    for (final FinancialSecurity security : securities) {
      sb.append("\t").append(security.getName()).append("\n");
    }
    s_logger.info(sb.toString());
    return securities;
  }

  private CapFloorSecurity makeCapFloor(final Random random, final Tenor tenor) {
    final ZonedDateTime maturityDate = TRADE_DATE.plus(tenor.getPeriod()).atStartOfDay(ZoneOffset.UTC);
    final boolean payer = random.nextBoolean();
    final boolean cap = random.nextBoolean();
    final double strike = getSwapRate(CURRENCY, TRADE_DATE, tenor) * (1 + ((0.5 - random.nextDouble()) / 30));
    final double notional = 1000000 * (1 + random.nextInt(50));
    final ExternalId underlyingId = TICKERS.get(tenor);
    if (underlyingId == null) {
      throw new OpenGammaRuntimeException("Could not get swap rate ticker for " + tenor);
    }
    final CapFloorSecurity security = new CapFloorSecurity(TRADE_DATE.atStartOfDay(ZoneOffset.UTC), maturityDate, notional, underlyingId, strike, PeriodFrequency.SEMI_ANNUAL,
        Currency.USD, ACT_360, payer, cap, false);
    security.setName("USD " + FORMAT.format(notional / 1000000) + (cap ? "MM cap " : "MM floor ") + "@ " + FORMAT.format(strike) +
        (payer ? "%, pay " : "%, receive ") + tenor.getPeriod().normalized().getYears() + "Y ISDA fixing" +
        " (" + TRADE_DATE.toString() + " - " + maturityDate.toLocalDate().toString() + ")");
    security.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    return security;
  }

  private SwapSecurity makeSwap(final Random random, final Tenor tenor) {
    final ZonedDateTime tradeDate = TRADE_DATE.atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime maturityDate = tradeDate.plus(tenor.getPeriod());
    final ExternalId iborReferenceRate = LIBOR_3M;
    final PeriodFrequency frequency = PeriodFrequency.QUARTERLY;
    final ExternalId cmsId = TICKERS.get(tenor);
    if (cmsId == null) {
      throw new OpenGammaRuntimeException("Could not get swap rate ticker for " + tenor);
    }
    final InterestRateNotional notional = new InterestRateNotional(Currency.USD, 1000000 * (1 + random.nextInt(50)));
    final FloatingInterestRateLeg iborLeg = new FloatingInterestRateLeg(ACT_360, frequency, REGION, FOLLOWING, notional, true,
        iborReferenceRate, FloatingRateType.IBOR);
    final FloatingInterestRateLeg cmsLeg = new FloatingInterestRateLeg(ACT_360, frequency, REGION, FOLLOWING, notional, true,
        cmsId, FloatingRateType.CMS);
    SwapSecurity security;
    boolean payIbor;
    if (random.nextBoolean()) {
      security = new SwapSecurity(tradeDate, tradeDate, maturityDate, COUNTERPARTY, iborLeg, cmsLeg);
      payIbor = true;
    } else {
      security = new SwapSecurity(tradeDate, tradeDate, maturityDate, COUNTERPARTY, cmsLeg, iborLeg);
      payIbor = false;
    }
    security.setName(CURRENCY.getCode() + " " + FORMAT.format(notional.getAmount() / 1000000) + "MM Swap, pay " +
        (payIbor ? frequency.getPeriod().getMonths() + "M Libor, receive " + tenor.getPeriod().getYears() + "Y ISDA fixing (" :
          tenor.getPeriod().getYears() + "Y ISDA fixing, receive " + frequency.getPeriod().getMonths() + "M Libor (") +
          tradeDate.toLocalDate().toString() + " - " + maturityDate.toLocalDate().toString() + ")");
    security.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    return security;
  }

  private CapFloorCMSSpreadSecurity makeCMSCapFloorSpread(final Random random, final Tenor payTenor, final Tenor receiveTenor, final Tenor maturity, final double strike) {
    final ZonedDateTime tradeDate = TRADE_DATE.atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime maturityDate = tradeDate.plus(maturity.getPeriod());
    final boolean payer = random.nextBoolean();
    final boolean cap = random.nextBoolean();
    final ExternalId payIdentifier = TICKERS.get(payTenor);
    if (payIdentifier == null) {
      throw new OpenGammaRuntimeException("Could not get swap rate ticker for " + payTenor);
    }
    final ExternalId receiveIdentifier = TICKERS.get(receiveTenor);
    if (receiveIdentifier == null) {
      throw new OpenGammaRuntimeException("Could not get swap rate ticker for " + receiveTenor);
    }
    final double notional = 1000000 * (1 + random.nextInt(50));
    final CapFloorCMSSpreadSecurity security = new CapFloorCMSSpreadSecurity(tradeDate, maturityDate, notional, payIdentifier, receiveIdentifier, strike,
        PeriodFrequency.ANNUAL, CURRENCY, ACT_360, payer, cap);
    security.setName(CURRENCY.getCode() + " " + FORMAT.format(notional / 1000000) + (cap ? "MM cap spread " : "MM floor spread ") + "@ " + FORMAT.format(strike) +
        "%, pay " + payTenor.getPeriod().normalized().getYears() + "Y ISDA fixing" + ", receive " +
        receiveTenor.getPeriod().normalized().getYears() + "Y ISDA fixing" +
        " (" + tradeDate.toLocalDate().toString() + " - " + maturityDate.toLocalDate().toString() + ")");
    security.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    return security;
  }

  private Double getSwapRate(final Currency ccy, final LocalDate tradeDate, final Tenor maturity) {
    final HistoricalTimeSeriesSource historicalSource = getToolContext().getHistoricalTimeSeriesSource();
    final MasterConfigSource configSource = new MasterConfigSource(getToolContext().getConfigMaster());
    final ExternalId swapRateForMaturityIdentifier = getSwapRateFor(configSource, ccy, tradeDate, maturity);
    if (swapRateForMaturityIdentifier == null) {
      throw new OpenGammaRuntimeException("Couldn't get swap rate identifier for " + ccy + " [" + maturity + "]" + " from " + tradeDate);
    }

    final HistoricalTimeSeries fixedRateSeries = historicalSource.getHistoricalTimeSeries("PX_LAST",
        swapRateForMaturityIdentifier.toBundle(), HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME, tradeDate.minusDays(30), true, tradeDate, true);
    if (fixedRateSeries == null) {
      throw new OpenGammaRuntimeException("Time series for " + swapRateForMaturityIdentifier + " was null");
    }
    if (fixedRateSeries.getTimeSeries().isEmpty()) {
      throw new OpenGammaRuntimeException("Time series for " + swapRateForMaturityIdentifier + " was empty");
    }
    return fixedRateSeries.getTimeSeries().getLatestValue();
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

  private void persistToPortfolio(final Collection<FinancialSecurity> securities, final String portfolioName) {
    final PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    final PositionMaster positionMaster = getToolContext().getPositionMaster();
    final SecurityMaster securityMaster = getToolContext().getSecurityMaster();

    final ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);
    final ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName, rootNode);
    final PortfolioDocument portfolioDoc = new PortfolioDocument();
    portfolioDoc.setPortfolio(portfolio);

    for (final FinancialSecurity security : securities) {
      final SecurityDocument securityToAddDoc = new SecurityDocument();
      securityToAddDoc.setSecurity(security);
      securityMaster.add(securityToAddDoc);
      final ManageablePosition position = new ManageablePosition(BigDecimal.ONE, security.getExternalIdBundle());
      final PositionDocument addedDoc = positionMaster.add(new PositionDocument(position));
      rootNode.addPosition(addedDoc.getUniqueId());
    }
    portfolioMaster.add(portfolioDoc);
  }
}
