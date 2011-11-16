/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.rest;

import static com.opengamma.financial.security.rest.SecuritySourceServiceNames.SECURITYSOURCE_SECURITY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.FinancialSecuritySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.ObjectsPairFudgeBuilder;

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
  public Security getSecurity(ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final RestTarget target = _targetBase.resolveBase("security")
        .resolveBase(objectId.toString())
        .resolveBase(versionCorrection.getVersionAsOfString())
        .resolve(versionCorrection.getCorrectedToString());
    return getRestClient().getSingleValue(Security.class, target, SECURITYSOURCE_SECURITY);
  }

  @Override
  public Collection<Security> getSecurities(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    final RestTarget target = _targetBase.resolveBase("securities").resolveQuery("id", bundle.toStringList());
    final FudgeMsg message = getRestClient().getMsg(target);
    final FudgeDeserializer deserializer = getRestClient().getFudgeDeserializer();
    final Collection<Security> securities = new ArrayList<Security>(message.getNumFields());
    for (FudgeField security : message) {
      if (SECURITYSOURCE_SECURITY.equals(security.getName())) {
        securities.add(deserializer.fieldValueToObject(Security.class, security));
      }
    }
    return securities;
  }
  
  @Override
  public Collection<Security> getSecurities(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final RestTarget target = _targetBase.resolveBase("securities")
        .resolveQuery("id", bundle.toStringList())
        .resolveQuery("versionAsOf", Collections.singletonList(versionCorrection.getVersionAsOfString()))
        .resolveQuery("correctedTo", Collections.singletonList(versionCorrection.getCorrectedToString()));
    final FudgeMsg message = getRestClient().getMsg(target);
    final FudgeDeserializer deserializer = getRestClient().getFudgeDeserializer();
    final Collection<Security> securities = new ArrayList<Security>(message.getNumFields());
    for (FudgeField security : message) {
      if (SECURITYSOURCE_SECURITY.equals(security.getName())) {
        securities.add(deserializer.fieldValueToObject(Security.class, security));
      }
    }
    return securities;
  }

  @Override
  public Security getSecurity(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    final RestTarget target = _targetBase.resolveBase("securities").resolve("security").resolveQuery("id", bundle.toStringList());
    return getRestClient().getSingleValue(Security.class, target, SECURITYSOURCE_SECURITY);
  }
  
  @Override
  public Security getSecurity(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final RestTarget target = _targetBase.resolveBase("securities").resolve("security")
        .resolveQuery("id", bundle.toStringList())
        .resolveQuery("versionAsOf", Collections.singletonList(versionCorrection.getVersionAsOfString()))
        .resolveQuery("correctedTo", Collections.singletonList(versionCorrection.getCorrectedToString()));
    return getRestClient().getSingleValue(Security.class, target, SECURITYSOURCE_SECURITY);
  }

  @Override
  public Collection<Security> getBondsWithIssuerName(String issuerName) {
    ArgumentChecker.notNull(issuerName, "issuerName");
    final RestTarget target = _targetBase.resolve("bonds").resolveQuery("issuerName", Collections.singletonList(issuerName));
    final FudgeMsg message = getRestClient().getMsg(target);
    final FudgeDeserializer deserializer = getRestClient().getFudgeDeserializer();
    final Collection<Security> securities = new ArrayList<Security>(message.getNumFields());
    for (FudgeField security : message) {
      if (SECURITYSOURCE_SECURITY.equals(security.getName())) {
        securities.add(deserializer.fieldValueToObject(Security.class, security));
      }
    }
    return securities;
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  @Override
  public Map<UniqueId, Security> getSecurity(Collection<UniqueId> uniqueIds) {
    ArgumentChecker.notNull(uniqueIds, "uniqueIds");
    List<String> uniqueIdStr = Lists.newArrayList();
    
    final RestTarget target = _targetBase.resolveBase("securities").resolve("uid")
        .resolveQuery("uids", uniqueIdStr);
    final FudgeMsg message = getRestClient().getMsg(target);
    final FudgeDeserializer deserializer = getRestClient().getFudgeDeserializer();
    
    Map<UniqueId, Security> result = Maps.newHashMap();
    for (FudgeField fudgeField : message) {
      if (SECURITYSOURCE_SECURITY.equals(fudgeField.getName())) {
        ObjectsPair<UniqueId, Security> objectsPair = ObjectsPairFudgeBuilder.buildObject(deserializer, (FudgeMsg) fudgeField.getValue(), UniqueId.class, Security.class);
        if (objectsPair != null && objectsPair.getKey() != null) {
          result.put(objectsPair.getKey(), objectsPair.getValue());
        }
      }
    }
    return result;
  }

}
