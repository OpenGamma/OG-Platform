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
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.DeliverableSwapFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.financial.convention.CMSLegConvention;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.DeliverablePriceQuotedSwapFutureConvention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.FederalFundsFutureConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
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

  /**
   * Gets the convention source.
   * @return The convention source
   */
  protected ConventionSource getConventionSource() {
    return _conventionSource;
  }

  @Override
  public Set<Currency> visitCashNode(final CashNode node) {
    final Convention convention = _conventionSource.getConvention(node.getConvention());
    if (convention == null) {
      throw new OpenGammaRuntimeException("Could not get convention with id " + node.getConvention());
    }
    return getCurrencies(convention);
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
  public Set<Currency> visitDeliverableSwapFutureNode(final DeliverableSwapFutureNode node) {
    final Convention convention = _conventionSource.getConvention(node.getFutureConvention());
    if (convention == null) {
      throw new OpenGammaRuntimeException("Could not get convention with id " + node.getFutureConvention());
    }
    return getCurrencies(convention);
  }

  @Override
  public Set<Currency> visitDiscountFactorNode(final DiscountFactorNode node) {
    return null;
  }

  @Override
  public Set<Currency> visitFRANode(final FRANode node) {
    final Convention convention = _conventionSource.getConvention(node.getConvention());
    if (convention == null) {
      throw new OpenGammaRuntimeException("Could not get convention with id " + node.getConvention());
    }
    return getCurrencies(convention);
  }

  @Override
  public Set<Currency> visitFXForwardNode(final FXForwardNode node) {
    return Sets.newHashSet(node.getPayCurrency(), node.getReceiveCurrency());
  }

  @Override
  public Set<Currency> visitRateFutureNode(final RateFutureNode node) {
    final Convention futureConvention = _conventionSource.getConvention(node.getFutureConvention());
    if (futureConvention == null) {
      throw new OpenGammaRuntimeException("Could not get future convention with id " + node.getFutureConvention());
    }
    final Convention underlyingConvention = _conventionSource.getConvention(node.getUnderlyingConvention());
    if (underlyingConvention == null) {
      throw new OpenGammaRuntimeException("Could not get convention with id " + node.getUnderlyingConvention());
    }
    final Set<Currency> currencies = new HashSet<>(getCurrencies(futureConvention));
    currencies.addAll(getCurrencies(underlyingConvention));
    return currencies;
  }

  @Override
  public Set<Currency> visitSwapNode(final SwapNode node) {
    final Convention payConvention = _conventionSource.getConvention(node.getPayLegConvention());
    if (payConvention == null) {
      throw new OpenGammaRuntimeException("Could not get pay convention with id " + node.getPayLegConvention());
    }
    final Convention receiveConvention = _conventionSource.getConvention(node.getReceiveLegConvention());
    if (receiveConvention == null) {
      throw new OpenGammaRuntimeException("Could not get receive convention with id " + node.getReceiveLegConvention());
    }
    final Set<Currency> currencies = new HashSet<>(getCurrencies(payConvention));
    currencies.addAll(getCurrencies(receiveConvention));
    return currencies;
  }

  @Override
  public Set<Currency> visitZeroCouponInflationNode(final ZeroCouponInflationNode node) {
    final Convention inflationLegConvention = _conventionSource.getConvention(node.getInflationLegConvention());
    if (inflationLegConvention == null) {
      throw new OpenGammaRuntimeException("Could not get inflation leg convention with id " + node.getInflationLegConvention());
    }
    if (!(inflationLegConvention instanceof InflationLegConvention)) {
      throw new OpenGammaRuntimeException("Type of convention " + inflationLegConvention + " was not InflationLegConvention");
    }
    final Convention priceIndexConvention = _conventionSource.getConvention(((InflationLegConvention) inflationLegConvention).getPriceIndexConvention());
    if (priceIndexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get price index convention with id " + ((InflationLegConvention) inflationLegConvention).getPriceIndexConvention());
    }
    return getCurrencies(priceIndexConvention);
  }

  protected Set<Currency> getCurrencies(final Convention convention) {
    ArgumentChecker.notNull(convention, "convention");
    if (convention instanceof CMSLegConvention) {
      final Convention underlyingConvention = _conventionSource.getConvention(((CMSLegConvention) convention).getSwapIndexConvention());
      if (underlyingConvention == null) {
        throw new OpenGammaRuntimeException("Could not get convention with id " + ((CMSLegConvention) convention).getSwapIndexConvention());
      }
      return getCurrencies(underlyingConvention);
    }
    if (convention instanceof CompoundingIborLegConvention) {
      final Convention underlyingConvention = _conventionSource.getConvention(((CompoundingIborLegConvention) convention).getIborIndexConvention());
      if (underlyingConvention == null) {
        throw new OpenGammaRuntimeException("Could not get convention with id " + ((CompoundingIborLegConvention) convention).getIborIndexConvention());
      }
      return getCurrencies(underlyingConvention);
    }
    if (convention instanceof DepositConvention) {
      return Collections.singleton(((DepositConvention) convention).getCurrency());
    }
    if (convention instanceof IborIndexConvention) {
      return Collections.singleton(((IborIndexConvention) convention).getCurrency());
    }
    if (convention instanceof InterestRateFutureConvention) {
      final Convention underlyingConvention = _conventionSource.getConvention(((InterestRateFutureConvention) convention).getIndexConvention());
      return getCurrencies(underlyingConvention);
    }
    if (convention instanceof DeliverablePriceQuotedSwapFutureConvention) {
      final Convention underlyingConvention = _conventionSource.getConvention(((DeliverablePriceQuotedSwapFutureConvention) convention).getSwapConvention());
      return getCurrencies(underlyingConvention);
    }
    if (convention instanceof FederalFundsFutureConvention) {
      final Convention underlyingConvention = _conventionSource.getConvention(((FederalFundsFutureConvention) convention).getIndexConvention());
      return getCurrencies(underlyingConvention);
    }
    if (convention instanceof InterestRateFutureConvention) {
      final Convention underlyingConvention = _conventionSource.getConvention(((InterestRateFutureConvention) convention).getIndexConvention());
      return getCurrencies(underlyingConvention);
    }
    if (convention instanceof OISLegConvention) {
      final Convention underlyingConvention = _conventionSource.getConvention(((OISLegConvention) convention).getOvernightIndexConvention());
      if (underlyingConvention == null) {
        throw new OpenGammaRuntimeException("Could not get convention with id " + ((OISLegConvention) convention).getOvernightIndexConvention());
      }
      return getCurrencies(underlyingConvention);
    }
    if (convention instanceof OvernightIndexConvention) {
      return Collections.singleton(((OvernightIndexConvention) convention).getCurrency());
    }
    if (convention instanceof PriceIndexConvention) {
      return Collections.singleton(((PriceIndexConvention) convention).getCurrency());
    }
    if (convention instanceof SwapFixedLegConvention) {
      return Collections.singleton(((SwapFixedLegConvention) convention).getCurrency());
    }
    if (convention instanceof SwapIndexConvention) {
      final Convention underlyingConvention = _conventionSource.getConvention(((SwapIndexConvention) convention).getSwapConvention());
      if (underlyingConvention == null) {
        throw new OpenGammaRuntimeException("Could not get convention with id " + ((SwapIndexConvention) convention).getSwapConvention());
      }
      return getCurrencies(underlyingConvention);
    }
    if (convention instanceof VanillaIborLegConvention) {
      final Convention underlyingConvention = _conventionSource.getConvention(((VanillaIborLegConvention) convention).getIborIndexConvention());
      if (underlyingConvention == null) {
        throw new OpenGammaRuntimeException("Could not get convention with id " + ((VanillaIborLegConvention) convention).getIborIndexConvention());
      }
      return getCurrencies(underlyingConvention);
    }
    throw new IllegalArgumentException("Cannot handle conventions of type " + convention.getClass());
  }
}
