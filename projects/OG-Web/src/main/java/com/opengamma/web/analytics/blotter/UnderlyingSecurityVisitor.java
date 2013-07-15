/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;

/**
 *
 */
public class UnderlyingSecurityVisitor extends FinancialSecurityVisitorSameValueAdapter<ManageableSecurity> {

  private final SecurityMaster _securityMaster;
  private final VersionCorrection _versionCorrection;

  public UnderlyingSecurityVisitor(VersionCorrection versionCorrection, SecurityMaster securityMaster) {
    super(null);
    _versionCorrection = versionCorrection;
    _securityMaster = securityMaster;
  }

  @Override
  public ManageableSecurity visitSwaptionSecurity(SwaptionSecurity security) {
    SecuritySearchResult result = _securityMaster.search(new SecuritySearchRequest(security.getUnderlyingId()));
    ManageableSecurity underlying = result.getSingleSecurity();
    return _securityMaster.get(underlying.getUniqueId().getObjectId(), _versionCorrection).getSecurity();
  }

  @Override
  public ManageableSecurity visitCreditDefaultSwapOptionSecurity(CreditDefaultSwapOptionSecurity security) {
    SecuritySearchResult result = _securityMaster.search(new SecuritySearchRequest(security.getUnderlyingId()));
    ManageableSecurity underlying = result.getSingleSecurity();
    return _securityMaster.get(underlying.getUniqueId().getObjectId(), _versionCorrection).getSecurity();
  }

}
