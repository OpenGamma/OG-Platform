/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.rest;

import static com.opengamma.financial.security.rest.SecurityMasterServiceNames.SECURITYMASTER_HISTORIC;
import static com.opengamma.financial.security.rest.SecurityMasterServiceNames.SECURITYMASTER_METADATA;
import static com.opengamma.financial.security.rest.SecurityMasterServiceNames.SECURITYMASTER_SEARCH;
import static com.opengamma.financial.security.rest.SecurityMasterServiceNames.SECURITYMASTER_SECURITY;

import java.util.Collections;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.IdUtils;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMetaDataRequest;
import com.opengamma.master.security.SecurityMetaDataResult;
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
  private final RestTarget _targetMetaData;
  private final RestTarget _targetSearch;
  private final RestTarget _targetHistoric;
  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @param baseTarget  the RESTful target, not null
   */
  public RemoteSecurityMaster(FudgeContext fudgeContext, RestTarget baseTarget) {
    this(fudgeContext, baseTarget, new BasicChangeManager());
  }

  /**
   * Creates an instance.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @param baseTarget  the RESTful target, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteSecurityMaster(FudgeContext fudgeContext, RestTarget baseTarget, ChangeManager changeManager) {
    ArgumentChecker.notNull(changeManager, "changeManager");
    _fudgeContext = fudgeContext;
    _targetSecurity = baseTarget.resolveBase(SECURITYMASTER_SECURITY);
    _targetMetaData = baseTarget.resolve(SECURITYMASTER_METADATA);
    _targetSearch = baseTarget.resolve(SECURITYMASTER_SEARCH);
    _targetHistoric = baseTarget.resolve(SECURITYMASTER_HISTORIC);
    _restClient = RestClient.getInstance(fudgeContext, null);
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected FudgeSerializer getFudgeSerializer() {
    return new FudgeSerializer(getFudgeContext());
  }

  protected FudgeDeserializer getFudgeDeserializer() {
    return new FudgeDeserializer(getFudgeContext());
  }

  protected RestClient getRestClient() {
    return _restClient;
  }

  @Override
  public SecurityDocument add(final SecurityDocument document) {
    final FudgeMsg payload = getFudgeSerializer().objectToFudgeMsg(document);
    s_logger.debug("add-post {} to {}", payload, _targetSecurity);
    final FudgeMsgEnvelope env = getRestClient().post(_targetSecurity, payload);
    if (env == null) {
      s_logger.debug("add-recv NULL");
      return null;
    }
    s_logger.debug("add-recv {}", env.getMessage());
    final UniqueId uid = getFudgeDeserializer().fudgeMsgToObject(UniqueId.class, env.getMessage());
    if (uid == null) {
      return null;
    }
    document.setUniqueId(uid);
    IdUtils.setInto(document.getSecurity(), uid);
    return document;
  }

  @Override
  public SecurityDocument correct(SecurityDocument document) {
    final FudgeMsg payload = getFudgeSerializer().objectToFudgeMsg(document);
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
  public SecurityDocument get(UniqueId uid) {
    final RestTarget target = _targetSecurity.resolve(uid.toString());
    s_logger.debug("get-get to {}", target);
    final FudgeMsg message = getRestClient().getMsg(target);
    if (message == null) {
      s_logger.debug("get-recv NULL");
      throw new DataNotFoundException("Security with identifier " + uid);
    }
    s_logger.debug("get-recv {}", message);
    return getFudgeDeserializer().fudgeMsgToObject(SecurityDocument.class, message);
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
    final FudgeMsg message = getRestClient().getMsg(target);
    if (message == null) {
      s_logger.debug("get-recv NULL");
      throw new DataNotFoundException("Security with identifier " + objectId);
    }
    s_logger.debug("get-recv {}", message);
    return getFudgeDeserializer().fudgeMsgToObject(SecurityDocument.class, message);
  }

  @Override
  public void remove(UniqueId uid) {
    final RestTarget target = _targetSecurity.resolve(uid.toString());
    s_logger.debug("remove-post to {}", target);
    getRestClient().delete(target);
  }

  @Override
  public SecurityMetaDataResult metaData(SecurityMetaDataRequest request) {
    // POST is wrong; but is easy to write if we have a "request" document
    final FudgeMsg payload = getFudgeSerializer().objectToFudgeMsg(request);
    s_logger.debug("metadata-post {} to {}", payload, _targetMetaData);
    final FudgeMsgEnvelope env = getRestClient().post(_targetMetaData, payload);
    if (env == null) {
      s_logger.debug("metadata-recv NULL");
      return null;
    }
    s_logger.debug("metadata-recv {}", env.getMessage());
    return getFudgeDeserializer().fudgeMsgToObject(SecurityMetaDataResult.class, env.getMessage());
  }

  @Override
  public SecuritySearchResult search(SecuritySearchRequest request) {
    // POST is wrong; but is easy to write if we have a "request" document
    final FudgeMsg payload = getFudgeSerializer().objectToFudgeMsg(request);
    s_logger.debug("search-post {} to {}", payload, _targetSearch);
    final FudgeMsgEnvelope env = getRestClient().post(_targetSearch, payload);
    if (env == null) {
      s_logger.debug("search-recv NULL");
      return null;
    }
    s_logger.debug("search-recv {}", env.getMessage());
    return getFudgeDeserializer().fudgeMsgToObject(SecuritySearchResult.class, env.getMessage());
  }

  @Override
  public SecurityHistoryResult history(SecurityHistoryRequest request) {
    // POST is wrong; but is easy to write if we have a "request" document
    final FudgeMsg payload = getFudgeSerializer().objectToFudgeMsg(request);
    s_logger.debug("history-post {} to {}", payload, _targetHistoric);
    final FudgeMsgEnvelope env = getRestClient().post(_targetHistoric, payload);
    if (env == null) {
      s_logger.debug("history-recv NULL");
      return null;
    }
    s_logger.debug("history-recv {}", env.getMessage());
    return getFudgeDeserializer().fudgeMsgToObject(SecurityHistoryResult.class, env.getMessage());
  }

  @Override
  public SecurityDocument update(SecurityDocument document) {
    final FudgeMsg payload = getFudgeSerializer().objectToFudgeMsg(document);
    final RestTarget target = _targetSecurity.resolve(document.getUniqueId().toString());
    s_logger.debug("update-post {} to {}", payload, target);
    final FudgeMsgEnvelope env = getRestClient().post(target, payload);
    if (env == null) {
      s_logger.debug("update-recv NULL");
      return null;
    }
    s_logger.debug("update-recv {}", env.getMessage());
    document.setUniqueId(getFudgeDeserializer().fudgeMsgToObject(UniqueId.class, env.getMessage()));
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
