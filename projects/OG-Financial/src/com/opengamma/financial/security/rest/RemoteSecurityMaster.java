/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.rest;

import static com.opengamma.financial.security.rest.SecurityMasterServiceNames.SECURITYMASTER_HISTORIC;
import static com.opengamma.financial.security.rest.SecurityMasterServiceNames.SECURITYMASTER_SEARCH;
import static com.opengamma.financial.security.rest.SecurityMasterServiceNames.SECURITYMASTER_SECURITY;

import java.util.Collections;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueIdentifiables;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.listener.BasicMasterChangeManager;
import com.opengamma.master.listener.MasterChangeManager;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

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
  private final MasterChangeManager _changeManager;

  /**
   * Creates an instance.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @param baseTarget  the RESTful target, not null
   */
  public RemoteSecurityMaster(FudgeContext fudgeContext, RestTarget baseTarget) {
    this(fudgeContext, baseTarget, new BasicMasterChangeManager());
  }

  /**
   * Creates an instance.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @param baseTarget  the RESTful target, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteSecurityMaster(FudgeContext fudgeContext, RestTarget baseTarget, MasterChangeManager changeManager) {
    ArgumentChecker.notNull(changeManager, "changeManager");
    _fudgeContext = fudgeContext;
    _targetSecurity = baseTarget.resolveBase(SECURITYMASTER_SECURITY);
    _targetSearch = baseTarget.resolve(SECURITYMASTER_SEARCH);
    _targetHistoric = baseTarget.resolve(SECURITYMASTER_HISTORIC);
    _restClient = RestClient.getInstance(fudgeContext, null);
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
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
    s_logger.debug("add-post {} to {}", payload, _targetSecurity);
    final FudgeMsgEnvelope env = getRestClient().post(_targetSecurity, payload);
    if (env == null) {
      s_logger.debug("add-recv NULL");
      return null;
    }
    s_logger.debug("add-recv {}", env.getMessage());
    final UniqueIdentifier uid = getFudgeDeserializationContext().fudgeMsgToObject(UniqueIdentifier.class, env.getMessage());
    if (uid == null) {
      return null;
    }
    document.setUniqueId(uid);
    UniqueIdentifiables.setInto(document.getSecurity(), uid);
    return document;
  }

  @Override
  public SecurityDocument correct(SecurityDocument document) {
    final FudgeFieldContainer payload = getFudgeSerializationContext().objectToFudgeMsg(document);
    final RestTarget target = _targetSecurity.resolve(document.getUniqueId().toString());
    s_logger.debug("correct-put {} to {}", payload, target);
    final FudgeMsgEnvelope env = getRestClient().put(target, payload);
    if (env == null) {
      s_logger.debug("correct-recv NULL");
      return null;
    }
    s_logger.debug("correct-recv {}", env.getMessage());
    return document;
  }

  @Override
  public SecurityDocument get(UniqueIdentifier uid) {
    final RestTarget target = _targetSecurity.resolve(uid.toString());
    s_logger.debug("get-get to {}", target);
    final FudgeFieldContainer message = getRestClient().getMsg(target);
    if (message == null) {
      s_logger.debug("get-recv NULL");
      throw new DataNotFoundException("Security with identifier " + uid);
    }
    s_logger.debug("get-recv {}", message);
    return getFudgeDeserializationContext().fudgeMsgToObject(SecurityDocument.class, message);
  }

  @Override
  public SecurityDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    final RestTarget target = _targetSecurity.resolve(objectId.getObjectId().toString());
    if (versionCorrection != null && versionCorrection.getVersionAsOf() != null) {
      target.resolveQuery("versionAsOf", Collections.singletonList(versionCorrection.getVersionAsOf().toString()));
    }
    if (versionCorrection != null && versionCorrection.getCorrectedTo() != null) {
      target.resolveQuery("correctedTo", Collections.singletonList(versionCorrection.getCorrectedTo().toString()));
    }
    s_logger.debug("get-get to {}", target);
    final FudgeFieldContainer message = getRestClient().getMsg(target);
    if (message == null) {
      s_logger.debug("get-recv NULL");
      throw new DataNotFoundException("Security with identifier " + objectId);
    }
    s_logger.debug("get-recv {}", message);
    return getFudgeDeserializationContext().fudgeMsgToObject(SecurityDocument.class, message);
  }

  @Override
  public void remove(UniqueIdentifier uid) {
    final RestTarget target = _targetSecurity.resolve(uid.toString());
    s_logger.debug("remove-post to {}", target);
    getRestClient().delete(target);
  }

  @Override
  public SecuritySearchResult search(SecuritySearchRequest request) {
    // POST is wrong; but is easy to write if we have a "request" document
    final FudgeFieldContainer payload = getFudgeSerializationContext().objectToFudgeMsg(request);
    s_logger.debug("search-post {} to {}", payload, _targetSearch);
    final FudgeMsgEnvelope env = getRestClient().post(_targetSearch, payload);
    if (env == null) {
      s_logger.debug("search-recv NULL");
      return null;
    }
    s_logger.debug("search-recv {}", env.getMessage());
    return getFudgeDeserializationContext().fudgeMsgToObject(SecuritySearchResult.class, env.getMessage());
  }

  @Override
  public SecurityHistoryResult history(SecurityHistoryRequest request) {
    // POST is wrong; but is easy to write if we have a "request" document
    final FudgeFieldContainer payload = getFudgeSerializationContext().objectToFudgeMsg(request);
    s_logger.debug("history-post {} to {}", payload, _targetHistoric);
    final FudgeMsgEnvelope env = getRestClient().post(_targetHistoric, payload);
    if (env == null) {
      s_logger.debug("history-recv NULL");
      return null;
    }
    s_logger.debug("history-recv {}", env.getMessage());
    return getFudgeDeserializationContext().fudgeMsgToObject(SecurityHistoryResult.class, env.getMessage());
  }

  @Override
  public SecurityDocument update(SecurityDocument document) {
    final FudgeFieldContainer payload = getFudgeSerializationContext().objectToFudgeMsg(document);
    final RestTarget target = _targetSecurity.resolve(document.getUniqueId().toString());
    s_logger.debug("update-post {} to {}", payload, target);
    final FudgeMsgEnvelope env = getRestClient().post(target, payload);
    if (env == null) {
      s_logger.debug("update-recv NULL");
      return null;
    }
    s_logger.debug("update-recv {}", env.getMessage());
    document.setUniqueId(getFudgeDeserializationContext().fudgeMsgToObject(UniqueIdentifier.class, env.getMessage()));
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public MasterChangeManager changeManager() {
    return _changeManager;
  }

}
