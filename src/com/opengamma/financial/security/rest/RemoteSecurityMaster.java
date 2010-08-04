/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.rest;

import static com.opengamma.financial.security.rest.SecurityMasterServiceNames.SECURITYMASTER_HISTORIC;
import static com.opengamma.financial.security.rest.SecurityMasterServiceNames.SECURITYMASTER_SEARCH;
import static com.opengamma.financial.security.rest.SecurityMasterServiceNames.SECURITYMASTER_SECURITY;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.financial.security.SecurityDocument;
import com.opengamma.financial.security.SecurityMaster;
import com.opengamma.financial.security.SecuritySearchHistoricRequest;
import com.opengamma.financial.security.SecuritySearchHistoricResult;
import com.opengamma.financial.security.SecuritySearchRequest;
import com.opengamma.financial.security.SecuritySearchResult;
import com.opengamma.id.UniqueIdentifiables;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;

/**
 * Provides access to a remote {@link SecurityMaster}.
 */
public class RemoteSecurityMaster implements SecurityMaster {

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteSecurityMaster.class);

  private final FudgeContext _fudgeContext;
  private final RestClient _restClient;
  private final RestTarget _targetSecurity;
  private final RestTarget _targetSearch;
  private final RestTarget _targetHistoric;

  public RemoteSecurityMaster(FudgeContext fudgeContext, RestTarget baseTarget) {
    _fudgeContext = fudgeContext;
    _targetSecurity = baseTarget.resolveBase(SECURITYMASTER_SECURITY);
    _targetSearch = baseTarget.resolve(SECURITYMASTER_SEARCH);
    _targetHistoric = baseTarget.resolve(SECURITYMASTER_HISTORIC);
    _restClient = RestClient.getInstance(fudgeContext, null);
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected FudgeSerializationContext getFudgeSerializationContext() {
    return new FudgeSerializationContext(getFudgeContext());
  }

  protected FudgeDeserializationContext getFudgeDeserializationContext() {
    return new FudgeDeserializationContext(getFudgeContext());
  }

  protected RestClient getRestClient() {
    return _restClient;
  }

  @Override
  public SecurityDocument add(final SecurityDocument document) {
    final FudgeFieldContainer payload = getFudgeSerializationContext().objectToFudgeMsg(document);
    final FudgeMsgEnvelope env = getRestClient().post(_targetSecurity, payload);
    if (env == null) {
      return null;
    }
    final UniqueIdentifier uid = getFudgeDeserializationContext().fudgeMsgToObject(UniqueIdentifier.class, env.getMessage());
    if (uid == null) {
      return null;
    }
    document.setUniqueIdentifier(uid);
    UniqueIdentifiables.setInto(document.getSecurity(), uid);
    return document;
  }

  @Override
  public SecurityDocument correct(SecurityDocument document) {
    final FudgeFieldContainer payload = getFudgeSerializationContext().objectToFudgeMsg(document);
    final FudgeMsgEnvelope env = getRestClient().put(_targetSecurity.resolve(document.getUniqueIdentifier().toString()), payload);
    if (env == null) {
      return null;
    }
    return document;
  }

  @Override
  public SecurityDocument get(UniqueIdentifier uid) {
    final FudgeFieldContainer message = getRestClient().getMsg(_targetSecurity.resolve(uid.toString()));
    s_logger.debug("Received {}", message);
    if (message == null) {
      throw new DataNotFoundException("Security with identifier " + uid);
    }
    return getFudgeDeserializationContext().fudgeMsgToObject(SecurityDocument.class, message);
  }

  @Override
  public void remove(UniqueIdentifier uid) {
    getRestClient().delete(_targetSecurity.resolve(uid.toString()));
  }

  @Override
  public SecuritySearchResult search(SecuritySearchRequest request) {
    // POST is wrong; but is easy to write if we have a "request" document
    final FudgeFieldContainer payload = getFudgeSerializationContext().objectToFudgeMsg(request);
    final FudgeMsgEnvelope env = getRestClient().post(_targetSearch, payload);
    if (env == null) {
      return null;
    }
    return getFudgeDeserializationContext().fudgeMsgToObject(SecuritySearchResult.class, env.getMessage());
  }

  @Override
  public SecuritySearchHistoricResult searchHistoric(SecuritySearchHistoricRequest request) {
    // POST is wrong; but is easy to write if we have a "request" document
    final FudgeFieldContainer payload = getFudgeSerializationContext().objectToFudgeMsg(request);
    final FudgeMsgEnvelope env = getRestClient().post(_targetHistoric, payload);
    if (env == null) {
      return null;
    }
    return getFudgeDeserializationContext().fudgeMsgToObject(SecuritySearchHistoricResult.class, env.getMessage());
  }

  @Override
  public SecurityDocument update(SecurityDocument document) {
    final FudgeFieldContainer payload = getFudgeSerializationContext().objectToFudgeMsg(document);
    final FudgeMsgEnvelope env = getRestClient().post(_targetSecurity.resolve(document.getUniqueIdentifier().toString()), payload);
    if (env == null) {
      return null;
    }
    s_logger.debug("Received {}", env.getMessage());
    document.setUniqueIdentifier(getFudgeDeserializationContext().fudgeMsgToObject(UniqueIdentifier.class, env.getMessage()));
    return document;
  }

}
