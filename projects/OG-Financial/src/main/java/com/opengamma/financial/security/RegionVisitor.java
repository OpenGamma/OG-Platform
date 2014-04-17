/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.security;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.id.ExternalId;

/**
 * Get the region for a security, null if not applicable
 */
public class RegionVisitor extends FinancialSecurityVisitorSameValueAdapter<ExternalId> {

  public RegionVisitor() {
    super(null);
  }

  @Override
  public ExternalId visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
    return ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2, security.getIssuerDomicile());
  }

  @Override
  public ExternalId visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
    return ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2, security.getIssuerDomicile());
  }

  @Override
  public ExternalId visitCorporateBondSecurity(final CorporateBondSecurity security) {
    return ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2, security.getIssuerDomicile());
  }

  @Override
  public ExternalId visitCashSecurity(final CashSecurity security) {
    return security.getRegionId();
  }

  @Override
  public ExternalId visitFRASecurity(final FRASecurity security) {
    return security.getRegionId();
  }

  @Override
  public ExternalId visitFXForwardSecurity(final FXForwardSecurity security) {
    return security.getRegionId();
  }

  @Override
  public ExternalId visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
    return security.getRegionId();
  }

  @Override
  public ExternalId visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
    return security.getRegionId();
  }

  @Override
  public ExternalId visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
    return security.getRegion();
  }

  @Override
  public ExternalId visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
    return security.getRegion();
  }

  @Override
  public ExternalId visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
    return security.getRegion();
  }

  @Override
  public ExternalId visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
    return security.getRegionId();
  }

  @Override
  public ExternalId visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
    return security.getRegionId();
  }

}
