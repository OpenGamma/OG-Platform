/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.net.URI;
import java.util.Collection;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.RemoteSecuritySource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeListWrapper;

/**
 * Provides remote access to a {@link FinancialSecuritySource}.
 */
public class RemoteFinancialSecuritySource extends RemoteSecuritySource implements FinancialSecuritySource {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteFinancialSecuritySource(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteFinancialSecuritySource(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public Collection<Security> getBondsWithIssuerName(String issuerName) {
    ArgumentChecker.notNull(issuerName, "issuerName");
    
    URI uri = DataFinancialSecuritySourceResource.uriSearchBonds(getBaseUri(), issuerName);
    return accessRemote(uri).get(FudgeListWrapper.class).getList();
  }

}
