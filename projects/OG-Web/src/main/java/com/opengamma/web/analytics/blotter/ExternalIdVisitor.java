/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.cds.AbstractCreditDefaultSwapSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.LegacyFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.LegacyRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.StandardRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Returns an {@link ExternalId} with the scheme {@link UniqueId#EXTERNAL_SCHEME} for on OTC security which can be
 * used as the underlying ID in another security. Currently only supports {@link SwapSecurity}, returns null for all
 * other security types. If the security doesn't have an existing ID then one is created from its unique ID. So
 * this can only be used on a security which have been saved and has an ID.
 * TODO this isn't nice as the security has to be saved twice, once to get the unique ID and once to add the external ID
 * it would be better if SwaptionSecurity referred to its underlying with a SecurityLink. but it doesn't (yet?)
 * TODO update this once Swaption has been updated to use a link to refer to its underlying
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
    UniqueId uniqueId = security.getUniqueId();
    if (uniqueId == null) {
      throw new IllegalStateException("Security must have a unique ID");
    }
    ObjectId objectId = uniqueId.getObjectId();
    ExternalId externalId = ExternalId.of(ObjectId.EXTERNAL_SCHEME, objectId.toString());
    security.addExternalId(externalId);
    _securityMaster.update(new SecurityDocument(security));
    return externalId;
  }

  @Override
  public ExternalId visitCreditDefaultSwapIndexSecurity(CreditDefaultSwapIndexSecurity security) {
    return commonCDSSecurityExternalId(security);
  }

  @Override
  public ExternalId visitLegacyFixedRecoveryCDSSecurity(LegacyFixedRecoveryCDSSecurity security) {
    return commonCDSSecurityExternalId(security);
  }

  @Override
  public ExternalId visitLegacyRecoveryLockCDSSecurity(LegacyRecoveryLockCDSSecurity security) {
    return commonCDSSecurityExternalId(security);
  }

  @Override
  public ExternalId visitStandardFixedRecoveryCDSSecurity(StandardFixedRecoveryCDSSecurity security) {
    return commonCDSSecurityExternalId(security);
  }

  @Override
  public ExternalId visitStandardRecoveryLockCDSSecurity(StandardRecoveryLockCDSSecurity security) {
    return commonCDSSecurityExternalId(security);
  }

  @Override
  public ExternalId visitStandardVanillaCDSSecurity(StandardVanillaCDSSecurity security) {
    return commonCDSSecurityExternalId(security);
  }

  @Override
  public ExternalId visitLegacyVanillaCDSSecurity(LegacyVanillaCDSSecurity security) {
    return commonCDSSecurityExternalId(security);
  }

  private ExternalId commonCDSSecurityExternalId(AbstractCreditDefaultSwapSecurity security) {
    if (!security.getExternalIdBundle().isEmpty()) {
      return security.getExternalIdBundle().getExternalIds().iterator().next();
    }
    UniqueId uniqueId = security.getUniqueId();
    if (uniqueId == null) {
      throw new IllegalStateException("Security must have a unique ID");
    }
    ObjectId objectId = uniqueId.getObjectId();
    ExternalId externalId = ExternalId.of(ObjectId.EXTERNAL_SCHEME, objectId.toString());
    security.addExternalId(externalId);
    _securityMaster.update(new SecurityDocument(security));
    return externalId;
  }
}
