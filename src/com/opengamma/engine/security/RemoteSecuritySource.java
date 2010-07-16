/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import static com.opengamma.engine.security.server.SecurityMasterServiceNames.SECURITYMASTER_SECURITIES;
import static com.opengamma.engine.security.server.SecurityMasterServiceNames.SECURITYMASTER_SECURITY;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.fudgemsg.FudgeContext;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

/**
 * An implementation of {@code SecuritySource} that calls a remote data source.
 * <p>
 * The implementation calls the remote data source in a RESTful manner using binary Fudge messages.
 */
public class RemoteSecuritySource implements SecuritySource {

  /**
   * The RESTful client instance.
   */
  private final RestClient _restClient;
  /**
   * The base URI of the RESTful server.
   */
  private final RestTarget _targetBase;

  /**
   * Creates an instance.
   * @param fudgeContext  the Fudge context, not null
   * @param baseTarget  the base target URI to call, not null
   */
  public RemoteSecuritySource(final FudgeContext fudgeContext, final RestTarget baseTarget) {
    Validate.notNull(fudgeContext, "FudgeContext must not be null");
    Validate.notNull(baseTarget, "RestTarget must not be null");
    _restClient = RestClient.getInstance(fudgeContext, null);
    _targetBase = baseTarget;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the RESTful client.
   * @return the client, not null
   */
  protected RestClient getRestClient() {
    return _restClient;
  }

  /**
   * Gets the base target URI.
   * @return the base target URI, not null
   */
  protected RestTarget getTargetBase() {
    return _targetBase;
  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    final RestTarget target = _targetBase.resolveBase("securities").resolveBase("security").resolve(uid.toString());
    return getRestClient().getSingleValueNotNull(Security.class, target, SECURITYMASTER_SECURITY);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Security> getSecurities(IdentifierBundle securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    final RestTarget target = _targetBase.resolveBase("securities").resolveQuery("id", securityKey.toStringList());
    return getRestClient().getSingleValueNotNull(List.class, target, SECURITYMASTER_SECURITIES);
  }

  @Override
  public Security getSecurity(IdentifierBundle securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    final RestTarget target = _targetBase.resolveBase("securities").resolve("security").resolveQuery("id", securityKey.toStringList());
    return getRestClient().getSingleValueNotNull(Security.class, target, SECURITYMASTER_SECURITY);
  }

}
