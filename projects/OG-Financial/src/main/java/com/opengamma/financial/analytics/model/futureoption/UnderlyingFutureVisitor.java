/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public final class UnderlyingFutureVisitor extends FinancialSecurityVisitorAdapter<ExternalId> {
  private static final FinancialSecurityVisitorAdapter<ExternalId> INSTANCE = new UnderlyingFutureVisitor();

  public static FinancialSecurityVisitorAdapter<ExternalId> getInstance() {
    return INSTANCE;
  }

  private UnderlyingFutureVisitor() {
  }

  @Override
  public ExternalId visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity commodityFutureOption) {
    ArgumentChecker.notNull(commodityFutureOption, "commodity future option security");
    return commodityFutureOption.getUnderlyingId();
  }

  @Override
  public ExternalId visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity equityIndexDividendOption) {
    ArgumentChecker.notNull(equityIndexDividendOption, "equity index dividend future option security");
    return equityIndexDividendOption.getUnderlyingId();
  }
}
