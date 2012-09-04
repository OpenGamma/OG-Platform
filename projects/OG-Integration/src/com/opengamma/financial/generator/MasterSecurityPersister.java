/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link SecurityPersister} that writes the securities to a master.
 */
public class MasterSecurityPersister extends SecurityPersister {

  private final SecurityMaster _securityMaster;

  public MasterSecurityPersister(final SecurityMaster securityMaster) {
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    _securityMaster = securityMaster;
  }

  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  @Override
  protected void storeSecurityImpl(final ManageableSecurity security) {
    final SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(security);
    getSecurityMaster().add(doc);
  }

}
