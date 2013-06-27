/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.convention.CMSLegConvention;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Returns all of the currencies relevant for a {@link CurveNode}. This information is pulled from
 * the convention(s) associated with the node.
 */
public class CurveNodeCurrencyVisitor implements CurveNodeVisitor<Set<Currency>> {
  /** The convention source */
  private final ConventionSource _conventionSource;

  /**
   * @param conventionSource The convention source, not null
   */
  public CurveNodeCurrencyVisitor(final ConventionSource conventionSource) {
    ArgumentChecker.notNull(conventionSource, "convention source");
    _conventionSource = conventionSource;
  }

  @Override
  public Set<Currency> visitCashNode(final CashNode node) {
    return getCurrencies(_conventionSource.getConvention(node.getConvention()));
  }

  @Override
  public Set<Currency> visitContinuouslyCompoundedRateNode(final ContinuouslyCompoundedRateNode node) {
    return null;
  }

  @Override
  public Set<Currency> visitCreditSpreadNode(final CreditSpreadNode node) {
    return null;
  }

  @Override
  public Set<Currency> visitDiscountFactorNode(final DiscountFactorNode node) {
    return null;
  }

  @Override
  public Set<Currency> visitFRANode(final FRANode node) {
    return getCurrencies(_conventionSource.getConvention(node.getConvention()));
  }

  @Override
  public Set<Currency> visitFXForwardNode(final FXForwardNode node) {
    return Sets.newHashSet(node.getPayCurrency(), node.getReceiveCurrency());
  }

  @Override
  public Set<Currency> visitRateFutureNode(final RateFutureNode node) {
    final Set<Currency> currencies = new HashSet<>(getCurrencies(_conventionSource.getConvention(node.getFutureConvention())));
    currencies.addAll(getCurrencies(_conventionSource.getConvention(node.getUnderlyingConvention())));
    return currencies;
  }

  @Override
  public Set<Currency> visitSwapNode(final SwapNode node) {
    final Set<Currency> currencies = new HashSet<>(getCurrencies(_conventionSource.getConvention(node.getPayLegConvention())));
    currencies.addAll(getCurrencies(_conventionSource.getConvention(node.getReceiveLegConvention())));
    return currencies;
  }

  private Set<Currency> getCurrencies(final Convention convention) {
    ArgumentChecker.notNull(convention, "convention");
    if (convention instanceof CMSLegConvention) {
      return getCurrencies(_conventionSource.getConvention(((CMSLegConvention) convention).getSwapIndexConvention()));
    }
    if (convention instanceof CompoundingIborLegConvention) {
      return getCurrencies(_conventionSource.getConvention(((CompoundingIborLegConvention) convention).getIborIndexConvention()));
    }
    if (convention instanceof DepositConvention) {
      return Collections.singleton(((DepositConvention) convention).getCurrency());
    }
    if (convention instanceof IborIndexConvention) {
      return Collections.singleton(((IborIndexConvention) convention).getCurrency());
    }
    if (convention instanceof InterestRateFutureConvention) {
      return getCurrencies(_conventionSource.getConvention(((InterestRateFutureConvention) convention).getIndexConvention()));
    }
    if (convention instanceof OISLegConvention) {
      return getCurrencies(_conventionSource.getConvention(((OISLegConvention) convention).getOvernightIndexConvention()));
    }
    if (convention instanceof OvernightIndexConvention) {
      return Collections.singleton(((OvernightIndexConvention) convention).getCurrency());
    }
    if (convention instanceof SwapConvention) {
      final SwapConvention swap = (SwapConvention) convention;
      final Set<Currency> currencies = new HashSet<>();
      currencies.addAll(getCurrencies(_conventionSource.getConvention(swap.getPayLegConvention())));
      currencies.addAll(getCurrencies(_conventionSource.getConvention(swap.getReceiveLegConvention())));
      return currencies;
    }
    if (convention instanceof SwapFixedLegConvention) {
      return Collections.singleton(((SwapFixedLegConvention) convention).getCurrency());
    }
    if (convention instanceof SwapIndexConvention) {
      return getCurrencies(_conventionSource.getConvention(((SwapIndexConvention) convention).getSwapConvention()));
    }
    if (convention instanceof VanillaIborLegConvention) {
      return getCurrencies(_conventionSource.getConvention(((VanillaIborLegConvention) convention).getIborIndexConvention()));
    }
    throw new IllegalArgumentException("Cannot handle conventions of type " + convention.getClass());
  }
}
