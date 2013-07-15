/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.credit.CdsRecoveryRateIdentifier;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public final class CreditSecurityToRecoveryRateVisitor extends FinancialSecurityVisitorAdapter<CdsRecoveryRateIdentifier> {
  private final SecuritySource _securitySource;

  public CreditSecurityToRecoveryRateVisitor(final SecuritySource securitySource) {
    ArgumentChecker.notNull(securitySource, "security source");
    _securitySource = securitySource;
  }

  @Override
  public CdsRecoveryRateIdentifier visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
    final ExternalId redCode = security.getReferenceEntity();
    final Currency currency = security.getNotional().getCurrency();
    final String seniority = security.getDebtSeniority().name();
    final String restructuringClause = security.getRestructuringClause().name();
    return CdsRecoveryRateIdentifier.forSamedayCds(redCode.getValue(), currency, seniority, restructuringClause);
  }

  @Override
  public CdsRecoveryRateIdentifier visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
    final ExternalId redCode = security.getReferenceEntity();
    final Currency currency = security.getNotional().getCurrency();
    final String seniority = security.getDebtSeniority().name();
    final String restructuringClause = security.getRestructuringClause().name();
    return CdsRecoveryRateIdentifier.forSamedayCds(redCode.getValue(), currency, seniority, restructuringClause);
  }

  @Override
  public CdsRecoveryRateIdentifier visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
    final CreditDefaultSwapSecurity underlyingSwap = (CreditDefaultSwapSecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId())); //TODO version correction?
    return underlyingSwap.accept(this);
  }

}
