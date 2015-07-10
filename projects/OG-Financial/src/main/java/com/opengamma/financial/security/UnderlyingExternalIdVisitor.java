/**
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.FloatingRateNoteSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.InflationBondSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.credit.IndexCDSSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Vistor to find the underlying {@code ExternalId}.
 */
public final class UnderlyingExternalIdVisitor extends FinancialSecurityVisitorAdapter<Void> {

  /**
   * The collected set of underlying identifiers.
   */
  private final Set<ExternalIdBundle> _underlyings = Sets.newHashSet();

  /**
   * Creates an instance
   */
  public UnderlyingExternalIdVisitor() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the set of underlying identifiers that were found.
   *
   * @return the underlying identifiers, not null
   */
  public Set<ExternalIdBundle> getUnderlyings() {
    return _underlyings;
  }

  //-------------------------------------------------------------------------
  @Override
  public Void visitEquitySecurity(final EquitySecurity security) {
    return null;
  }

  @Override
  public Void visitBillSecurity(final BillSecurity security) {
    return null;
  }

  @Override
  public Void visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
    return null;
  }

  @Override
  public Void visitCorporateBondSecurity(final CorporateBondSecurity security) {
    return null;
  }

  @Override
  public Void visitInflationBondSecurity(final InflationBondSecurity security) {
    return null;
  }
  
  @Override
  public Void visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
    return null;
  }

  @Override
  public Void visitBondFutureSecurity(final BondFutureSecurity security) {
    final List<BondFutureDeliverable> basketList = security.getBasket();
    for (final BondFutureDeliverable deliverable : basketList) {
      final ExternalIdBundle identifiers = deliverable.getIdentifiers();
      if (identifiers != null) {
        _underlyings.add(identifiers);
      }
    }
    return null;
  }

  @Override
  public Void visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
    final ExternalId identifier = security.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitFXFutureSecurity(final FXFutureSecurity security) {
    return null;
  }

  @Override
  public Void visitIndexFutureSecurity(final IndexFutureSecurity security) {
    final ExternalId identifier = security.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    final ExternalId identifier = security.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitMetalFutureSecurity(final MetalFutureSecurity security) {
    final ExternalId identifier = security.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitStockFutureSecurity(final StockFutureSecurity security) {
    final ExternalId identifier = security.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitEquityFutureSecurity(final EquityFutureSecurity security) {
    final ExternalId identifier = security.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
    final ExternalId identifier = security.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitEquityOptionSecurity(final EquityOptionSecurity equityOptionSecurity) {
    final ExternalId identifier = equityOptionSecurity.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
    final ExternalId identifier = security.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    final ExternalId underlyingIdentifier = security.getUnderlyingId();
    if (underlyingIdentifier != null) {
      _underlyings.add(ExternalIdBundle.of(underlyingIdentifier));
    }
    return null;
  }

  @Override
  public Void visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
    final ExternalId underlyingIdentifier = security.getUnderlyingId();
    if (underlyingIdentifier != null) {
      _underlyings.add(ExternalIdBundle.of(underlyingIdentifier));
    }
    return null;
  }

  @Override
  public Void visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
    final ExternalId underlyingIdentifier = security.getUnderlyingId();
    if (underlyingIdentifier != null) {
      _underlyings.add(ExternalIdBundle.of(underlyingIdentifier));
    }
    return null;
  }

  @Override
  public Void visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
    final ExternalId underlyingIdentifier = security.getUnderlyingId();
    if (underlyingIdentifier != null) {
      _underlyings.add(ExternalIdBundle.of(underlyingIdentifier));
    }
    return null;
  }

  @Override
  public Void visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
    final ExternalId underlyingIdentifier = security.getUnderlyingId();
    if (underlyingIdentifier != null) {
      _underlyings.add(ExternalIdBundle.of(underlyingIdentifier));
    }
    return null;
  }

  @Override
  public Void visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
    final ExternalId underlyingIdentifier = security.getUnderlyingId();
    if (underlyingIdentifier != null) {
      _underlyings.add(ExternalIdBundle.of(underlyingIdentifier));
    }
    return null;
  }

  @Override
  public Void visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
    final ExternalId identifier = security.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
    final ExternalId identifier = security.getReferenceEntity();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
    final ExternalId identifier = security.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitFloatingRateNoteSecurity(final FloatingRateNoteSecurity security) {
    return null; //TODO the index?
  }

  @Override
  public Void visitIndexCDSSecurity(final IndexCDSSecurity security) {
    final ExternalIdBundle identifier = security.getUnderlyingIndex().resolve().getExternalIdBundle();
    if (identifier != null) {
      _underlyings.add(identifier);
    }
    return null;
  }
}
