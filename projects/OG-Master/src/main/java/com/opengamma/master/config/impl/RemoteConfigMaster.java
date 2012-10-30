/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import java.net.URI;
import java.util.List;

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
import com.opengamma.master.impl.AbstractRemoteDocumentMaster;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.api.client.GenericType;

/**
 * Provides access to a remote {@link ConfigMaster}.
 */
public class RemoteConfigMaster
    extends AbstractRemoteDocumentMaster<ConfigDocument>
    implements ConfigMaster {

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
    
    URI uri = DataConfigMasterResource.uriMetaData(getBaseUri(), request);
    return accessRemote(uri).get(ConfigMetaDataResult.class);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public <R> ConfigSearchResult<R> search(final ConfigSearchRequest<R> request) {
    ArgumentChecker.notNull(request, "request");

    URI uri = DataConfigMasterResource.uriSearch(getBaseUri());
    return accessRemote(uri).post(ConfigSearchResult.class, request);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    if (uniqueId.isVersioned()) {
      URI uri = DataConfigResource.uriVersion(getBaseUri(), uniqueId);
      return accessRemote(uri).get(ConfigDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");

    URI uri = DataConfigResource.uri(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(ConfigDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument add(final ConfigDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getConfig(), "document.config");

    URI uri = DataConfigMasterResource.uriAdd(getBaseUri());
    return accessRemote(uri).post(ConfigDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument update(final ConfigDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getConfig(), "document.config");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = DataConfigResource.uri(getBaseUri(), document.getUniqueId(), null);
    return accessRemote(uri).post(ConfigDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");

    URI uri = DataConfigResource.uri(getBaseUri(), objectIdentifiable, null);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public <R> ConfigHistoryResult<R> history(final ConfigHistoryRequest<R> request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");

    URI uri = DataConfigResource.uriVersions(getBaseUri(), request.getObjectId(), request);
    return accessRemote(uri).get(ConfigHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument correct(final ConfigDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getConfig(), "document.config");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = DataConfigResource.uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(uri).post(ConfigDocument.class, document);
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<ConfigDocument> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (ConfigDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "replacementDocument");
      ArgumentChecker.notNull(replacementDocument.getConfig(), "replacementDocument.config");
    }

    URI uri = DataConfigResource.uriVersion(getBaseUri(), uniqueId);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<ConfigDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (ConfigDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "replacementDocument");
      ArgumentChecker.notNull(replacementDocument.getConfig(), "replacementDocument.config");
    }
    URI uri = DataConfigResource.uriAll(getBaseUri(), objectId, null, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<ConfigDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (ConfigDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "replacementDocument");
      ArgumentChecker.notNull(replacementDocument.getConfig(), "replacementDocument.config");
    }
    URI uri = DataConfigResource.uri(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }
}
