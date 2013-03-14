/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.LegacyFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.LegacyRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.credit.CreditCurveIdentifier;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public final class CreditSecurityToIdentifierVisitor extends FinancialSecurityVisitorAdapter<CreditCurveIdentifier> {
  private static final CreditSecurityToIdentifierVisitor INSTANCE = new CreditSecurityToIdentifierVisitor();

  public static CreditSecurityToIdentifierVisitor getInstance() {
    return INSTANCE;
  }

  private CreditSecurityToIdentifierVisitor() {
  }

  @Override
  public CreditCurveIdentifier visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
    final ExternalId redCode = security.getReferenceEntity();
    final Currency currency = security.getNotional().getCurrency();
    final String seniority = security.getDebtSeniority().name();
    final String restructuringClause = security.getRestructuringClause().name();
    return CreditCurveIdentifier.of(redCode, currency, seniority, restructuringClause);
  }

  @Override
  public CreditCurveIdentifier visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
    final ExternalId redCode = security.getReferenceEntity();
    final Currency currency = security.getNotional().getCurrency();
    final String seniority = security.getDebtSeniority().name();
    final String restructuringClause = security.getRestructuringClause().name();
    return CreditCurveIdentifier.of(redCode, currency, seniority, restructuringClause);
  }

}
