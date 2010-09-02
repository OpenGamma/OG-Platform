/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A security master implementation that delegates to an underlying implementation.
 */
public class SecurityMasterAdapter implements SecurityMaster {

  /**
   * The underlying security master.
   */
  private final SecurityMaster _securityMaster;

  /**
   * Creates an instance with an underlying security master.
   * @param securityMaster  the security master, not null
   */
  public SecurityMasterAdapter(final SecurityMaster securityMaster) {
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    _securityMaster = securityMaster;
  }

  protected SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  @Override
  public SecurityDocument add(SecurityDocument document) {
    return getSecurityMaster().add(document);
  }

  @Override
  public SecurityDocument correct(SecurityDocument document) {
    return getSecurityMaster().add(document);
  }

  @Override
  public SecurityDocument get(UniqueIdentifier uid) {
    return getSecurityMaster().get(uid);
  }

  @Override
  public void remove(UniqueIdentifier uid) {
    getSecurityMaster().remove(uid);
  }

  @Override
  public SecuritySearchResult search(SecuritySearchRequest request) {
    return getSecurityMaster().search(request);
  }

  @Override
  public SecuritySearchHistoricResult searchHistoric(SecuritySearchHistoricRequest request) {
    return getSecurityMaster().searchHistoric(request);
  }

  @Override
  public SecurityDocument update(SecurityDocument document) {
    return getSecurityMaster().update(document);
  }

}
