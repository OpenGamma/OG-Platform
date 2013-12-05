/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.generator;

import java.math.BigDecimal;

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
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.PortfolioNodeGenerator;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.generator.SimplePositionGenerator;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.generator.TreePortfolioNodeGenerator;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Generates a portfolio of USD swaps and swaptions.
 */
public class SwaptionParityPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {
  /** The currency */
  private static final Currency CURRENCY = Currency.USD;
  /** Act/360 day-count */
  private static final DayCount ACT_360 = DayCounts.ACT_360;
  /** Act/365 day-count */
  private static final DayCount ACT_365 = DayCounts.ACT_365;
  /** 30U/360 day-count */
  private static final DayCount THIRTYU_360 = DayCounts.THIRTY_U_360;
  /** Quarterly frequency */
  private static final Frequency QUARTERLY = PeriodFrequency.QUARTERLY;
  /** Semi-annual frequency */
  private static final Frequency SEMI_ANNUAL = PeriodFrequency.SEMI_ANNUAL;
  /** Modified following business day convention */
  private static final BusinessDayConvention MODIFIED_FOLLOWING = BusinessDayConventions.MODIFIED_FOLLOWING;
  /** The holiday region */
  private static final ExternalId REGION = ExternalSchemes.financialRegionId("US+GB");
  /** The ibor ticker */
  private static final ExternalId LIBOR_3M = ExternalSchemes.syntheticSecurityId("USDLIBORP3M");
  /** The counterparty */
  private static final String COUNTERPARTY = "Cpty";

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int size) {
    final SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    final MySecurityGenerator<ManageableSecurity> firstGenerator = getSwapParityGenerator();
    final MySecurityGenerator<ManageableSecurity> secondGenerator = getSwaptionLongShortGenerator(securityMaster);
    final MySecurityGenerator<ManageableSecurity> thirdGenerator = getSwaptionConventionGenerator(securityMaster);
    final MySecurityGenerator<ManageableSecurity> fourthGenerator = getSwaptionParityGenerator(securityMaster);
    configure(firstGenerator);
    configure(secondGenerator);
    configure(thirdGenerator);
    configure(fourthGenerator);
    final TreePortfolioNodeGenerator rootNode = new TreePortfolioNodeGenerator(new StaticNameGenerator("Swap / Swaption Portfolio"));
    rootNode.addChildNode(firstGenerator);
    rootNode.addChildNode(secondGenerator);
    rootNode.addChildNode(thirdGenerator);
    rootNode.addChildNode(fourthGenerator);
    return rootNode;
  }

  private MySecurityGenerator<ManageableSecurity> getSwapParityGenerator() {
    final ZonedDateTime tradeDate = DateUtils.previousWeekDay().atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime effectiveDate = tradeDate;
    final ZonedDateTime maturity = DateUtils.getUTCDate(2024, 9, 5);
    final InterestRateNotional notional = new InterestRateNotional(CURRENCY, 10000000);
    final FloatingInterestRateLeg receiveLeg1 = new FloatingInterestRateLeg(ACT_360, QUARTERLY, REGION, MODIFIED_FOLLOWING, notional, true, LIBOR_3M, FloatingRateType.IBOR);
    final FixedInterestRateLeg payLeg1 = new FixedInterestRateLeg(THIRTYU_360, SEMI_ANNUAL, REGION, MODIFIED_FOLLOWING, notional, true, 0.02);
    final SwapSecurity swap1 = new SwapSecurity(tradeDate, effectiveDate, maturity, COUNTERPARTY, payLeg1, receiveLeg1);
    swap1.setName("Pay Fixed @ 2% v USD 3m Libor");
    final SwapSecurity swap2 = new SwapSecurity(tradeDate, effectiveDate, maturity, COUNTERPARTY, receiveLeg1, payLeg1);
    swap2.setName("Receive Fixed @ 2% v USD 3m Libor");
    final SwapSecurity[] swapParity = new SwapSecurity[] {swap1, swap2};
    final ZonedDateTime[] tradeDates = new ZonedDateTime[] {tradeDate, tradeDate};
    return new MySecurityGenerator<>(swapParity, tradeDates, "Swap payer / receiver parity");
  }

  private MySecurityGenerator<ManageableSecurity> getSwaptionLongShortGenerator(final SecurityMaster securityMaster) {
    final ZonedDateTime tradeDate = DateUtils.previousWeekDay().atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime effectiveDate = tradeDate.plusYears(1);
    final ZonedDateTime maturityDate = effectiveDate.plusYears(10);
    final Expiry expiry = new Expiry(effectiveDate.minusDays(2));
    final InterestRateNotional notional = new InterestRateNotional(CURRENCY, 10000000);
    final FloatingInterestRateLeg payLeg = new FloatingInterestRateLeg(ACT_360, QUARTERLY, REGION, MODIFIED_FOLLOWING, notional, true, LIBOR_3M, FloatingRateType.IBOR);
    final FixedInterestRateLeg receiveLeg = new FixedInterestRateLeg(THIRTYU_360, SEMI_ANNUAL, REGION, MODIFIED_FOLLOWING, notional, true, 0.02);
    final SwapSecurity underlyingSwap = new SwapSecurity(tradeDate, effectiveDate, maturityDate, COUNTERPARTY, payLeg, receiveLeg);
    underlyingSwap.setName("Receive fixed @ 2% v USD 3m Libor");
    final SecurityDocument toAddDoc = new SecurityDocument();
    toAddDoc.setSecurity(underlyingSwap);
    securityMaster.add(toAddDoc);
    final ZonedDateTime[] tradeDates = new ZonedDateTime[] {tradeDate, tradeDate};
    final ExternalId underlyingId = getSecurityPersister().storeSecurity(underlyingSwap).iterator().next();
    final SwaptionSecurity swaption1 = new SwaptionSecurity(true, underlyingId, true, expiry, false, CURRENCY);
    swaption1.setName("Long payer 1Yx10Y @ 2%");
    final SwaptionSecurity swaption2 = new SwaptionSecurity(true, underlyingId, false, expiry, false, CURRENCY);
    swaption2.setName("Short payer 1Yx10Y @ 2%");
    final SwaptionSecurity[] swaptionParity = new SwaptionSecurity[] {swaption1, swaption2};
    return new MySecurityGenerator<>(swaptionParity, tradeDates, "Swaption long / short parity");
  }

  private MySecurityGenerator<ManageableSecurity> getSwaptionConventionGenerator(final SecurityMaster securityMaster) {
    final ZonedDateTime tradeDate = DateUtils.previousWeekDay().atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime effectiveDate = tradeDate.plusYears(1);
    final ZonedDateTime maturityDate = effectiveDate.plusYears(10);
    final Expiry expiry = new Expiry(effectiveDate.minusDays(2));
    final InterestRateNotional notional = new InterestRateNotional(CURRENCY, 10000000);
    final FloatingInterestRateLeg payLeg = new FloatingInterestRateLeg(ACT_360, QUARTERLY, REGION, MODIFIED_FOLLOWING, notional, true, LIBOR_3M, FloatingRateType.IBOR);
    final FixedInterestRateLeg receiveLeg1 = new FixedInterestRateLeg(ACT_360, SEMI_ANNUAL, REGION, MODIFIED_FOLLOWING, notional, true, 0.018);
    final FixedInterestRateLeg receiveLeg2 = new FixedInterestRateLeg(ACT_365, SEMI_ANNUAL, REGION, MODIFIED_FOLLOWING, notional, true, 0.01825);
    final SwapSecurity underlyingSwap1 = new SwapSecurity(tradeDate, effectiveDate, maturityDate, COUNTERPARTY, payLeg, receiveLeg1);
    underlyingSwap1.setName("Receive fixed @ 1.8% v USD 3m Libor");
    final SwapSecurity underlyingSwap2 = new SwapSecurity(tradeDate, effectiveDate, maturityDate, COUNTERPARTY, payLeg, receiveLeg2);
    underlyingSwap2.setName("Receive fixed @ 1.825% v USD 3m Libor");
    SecurityDocument toAddDoc = new SecurityDocument();
    toAddDoc.setSecurity(underlyingSwap1);
    securityMaster.add(toAddDoc);
    toAddDoc = new SecurityDocument();
    toAddDoc.setSecurity(underlyingSwap2);
    securityMaster.add(toAddDoc);
    final ExternalId underlyingId1 = getSecurityPersister().storeSecurity(underlyingSwap1).iterator().next();
    final ExternalId underlyingId2 = getSecurityPersister().storeSecurity(underlyingSwap2).iterator().next();
    final ZonedDateTime[] tradeDates = new ZonedDateTime[] {tradeDate, tradeDate};
    final SwaptionSecurity swaption1 = new SwaptionSecurity(false, underlyingId1, true, expiry, false, CURRENCY);
    swaption1.setName("Long receiver 1Yx10Y @ 1.8% - ACT/360");
    final SwaptionSecurity swaption2 = new SwaptionSecurity(false, underlyingId2, false, expiry, false, CURRENCY);
    swaption2.setName("Short receiver 1Yx10Y @ 1.825% - ACT/365");
    final SwaptionSecurity[] swaptionParity = new SwaptionSecurity[] {swaption1, swaption2};
    return new MySecurityGenerator<>(swaptionParity, tradeDates, "Swaption convention parity");
  }

  private MySecurityGenerator<ManageableSecurity> getSwaptionParityGenerator(final SecurityMaster securityMaster) {
    final ZonedDateTime tradeDate = DateUtils.previousWeekDay().atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime effectiveDate = tradeDate.plusYears(1);
    final ZonedDateTime maturityDate = effectiveDate.plusYears(10);
    final Expiry expiry = new Expiry(effectiveDate.minusDays(2));
    final InterestRateNotional notional = new InterestRateNotional(CURRENCY, 10000000);
    final FloatingInterestRateLeg payLeg = new FloatingInterestRateLeg(ACT_360, QUARTERLY, REGION, MODIFIED_FOLLOWING, notional, true, LIBOR_3M, FloatingRateType.IBOR);
    final FixedInterestRateLeg receiveLeg = new FixedInterestRateLeg(THIRTYU_360, SEMI_ANNUAL, REGION, MODIFIED_FOLLOWING, notional, true, 0.02);
    final SwapSecurity underlyingSwap1 = new SwapSecurity(tradeDate, effectiveDate, maturityDate, COUNTERPARTY, payLeg, receiveLeg);
    underlyingSwap1.setName("Receive fixed @ 2% v USD 3m Libor");
    final SwapSecurity underlyingSwap2 = new SwapSecurity(tradeDate, effectiveDate, maturityDate, COUNTERPARTY, receiveLeg, payLeg);
    underlyingSwap2.setName("Pay fixed @ 2% v USD 3m Libor");
    SecurityDocument toAddDoc = new SecurityDocument();
    toAddDoc.setSecurity(underlyingSwap1);
    securityMaster.add(toAddDoc);
    toAddDoc = new SecurityDocument();
    toAddDoc.setSecurity(underlyingSwap2);
    securityMaster.add(toAddDoc);
    final ExternalId underlyingId1 = getSecurityPersister().storeSecurity(underlyingSwap1).iterator().next();
    final ExternalId underlyingId2 = getSecurityPersister().storeSecurity(underlyingSwap2).iterator().next();
    final ZonedDateTime[] tradeDates = new ZonedDateTime[] {tradeDate, tradeDate, tradeDate};
    final SwaptionSecurity swaption1 = new SwaptionSecurity(true, underlyingId1, true, expiry, false, CURRENCY);
    swaption1.setName("Long payer 1Yx10Y @ 2%");
    final SwaptionSecurity swaption2 = new SwaptionSecurity(true, underlyingId2, false, expiry, false, CURRENCY);
    swaption2.setName("Short receiver 1Yx10Y @ 2%");
    final ManageableSecurity[] swaptionParity = new ManageableSecurity[] {underlyingSwap2, swaption1, swaption2};
    return new MySecurityGenerator<>(swaptionParity, tradeDates, "Swaption payer / receiver parity");
  }

  /**
   * Generates trades and adds them to a portfolio.
   * @param <T> The type of the security
   */
  private class MySecurityGenerator<T extends ManageableSecurity> extends SecurityGenerator<T> implements PortfolioNodeGenerator {
    /** The securities */
    private final ManageableSecurity[] _securities;
    /** The trade dates */
    private final ZonedDateTime[] _tradeDates;
    /** The name */
    private final String _name;

    public MySecurityGenerator(final ManageableSecurity[] securities, final ZonedDateTime[] tradeDates, final String name) {
      _securities = securities;
      _tradeDates = tradeDates;
      _name = name;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public PortfolioNode createPortfolioNode() {
      final SimplePortfolioNode node = new SimplePortfolioNode(_name);
      for (int i = 0; i < _securities.length; i++) {
        final ManageableTrade trade = new ManageableTrade(BigDecimal.ONE, getSecurityPersister().storeSecurity(_securities[i]), _tradeDates[i].toLocalDate(),
            _tradeDates[i].toOffsetDateTime().toOffsetTime(), ExternalId.of(Counterparty.DEFAULT_SCHEME, COUNTERPARTY));
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
}
