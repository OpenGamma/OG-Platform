/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import static com.opengamma.engine.security.server.SecurityMasterServiceNames.PATH_IDENTIFIER;
import static com.opengamma.engine.security.server.SecurityMasterServiceNames.PATH_VALUE;
import static com.opengamma.engine.security.server.SecurityMasterServiceNames.SECURITYMASTER_ALLSECURITYTYPES;
import static com.opengamma.engine.security.server.SecurityMasterServiceNames.SECURITYMASTER_SECURITIES;
import static com.opengamma.engine.security.server.SecurityMasterServiceNames.SECURITYMASTER_SECURITY;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.fudgemsg.FudgeContext;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;

/**
 * A remote implementation of security master.
 */
public class RemoteSecurityMaster implements SecurityMaster {

  private final RestClient _restClient;
  private final RestTarget _targetSecurities;
  private final RestTarget _targetSecurity;
  private final RestTarget _targetAllSecurityTypes;

  public RemoteSecurityMaster(final FudgeContext fudgeContext, final RestTarget baseTarget) {
    _restClient = RestClient.getInstance(fudgeContext, null);
    _targetSecurities = baseTarget.resolveBase(SECURITYMASTER_SECURITIES);
    _targetSecurity = baseTarget.resolveBase(SECURITYMASTER_SECURITY);
    _targetAllSecurityTypes = baseTarget.resolve(SECURITYMASTER_ALLSECURITYTYPES);
  }

  protected RestClient getRestClient () {
    return _restClient;
  }

  protected RestTarget resolveIdentifierBase(final RestTarget base, final UniqueIdentifier identifier) {
    return base.resolveBase(PATH_IDENTIFIER).resolveBase(identifier.getScheme()).resolveBase(identifier.getValue());
  }

  protected RestTarget resolveIdentifierBundleBase(final RestTarget base, final IdentifierBundle bundle) {
    RestTarget target = base;
    for (Identifier identifier : bundle.getIdentifiers()) {
      target = target.resolveBase(PATH_IDENTIFIER).resolveBase(identifier.getScheme().getName()).resolveBase(identifier.getValue());
    }
    return target;
  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(UniqueIdentifier uid) {
    return getRestClient().getSingleValueNotNull(Security.class,
        resolveIdentifierBase(_targetSecurity, uid).resolve(PATH_VALUE), SECURITYMASTER_SECURITY);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Security> getSecurities(IdentifierBundle secKey) {
    return getRestClient().getSingleValueNotNull(List.class,
        resolveIdentifierBundleBase(_targetSecurities, secKey).resolve(PATH_VALUE), SECURITYMASTER_SECURITIES);
  }

  @Override
  public Security getSecurity(IdentifierBundle secKey) {
    return getRestClient().getSingleValueNotNull(Security.class,
        resolveIdentifierBundleBase(_targetSecurity, secKey).resolve(PATH_VALUE), SECURITYMASTER_SECURITY);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<String> getAllSecurityTypes() {
    return getRestClient().getSingleValueNotNull(Set.class, _targetAllSecurityTypes, SECURITYMASTER_ALLSECURITYTYPES);
  }

}
