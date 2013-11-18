/**
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
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
  public Void visitEquitySecurity(EquitySecurity security) {
    return null;
  }
  
  @Override
  public Void visitGovernmentBondSecurity(GovernmentBondSecurity security) {
    return null;
  }

  @Override
  public Void visitCorporateBondSecurity(CorporateBondSecurity security) {
    return null;
  }

  @Override
  public Void visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
    return null;
  }

  @Override
  public Void visitBondFutureSecurity(BondFutureSecurity security) {
    List<BondFutureDeliverable> basketList = security.getBasket();
    for (BondFutureDeliverable deliverable : basketList) {
      ExternalIdBundle identifiers = deliverable.getIdentifiers();
      if (identifiers != null) {
        _underlyings.add(identifiers);
      }
    }
    return null;
  }

  @Override
  public Void visitEnergyFutureSecurity(EnergyFutureSecurity security) {
    ExternalId identifier = security.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitFXFutureSecurity(FXFutureSecurity security) {
    return null;
  }

  @Override
  public Void visitIndexFutureSecurity(IndexFutureSecurity security) {
    ExternalId identifier = security.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
    ExternalId identifier = security.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitMetalFutureSecurity(MetalFutureSecurity security) {
    ExternalId identifier = security.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitStockFutureSecurity(StockFutureSecurity security) {
    ExternalId identifier = security.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitEquityFutureSecurity(EquityFutureSecurity security) {
    ExternalId identifier = security.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitEquityIndexDividendFutureSecurity(EquityIndexDividendFutureSecurity security) {
    ExternalId identifier = security.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitEquityOptionSecurity(EquityOptionSecurity equityOptionSecurity) {
    ExternalId identifier = equityOptionSecurity.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
    ExternalId identifier = security.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitIRFutureOptionSecurity(IRFutureOptionSecurity security) {
    ExternalId underlyingIdentifier = security.getUnderlyingId();
    if (underlyingIdentifier != null) {
      _underlyings.add(ExternalIdBundle.of(underlyingIdentifier));
    }
    return null;
  }

  @Override
  public Void visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
    ExternalId underlyingIdentifier = security.getUnderlyingId();
    if (underlyingIdentifier != null) {
      _underlyings.add(ExternalIdBundle.of(underlyingIdentifier));
    }
    return null;
  }

  @Override
  public Void visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
    ExternalId underlyingIdentifier = security.getUnderlyingId();
    if (underlyingIdentifier != null) {
      _underlyings.add(ExternalIdBundle.of(underlyingIdentifier));
    }
    return null;
  }

  @Override
  public Void visitCommodityFutureOptionSecurity(CommodityFutureOptionSecurity security) {
    ExternalId underlyingIdentifier = security.getUnderlyingId();
    if (underlyingIdentifier != null) {
      _underlyings.add(ExternalIdBundle.of(underlyingIdentifier));
    }
    return null;
  }

  @Override
  public Void visitFxFutureOptionSecurity(FxFutureOptionSecurity security) {
    ExternalId underlyingIdentifier = security.getUnderlyingId();
    if (underlyingIdentifier != null) {
      _underlyings.add(ExternalIdBundle.of(underlyingIdentifier));
    }
    return null;
  }

  @Override
  public Void visitBondFutureOptionSecurity(BondFutureOptionSecurity security) {
    ExternalId underlyingIdentifier = security.getUnderlyingId();
    if (underlyingIdentifier != null) {
      _underlyings.add(ExternalIdBundle.of(underlyingIdentifier));
    }
    return null;
  }

  @Override
  public Void visitFederalFundsFutureSecurity(FederalFundsFutureSecurity security) {
    ExternalId identifier = security.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitCreditDefaultSwapIndexSecurity(CreditDefaultSwapIndexSecurity security) {
    ExternalId identifier = security.getReferenceEntity();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }

  @Override
  public Void visitCreditDefaultSwapOptionSecurity(CreditDefaultSwapOptionSecurity security) {
    ExternalId identifier = security.getUnderlyingId();
    if (identifier != null) {
      _underlyings.add(ExternalIdBundle.of(identifier));
    }
    return null;
  }
}
