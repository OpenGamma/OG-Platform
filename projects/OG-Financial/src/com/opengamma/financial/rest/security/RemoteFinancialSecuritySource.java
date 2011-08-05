/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.rest.security;

import static com.opengamma.financial.rest.security.SecuritySourceServiceNames.SECURITYSOURCE_SECURITY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializationContext;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.FinancialSecuritySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

/**
 * An implementation of {@code FinancialSecuritySource} client that calls a remote data source.
 * <p>
 * The implementation calls the remote data source in a RESTful manner using binary Fudge messages.
 */
public class RemoteFinancialSecuritySource implements FinancialSecuritySource {

  /**
   * The RESTful client instance.
   */
  private final RestClient _restClient;
  /**
   * The base URI of the RESTful server.
   */
  private final RestTarget _targetBase;
  /**
   * The change manager
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @param baseTarget  the base target URI to call, not null
   */
  public RemoteFinancialSecuritySource(final FudgeContext fudgeContext, final RestTarget baseTarget) {
    this(fudgeContext, baseTarget, new BasicChangeManager());
  }
  
  /**
   * Creates an instance.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @param baseTarget  the base target URI to call, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteFinancialSecuritySource(final FudgeContext fudgeContext, final RestTarget baseTarget, ChangeManager changeManager) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(baseTarget, "baseTarget");
    ArgumentChecker.notNull(changeManager, "changeManager");
    _restClient = RestClient.getInstance(fudgeContext, null);
    _targetBase = baseTarget;
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the RESTful client.
   * 
   * @return the client, not null
   */
  protected RestClient getRestClient() {
    return _restClient;
  }

  /**
   * Gets the base target URI.
   * 
   * @return the base target URI, not null
   */
  protected RestTarget getTargetBase() {
    return _targetBase;
  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(UniqueId uid) {
    ArgumentChecker.notNull(uid, "uid");
    final RestTarget target = _targetBase.resolveBase("security").resolve(uid.toString());
    return getRestClient().getSingleValue(Security.class, target, SECURITYSOURCE_SECURITY);
  }

  @Override
  public Collection<Security> getSecurities(ExternalIdBundle securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    final RestTarget target = _targetBase.resolveBase("securities").resolveQuery("id", securityKey.toStringList());
    final FudgeMsg message = getRestClient().getMsg(target);
    final FudgeDeserializationContext context = getRestClient().getFudgeDeserializationContext();
    final Collection<Security> securities = new ArrayList<Security>(message.getNumFields());
    for (FudgeField security : message) {
      if (SECURITYSOURCE_SECURITY.equals(security.getName())) {
        securities.add(context.fieldValueToObject(Security.class, security));
      }
    }
    return securities;
  }

  @Override
  public Security getSecurity(ExternalIdBundle securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    final RestTarget target = _targetBase.resolveBase("securities").resolve("security").resolveQuery("id", securityKey.toStringList());
    return getRestClient().getSingleValue(Security.class, target, SECURITYSOURCE_SECURITY);
  }

  @Override
  public Collection<Security> getBondsWithIssuerName(String issuerName) {
    ArgumentChecker.notNull(issuerName, "issuerName");
    final RestTarget target = _targetBase.resolve("bonds").resolveQuery("issuerName", Collections.singletonList(issuerName));
    final FudgeMsg message = getRestClient().getMsg(target);
    final FudgeDeserializationContext context = getRestClient().getFudgeDeserializationContext();
    final Collection<Security> securities = new ArrayList<Security>(message.getNumFields());
    for (FudgeField security : message) {
      if (SECURITYSOURCE_SECURITY.equals(security.getName())) {
        securities.add(context.fieldValueToObject(Security.class, security));
      }
    }
    return securities;
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
