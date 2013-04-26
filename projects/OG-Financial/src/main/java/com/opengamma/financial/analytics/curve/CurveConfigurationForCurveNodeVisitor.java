/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CurveConfigurationForCurveNodeVisitor implements CurveNodeVisitor<List<ExternalId>> {
  private final ConventionSource _conventionSource;

  public CurveConfigurationForCurveNodeVisitor(final ConventionSource conventions) {
    _conventionSource = conventions;
  }

  @Override
  public List<ExternalId> visitCashNode(final CashNode node) {
    final List<ExternalId> result = new ArrayList<>();
    result.add(ExternalId.of(CurveConfigurationForSecurityVisitor.SECURITY_IDENTIFIER, CashSecurity.SECURITY_TYPE));
    final ExternalId conventionName = node.getConvention();
    final Convention convention = _conventionSource.getConvention(conventionName);
    if (!(convention instanceof DepositConvention) || (convention instanceof IborIndexConvention)) {
      throw new OpenGammaRuntimeException("Could not get convention called " + conventionName + " for " + node);
    }
    if (convention instanceof DepositConvention) {
      final DepositConvention depositConvention = (DepositConvention) convention;
      result.add(depositConvention.getRegionCalendar());
      result.add(ExternalId.of(Currency.OBJECT_SCHEME, depositConvention.getCurrency().getCode()));
    } else {
      final IborIndexConvention iborConvention = (IborIndexConvention) convention;
      result.add(iborConvention.getRegionCalendar());
      result.add(ExternalId.of(Currency.OBJECT_SCHEME, iborConvention.getCurrency().getCode()));
    }
    return result;
  }

  @Override
  public List<ExternalId> visitContinuouslyCompoundedRateNode(final ContinuouslyCompoundedRateNode node) {
    return null;
  }

  @Override
  public List<ExternalId> visitCreditSpreadNode(final CreditSpreadNode node) {
    return null;
  }

  @Override
  public List<ExternalId> visitDiscountFactorNode(final DiscountFactorNode node) {
    return null;
  }

  @Override
  public List<ExternalId> visitFRANode(final FRANode node) {
    return null;
  }

  @Override
  public List<ExternalId> visitRateFutureNode(final RateFutureNode node) {
    return null;
  }

  @Override
  public List<ExternalId> visitSwapNode(final SwapNode node) {
    return null;
  }


}
