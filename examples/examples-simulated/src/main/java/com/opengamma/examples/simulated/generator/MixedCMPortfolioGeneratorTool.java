/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.generator;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Random;

import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.PortfolioNodeGenerator;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.generator.SimplePositionGenerator;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.generator.TreePortfolioNodeGenerator;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 * Creates a portfolio of USD constant-maturity swaps, cap-floors and cap-floor CMS spreads.
 */
public class MixedCMPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {
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
  private static final ExternalId LIBOR_3M = ExternalSchemes.syntheticSecurityId("USDLIBORP3M");
  /** 6m Libor ticker */
  private static final ExternalId LIBOR_6M = ExternalSchemes.syntheticSecurityId("USDLIBORP6M");
  /** The tenors */
  private static final Tenor[] TENORS = new Tenor[] {Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FIVE_YEARS, Tenor.TEN_YEARS};
  /** The strikes */
  private static final double[] STRIKES = new double[] {0.01, 0.02, 0.03};
  /** The trade date */
  private static final ZonedDateTime TRADE_DATE = DateUtils.previousWeekDay().atStartOfDay(ZoneOffset.UTC);
  /** The pay tenors */
  private static final Tenor[] PAY_TENORS = new Tenor[] {Tenor.ONE_YEAR, Tenor.FIVE_YEARS};
  /** The receive tenors */
  private static final Tenor[] RECEIVE_TENORS = new Tenor[] {Tenor.TWO_YEARS, Tenor.TEN_YEARS};
  /** The strike formatter */
  private static final DecimalFormat FORMAT = new DecimalFormat("##.##");
  /** Random number generator */
  private static final Random RANDOM = new Random(345);

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int size) {
    final CapFloorSecurityGenerator capFloors = new CapFloorSecurityGenerator(TRADE_DATE, TENORS, STRIKES);
    final CMSSwapSecurityGenerator cmsSwap = new CMSSwapSecurityGenerator(TRADE_DATE, TENORS);
    final CMSCapFloorSpreadSecurityGenerator cmsSpreads = new CMSCapFloorSpreadSecurityGenerator(TRADE_DATE, PAY_TENORS, RECEIVE_TENORS, TENORS, STRIKES);
    configure(capFloors);
    configure(cmsSwap);
    configure(cmsSpreads);
    final TreePortfolioNodeGenerator rootNode = new TreePortfolioNodeGenerator(new StaticNameGenerator("Mixed CM Portfolio"));
    rootNode.addChildNode(capFloors);
    rootNode.addChildNode(cmsSwap);
    rootNode.addChildNode(cmsSpreads);
    return rootNode;
  }

  /**
   * Generates cap-floors.
   */
  private class CapFloorSecurityGenerator extends SecurityGenerator<CapFloorSecurity> implements PortfolioNodeGenerator {
    /** The notional */
    private final double _notional = 10000000;
    /** The trade date */
    private final ZonedDateTime _tradeDate;
    /** The tenors */
    private final Tenor[] _tenors;
    /** The strikes */
    private final double[] _strikes;

    public CapFloorSecurityGenerator(final ZonedDateTime tradeDate, final Tenor[] tenors, final double[] strikes) {
      _tradeDate = tradeDate;
      _tenors = tenors;
      _strikes = strikes;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public PortfolioNode createPortfolioNode() {
      final SimplePortfolioNode node = new SimplePortfolioNode("CM Cap/Floor");
      for (final Tenor tenor : _tenors) {
        for (final double strike : _strikes) {
          final CapFloorSecurity cap = createCapFloor(tenor, strike);
          final ManageableTrade trade = new ManageableTrade(BigDecimal.ONE, getSecurityPersister().storeSecurity(cap), _tradeDate.toLocalDate(),
              _tradeDate.toOffsetDateTime().toOffsetTime(), ExternalId.of(Counterparty.DEFAULT_SCHEME, COUNTERPARTY));
          trade.setPremium(0.);
          trade.setPremiumCurrency(CURRENCY);
          final Position position = SimplePositionGenerator.createPositionFromTrade(trade);
          node.addPosition(position);
        }
      }
      return node;
    }

    @Override
    public CapFloorSecurity createSecurity() {
      return null;
    }

    @SuppressWarnings("synthetic-access")
    private CapFloorSecurity createCapFloor(final Tenor tenor, final double strike) {
      final ZonedDateTime maturityDate = _tradeDate.plus(tenor.getPeriod());
      final boolean payer = RANDOM.nextBoolean();
      final boolean cap = RANDOM.nextBoolean();
      final String ticker = "USDISDA10" + tenor.getPeriod().toString();
      final ExternalId underlyingIdentifier = ExternalSchemes.syntheticSecurityId(ticker);
      final CapFloorSecurity security = new CapFloorSecurity(_tradeDate, maturityDate, _notional, underlyingIdentifier, strike, PeriodFrequency.SEMI_ANNUAL,
          CURRENCY, ACT_360, payer, cap, false);
      security.setName(CURRENCY.getCode() + " " + FORMAT.format(_notional / 1000000) + (cap ? "MM cap " : "MM floor ") + "@ " + FORMAT.format(strike) +
          (payer ? "%, pay " : "%, receive ") + tenor.getPeriod().normalized().getYears() + "Y ISDA fixing" +
          " (" + _tradeDate.toLocalDate().toString() + " - " + maturityDate.toLocalDate().toString() + ")");
      return security;
    }
  }

  /**
   * Generates constant-maturity swaps.
   */
  private class CMSSwapSecurityGenerator extends SecurityGenerator<SwapSecurity> implements PortfolioNodeGenerator {
    /** The notional */
    private final InterestRateNotional _notional = new InterestRateNotional(Currency.USD, 150000000);
    /** The trade date */
    private final ZonedDateTime _tradeDate;
    /** The tenors */
    private final Tenor[] _tenors;

    public CMSSwapSecurityGenerator(final ZonedDateTime tradeDate, final Tenor[] tenors) {
      _tradeDate = tradeDate;
      _tenors = tenors;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public PortfolioNode createPortfolioNode() {
      final SimplePortfolioNode node = new SimplePortfolioNode("CM Swap");
      for (final Tenor tenor : _tenors) {
        final SwapSecurity swap = createSwap(tenor);
        final ManageableTrade trade = new ManageableTrade(BigDecimal.ONE, getSecurityPersister().storeSecurity(swap), _tradeDate.toLocalDate(),
            _tradeDate.toOffsetDateTime().toOffsetTime(), ExternalId.of(Counterparty.DEFAULT_SCHEME, COUNTERPARTY));
        trade.setPremium(0.);
        trade.setPremiumCurrency(CURRENCY);
        final Position position = SimplePositionGenerator.createPositionFromTrade(trade);
        node.addPosition(position);
      }
      return node;
    }

    @Override
    public SwapSecurity createSecurity() {
      return null;
    }

    @SuppressWarnings("synthetic-access")
    private SwapSecurity createSwap(final Tenor tenor) {
      final ZonedDateTime maturityDate = _tradeDate.plus(tenor.getPeriod());
      ExternalId iborReferenceRate;
      PeriodFrequency frequency;
      if (RANDOM.nextBoolean()) {
        iborReferenceRate = LIBOR_3M;
        frequency = PeriodFrequency.QUARTERLY;
      } else {
        iborReferenceRate = LIBOR_6M;
        frequency = PeriodFrequency.SEMI_ANNUAL;
      }
      final FloatingInterestRateLeg iborLeg = new FloatingInterestRateLeg(ACT_360, frequency, REGION, FOLLOWING, _notional, true,
          iborReferenceRate, FloatingRateType.IBOR);
      final String ticker = "USDISDA10" + tenor.getPeriod().toString();
      final ExternalId cmsReferenceRate = ExternalSchemes.syntheticSecurityId(ticker);
      final FloatingInterestRateLeg cmsLeg = new FloatingInterestRateLeg(ACT_360, frequency, REGION, FOLLOWING, _notional, true,
          cmsReferenceRate, FloatingRateType.CMS);
      SwapSecurity security;
      boolean payIbor;
      if (RANDOM.nextBoolean()) {
        security = new SwapSecurity(_tradeDate, _tradeDate, maturityDate, COUNTERPARTY, iborLeg, cmsLeg);
        payIbor = true;
      } else {
        security = new SwapSecurity(_tradeDate, _tradeDate, maturityDate, COUNTERPARTY, cmsLeg, iborLeg);
        payIbor = false;
      }
      security.setName(CURRENCY.getCode() + " " + FORMAT.format(_notional.getAmount() / 1000000) + "MM Swap, pay " +
          (payIbor ? frequency.getPeriod().getMonths() + "M Libor, receive " + tenor.getPeriod().getYears() + "Y ISDA fixing (" :
            tenor.getPeriod().getYears() + "Y ISDA fixing, receive " + frequency.getPeriod().getMonths() + "M Libor (") +
            _tradeDate.toLocalDate().toString() + " - " + maturityDate.toLocalDate().toString() + ")");
      return security;
    }
  }

  /**
   * Generates cap-floor CMS spreads.
   */
  private class CMSCapFloorSpreadSecurityGenerator extends SecurityGenerator<CapFloorSecurity> implements PortfolioNodeGenerator {
    /** The notional */
    private final double _notional = 34000000;
    /** The trade date */
    private final ZonedDateTime _tradeDate;
    /** The pay tenors */
    private final Tenor[] _payTenors;
    /** The receive tenors */
    private final Tenor[] _receiveTenors;
    /** The maturities */
    private final Tenor[] _maturities;
    /** The strikes */
    private final double[] _strikes;

    public CMSCapFloorSpreadSecurityGenerator(final ZonedDateTime tradeDate, final Tenor[] payTenors, final Tenor[] receiveTenors,
        final Tenor[] maturities, final double[] strikes) {
      _tradeDate = tradeDate;
      _payTenors = payTenors;
      _receiveTenors = receiveTenors;
      _maturities = maturities;
      _strikes = strikes;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public PortfolioNode createPortfolioNode() {
      final SimplePortfolioNode node = new SimplePortfolioNode("CM Cap / Floor Spread");
      for (final Tenor payTenor : _payTenors) {
        for (final Tenor receiveTenor : _receiveTenors) {
          for (final Tenor maturity : _maturities) {
            for (final double strike : _strikes) {
              final CapFloorCMSSpreadSecurity cap = createCMSCapFloorSpread(payTenor, receiveTenor, maturity, strike);
              final ManageableTrade trade = new ManageableTrade(BigDecimal.ONE, getSecurityPersister().storeSecurity(cap), _tradeDate.toLocalDate(),
                  _tradeDate.toOffsetDateTime().toOffsetTime(), ExternalId.of(Counterparty.DEFAULT_SCHEME, COUNTERPARTY));
              trade.setPremium(0.);
              trade.setPremiumCurrency(CURRENCY);
              final Position position = SimplePositionGenerator.createPositionFromTrade(trade);
              node.addPosition(position);
            }
          }
        }
      }
      return node;
    }

    @Override
    public CapFloorSecurity createSecurity() {
      return null;
    }

    @SuppressWarnings("synthetic-access")
    private CapFloorCMSSpreadSecurity createCMSCapFloorSpread(final Tenor payTenor, final Tenor receiveTenor, final Tenor maturity, final double strike) {
      final ZonedDateTime maturityDate = _tradeDate.plus(maturity.getPeriod());
      final boolean payer = RANDOM.nextBoolean();
      final boolean cap = RANDOM.nextBoolean();
      final String payTicker = "USDISDA10" + payTenor.getPeriod().toString();
      final ExternalId payIdentifier = ExternalSchemes.syntheticSecurityId(payTicker);
      final String receiveTicker = "USDISDA10" + receiveTenor.getPeriod().toString();
      final ExternalId receiveIdentifier = ExternalSchemes.syntheticSecurityId(receiveTicker);
      final CapFloorCMSSpreadSecurity security = new CapFloorCMSSpreadSecurity(_tradeDate, maturityDate, _notional, payIdentifier, receiveIdentifier, strike,
          PeriodFrequency.ANNUAL, CURRENCY, ACT_360, payer, cap);
      security.setName(CURRENCY.getCode() + " " + FORMAT.format(_notional / 1000000) + (cap ? "MM cap spread " : "MM floor spread ") + "@ " + FORMAT.format(strike) +
          "%, pay " + payTenor.getPeriod().normalized().getYears() + "Y ISDA fixing" + ", receive " +
          receiveTenor.getPeriod().normalized().getYears() + "Y ISDA fixing" +
          " (" + _tradeDate.toLocalDate().toString() + " - " + maturityDate.toLocalDate().toString() + ")");
      return security;
    }
  }
}
