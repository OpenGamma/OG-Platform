/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import java.net.URI;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMetaDataRequest;
import com.opengamma.master.config.ConfigMetaDataResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.impl.AbstractRemoteMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides access to a remote {@link ConfigMaster}.
 */
public class RemoteConfigMaster extends AbstractRemoteMaster implements ConfigMaster {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteConfigMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteConfigMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigMetaDataResult metaData(ConfigMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    String msgBase64 = getRestClient().encodeBean(request);
    URI uri = DataConfigsResource.uriMetaData(getBaseUri(), msgBase64);
    return accessRemote(uri).get(ConfigMetaDataResult.class);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigSearchResult<T> search(final ConfigSearchRequest<T> request) {
    ArgumentChecker.notNull(request, "request");
    
    String msgBase64 = getRestClient().encodeBean(request);
    URI uri = DataConfigsResource.uri(getBaseUri(), msgBase64);
    return accessRemote(uri).get(ConfigSearchResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<?> get(final UniqueId uniqueId) {
    return get(uniqueId, (Class<?>) null);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> get(UniqueId uniqueId, Class<T> clazz) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    if (uniqueId.isVersioned()) {
      URI uri = DataConfigResource.uriVersion(getBaseUri(), uniqueId, clazz);
      return accessRemote(uri).get(ConfigDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST, null);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<?> get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    return get(objectId, versionCorrection, null);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> get(ObjectIdentifiable objectId, VersionCorrection versionCorrection, Class<T> clazz) {
    ArgumentChecker.notNull(objectId, "objectId");
    
    URI uri = DataConfigResource.uri(getBaseUri(), objectId, versionCorrection, clazz);
    return accessRemote(uri).get(ConfigDocument.class);
  }


  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> add(final ConfigDocument<T> document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getValue(), "document.config");
    
    URI uri = DataConfigsResource.uri(getBaseUri(), null);
    return accessRemote(uri).post(ConfigDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> update(final ConfigDocument<T> document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getValue(), "document.config");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    URI uri = DataConfigResource.uri(getBaseUri(), document.getUniqueId(), VersionCorrection.LATEST, null);
    return accessRemote(uri).put(ConfigDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    URI uri = DataConfigResource.uri(getBaseUri(), uniqueId, VersionCorrection.LATEST, null);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigHistoryResult<T> history(final ConfigHistoryRequest<T> request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    
    String msgBase64 = getRestClient().encodeBean(request);
    URI uri = DataConfigResource.uriVersions(getBaseUri(), request.getObjectId(), msgBase64);
    return accessRemote(uri).get(ConfigHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> correct(final ConfigDocument<T> document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getValue(), "document.config");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    URI uri = DataConfigResource.uriVersion(getBaseUri(), document.getUniqueId(), null);
    return accessRemote(uri).get(ConfigDocument.class);
  }

}
