/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.generator;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.Month;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.PortfolioNodeGenerator;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.generator.SimplePositionGenerator;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.generator.TreePortfolioNodeGenerator;
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
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Generates a portfolio of EUR vanilla swaps, basis swaps, OIS and futures.
 */
public class EURFixedIncomePortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {
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
  private static final ExternalId EURIBOR_3M = ExternalSchemes.syntheticSecurityId("EUREURIBORP3M");
  /** The 6m ibor ticker */
  private static final ExternalId EURIBOR_6M = ExternalSchemes.syntheticSecurityId("EUREURIBORP6M");
  /** The EONIA ticker */
  private static final ExternalId EONIA = ExternalSchemes.syntheticSecurityId("EONIA");
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
  /** A random number generator */
  private static final Random RANDOM = new Random(1235);

  static {
    MONTHS = new HashMap<>();
    MONTHS.put(Month.MARCH, "H");
    MONTHS.put(Month.JUNE, "M");
    MONTHS.put(Month.SEPTEMBER, "U");
    MONTHS.put(Month.DECEMBER, "Z");
  }

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int size) {
    final MySecurityGenerator<ManageableSecurity> firstGenerator = getBasisSwapSecurityGenerator();
    final FutureSecurityGenerator<ManageableSecurity> secondGenerator = getIRFutureSecurityGenerator();
    final MySecurityGenerator<ManageableSecurity> thirdGenerator = getOISSwapSecurityGenerator();
    final MySecurityGenerator<ManageableSecurity> fourthGenerator = getVanillaSwapSecurityGenerator();
    configure(firstGenerator);
    configure(secondGenerator);
    configure(thirdGenerator);
    configure(fourthGenerator);
    final TreePortfolioNodeGenerator rootNode = new TreePortfolioNodeGenerator(new StaticNameGenerator("EUR Fixed Income Portfolio"));
    rootNode.addChildNode(firstGenerator);
    rootNode.addChildNode(secondGenerator);
    rootNode.addChildNode(thirdGenerator);
    rootNode.addChildNode(fourthGenerator);
    return rootNode;
  }

  private MySecurityGenerator<ManageableSecurity> getVanillaSwapSecurityGenerator() {
    final ZonedDateTime tradeDate = DateUtils.previousWeekDay().atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime effectiveDate = tradeDate;
    final SwapSecurity[] securities = new SwapSecurity[N_VANILLA_SWAPS];
    for (int i = 0; i < N_VANILLA_SWAPS; i++) {
      final InterestRateNotional notional = new InterestRateNotional(CURRENCY, 10000000 * (1 + RANDOM.nextInt(9)));
      final int years = 1 + RANDOM.nextInt(30);
      final ZonedDateTime maturityDate = tradeDate.plusYears(years);
      final double rate = years * 0.001 + RANDOM.nextDouble() / 5000;
      final FixedInterestRateLeg fixedLeg = new FixedInterestRateLeg(ACT_360, SEMI_ANNUAL, REGION, MODIFIED_FOLLOWING, notional, false, rate);
      final Frequency frequency;
      final ExternalId euribor;
      final String frequencyLabel;
      if (RANDOM.nextBoolean()) {
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
      if (RANDOM.nextBoolean()) {
        swap = new SwapSecurity(tradeDate, effectiveDate, maturityDate, COUNTERPARTY, fixedLeg, floatingLeg);
        name = years + "Y EUR " + FORMATTER.format(notional.getAmount() / 1000000) + "MM, pay " + FORMATTER.format(rate * 100) + "% vs " + frequencyLabel;
      } else {
        swap = new SwapSecurity(tradeDate, effectiveDate, maturityDate, COUNTERPARTY, floatingLeg, fixedLeg);
        name = years + "Y EUR " + FORMATTER.format(notional.getAmount() / 1000000) + "MM, receive " + FORMATTER.format(rate * 100) + "% vs " + frequencyLabel;
      }
      swap.setName(name);
      securities[i] = swap;
    }
    return new MySecurityGenerator<>(securities, tradeDate, "Vanilla swaps");
  }

  private MySecurityGenerator<ManageableSecurity> getOISSwapSecurityGenerator() {
    final ZonedDateTime tradeDate = DateUtils.previousWeekDay().atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime effectiveDate = tradeDate;
    final SwapSecurity[] securities = new SwapSecurity[N_OIS_SWAPS];
    for (int i = 0; i < N_OIS_SWAPS; i++) {
      final InterestRateNotional notional = new InterestRateNotional(CURRENCY, 10000000 * (1 + RANDOM.nextInt(9)));
      final int years = 1 + RANDOM.nextInt(30);
      final ZonedDateTime maturityDate = tradeDate.plusYears(years);
      final double rate = years * 0.001 + RANDOM.nextDouble() / 5000;
      final FixedInterestRateLeg fixedLeg = new FixedInterestRateLeg(ACT_360, SEMI_ANNUAL, REGION, MODIFIED_FOLLOWING, notional, false, rate);
      final Frequency frequency;
      if (RANDOM.nextBoolean()) {
        frequency = QUARTERLY;
      } else {
        frequency = SEMI_ANNUAL;
      }
      final FloatingInterestRateLeg floatingLeg = new FloatingInterestRateLeg(ACT_360, frequency, REGION, MODIFIED_FOLLOWING, notional, false, EONIA, FloatingRateType.OIS);
      final SwapSecurity swap;
      final String name;
      if (RANDOM.nextBoolean()) {
        swap = new SwapSecurity(tradeDate, effectiveDate, maturityDate, COUNTERPARTY, fixedLeg, floatingLeg);
        name = years + "Y EUR " + FORMATTER.format(notional.getAmount() / 1000000) + "MM, pay " + FORMATTER.format(rate * 100) + "% vs EONIA";
      } else {
        swap = new SwapSecurity(tradeDate, effectiveDate, maturityDate, COUNTERPARTY, floatingLeg, fixedLeg);
        name = years + "Y EUR " + FORMATTER.format(notional.getAmount() / 1000000) + "MM, receive " + FORMATTER.format(rate * 100) + "% vs EONIA";
      }
      swap.setName(name);
      securities[i] = swap;
    }
    return new MySecurityGenerator<>(securities, tradeDate, "OIS swaps");
  }

  private MySecurityGenerator<ManageableSecurity> getBasisSwapSecurityGenerator() {
    final ZonedDateTime tradeDate = DateUtils.previousWeekDay().atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime effectiveDate = tradeDate;
    final SwapSecurity[] securities = new SwapSecurity[N_BASIS_SWAPS];
    for (int i = 0; i < N_BASIS_SWAPS; i++) {
      final InterestRateNotional notional = new InterestRateNotional(CURRENCY, 10000000 * (1 + RANDOM.nextInt(9)));
      final int years = 1 + RANDOM.nextInt(30);
      final ZonedDateTime maturityDate = tradeDate.plusYears(years);
      final double spread = years * 0.002 + RANDOM.nextDouble() / 1000.;
      final Frequency payFrequency, receiveFrequency;
      final ExternalId payRate, receiveRate;
      final FloatingInterestRateLeg payLeg, receiveLeg;
      final String frequencyLabel;
      if (RANDOM.nextBoolean()) {
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
      securities[i] = swap;
    }
    return new MySecurityGenerator<>(securities, tradeDate, "Basis swaps");
  }

  private FutureSecurityGenerator<ManageableSecurity> getIRFutureSecurityGenerator() {    
    final ZonedDateTime tradeDate = DateUtils.getUTCDate(2014, 9, 1);
    final FutureSecurity[] securities = new FutureSecurity[N_FUTURES];
    final int[] amounts = new int[N_FUTURES];
    final double[] prices = new double[N_FUTURES];
    for (int i = 0; i < N_FUTURES; i++) {
      final int n = 1 + RANDOM.nextInt(20);
      final Expiry expiry = new Expiry(tradeDate.plusMonths(3 * n).with(THIRD_WED_ADJUSTER));
      final String letter = MONTHS.get(expiry.getExpiry().getMonth());
      final String year = Integer.toString(expiry.getExpiry().getYear() - 2000);
      final String code = "ER" + letter + year;
      final FutureSecurity security = new InterestRateFutureSecurity(expiry, "EUREX", "EUREX", CURRENCY, 2500, EURIBOR_3M, "Interest rate");
      security.setName(code);
      security.setExternalIdBundle(ExternalIdBundle.of(ExternalSchemes.syntheticSecurityId(code)));
      securities[i] = security;
      amounts[i] = RANDOM.nextInt(100) - 50;
      prices[i] = 1 - (1e-5 + RANDOM.nextDouble() / 100.);
    }
    return new FutureSecurityGenerator<>(securities, amounts, prices, tradeDate, "Euribor futures");
  }

  /**
   * Generates trades and adds them to a portfolio.
   * @param <T> The type of the security
   */
  private class MySecurityGenerator<T extends ManageableSecurity> extends SecurityGenerator<T> implements PortfolioNodeGenerator {
    /** The securities */
    private final ManageableSecurity[] _securities;
    /** The trade date */
    private final ZonedDateTime _tradeDate;
    /** The name */
    private final String _name;

    public MySecurityGenerator(final ManageableSecurity[] securities, final ZonedDateTime tradeDate, final String name) {
      _securities = securities;
      _tradeDate = tradeDate;
      _name = name;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public PortfolioNode createPortfolioNode() {
      final SimplePortfolioNode node = new SimplePortfolioNode(_name);
      for (final ManageableSecurity security : _securities) {
        final ManageableTrade trade = new ManageableTrade(BigDecimal.ONE, getSecurityPersister().storeSecurity(security), _tradeDate.toLocalDate(),
            _tradeDate.toOffsetDateTime().toOffsetTime(), ExternalId.of(Counterparty.DEFAULT_SCHEME, COUNTERPARTY));
        trade.setPremium(0.);
        trade.setPremiumCurrency(CURRENCY);
        final Position position = SimplePositionGenerator.createPositionFromTrade(trade);
        node.addPosition(position);
      }
      return node;
    }

    @Override
    public T createSecurity() {
      return null;
    }
  }

  /**
   * Generates future trades and adds them to a portfolio.
   * @param <T> The type of the security
   */
  private class FutureSecurityGenerator<T extends ManageableSecurity> extends SecurityGenerator<T> implements PortfolioNodeGenerator {
    /** The securities */
    private final ManageableSecurity[] _securities;
    /** The amounts */
    private final int[] _amounts;
    /** The prices */
    private final double[] _prices;
    /** The trade date */
    private final ZonedDateTime _tradeDate;
    /** The name */
    private final String _name;

    public FutureSecurityGenerator(final ManageableSecurity[] securities, final int[] amounts, final double[] prices, final ZonedDateTime tradeDate, final String name) {
      _securities = securities;
      _amounts = amounts;
      _prices = prices;
      _tradeDate = tradeDate;
      _name = name;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public PortfolioNode createPortfolioNode() {
      final SimplePortfolioNode node = new SimplePortfolioNode(_name);
      for (int i = 0; i < _securities.length; i++) {
        final BigDecimal n = new BigDecimal(_amounts[i]);
        final double premium = _prices[i];
        final ManageableTrade trade = new ManageableTrade(n, getSecurityPersister().storeSecurity(_securities[i]), _tradeDate.toLocalDate(),
            _tradeDate.toOffsetDateTime().toOffsetTime(), ExternalId.of(Counterparty.DEFAULT_SCHEME, COUNTERPARTY));
        trade.setPremium(premium);
        trade.setPremiumCurrency(CURRENCY);
        final Position position = SimplePositionGenerator.createPositionFromTrade(trade);
        node.addPosition(position);
      }
      return node;
    }

    @Override
    public T createSecurity() {
      return null;
    }
  }
}
