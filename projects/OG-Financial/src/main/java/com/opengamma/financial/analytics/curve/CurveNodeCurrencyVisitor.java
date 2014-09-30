/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.link.ConventionLink;
import com.opengamma.core.link.SecurityLink;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.ircurve.strips.BillNode;
import com.opengamma.financial.analytics.ircurve.strips.BondNode;
import com.opengamma.financial.analytics.ircurve.strips.CalendarSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.DeliverableSwapFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.ircurve.strips.FXSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.PeriodicallyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateFRANode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ThreeLegBasisSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.financial.convention.BondConvention;
import com.opengamma.financial.convention.CMSLegConvention;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.DeliverablePriceQuotedSwapFutureConvention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.EquityConvention;
import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.FederalFundsFutureConvention;
import com.opengamma.financial.convention.FinancialConvention;
import com.opengamma.financial.convention.FinancialConventionVisitor;
import com.opengamma.financial.convention.FixedInterestRateSwapLegConvention;
import com.opengamma.financial.convention.FixedLegRollDateConvention;
import com.opengamma.financial.convention.FloatingInterestRateSwapLegConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.ONArithmeticAverageLegConvention;
import com.opengamma.financial.convention.ONCompoundedLegRollDateConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.convention.RollDateFRAConvention;
import com.opengamma.financial.convention.RollDateSwapConvention;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.VanillaIborLegRollDateConvention;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.financial.security.index.PriceIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * Returns all of the currencies relevant for a {@link CurveNode}. This information is pulled from
 * the convention(s) associated with the node. Returns null if there are no currencies applicable
 * to the curve node.
 */
public class CurveNodeCurrencyVisitor implements CurveNodeVisitor<Set<Currency>>, FinancialConventionVisitor<Set<Currency>> {

  /**
   * Creates the visitor.
   */
  public CurveNodeCurrencyVisitor() {
  }

  /**
   * @param securitySource The security source. Not null.
   * @param conventionSource The convention source, not null
   * @deprecated use the no-arg constructor
   */
  @Deprecated
  public CurveNodeCurrencyVisitor(ConventionSource conventionSource, SecuritySource securitySource) {
  }

  /**
   * @param securitySource The security source. Not null.
   * @param conventionSource The convention source, not null
   * @param configSource The config source, not null
   * @deprecated use the no-arg constructor
   */
  @Deprecated
  public CurveNodeCurrencyVisitor(ConventionSource conventionSource, SecuritySource securitySource,
      ConfigSource configSource) {
  }

  @Override
  public Set<Currency> visitBillNode(BillNode node) {
    CurveNodeIdMapper idMapper =
        ConfigLink.resolvable(node.getCurveNodeIdMapperName(), CurveNodeIdMapper.class).resolve();
    ExternalId nodeId = idMapper.getBillNodeId(null, node.getMaturityTenor()); // curve date is not relevant for bills
    BillSecurity security = SecurityLink.resolvable(nodeId, BillSecurity.class).resolve();
    return ImmutableSet.of(security.getCurrency());
  }

  @Override
  public Set<Currency> visitBondNode(BondNode node) {
    CurveNodeIdMapper idMapper =
        ConfigLink.resolvable(node.getCurveNodeIdMapperName(), CurveNodeIdMapper.class).resolve();
    ExternalId nodeId = idMapper.getBondNodeId(null, node.getMaturityTenor()); // curve date is not relevant for bonds
    BondSecurity security = SecurityLink.resolvable(nodeId, BondSecurity.class).resolve();
    return ImmutableSet.of(security.getCurrency());
  }

  @Override
  public Set<Currency> visitCalendarSwapNode(CalendarSwapNode node) {
    FinancialConvention convention =
        ConventionLink.resolvable(node.getSwapConvention(), SwapConvention.class).resolve();
    return convention.accept(this);
  }

  @Override
  public Set<Currency> visitCashNode(CashNode node) {
    try {
      FinancialConvention convention =
          ConventionLink.resolvable(node.getConvention(), FinancialConvention.class).resolve();
      return convention.accept(this);
    } catch (DataNotFoundException e) {
      // If the convention is not found in the convention source
      // then try with the security source.
      com.opengamma.financial.security.index.IborIndex security =
          SecurityLink.resolvable(
              node.getConvention(),
              com.opengamma.financial.security.index.IborIndex.class)
              .resolve();
      IborIndexConvention indexConvention =
            ConventionLink.resolvable(security.getConventionId(), IborIndexConvention.class).resolve();
      return indexConvention.accept(this);
        /* } else if (security instanceof com.opengamma.financial.security.index.OvernightIndex) { // is this necessary/a good idea?
        final com.opengamma.financial.security.index.OvernightIndex indexSecurity = (com.opengamma.financial.security.index.OvernightIndex) security;
        final IborIndexConvention indexConvention = _conventionSource.getSingle(indexSecurity.getConventionId(), IborIndexConvention.class);
        return indexConvention.accept(this);*/
    }
  }

  @Override
  public Set<Currency> visitContinuouslyCompoundedRateNode(ContinuouslyCompoundedRateNode node) {
    return ImmutableSet.of();
  }

  @Override
  public Set<Currency> visitPeriodicallyCompoundedRateNode(PeriodicallyCompoundedRateNode node) {
    return ImmutableSet.of();
  }

  @Override
  public Set<Currency> visitCreditSpreadNode(CreditSpreadNode node) {
    return ImmutableSet.of();
  }

  @Override
  public Set<Currency> visitDeliverableSwapFutureNode(DeliverableSwapFutureNode node) {
    FinancialConvention convention =
        ConventionLink.resolvable(node.getFutureConvention(), FinancialConvention.class).resolve();
    return convention.accept(this);
  }

  @Override
  public Set<Currency> visitDiscountFactorNode(DiscountFactorNode node) {
    return ImmutableSet.of();
  }

  @Override
  public Set<Currency> visitFRANode(FRANode node) {
    com.opengamma.financial.security.index.IborIndex indexSecurity =
        SecurityLink.resolvable(node.getConvention(), com.opengamma.financial.security.index.IborIndex.class).resolve();
    IborIndexConvention indexConvention =
        ConventionLink.resolvable(indexSecurity.getConventionId(), IborIndexConvention.class).resolve();
    return indexConvention.accept(this);
  }

  @Override
  public Set<Currency> visitFXForwardNode(FXForwardNode node) {
    return ImmutableSet.of(node.getPayCurrency(), node.getReceiveCurrency());
  }

  @Override
  public Set<Currency> visitFXSwapNode(FXSwapNode node) {
    return ImmutableSet.of(node.getPayCurrency(), node.getReceiveCurrency());
  }

  @Override
  public Set<Currency> visitRollDateFRANode(RollDateFRANode node) {
    RollDateFRAConvention convention =
        ConventionLink.resolvable(node.getRollDateFRAConvention(), RollDateFRAConvention.class).resolve();
    return convention.accept(this);
  }

  @Override
  public Set<Currency> visitRollDateSwapNode(RollDateSwapNode node) {
    RollDateSwapConvention convention =
        ConventionLink.resolvable(node.getRollDateSwapConvention(), RollDateSwapConvention.class).resolve();
    return convention.accept(this);
  }

  @Override
  public Set<Currency> visitRateFutureNode(RateFutureNode node) {

    FinancialConvention convention =
        ConventionLink.resolvable(node.getFutureConvention(), FinancialConvention.class).resolve();

    if (convention instanceof InterestRateFutureConvention) {
      InterestRateFutureConvention conventionSTIRFut = (InterestRateFutureConvention) convention;
      com.opengamma.financial.security.index.IborIndex indexSecurity =
          SecurityLink.resolvable(
              conventionSTIRFut.getIndexConvention(),
              com.opengamma.financial.security.index.IborIndex.class).resolve();
      IborIndexConvention indexConvention =
          ConventionLink.resolvable(indexSecurity.getConventionId(), IborIndexConvention.class).resolve();
      return indexConvention.accept(this);
    }
    final FederalFundsFutureConvention conventionFedFut = (FederalFundsFutureConvention) convention;
    final OvernightIndex indexSecurity =
        SecurityLink.resolvable(conventionFedFut.getIndexConvention(), OvernightIndex.class).resolve();
    OvernightIndexConvention indexConvention =
        ConventionLink.resolvable(indexSecurity.getConventionId(), OvernightIndexConvention.class).resolve();
    return indexConvention.accept(this);
  }

  @Override
  public Set<Currency> visitSwapNode(SwapNode node) {
    FinancialConvention payConvention =
        ConventionLink.resolvable(node.getPayLegConvention(), FinancialConvention.class).resolve();
    FinancialConvention receiveConvention =
        ConventionLink.resolvable(node.getReceiveLegConvention(), FinancialConvention.class).resolve();
    return combineCurrencySets(payConvention.accept(this), receiveConvention.accept(this));
  }

  @Override
  public Set<Currency> visitThreeLegBasisSwapNode(ThreeLegBasisSwapNode node) {
    FinancialConvention payConvention =
        ConventionLink.resolvable(node.getPayLegConvention(), FinancialConvention.class).resolve();
    FinancialConvention receiveConvention =
        ConventionLink.resolvable(node.getReceiveLegConvention(), FinancialConvention.class).resolve();
    FinancialConvention spreadConvention =
        ConventionLink.resolvable(node.getSpreadLegConvention(), FinancialConvention.class).resolve();
    return combineCurrencySets(
        payConvention.accept(this), receiveConvention.accept(this), spreadConvention.accept(this));
  }

  private ImmutableSet<Currency> combineCurrencySets(Set<Currency>... currencies) {
    ImmutableSet.Builder<Currency> builder = ImmutableSet.builder();
    for (Set<Currency> currencySet : currencies) {
      builder.addAll(currencySet);
    }
    return builder.build();
  }

  @Override
  public Set<Currency> visitZeroCouponInflationNode(ZeroCouponInflationNode node) {
    final FinancialConvention convention =
        ConventionLink.resolvable(node.getInflationLegConvention(), InflationLegConvention.class).resolve();
    return convention.accept(this);
  }

  @Override
  public Set<Currency> visitBondConvention(BondConvention convention) {
    return ImmutableSet.of();
  }

  @Override
  public Set<Currency> visitCMSLegConvention(CMSLegConvention convention) {
    FinancialConvention underlyingConvention =
        ConventionLink.resolvable(convention.getSwapIndexConvention(), FinancialConvention.class).resolve();
    return underlyingConvention.accept(this);
  }

  @Override
  public Set<Currency> visitCompoundingIborLegConvention(CompoundingIborLegConvention convention) {
    com.opengamma.financial.security.index.IborIndex indexSecurity =
        SecurityLink.resolvable(
            convention.getIborIndexConvention(),
            com.opengamma.financial.security.index.IborIndex.class)
            .resolve();
    IborIndexConvention indexConvention =
        ConventionLink.resolvable(indexSecurity.getConventionId(), IborIndexConvention.class).resolve();
    return indexConvention.accept(this);
  }

  @Override
  public Set<Currency> visitDepositConvention(DepositConvention convention) {
    return Collections.singleton(convention.getCurrency());
  }

  @Override
  public Set<Currency> visitEquityConvention(EquityConvention convention) {
    return ImmutableSet.of();
  }

  @Override
  public Set<Currency> visitDeliverablePriceQuotedSwapFutureConvention(DeliverablePriceQuotedSwapFutureConvention convention) {
    FinancialConvention underlyingConvention =
        ConventionLink.resolvable(convention.getSwapConvention(), FinancialConvention.class).resolve();
    return underlyingConvention.accept(this);
  }

  @Override
  public Set<Currency> visitFederalFundsFutureConvention(FederalFundsFutureConvention convention) {
    OvernightIndex index =
        SecurityLink.resolvable(convention.getIndexConvention(), OvernightIndex.class).resolve();
    FinancialConvention underlyingConvention =
        ConventionLink.resolvable(index.getConventionId(), FinancialConvention.class).resolve();
    return underlyingConvention.accept(this);
  }

  @Override
  public Set<Currency> visitFixedInterestRateSwapLegConvention(FixedInterestRateSwapLegConvention convention) {
    return ImmutableSet.of();
  }

  @Override
  public Set<Currency> visitFloatingInterestRateSwapLegConvention(FloatingInterestRateSwapLegConvention convention) {
    return ImmutableSet.of();
  }

  @Override
  public Set<Currency> visitFXForwardAndSwapConvention(FXForwardAndSwapConvention convention) {
    return ImmutableSet.of();
  }

  @Override
  public Set<Currency> visitFXSpotConvention(FXSpotConvention convention) {
    return ImmutableSet.of();
  }

  @Override
  public Set<Currency> visitIborIndexConvention(IborIndexConvention convention) {
    return Collections.singleton(convention.getCurrency());
  }

  @Override
  public Set<Currency> visitIMMFRAConvention(RollDateFRAConvention convention) {
    com.opengamma.financial.security.index.IborIndex indexSecurity =
        SecurityLink.resolvable(
            convention.getIndexConvention(),
            com.opengamma.financial.security.index.IborIndex.class)
            .resolve();
    IborIndexConvention indexConvention =
        ConventionLink.resolvable(indexSecurity.getConventionId(), IborIndexConvention.class).resolve();
    return indexConvention.accept(this);
  }

  @Override
  public Set<Currency> visitIMMSwapConvention(RollDateSwapConvention convention) {
    FinancialConvention payConvention =
        ConventionLink.resolvable(convention.getPayLegConvention(), FinancialConvention.class).resolve();
    FinancialConvention receiveConvention =
        ConventionLink.resolvable(convention.getReceiveLegConvention(), FinancialConvention.class).resolve();
    return combineCurrencySets(payConvention.accept(this), receiveConvention.accept(this));
  }

  @Override
  public Set<Currency> visitInflationLegConvention(InflationLegConvention convention) {
    PriceIndex indexSecurity =
        SecurityLink.resolvable(convention.getPriceIndexConvention(), PriceIndex.class).resolve();
    PriceIndexConvention indexConvention =
        ConventionLink.resolvable(indexSecurity.getConventionId(), PriceIndexConvention.class).resolve();
    return indexConvention.accept(this);
  }

  @Override
  public Set<Currency> visitInterestRateFutureConvention(InterestRateFutureConvention convention) {
    final FinancialConvention underlyingConvention =
        ConventionLink.resolvable(convention.getIndexConvention(), FinancialConvention.class).resolve();
    return underlyingConvention.accept(this);
  }

  @Override
  public Set<Currency> visitOISLegConvention(final OISLegConvention convention) {
    FinancialConvention underlyingConvention =
        extractUnderlyingConvention(convention.getOvernightIndexConvention());
    return underlyingConvention.accept(this);
  }

  @Override
  public Set<Currency> visitONCompoundedLegRollDateConvention(ONCompoundedLegRollDateConvention convention) {
    FinancialConvention underlyingConvention =
        extractUnderlyingConvention(convention.getOvernightIndexConvention());
    return underlyingConvention.accept(this);
  }

  @Override
  public Set<Currency> visitONArithmeticAverageLegConvention(final ONArithmeticAverageLegConvention convention) {
    final FinancialConvention underlyingConvention =
        extractUnderlyingConvention(convention.getOvernightIndexConvention());
    return underlyingConvention.accept(this);
  }

  private FinancialConvention extractUnderlyingConvention(ExternalId overnightIndexConvention) {
    OvernightIndex index =
        SecurityLink.resolvable(overnightIndexConvention, OvernightIndex.class).resolve();
    return ConventionLink.resolvable(index.getConventionId(), FinancialConvention.class).resolve();
  }

  @Override
  public Set<Currency> visitOvernightIndexConvention(final OvernightIndexConvention convention) {
    return Collections.singleton(convention.getCurrency());
  }

  @Override
  public Set<Currency> visitPriceIndexConvention(final PriceIndexConvention convention) {
    return Collections.singleton(convention.getCurrency());
  }

  @Override
  public Set<Currency> visitSwapConvention(final SwapConvention convention) {
    FinancialConvention payConvention = ConventionLink.resolvable(convention.getPayLegConvention(),
                                                             FinancialConvention.class).resolve();
    FinancialConvention receiveConvention = ConventionLink.resolvable(convention.getReceiveLegConvention(),
                                                                 FinancialConvention.class).resolve();
    return combineCurrencySets(payConvention.accept(this), receiveConvention.accept(this));
  }

  @Override
  public Set<Currency> visitSwapFixedLegConvention(final SwapFixedLegConvention convention) {
    return Collections.singleton(convention.getCurrency());
  }

  @Override
  public Set<Currency> visitFixedLegRollDateConvention(final FixedLegRollDateConvention convention) {
    return Collections.singleton(convention.getCurrency());
  }

  @Override
  public Set<Currency> visitSwapIndexConvention(final SwapIndexConvention convention) {
    final FinancialConvention underlyingConvention = ConventionLink.resolvable(convention.getSwapConvention(),
                                                                    FinancialConvention.class).resolve();
    return underlyingConvention.accept(this);
  }

  @Override
  public Set<Currency> visitVanillaIborLegConvention(final VanillaIborLegConvention convention) {
    com.opengamma.financial.security.index.IborIndex indexSecurity =
        SecurityLink.resolvable(
            convention.getIborIndexConvention(),
            com.opengamma.financial.security.index.IborIndex.class)
            .resolve();
    IborIndexConvention indexConvention =
        ConventionLink.resolvable(indexSecurity.getConventionId(), IborIndexConvention.class).resolve();
    return indexConvention.accept(this);
  }

  @Override
  public Set<Currency> visitVanillaIborLegRollDateConvention(final VanillaIborLegRollDateConvention convention) {
    com.opengamma.financial.security.index.IborIndex indexSecurity =
        SecurityLink.resolvable(
            convention.getIborIndexConvention(),
            com.opengamma.financial.security.index.IborIndex.class)
            .resolve();
    IborIndexConvention indexConvention =
        ConventionLink.resolvable(indexSecurity.getConventionId(), IborIndexConvention.class).resolve();
    return indexConvention.accept(this);
  }

}
