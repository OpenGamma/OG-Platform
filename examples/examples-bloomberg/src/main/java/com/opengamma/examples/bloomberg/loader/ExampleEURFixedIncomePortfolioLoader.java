/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.bloomberg.loader;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.Month;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.integration.tool.IntegrationToolContext;
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
import com.opengamma.util.time.Expiry;

/**
 * Example code to load a very simple EUR fixed-income portfolio.
 * <p>
 * This code is kept deliberately as simple as possible.
 * There are no checks for the securities or portfolios already existing, so if you run it
 * more than once you will get multiple copies portfolios and securities with the same names.
 * It is designed to run against the HSQLDB example database.
 */
@Scriptable
public class ExampleEURFixedIncomePortfolioLoader extends AbstractTool<IntegrationToolContext> {

  /** The currency */
  private static final Currency CURRENCY = Currency.EUR;
  /** Act/360 day-count */
  private static final DayCount ACT_360 = DayCounts.ACT_360;
  /** Quarterly frequency */
  private static final Frequency QUARTERLY = PeriodFrequency.QUARTERLY;
  /** Semi-annual frequency */
  private static final Frequency SEMI_ANNUAL = PeriodFrequency.SEMI_ANNUAL;
  /** Modified following business day convention */
  private static final BusinessDayConvention MODIFIED_FOLLOWING = BusinessDayConventions.MODIFIED_FOLLOWING;
  /** The holiday region */
  private static final ExternalId REGION = ExternalSchemes.financialRegionId("EU");
  /** The 3m ibor ticker */
  private static final ExternalId EURIBOR_3M = ExternalSchemes.bloombergTickerSecurityId("EUR003M Index");
  /** The 6m ibor ticker */
  private static final ExternalId EURIBOR_6M = ExternalSchemes.bloombergTickerSecurityId("EUR006M Index");
  /** The EONIA ticker */
  private static final ExternalId EONIA = ExternalSchemes.bloombergTickerSecurityId("EONIA Index");
  /** Gets the future expiry date */
  private static final TemporalAdjuster THIRD_WED_ADJUSTER = TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.WEDNESDAY);
  /** Contains month codes for futures */
  private static final Map<Month, String> MONTHS;
  /** The counterparty */
  private static final String COUNTERPARTY = "Cpty";
  /** The number of vanilla swaps */
  private static final int N_VANILLA_SWAPS = 100;
  /** The number of OIS swaps */
  private static final int N_OIS_SWAPS = 100;
  /** The number of basis swaps */
  private static final int N_BASIS_SWAPS = 100;
  /** The number of futures */
  private static final int N_FUTURES = 100;
  /** The rate formatter */
  private static final DecimalFormat FORMATTER = new DecimalFormat("#.###");
  /** The scheme for the identifier */
  private static final String ID_SCHEME = "EUR_SWAP_GENERATOR";
  /** The portfolio name */
  public static final String PORTFOLIO_NAME = "EUR Swap Desk Portfolio";

  static {
    MONTHS = new HashMap<>();
    MONTHS.put(Month.MARCH, "H");
    MONTHS.put(Month.JUNE, "M");
    MONTHS.put(Month.SEPTEMBER, "U");
    MONTHS.put(Month.DECEMBER, "Z");
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
          new ExampleEURFixedIncomePortfolioLoader().initAndRun(args, IntegrationToolContext.class);
      ShutdownUtils.exit(success ? 0 : -1);
    } catch (Throwable ex) {
      ex.printStackTrace();
      ShutdownUtils.exit(-2);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {
    final Random random = new Random(3457);
    final Collection<FinancialSecurity> vanillaSwaps = getVanillaSwapSecurities(random);
    if (vanillaSwaps.isEmpty()) {
      throw new OpenGammaRuntimeException("No valid vanilla swaps were generated");
    }
    final Collection<FinancialSecurity> oisSwaps = getOISSwapSecurities(random);
    if (oisSwaps.isEmpty()) {
      throw new OpenGammaRuntimeException("No valid OIS were generated");
    }
    final Collection<FinancialSecurity> basisSwaps = getBasisSwapSecurities(random);
    if (basisSwaps.isEmpty()) {
      throw new OpenGammaRuntimeException("No valid basis swaps were generated");
    }
    final Collection<FinancialSecurity> futures = getIRFutureSecurities(random);
    if (futures.isEmpty()) {
      throw new OpenGammaRuntimeException("No valid futures were generated");
    }
//    persistToPortfolio(Arrays.asList(vanillaSwaps, oisSwaps, basisSwaps, futures), Arrays.asList("EUR Vanilla Swaps", "EUR OIS Swaps", "EUR 3m/6m Basis Swaps", "STIR Futures"), PORTFOLIO_NAME);    
    persistToPortfolio(Arrays.asList(vanillaSwaps, oisSwaps, basisSwaps), Arrays.asList("EUR Vanilla Swaps", "EUR OIS Swaps", "EUR 3m/6m Basis Swaps"), PORTFOLIO_NAME);
  }

  private Collection<FinancialSecurity> getVanillaSwapSecurities(final Random random) {
    final ZonedDateTime tradeDate = DateUtils.getUTCDate(2013, 8, 1);
    final ZonedDateTime effectiveDate = DateUtils.getUTCDate(2013, 8, 5);
    final Collection<FinancialSecurity> securities = new ArrayList<>();
    for (int i = 0; i < N_VANILLA_SWAPS; i++) {
      final InterestRateNotional notional = new InterestRateNotional(CURRENCY, 10000000 * (1 + random.nextInt(9)));
      final int years = 1 + random.nextInt(30);
      final ZonedDateTime maturityDate = tradeDate.plusYears(years);
      final double rate = years * 0.001 + random.nextDouble() / 5000;
      final FixedInterestRateLeg fixedLeg = new FixedInterestRateLeg(ACT_360, SEMI_ANNUAL, REGION, MODIFIED_FOLLOWING, notional, false, rate);
      final Frequency frequency;
      final ExternalId euribor;
      final String frequencyLabel;
      if (random.nextBoolean()) {
        frequency = QUARTERLY;
        euribor = EURIBOR_3M;
        frequencyLabel = "3m Euribor";
      } else {
        frequency = SEMI_ANNUAL;
        euribor = EURIBOR_6M;
        frequencyLabel = "6m Euribor";
      }
      final FloatingInterestRateLeg floatingLeg = new FloatingInterestRateLeg(ACT_360, frequency, REGION, MODIFIED_FOLLOWING, notional, false, euribor, FloatingRateType.IBOR);
      final SwapSecurity swap;
      final String name;
      if (random.nextBoolean()) {
        swap = new SwapSecurity(tradeDate, effectiveDate, maturityDate, COUNTERPARTY, fixedLeg, floatingLeg);
        name = years + "Y EUR " + FORMATTER.format(notional.getAmount() / 1000000) + "MM, pay " + FORMATTER.format(rate * 100) + "% vs " + frequencyLabel;
      } else {
        swap = new SwapSecurity(tradeDate, effectiveDate, maturityDate, COUNTERPARTY, floatingLeg, fixedLeg);
        name = years + "Y EUR " + FORMATTER.format(notional.getAmount() / 1000000) + "MM, receive " + FORMATTER.format(rate * 100) + "% vs " + frequencyLabel;
      }
      swap.setName(name);
      swap.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
      securities.add(swap);
    }
    return securities;
  }

  private Collection<FinancialSecurity> getOISSwapSecurities(final Random random) {
    final ZonedDateTime tradeDate = DateUtils.getUTCDate(2013, 8, 1);
    final ZonedDateTime effectiveDate = DateUtils.getUTCDate(2013, 8, 5);
    final Collection<FinancialSecurity> securities = new ArrayList<>();
    for (int i = 0; i < N_OIS_SWAPS; i++) {
      final InterestRateNotional notional = new InterestRateNotional(CURRENCY, 10000000 * (1 + random.nextInt(9)));
      final int years = 1 + random.nextInt(30);
      final ZonedDateTime maturityDate = tradeDate.plusYears(years);
      final double rate = years * 0.001 + random.nextDouble() / 5000;
      final FixedInterestRateLeg fixedLeg = new FixedInterestRateLeg(ACT_360, SEMI_ANNUAL, REGION, MODIFIED_FOLLOWING, notional, false, rate);
      final Frequency frequency;
      if (random.nextBoolean()) {
        frequency = QUARTERLY;
      } else {
        frequency = SEMI_ANNUAL;
      }
      final FloatingInterestRateLeg floatingLeg = new FloatingInterestRateLeg(ACT_360, frequency, REGION, MODIFIED_FOLLOWING, notional, false, EONIA, FloatingRateType.OIS);
      final SwapSecurity swap;
      final String name;
      if (random.nextBoolean()) {
        swap = new SwapSecurity(tradeDate, effectiveDate, maturityDate, COUNTERPARTY, fixedLeg, floatingLeg);
        name = years + "Y EUR " + FORMATTER.format(notional.getAmount() / 1000000) + "MM, pay " + FORMATTER.format(rate * 100) + "% vs EONIA";
      } else {
        swap = new SwapSecurity(tradeDate, effectiveDate, maturityDate, COUNTERPARTY, floatingLeg, fixedLeg);
        name = years + "Y EUR " + FORMATTER.format(notional.getAmount() / 1000000) + "MM, receive " + FORMATTER.format(rate * 100) + "% vs EONIA";
      }
      swap.setName(name);
      swap.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
      securities.add(swap);
    }
    return securities;
  }

  private Collection<FinancialSecurity> getBasisSwapSecurities(final Random random) {
    final ZonedDateTime tradeDate = DateUtils.getUTCDate(2013, 8, 1);
    final ZonedDateTime effectiveDate = DateUtils.getUTCDate(2013, 8, 5);
    final Collection<FinancialSecurity> securities = new ArrayList<>();
    for (int i = 0; i < N_BASIS_SWAPS; i++) {
      final InterestRateNotional notional = new InterestRateNotional(CURRENCY, 10000000 * (1 + random.nextInt(9)));
      final int years = 1 + random.nextInt(30);
      final ZonedDateTime maturityDate = tradeDate.plusYears(years);
      final double spread = years * 0.002 + random.nextDouble() / 1000.;
      final Frequency payFrequency, receiveFrequency;
      final ExternalId payRate, receiveRate;
      final FloatingInterestRateLeg payLeg, receiveLeg;
      final String frequencyLabel;
      if (random.nextBoolean()) {
        payFrequency = QUARTERLY;
        receiveFrequency = SEMI_ANNUAL;
        payRate = EURIBOR_3M;
        receiveRate = EURIBOR_6M;
        payLeg = new FloatingSpreadIRLeg(ACT_360, payFrequency, REGION, MODIFIED_FOLLOWING, notional, false, payRate, FloatingRateType.IBOR, spread);
        receiveLeg = new FloatingInterestRateLeg(ACT_360, receiveFrequency, REGION, MODIFIED_FOLLOWING, notional, false, receiveRate, FloatingRateType.IBOR);
        frequencyLabel = "pay 3M Euribor + " + FORMATTER.format((int) (spread * 1000)) + "bp, receive 6M Euribor";
      } else {
        payFrequency = SEMI_ANNUAL;
        receiveFrequency = QUARTERLY;
        payRate = EURIBOR_6M;
        receiveRate = EURIBOR_3M;
        payLeg = new FloatingInterestRateLeg(ACT_360, payFrequency, REGION, MODIFIED_FOLLOWING, notional, false, payRate, FloatingRateType.IBOR);
        receiveLeg = new FloatingSpreadIRLeg(ACT_360, receiveFrequency, REGION, MODIFIED_FOLLOWING, notional, false, receiveRate, FloatingRateType.IBOR, spread);
        frequencyLabel = "receive 3M Euribor + " + FORMATTER.format((int) (spread * 1000)) + "bp, pay 6M Euribor";
      }
      final SwapSecurity swap = new SwapSecurity(tradeDate, effectiveDate, maturityDate, COUNTERPARTY, payLeg, receiveLeg);
      final String name = years + "Y EUR " + FORMATTER.format(notional.getAmount() / 1000000) + "MM, " + frequencyLabel;
      swap.setName(name);
      swap.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
      securities.add(swap);
    }
    return securities;
  }

  private Collection<FinancialSecurity> getIRFutureSecurities(final Random random) {
    final ZonedDateTime tradeDate = DateUtils.getUTCDate(2014, 9, 1);
    final Collection<FinancialSecurity> securities = new ArrayList<>();
    final int[] amounts = new int[N_FUTURES];
    final double[] prices = new double[N_FUTURES];
    for (int i = 0; i < N_FUTURES; i++) {
      final int n = 1 + random.nextInt(5);
      final Expiry expiry = new Expiry(tradeDate.plusMonths(3 * n).with(THIRD_WED_ADJUSTER));
      final String letter = MONTHS.get(expiry.getExpiry().getMonth());
      final String year = Integer.toString(expiry.getExpiry().getYear() - 2010);
      final String code = "ER" + letter + year;
      final FutureSecurity security = new InterestRateFutureSecurity(expiry, "XLIF", "XLIF", CURRENCY, 2500, EURIBOR_3M, "Interest rate");
      security.setName(code);
      security.setExternalIdBundle(ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId(code)));
      security.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
      securities.add(security);
      amounts[i] = 50 - random.nextInt(100);
      prices[i] = 1 - (1e-5 + random.nextDouble() / 100.);
    }
    return securities;
  }

  private void persistToPortfolio(final Collection<Collection<FinancialSecurity>> subPortfolios, final Collection<String> names, final String portfolioName) {
    final PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    final PositionMaster positionMaster = getToolContext().getPositionMaster();
    final SecurityMaster securityMaster = getToolContext().getSecurityMaster();

    final ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);
    final ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName, rootNode);
    final PortfolioDocument portfolioDoc = new PortfolioDocument();
    portfolioDoc.setPortfolio(portfolio);

    final Iterator<Collection<FinancialSecurity>> iter1 = subPortfolios.iterator();
    final Iterator<String> iter2 = names.iterator();
    final Random random = new Random(2349);
    while (iter1.hasNext()) {
      final Collection<FinancialSecurity> securities = iter1.next();
      final String name = iter2.next();
      final ManageablePortfolioNode subNode = new ManageablePortfolioNode(name);
      final ManageablePortfolio subPortfolio = new ManageablePortfolio(name, subNode);
      final PortfolioDocument subPortfolioDoc = new PortfolioDocument();
      subPortfolioDoc.setPortfolio(subPortfolio);
      for (final FinancialSecurity security : securities) {
        final SecurityDocument securityToAddDoc = new SecurityDocument();
        securityToAddDoc.setSecurity(security);
        securityMaster.add(securityToAddDoc);
        BigDecimal trades;
        if (security instanceof FutureSecurity) {
          trades = new BigDecimal(1 + (random.nextInt(150) - 75));
        } else {
          trades = BigDecimal.ONE;
        }
        final ManageablePosition securityPosition = new ManageablePosition(trades, security.getExternalIdBundle());
        final PositionDocument addedDoc = positionMaster.add(new PositionDocument(securityPosition));
        subNode.addPosition(addedDoc.getUniqueId());
      }
      portfolioMaster.add(subPortfolioDoc);
      rootNode.addChildNode(subNode);
    }
    portfolioMaster.add(portfolioDoc);
  }
}
