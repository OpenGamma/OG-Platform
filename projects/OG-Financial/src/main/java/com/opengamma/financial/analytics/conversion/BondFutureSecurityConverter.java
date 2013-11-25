/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Converts bond future securities into the form that is used by the analytics library
 */
public class BondFutureSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** The security source */
  private final SecuritySource _securitySource;
  /** Converter for the bonds in the deliverable basket */
  private final BondSecurityConverter _bondConverter;

  private static final Logger s_logger = LoggerFactory.getLogger(BondFutureSecurityConverter.class);
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

  @Override
  public BondFutureDefinition visitBondFutureSecurity(final BondFutureSecurity bondFuture) {
    ArgumentChecker.notNull(bondFuture, "security");
    final ZonedDateTime tradingLastDate = bondFuture.getExpiry().getExpiry();
    final ZonedDateTime noticeFirstDate = bondFuture.getFirstDeliveryDate();
    final ZonedDateTime noticeLastDate = bondFuture.getLastDeliveryDate();
    final double notional = bondFuture.getUnitAmount();
    final List<BondFutureDeliverable> basket = bondFuture.getBasket();
    final int n = basket.size();
    final ArrayList<BondFixedSecurityDefinition> deliverablesList = new ArrayList<>();
    final ArrayList<Double> conversionFactorList = new ArrayList<>();
    

    for (int i = 0; i < n; i++) {
      final BondFutureDeliverable deliverable = basket.get(i);
      final BondSecurity bondSecurity = (BondSecurity) _securitySource.getSingle(deliverable.getIdentifiers());
      if (bondSecurity == null) {
        s_logger.debug("Unable to create underlying bond with identifiers " + deliverable.getIdentifiers() + " in bond future deliverable basket");
      } else {
        deliverablesList.add((BondFixedSecurityDefinition) bondSecurity.accept(_bondConverter));
        conversionFactorList.add(deliverable.getConversionFactor());
      }
    }
    final int m = deliverablesList.size();
    if (m == 0) {
      throw new OpenGammaRuntimeException("Unable to satisfy a single identifier in the bond future deliverable basket for " + bondFuture.toString());
    }
    final BondFixedSecurityDefinition[] deliverables = new BondFixedSecurityDefinition[m];
    final double[] conversionFactor = new double[m];
    for (int j = 0; j < m; j++) {
      deliverables[j] = deliverablesList.get(j);
      conversionFactor[j] = conversionFactorList.get(j);
    }
    
    return new BondFutureDefinition(tradingLastDate, noticeFirstDate, noticeLastDate, notional, deliverables, conversionFactor);
  }
}
