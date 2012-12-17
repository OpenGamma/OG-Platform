/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class ExternalIdVisitor extends FinancialSecurityVisitorSameValueAdapter<ExternalId> {

  private final SecurityMaster _securityMaster;

  /* package */  ExternalIdVisitor(SecurityMaster securityMaster) {
    super(null);
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    _securityMaster = securityMaster;
  }

  @Override
  public ExternalId visitSwapSecurity(SwapSecurity underlying) {
    ExternalId id = underlying.getExternalIdBundle().getExternalId(UniqueId.EXTERNAL_SCHEME);
    if (id != null) {
      return id;
    }
    UniqueId uniqueId = underlying.getUniqueId();
    if (uniqueId == null) {
      throw new IllegalStateException("Security must have a unique ID");
    }
    ExternalId externalId = uniqueId.toExternalId();
    underlying.addExternalId(externalId);
    _securityMaster.update(new SecurityDocument(underlying));
    return externalId;
  }
}
