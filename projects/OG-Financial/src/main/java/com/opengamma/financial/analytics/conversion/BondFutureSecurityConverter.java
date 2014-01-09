/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import java.util.List;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts bond future securities into the form that is used by the analytics library.
 * @deprecated This converter creates a deprecated analytics object and does not add
 * issuer information. Use {@link BondAndBondFutureTradeWithEntityConverter}
 */
@Deprecated
public class BondFutureSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** The security source */
  private final SecuritySource _securitySource;
  /** Converter for the bonds in the deliverable basket */
  private final BondSecurityConverter _bondConverter;

  /**
   * @param securitySource The security source, not null
   * @param bondConverter The bond converter, not null
   */
  public BondFutureSecurityConverter(final SecuritySource securitySource, final BondSecurityConverter bondConverter) {
    ArgumentChecker.notNull(securitySource, "security source");
    ArgumentChecker.notNull(bondConverter, "bond converter");
    _securitySource = securitySource;
    _bondConverter = bondConverter;
  }

  //FIXME CASE - BondFutureDefinition needs a reference price. Without a trade, where will it come from?
  @Override
  public BondFutureDefinition visitBondFutureSecurity(final BondFutureSecurity bondFuture) {
    ArgumentChecker.notNull(bondFuture, "security");
    final ZonedDateTime tradingLastDate = bondFuture.getExpiry().getExpiry();
    final ZonedDateTime noticeFirstDate = bondFuture.getFirstDeliveryDate();
    final ZonedDateTime noticeLastDate = bondFuture.getLastDeliveryDate();
    final double notional = bondFuture.getUnitAmount();
    final List<BondFutureDeliverable> basket = bondFuture.getBasket();
    final int n = basket.size();
    final BondFixedSecurityDefinition[] deliverables = new BondFixedSecurityDefinition[n];
    final double[] conversionFactor = new double[n];
    for (int i = 0; i < n; i++) {
      final BondFutureDeliverable deliverable = basket.get(i);
      final BondSecurity bondSecurity = (BondSecurity) _securitySource.getSingle(deliverable.getIdentifiers());
      if (bondSecurity == null) {
        throw new OpenGammaRuntimeException("No security found with identifiers " + deliverable.getIdentifiers() + " in bond future deliverable basket");
      }
      deliverables[i] = (BondFixedSecurityDefinition) bondSecurity.accept(_bondConverter);
      conversionFactor[i] = deliverable.getConversionFactor();
    }
    return new BondFutureDefinition(tradingLastDate, noticeFirstDate, noticeLastDate, notional, deliverables, conversionFactor);
  }
}
