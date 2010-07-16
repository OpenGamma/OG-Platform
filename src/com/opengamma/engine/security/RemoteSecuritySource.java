/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import static com.opengamma.engine.security.server.SecurityMasterServiceNames.SECURITYMASTER_ALLSECURITYTYPES;
import static com.opengamma.engine.security.server.SecurityMasterServiceNames.SECURITYMASTER_SECURITIES;
import static com.opengamma.engine.security.server.SecurityMasterServiceNames.SECURITYMASTER_SECURITY;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.fudgemsg.FudgeContext;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;

/**
 * A remote implementation of security master.
 */
public class RemoteSecuritySource implements SecuritySource {

  private final RestClient _restClient;
  private final RestTarget _targetBase;

  public RemoteSecuritySource(final FudgeContext fudgeContext, final RestTarget baseTarget) {
    _restClient = RestClient.getInstance(fudgeContext, null);
    _targetBase = baseTarget;
  }

  protected RestClient getRestClient() {
    return _restClient;
  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(UniqueIdentifier uid) {
    final RestTarget target = _targetBase.resolveBase("securities").resolveBase("security").resolve(uid.toString());
    return getRestClient().getSingleValueNotNull(Security.class, target, SECURITYMASTER_SECURITY);
  }

  @Override
  public Security getSecurity(IdentifierBundle secKey) {
    final RestTarget target = _targetBase.resolveBase("securities").resolve("security").resolveQuery("id", secKey.toStringList());
    return getRestClient().getSingleValueNotNull(Security.class, target, SECURITYMASTER_SECURITY);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Security> getSecurities(IdentifierBundle secKey) {
    final RestTarget target = _targetBase.resolveBase("securities").resolveQuery("id", secKey.toStringList());
    return getRestClient().getSingleValueNotNull(List.class, target, SECURITYMASTER_SECURITIES);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<String> getAllSecurityTypes() {
    final RestTarget target = _targetBase.resolveBase("securities").resolve("types");
    return getRestClient().getSingleValueNotNull(Set.class, target, SECURITYMASTER_ALLSECURITYTYPES);
  }

}
