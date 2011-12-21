/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import java.net.URI;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
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
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.FudgeRestClient;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * Provides access to a remote {@link SecurityMaster}.
 */
public class RemoteSecurityMaster implements SecurityMaster {

  /**
   * The base URI to call.
   */
  private final URI _baseUri;
  /**
   * The client API.
   */
  private final FudgeRestClient _client;
  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteSecurityMaster(final URI baseUri) {
    this(baseUri, new BasicChangeManager());
  }

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteSecurityMaster(final URI baseUri, ChangeManager changeManager) {
    ArgumentChecker.notNull(baseUri, "baseUri");
    ArgumentChecker.notNull(changeManager, "changeManager");
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityMetaDataResult metaData(SecurityMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    String msgBase64 = _client.encodeBean(request);
    URI uri = DataSecuritiesResource.uriMetaData(_baseUri, msgBase64);
    return accessRemote(uri).get(SecurityMetaDataResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecuritySearchResult search(final SecuritySearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    String msgBase64 = _client.encodeBean(request);
    URI uri = DataSecuritiesResource.uri(_baseUri, msgBase64);
    return accessRemote(uri).get(SecuritySearchResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    if (uniqueId.isVersioned()) {
      URI uri = DataSecurityResource.uriVersion(_baseUri, uniqueId);
      return accessRemote(uri).get(SecurityDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    
    URI uri = DataSecurityResource.uri(_baseUri, objectId, versionCorrection);
    return accessRemote(uri).get(SecurityDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument add(final SecurityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSecurity(), "document.security");
    
    URI uri = DataSecuritiesResource.uri(_baseUri, null);
    return accessRemote(uri).post(SecurityDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument update(final SecurityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSecurity(), "document.security");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    URI uri = DataSecurityResource.uri(_baseUri, document.getUniqueId(), VersionCorrection.LATEST);
    return accessRemote(uri).put(SecurityDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    URI uri = DataSecurityResource.uri(_baseUri, uniqueId, VersionCorrection.LATEST);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityHistoryResult history(final SecurityHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    
    String msgBase64 = _client.encodeBean(request);
    URI uri = DataSecurityResource.uriVersions(_baseUri, request.getObjectId(), msgBase64);
    return accessRemote(uri).get(SecurityHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument correct(final SecurityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSecurity(), "document.security");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    URI uri = DataSecurityResource.uriVersion(_baseUri, document.getUniqueId());
    return accessRemote(uri).get(SecurityDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  //-------------------------------------------------------------------------
  /**
   * Accesses the remote master.
   * 
   * @param uri  the URI to call, not null
   * @return the resource, suitable for calling get/post/put/delete on, not null
   */
  protected Builder accessRemote(URI uri) {
    // TODO: Better solution to this limitation in JAX-RS (we shouldn't have "data" in URI)
    // this code removes a second duplicate "data"
    String uriStr = uri.toString();
    int pos = uriStr.indexOf("/jax/data/");
    if (pos > 0) {
      pos = uriStr.indexOf("/data/", pos + 10);
      if (pos > 0) {
        uriStr = uriStr.substring(0, pos) + uriStr.substring(pos + 5);
      }
    }
    uri = URI.create(uriStr);
    return _client.access(uri).type(FudgeRest.MEDIA_TYPE).accept(FudgeRest.MEDIA_TYPE);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of this master.
   * 
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + _baseUri + "]";
  }

}
