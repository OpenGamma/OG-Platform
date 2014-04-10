/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.security;

import com.opengamma.core.security.Security;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.credit.IndexCDSSecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.forward.AgricultureForwardSecurity;
import com.opengamma.financial.security.forward.EnergyForwardSecurity;
import com.opengamma.financial.security.forward.MetalForwardSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.id.ExternalId;

/**
 * Visitor to obtain the id of an underlying, null if not applicable
 */
public class UnderlyingVisitor extends FinancialSecurityVisitorSameValueAdapter<ExternalId> {

  public UnderlyingVisitor() {
    super(null);
  }

  private static final UnderlyingVisitor INSTANCE = new UnderlyingVisitor();

  public static UnderlyingVisitor getInstance() {
    return INSTANCE;
  }

  /**
   * Returns the underlying id of a security (e.g. the id of the equity underlying an equity future).
   *
   * @param security The security, not null
   * @return The id of the underlying of a security, where it is possible to identify this, or null
   */
  public static ExternalId getUnderlyingId(final Security security) {
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;
      return finSec.accept(getInstance());
    }
    return null;
  }


  @Override
  public ExternalId visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
    return security.getUnderlyingId();
  }

  @Override
  public ExternalId visitEnergyForwardSecurity(final EnergyForwardSecurity security) {
    return security.getUnderlyingId();
  }

  @Override
  public ExternalId visitAgricultureForwardSecurity(final AgricultureForwardSecurity security) {
    return security.getUnderlyingId();
  }

  @Override
  public ExternalId visitMetalForwardSecurity(final MetalForwardSecurity security) {
    return security.getUnderlyingId();
  }

  @Override
  public ExternalId visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
    return security.getUnderlyingId();
  }

  @Override
  public ExternalId visitStockFutureSecurity(final StockFutureSecurity security) {
    return security.getUnderlyingId();
  }

  @Override
  public ExternalId visitEquityFutureSecurity(final EquityFutureSecurity security) {
    return security.getUnderlyingId();
  }

  @Override
  public ExternalId visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
    return security.getUnderlyingId();
  }

  @Override
  public ExternalId visitIndexFutureSecurity(final IndexFutureSecurity security) {
    return security.getUnderlyingId();
  }

  @Override
  public ExternalId visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    return security.getUnderlyingId();
  }

  @Override
  public ExternalId visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
    return security.getUnderlyingId();
  }

  @Override
  public ExternalId visitMetalFutureSecurity(final MetalFutureSecurity security) {
    return security.getUnderlyingId();
  }

  @Override
  public ExternalId visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
    return security.getUnderlyingId();
  }

  @Override
  public ExternalId visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
    return security.getUnderlyingId();
  }

  @Override
  public ExternalId visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
    return security.getUnderlyingId();
  }

  @Override
  public ExternalId visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
    return security.getUnderlyingId();
  }

  @Override
  public ExternalId visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
    return security.getUnderlyingId();
  }

  @Override
  public ExternalId visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
    return security.getUnderlyingId();
  }

  @Override
  public ExternalId visitEquityOptionSecurity(final EquityOptionSecurity security) {
    return security.getUnderlyingId();
  }

  @Override
  public ExternalId visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
    return security.getSpotUnderlyingId();
  }

  @Override
  public ExternalId visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    return security.getUnderlyingId();
  }

  @Override
  public ExternalId visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
    return security.getReferenceEntity();
  }

  @Override
  public ExternalId visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
    return security.getUnderlyingId();
  }

  @Override
  public ExternalId visitIndexCDSSecurity(IndexCDSSecurity security) {
    return security.getUnderlyingIndex();
  }
}