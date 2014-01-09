/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.generator;

import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.LeafPortfolioNodeGenerator;
import com.opengamma.financial.generator.NameGenerator;
import com.opengamma.financial.generator.PortfolioGenerator;
import com.opengamma.financial.generator.PortfolioNodeGenerator;
import com.opengamma.financial.generator.PositionGenerator;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.generator.SimplePositionGenerator;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Generates a portfolio of AUD swaps.
 */
public class AUDSwapPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {
  /** The trade date */
  private static final ZonedDateTime TRADE_DATE = DateUtils.previousWeekDay().atStartOfDay(ZoneOffset.UTC);
  /** The maturity */
  private static final ZonedDateTime MATURITY = TRADE_DATE.plusYears(4);
  /** The counterparty */
  private static final String COUNTERPARTY = "Cpty";
  /** Act/365 day-count */
  private static final DayCount ACT_365 = DayCounts.ACT_365;
  /** Act/360 day-count */
  private static final DayCount ACT_360 = DayCounts.ACT_360;
  /** Quarterly frequency */
  private static final Frequency QUARTERLY = PeriodFrequency.QUARTERLY;
  /** Semi-annual frequency */
  private static final Frequency SEMI_ANNUAL = PeriodFrequency.SEMI_ANNUAL;
  /** The region */
  private static final ExternalId REGION =  ExternalSchemes.financialRegionId("AU");
  /** Following business day convention */
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;
  /** The notional */
  private static final InterestRateNotional NOTIONAL = new InterestRateNotional(Currency.AUD, 15000000);
  /** 3m Libor ticker */
  private static final ExternalId AUD_LIBOR_3M = ExternalSchemes.syntheticSecurityId("AUDLIBORP3M");
  /** 6m Libor ticker */
  private static final ExternalId AUD_LIBOR_6M = ExternalSchemes.syntheticSecurityId("AUDLIBORP6M");
  /** The scheme that is used for these swaps */
  private static final String ID_SCHEME = "AUD_SWAP_GENERATOR";
  /** Swaps */
  private static final SwapSecurity[] SWAPS = new SwapSecurity[4];

  static {
    final FloatingInterestRateLeg payLeg1 = new FloatingInterestRateLeg(ACT_365, QUARTERLY, REGION, FOLLOWING, NOTIONAL, true, AUD_LIBOR_3M, FloatingRateType.IBOR);
    final FixedInterestRateLeg receiveLeg1 = new FixedInterestRateLeg(ACT_365, QUARTERLY, REGION, FOLLOWING, NOTIONAL, true, 0.04);
    final SwapSecurity swap1 = new SwapSecurity(TRADE_DATE, TRADE_DATE, MATURITY, COUNTERPARTY, payLeg1, receiveLeg1);
    swap1.setName("Swap AUD Bank Bill 3m");
    swap1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    final FloatingInterestRateLeg payLeg2 = new FloatingInterestRateLeg(ACT_365, SEMI_ANNUAL, REGION, FOLLOWING, NOTIONAL, true, AUD_LIBOR_6M, FloatingRateType.IBOR);
    final FixedInterestRateLeg receiveLeg2 = new FixedInterestRateLeg(ACT_365, SEMI_ANNUAL, REGION, FOLLOWING, NOTIONAL, true, 0.04);
    final SwapSecurity swap2 = new SwapSecurity(TRADE_DATE, TRADE_DATE, MATURITY, COUNTERPARTY, payLeg2, receiveLeg2);
    swap2.setName("Swap AUD Bank Bill 6m");
    swap2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    final FloatingInterestRateLeg payLeg3 = new FloatingInterestRateLeg(ACT_365, QUARTERLY, REGION, FOLLOWING, NOTIONAL, true, AUD_LIBOR_3M, FloatingRateType.IBOR);
    final FixedInterestRateLeg receiveLeg3 = new FixedInterestRateLeg(ACT_365, QUARTERLY, REGION, FOLLOWING, NOTIONAL, true, 0.0365);
    final SwapSecurity swap3 = new SwapSecurity(TRADE_DATE, TRADE_DATE, MATURITY, COUNTERPARTY, payLeg3, receiveLeg3);
    swap3.setName("Swap: receive 3.65% fixed ACT/365 vs 3m Bank Bill");
    swap3.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    final FloatingInterestRateLeg receiveLeg4 = new FloatingInterestRateLeg(ACT_365, QUARTERLY, REGION, FOLLOWING, NOTIONAL, true, AUD_LIBOR_3M, FloatingRateType.IBOR);
    final FixedInterestRateLeg payLeg4 = new FixedInterestRateLeg(ACT_360, QUARTERLY, REGION, FOLLOWING, NOTIONAL, true, 0.036);
    final SwapSecurity swap4 = new SwapSecurity(TRADE_DATE, TRADE_DATE, MATURITY, COUNTERPARTY, payLeg4, receiveLeg4);
    swap4.setName("Swap: receive 3.60% fixed ACT/360 vs 3m Bank Bill");
    swap4.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    SWAPS[0] = swap1;
    SWAPS[1] = swap2;
    SWAPS[2] = swap3;
    SWAPS[3] = swap4;
  }

  @Override
  public PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final SecurityGenerator<SwapSecurity> securities = createSwapSecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<>(securities, getSecurityPersister(), getCounterPartyGenerator());
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("AUD Swaps"), positions, 4);
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int portfolioSize) {
    final SecurityGenerator<SwapSecurity> securities = createSwapSecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<>(securities, getSecurityPersister(), getCounterPartyGenerator());
    return new LeafPortfolioNodeGenerator(new StaticNameGenerator("Swaps"), positions, 4);
  }

  private SecurityGenerator<SwapSecurity> createSwapSecurityGenerator() {
    final SecurityGenerator<SwapSecurity> securities = new SecurityGenerator<SwapSecurity>() {
      private int _count;

      @SuppressWarnings("synthetic-access")
      @Override
      public SwapSecurity createSecurity() {
        if (_count > 3) {
          throw new IllegalStateException("Should not ask for more than 4 securities");
        }
        final SwapSecurity swap = SWAPS[_count++];
        return swap;
      }

    };
    configure(securities);
    return securities;
  }
}
