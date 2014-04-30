/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.security;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.id.ExternalId;

/**
 * Get the exchange for a security, null if not applicable
 */
public class ExchangeVisitor extends FinancialSecurityVisitorSameValueAdapter<ExternalId> {

  public ExchangeVisitor() {
    super(null);
  }

  @Override
  public ExternalId visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
    return ExternalId.of(ExternalSchemes.ISO_MIC, security.getExchange());
  }

  @Override
  public ExternalId visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
    return ExternalId.of(ExternalSchemes.ISO_MIC, security.getExchange());
  }

  @Override
  public ExternalId visitEquityOptionSecurity(final EquityOptionSecurity security) {
    return ExternalId.of(ExternalSchemes.ISO_MIC, security.getExchange());
  }

  @Override
  public ExternalId visitEquitySecurity(final EquitySecurity security) {
    return ExternalId.of(ExternalSchemes.ISO_MIC, security.getExchangeCode());
  }

  @Override
  public ExternalId visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
    return ExternalId.of(ExternalSchemes.ISO_MIC, security.getTradingExchange());
  }

  @Override
  public ExternalId visitBondFutureSecurity(final BondFutureSecurity security) {
    return ExternalId.of(ExternalSchemes.ISO_MIC, security.getTradingExchange());
  }

  @Override
  public ExternalId visitEquityFutureSecurity(final EquityFutureSecurity security) {
    return ExternalId.of(ExternalSchemes.ISO_MIC, security.getTradingExchange());
  }

  @Override
  public ExternalId visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
    return ExternalId.of(ExternalSchemes.ISO_MIC, security.getTradingExchange());
  }

  @Override
  public ExternalId visitFXFutureSecurity(final FXFutureSecurity security) {
    return ExternalId.of(ExternalSchemes.ISO_MIC, security.getTradingExchange());
  }

  @Override
  public ExternalId visitIndexFutureSecurity(final IndexFutureSecurity security) {
    return ExternalId.of(ExternalSchemes.ISO_MIC, security.getTradingExchange());
  }

  @Override
  public ExternalId visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    return ExternalId.of(ExternalSchemes.ISO_MIC, security.getTradingExchange());
  }

  @Override
  public ExternalId visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
    return ExternalId.of(ExternalSchemes.ISO_MIC, security.getTradingExchange());
  }

  @Override
  public ExternalId visitMetalFutureSecurity(final MetalFutureSecurity security) {
    return ExternalId.of(ExternalSchemes.ISO_MIC, security.getTradingExchange());
  }

  @Override
  public ExternalId visitStockFutureSecurity(final StockFutureSecurity security) {
    return ExternalId.of(ExternalSchemes.ISO_MIC, security.getTradingExchange());
  }

  @Override
  public ExternalId visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
    return ExternalId.of(ExternalSchemes.ISO_MIC, security.getExchange());
  }

}