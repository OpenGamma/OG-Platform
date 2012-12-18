/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * TODO this isn't nice as it has to save the security twice, once to get the unique ID and once to add the external ID
 * it would be better if SwaptionSecurity referred to its underlying with a SecurityLink. but it doesn't (yet?)
 */
/* package */ class ExternalIdVisitor extends FinancialSecurityVisitorSameValueAdapter<ExternalId> {

  private final SecurityMaster _securityMaster;

  /* package */  ExternalIdVisitor(SecurityMaster securityMaster) {
    super(null);
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    _securityMaster = securityMaster;
  }

  @Override
  public ExternalId visitSwapSecurity(SwapSecurity security) {
    ExternalId id = security.getExternalIdBundle().getExternalId(UniqueId.EXTERNAL_SCHEME);
    if (id != null) {
      return id;
    }
    ObjectId objectId = security.getUniqueId().getObjectId();
    if (objectId == null) {
      throw new IllegalStateException("Security must have a unique ID");
    }
    ExternalId externalId = ExternalId.of(ObjectId.EXTERNAL_SCHEME, objectId.toString());
    security.addExternalId(externalId);
    _securityMaster.update(new SecurityDocument(security));
    return externalId;
  }
}
