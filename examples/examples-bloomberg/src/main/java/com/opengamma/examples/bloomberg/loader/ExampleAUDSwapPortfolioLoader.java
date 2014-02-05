/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.bloomberg.loader;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;

import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
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
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.ShutdownUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Example code to load a portfolio of four AUD swaps.
 * <p>
 * This code is kept deliberately as simple as possible.
 * There are no checks for the securities or portfolios already existing, so if you run it
 * more than once you will get multiple copies portfolios and securities with the same names.
 * It is designed to run against the HSQLDB example database.
 */
public class ExampleAUDSwapPortfolioLoader extends AbstractTool<IntegrationToolContext> {

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
  private static final ExternalId AUD_LIBOR_3M = ExternalSchemes.bloombergTickerSecurityId("AU0003M Index");
  /** 6m Libor ticker */
  private static final ExternalId AUD_LIBOR_6M = ExternalSchemes.bloombergTickerSecurityId("AU0006M Index");
  /** The scheme used for an identifier */
  private static final String ID_SCHEME = "AUD_SWAP_GENERATOR";
  /** The portfolio name */
  public static final String PORTFOLIO_NAME = "AUD Swap Portfolio";

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
          new ExampleAUDSwapPortfolioLoader().initAndRun(args, IntegrationToolContext.class);
      ShutdownUtils.exit(success ? 0 : -1);
    } catch (Throwable ex) {
      ex.printStackTrace();
      ShutdownUtils.exit(-2);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
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
    final Collection<SwapSecurity> swaps = new HashSet<>();
    swaps.add(swap1);
    swaps.add(swap2);
    swaps.add(swap3);
    swaps.add(swap4);
    persistToPortfolio(swaps, PORTFOLIO_NAME);
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
