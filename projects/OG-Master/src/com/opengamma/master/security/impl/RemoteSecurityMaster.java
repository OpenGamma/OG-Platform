/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import java.net.URI;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.impl.AbstractRemoteMaster;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMetaDataRequest;
import com.opengamma.master.security.SecurityMetaDataResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides access to a remote {@link SecurityMaster}.
 */
public class RemoteSecurityMaster extends AbstractRemoteMaster implements SecurityMaster {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteSecurityMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteSecurityMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityMetaDataResult metaData(SecurityMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    String msgBase64 = getRestClient().encodeBean(request);
    URI uri = DataSecuritiesResource.uriMetaData(getBaseUri(), msgBase64);
    return accessRemote(uri).get(SecurityMetaDataResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecuritySearchResult search(final SecuritySearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    String msgBase64 = getRestClient().encodeBean(request);
    URI uri = DataSecuritiesResource.uri(getBaseUri(), msgBase64);
    return accessRemote(uri).get(SecuritySearchResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    if (uniqueId.isVersioned()) {
      URI uri = DataSecurityResource.uriVersion(getBaseUri(), uniqueId);
      return accessRemote(uri).get(SecurityDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    
    URI uri = DataSecurityResource.uri(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(SecurityDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument add(final SecurityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSecurity(), "document.security");
    
    URI uri = DataSecuritiesResource.uri(getBaseUri(), null);
    return accessRemote(uri).post(SecurityDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument update(final SecurityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSecurity(), "document.security");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    URI uri = DataSecurityResource.uri(getBaseUri(), document.getUniqueId(), VersionCorrection.LATEST);
    return accessRemote(uri).put(SecurityDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    URI uri = DataSecurityResource.uri(getBaseUri(), uniqueId, VersionCorrection.LATEST);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityHistoryResult history(final SecurityHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    
    String msgBase64 = getRestClient().encodeBean(request);
    URI uri = DataSecurityResource.uriVersions(getBaseUri(), request.getObjectId(), msgBase64);
    return accessRemote(uri).get(SecurityHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument correct(final SecurityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSecurity(), "document.security");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    URI uri = DataSecurityResource.uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(uri).get(SecurityDocument.class);
  }

}
